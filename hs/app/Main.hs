module Main (main) where

import System.Process
import Prelude

main :: IO ()
main = spawnCommand "cd ../clj && lein run" >> return ()
