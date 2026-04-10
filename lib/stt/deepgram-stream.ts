import { DeepgramClient } from "@deepgram/sdk";
// ListenV1Results is namespaced under Deepgram.listen
import type { listen as DeepgramListen } from "@deepgram/sdk";
type ListenV1Results = DeepgramListen.ListenV1Results;
import { detectVerseFromText } from "@/lib/ai/verse-detection";
import { semanticVerseSearch } from "@/lib/ai/bible-embeddings";
import type { VerseDetectedPayload } from "@/types/sermon";

// Callback types
type OnVerseDetected = (payload: VerseDetectedPayload, isSuggestion: boolean) => void;
type OnTranscript    = (text: string, isFinal: boolean) => void;

// Minimal live socket interface (Deepgram SDK v5)
interface LiveSocket {
  readyState: number;
  on(event: "open",    cb: () => void): void;
  on(event: "message", cb: (msg: LiveSocketMessage) => void): void;
  on(event: "close",   cb: (ev: unknown) => void): void;
  on(event: "error",   cb: (err: Error) => void): void;
  connect(): void;
  sendMedia(data: ArrayBufferLike | Blob | ArrayBufferView): void;
  close(): void;
}

type LiveSocketMessage = ListenV1Results | { type: string };

let deepgramClient: DeepgramClient | null = null;

function getDeepgram() {
  if (!deepgramClient) {
    deepgramClient = new DeepgramClient({ apiKey: process.env.DEEPGRAM_API_KEY ?? "" });
  }
  return deepgramClient;
}

export class DeepgramStreamer {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private socket: LiveSocket | null = null;
  private translation: string;
  private onVerse: OnVerseDetected;
  private onTranscript: OnTranscript;
  private buffer: string[] = [];
  private detectionInProgress = false;

  constructor(opts: {
    translation:  string;
    onVerse:      OnVerseDetected;
    onTranscript: OnTranscript;
  }) {
    this.translation  = opts.translation;
    this.onVerse      = opts.onVerse;
    this.onTranscript = opts.onTranscript;
  }

  async connect() {
    const dg = getDeepgram();

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const socket = await (dg.listen.v1 as any).connect({
      model:           "nova-2",
      smart_format:    "true",
      punctuate:       "true",
      interim_results: "true",
      language:        "en-US",
      Authorization:   process.env.DEEPGRAM_API_KEY ?? "",
    }) as LiveSocket;

    this.socket = socket;

    socket.on("open", () => {
      console.log("[deepgram] Connection opened");
    });

    socket.on("message", async (msg) => {
      // Only handle transcript results
      if (msg.type !== "Results") return;

      const result  = msg as ListenV1Results;
      const alt     = result.channel?.alternatives?.[0];
      const text    = alt?.transcript ?? "";
      const isFinal = result.is_final ?? false;

      if (!text.trim()) return;

      this.onTranscript(text, isFinal);

      if (isFinal) {
        this.buffer.push(text);
        // Keep rolling window of ~3 sentences
        if (this.buffer.length > 3) this.buffer.shift();
        await this.runDetection(this.buffer.join(" "));
      }
    });

    socket.on("error", (err) => {
      console.error("[deepgram] Error:", err);
    });

    socket.on("close", () => {
      console.log("[deepgram] Connection closed");
    });

    // Establish the WebSocket connection
    socket.connect();
  }

  private async runDetection(text: string) {
    if (this.detectionInProgress) return;
    this.detectionInProgress = true;

    try {
      const result = await detectVerseFromText(text, this.translation);

      if (result.detected && result.confidence >= 0.75) {
        this.onVerse({
          verseRef:    result.verseRef,
          verseText:   result.verseText,
          translation: result.translation,
          confidence:  result.confidence,
          detectionMs: result.detectionMs,
          snippetUsed: result.snippetUsed,
          detectedAt:  new Date().toISOString(),
        }, false);

      } else if (result.detected && result.confidence >= 0.60) {
        const matches = await semanticVerseSearch(result.snippetUsed || text, 1, this.translation);

        if (matches.length > 0 && matches[0].score >= 0.82) {
          this.onVerse({
            verseRef:    matches[0].verseRef,
            verseText:   matches[0].verseText,
            translation: matches[0].translation,
            confidence:  matches[0].score,
            detectionMs: result.detectionMs,
            snippetUsed: result.snippetUsed,
            detectedAt:  new Date().toISOString(),
          }, false);
        } else {
          this.onVerse({
            verseRef:    result.verseRef,
            verseText:   result.verseText,
            translation: result.translation,
            confidence:  result.confidence,
            detectionMs: result.detectionMs,
            snippetUsed: result.snippetUsed,
            detectedAt:  new Date().toISOString(),
          }, true);
        }
      }
    } finally {
      this.detectionInProgress = false;
    }
  }

  sendAudio(chunk: Buffer | ArrayBuffer) {
    if (this.socket?.readyState === 1) {
      this.socket.sendMedia(chunk instanceof Buffer ? chunk : new Uint8Array(chunk));
    }
  }

  disconnect() {
    this.socket?.close();
    this.socket = null;
  }
}
