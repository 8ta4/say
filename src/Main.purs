module Main where

import Prelude

import Effect (Effect)

main :: Effect Unit
main = do
  launch

foreign import launch :: Effect Unit
