export const length = (float32Array) => {
  return float32Array.length;
};

export const appendFloat32Array = (first) => (second) => {
  const combined = new Float32Array(first.length + second.length);
  combined.set(first);
  combined.set(second, first.length);
  return combined;
};

export const memptyFloat32Array = new Float32Array();

// https://github.com/purescript/purescript-arrays/blob/6554b3d9c1ebb871477ffa88c2f3850d714b42b0/src/Data/Array.js#L255-L257
export const sliceImpl = function (s, e, l) {
  return l.slice(s, e);
};
