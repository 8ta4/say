module Renderer where

import Prelude

import Data.Array (find)
import Data.Maybe (Maybe(..))
import Debug (traceM)
import Effect (Effect)
import Effect.Aff (launchAff_)
import Effect.Ref (new, read, write)
import Promise.Aff (Promise, toAffE)

foreign import data AudioContext :: Type

main :: Effect Unit
main = do
  context <- audioContext
  ref <- new context
  let
    process = do
      context' <- read ref
      context'' <- audioContext
      write context'' ref
      launchAff_ $ do
        devices <- toAffE getDevices
        let microphoneDevice = find (\device -> endsWith "(Built-in)" device.label) devices
        case microphoneDevice of
          Just device -> toAffE $ record device.deviceId context' context''
          Nothing -> traceM "(Built-in) not found"
  process
  handle process

foreign import audioContext :: Effect AudioContext

foreign import getDevices :: Effect (Promise (Array { deviceId :: String, label :: String }))

foreign import endsWith :: String -> String -> Boolean

foreign import record :: String -> AudioContext -> AudioContext -> Effect (Promise Unit)

foreign import handle :: Effect Unit -> Effect Unit