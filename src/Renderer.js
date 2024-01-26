const { ipcRenderer } = require("electron");

export async function getDevices() {
  await navigator.mediaDevices.getUserMedia({ audio: true });
  return navigator.mediaDevices.enumerateDevices();
}

export const record = (deviceId) => async () => {
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
