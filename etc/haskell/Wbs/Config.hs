{-# LANGUAGE Arrows, NoMonomorphismRestriction #-}

module Wbs.Config where

import           Control.Monad (foldM)

import           Data.String.Utils (replace)
import           Data.Maybe

import           Text.XML.HXT.Core

-------------------- records

data BuildProjectConfig =

	BuildProjectConfig {
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
		bcProjects :: [ BuildProjectConfig ],
		bcGitLinks :: [ BuildGitLinkConfig ]
	}

data ProjectPluginConfig =

	ProjectPluginConfig {
		ppcName :: String,
		ppcPackage :: String
	}

data ProjectConfig =

	ProjectConfig {
		prcName :: String,
		prcPackage :: String,
		prcPlugins :: [ ProjectPluginConfig ]
	}

data PluginConfig =

	PluginConfig {
		plcName :: String,
		plcPackage :: String,
		plcSqlSchemas :: [ String ],
		plcSqlDatas :: [ String ]
	}

{-
<plugin
	name="menu"
	package="menu">

	<dependencies>
		<project name="wbs-platform">
			<plugin name="platform-common"/>
		</project>
	</dependencies>

	<sql-scripts>
		<sql-schema name="menu"/>
		<sql-data name="menu-data"/>
	</sql-scripts>

	<models>
		<model name="menu"/>
		<model name="menuGroup"/>
	</models>

	<fixtures>
		<fixture name="menu"/>
	</fixtures>

	<console-modules>
		<console-module name="menu"/>
		<console-module name="menu-group"/>
	</console-modules>

</plugin>
-}

data WorldConfig =

	WorldConfig {
		wcBuild :: BuildConfig,
		wcProjects :: [ ProjectConfig ],
		wcProjectsAndPlugins :: [ (ProjectConfig, PluginConfig) ]
	}

-------------------- misc

parseXML file =
	readDocument [] file

atTag tag =
	getChildren >>> isElem >>> hasName tag

getAttrArray elemName attrName =
	atTag elemName >>> proc parent -> do

		name <- getAttrValue attrName -< parent

		returnA -< name

-------------------- loadBuildConfig

loadBuildConfig ::
	IO (BuildConfig)

loadBuildConfig = do

	let getProjects =
		atTag "project" >>> proc projectTag -> do

			name <- getAttrValue "name" -< projectTag
			package <- getAttrValue "package" -< projectTag

			returnA -< BuildProjectConfig {
				bpcName = name,
				bpcPackage = package
			}

	let getGitLink =
		atTag "git-link" >>> proc gitLinkTag -> do

			name <- getAttrValue "name" -< gitLinkTag
			source <- getAttrValue "source" -< gitLinkTag
			target <- getAttrValue "target" -< gitLinkTag
			local <- getAttrValue "local" -< gitLinkTag

			paths <- listA $ getAttrArray "path" "name" -< gitLinkTag

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

			projectsTag <- atTag "projects" -< buildTag
			projects <- listA getProjects -< projectsTag

			gitLinksTag <- atTag "git-links" -< buildTag
			gitLinks <- listA getGitLink -< gitLinksTag

			returnA -< BuildConfig {
				bcName = name,
				bcProjects = projects,
				bcGitLinks = gitLinks
			}

	[ buildConfig ] <-
		runX (parseXML "wbs-build.xml" >>> getBuildConfig)

	return buildConfig

-------------------- loadProjectConfig

loadProjectConfig ::
	BuildConfig ->
	BuildProjectConfig ->
	IO (ProjectConfig)

loadProjectConfig buildConfig buildProjectConfig = do

	let getPlugins =
		atTag "plugin" >>> proc pluginTag -> do

			name <- getAttrValue "name" -< pluginTag
			package <- getAttrValue "package" -< pluginTag

			returnA -< ProjectPluginConfig {
				ppcName = name,
				ppcPackage = package
			}

	let getConfig =
		atTag "project" >>> proc projectTag -> do

			name <- getAttrValue "name" -< projectTag
			package <- getAttrValue "package" -< projectTag

			plugins <- listA getPlugins -< projectTag

			returnA -< ProjectConfig {
				prcName = name,
				prcPackage = package,
				prcPlugins = plugins
			}

	let projectConfigPath =
		"src/" ++
		(replace "." "/" $ bpcPackage buildProjectConfig) ++
		"/" ++
		(bpcName buildProjectConfig) ++
		"-project.xml"

	putStrLn projectConfigPath

	[ projectConfig ] <-
		runX (parseXML projectConfigPath >>> getConfig)

	return projectConfig

-------------------- loadPluginConfig

loadPluginConfig ::
	BuildConfig ->
	ProjectConfig ->
	ProjectPluginConfig ->
	IO (PluginConfig)

loadPluginConfig buildConfig projectConfig projectPluginConfig = do

	let getPluginConfig =
		atTag "plugin" >>> proc pluginTag -> do

			name <-
				getAttrValue "name" -< pluginTag

			package <-
				getAttrValue "package" -< pluginTag

			sqlScriptsTags <- listA $
				atTag "sql-scripts" -< pluginTag

			case sqlScriptsTags of

				[ sqlScriptsTag ] -> do

					sqlSchemas <- listA $
						getAttrArray "sql-schema" "name" -< sqlScriptsTag

					sqlDatas <- listA $
						getAttrArray "sql-data" "name" -< sqlScriptsTag

					returnA -< PluginConfig {
						plcName = name,
						plcPackage = package,
						plcSqlSchemas = sqlSchemas,
						plcSqlDatas = sqlDatas
					}

				_ -> do

					returnA -< PluginConfig {
						plcName = name,
						plcPackage = package,
						plcSqlSchemas = [],
						plcSqlDatas = []
					}

	let pluginConfigPath =
		"src/" ++
		(replace "." "/" $ prcPackage projectConfig) ++
		"/" ++
		(replace "." "/" $ ppcPackage projectPluginConfig) ++
		"/" ++
		(ppcName projectPluginConfig) ++
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

	-- load project configs

	projectConfigs <-
		mapM (loadProjectConfig buildConfig) $ bcProjects buildConfig

	-- load plugin configs

	let loadPlugin' (projectConfig, projectPluginConfig) = do

		pluginConfig <-
			loadPluginConfig buildConfig projectConfig projectPluginConfig

		return (projectConfig, pluginConfig)

	let projectWithPlugins projectConfig =
		zip (repeat projectConfig) (prcPlugins projectConfig)

	projectsAndPlugins <-
		mapM loadPlugin' $
			concat $
				map projectWithPlugins projectConfigs

	return WorldConfig {
		wcBuild = buildConfig,
		wcProjects = projectConfigs,
		wcProjectsAndPlugins = projectsAndPlugins
	}
