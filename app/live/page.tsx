"use client";

import { useEffect, useRef, useState, useCallback } from "react";
import { io, type Socket } from "socket.io-client";
import type {
  ServerToClientEvents,
  ClientToServerEvents,
  VerseDetectedPayload,
  ServiceStatsPayload,
} from "@/types/sermon";

/* ─────────────────────────────────────────────────────────────────────────────
   DESIGN SYSTEM
───────────────────────────────────────────────────────────────────────────── */
const T = {
  /* Backgrounds */
  bg:      "#04040A",
  panel:   "#08080F",
  raised:  "#0D0D18",
  hover:   "#12121E",
  input:   "#0A0A15",

  /* Borders */
  line:    "rgba(255,255,255,0.055)",
  border:  "rgba(255,255,255,0.085)",
  borderHi:"rgba(255,255,255,0.15)",

  /* Brand */
  gold:    "#E8A820",
  goldDim: "rgba(232,168,32,0.12)",
  goldGlow:"rgba(232,168,32,0.06)",

  /* Status */
  red:     "#FF3B30",
  redDim:  "rgba(255,59,48,0.12)",
  green:   "#34C759",
  greenDim:"rgba(52,199,89,0.12)",
  blue:    "#0A84FF",
  orange:  "#FF9F0A",

  /* Text */
  t1:      "#FFFFFF",
  t2:      "rgba(255,255,255,0.62)",
  t3:      "rgba(255,255,255,0.35)",
  t4:      "rgba(255,255,255,0.18)",
  t5:      "rgba(255,255,255,0.08)",
};

const F = { mono: "'SF Mono','Fira Code','Consolas',monospace" };

/* ─────────────────────────────────────────────────────────────────────────────
   HELPERS
───────────────────────────────────────────────────────────────────────────── */
type SvcState = "idle" | "live" | "ended";
interface SearchResult { ref: string; text: string; match: string }

function confColor(c: number) {
  if (c >= 0.9) return T.green;
  if (c >= 0.75) return T.gold;
  return T.orange;
}
function fmtDur(s: number) {
  const h = Math.floor(s / 3600);
  const m = Math.floor((s % 3600) / 60).toString().padStart(2, "0");
  const sc = (s % 60).toString().padStart(2, "0");
  return h ? `${h}:${m}:${sc}` : `${m}:${sc}`;
}
function relTime(iso: string) {
  const s = Math.floor((Date.now() - new Date(iso).getTime()) / 1000);
  if (s < 5) return "just now";
  if (s < 60) return `${s}s ago`;
  return `${Math.floor(s / 60)}m ago`;
}

const TRANSLATIONS = ["NIV","KJV","ESV","NLT","NKJV","NASB","CSB","AMP","NRSV"];
const QUICK_REFS   = ["John 3:16","Psalm 23:1","Romans 8:28","Philippians 4:13",
                      "Isaiah 40:31","Jeremiah 29:11","Proverbs 3:5","Matthew 6:33"];

/* ─────────────────────────────────────────────────────────────────────────────
   SUB-COMPONENTS
───────────────────────────────────────────────────────────────────────────── */

/** Tiny pill badge */
function Badge({ label, color, pulse = false }: { label: string; color: string; pulse?: boolean }) {
  return (
    <span style={{
      display:      "inline-flex",
      alignItems:   "center",
      gap:          "5px",
      padding:      "3px 8px",
      borderRadius: "100px",
      border:       `1px solid ${color}40`,
      background:   `${color}14`,
      fontSize:     "9px",
      fontWeight:   700,
      color,
      letterSpacing:"0.09em",
      textTransform:"uppercase",
    }}>
      <span style={{
        width:        "5px",
        height:       "5px",
        borderRadius: "50%",
        background:   color,
        flexShrink:   0,
        animation:    pulse ? "dotPulse 1.4s ease infinite" : "none",
      }} />
      {label}
    </span>
  );
}

/** Panel with header chrome */
function Panel({
  id, title, badge, extra, children, style = {},
}: {
  id?: string;
  title: string;
  badge?: React.ReactNode;
  extra?: React.ReactNode;
  children: React.ReactNode;
  style?: React.CSSProperties;
}) {
  return (
    <section
      id={id}
      style={{
        display:        "flex",
        flexDirection:  "column",
        background:     T.panel,
        borderRight:    `1px solid ${T.line}`,
        overflow:       "hidden",
        position:       "relative",
        ...style,
      }}
    >
      {/* Panel chrome bar */}
      <header style={{
        display:        "flex",
        alignItems:     "center",
        gap:            "8px",
        height:         "34px",
        padding:        "0 14px",
        borderBottom:   `1px solid ${T.line}`,
        flexShrink:     0,
        background:     `linear-gradient(180deg, ${T.raised} 0%, ${T.panel} 100%)`,
      }}>
        <span style={{
          fontSize:     "9.5px",
          fontWeight:   600,
          color:        T.t3,
          letterSpacing:"0.11em",
          textTransform:"uppercase",
          userSelect:   "none",
        }}>
          {title}
        </span>
        {badge && <div style={{ marginLeft: "2px" }}>{badge}</div>}
        {extra && <div style={{ marginLeft: "auto" }}>{extra}</div>}
      </header>

      <div style={{ flex: 1, overflow: "hidden", display: "flex", flexDirection: "column" }}>
        {children}
      </div>
    </section>
  );
}

/** Confidence arc / ring */
function ConfRing({ value, size = 32 }: { value: number; size?: number }) {
  const r       = (size - 4) / 2;
  const circ    = 2 * Math.PI * r;
  const filled  = circ * value;
  const color   = confColor(value);
  return (
    <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} style={{ flexShrink: 0 }}>
      <circle cx={size/2} cy={size/2} r={r} fill="none" stroke={T.t5} strokeWidth="2" />
      <circle
        cx={size/2} cy={size/2} r={r} fill="none"
        stroke={color} strokeWidth="2.5"
        strokeDasharray={`${filled} ${circ - filled}`}
        strokeLinecap="round"
        transform={`rotate(-90 ${size/2} ${size/2})`}
        style={{ transition: "stroke-dasharray 600ms ease" }}
      />
      <text x="50%" y="50%" dominantBaseline="middle" textAnchor="middle"
        style={{ fontSize: "8px", fontFamily: F.mono, fill: color, fontWeight: 700 }}>
        {Math.round(value * 100)}
      </text>
    </svg>
  );
}

/** Audio waveform bars */
function Waveform({ active }: { active: boolean }) {
  return (
    <div style={{ display: "flex", alignItems: "center", gap: "2px", height: "22px" }}>
      {Array.from({ length: 20 }).map((_, i) => (
        <div key={i} style={{
          width:        "2.5px",
          background:   active ? T.red : T.t5,
          borderRadius: "2px",
          minHeight:    "3px",
          animation:    active
            ? `wv ${(0.5 + (i % 7) * 0.12).toFixed(2)}s ease-in-out infinite alternate`
            : "none",
          animationDelay: `${(i * 0.04).toFixed(2)}s`,
          transition:   "background 400ms ease",
        }} />
      ))}
    </div>
  );
}

