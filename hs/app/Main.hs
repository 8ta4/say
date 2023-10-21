module Main (main) where

import Control.Monad (void)
import System.Process (spawnCommand)
import Prelude

main :: IO ()
main = void (spawnCommand "cd ../clj && lein run")
