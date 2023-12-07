module Main where

import Prelude

import Effect (Effect)
import Effect.Ref (new, read, write)
import Node.ChildProcess (defaultSpawnOptions, spawn, stdin)
import Node.Stream (Readable, pipe)

foreign import data Float32Array :: Type

createStream :: Effect (Readable ())
createStream = do
  stream <- newReadable
  ffmpeg <- spawn "ffmpeg" [ "-y", "-f", "f32le", "-ar", "16000", "-i", "pipe:0", "-ar", "16000", "output.opus" ] defaultSpawnOptions
  _ <- pipe stream $ stdin ffmpeg
  pure stream

main :: Effect Unit
main = do
  stream <- createStream
  ref <- new { buffer: mempty :: Float32Array, stream: stream }
  let
    record = \audio -> do
      state <- read ref
      -- TODO: Add your audio recording logic here
      pushAudio state.stream audio
      write (state { buffer = state.buffer <> audio }) ref
  let
    process = do
      state <- read ref
      end state.stream
      newStream <- createStream
      write (state { stream = newStream }) ref
      -- TODO: Add your audio processing logic here
      foo state.buffer
  launch record process

foreign import newReadable :: Effect (Readable ())

foreign import pushAudio :: Readable () -> Float32Array -> Effect Unit

foreign import end :: Readable () -> Effect Unit

foreign import launch :: (Float32Array -> Effect Unit) -> Effect Unit -> Effect Unit

foreign import appendFloat32Array :: Float32Array -> Float32Array -> Float32Array

instance semigroupFloat32Array :: Semigroup Float32Array where
  append = appendFloat32Array

foreign import memptyFloat32Array :: Float32Array

instance monoidFloat32Array :: Monoid Float32Array where
  mempty = memptyFloat32Array

foreign import foo :: Float32Array -> Effect Unit
