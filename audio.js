class Processor extends AudioWorkletProcessor {
  // https://developer.mozilla.org/en-US/docs/Web/API/AudioWorkletProcessor/process#syntax
  process(inputs, outputs, parameters) {
    return true;
  }
}

registerProcessor("processor", Processor);
