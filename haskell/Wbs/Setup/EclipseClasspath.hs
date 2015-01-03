module Wbs.Setup.EclipseClasspath where

import Data.List
import Data.String.Utils (replace)

import Text.XML.HXT.Core

import Wbs.Config

writeClasspath :: WorldConfig -> IO ()
writeClasspath world = do

	let build = wcBuild world
	let libraries = wcLibraries world

	let makeOriginalSrcEntry =
		mkelem "classpathentry" [
			sattr "kind" "src",
			sattr "path" "src"
		] []

	let makeConEntry =
		mkelem "classpathentry" [
			sattr "kind" "con",
			sattr "path" "org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/java-6-openjdk-amd64"
		] []

	let makeLibEntry library =

		mkelem "classpathentry" [
			sattr "kind" "lib",
			sattr "path" libPath,
			if libSource library
				then sattr "sourcepath" sourcePath
				else zeroArrow
		] []

		where

			libPath = concat [
				"binaries/libraries/",
				libName library,
				"-",
				libType library,
				"-",
				libVersion library,
				".jar" ]

			sourcePath = concat [
				"/home/vagrant/",
				bcName build,
				"/bnaries/libraries/",
				libName library,
				"-source-",
				libVersion library,
				".jar" ]

	let makeGeneratedSrcEntry =
		mkelem "classpathentry" [
			sattr "kind" "src",
			sattr "path" "work/eclipse/generated"
		] [
			mkelem "attributes" [] [
				mkelem "attribute" [
					sattr "name" "optional",
					sattr "value" "true"
				] []
			]
		]

	let makeOutputEntry =
		mkelem "classpathentry" [
			sattr "kind" "output",
			sattr "path" "work/eclipse/bin"
		] []

	let makeClasspath =
		root [] [
			mkelem "classpath" [] (
				[ makeOriginalSrcEntry ] ++
				[ makeConEntry ] ++
				map makeLibEntry libraries ++
				[ makeGeneratedSrcEntry ] ++
				[ makeOutputEntry ]
			)
		]

	let writeClasspath =
		writeDocument [withIndent yes] ".classpath"

	runX (makeClasspath >>> writeClasspath)

	return ()
