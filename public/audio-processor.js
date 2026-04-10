class AudioChunkProcessor extends AudioWorkletProcessor {
  process(inputs) {
    const channel = inputs[0]?.[0];
    if (channel) {
      const i16 = new Int16Array(channel.length);
      for (let i = 0; i < channel.length; i++) {
        i16[i] = Math.max(-32768, Math.min(32767, channel[i] * 32768));
      }
      // Transfer the buffer (zero-copy) to the main thread
      this.port.postMessage(i16.buffer, [i16.buffer]);
    }
    return true;
  }
}

registerProcessor("audio-chunk-processor", AudioChunkProcessor);
