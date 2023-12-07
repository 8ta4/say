module Main where

import Prelude

import Effect (Effect)
import Effect.Ref (modify_, new, read)

foreign import data Float32Array :: Type

main :: Effect Unit
main = do
  bufferRef <- new mempty
  let record = \audio -> modify_ (\buffer -> buffer <> audio) bufferRef
  let
    process = do
      buffer <- read bufferRef
      -- TODO: Add your audio processing logic here
      foo buffer
  launch record process

foreign import launch :: (Float32Array -> Effect Unit) -> Effect Unit -> Effect Unit

foreign import appendFloat32Array :: Float32Array -> Float32Array -> Float32Array

instance semigroupFloat32Array :: Semigroup Float32Array where
  append = appendFloat32Array

foreign import memptyFloat32Array :: Float32Array

instance monoidFloat32Array :: Monoid Float32Array where
  mempty = memptyFloat32Array

foreign import foo :: Float32Array -> Effect Unit
