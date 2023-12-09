module Main where

import Prelude

import Data.Int (floor, toNumber)
import Data.UUID (genUUID, toString)
import Debug (traceM)
import Effect (Effect)
import Effect.Aff (Aff, launchAff_)
import Effect.Class (liftEffect)
import Effect.Ref (Ref, new, read, write)
import Float32Array (Float32Array, length, splitAt, takeEnd)
import Node.ChildProcess (defaultSpawnOptions, spawn, stdin)
import Node.FS.Sync (mkdtemp)
import Node.OS (tmpdir)
import Node.Stream (Read, Readable, Stream, pipe)
import Promise.Aff (Promise, toAffE)

type StateRef r = Ref { stream :: Stream (read :: Read), streamLength :: Int, pause :: Float32Array, h :: Tensor, c :: Tensor | r }

foreign import data Tensor :: Type

main :: Effect Unit
main = do
  tempDirectory <- tmpdir
  -- https://nodejs.org/api/fs.html#fspromisesmkdtempprefix-options:~:text=mkdtemp(join(tmpdir()%2C%20%27foo%2D%27))
  appTempDirectory <- mkdtemp $ tempDirectory <> "/say-"
  stream <- createStream appTempDirectory
  ref <- new { stream: stream, pause: mempty, streamLength: 0, raw: mempty, h: tensor, c: tensor }
  let
    record = \audio -> do

      -- TODO: Add your audio recording logic here
      state <- read ref
      let raw = state.raw <> audio

      -- https://developer.mozilla.org/en-US/docs/Web/API/AudioWorkletProcessor/process#sect1
      if length raw >= windowSizeSamples then do
        let { before, after } = splitAt windowSizeSamples raw
        write (state { raw = after, pause = takeEnd samplesInPause $ state.pause <> before }) ref
        launchAff_ $ detect ref before
      else
        write (state { raw = raw }) ref
  let
    process = do
      -- TODO: Add your audio processing logic here

      state <- read ref
      push state.stream $ state.pause <> state.raw
      end state.stream
      stream' <- createStream appTempDirectory
      write (state { stream = stream', pause = mempty, raw = mempty, streamLength = 0 }) ref
      traceM state.streamLength
  launch record process

detect :: forall r. StateRef r -> Float32Array -> Aff Unit
detect ref audio = do
  state <- liftEffect $ read ref
  result <- toAffE $ run audio state.h state.c
  let state' = state { h = result.h, c = result.c }

  -- https://github.com/snakers4/silero-vad/blob/5e7ee10ee065ab2b98751dd82b28e3c6360e19aa/utils_vad.py#L187-L188
  if (result.probability > 0.5) then do
    liftEffect $ write (state' { streamLength = state.streamLength + length state.pause, pause = mempty }) ref
    liftEffect $ push state.stream $ state.pause
  else
    liftEffect $ write state' ref

createStream :: String -> Effect (Readable ())
createStream appTempDirectory = do
  stream <- newReadable
  uuid <- genUUID
  let filepath = appTempDirectory <> "/" <> toString uuid <> ".opus"
  traceM filepath
  ffmpeg <- spawn "ffmpeg" [ "-f", "f32le", "-ar", show ar, "-i", "pipe:0", "-b:a", "24k", filepath ] defaultSpawnOptions
  _ <- pipe stream $ stdin ffmpeg
  pure stream

foreign import run :: Float32Array -> Tensor -> Tensor -> Effect (Promise { probability :: Number, h :: Tensor, c :: Tensor })

foreign import tensor :: Tensor

foreign import newReadable :: Effect (Readable ())

ar :: Int
ar = 16000

pauseDuration :: Number
pauseDuration = 1.5

samplesInPause :: Int
samplesInPause = floor $ toNumber ar * pauseDuration

foreign import push :: Readable () -> Float32Array -> Effect Unit

-- https://github.com/snakers4/silero-vad/blob/5e7ee10ee065ab2b98751dd82b28e3c6360e19aa/utils_vad.py#L207
windowSizeSamples :: Int
windowSizeSamples = 1536

foreign import end :: Readable () -> Effect Unit

foreign import launch :: (Float32Array -> Effect Unit) -> Effect Unit -> Effect Unit
