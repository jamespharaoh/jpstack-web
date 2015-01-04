{-# LANGUAGE OverloadedStrings #-}

module Main where

import qualified Data.Text.Lazy.IO           as TextIo

import           Text.Blaze.Html5            ((!))
import qualified Text.Blaze.Html5            as Html5
import qualified Text.Blaze.Html5.Attributes as Attr

import qualified Text.Blaze.Html.Renderer.Text as Renderer

import qualified Wbs.NodeLang                as NodeLang

renderPage :: Html5.Html
renderPage = Html5.docTypeHtml $ do
	renderPageHead
	renderPageBody

renderPageHead :: Html5.Html
renderPageHead = Html5.head $ do

	Html5.title "WBS developers documentation"

	--Html5.link
	--	! Attr.rel "stylesheet"
	--	! Attr.href "/styles"

	Html5.link
		! Attr.href "http://fonts.googleapis.com/css?family=Montserrat:400,700|Muli:400,300,300italic,400italic"
		! Attr.rel "stylesheet"
		! Attr.type_ "text/css"

renderPageBody :: Html5.Html
renderPageBody = Html5.body $ do

	Html5.p "Hello world"

main :: IO ()
main = do

	-- read input
	putStrLn "Loading data"
	file <- NodeLang.parseFile "input"

	-- render page to abstract form
	putStrLn "Generating page"
	let abstractPage = renderPage

	-- render page to concrete form
	putStrLn "Rendering HTML"
	let concretePage = Renderer.renderHtml abstractPage

	-- write page to file
	putStrLn "Writing output"
	TextIo.writeFile "output.html" concretePage

	putStrLn "All done"