/** Queue verse card */
function QueueCard({
  verse, isActive, onClick, onPushLive,
}: {
  verse: VerseDetectedPayload; isActive: boolean; onClick: () => void; onPushLive: () => void;
}) {
  return (
    <div
      onClick={onClick}
      style={{
        display:      "flex",
        alignItems:   "flex-start",
        gap:          "10px",
        padding:      "10px 12px",
        background:   isActive ? T.goldDim : "transparent",
        borderLeft:   `2px solid ${isActive ? T.gold : confColor(verse.confidence)}40`,
        cursor:       "pointer",
        transition:   "background 140ms ease",
      }}
      onMouseEnter={e => { if (!isActive) (e.currentTarget as HTMLDivElement).style.background = T.raised; }}
      onMouseLeave={e => { (e.currentTarget as HTMLDivElement).style.background = isActive ? T.goldDim : "transparent"; }}
    >
      <ConfRing value={verse.confidence} size={30} />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: "6px", marginBottom: "3px" }}>
          <span style={{ fontSize: "11.5px", fontWeight: 700, color: T.gold, whiteSpace: "nowrap" }}>{verse.verseRef}</span>
          <span style={{ fontSize: "9px", color: T.t4, fontFamily: F.mono }}>{relTime(verse.detectedAt)}</span>
        </div>
        <p style={{
          fontSize:   "10.5px",
          color:      T.t2,
          lineHeight: 1.5,
          margin:     0,
          fontStyle:  "italic",
          display:    "-webkit-box",
          WebkitLineClamp: 2,
          WebkitBoxOrient: "vertical",
          overflow:   "hidden",
        }}>
          {verse.verseText}
        </p>
      </div>
      <button
        onClick={e => { e.stopPropagation(); onPushLive(); }}
        title="Push to live"
        style={{
          flexShrink:   0,
          width:        "26px",
          height:       "26px",
          borderRadius: "5px",
          border:       `1px solid ${T.green}50`,
          background:   T.greenDim,
          color:        T.green,
          fontSize:     "11px",
          cursor:       "pointer",
          display:      "flex",
          alignItems:   "center",
          justifyContent: "center",
          transition:   "all 120ms ease",
        }}
        onMouseEnter={e => { (e.currentTarget as HTMLButtonElement).style.background = `rgba(52,199,89,0.25)`; }}
        onMouseLeave={e => { (e.currentTarget as HTMLButtonElement).style.background = T.greenDim; }}
      >
        ▶
      </button>
    </div>
  );
}

