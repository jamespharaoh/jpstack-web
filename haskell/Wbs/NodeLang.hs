module Wbs.NodeLang where

import Text.ParserCombinators.Parsec
import Control.Applicative hiding ((<|>), optional, many)

data Node =
		  TagNode String String [Node]
		| TextNode String
	deriving Show

parseWhitespace :: Parser String
parseWhitespace = many $ oneOf [' ']

parseBlank :: Parser String
parseBlank = do
	white <- many $ oneOf [' ', '\t']

	oneOf ['\n']

	--if white == ""
	--	then oneOf ['\n'] *> return ()
	--	else optional $ oneOf ['\n']

	return white

parseIndent :: Int -> Parser String
parseIndent 0 = return ""
parseIndent n = do oneOf "\t"; parseIndent (n - 1)

parseTagLabel :: Parser String
parseTagLabel = oneOf "@" *> many (noneOf [' ', '\n'])

parseTagRest :: Parser String
parseTagRest = parseWhitespace *> many (noneOf ['\n'])

parseTag :: Int -> Parser Node
parseTag indent = do

	many $ try $ parseBlank

	parseIndent indent
	tagLabel <- parseTagLabel
	tagRest <- parseTagRest
	oneOf ['\n']

	children <- many $ try $ parseNode (indent + 1)

	return $ TagNode tagLabel tagRest children

parseTextLine :: Int -> Parser String
parseTextLine indent = do
	parseIndent indent
	line <- many (noneOf ['\n'])
	oneOf ['\n']
	return line

parseText :: Int -> Parser Node
parseText indent = do
	many $ try $ parseBlank
	lines <- many1 $ parseTextLine indent
	return $ TextNode $ concat lines

parseNode :: Int -> Parser Node
parseNode indent =
	try (parseTag indent) <|> parseText indent

parseDocument :: Parser [Node]
parseDocument = do
	nodes <- many $ try $ parseNode 0
	many $ parseBlank
	eof
	return nodes

parseFile :: String -> IO (Either ParseError [Node])
parseFile filename = do
	input <- readFile filename
	return $ parse parseDocument filename input
