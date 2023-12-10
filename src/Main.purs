module Main where

import Prelude

import Data.Int (floor, toNumber)
import Data.UUID (genUUID, toString)
import Debug (traceM)
import Effect (Effect)
import Effect.Aff (launchAff_)
import Effect.Class (liftEffect)
import Effect.Ref (modify_, new, read, write)
import Float32Array (Float32Array, length, splitAt, takeEnd)
import Node.ChildProcess (ChildProcess, defaultSpawnOptions, spawn, stdin)
import Node.Encoding (Encoding(..))
import Node.FS.Sync (mkdtemp, readTextFile)
import Node.OS (homedir, tmpdir)
import Node.Stream (Readable, pipe)
import Promise.Aff (Promise, toAffE)

foreign import data Tensor :: Type

main :: Effect Unit
main = do
  tempDirectory <- tmpdir
  -- https://nodejs.org/api/fs.html#fspromisesmkdtempprefix-options:~:text=mkdtemp(join(tmpdir()%2C%20%27foo%2D%27))
  appTempDirectory <- mkdtemp $ tempDirectory <> "/say-"
  homeDirectory <- homedir
  key <- readTextFile UTF8 $ homeDirectory <> "/.config/say/key"
  stream <- newReadable
  ref <- new { stream: stream, pause: mempty, streamLength: 0, raw: mempty, h: tensor, c: tensor, processing: false }
  let
    record audio = do

      -- TODO: Add your audio recording logic here
      state <- read ref
      let raw = state.raw <> audio

      -- https://developer.mozilla.org/en-US/docs/Web/API/AudioWorkletProcessor/process#sect1
      if length raw >= windowSizeSamples then do
        let { before, after } = splitAt windowSizeSamples raw
        write (state { raw = after, pause = takeEnd samplesInPause $ state.pause <> before }) ref
        launchAff_ $ detect before
      else
        write (state { raw = raw }) ref
    detect audio = do
      state <- liftEffect $ read ref
      result <- toAffE $ run audio state.h state.c
      let state' = state { h = result.h, c = result.c }

      -- https://github.com/snakers4/silero-vad/blob/5e7ee10ee065ab2b98751dd82b28e3c6360e19aa/utils_vad.py#L187-L188
      if (0.5 < result.probability) then do
        liftEffect $ write (state' { streamLength = state.streamLength + length state.pause, pause = mempty }) ref
        liftEffect $ push state.stream $ state.pause
      else if samplesInStream < state.streamLength && samplesInPause == length state.pause then liftEffect process'
      else liftEffect $ write state' ref
    process = do

      -- TODO: Add your audio processing logic here
      process'
    process' = do
      state <- read ref
      push state.stream $ state.pause <> state.raw
      end state.stream
      initializeStream
      write (state { pause = mempty, raw = mempty, streamLength = 0 }) ref
      traceM state.streamLength
    initializeStream = do
      stream' <- newReadable
      modify_ (\state -> state { stream = stream' }) ref
      uuid <- genUUID
      let filepath = appTempDirectory <> "/" <> toString uuid <> ".opus"
      traceM filepath
      ffmpeg <- spawn "ffmpeg" [ "-f", "f32le", "-ar", show ar, "-i", "pipe:0", "-b:a", "24k", filepath ] defaultSpawnOptions
      _ <- pipe stream $ stdin ffmpeg
      handleClose ffmpeg $ \processing' -> modify_ (\state -> state { processing = processing' }) ref
  initializeStream
  launch record process

foreign import tensor :: Tensor

foreign import newReadable :: Effect (Readable ())

ar :: Int
ar = 16000

foreign import handleClose :: ChildProcess -> (Boolean -> Effect Unit) -> Effect Unit

streamDuration :: Int
streamDuration = 60

samplesInStream :: Int
samplesInStream = ar * streamDuration

pauseDuration :: Number
pauseDuration = 1.5

samplesInPause :: Int
samplesInPause = floor $ toNumber ar * pauseDuration

foreign import run :: Float32Array -> Tensor -> Tensor -> Effect (Promise { probability :: Number, h :: Tensor, c :: Tensor })

foreign import push :: Readable () -> Float32Array -> Effect Unit

-- https://github.com/snakers4/silero-vad/blob/5e7ee10ee065ab2b98751dd82b28e3c6360e19aa/utils_vad.py#L207
windowSizeSamples :: Int
windowSizeSamples = 1536

foreign import end :: Readable () -> Effect Unit

foreign import launch :: (Float32Array -> Effect Unit) -> Effect Unit -> Effect Unit