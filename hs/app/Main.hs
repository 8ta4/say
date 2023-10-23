module Main (main) where

import Control.Exception (try)
import Control.Monad (void)
import Data.ByteString.Lazy (ByteString)
import Network.HTTP.Simple
import System.Process (spawnCommand)
import Prelude

main :: IO ()
main = do
  response <- try $ httpLBS "http://localhost:8080/" :: IO (Either HttpException (Response ByteString))
  case response of
    Left _ -> void (spawnCommand "cd ../clj && lein run")
    Right _ -> pure ()
