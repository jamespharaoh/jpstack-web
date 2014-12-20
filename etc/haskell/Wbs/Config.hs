{-# LANGUAGE Arrows, NoMonomorphismRestriction #-}

module Wbs.Config where

import           Control.Monad (foldM)

import           Data.String.Utils (replace)
import           Data.Maybe

import           Text.XML.HXT.Core

-------------------- records

data BuildPluginConfig =

	BuildPluginConfig {
		bpcName :: String,
		bpcPackage :: String
	}

data BuildGitLinkConfig =

	BuildGitLinkConfig {
		bglcName :: String,
		bglcSource :: String,
		bglcLocal :: String,
		bglcTarget :: String,
		bglcPaths :: [ String ]
	}

data BuildConfig =

	BuildConfig {
		bcName :: String,
		bcPlugins :: [ BuildPluginConfig ],
		bcGitLinks :: [ BuildGitLinkConfig ]
	}

data PluginConfig =

	PluginConfig {
		plcName :: String,
		plcPackage :: String,
		plcSqlSchemas :: [ String ],
		plcSqlDatas :: [ String ]
	}

data WorldConfig =

	WorldConfig {
		wcBuild :: BuildConfig,
		wcPlugins :: [ PluginConfig ]
	}

-------------------- misc

parseXML file =
	readDocument [] file

atTag tag =
	getChildren >>> isElem >>> hasName tag

optTag tag =
	atTag tag `orElse` mkelem "tag" [] []

getAttrArray elemName attrName =
	listA $ atTag elemName >>> proc parent -> do
		name <- getAttrValue attrName -< parent
		returnA -< name

-------------------- loadBuildConfig

loadBuildConfig ::
	IO (BuildConfig)

loadBuildConfig = do

	let getPlugins =
		atTag "plugin" >>> proc pluginTag -> do

			name <- getAttrValue "name" -< pluginTag
			package <- getAttrValue "package" -< pluginTag

			returnA -< BuildPluginConfig {
				bpcName = name,
				bpcPackage = package
			}

	let getGitLink =
		atTag "git-link" >>> proc gitLinkTag -> do

			name <- getAttrValue "name" -< gitLinkTag
			source <- getAttrValue "source" -< gitLinkTag
			target <- getAttrValue "target" -< gitLinkTag
			local <- getAttrValue "local" -< gitLinkTag

			paths <- getAttrArray "path" "name" -< gitLinkTag

			returnA -< BuildGitLinkConfig {
				bglcName = name,
				bglcSource = source,
				bglcTarget = target,
				bglcLocal = local,
				bglcPaths = paths
			}

	let getBuildConfig =
		atTag "wbs-build" >>> proc buildTag -> do

			name <- getAttrValue "name" -< buildTag

			pluginsTag <- atTag "plugins" -< buildTag
			plugins <- listA getPlugins -< pluginsTag

			gitLinksTag <- atTag "git-links" -< buildTag
			gitLinks <- listA getGitLink -< gitLinksTag

			returnA -< BuildConfig {
				bcName = name,
				bcPlugins = plugins,
				bcGitLinks = gitLinks
			}

	[ buildConfig ] <-
		runX (parseXML "wbs-build.xml" >>> getBuildConfig)

	return buildConfig

-------------------- loadPluginConfig

loadPluginConfig ::
	BuildConfig ->
	BuildPluginConfig ->
	IO (PluginConfig)

loadPluginConfig buildConfig buildPluginConfig = do

	let getPluginConfig =
		atTag "plugin" >>> proc pluginTag -> do

			name <- getAttrValue "name" -< pluginTag
			package <- getAttrValue "package" -< pluginTag

			sqlScriptsTag <- optTag "sql-scripts" -< pluginTag
			sqlSchemas <- getAttrArray "sql-schema" "name" -< sqlScriptsTag
			sqlDatas <- getAttrArray "sql-data" "name" -< sqlScriptsTag

			returnA -< PluginConfig {
				plcName = name,
				plcPackage = package,
				plcSqlSchemas = sqlSchemas,
				plcSqlDatas = sqlDatas
			}

	let pluginConfigPath =
		"src/" ++
		(replace "." "/" $ bpcPackage buildPluginConfig) ++
		"/" ++
		(bpcName buildPluginConfig) ++
		"-plugin.xml"

	[ pluginConfig ] <-
		runX (parseXML pluginConfigPath >>> getPluginConfig)

	return pluginConfig

-------------------- loadWorld

loadWorld ::
	IO (WorldConfig)

loadWorld = do

	-- load build config

	buildConfig <-
		loadBuildConfig

	-- load plugin configs

	pluginConfigs <-
		mapM (loadPluginConfig buildConfig) $
			bcPlugins buildConfig

	return WorldConfig {
		wcBuild = buildConfig,
		wcPlugins = pluginConfigs
	}
