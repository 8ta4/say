module Main where

import Prelude

import Data.Function.Uncurried (Fn3, runFn3)
import Debug (traceM)
import Effect (Effect)
import Effect.Aff (launchAff_)
import Effect.Class (liftEffect)
import Effect.Ref (new, read, write)
import Node.ChildProcess (defaultSpawnOptions, spawn, stdin)
import Node.Stream (Readable, pipe)
import Promise.Aff (Promise, toAffE)

foreign import data Tensor :: Type

foreign import data Float32Array :: Type

main :: Effect Unit
main = do
  stream <- createStream
  ref <- new { raw: mempty, temporary: mempty, stream: stream, h: tensor, c: tensor }
  let
    record = \audio -> do
      state <- read ref

      -- TODO: Add your audio recording logic here
      let raw' = state.raw <> audio

      -- https://developer.mozilla.org/en-US/docs/Web/API/AudioWorkletProcessor/process#sect1
      if length raw' >= windowSizeSamples then launchAff_ do
        let splitRaw' = splitAt windowSizeSamples raw'
        result <- toAffE $ run splitRaw'.before state.h state.c
        liftEffect $ write (state { raw = splitRaw'.after, temporary = state.temporary <> raw', h = result.h, c = result.c }) ref
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

foreign import run :: Float32Array -> Tensor -> Tensor -> Effect (Promise { probability :: Number, h :: Tensor, c :: Tensor })

foreign import tensor :: Tensor

foreign import length :: Float32Array -> Int

foreign import newReadable :: Effect (Readable ())

foreign import push :: Readable () -> Float32Array -> Effect Unit

foreign import appendFloat32Array :: Float32Array -> Float32Array -> Float32Array

instance semigroupFloat32Array :: Semigroup Float32Array where
  append = appendFloat32Array

foreign import memptyFloat32Array :: Float32Array

instance monoidFloat32Array :: Monoid Float32Array where
  mempty = memptyFloat32Array

-- https://github.com/snakers4/silero-vad/blob/5e7ee10ee065ab2b98751dd82b28e3c6360e19aa/utils_vad.py#L207
windowSizeSamples :: Int
windowSizeSamples = 1536

-- https://github.com/purescript/purescript-arrays/blob/6554b3d9c1ebb871477ffa88c2f3850d714b42b0/src/Data/Array.purs#L713-L715
splitAt :: Int -> Float32Array -> { before :: Float32Array, after :: Float32Array }
splitAt i xs | i <= 0 = { before: mempty, after: xs }
splitAt i xs = { before: slice 0 i xs, after: slice i (length xs) xs }

-- https://github.com/purescript/purescript-arrays/blob/6554b3d9c1ebb871477ffa88c2f3850d714b42b0/src/Data/Array.purs#L929C30-L930
slice :: Int -> Int -> Float32Array -> Float32Array
slice = runFn3 sliceImpl

-- https://github.com/purescript/purescript-arrays/blob/6554b3d9c1ebb871477ffa88c2f3850d714b42b0/src/Data/Array.purs#L932
foreign import sliceImpl :: Fn3 Int Int Float32Array Float32Array

foreign import end :: Readable () -> Effect Unit

foreign import launch :: (Float32Array -> Effect Unit) -> Effect Unit -> Effect Unit
