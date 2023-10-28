module Main (main) where

import Control.Exception (try)
import Control.Monad (void)
import Data.ByteString.Lazy (ByteString)
import Network.HTTP.Simple
import System.Console.Haskeline
import System.Environment (getExecutablePath)
import System.FilePath (takeDirectory, takeFileName)
import System.Process (spawnCommand)
import Prelude

main :: IO ()
main = do
  response <- try $ httpLBS "http://localhost:8080/" :: IO (Either HttpException (Response ByteString))
  case response of
    Left _ -> do
      minput <- runInputT defaultSettings $ getPassword (Just '*') "Please enter your API key: "
      case minput of
        Nothing -> putStrLn "No input received."
        Just input -> do
          executablePath <- getExecutablePath
          let command = case takeFileName executablePath of
                "say" -> "cd " <> takeDirectory (takeDirectory executablePath) <> "/clj && java -jar target/uberjar/say.jar " <> input
                _ -> "cd ../clj && lein run " <> input
          void $ spawnCommand command
    Right _ -> pure ()
