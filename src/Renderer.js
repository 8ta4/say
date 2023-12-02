export async function record() {
  const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
  const context = new AudioContext();
  await context.audioWorklet.addModule("audio.js");
  const source = context.createMediaStreamSource(stream);
  const processor = new AudioWorkletNode(context, "processor");
  source.connect(processor);
  processor.port.onmessage = (event) => {};
}
