const { ipcRenderer } = require("electron");

export const audioContext = () => new AudioContext({ sampleRate: 16000 });

export async function getDevices() {
  await navigator.mediaDevices.getUserMedia({ audio: true });
  return navigator.mediaDevices.enumerateDevices();
}

// https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/String/endsWith#searchstring
export const endsWith = (searchString) => (str) => str.endsWith(searchString);

export const record =
  (deviceId) => (oldContext) => (newContext) => async () => {
    oldContext.close();
    const stream = await navigator.mediaDevices.getUserMedia({
      audio: { deviceId: { exact: deviceId } },
    });
    await newContext.audioWorklet.addModule("audio.js");
    const source = newContext.createMediaStreamSource(stream);
    const processor = new AudioWorkletNode(newContext, "processor");
    source.connect(processor);
    processor.port.onmessage = (event) => {
      ipcRenderer.send("audio", event.data);
    };
  };

export const handle = (process) => () => {
  navigator.mediaDevices.ondevicechange = process;
};
