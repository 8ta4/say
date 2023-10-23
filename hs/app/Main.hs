module Main (main) where

import Control.Exception (try)
import Control.Monad (void)
import Data.ByteString.Lazy (ByteString)
import Network.HTTP.Simple
import System.Console.Haskeline
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
          void $ spawnCommand $ "cd ../clj && lein run " <> input
    Right _ -> pure ()
