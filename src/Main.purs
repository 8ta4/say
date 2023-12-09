module Main where

import Prelude

import Data.Int (floor, toNumber)
import Data.UUID (genUUID, toString)
import Debug (traceM)
import Effect (Effect)
import Effect.Aff (launchAff_)
import Effect.Class (liftEffect)
import Effect.Ref (new, read, write)
import Float32Array (Float32Array, length, splitAt, takeEnd)
import Node.ChildProcess (defaultSpawnOptions, spawn, stdin)
import Node.FS.Sync (mkdtemp)
import Node.OS (tmpdir)
import Node.Stream (Readable, pipe)
import Promise.Aff (Promise, toAffE)

foreign import data Tensor :: Type

main :: Effect Unit
main = do
  tempDirectory <- tmpdir
  -- https://nodejs.org/api/fs.html#fspromisesmkdtempprefix-options:~:text=mkdtemp(join(tmpdir()%2C%20%27foo%2D%27))
  appTempDirectory <- mkdtemp $ tempDirectory <> "/say-"
  stream <- createStream appTempDirectory
  ref <- new { raw: mempty, temporary: mempty, stream: stream, audioLength: 0, h: tensor, c: tensor }
  let
    record = \audio -> do
      state <- read ref

      -- TODO: Add your audio recording logic here
      let raw' = state.raw <> audio

      -- https://developer.mozilla.org/en-US/docs/Web/API/AudioWorkletProcessor/process#sect1
      if length raw' >= windowSizeSamples then launchAff_ do
        let { before, after } = splitAt windowSizeSamples raw'
        result <- toAffE $ run before state.h state.c
        let state' = state { raw = after, h = result.h, c = result.c }
        let temporary' = state.temporary <> before
        if result.probability > 0.5 then do
          liftEffect $ write (state' { temporary = mempty, audioLength = state.audioLength + length state.temporary }) ref
          liftEffect $ push stream $ temporary'
        else
          liftEffect $ write (state' { temporary = takeEnd samplesInPause temporary' }) ref
      else
        write (state { raw = raw' }) ref
  let
    process = do
      state <- read ref
      end state.stream
      stream' <- createStream appTempDirectory
      write (state { stream = stream', audioLength = 0 }) ref

      -- TODO: Add your audio processing logic here
      traceM state.audioLength
  launch record process

createStream :: String -> Effect (Readable ())
createStream appTempDirectory = do
  stream <- newReadable
  uuid <- genUUID
  let filepath = appTempDirectory <> "/" <> toString uuid <> ".opus"
  traceM filepath
  ffmpeg <- spawn "ffmpeg" [ "-y", "-f", "f32le", "-ar", show ar, "-i", "pipe:0", "-b:a", "24k", filepath ] defaultSpawnOptions
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
