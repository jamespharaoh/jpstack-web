#!/usr/bin/env runhaskell

{-# LANGUAGE Arrows, NoMonomorphismRestriction #-}

module Main where

import           Control.Monad
import           Control.Monad.IO.Class

import qualified Data.ByteString.Char8 as Char8
import qualified Data.IORef as IORef
import qualified Data.List as List
import qualified Data.List.Split as Split
import qualified Data.Map.Strict as Map
import qualified Data.Maybe as Maybe
import           Data.Tagged
import qualified Data.Text as Text

-- import qualified Git
import qualified Git.Libgit2
import qualified Git.Reference
import qualified Git.Repository
import qualified Git.Types
import qualified Git.Tree

import qualified System.Environment as Environment

import           Text.XML.HXT.Core

type TreeEntries r =
	[(Char8.ByteString, Git.Types.TreeEntry r)]

type CacheRef r =
	IORef.IORef (Cache r)

entriesToTree ::
	(Git.Types.MonadGit r m, MonadIO m) =>
	CacheRef r ->
	TreeEntries r ->
	m (Git.Types.Tree r)
entriesToTree cacheRef entries = do

	let pathSplit path =
		Split.splitOn "/" (Char8.unpack path)

	let entriesTemp =
		foldl addEntry Map.empty entries

		where addEntry map (path, entry) = let

			pathParts =
				pathSplit path

			firstPart =
				head pathParts

			remainingParts =
				tail pathParts

			remainingPathString =
				List.intercalate "/" remainingParts

			remainingPath =
				Char8.pack remainingPathString

			in case (length pathParts) of

				1 ->
					Map.insert firstPart (Left (path, entry)) map

				_ ->
					Map.alter mergeEntries firstPart map where

						mergeEntries Nothing =
							Just $ Right $
								[ (remainingPath, entry) ]

						mergeEntries (Just (Right list)) =
							Just $ Right $
								((remainingPath, entry) : list)

	let

		makeSubEntry (_path, Left (path, entry)) = do
			return (path, entry)

		makeSubEntry (path, Right entries) = do

			subTree <-
				lookupBuiltTree cacheRef entries

			subTreeOid <-
				Git.Types.treeOid subTree

			let subTreeEntry =
				Git.Types.TreeEntry {
					Git.Types.treeEntryOid = subTreeOid }

			return ((Char8.pack path), subTreeEntry)

	newEntries <-
		mapM makeSubEntry (Map.toAscList entriesTemp)

	treeBuilder <-
		Git.Types.newTreeBuilder Nothing

	let addEntry treeBuilder (path, entry) = do

		modifiedTree <-
			Git.Types.mtbPutEntry treeBuilder treeBuilder path entry

		return $
			Git.Types.fromBuilderMod modifiedTree

	modifiedBuilder <-
		foldM addEntry treeBuilder newEntries

	(_, newTreeOid) <-
		Git.Types.mtbWriteContents modifiedBuilder modifiedBuilder

	newTree <-
		loadTree cacheRef newTreeOid

	return newTree

treeLookup ::
	(Git.Types.MonadGit r m, MonadIO m) =>
	CacheRef r ->
	Git.Types.Tree r ->
	Char8.ByteString ->
	m (Maybe (Git.Types.TreeEntry r))

treeLookup cacheRef tree path = do

	treeBuilder <-
		Git.Types.newTreeBuilder (Just tree)

	let pathParts =
		Split.splitOn "/" (Char8.unpack path)

	case pathParts of

		[ singlePathPart ] ->

			Git.Types.mtbLookupEntry
				treeBuilder
				path

		(firstPathPart : restOfPathParts) -> do

			maybeSubTreeEntry <-
				Git.Types.mtbLookupEntry treeBuilder (Char8.pack firstPathPart)

			case maybeSubTreeEntry of

				Nothing ->
					return Nothing

				_ -> do

					let Just subTreeEntry =
						maybeSubTreeEntry

					let subTreeOid =
						Git.Types.treeEntryToOid subTreeEntry

					subTree <-
						loadTree cacheRef (Tagged subTreeOid)

					let restOfPath =
						Char8.pack $
							List.intercalate "/" restOfPathParts

					treeLookup cacheRef subTree restOfPath

reduceTree ::
	(Git.Types.MonadGit r m, MonadIO m) =>
	IORef.IORef (Cache r) ->
	[Char8.ByteString] ->
	Git.Types.Tree r ->
	m (Git.Types.Tree r)
reduceTree cacheRef paths tree = do

	treeBuilder <-
		Git.Types.newTreeBuilder (Just tree)

	let lookupOne path = do

		treeEntryMaybe <-
			treeLookup cacheRef tree path

		return (case treeEntryMaybe of

			Nothing ->
				Nothing

			Just treeEntry ->
				Just (path, treeEntry))

	filteredTreeEntriesTemp <-
		mapM lookupOne paths

	let filteredTreeEntries =
		Maybe.catMaybes filteredTreeEntriesTemp

	reducedTree <-
		lookupBuiltTree cacheRef filteredTreeEntries

	return reducedTree

reduceCommit ::
	(Git.Types.MonadGit r m, MonadIO m) =>
	IORef.IORef (Cache r) ->
	[Char8.ByteString] ->
	Git.Types.Commit r ->
	m (Git.Types.Commit r)
reduceCommit cacheRef paths commit = do

	fullTree <-
		loadTree cacheRef $
			Git.Types.commitTree commit

	reducedTree <-
		reduceTree cacheRef paths fullTree

	reducedTreeOid <-
		Git.Types.treeOid reducedTree

	let parentCommitOids =
		Git.Types.commitParents commit

	parentCommits <-
		mapM (loadCommit cacheRef) parentCommitOids

	reducedParentCommits <-
		mapM (reduceCommit cacheRef paths) parentCommits

	let reduceToParent =

		case reducedParentCommits of

			[oneReducedParentCommit] -> do

				reducedParentTree <-
					loadTree cacheRef $
						Git.Types.commitTree oneReducedParentCommit

				reducedParentTreeOid <-
					Git.Types.treeOid reducedParentTree

				return (if reducedParentTreeOid == reducedTreeOid
					then Just oneReducedParentCommit
					else Nothing)

			_ ->

				return Nothing

	reducedMaybe <-
		reduceToParent

	case reducedMaybe of

		Just reduced ->
			return reduced

		_ -> do

			let reducedParentCommitIds =
				map Git.Types.commitOid reducedParentCommits

			reducedTreeOid <-
				Git.Types.treeOid reducedTree

			reducedCommit <-
				Git.Types.createCommit
					reducedParentCommitIds
					reducedTreeOid
					(Git.Types.commitAuthor commit)
					(Git.Types.commitCommitter commit)
					(Git.Types.commitLog commit)
					Nothing

			-- liftIO $ putStrLn "finish"

			return reducedCommit

---------- cache

data Cache r = Cache {

	cacheCommits ::
		Map.Map
		(Git.Types.CommitOid r)
		(Git.Types.Commit r),

	cacheTrees ::
		Map.Map
		(Git.Types.TreeOid r)
		(Git.Types.Tree r),

	cacheBuiltTrees ::
		Map.Map
		[(Char8.ByteString, Git.Types.Oid r)]
		(Git.Types.Tree r)

}

createCache = do

	liftIO $ IORef.newIORef Cache {

		cacheCommits =
			Map.empty,

		cacheTrees =
			Map.empty,

		cacheBuiltTrees =
			Map.empty

	}

loadCached ::
	(Git.Types.MonadGit r m, MonadIO m, Ord k') =>
	(Cache r -> Map.Map k' a) ->
	(Cache r -> Map.Map k' a -> Cache r) ->
	(k -> k') ->
	(k -> m a) ->
	IORef.IORef (Cache r) ->
	k ->
	m (a)

loadCached getMap setMap mungeKey lookup cacheRef id = do

	cache <-
		liftIO $ IORef.readIORef cacheRef

	let map =
		getMap cache

	let maybeValue =
		Map.lookup (mungeKey id) map

	case maybeValue of

		Just value -> do

			return value

		Nothing -> do

			value <-
				lookup id

			let map' =
				Map.insert (mungeKey id) value map

			let cache' =
				setMap cache map'

			liftIO $ IORef.writeIORef cacheRef cache'

			return value

loadTree ::
	(Git.Types.MonadGit r m, MonadIO m) =>
	IORef.IORef (Cache r) ->
	Git.Types.TreeOid r ->
	m (Git.Types.Tree r)

loadTree cacheRef treeOid = do

	loadCached
		cacheTrees
		(\cache map -> cache { cacheTrees = map })
		id
		Git.Types.lookupTree
		cacheRef
		treeOid

loadCommit ::
	(Git.Types.MonadGit r m, MonadIO m) =>
	IORef.IORef (Cache r) ->
	Git.Types.CommitOid r ->
	m (Git.Types.Commit r)

loadCommit cacheRef commitOid = do

	loadCached
		cacheCommits
		(\cache map -> cache { cacheCommits = map })
		id
		Git.Types.lookupCommit
		cacheRef
		commitOid

lookupBuiltTree ::
	(Git.Types.MonadGit r m, MonadIO m) =>
	IORef.IORef (Cache r) ->
	TreeEntries r ->
	m (Git.Types.Tree r)

lookupBuiltTree cacheRef entries = do

	loadCached
		cacheBuiltTrees
		(\cache map -> cache { cacheBuiltTrees = map })
		(map (\(path, entry) -> (path, Git.Types.treeEntryToOid entry)))
		(entriesToTree cacheRef)
		cacheRef
		entries

---------- main

withGit path =
	Git.Repository.withRepository
		Git.Libgit2.lgFactory
		path

data GitLink = GitLink {
	gitLinkName :: String,
	gitLinkSource :: String,
	gitLinkTarget :: String,
	gitLinkPaths :: [String]
}

data Config = Config {
	configGitLinks :: [GitLink]
}

readConfig = do

	let parseXML file =
		readDocument [] file

	let atTag tag =
		deep (isElem >>> hasName tag)

	let getPaths =
		atTag "path" >>> proc pathTag -> do

			name <- getAttrValue "name" -< pathTag

			returnA -< name

	let getGitLink =
		atTag "git-link" >>> proc gitLinkTag -> do

			name <- getAttrValue "name" -< gitLinkTag
			source <- getAttrValue "source" -< gitLinkTag
			target <- getAttrValue "target" -< gitLinkTag
			paths <- listA getPaths -< gitLinkTag

			returnA -< GitLink {
				gitLinkName = name,
				gitLinkSource = source,
				gitLinkTarget = target,
				gitLinkPaths = paths
			}

	let getConfig =
		atTag "wbs-build" >>> proc buildTag -> do

			gitLinksTag <- atTag "git-links" -< buildTag
			gitLinks <- listA getGitLink -< gitLinksTag

			returnA -< Config {
				configGitLinks = gitLinks
			}

--	deep (isElem >>> hasName "git-link") >>>
--		proc l -> do

--			name <- getAttrValue "link-name" -< l
--			remoteRepo <- getAttrValue "remote-repo" -< l
--			remoteBranch <- getAttrValue "remote-branch" -< l
--			localBranch <- getAttrValue "local-branch" -< l

--			paths <-

	[ config ] <-
		runX (parseXML "wbs-build.xml" >>> getConfig)

	return config

main ::
	IO ()

main = do

	config <-
		readConfig

	arguments <-
		Environment.getArgs

	let [ gitLinkNameArg ] =
		arguments

	let Just gitLink =
		List.find
			(\gitLink -> (gitLinkName gitLink) == gitLinkNameArg)
			(configGitLinks config)

	name <- withGit "." $ do

		cacheRef <-
			createCache

		let linkSource =
			gitLinkSource gitLink

		sourceOidUntaggedMaybe <-
			Git.Reference.resolveReference $
				Text.pack linkSource

		case sourceOidUntaggedMaybe of

			Nothing ->
				error $ "error resolving source reference " ++ linkSource

			Just sourceOidUntagged -> do

				let sourceOid =
					Tagged sourceOidUntagged

				sourceCommit <-
					loadCommit cacheRef sourceOid

				let paths =
					map Char8.pack $
						gitLinkPaths gitLink

				reducedCommit <-
					reduceCommit cacheRef paths sourceCommit

				let reducedCommitOid =
					Git.Types.commitOid reducedCommit

				Git.Types.updateReference
					(Text.pack $ gitLinkTarget gitLink)
					(Git.Types.RefObj (untag reducedCommitOid))

				return $ show $ untag reducedCommitOid

	putStrLn name
