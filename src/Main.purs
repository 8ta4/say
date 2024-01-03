module Main where

import Prelude

import Data.Array (intercalate)
import Data.Int (floor, toNumber)
import Data.Maybe (Maybe(..))
import Data.UUID (genUUID, toString)
import Debug (traceM)
import Effect (Effect)
import Effect.Aff (launchAff_)
import Effect.Class (liftEffect)
import Effect.Ref (modify_, new, read, write)
import Float32Array (Float32Array, length, splitAt, takeEnd)
import Node.ChildProcess (ChildProcess, spawn, stdin)
import Node.Encoding (Encoding(..))
import Node.FS.Aff (appendTextFile, mkdir')
import Node.FS.Perms (all, mkPerms, none)
import Node.FS.Sync (exists, mkdtemp, readTextFile)
import Node.OS (homedir, tmpdir)
import Node.Stream (Readable, pipe)
import Promise.Aff (Promise, toAffE)

foreign import data Tensor :: Type

foreign import data Deepgram :: Type

foreign import data Session :: Type

main :: Effect Unit
main = do
  fixPath
  appRootPath <- getAppRootPath
  launchAff_ $ do
    session <- toAffE $ createSession $ appRootPath <> "/vad.onnx"
    liftEffect do
      tempDirectory <- tmpdir

      -- https://nodejs.org/api/fs.html#fspromisesmkdtempprefix-options:~:text=mkdtemp(join(tmpdir()%2C%20%27foo%2D%27))
      appTempDirectory <- mkdtemp $ tempDirectory <> "/say-"
      traceM "App temp directory:"
      traceM appTempDirectory
      homeDirectoryPath <- homedir
      key <- readTextFile UTF8 $ homeDirectoryPath <> "/.config/say/key"
      stream <- newReadable

      -- TODO: Enable concurrent processing.
      -- https://github.com/snakers4/silero-vad/blob/94504ece54c8caeebb808410b08ae55ee82dba82/utils_vad.py#L210-L211
      ref <- new { stream: stream, streamLength: 0, pad: mempty, pauseLength: 0, raw: mempty, h: tensor, c: tensor, processing: false, manual: false }
      let
        record audio = do
          state <- read ref
          let raw = state.raw <> audio

          -- https://developer.mozilla.org/en-US/docs/Web/API/AudioWorkletProcessor/process#sect1
          if length raw >= windowSizeSamples then do
            let { before, after } = splitAt windowSizeSamples raw
            write (state { raw = after }) ref
            launchAff_ $ do
              -- TODO: Ensure `run` is not executed concurrently to avoid using incorrect `h` and `c`
              result <- toAffE $ run session before state.h state.c
              liftEffect do
                state' <- read ref
                let state'' = state' { h = result.h, c = result.c }
                if (0.5 < result.probability) then do
                  write (state'' { streamLength = state'.streamLength + length state'.pad + length before, pad = mempty, pauseLength = 0 }) ref
                  push state'.stream $ state'.pad <> before
                else do
                  let state''' = state'' { pauseLength = state'.pauseLength + length before }
                  if state'.pauseLength <= samplesInPause then do
                    write state''' { streamLength = state'.streamLength + length before } ref
                    push state'.stream before
                  else write state''' { pad = takeEnd samplesInPause $ state'.pad <> before } ref
                  when (samplesInStream < state'.streamLength && samplesInPause < state'.pauseLength) save'
          else
            write (state { raw = raw }) ref
        save = do
          modify_ (\state -> state { manual = true }) ref
          save'
        save' = do
          state <- read ref
          traceM "Current stream length:"
          traceM state.streamLength
          push state.stream $ state.pad <> state.raw
          end state.stream
          stream' <- createStream
          write (state { stream = stream', streamLength = 0, pad = mempty, pauseLength = 0, raw = mempty }) ref
        createStream = do
          stream' <- newReadable
          uuid <- genUUID
          let audioFilepath = appTempDirectory <> "/" <> toString uuid <> ".opus"
          traceM "Audio filepath:"
          traceM audioFilepath
          ffmpeg <- spawn "ffmpeg" [ "-f", "f32le", "-ar", show ar, "-i", "pipe:0", "-b:a", "24k", audioFilepath ]
          _ <- pipe stream' $ stdin ffmpeg
          handleClose ffmpeg do
            state <- read ref
            when (not state.processing) do
              write state { processing = true } ref
              currentDate <- getCurrentDate
              let
                transcriptDirectoryPath = homeDirectoryPath <> "/.local/share/say/" <> currentDate.year <> "/" <> currentDate.month
                transcriptFilepath = transcriptDirectoryPath <> "/" <> currentDate.day <> ".txt"
              fileExists <- exists transcriptFilepath
              launchAff_ do

                -- TODO: Add your error handling logic here
                maybeParagraphs <- toAffE $ transcribe deepgram audioFilepath
                case maybeParagraphs of
                  Just paragraphs -> do
                    let transcript = (if fileExists then (<>) "\n\n" else identity) $ intercalate "\n" $ map _.text $ paragraphs.paragraphs >>= _.sentences
                    mkdir' transcriptDirectoryPath { mode: mkPerms all none none, recursive: true }
                    appendTextFile UTF8 transcriptFilepath transcript
                  _ -> pure unit
                liftEffect do
                  when state.manual $ void $ spawn "code" [ "-g", transcriptFilepath <> ":" <> "10000" ]
                  modify_ (\state' -> state' { processing = false, manual = false }) ref
          pure stream'

        -- https://github.com/deepgram/deepgram-node-sdk/blob/7c416605fc5953c8777b3685e014cf874c08eecf/README.md?plain=1#L123
        deepgram = createClient key
      stream' <- createStream
      modify_ (\state -> state { stream = stream' }) ref
      launch record save

foreign import fixPath :: Effect Unit

foreign import getAppRootPath :: Effect String

foreign import createSession :: String -> Effect (Promise Session)

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

foreign import run :: Session -> Float32Array -> Tensor -> Tensor -> Effect (Promise { probability :: Number, h :: Tensor, c :: Tensor })

foreign import push :: Readable () -> Float32Array -> Effect Unit

-- https://github.com/snakers4/silero-vad/blob/5e7ee10ee065ab2b98751dd82b28e3c6360e19aa/utils_vad.py#L207
windowSizeSamples :: Int
windowSizeSamples = 1536

foreign import end :: Readable () -> Effect Unit

foreign import handleClose :: ChildProcess -> Effect Unit -> Effect Unit

foreign import getCurrentDate :: Effect { year :: String, month :: String, day :: String }

transcribe :: Deepgram -> String -> Effect (Promise (Maybe { paragraphs :: Array { sentences :: Array { text :: String } } }))
transcribe = transcribeImpl Just Nothing

foreign import transcribeImpl :: forall a. (a -> Maybe a) -> Maybe a -> Deepgram -> String -> Effect (Promise (Maybe a))

foreign import createClient :: String -> Deepgram

foreign import launch :: (Float32Array -> Effect Unit) -> Effect Unit -> Effect Unit
