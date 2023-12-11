module Main where

import Prelude

import Data.DateTime (month, year)
import Data.Enum (fromEnum)
import Data.Int (floor, toNumber)
import Data.UUID (genUUID, toString)
import Debug (traceM)
import Effect (Effect)
import Effect.Aff (launchAff_)
import Effect.Class (liftEffect)
import Effect.Now (nowDate)
import Effect.Ref (modify_, new, read, write)
import Float32Array (Float32Array, length, splitAt, takeEnd)
import Node.ChildProcess (ChildProcess, defaultSpawnOptions, spawn, stdin)
import Node.Encoding (Encoding(..))
import Node.FS.Aff (mkdir')
import Node.FS.Perms (all, mkPerms, none)
import Node.FS.Sync (mkdtemp, readTextFile)
import Node.OS (homedir, tmpdir)
import Node.Stream (Readable, pipe)
import Promise.Aff (Promise, toAffE)

foreign import data Tensor :: Type

foreign import data Deepgram :: Type

main :: Effect Unit
main = do
  tempDirectory <- tmpdir
  -- https://nodejs.org/api/fs.html#fspromisesmkdtempprefix-options:~:text=mkdtemp(join(tmpdir()%2C%20%27foo%2D%27))
  appTempDirectory <- mkdtemp $ tempDirectory <> "/say-"
  traceM "App temp directory:"
  traceM appTempDirectory
  homeDirectory <- homedir
  key <- readTextFile UTF8 $ homeDirectory <> "/.config/say/key"
  stream <- newReadable
  ref <- new { stream: stream, pause: mempty, streamLength: 0, raw: mempty, h: tensor, c: tensor, processing: false }
  let
    record audio = do
      state <- read ref
      let raw = state.raw <> audio

      -- https://developer.mozilla.org/en-US/docs/Web/API/AudioWorkletProcessor/process#sect1
      if length raw >= windowSizeSamples then do
        let { before, after } = splitAt windowSizeSamples raw
        write (state { raw = after, pause = takeEnd samplesInPause $ state.pause <> before }) ref
        launchAff_ $ do

          -- TODO: Ensure `run` is not executed concurrently to avoid using incorrect `h` and `c`
          result <- toAffE $ run before state.h state.c
          liftEffect do
            state' <- read ref
            let state'' = state' { h = result.h, c = result.c }

            -- https://github.com/snakers4/silero-vad/blob/5e7ee10ee065ab2b98751dd82b28e3c6360e19aa/utils_vad.py#L187-L188
            if (0.5 < result.probability) then do
              write (state'' { streamLength = state'.streamLength + length state'.pause, pause = mempty }) ref
              push state'.stream $ state'.pause
            else if samplesInStream < state'.streamLength && samplesInPause == length state'.pause then save'
            else write state'' ref
      else
        write (state { raw = raw }) ref
    save = do

      -- TODO: Add your audio processing logic here
      save'
    save' = do
      state <- read ref
      traceM "Current stream length:"
      traceM state.streamLength
      push state.stream $ state.pause <> state.raw
      end state.stream
      stream' <- createStream
      write (state { stream = stream', pause = mempty, raw = mempty, streamLength = 0 }) ref
    createStream = do
      stream' <- newReadable
      uuid <- genUUID
      let filepath = appTempDirectory <> "/" <> toString uuid <> ".opus"
      traceM "Filepath:"
      traceM filepath
      ffmpeg <- spawn "ffmpeg" [ "-f", "f32le", "-ar", show ar, "-i", "pipe:0", "-b:a", "24k", filepath ] defaultSpawnOptions
      _ <- pipe stream' $ stdin ffmpeg
      handleClose ffmpeg do
        state <- read ref
        when (not state.processing) do
          write state { processing = true } ref
          currentDate <- nowDate
          launchAff_ do

            -- TODO: Add your error handling logic here
            transcript <- toAffE $ transcribe deepgram filepath
            let transcriptDirectory = homeDirectory <> "/.local/share/say/" <> (show $ fromEnum $ year currentDate) <> "/" <> (show $ fromEnum $ month currentDate)
            mkdir' transcriptDirectory { mode: mkPerms all none none, recursive: true }
            traceM "Transcript:"
            traceM transcript
            liftEffect $ modify_ (\state' -> state' { processing = false }) ref
      pure stream'

    -- https://github.com/deepgram/deepgram-node-sdk/blob/7c416605fc5953c8777b3685e014cf874c08eecf/README.md?plain=1#L123
    deepgram = createClient key
  stream' <- createStream
  modify_ (\state -> state { stream = stream' }) ref
  launch record save

foreign import tensor :: Tensor

foreign import newReadable :: Effect (Readable ())

ar :: Int
ar = 16000

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

foreign import handleClose :: ChildProcess -> Effect Unit -> Effect Unit

foreign import createClient :: String -> Deepgram

foreign import transcribe :: Deepgram -> String -> Effect (Promise String)

foreign import launch :: (Float32Array -> Effect Unit) -> Effect Unit -> Effect Unit