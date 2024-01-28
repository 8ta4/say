module Renderer where

import Prelude

import Data.Array (find)
import Data.Maybe (Maybe(..))
import Data.String (Pattern(..), contains)
import Debug (traceM)
import Effect (Effect)
import Effect.Aff (launchAff_)
import Promise.Aff (Promise, toAffE)

main :: Effect Unit
main = launchAff_ $ do
  devices <- toAffE getDevices

  -- Exclude the default device because when an external microphone is unplugged, the recording stops working if the default device is selected.
  let microphoneDevice = find (\device -> endsWith "(Built-in)" device.label && device.deviceId /= "default" && not (contains (Pattern "External") device.label)) devices
  case microphoneDevice of
    Just device -> toAffE $ record device.deviceId
    Nothing -> traceM "(Built-in) not found"

foreign import getDevices :: Effect (Promise (Array { deviceId :: String, label :: String }))

foreign import endsWith :: String -> String -> Boolean

foreign import record :: String -> Effect (Promise Unit)