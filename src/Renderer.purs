module Renderer where

import Prelude

import Data.Array (find)
import Data.Maybe (Maybe(..))
import Debug (traceM)
import Effect (Effect)
import Effect.Aff (launchAff_)
import Promise.Aff (Promise, toAffE)

main :: Effect Unit
main = launchAff_ $ do
  devices <- toAffE getDevices
  let microphoneDevice = find (\device -> device.label == "Microphone (Built-in)") devices
  case microphoneDevice of
    Just device -> toAffE $ record device.deviceId
    Nothing -> traceM "Microphone (Built-in) not found"

foreign import getDevices :: Effect (Promise (Array { deviceId :: String, label :: String }))

foreign import record :: String -> Effect (Promise Unit)