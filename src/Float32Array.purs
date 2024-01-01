module Float32Array where

import Prelude

import Data.Function.Uncurried (Fn3, runFn3)

foreign import data Float32Array :: Type

foreign import appendFloat32Array :: Float32Array -> Float32Array -> Float32Array

instance semigroupFloat32Array :: Semigroup Float32Array where
  append = appendFloat32Array

foreign import memptyFloat32Array :: Float32Array

instance monoidFloat32Array :: Monoid Float32Array where
  mempty = memptyFloat32Array

-- https://github.com/purescript/purescript-arrays/blob/6554b3d9c1ebb871477ffa88c2f3850d714b42b0/src/Data/Array.purs#L243
foreign import length :: Float32Array -> Int

-- https://github.com/purescript/purescript-arrays/blob/6554b3d9c1ebb871477ffa88c2f3850d714b42b0/src/Data/Array.purs#L713-L715
splitAt :: Int -> Float32Array -> { before :: Float32Array, after :: Float32Array }
splitAt i xs | i <= 0 = { before: mempty, after: xs }
splitAt i xs = { before: slice 0 i xs, after: slice i (length xs) xs }

-- https://github.com/purescript/purescript-arrays/blob/6554b3d9c1ebb871477ffa88c2f3850d714b42b0/src/Data/Array.purs#L929C30-L930
slice :: Int -> Int -> Float32Array -> Float32Array
slice = runFn3 sliceImpl

-- https://github.com/purescript/purescript-arrays/blob/6554b3d9c1ebb871477ffa88c2f3850d714b42b0/src/Data/Array.purs#L932
foreign import sliceImpl :: Fn3 Int Int Float32Array Float32Array
