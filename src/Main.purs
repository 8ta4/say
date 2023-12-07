module Main where

import Prelude

import Effect (Effect)
import Effect.Ref (modify_, new, read)
import Node.Stream (Readable)

foreign import data Float32Array :: Type

main :: Effect Unit
main = do
  ref <- new $ { buffer: mempty :: Float32Array, stream: newReadable }
  pure unit
  let record = \audio -> modify_ (\state -> (state { buffer = state.buffer <> audio })) ref
  let
    process = do
      state <- read ref
      -- TODO: Add your audio processing logic here
      foo state.buffer
  launch record process

foreign import newReadable :: Readable ()

foreign import launch :: (Float32Array -> Effect Unit) -> Effect Unit -> Effect Unit

foreign import appendFloat32Array :: Float32Array -> Float32Array -> Float32Array

instance semigroupFloat32Array :: Semigroup Float32Array where
  append = appendFloat32Array

foreign import memptyFloat32Array :: Float32Array

instance monoidFloat32Array :: Monoid Float32Array where
  mempty = memptyFloat32Array

foreign import foo :: Float32Array -> Effect Unit
