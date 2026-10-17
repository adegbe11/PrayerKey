# The speech engine (sherpa-onnx) is native code that reads its settings from
# the Kotlin config objects by their Java names and types - GetFieldID on
# "modelConfig", "Lcom/k2fsa/sherpa/onnx/OnlineModelConfig;" and so on. The
# library ships no rules of its own, so R8 renamed those classes and removed
# their fields as unused (nothing on the Java side reads them), and the
# offline transcription and punctuation failed in release builds only.
-keep class com.k2fsa.sherpa.onnx.** { *; }
