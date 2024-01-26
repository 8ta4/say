const { ipcRenderer } = require("electron");

export async function getAudioDevices() {
  await navigator.mediaDevices.getUserMedia({ audio: true });
  return navigator.mediaDevices.enumerateDevices();
}

export async function record() {
  const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
  const context = new AudioContext({ sampleRate: 16000 });
  await context.audioWorklet.addModule("audio.js");
  const source = context.createMediaStreamSource(stream);
  const processor = new AudioWorkletNode(context, "processor");
  source.connect(processor);
  processor.port.onmessage = (event) => {
    ipcRenderer.send("audio", event.data);
  };
}