/* ─────────────────────────────────────────────────────────────────────────────
   MAIN PAGE
───────────────────────────────────────────────────────────────────────────── */
export default function LivePage() {
  /* ── State ────────────────────────────────────────────────────────────────── */
  const [svcState,    setSvcState]    = useState<SvcState>("idle");
  const [serviceId,   setServiceId]   = useState("");
  const [connected,   setConnected]   = useState(false);
  const [micError,    setMicError]    = useState("");
  const [autoMode,    setAutoMode]    = useState(true);
  const [translation, setTranslation] = useState("NIV");
  const [serviceTitle,setServiceTitle]= useState("");

  const [previewVerse, setPreviewVerse] = useState<VerseDetectedPayload | null>(null);
  const [liveVerse,    setLiveVerse]    = useState<VerseDetectedPayload | null>(null);
  const [goLive,       setGoLive]       = useState(false);

  const [queue,      setQueue]      = useState<VerseDetectedPayload[]>([]);
  const [transcript, setTranscript] = useState("");
  const [stats,      setStats]      = useState<ServiceStatsPayload>({ versesDetected: 0, powerMoments: 0, duration: 0, attendees: 0 });

  const [searchQ,    setSearchQ]    = useState("");
  const [searching,  setSearching]  = useState(false);
  const [results,    setResults]    = useState<SearchResult[]>([]);

  /* ── Refs ─────────────────────────────────────────────────────────────────── */
  const socketRef    = useRef<Socket<ServerToClientEvents, ClientToServerEvents> | null>(null);
  const streamRef    = useRef<MediaStream | null>(null);
  const ctxRef       = useRef<AudioContext | null>(null);
  const serviceIdRef = useRef("");
  const searchRef    = useRef<HTMLInputElement>(null);

  useEffect(() => { serviceIdRef.current = serviceId; }, [serviceId]);

  /* ── Verse received ───────────────────────────────────────────────────────── */
  const onVerseReceived = useCallback((v: VerseDetectedPayload) => {
    setQueue(prev => [v, ...prev.filter(x => x.verseRef !== v.verseRef)].slice(0, 40));
    setAutoMode(auto => {
      if (auto) {
        setPreviewVerse(v);
        setLiveVerse(v);
        setGoLive(true);
        socketRef.current?.emit("verse:push", v, serviceIdRef.current);
      } else {
        setPreviewVerse(p => p ?? v);
      }
      return auto;
    });
  }, []);

  /* ── Start service ────────────────────────────────────────────────────────── */
  function startService() {
    setMicError("");

    // ── IMMEDIATELY show the dashboard — nothing blocks this ─────────────────
    const title = `Service — ${new Date().toLocaleDateString("en-US", { weekday: "long", month: "long", day: "numeric" })}`;
    const sid   = `svc-${Date.now()}`;
    setServiceId(sid);
    setServiceTitle(title);
    serviceIdRef.current = sid;
    setSvcState("live");   // ← dashboard opens RIGHT NOW

    // ── Everything else runs async in the background ──────────────────────────
    void (async () => {
      // 1. Create service record (updates title if API responds)
      try {
        const res  = await fetch("/api/service/start", {
          method:  "POST",
          headers: { "Content-Type": "application/json" },
          body:    JSON.stringify({ title }),
        });
        const data = await res.json() as { serviceId: string; title: string };
        setServiceId(data.serviceId);
        setServiceTitle(data.title ?? title);
        serviceIdRef.current = data.serviceId;
      } catch {
        // keep local sid — service already showing, no disruption
      }

      // 2. Connect socket
      try {
        const socket: Socket<ServerToClientEvents, ClientToServerEvents> = io(
          process.env.NEXT_PUBLIC_SOCKET_URL ?? "http://localhost:3001",
          { transports: ["websocket", "polling"], reconnectionAttempts: 5 }
        );
        socketRef.current = socket;
        socket.on("connect",           () => { setConnected(true); socket.emit("service:join", serviceIdRef.current); });
        socket.on("disconnect",        () => setConnected(false));
        socket.on("verse:detected",    onVerseReceived);
        socket.on("verse:display",     onVerseReceived);
        socket.on("verse:suggested",   p => { if (p.confidence >= 0.70) onVerseReceived(p); });
        socket.on("transcript:update", ({ text, isFinal }) => { if (isFinal) setTranscript(text); });
        socket.on("service:stats",     setStats);
      } catch (socketErr) {
        console.error("[live] Socket connection failed:", socketErr);
      }

      // 3. Microphone + audio pipeline
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        streamRef.current = stream;

        const ctx = new AudioContext({ sampleRate: 16000 });
        ctxRef.current = ctx;
        await ctx.audioWorklet.addModule("/audio-processor.js");
        const src     = ctx.createMediaStreamSource(stream);
        const worklet = new AudioWorkletNode(ctx, "audio-chunk-processor");
        worklet.port.onmessage = (e: MessageEvent<ArrayBuffer>) => {
          socketRef.current?.emit("audio:chunk", e.data);
        };
        src.connect(worklet);
        worklet.connect(ctx.destination);
      } catch (micErr) {
        const m = micErr instanceof Error ? micErr.message : "";
        if (m.toLowerCase().includes("denied") || m.toLowerCase().includes("permission")) {
          setMicError("Microphone access denied — click the camera/mic icon in the address bar to allow it.");
        } else {
          console.error("[live] Audio pipeline failed:", micErr);
        }
      }
    })();
  }

  /* ── Push verse to projector ──────────────────────────────────────────────── */
  function pushToLive(verse?: VerseDetectedPayload) {
    const v = verse ?? previewVerse;
    if (!v) return;
    setLiveVerse(v);
    setGoLive(true);
    socketRef.current?.emit("verse:push", v, serviceIdRef.current);
  }

  function changeTranslation(t: string) {
    setTranslation(t);
    socketRef.current?.emit("service:translation", t, serviceIdRef.current);
  }

  /* ── Bible search ─────────────────────────────────────────────────────────── */
  const runSearch = useCallback(async (q: string) => {
    if (!q.trim()) { setResults([]); return; }
    setSearching(true);
    try {
      const r = await fetch(`/api/bible/search?q=${encodeURIComponent(q)}&translation=${translation}`);
      const d = await r.json() as { results: SearchResult[] };
      setResults(d.results ?? []);
    } finally { setSearching(false); }
  }, [translation]);

  useEffect(() => {
    const t = setTimeout(() => { if (searchQ) runSearch(searchQ); else setResults([]); }, 420);
    return () => clearTimeout(t);
  }, [searchQ, runSearch]);

  function stageResult(r: SearchResult) {
    const v: VerseDetectedPayload = {
      verseRef: r.ref, verseText: r.text, translation,
      confidence: 1.0, detectionMs: 0, snippetUsed: "", detectedAt: new Date().toISOString(),
    };
    setPreviewVerse(v);
    setQueue(prev => [v, ...prev.filter(x => x.verseRef !== v.verseRef)].slice(0, 40));
  }

  /* ── End service ──────────────────────────────────────────────────────────── */
  function endService() {
    socketRef.current?.emit("service:leave", serviceIdRef.current);
    socketRef.current?.disconnect();
    streamRef.current?.getTracks().forEach(t => t.stop());
    ctxRef.current?.close();
    setConnected(false);
    setSvcState("ended");
  }

  /* ── Cleanup ──────────────────────────────────────────────────────────────── */
  useEffect(() => () => {
    socketRef.current?.disconnect();
    streamRef.current?.getTracks().forEach(t => t.stop());
    ctxRef.current?.close();
  }, []);

  /* ── Keyboard shortcut: ⌘K / Ctrl+K → focus search ──────────────────────── */
  useEffect(() => {
    function onKey(e: KeyboardEvent) {
      if ((e.metaKey || e.ctrlKey) && e.key === "k") { e.preventDefault(); searchRef.current?.focus(); }
    }
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, []);

  /* ═══════════════════════════════════════════════════════════════════════════
     IDLE  ─ Pre-service start screen
  ═══════════════════════════════════════════════════════════════════════════ */
  if (svcState === "idle") {
    return (
      <>
        <div style={{
          minHeight:      "70vh",
          display:        "flex",
          flexDirection:  "column",
          alignItems:     "center",
          justifyContent: "center",
          padding:        "40px 24px 80px",
          textAlign:      "center",
        }}>
          {/* Logo mark */}
          <div style={{
            width:        "64px",
            height:       "64px",
            borderRadius: "16px",
            background:   `linear-gradient(135deg, ${T.goldDim} 0%, rgba(232,168,32,0.04) 100%)`,
            border:       `1px solid rgba(232,168,32,0.25)`,
            display:      "flex",
            alignItems:   "center",
            justifyContent:"center",
            marginBottom: "28px",
            boxShadow:    `0 0 40px rgba(232,168,32,0.08), 0 1px 0 rgba(255,255,255,0.06) inset`,
          }}>
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
              <path d="M12 2L13.6 8.7L20.5 10L13.6 11.3L12 18L10.4 11.3L3.5 10L10.4 8.7L12 2Z" fill={T.gold} />
            </svg>
          </div>

          <Badge label="Live Sermon Control" color={T.red} />

          <h1 style={{
            fontSize:      "clamp(36px, 5vw, 58px)",
            fontWeight:    800,
            color:         T.t1,
            margin:        "20px 0 16px",
            letterSpacing: "-0.04em",
            lineHeight:    1.04,
          }}>
            Ready to Go Live
          </h1>

          <p style={{
            fontSize:    "clamp(15px, 1.3vw, 17px)",
            color:       T.t3,
            maxWidth:    "420px",
            margin:      "0 0 40px",
            lineHeight:  1.75,
          }}>
            Press start, begin preaching. Verses are detected automatically
            and appear on your congregation&apos;s screen within seconds.
          </p>

          {micError && (
            <div style={{
              marginBottom: "24px",
              padding:      "13px 18px",
              borderRadius: "10px",
              background:   T.redDim,
              border:       `1px solid rgba(255,59,48,0.2)`,
              color:        T.red,
              fontSize:     "13px",
              maxWidth:     "400px",
              lineHeight:   1.5,
            }}>
              {micError}
            </div>
          )}

          <button
            onClick={startService}
            style={{
              display:       "inline-flex",
              alignItems:    "center",
              gap:           "12px",
              padding:       "0 48px",
              height:        "60px",
              borderRadius:  "12px",
              border:        "none",
              background:    `linear-gradient(135deg, #FF4F44 0%, ${T.red} 100%)`,
              color:         "#fff",
              fontSize:      "16px",
              fontWeight:    700,
              cursor:        "pointer",
              letterSpacing: "-0.01em",
              boxShadow:     `0 0 0 1px rgba(255,59,48,0.4), 0 8px 32px rgba(255,59,48,0.28), 0 1px 0 rgba(255,255,255,0.15) inset`,
              transition:    "transform 160ms cubic-bezier(0.34,1.56,0.64,1), box-shadow 160ms ease",
            }}
            onMouseEnter={e => {
              (e.currentTarget as HTMLButtonElement).style.transform = "scale(1.025)";
              (e.currentTarget as HTMLButtonElement).style.boxShadow = `0 0 0 1px rgba(255,59,48,0.5), 0 12px 40px rgba(255,59,48,0.38), 0 1px 0 rgba(255,255,255,0.15) inset`;
            }}
            onMouseLeave={e => {
              (e.currentTarget as HTMLButtonElement).style.transform = "scale(1)";
              (e.currentTarget as HTMLButtonElement).style.boxShadow = `0 0 0 1px rgba(255,59,48,0.4), 0 8px 32px rgba(255,59,48,0.28), 0 1px 0 rgba(255,255,255,0.15) inset`;
            }}
          >
            <span style={{
              width: "10px", height: "10px", borderRadius: "50%",
              background: "#fff", opacity: 0.9, flexShrink: 0,
              animation: "dotPulse 1.4s ease infinite",
            }} />
            Start Service
          </button>

          <p style={{ marginTop: "16px", fontSize: "12px", color: T.t4, letterSpacing: "0.02em" }}>
            Deepgram Nova-2 · Groq AI Detection · Requires microphone
          </p>

          {/* Feature grid */}
          <div style={{
            display:             "grid",
            gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))",
            gap:                 "1px",
            marginTop:           "60px",
            width:               "100%",
            maxWidth:            "680px",
            background:          T.line,
            borderRadius:        "14px",
            overflow:            "hidden",
            border:              `1px solid ${T.border}`,
          }}>
            {[
              { icon: "🎙", title: "Real-Time STT",        body: "Deepgram Nova-2 transcribes your sermon with sub-300ms latency." },
              { icon: "✦",  title: "AI Verse Detection",   body: "Groq Llama detects direct quotes, references, and paraphrases." },
              { icon: "📺", title: "Instant Projection",   body: "Verses appear on the congregation screen as you preach them." },
            ].map(c => (
              <div key={c.title} style={{ padding: "24px 20px", background: T.panel }}>
                <div style={{ fontSize: "22px", marginBottom: "10px" }}>{c.icon}</div>
                <div style={{ fontSize: "13px", fontWeight: 600, color: T.t1, marginBottom: "6px" }}>{c.title}</div>
                <div style={{ fontSize: "12px", color: T.t3, lineHeight: 1.65 }}>{c.body}</div>
              </div>
            ))}
          </div>
        </div>
        <style>{CSS_ANIMATIONS}</style>
      </>
    );
  }

  /* ═══════════════════════════════════════════════════════════════════════════
     ENDED  ─ Post-service summary
  ═══════════════════════════════════════════════════════════════════════════ */
  if (svcState === "ended") {
    return (
      <>
        <div style={{ maxWidth: "520px", margin: "0 auto", textAlign: "center", padding: "60px 20px 80px" }}>
          <div style={{
            width: "72px", height: "72px", borderRadius: "18px",
            background: T.greenDim, border: `1px solid rgba(52,199,89,0.25)`,
            display: "flex", alignItems: "center", justifyContent: "center",
            margin: "0 auto 28px",
            boxShadow: "0 0 40px rgba(52,199,89,0.1)",
            fontSize: "28px",
          }}>
            ✓
          </div>
          <h2 style={{ fontSize: "clamp(26px,4vw,38px)", fontWeight: 700, color: T.t1, marginBottom: "12px", letterSpacing: "-0.03em" }}>
            Service Complete
          </h2>
          <div style={{ display: "flex", justifyContent: "center", gap: "24px", marginBottom: "36px" }}>
            {[
              { label: "Verses Detected",  val: stats.versesDetected, color: T.gold  },
              { label: "Power Moments",    val: stats.powerMoments,   color: T.green },
              { label: "Duration",         val: fmtDur(stats.duration), color: T.t2  },
            ].map(s => (
              <div key={s.label} style={{ textAlign: "center" }}>
                <div style={{ fontSize: "28px", fontWeight: 800, color: s.color, letterSpacing: "-0.03em", fontFamily: F.mono }}>{s.val}</div>
                <div style={{ fontSize: "11px", color: T.t4, marginTop: "2px", letterSpacing: "0.04em" }}>{s.label}</div>
              </div>
            ))}
          </div>
          <button
            onClick={() => {
              setQueue([]); setPreviewVerse(null); setLiveVerse(null);
              setTranscript(""); setStats({ versesDetected: 0, powerMoments: 0, duration: 0, attendees: 0 });
              setSvcState("idle");
            }}
            style={{
              padding: "0 40px", height: "52px", borderRadius: "10px",
              border: "none", background: T.red, color: "#fff",
              fontSize: "15px", fontWeight: 700, cursor: "pointer",
              boxShadow: "0 4px 20px rgba(255,59,48,0.3)",
              transition: "transform 150ms ease",
            }}
          >
            Start New Service
          </button>
        </div>
        <style>{CSS_ANIMATIONS}</style>
      </>
    );
  }

  /* ═══════════════════════════════════════════════════════════════════════════
     LIVE  ─ Broadcast control dashboard
  ═══════════════════════════════════════════════════════════════════════════ */
  return (
    <div style={{
      position:      "fixed",
      top:           "64px",
      left:          0,
      right:         0,
      bottom:        0,
      background:    T.bg,
      display:       "flex",
      flexDirection: "column",
      overflow:      "hidden",
      fontFamily:    "-apple-system,'SF Pro Display','Helvetica Neue',sans-serif",
    }}>

      {/* ════════════════════════════════════════════════════════════════════
          TOP STATUS BAR
      ══════════════════════════════════════════════════════════════════════ */}
      <div style={{
        display:      "flex",
        alignItems:   "center",
        gap:          "10px",
        height:       "46px",
        padding:      "0 16px",
        background:   `linear-gradient(180deg, ${T.raised} 0%, ${T.panel} 100%)`,
        borderBottom: `1px solid ${T.line}`,
        flexShrink:   0,
      }}>
        {/* Live badge */}
        <Badge label="Live" color={T.red} pulse />

        {/* Divider */}
        <div style={{ width: "1px", height: "18px", background: T.line }} />

        {/* Service title */}
        <span style={{ fontSize: "12px", fontWeight: 500, color: T.t2, letterSpacing: "-0.01em" }}>
          {serviceTitle}
        </span>

        {/* Divider */}
        <div style={{ width: "1px", height: "18px", background: T.line }} />

        {/* Socket status */}
        <div style={{ display: "flex", alignItems: "center", gap: "5px" }}>
          <span style={{
            width:        "6px",
            height:       "6px",
            borderRadius: "50%",
            background:   connected ? T.green : T.orange,
            display:      "inline-block",
            boxShadow:    connected ? `0 0 6px ${T.green}` : "none",
            transition:   "background 300ms ease",
          }} />
          <span style={{ fontSize: "11px", color: connected ? T.green : T.orange, fontWeight: 500 }}>
            {connected ? "Connected" : "Reconnecting…"}
          </span>
        </div>

        {/* Divider */}
        <div style={{ width: "1px", height: "18px", background: T.line }} />

        {/* Stats row */}
        <div style={{ display: "flex", gap: "16px" }}>
          {[
            { label: "Detected", val: stats.versesDetected, color: T.gold  },
            { label: "Power",    val: stats.powerMoments,   color: T.green },
            { label: "Duration", val: fmtDur(stats.duration), color: T.t3  },
          ].map(s => (
            <div key={s.label} style={{ display: "flex", alignItems: "baseline", gap: "4px" }}>
              <span style={{ fontSize: "14px", fontWeight: 700, color: s.color, fontFamily: F.mono }}>{s.val}</span>
              <span style={{ fontSize: "9px", color: T.t4, letterSpacing: "0.06em", textTransform: "uppercase" }}>{s.label}</span>
            </div>
          ))}
        </div>

        <div style={{ flex: 1 }} />

        {/* AUTO toggle */}
        <button
          onClick={() => setAutoMode(a => !a)}
          style={{
            display:      "flex",
            alignItems:   "center",
            gap:          "6px",
            padding:      "0 12px",
            height:       "28px",
            borderRadius: "7px",
            border:       `1px solid ${autoMode ? `${T.green}50` : T.border}`,
            background:   autoMode ? T.greenDim : "transparent",
            color:        autoMode ? T.green : T.t3,
            fontSize:     "10.5px",
            fontWeight:   600,
            cursor:       "pointer",
            letterSpacing:"0.06em",
            textTransform:"uppercase",
            transition:   "all 160ms ease",
          }}
        >
          <span style={{
            width: "7px", height: "7px", borderRadius: "50%",
            background: autoMode ? T.green : T.t4,
            animation:  autoMode ? "dotPulse 1.6s ease infinite" : "none",
            flexShrink: 0,
            transition: "background 160ms ease",
          }} />
          Auto {autoMode ? "On" : "Off"}
        </button>

        {/* Translation select */}
        <select
          value={translation}
          onChange={e => changeTranslation(e.target.value)}
          style={{
            height:      "28px",
            padding:     "0 8px",
            background:  T.raised,
            border:      `1px solid ${T.border}`,
            borderRadius:"7px",
            color:       T.t2,
            fontSize:    "11px",
            fontWeight:  500,
            cursor:      "pointer",
            outline:     "none",
          }}
        >
          {TRANSLATIONS.map(t => <option key={t} value={t}>{t}</option>)}
        </select>

        {/* Projector */}
        <a
          href={`/live/projector?serviceId=${serviceId}`}
          target="_blank"
          rel="noreferrer"
          style={{
            display:      "flex",
            alignItems:   "center",
            gap:          "6px",
            padding:      "0 12px",
            height:       "28px",
            borderRadius: "7px",
            border:       `1px solid ${T.border}`,
            background:   "transparent",
            color:        T.t2,
            fontSize:     "11px",
            fontWeight:   500,
            textDecoration:"none",
            transition:   "all 150ms ease",
          }}
          onMouseEnter={e => { (e.currentTarget as HTMLAnchorElement).style.borderColor = T.borderHi; (e.currentTarget as HTMLAnchorElement).style.color = T.t1; }}
          onMouseLeave={e => { (e.currentTarget as HTMLAnchorElement).style.borderColor = T.border; (e.currentTarget as HTMLAnchorElement).style.color = T.t2; }}
        >
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
            <rect x="2" y="3" width="20" height="14" rx="2" />
            <path d="M8 21h8M12 17v4" />
          </svg>
          Projector
        </a>

        {/* End service */}
        <button
          onClick={endService}
          style={{
            display:      "flex",
            alignItems:   "center",
            gap:          "6px",
            padding:      "0 14px",
            height:       "28px",
            borderRadius: "7px",
            border:       `1px solid rgba(255,59,48,0.3)`,
            background:   T.redDim,
            color:        T.red,
            fontSize:     "10.5px",
            fontWeight:   600,
            cursor:       "pointer",
            letterSpacing:"0.04em",
            textTransform:"uppercase",
            transition:   "all 150ms ease",
          }}
          onMouseEnter={e => { (e.currentTarget as HTMLButtonElement).style.background = `rgba(255,59,48,0.2)`; }}
          onMouseLeave={e => { (e.currentTarget as HTMLButtonElement).style.background = T.redDim; }}
        >
          <svg width="8" height="8" viewBox="0 0 8 8" fill="currentColor">
            <rect width="8" height="8" rx="1" />
          </svg>
          End Service
        </button>
      </div>

      {/* ════════════════════════════════════════════════════════════════════
          PANEL GRID  (all panels separated by 1px gap = T.line background)
      ══════════════════════════════════════════════════════════════════════ */}
      <div style={{
        flex:       1,
        display:    "flex",
        flexDirection:"column",
        background: T.line,
        gap:        "1px",
        overflow:   "hidden",
      }}>

        {/* ── TOP ROW ─────────────────────────────────────────────────────── */}
        <div style={{ display: "flex", flex: 1, gap: "1px", overflow: "hidden" }}>

          {/* ── 1. LIVE TRANSCRIPT ──────────────────────────────────────── */}
          <Panel
            title="Transcript"
            style={{ width: "196px", flexShrink: 0 }}
            badge={connected
              ? <Badge label="Live" color={T.red} pulse />
              : <Badge label="Offline" color={T.orange} />
            }
          >
            <div style={{ flex: 1, overflowY: "auto", padding: "14px" }}>
              {/* Audio waveform */}
              <div style={{ marginBottom: "14px" }}>
                <Waveform active={connected} />
              </div>

              {/* Transcript text */}
              <div style={{
                minHeight:    "80px",
                padding:      "12px",
                background:   T.raised,
                borderRadius: "8px",
                border:       `1px solid ${T.line}`,
                marginBottom: "14px",
              }}>
                {transcript ? (
                  <p style={{ fontSize: "11.5px", color: T.t2, lineHeight: 1.7, margin: 0, fontStyle: "italic" }}>
                    {transcript}
                  </p>
                ) : (
                  <p style={{ fontSize: "11px", color: T.t4, lineHeight: 1.6, margin: 0 }}>
                    Start preaching — transcript appears here as you speak…
                  </p>
                )}
              </div>

              {/* Stats */}
              <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
                {[
                  { label: "Detected",       val: stats.versesDetected, color: T.gold  },
                  { label: "Power Moments",  val: stats.powerMoments,   color: T.green },
                  { label: "Duration",       val: fmtDur(stats.duration),color: T.t2   },
                  { label: "Connected",      val: stats.attendees,      color: T.blue  },
                ].map(s => (
                  <div key={s.label} style={{
                    display:        "flex",
                    alignItems:     "center",
                    justifyContent: "space-between",
                    padding:        "6px 0",
                    borderBottom:   `1px solid ${T.line}`,
                  }}>
                    <span style={{ fontSize: "10px", color: T.t4, letterSpacing: "0.04em" }}>{s.label}</span>
                    <span style={{ fontSize: "13px", fontWeight: 700, color: s.color, fontFamily: F.mono }}>{s.val}</span>
                  </div>
                ))}
              </div>
            </div>
          </Panel>

          {/* ── 2. PROGRAM PREVIEW ──────────────────────────────────────── */}
          <Panel
            title="Program Preview"
            style={{ flex: 1 }}
            badge={<span style={{ fontSize: "9px", color: T.t4 }}>Staged — review before going live</span>}
          >
            <div style={{ flex: 1, padding: "16px", display: "flex", flexDirection: "column", gap: "12px" }}>
              {previewVerse ? (
                <>
                  {/* Verse display */}
                  <div style={{
                    flex:           1,
                    padding:        "20px 24px",
                    background:     `linear-gradient(135deg, ${T.goldGlow} 0%, ${T.raised} 100%)`,
                    border:         `1px solid rgba(232,168,32,0.18)`,
                    borderRadius:   "10px",
                    display:        "flex",
                    flexDirection:  "column",
                    justifyContent: "center",
                    alignItems:     "center",
                    textAlign:      "center",
                    gap:            "14px",
                    position:       "relative",
                    overflow:       "hidden",
                  }}>
                    {/* Top accent line */}
                    <div style={{ position: "absolute", top: 0, left: "20%", right: "20%", height: "1px", background: `linear-gradient(90deg, transparent, ${T.gold}60, transparent)` }} />

                    {/* Translation + confidence */}
                    <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                      <span style={{ fontSize: "10px", color: T.t4, letterSpacing: "0.06em", textTransform: "uppercase" }}>
                        {previewVerse.translation}
                      </span>
                      <span style={{ width: "3px", height: "3px", borderRadius: "50%", background: T.t5, display: "inline-block" }} />
                      <ConfRing value={previewVerse.confidence} size={28} />
                    </div>

                    {/* Verse text */}
                    <p style={{
                      fontSize:      "clamp(13px, 1.5vw, 17px)",
                      fontStyle:     "italic",
                      color:         T.t1,
                      lineHeight:    1.75,
                      margin:        0,
                      letterSpacing: "0.005em",
                    }}>
                      &ldquo;{previewVerse.verseText}&rdquo;
                    </p>

                    {/* Reference */}
                    <span style={{
                      fontSize:      "15px",
                      fontWeight:    700,
                      color:         T.gold,
                      letterSpacing: "-0.01em",
                    }}>
                      {previewVerse.verseRef}
                    </span>
                  </div>

                  {/* Push button */}
                  <button
                    onClick={() => pushToLive()}
                    style={{
                      display:       "flex",
                      alignItems:    "center",
                      justifyContent:"center",
                      gap:           "8px",
                      height:        "46px",
                      borderRadius:  "9px",
                      border:        "none",
                      background:    `linear-gradient(135deg, #3adb6e 0%, ${T.green} 100%)`,
                      color:         "#fff",
                      fontSize:      "13px",
                      fontWeight:    700,
                      cursor:        "pointer",
                      letterSpacing: "0.02em",
                      boxShadow:     `0 0 0 1px rgba(52,199,89,0.3), 0 6px 20px rgba(52,199,89,0.2)`,
                      transition:    "transform 150ms cubic-bezier(0.34,1.56,0.64,1), box-shadow 150ms ease",
                    }}
                    onMouseEnter={e => {
                      (e.currentTarget as HTMLButtonElement).style.transform = "scale(1.02)";
                      (e.currentTarget as HTMLButtonElement).style.boxShadow = `0 0 0 1px rgba(52,199,89,0.4), 0 8px 28px rgba(52,199,89,0.3)`;
                    }}
                    onMouseLeave={e => {
                      (e.currentTarget as HTMLButtonElement).style.transform = "scale(1)";
                      (e.currentTarget as HTMLButtonElement).style.boxShadow = `0 0 0 1px rgba(52,199,89,0.3), 0 6px 20px rgba(52,199,89,0.2)`;
                    }}
                  >
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="currentColor">
                      <polygon points="5,3 19,12 5,21" />
                    </svg>
                    Push to Live Display
                  </button>
                </>
              ) : (
                <div style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: "10px" }}>
                  <div style={{
                    width:        "48px",
                    height:       "48px",
                    borderRadius: "12px",
                    background:   T.raised,
                    border:       `1px solid ${T.line}`,
                    display:      "flex",
                    alignItems:   "center",
                    justifyContent:"center",
                    opacity:      0.4,
                  }}>
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke={T.t3} strokeWidth="1.5" strokeLinecap="round">
                      <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"/>
                    </svg>
                  </div>
                  <p style={{ fontSize: "12px", color: T.t4, textAlign: "center", lineHeight: 1.65, margin: 0 }}>
                    No verse staged.<br />
                    <span style={{ color: T.t3 }}>Auto-detected or searched verses appear here.</span>
                  </p>
                </div>
              )}
            </div>
          </Panel>

          {/* ── 3. LIVE DISPLAY ─────────────────────────────────────────── */}
          <Panel
            title="Live Display"
            style={{ flex: 1 }}
            badge={
              goLive
                ? <Badge label="On Air" color={T.red} pulse />
                : <span style={{ fontSize: "9px", color: T.t4 }}>Off Air — nothing showing</span>
            }
          >
            <div style={{ flex: 1, padding: "16px", display: "flex", flexDirection: "column", gap: "12px" }}>
              {/* Monitor frame */}
              <div style={{
                flex:       1,
                position:   "relative",
                borderRadius:"10px",
                overflow:   "hidden",
                background: "#020208",
                border:     `1px solid ${goLive ? `rgba(255,59,48,0.3)` : T.line}`,
                boxShadow:  goLive ? `0 0 0 1px rgba(255,59,48,0.15), inset 0 0 60px rgba(232,168,32,0.04)` : `inset 0 0 30px rgba(0,0,0,0.4)`,
                transition: "all 400ms ease",
              }}>
                {/* Scan-line texture */}
                <div style={{ position: "absolute", inset: 0, backgroundImage: "repeating-linear-gradient(0deg, transparent, transparent 2px, rgba(255,255,255,0.012) 2px, rgba(255,255,255,0.012) 4px)", pointerEvents: "none", zIndex: 2 }} />

                {/* Cross watermark */}
                <div style={{ position: "absolute", inset: 0, display: "flex", alignItems: "center", justifyContent: "center", opacity: 0.035, pointerEvents: "none" }}>
                  <svg width="130" height="130" viewBox="0 0 100 100">
                    <rect x="44" y="8"  width="12" height="84" rx="5" fill={T.gold} />
                    <rect x="14" y="30" width="72" height="12" rx="5" fill={T.gold} />
                  </svg>
                </div>

                {/* Radial glow */}
                {goLive && liveVerse && (
                  <div style={{
                    position: "absolute", inset: 0,
                    background: `radial-gradient(ellipse 80% 60% at 50% 50%, rgba(232,168,32,0.06) 0%, transparent 70%)`,
                    pointerEvents: "none",
                  }} />
                )}

                {/* Verse content */}
                <div style={{
                  position:  "absolute",
                  inset:     0,
                  display:   "flex",
                  flexDirection:"column",
                  alignItems:"center",
                  justifyContent:"center",
                  padding:   "20px 24px",
                  textAlign: "center",
                  gap:       "10px",
                  zIndex:    1,
                }}>
                  {liveVerse && goLive ? (
                    <div style={{ animation: "fadeSlideUp 350ms cubic-bezier(0.22,1,0.36,1)" }}>
                      <p style={{
                        fontSize:  "clamp(10px, 1.1vw, 14px)",
                        fontStyle: "italic",
                        color:     "rgba(255,255,255,0.9)",
                        lineHeight:1.7,
                        margin:    "0 0 10px",
                      }}>
                        &ldquo;{liveVerse.verseText}&rdquo;
                      </p>
                      <span style={{ fontSize: "12px", fontWeight: 700, color: T.gold }}>{liveVerse.verseRef}</span>
                      <span style={{ fontSize: "10px", color: "rgba(255,255,255,0.25)", marginLeft: "8px" }}>{liveVerse.translation}</span>
                    </div>
                  ) : (
                    <p style={{ fontSize: "11px", color: "rgba(255,255,255,0.12)", margin: 0, userSelect: "none" }}>
                      Projector is dark
                    </p>
                  )}
                </div>

                {/* ON AIR badge */}
                {goLive && (
                  <div style={{
                    position:     "absolute",
                    top:          "8px",
                    right:        "8px",
                    zIndex:       3,
                  }}>
                    <Badge label="On Air" color={T.red} pulse />
                  </div>
                )}
              </div>

              {/* Controls */}
              <div style={{ display: "flex", gap: "8px" }}>
                <button
                  onClick={() => pushToLive()}
                  disabled={!previewVerse}
                  style={{
                    flex:          1,
                    height:        "38px",
                    borderRadius:  "8px",
                    border:        `1px solid ${previewVerse ? `${T.green}50` : T.line}`,
                    background:    previewVerse ? T.greenDim : "transparent",
                    color:         previewVerse ? T.green : T.t4,
                    fontSize:      "11px",
                    fontWeight:    700,
                    cursor:        previewVerse ? "pointer" : "default",
                    letterSpacing: "0.05em",
                    textTransform: "uppercase",
                    display:       "flex",
                    alignItems:    "center",
                    justifyContent:"center",
                    gap:           "6px",
                    transition:    "all 150ms ease",
                  }}
                  onMouseEnter={e => { if (previewVerse) (e.currentTarget as HTMLButtonElement).style.background = `rgba(52,199,89,0.2)`; }}
                  onMouseLeave={e => { if (previewVerse) (e.currentTarget as HTMLButtonElement).style.background = T.greenDim; }}
                >
                  <svg width="9" height="9" viewBox="0 0 24 24" fill="currentColor">
                    <polygon points="5,3 19,12 5,21" />
                  </svg>
                  Go Live
                </button>
                <button
                  onClick={() => setGoLive(false)}
                  style={{
                    width:         "80px",
                    height:        "38px",
                    borderRadius:  "8px",
                    border:        `1px solid ${T.border}`,
                    background:    "transparent",
                    color:         T.t3,
                    fontSize:      "11px",
                    fontWeight:    500,
                    cursor:        "pointer",
                    letterSpacing: "0.03em",
                    transition:    "all 150ms ease",
                  }}
                  onMouseEnter={e => { (e.currentTarget as HTMLButtonElement).style.borderColor = T.borderHi; (e.currentTarget as HTMLButtonElement).style.color = T.t1; }}
                  onMouseLeave={e => { (e.currentTarget as HTMLButtonElement).style.borderColor = T.border; (e.currentTarget as HTMLButtonElement).style.color = T.t3; }}
                >
                  Clear
                </button>
              </div>
            </div>
          </Panel>

          {/* ── 4. VERSE QUEUE ──────────────────────────────────────────── */}
          <Panel
            title="Verse Queue"
            style={{ width: "230px", flexShrink: 0, borderRight: "none" }}
            badge={
              queue.length > 0
                ? <span style={{ fontSize: "9px", color: T.gold, fontFamily: F.mono, fontWeight: 700 }}>{queue.length}</span>
                : undefined
            }
            extra={
              <button
                onClick={() => setAutoMode(a => !a)}
                style={{
                  fontSize:      "9px",
                  fontWeight:    600,
                  color:         autoMode ? T.green : T.t4,
                  background:    "none",
                  border:        "none",
                  cursor:        "pointer",
                  letterSpacing: "0.06em",
                  textTransform: "uppercase",
                  display:       "flex",
                  alignItems:    "center",
                  gap:           "4px",
                }}
              >
                <span style={{ width: "5px", height: "5px", borderRadius: "50%", background: autoMode ? T.green : T.t5, display: "inline-block" }} />
                Auto {autoMode ? "On" : "Off"}
              </button>
            }
          >
            <div style={{ flex: 1, overflowY: "auto" }}>
              {queue.length === 0 ? (
                <div style={{ padding: "24px 16px", textAlign: "center" }}>
                  <div style={{ fontSize: "26px", opacity: 0.15, marginBottom: "8px" }}>🎙</div>
                  <p style={{ fontSize: "11px", color: T.t4, lineHeight: 1.65, margin: 0 }}>
                    Verses detected while you preach will appear here.
                  </p>
                </div>
              ) : (
                <div style={{ borderTop: `1px solid ${T.line}` }}>
                  {queue.map((v, i) => (
                    <div key={v.verseRef + i} style={{ borderBottom: `1px solid ${T.line}` }}>
                      <QueueCard
                        verse={v}
                        isActive={previewVerse?.verseRef === v.verseRef}
                        onClick={() => setPreviewVerse(v)}
                        onPushLive={() => {
                          setPreviewVerse(v);
                          pushToLive(v);
                        }}
                      />
                    </div>
                  ))}
                </div>
              )}
            </div>
          </Panel>
        </div>

        {/* ── BOTTOM ROW ──────────────────────────────────────────────────── */}
        <div style={{ display: "flex", height: "240px", gap: "1px", overflow: "hidden", flexShrink: 0 }}>

          {/* ── 5. BIBLE SEARCH ─────────────────────────────────────────── */}
          <Panel
            title="Bible Search"
            style={{ flex: 1 }}
            extra={
              <span style={{ fontSize: "9px", color: T.t4, fontFamily: F.mono }}>
                ⌘K
              </span>
            }
          >
            <div style={{ display: "flex", flexDirection: "column", flex: 1, overflow: "hidden" }}>
              {/* Input */}
              <div style={{ padding: "10px 14px", borderBottom: `1px solid ${T.line}`, flexShrink: 0 }}>
                <div style={{ position: "relative" }}>
                  <div style={{ position: "absolute", left: "11px", top: "50%", transform: "translateY(-50%)", color: T.t4, pointerEvents: "none", display: "flex" }}>
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
                      <circle cx="11" cy="11" r="8" />
                      <path d="m21 21-4.35-4.35" />
                    </svg>
                  </div>
                  <input
                    ref={searchRef}
                    type="text"
                    value={searchQ}
                    onChange={e => setSearchQ(e.target.value)}
                    onKeyDown={e => { if (e.key === "Enter") runSearch(searchQ); }}
                    placeholder='Search verses — "John 3:16", "faith", "do not fear"…'
                    style={{
                      width:        "100%",
                      height:       "36px",
                      padding:      "0 12px 0 32px",
                      background:   T.input,
                      border:       `1px solid ${T.border}`,
                      borderRadius: "8px",
                      color:        T.t1,
                      fontSize:     "12px",
                      outline:      "none",
                      boxSizing:    "border-box",
                      transition:   "border-color 150ms ease, box-shadow 150ms ease",
                    }}
                    onFocus={e => {
                      (e.target as HTMLInputElement).style.borderColor = `${T.gold}80`;
                      (e.target as HTMLInputElement).style.boxShadow = `0 0 0 3px ${T.goldGlow}`;
                    }}
                    onBlur={e => {
                      (e.target as HTMLInputElement).style.borderColor = T.border;
                      (e.target as HTMLInputElement).style.boxShadow = "none";
                    }}
                  />
                  {searching && (
                    <div style={{ position: "absolute", right: "12px", top: "50%", transform: "translateY(-50%)" }}>
                      <div style={{ width: "12px", height: "12px", borderRadius: "50%", border: `2px solid ${T.gold}40`, borderTopColor: T.gold, animation: "spin 0.7s linear infinite" }} />
                    </div>
                  )}
                </div>
              </div>

              {/* Results / Quick refs */}
              <div style={{ flex: 1, overflowY: "auto", padding: "8px 14px" }}>
                {results.length > 0 ? (
                  <div style={{ display: "flex", flexDirection: "column", gap: "4px" }}>
                    {results.map((r, i) => (
                      <div
                        key={r.ref + i}
                        onClick={() => stageResult(r)}
                        style={{
                          display:      "flex",
                          alignItems:   "flex-start",
                          gap:          "12px",
                          padding:      "9px 11px",
                          borderRadius: "7px",
                          border:       `1px solid transparent`,
                          cursor:       "pointer",
                          transition:   "all 120ms ease",
                        }}
                        onMouseEnter={e => {
                          (e.currentTarget as HTMLDivElement).style.background = T.raised;
                          (e.currentTarget as HTMLDivElement).style.borderColor = T.border;
                        }}
                        onMouseLeave={e => {
                          (e.currentTarget as HTMLDivElement).style.background = "transparent";
                          (e.currentTarget as HTMLDivElement).style.borderColor = "transparent";
                        }}
                      >
                        <div style={{ flexShrink: 0 }}>
                          <div style={{ fontSize: "11.5px", fontWeight: 700, color: T.gold }}>{r.ref}</div>
                          <div style={{
                            fontSize:     "8.5px",
                            color:        T.t4,
                            textTransform:"uppercase",
                            letterSpacing:"0.06em",
                            marginTop:    "2px",
                          }}>
                            {r.match}
                          </div>
                        </div>
                        <p style={{
                          fontSize:   "11px",
                          color:      T.t2,
                          lineHeight: 1.55,
                          margin:     0,
                          display:    "-webkit-box",
                          WebkitLineClamp: 2,
                          WebkitBoxOrient:"vertical",
                          overflow:   "hidden",
                          paddingTop: "1px",
                        }}>
                          {r.text}
                        </p>
                        <div style={{ flexShrink: 0, marginLeft: "auto" }}>
                          <button
                            onClick={e => { e.stopPropagation(); stageResult(r); pushToLive({ verseRef: r.ref, verseText: r.text, translation, confidence: 1, detectionMs: 0, snippetUsed: "", detectedAt: new Date().toISOString() }); }}
                            style={{
                              padding:      "3px 8px",
                              borderRadius: "4px",
                              border:       `1px solid ${T.green}50`,
                              background:   T.greenDim,
                              color:        T.green,
                              fontSize:     "9px",
                              fontWeight:   700,
                              cursor:       "pointer",
                              letterSpacing:"0.06em",
                            }}
                          >
                            LIVE
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : searchQ && !searching ? (
                  <p style={{ fontSize: "11px", color: T.t4, padding: "12px 0", margin: 0 }}>
                    No results for &ldquo;{searchQ}&rdquo;
                  </p>
                ) : !searchQ ? (
                  <div>
                    <p style={{ fontSize: "10px", color: T.t4, marginBottom: "8px", letterSpacing: "0.04em", textTransform: "uppercase" }}>Quick Access</p>
                    <div style={{ display: "flex", flexWrap: "wrap", gap: "6px" }}>
                      {QUICK_REFS.map(q => (
                        <button
                          key={q}
                          onClick={() => { setSearchQ(q); runSearch(q); }}
                          style={{
                            padding:      "5px 10px",
                            borderRadius: "6px",
                            border:       `1px solid ${T.border}`,
                            background:   T.raised,
                            color:        T.t3,
                            fontSize:     "11px",
                            cursor:       "pointer",
                            transition:   "all 130ms ease",
                            fontWeight:   500,
                          }}
                          onMouseEnter={e => {
                            (e.currentTarget as HTMLButtonElement).style.borderColor = `${T.gold}60`;
                            (e.currentTarget as HTMLButtonElement).style.color = T.gold;
                            (e.currentTarget as HTMLButtonElement).style.background = T.goldDim;
                          }}
                          onMouseLeave={e => {
                            (e.currentTarget as HTMLButtonElement).style.borderColor = T.border;
                            (e.currentTarget as HTMLButtonElement).style.color = T.t3;
                            (e.currentTarget as HTMLButtonElement).style.background = T.raised;
                          }}
                        >
                          {q}
                        </button>
                      ))}
                    </div>
                  </div>
                ) : null}
              </div>
            </div>
          </Panel>

          {/* ── 6. RECENT DETECTIONS ────────────────────────────────────── */}
          <Panel
            title="Detections"
            style={{ width: "290px", flexShrink: 0, borderRight: "none" }}
            badge={queue.length > 0 ? <span style={{ fontSize: "9px", color: T.t4, fontFamily: F.mono }}>{queue.length} this session</span> : undefined}
          >
            <div style={{ flex: 1, overflowY: "auto" }}>
              {queue.length === 0 ? (
                <div style={{ padding: "24px 16px", textAlign: "center" }}>
                  <div style={{ fontSize: "22px", opacity: 0.2, marginBottom: "8px" }}>👂</div>
                  <p style={{ fontSize: "11px", color: T.t4, lineHeight: 1.65, margin: 0 }}>
                    Listening for verses…<br />
                    Start preaching and they&apos;ll appear here automatically.
                  </p>
                </div>
              ) : (
                <div style={{ borderTop: `1px solid ${T.line}` }}>
                  {queue.map((v, i) => (
                    <div
                      key={v.verseRef + i}
                      onClick={() => setPreviewVerse(v)}
                      style={{
                        padding:     "9px 14px",
                        borderBottom:`1px solid ${T.line}`,
                        cursor:      "pointer",
                        animation:   i === 0 ? "fadeSlideUp 350ms cubic-bezier(0.22,1,0.36,1)" : "none",
                        background:  i === 0 ? `rgba(232,168,32,0.04)` : "transparent",
                        transition:  "background 130ms ease",
                      }}
                      onMouseEnter={e => { (e.currentTarget as HTMLDivElement).style.background = T.hover; }}
                      onMouseLeave={e => { (e.currentTarget as HTMLDivElement).style.background = i === 0 ? `rgba(232,168,32,0.04)` : "transparent"; }}
                    >
                      {/* Row 1 */}
                      <div style={{ display: "flex", alignItems: "center", gap: "8px", marginBottom: "4px" }}>
                        {i === 0 && (
                          <span style={{ fontSize: "8px", fontWeight: 700, color: T.gold, letterSpacing: "0.1em", textTransform: "uppercase" }}>
                            NEW
                          </span>
                        )}
                        <span style={{ fontSize: "12px", fontWeight: 700, color: T.gold }}>{v.verseRef}</span>
                        <span style={{ fontSize: "9.5px", color: T.t4 }}>{v.translation}</span>
                        <span style={{ marginLeft: "auto", fontSize: "9px", color: confColor(v.confidence), fontWeight: 600, fontFamily: F.mono }}>
                          {Math.round(v.confidence * 100)}%
                        </span>
                      </div>
                      {/* Row 2: snippet */}
                      {v.snippetUsed && (
                        <p style={{ fontSize: "10px", color: T.t4, margin: "0 0 3px", fontStyle: "italic", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                          &ldquo;{v.snippetUsed}&rdquo;
                        </p>
                      )}
                      {/* Row 3: time */}
                      <span style={{ fontSize: "9px", color: T.t5, fontFamily: F.mono }}>{relTime(v.detectedAt)}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </Panel>
        </div>
      </div>

      <style>{CSS_ANIMATIONS}</style>
    </div>
  );
}

/* ─────────────────────────────────────────────────────────────────────────────
   GLOBAL ANIMATIONS & SCROLLBAR
───────────────────────────────────────────────────────────────────────────── */
const CSS_ANIMATIONS = `
  @keyframes dotPulse    { 0%,100%{opacity:1;transform:scale(1)} 50%{opacity:0.35;transform:scale(0.65)} }
  @keyframes fadeSlideUp { from{opacity:0;transform:translateY(10px)} to{opacity:1;transform:translateY(0)} }
  @keyframes wv          { from{height:3px} to{height:20px} }
  @keyframes spin        { to{transform:translateY(-50%) rotate(360deg)} }
  *, *::before, *::after { box-sizing: border-box; }
  ::-webkit-scrollbar             { width: 3px; height: 3px; }
  ::-webkit-scrollbar-track       { background: transparent; }
  ::-webkit-scrollbar-thumb       { background: rgba(255,255,255,0.08); border-radius: 2px; }
  ::-webkit-scrollbar-thumb:hover { background: rgba(255,255,255,0.15); }
  select option { background: #0D0D18; }
`;
