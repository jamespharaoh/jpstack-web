-- TODO:
-- - verify plugin dependencies
-- - establish and use correct plugin order
-- - output eclipse configs
-- - output web configs

module Main where

import Text.XML.HXT.Core

import qualified Wbs.Config as Config
import qualified Wbs.Setup.AntProject as AntProject
import qualified Wbs.Setup.EclipseClasspath as EclipseClasspath

main :: IO ()
main = do

	worldConfig <- Config.loadWorld

	AntProject.writeBuildFile worldConfig
	EclipseClasspath.writeClasspath worldConfig

	return ()
