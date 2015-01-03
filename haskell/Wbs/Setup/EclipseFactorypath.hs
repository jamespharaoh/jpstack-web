module Wbs.Setup.EclipseFactorypath where

import Data.List
import Data.String.Utils (replace)

import Text.XML.HXT.Core

import Wbs.Config

writeFactorypath :: WorldConfig -> IO ()
writeFactorypath world = do

	let build = wcBuild world

	let makeEntry kind id =
		mkelem "factorypathentry" [
			sattr "kind" kind,
			sattr "id" id,
			sattr "enabled" "true",
			sattr "runInBatchMode" "false"
		] []

	let makeAnnotationsEntry =
		makeEntry "PLUGIN" "org.eclipse.jst.ws.annotations.core"

	let makeGuavaEntry =
		makeEntry "WKSPJAR" $ concat [
			"/",
			bcName build,
			"/binaries/libraries/guava-bundle-16.0.1.jar"
		]

	let makeJodaTimeEntry =
		makeEntry "WKSPJAR" $ concat [
			"/",
			bcName build,
			"/binaries/libraries/joda-time-jar-2.3.jar"
		]

	let makeWbsFrameworkEntry =
		makeEntry "WKSPJAR" $ concat [
			"/",
			bcName build,
			"/work/wbs-framework.jar"
		]

	let makeFactorypath =
		root [] [
			mkelem "factorypath" [] [
				makeAnnotationsEntry,
				makeGuavaEntry,
				makeJodaTimeEntry,
				makeWbsFrameworkEntry
			]
		]

	let writeFactorypath =
		writeDocument [withIndent yes] ".factorypath"

	runX (makeFactorypath >>> writeFactorypath)

	return ()
