module Wbs.Setup.EclipseFactorypath where

import Data.List
import Data.String.Utils (replace)

import Text.XML.HXT.Core

import Wbs.Config

writeFactorypath :: World -> IO ()
writeFactorypath world = do

	let build = wldBuild world

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
			bldName build,
			"/binaries/libraries/guava-bundle-16.0.1.jar"
		]

	let makeJodaTimeEntry =
		makeEntry "WKSPJAR" $ concat [
			"/",
			bldName build,
			"/binaries/libraries/joda-time-jar-2.3.jar"
		]

	let makeFactorypath =
		root [] [
			mkelem "factorypath" [] [
				makeAnnotationsEntry,
				makeGuavaEntry,
				makeJodaTimeEntry
			]
		]

	let writeFactorypath =
		writeDocument [withIndent yes] ".factorypath"

	runX (makeFactorypath >>> writeFactorypath)

	return ()
