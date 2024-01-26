module Renderer where

import Prelude

import Effect (Effect)
import Effect.Aff (launchAff_)
import Promise.Aff (Promise, toAffE)

main :: Effect Unit
main = launchAff_ $ do
  audioDevices <- toAffE getAudioDevices
  toAffE record

foreign import getAudioDevices :: Effect (Promise (Array { deviceId :: String, label :: String }))

foreign import record :: Effect (Promise Unit)