class Processor extends AudioWorkletProcessor {
  // https://developer.mozilla.org/en-US/docs/Web/API/AudioWorkletProcessor/process#syntax
  process(inputs, outputs, parameters) {
    this.port.postMessage(inputs[0][0]);
    return true;
  }
}

registerProcessor("processor", Processor);
