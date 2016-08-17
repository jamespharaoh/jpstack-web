module Wbs.Setup.AntProject where

import Data.List
import Data.String.Utils (replace)

import Text.XML.HXT.Core

import Wbs.Config

writeBuildFile :: World -> IO ()
writeBuildFile world = do

	let buildConfig = wldBuild world
	let plugins = wldPlugins world

	let makeProperties =
		[
			mkelem "property" [
				sattr "environment" "env"
			] [],
			mkelem "property" [
				sattr "file" "${env.WBS_BUILD_PROPERTIES}"
			] []
		]

	let makeClasspath =
		[
			mkelem "path" [
				sattr "id" "classpath"
			] [
				mkelem "fileset" [
					sattr "dir" "lib",
					sattr "includes" "*.jar"
				] [],
				mkelem "pathelement" [
					sattr "path" "work/bin"
				] []
			]
		]

	let makeComboTarget name parts =
		mkelem "target" [
			sattr "name" name,
			sattr "depends" $
				intercalate ", " $
					map ("just-" ++) parts
		] []

	let makeComboTargets =
		[

			makeComboTarget "clean" [
				"clean" ],

			makeComboTarget "build" [
				"build-framework",
				"build-meta",
				"generate-records",
				"build-rest" ],

			makeComboTarget "framework-jar" [
				"build-framework",
				"framework-jar" ],

			makeComboTarget "console-live" [
				"build-framework",
				"build-meta",
				"generate-records",
				"build-rest",
				"console-live" ],

			makeComboTarget "console-test" [
				"build-framework",
				"build-meta",
				"generate-records",
				"build-rest",
				"console-test" ],

			makeComboTarget "console-auto" [
				"build-framework",
				"build-meta",
				"generate-records",
				"build-rest",
				"console-live",
				"console-restart" ],

			makeComboTarget "api-live" [
				"build-framework",
				"build-meta",
				"generate-records",
				"build-rest",
				"api-live" ],

			makeComboTarget "api-test" [
				"build-framework",
				"build-meta",
				"generate-records",
				"build-rest",
				"api-test" ],

			makeComboTarget "api-auto" [
				"build-framework",
				"build-meta",
				"generate-records",
				"build-rest",
				"api-live",
				"api-restart" ],

			makeComboTarget "daemon-auto" [
				"build-framework",
				"build-meta",
				"generate-records",
				"build-rest",
				"daemon-restart" ],

			makeComboTarget "all-auto" [
				"build-framework",
				"build-meta",
				"generate-records",
				"build-rest",
				"api-live",
				"api-restart",
				"console-live",
				"console-restart",
				"daemon-restart" ],

			makeComboTarget "javadoc" [
				"javadoc" ],

			makeComboTarget "fixtures" [
				"build-framework",
				"build-meta",
				"generate-records",
				"build-rest",
				"db-drop",
				"db-create",
				"schema-create",
				"sql-schema",
				"sql-data",
				"fixtures" ],

			makeComboTarget "tomcat-test" [
				"build-framework",
				"build-meta",
				"generate-records",
				"build-rest",
				"console-test",
				"api-test",
				"tomcat-test" ],

			makeComboTarget "generate-records" [
				"build-framework",
				"build-meta",
				"generate-records" ]

		]

	let makeSimpleTarget name elems =
		mkelem "target" [
			sattr "name" $ "just-" ++ name
		] elems

	let makeDeleteDir dir =
		mkelem "delete" [
			sattr "dir" dir
		] []

	let makeDeleteDirContents dir =
		mkelem "delete" [
			sattr "includeEmptyDirs" "true",
			sattr "failonerror" "false"
		] [
			mkelem "fileset" [
				sattr "dir" dir,
				sattr "includes" "**/*"
			] []
		]

	let makeMkdir dir =
		mkelem "mkdir" [
			sattr "dir" dir
		] []

	let makeJavacTask attrs elems =
		mkelem "javac" ([
			sattr "destdir" "work/bin",
			sattr "debug" "on",
			sattr "includeantruntime" "false",
			sattr "classpathref" "classpath",
			sattr "encoding" "utf8"
		] ++ attrs) elems

	let makeCleanTargets =
		[
			makeSimpleTarget "clean" [
				makeDeleteDirContents "work/bin",
				makeDeleteDirContents "work/generated",
				makeDeleteDirContents "work/test"
			]
		]

	let makeIncludeName name =
		mkelem "include" [
			sattr "name" name
		] []

	let makeExcludeName name =
		mkelem "exclude" [
			sattr "name" name
		] []

	let makeFilesetDir dir elems =
		mkelem "fileset" [
			sattr "dir" dir
		] elems

	let makeCopyToDir todir elems =
		mkelem "copy" [
			sattr "todir" todir
		] elems

	let makeCopyDirToDir dir todir =
		makeCopyToDir todir [
			makeFilesetDir dir []
		]

	let makeCopyToDirNofail todir elems =
		mkelem "copy" [
			sattr "todir" todir,
			sattr "failonerror" "false"
		] elems

	let makeCopyFileToDir file todir elems =
		mkelem "copy" [
			sattr "file" file,
			sattr "todir" todir
		] elems

	let makeCopyFileToFile file tofile =
		mkelem "copy" [
			sattr "file" file,
			sattr "tofile" tofile
		] []

	let makeCopyFileToFileOverwrite file tofile =
		mkelem "copy" [
			sattr "file" file,
			sattr "tofile" tofile,
			sattr "overwrite" "true"
		] []

	let makeMoveFileToFile file tofile =
		mkelem "move" [
			sattr "file" file,
			sattr "tofile" tofile
		] []

	let makeBuildTargets =
		[

			makeSimpleTarget "build-framework" [
				makeMkdir "work/bin",
				makeJavacTask [] [

					mkelem "include" [
						sattr "name" "wbs/api/**"
					] [],
					mkelem "include" [
						sattr "name" "wbs/console/**"
					] [],
					mkelem "include" [
						sattr "name" "wbs/framework/**"
					] [],

					mkelem "src" [
						sattr "path" "src"
					] []

				]
			],

			makeSimpleTarget "framework-jar" [
				mkelem "jar" [
					sattr "destfile" "work/wbs-framework.jar",
					sattr "basedir" "work/bin"
				] [

					mkelem "include" [
						sattr "name" "wbs/framework/**"
					] [],
					mkelem "include" [
						sattr "name" "wbs/api/**"
					] [],
					mkelem "include" [
						sattr "name" "wbs/console/**"
					] [],

					mkelem "service" [
						sattr "type" "javax.annotation.processing.Processor",
						sattr "provider"
							"wbs.framework.object.ObjectHelperAnnotationProcessor"
					] []

				]
			],

			makeSimpleTarget "build-meta" [
				makeJavacTask [] [

					mkelem "src" [
						sattr "path" "src"
					] [],
					mkelem "src" [
						sattr "path" "work/bin"
					] [],

					mkelem "include" [
						sattr "name" "wbs/framework/**"
					] [],
					mkelem "include" [
						sattr "name" "wbs/api/**"
					] [],
					mkelem "include" [
						sattr "name" "wbs/console/**"
					] [],
					mkelem "include" [
						sattr "name" "wbs/**/generate/**"
					] [],
					mkelem "include" [
						sattr "name" "wbs/**/metamodel/**"
					] []

				],
				makeCopyToDir "work/bin" [
					makeFilesetDir "src" [
						makeIncludeName "**/*.xml",
						makeIncludeName "log4j.properties"
					]
				],
				makeCopyFileToDir "wbs-build.xml" "work/bin" []
			],

			makeSimpleTarget "build-rest" [
				makeJavacTask [] [
					mkelem "src" [
						sattr "path" "src"
					] [],
					mkelem "src" [
						sattr "path" "work/generated"
					] [],
					mkelem "src" [
						sattr "path" "work/bin"
					] []
				],
				makeCopyToDir "work/bin" [
					makeFilesetDir "src" [
						makeIncludeName "**/*.xml",
						makeIncludeName "log4j.properties"
					]
				],
				makeCopyFileToDir "wbs-build.xml" "work/bin" []
			]

		]


	let pluginDir pluginConfig =
		"src/" ++
		(replace "." "/" $ plgPackage pluginConfig)

	let makeWebTarget name env = let

		workDir =
			"work/" ++ env ++ "/" ++ name

		pluginDir pluginConfig =
			"src/" ++
			(replace "." "/" $ plgPackage pluginConfig)

		makeWebFileset plugin = let

			thisPluginDir =
				pluginDir plugin

			in makeFilesetDir (
				if isInfixOf ("/" ++ name ++ "/") thisPluginDir
				then (
					(pluginDir plugin) ++
					"/files"
				) else (
					(pluginDir plugin) ++
					"/" ++
					name ++
					"/files"
				)
			) []

		in makeSimpleTarget (name ++ "-" ++ env) [

			makeMkdir workDir,
			makeMkdir $ workDir ++ "/WEB-INF",
			makeMkdir $ workDir ++ "/WEB-INF/classes",
			makeMkdir $ workDir ++ "/WEB-INF/lib",

			makeCopyToDirNofail workDir $
				map makeWebFileset plugins,

			makeCopyToDir (workDir ++ "/WEB-INF/classes") [
				makeFilesetDir "work/bin" []
			],

			makeCopyToDir (workDir ++ "/WEB-INF/lib") [
				makeFilesetDir "lib" [
					makeExcludeName "service-api.jar"
				]
			],

			makeCopyFileToFile
				(name ++ "/web-" ++ env ++ ".xml")
				(workDir ++ "/WEB-INF/web.xml")

		]

	let makeArgLine line =
		mkelem "arg" [
			sattr "line" line
		] []

	let makeArgValue value =
		mkelem "arg" [
			sattr "value" value
		] []

	let makeExec exec elems =
		mkelem "exec" [
			sattr "failonerror" "false",
			sattr "executable" exec
		] elems

	let makeExecNoFail exec elems =
		mkelem "exec" [
			sattr "failonerror" "true",
			sattr "executable" exec
		] elems

	let makeExecDir dir exec elems =
		mkelem "exec" [
			sattr "failonerror" "true",
			sattr "dir" dir,
			sattr "executable" exec
		] elems

	let makeTomcatTarget env = let

		workDir =
			"work/" ++ env

		tomcatDir =
			 workDir ++ "/tomcat"

		in makeSimpleTarget ("tomcat-" ++ env) [

			-- deploy tomcat

			makeMkdir "temp",

			makeExecDir "temp" "tar" [
				makeArgLine "--extract",
				makeArgLine "--file ../binaries/packages/apache-tomcat-7.0.67.tar.gz"
			],

			makeDeleteDir $ tomcatDir ++ "/**",
			makeMoveFileToFile "temp/apache-tomcat-7.0.67" tomcatDir,

			-- configure tomcat

			makeCopyFileToFileOverwrite
				("console/server-" ++ env ++ ".xml")
				(tomcatDir ++ "/conf/server.xml"),

			makeCopyFileToFile
				"config/tomcat-users.xml"
				(tomcatDir ++ "/conf/tomcat-users.xml"),

			-- deploy console

			makeDeleteDir $ tomcatDir ++ "/apps/console/ROOT",

			makeCopyDirToDir
				(workDir ++ "/console")
				(tomcatDir ++ "/apps/console/ROOT"),

			makeCopyDirToDir
				(tomcatDir ++ "/webapps/manager")
				(tomcatDir ++ "/apps/console/manager"),

			makeCopyDirToDir
				(tomcatDir ++ "/webapps/host-manager")
				(tomcatDir ++ "/apps/console/host-manager"),

			-- deploy api

			makeDeleteDir $ tomcatDir ++ "/apps/api/ROOT",

			makeCopyDirToDir
				(workDir ++ "/api")
				(tomcatDir ++ "/apps/api/ROOT"),

			makeCopyDirToDir
				(tomcatDir ++ "/webapps/manager")
				(tomcatDir ++ "/apps/api/manager"),

			makeCopyDirToDir
				(tomcatDir ++ "/webapps/host-manager")
				(tomcatDir ++ "/apps/api/host-manager"),

			makeExec (tomcatDir ++ "/bin/catalina.sh") [
				makeArgLine "run"
			]

		]

	let makeWebTargets env =
		[
			makeWebTarget "console" env,
			makeWebTarget "api" env,
			makeTomcatTarget env
		]

	let makeServiceTarget name service action =
		makeSimpleTarget (name ++ "-" ++ action) [

			makeExec "./service" [
				makeArgValue service,
				makeArgValue action
			]

		]

	let makeServiceTargets =
		[
			makeServiceTarget "api" "tomcat_api" "restart",
			makeServiceTarget "console" "tomcat_console" "restart",
			makeServiceTarget "daemon" "daemon" "restart"
		]

	let makeJavadocTargets =
		[
			makeSimpleTarget "javadoc" [

				makeMkdir "javadoc",

				mkelem "javadoc" [
					sattr "destdir" "javadoc",
					sattr "access" "private",
					sattr "linksource" "yes"
				] [
					makeFilesetDir "src" [],
					mkelem "classpath" [
						sattr "refid" "classpath"
					] [],
					mkelem "link" [
						sattr "href" "https://docs.oracle.com/javase/8/docs/api/"
					] [],
					mkelem "link" [
						sattr "href" "http://logging.apache.org/log4j/docs/api"
					] [],
					mkelem "link" [
						sattr "href" "http://www.hibernate.org/hib_docs/v3/api"
					] [],
					mkelem "link" [
						sattr "href" "http://www.xom.nu/apidocs"
					] []
				]

			]
		]

	let makeDatabaseTargets =
		[
			makeSimpleTarget "db-drop" [
				makeExec "dropdb" [
					makeArgLine "${database.name}"
				]
			],
			makeSimpleTarget "db-create" [
				makeExecNoFail "createdb" [
					makeArgLine "${database.name}"
				]
			]
		]

	let makeScriptName name =
		mkelem "script" [
			sattr "name" name
		] []

	let makeSqlScripts getter suffix pluginConfig =
		map makeOne (getter pluginConfig)
		where makeOne scriptName =
			makeScriptName $
				"src/" ++
				(replace "." "/" $ plgPackage pluginConfig) ++
				"/model/" ++
				scriptName ++
				suffix ++
				".sql"

	let makeSqlTarget name getter suffix =
		makeSimpleTarget name [

			mkelem "taskdef" [
				sattr "name" "database-init",
				sattr "classname" "wbs.framework.utils.ant.DatabaseInitTask",
				sattr "classpathref" "classpath"
			] [],

			mkelem "database-init" [] $
				(concat . map (makeSqlScripts getter suffix)) plugins

		]

	let makeSqlTargets =
		[
			makeSqlTarget "sql-schema" plgSqlSchemas "",
			makeSqlTarget "sql-data" plgSqlDatas ""
		]

	let makeGenerateTargets =
		[

			makeSimpleTarget "generate-records" [

				mkelem "java" [
					sattr "classname"
						"wbs.framework.application.tools.BeanRunner",
					sattr "classpathref" "classpath",
					sattr "failonerror" "true"
				] [
					makeArgValue "wbs-test",
					makeArgValue "wbs.test",
					makeArgValue "model-meta,model-generate,utils",
					makeArgValue "",
					makeArgValue "wbs.framework.entity.generate.ModelGeneratorTool",
					makeArgValue "generateModels"
				]

			]

		]

	let makeSchemaTargets =
		[

			makeSimpleTarget "schema-create" [

				mkelem "java" [
					sattr "classname"
						"wbs.framework.application.tools.BeanRunner",
					sattr "classpathref" "classpath",
					sattr "failonerror" "true"
				] [
					makeArgValue "wbs-test",
					makeArgValue "wbs.test",
					makeArgValue "utils,config,data,entity,model-meta,schema,sql,schema-tool",
					makeArgValue "",
					makeArgValue "wbs.framework.schema.tool.SchemaTool",
					makeArgValue "schemaCreate"
				]

			]

		]

	let makeFixtureTargets =
		[

			makeSimpleTarget "fixtures" [

				mkelem "java" [
					sattr "classname"
						"wbs.framework.application.tools.BeanRunner",
					sattr "classpathref" "classpath",
					sattr "failonerror" "true"
				] [
					makeArgValue "wbs-test",
					makeArgValue "wbs.test",
					makeArgValue ("utils,config,data,entity,schema,sql," ++
						"model,model-meta,hibernate,object,logic,fixture"),
					makeArgValue "",
					makeArgValue "wbs.framework.entity.meta.ModelFixtureCreator",
					makeArgValue "runModelFixtureCreators"
				],

				mkelem "java" [
					sattr "classname"
						"wbs.framework.application.tools.BeanRunner",
					sattr "classpathref" "classpath",
					sattr "failonerror" "true"
				] [
					makeArgValue "wbs-test",
					makeArgValue "wbs.test",
					makeArgValue ("utils,config,data,entity,schema,sql," ++
						"model,model-meta,hibernate,object,logic,fixture"),
					makeArgValue "",
					makeArgValue "wbs.framework.fixtures.FixturesTool",
					makeArgValue "runFixtureProviders"
				]

			]
		]

	let makeCodeStyleTargets =
		[
			makeSimpleTarget "code-style" [
				mkelem "cs:checkstyle" [
					sattr "config" "etc/style.xml",
					sattr "classpathref" "classpath"
				] [
					mkelem "fileset" [
						sattr "dir" "src",
						sattr "includes" "**/*.java"
					] []
				]
			]
		]

	let makeProject =
		root [] [
			mkelem "project" [
				sattr "name" $ bldName buildConfig,
				sattr "basedir" ".",
				sattr "default" "build",
				sattr "xmlns:cs" "antlib:com.puppycrawl.tools.checkstyle.ant"
			] (
				makeProperties ++
				makeClasspath ++
				makeComboTargets ++
				makeCleanTargets ++
				makeBuildTargets ++
				makeWebTargets "live" ++
				makeWebTargets "test" ++
				makeServiceTargets ++
				makeJavadocTargets ++
				makeDatabaseTargets ++
				makeSqlTargets ++
				makeGenerateTargets ++
				makeSchemaTargets ++
				makeFixtureTargets ++
				makeCodeStyleTargets
			)
		]

	let writeProject =
		writeDocument [withIndent yes] "build.xml"

	runX (makeProject >>> writeProject)

	return ()
