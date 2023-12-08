module Main where

import Prelude

import Data.Array (snoc)
import Debug (traceM)
import Effect (Effect)
import Effect.Ref (new, read, write)
import Node.ChildProcess (defaultSpawnOptions, spawn, stdin)
import Node.Stream (Readable, pipe)

foreign import data Float32Array :: Type

main :: Effect Unit
main = do
  stream <- createStream
  ref <- new { raw: mempty, temporary: mempty, stream: stream }
  let
    record = \audio -> do
      state <- read ref

      -- TODO: Add your audio recording logic here
      let raw' = state.raw <> audio

      -- https://github.com/snakers4/silero-vad/blob/5e7ee10ee065ab2b98751dd82b28e3c6360e19aa/utils_vad.py#L207
      if length raw' == 1536 then
        write (state { raw = mempty, temporary = snoc state.temporary raw' }) ref
      else
        write (state { raw = raw' }) ref
      push state.stream audio
  let
    process = do
      state <- read ref
      end state.stream
      stream' <- createStream
      write (state { stream = stream' }) ref

      -- TODO: Add your audio processing logic here
      traceM state.temporary
  launch record process

createStream :: Effect (Readable ())
createStream = do
  stream <- newReadable
  ffmpeg <- spawn "ffmpeg" [ "-y", "-f", "f32le", "-ar", "16000", "-i", "pipe:0", "-b:a", "24k", "output.opus" ] defaultSpawnOptions
  _ <- pipe stream $ stdin ffmpeg
  pure stream

foreign import length :: Float32Array -> Int

foreign import newReadable :: Effect (Readable ())

foreign import push :: Readable () -> Float32Array -> Effect Unit

foreign import appendFloat32Array :: Float32Array -> Float32Array -> Float32Array

instance semigroupFloat32Array :: Semigroup Float32Array where
  append = appendFloat32Array

foreign import memptyFloat32Array :: Float32Array

instance monoidFloat32Array :: Monoid Float32Array where
  mempty = memptyFloat32Array

foreign import end :: Readable () -> Effect Unit

foreign import launch :: (Float32Array -> Effect Unit) -> Effect Unit -> Effect Unit
