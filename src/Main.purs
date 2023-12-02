module Main where

import Prelude

import Data.Array (snoc)
import Data.ArrayBuffer.Types (Float32Array)
import Effect (Effect)
import Effect.Ref (modify_, new, read)

main :: Effect Unit
main = do
  bufferRef <- new []
  let record = \audio -> modify_ (\buffer -> snoc buffer audio) bufferRef
  let
    process = do
      buffer <- read bufferRef
      -- TODO: Add your audio processing logic here
      foo buffer
  launch record process

foreign import launch :: (Float32Array -> Effect Unit) -> Effect Unit -> Effect Unit

foreign import foo :: Array Float32Array -> Effect Unit
