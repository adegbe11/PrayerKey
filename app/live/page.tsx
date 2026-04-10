"use client";
import { useEffect, useRef, useState } from "react";
import { io, type Socket } from "socket.io-client";
import type { ServerToClientEvents, ClientToServerEvents, VerseDetectedPayload } from "@/types/sermon";

type ServiceState = "idle" | "live" | "ended";

export default function LivePage() {
  const [state,      setState]      = useState<ServiceState>("idle");
  const [serviceId,  setServiceId]  = useState("");
  const [verses,     setVerses]     = useState<VerseDetectedPayload[]>([]);
  const [transcript, setTranscript] = useState("");
  const [micError,   setMicError]   = useState("");
  const socketRef = useRef<Socket<ServerToClientEvents, ClientToServerEvents> | null>(null);
  const streamRef = useRef<MediaStream | null>(null);
  const ctxRef    = useRef<AudioContext | null>(null);

  async function startService() {
    setMicError("");
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      streamRef.current = stream;

      const res  = await fetch("/api/service/start", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ title: "Sunday Service" }) });
      const data = await res.json();
      const sid  = data.serviceId as string;
      setServiceId(sid);

      const socket: Socket<ServerToClientEvents, ClientToServerEvents> = io(
        process.env.NEXT_PUBLIC_SOCKET_URL ?? "http://localhost:3001",
        { transports: ["websocket", "polling"] }
      );
      socketRef.current = socket;

      socket.on("connect", () => socket.emit("service:join", sid));
      socket.on("verse:detected",  (p) => addVerse(p));
      socket.on("verse:display",   (p) => addVerse(p));
      socket.on("verse:suggested", (p) => { if (p.confidence >= 0.7) addVerse(p); });
      socket.on("transcript:update", ({ text, isFinal }) => { if (isFinal) setTranscript(text); });

      const ctx = new AudioContext({ sampleRate: 16000 });
      ctxRef.current = ctx;
      await ctx.audioWorklet.addModule("/audio-processor.js");
      const source = ctx.createMediaStreamSource(stream);
      const worklet = new AudioWorkletNode(ctx, "audio-chunk-processor");
      worklet.port.onmessage = (e: MessageEvent<ArrayBuffer>) => {
        socket.emit("audio:chunk", e.data);
      };
      source.connect(worklet);
      worklet.connect(ctx.destination);

      setState("live");
    } catch {
      setMicError("Microphone access is needed. Please allow it and try again.");
    }
  }

  function addVerse(p: VerseDetectedPayload) {
    setVerses((prev) => [p, ...prev.filter((v) => v.verseRef !== p.verseRef)].slice(0, 20));
  }

  function endService() {
    socketRef.current?.disconnect();
    streamRef.current?.getTracks().forEach((t) => t.stop());
    ctxRef.current?.close();
    setState("ended");
  }

  useEffect(() => () => {
    socketRef.current?.disconnect();
    streamRef.current?.getTracks().forEach((t) => t.stop());
    ctxRef.current?.close();
  }, []);

  return (
    <div style={{ maxWidth: "760px", margin: "0 auto", padding: "0 0 80px" }}>

      {/* ── Page header ── */}
      <div className="animate-fadeUp" style={{ textAlign: "center", marginBottom: "48px" }}>
        <div style={{
          display:      "inline-flex",
          alignItems:   "center",
          gap:          "8px",
          padding:      "4px 12px",
          border:       "1.5px solid rgba(255,59,48,0.35)",
          borderRadius: "4px",
          marginBottom: "20px",
          background:   "rgba(255,59,48,0.06)",
          boxShadow:    "3px 3px 0 0 rgba(255,59,48,0.15)",
        }}>
          <span style={{ width: "6px", height: "6px", borderRadius: "50%", background: "#FF3B30", display: "inline-block", animation: state === "live" ? "liveDot 1.2s ease infinite" : "none", opacity: state === "live" ? 1 : 0.5 }} />
          <span style={{ fontSize: "10px", fontWeight: 700, color: "#FF3B30", letterSpacing: "0.12em", textTransform: "uppercase" }}>
            Live Sermon
          </span>
        </div>

        <h1 style={{
          fontSize:      "clamp(32px, 6vw, 56px)",
          fontWeight:    800,
          color:         "#fff",
          margin:        "0 0 14px",
          letterSpacing: "-0.03em",
          lineHeight:    1.05,
        }}>
          {state === "idle"  ? "Start Your Service" :
           state === "live"  ? "Service is Live" :
           "Service Complete"}
        </h1>
        <p style={{
          fontSize:    "clamp(15px, 1.4vw, 17px)",
          color:       "rgba(255,255,255,0.42)",
          margin:      0,
          lineHeight:  1.65,
          maxWidth:    "440px",
          marginInline:"auto",
        }}>
          {state === "idle"  ? "Press the button, start preaching. Bible verses appear on the screen by themselves." :
           state === "live"  ? "Listening for Bible verses. Keep preaching." :
           `${verses.length} verse${verses.length !== 1 ? "s" : ""} detected this session.`}
        </p>
      </div>

      {/* ══ IDLE ══ */}
      {state === "idle" && (
        <div className="animate-fadeUp delay-100" style={{ textAlign: "center" }}>

          {micError && (
            <div style={{ marginBottom: "24px", padding: "14px 18px", borderRadius: "8px", background: "rgba(255,59,48,0.08)", border: "1.5px solid rgba(255,59,48,0.25)", color: "#FF3B30", fontSize: "14px", boxShadow: "3px 3px 0 0 rgba(255,59,48,0.15)" }}>
              {micError}
            </div>
          )}

          {/* Big start button */}
          <button
            onClick={startService}
            style={{
              display:      "inline-flex",
              alignItems:   "center",
              justifyContent:"center",
              gap:          "12px",
              padding:      "20px 52px",
              minHeight:    "72px",
              borderRadius: "6px",
              border:       "2px solid #FF3B30",
              background:   "#FF3B30",
              color:        "#fff",
              fontSize:     "clamp(16px, 2vw, 20px)",
              fontWeight:   800,
              cursor:       "pointer",
              letterSpacing:"-0.01em",
              boxShadow:    "5px 5px 0 0 rgba(255,59,48,0.35)",
              transition:   "transform 150ms ease, box-shadow 150ms ease",
            }}
            onMouseEnter={e => {
              (e.currentTarget as HTMLButtonElement).style.transform = "translate(-3px,-3px)";
              (e.currentTarget as HTMLButtonElement).style.boxShadow = "8px 8px 0 0 rgba(255,59,48,0.35)";
            }}
            onMouseLeave={e => {
              (e.currentTarget as HTMLButtonElement).style.transform = "translate(0,0)";
              (e.currentTarget as HTMLButtonElement).style.boxShadow = "5px 5px 0 0 rgba(255,59,48,0.35)";
            }}
          >
            <span style={{ width: "10px", height: "10px", borderRadius: "50%", background: "#fff", display: "inline-block", flexShrink: 0 }} />
            Start Service
          </button>

          <p style={{ marginTop: "16px", fontSize: "12px", color: "rgba(255,255,255,0.22)", letterSpacing: "0.04em" }}>
            Requires microphone · Verse detection is fully automatic
          </p>

          {/* Info cards */}
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))", gap: "12px", marginTop: "48px", textAlign: "left" }}>
            {[
              { icon: "🎤", title: "Auto Detection", body: "Quotes and references are detected in real time as you preach." },
              { icon: "📺", title: "Projector Screen", body: "Open a second window on your display — verses appear instantly." },
              { icon: "📖", title: "11 Translations", body: "NIV, KJV, ESV, NKJV, NLT and more — switch anytime." },
            ].map((c) => (
              <div key={c.title} style={{
                padding:      "20px",
                background:   "rgba(255,255,255,0.025)",
                border:       "1px solid rgba(255,255,255,0.07)",
                borderRadius: "12px",
                backdropFilter: "blur(12px)",
              }}>
                <div style={{ fontSize: "22px", marginBottom: "8px" }}>{c.icon}</div>
                <div style={{ fontSize: "13px", fontWeight: 700, color: "#fff", marginBottom: "6px", letterSpacing: "-0.01em" }}>{c.title}</div>
                <div style={{ fontSize: "12px", color: "rgba(255,255,255,0.38)", lineHeight: 1.6 }}>{c.body}</div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* ══ LIVE ══ */}
      {state === "live" && (
        <div>
          {/* Status bar */}
          <div style={{
            display:        "flex",
            alignItems:     "center",
            justifyContent: "space-between",
            flexWrap:       "wrap",
            gap:            "10px",
            marginBottom:   "20px",
            padding:        "14px 20px",
            background:     "rgba(255,59,48,0.07)",
            border:         "1.5px solid rgba(255,59,48,0.2)",
            borderRadius:   "10px",
            boxShadow:      "3px 3px 0 0 rgba(255,59,48,0.12)",
          }}>
            <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
              <span style={{ width: "8px", height: "8px", borderRadius: "50%", background: "#FF3B30", display: "inline-block", animation: "liveDot 1.2s ease infinite" }} />
              <span style={{ fontSize: "14px", fontWeight: 700, color: "#FF3B30", letterSpacing: "0.02em" }}>LIVE — Listening</span>
            </div>
            <div style={{ display: "flex", gap: "8px" }}>
              <a href={`/live/projector?serviceId=${serviceId}`} target="_blank" rel="noreferrer" style={{
                padding:      "8px 16px",
                minHeight:    "36px",
                borderRadius: "4px",
                border:       "1.5px solid rgba(255,255,255,0.12)",
                color:        "rgba(255,255,255,0.65)",
                fontSize:     "12px",
                textDecoration:"none",
                fontWeight:   600,
                display:      "flex",
                alignItems:   "center",
                gap:          "6px",
                transition:   "all 150ms ease",
              }}>
                📺 Open Projector
              </a>
              <button onClick={endService} style={{
                padding:      "8px 16px",
                minHeight:    "36px",
                borderRadius: "4px",
                border:       "1.5px solid rgba(255,59,48,0.35)",
                background:   "rgba(255,59,48,0.1)",
                color:        "#FF3B30",
                fontSize:     "12px",
                fontWeight:   700,
                cursor:       "pointer",
                display:      "flex",
                alignItems:   "center",
                gap:          "6px",
              }}>
                ■ End Service
              </button>
            </div>
          </div>

          {/* Transcript ticker */}
          {transcript && (
            <div style={{ marginBottom: "16px", padding: "10px 16px", background: "rgba(255,255,255,0.02)", border: "1px solid rgba(255,255,255,0.06)", borderRadius: "8px" }}>
              <p style={{ fontSize: "13px", color: "rgba(255,255,255,0.3)", margin: 0, fontStyle: "italic", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                🎤 {transcript}
              </p>
            </div>
          )}

          {/* Verses */}
          {verses.length === 0 ? (
            <div style={{
              textAlign:  "center",
              padding:    "72px 20px",
              background: "rgba(255,255,255,0.015)",
              border:     "1px solid rgba(255,255,255,0.06)",
              borderRadius:"16px",
            }}>
              <div style={{ fontSize: "40px", marginBottom: "16px", animation: "pulse 2s ease infinite" }}>👂</div>
              <p style={{ fontSize: "16px", color: "rgba(255,255,255,0.25)", margin: 0, lineHeight: 1.6 }}>
                Listening for verses...<br />Start preaching and they&apos;ll appear here automatically.
              </p>
            </div>
          ) : (
            <div style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
              {verses.map((v, i) => (
                <div key={v.verseRef + i} style={{
                  padding:      "clamp(16px,3vw,24px)",
                  borderRadius: "12px",
                  background:   i === 0 ? "rgba(176,124,31,0.09)" : "rgba(255,255,255,0.025)",
                  border:       `1.5px solid ${i === 0 ? "rgba(176,124,31,0.28)" : "rgba(255,255,255,0.07)"}`,
                  boxShadow:    i === 0 ? "3px 3px 0 0 rgba(176,124,31,0.12)" : "none",
                  animation:    i === 0 ? "verseIn 420ms cubic-bezier(0.22,1,0.36,1)" : "none",
                }}>
                  {i === 0 && (
                    <span style={{ fontSize: "10px", fontWeight: 700, color: "#B07C1F", letterSpacing: "0.1em", textTransform: "uppercase", display: "block", marginBottom: "10px" }}>
                      ✦ Just Detected
                    </span>
                  )}
                  <p style={{ fontSize: i === 0 ? "16px" : "14px", color: "rgba(255,255,255,0.85)", lineHeight: 1.7, margin: "0 0 12px", fontStyle: "italic" }}>
                    &ldquo;{v.verseText}&rdquo;
                  </p>
                  <div style={{ display: "flex", alignItems: "center", gap: "10px", flexWrap: "wrap" }}>
                    <span style={{ fontSize: "14px", fontWeight: 700, color: "#B07C1F" }}>{v.verseRef}</span>
                    <span style={{ fontSize: "12px", color: "rgba(255,255,255,0.28)" }}>{v.translation}</span>
                    <span style={{
                      marginLeft: "auto",
                      fontSize:   "12px",
                      fontWeight: 600,
                      color:      v.confidence >= 0.9 ? "#34C759" : v.confidence >= 0.7 ? "#C49A2A" : "rgba(255,255,255,0.3)",
                    }}>
                      {Math.round(v.confidence * 100)}% match
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* ══ ENDED ══ */}
      {state === "ended" && (
        <div className="animate-scaleIn" style={{ textAlign: "center", padding: "60px 20px" }}>
          <div style={{
            display:      "inline-flex",
            alignItems:   "center",
            justifyContent:"center",
            width:        "72px",
            height:       "72px",
            background:   "rgba(52,199,89,0.1)",
            border:       "2px solid rgba(52,199,89,0.3)",
            borderRadius: "8px",
            boxShadow:    "4px 4px 0 0 rgba(52,199,89,0.15)",
            fontSize:     "32px",
            marginBottom: "24px",
          }}>
            ✓
          </div>
          <h2 style={{ fontSize: "clamp(24px, 4vw, 36px)", fontWeight: 700, color: "#fff", marginBottom: "10px", letterSpacing: "-0.02em" }}>
            Service Complete
          </h2>
          <p style={{ fontSize: "16px", color: "rgba(255,255,255,0.4)", marginBottom: "36px", lineHeight: 1.6 }}>
            {verses.length} verse{verses.length !== 1 ? "s" : ""} were detected during this service.
          </p>
          <button
            onClick={() => { setVerses([]); setTranscript(""); setState("idle"); }}
            style={{
              padding:      "14px 36px",
              minHeight:    "52px",
              borderRadius: "6px",
              border:       "2px solid #FF3B30",
              background:   "#FF3B30",
              color:        "#fff",
              fontSize:     "15px",
              fontWeight:   700,
              cursor:       "pointer",
              boxShadow:    "4px 4px 0 0 rgba(255,59,48,0.3)",
              transition:   "transform 150ms ease, box-shadow 150ms ease",
              letterSpacing:"-0.01em",
            }}
            onMouseEnter={e => {
              (e.currentTarget as HTMLButtonElement).style.transform = "translate(-2px,-2px)";
              (e.currentTarget as HTMLButtonElement).style.boxShadow = "6px 6px 0 0 rgba(255,59,48,0.3)";
            }}
            onMouseLeave={e => {
              (e.currentTarget as HTMLButtonElement).style.transform = "translate(0,0)";
              (e.currentTarget as HTMLButtonElement).style.boxShadow = "4px 4px 0 0 rgba(255,59,48,0.3)";
            }}
          >
            Start a New Service
          </button>
        </div>
      )}

      <style>{`
        @keyframes verseIn { from { opacity:0; transform:translateY(12px); } to { opacity:1; transform:translateY(0); } }
        @keyframes pulse   { 0%,100% { opacity:1; } 50% { opacity:0.35; } }
        @keyframes liveDot { 0%,100% { opacity:1; transform:scale(1); } 50% { opacity:0.4; transform:scale(0.7); } }
      `}</style>
    </div>
  );
}
