const { ipcRenderer } = require("electron");

export async function getDevices() {
  await navigator.mediaDevices.getUserMedia({ audio: true });
  return navigator.mediaDevices.enumerateDevices();
}

// https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/String/endsWith#searchstring
export const endsWith = (searchString) => (str) => str.endsWith(searchString);

export const record = (deviceId) => async () => {
  console.log("Recording with device ID:");
  console.log(deviceId);
  const stream = await navigator.mediaDevices.getUserMedia({
    audio: { deviceId: { exact: deviceId } },
  });
  const context = new AudioContext({ sampleRate: 16000 });
  await context.audioWorklet.addModule("audio.js");
  const source = context.createMediaStreamSource(stream);
  const processor = new AudioWorkletNode(context, "processor");
  source.connect(processor);
  processor.port.onmessage = (event) => {
    ipcRenderer.send("audio", event.data);
  };
};
