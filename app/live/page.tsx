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
   ACCENT TOKENS  (used for verse colours, idle / ended screens)
───────────────────────────────────────────────────────────────────────────── */
const T = {
  gold:    "#C8920A",
  red:     "#B22222",
  green:   "#1B7A3C",
  orange:  "#E07000",
};

const F = { mono: "'SF Mono','Fira Code','Consolas',monospace" };

/* Dashboard light tokens */
const BD   = "#E8E8E8";   // border / divider
const TXT1 = "#111111";
const TXT2 = "#444444";
const TXT3 = "#888888";
const TXT4 = "#BBBBBB";
const GRN  = "#1B7A3C";
const SCR  = "#060A14";   // dark screen background for verse display

/* ─────────────────────────────────────────────────────────────────────────────
   HELPERS
───────────────────────────────────────────────────────────────────────────── */
type SvcState = "idle" | "live" | "ended";
interface SearchResult { ref: string; text: string; match: string }

function fmtDur(s: number) {
  const h  = Math.floor(s / 3600);
  const m  = Math.floor((s % 3600) / 60).toString().padStart(2, "0");
  const sc = (s % 60).toString().padStart(2, "0");
  return h ? `${h}:${m}:${sc}` : `${m}:${sc}`;
}

const TRANSLATIONS = ["NIV","KJV","ESV","NLT","NKJV","NASB","CSB","AMP","NRSV"];
const QUICK_REFS   = [
  "John 3:16","Psalm 23:1","Romans 8:28","Philippians 4:13",
  "Isaiah 40:31","Jeremiah 29:11","Proverbs 3:5","Matthew 6:33",
];

/* ─────────────────────────────────────────────────────────────────────────────
   TINY BADGE
───────────────────────────────────────────────────────────────────────────── */
function Badge({ label, color, pulse = false }: { label: string; color: string; pulse?: boolean }) {
  return (
    <span style={{
      display: "inline-flex", alignItems: "center", gap: "5px",
      padding: "2px 7px", borderRadius: "100px",
      border: `1px solid ${color}40`, background: `${color}14`,
      fontSize: "9px", fontWeight: 700, color, letterSpacing: "0.09em", textTransform: "uppercase",
    }}>
      <span style={{
        width: "5px", height: "5px", borderRadius: "50%", background: color, flexShrink: 0,
        animation: pulse ? "dotPulse 1.4s ease infinite" : "none",
      }} />
      {label}
    </span>
  );
}

/* ─────────────────────────────────────────────────────────────────────────────
   MAIN PAGE
───────────────────────────────────────────────────────────────────────────── */
export default function LivePage() {

  /* ── State ── */
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

  const [searchQ,   setSearchQ]   = useState("");
  const [searching, setSearching] = useState(false);
  const [results,   setResults]   = useState<SearchResult[]>([]);

  /* ── Refs ── */
  const socketRef    = useRef<Socket<ServerToClientEvents, ClientToServerEvents> | null>(null);
  const streamRef    = useRef<MediaStream | null>(null);
  const ctxRef       = useRef<AudioContext | null>(null);
  const serviceIdRef = useRef("");
  const searchRef    = useRef<HTMLInputElement>(null);

  useEffect(() => { serviceIdRef.current = serviceId; }, [serviceId]);

  /* ── Verse received ── */
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

  /* ── Start service ── */
  function startService() {
    setMicError("");
    const title = `Service — ${new Date().toLocaleDateString("en-US", { weekday: "long", month: "long", day: "numeric" })}`;
    const sid   = `svc-${Date.now()}`;
    setServiceId(sid);
    setServiceTitle(title);
    serviceIdRef.current = sid;
    setSvcState("live");

    void (async () => {
      try {
        const res  = await fetch("/api/service/start", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ title }) });
        const data = await res.json() as { serviceId: string; title: string };
        setServiceId(data.serviceId);
        setServiceTitle(data.title ?? title);
        serviceIdRef.current = data.serviceId;
      } catch { /* keep local sid */ }

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
      } catch (e) { console.error("[live] socket:", e); }

      try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        streamRef.current = stream;
        const ctx = new AudioContext({ sampleRate: 16000 });
        ctxRef.current = ctx;
        await ctx.audioWorklet.addModule("/audio-processor.js");
        const src     = ctx.createMediaStreamSource(stream);
        const worklet = new AudioWorkletNode(ctx, "audio-chunk-processor");
        worklet.port.onmessage = (e: MessageEvent<ArrayBuffer>) => { socketRef.current?.emit("audio:chunk", e.data); };
        src.connect(worklet);
        worklet.connect(ctx.destination);
      } catch (micErr) {
        const m = micErr instanceof Error ? micErr.message : "";
        if (m.toLowerCase().includes("denied") || m.toLowerCase().includes("permission")) {
          setMicError("Microphone access denied — click the camera/mic icon in the address bar to allow it.");
        }
      }
    })();
  }

  /* ── Push to projector ── */
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

  /* ── Bible search ── */
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
    const v: VerseDetectedPayload = { verseRef: r.ref, verseText: r.text, translation, confidence: 1.0, detectionMs: 0, snippetUsed: "", detectedAt: new Date().toISOString() };
    setPreviewVerse(v);
    setQueue(prev => [v, ...prev.filter(x => x.verseRef !== v.verseRef)].slice(0, 40));
  }

  /* ── End service ── */
  function endService() {
    socketRef.current?.emit("service:leave", serviceIdRef.current);
    socketRef.current?.disconnect();
    streamRef.current?.getTracks().forEach(t => t.stop());
    ctxRef.current?.close();
    setConnected(false);
    setSvcState("ended");
  }

  /* ── Cleanup ── */
  useEffect(() => () => {
    socketRef.current?.disconnect();
    streamRef.current?.getTracks().forEach(t => t.stop());
    ctxRef.current?.close();
  }, []);

  /* ── ⌘K shortcut ── */
  useEffect(() => {
    function onKey(e: KeyboardEvent) {
      if ((e.metaKey || e.ctrlKey) && e.key === "k") { e.preventDefault(); searchRef.current?.focus(); }
    }
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, []);

  /* ═══════════════════════════════════════════════════════════════════════════
     IDLE
  ═══════════════════════════════════════════════════════════════════════════ */
  if (svcState === "idle") {
    return (
      <>
        <div style={{ display: "flex", gap: "48px", alignItems: "flex-start", maxWidth: "1000px", margin: "0 auto", padding: "48px 0 80px" }}>

          {/* Left sidebar */}
          <div style={{ width: "260px", flexShrink: 0, borderRadius: "12px", overflow: "hidden", border: "1px solid var(--pk-border)" }}>
            <div style={{ background: "#8B1A1A", padding: "20px 20px 18px" }}>
              <div style={{ display: "flex", alignItems: "center", gap: "8px", marginBottom: "4px" }}>
                <span style={{ width: "7px", height: "7px", borderRadius: "50%", background: "#fff", opacity: 0.7, animation: "dotPulse 1.4s ease infinite", flexShrink: 0 }} />
                <span style={{ fontSize: "11px", fontWeight: 700, color: "rgba(255,255,255,0.7)", letterSpacing: "0.1em", textTransform: "uppercase" }}>Live Sermon</span>
              </div>
              <p style={{ fontSize: "18px", fontWeight: 700, color: "#fff", margin: 0, letterSpacing: "-0.02em" }}>Sermon Control</p>
            </div>
            {[
              { label: "Real-Time Transcription", desc: "Deepgram Nova-2 captures every word with sub-300ms latency.",
                icon: <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><path d="M12 2a3 3 0 0 1 3 3v7a3 3 0 0 1-6 0V5a3 3 0 0 1 3-3z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/><line x1="12" y1="19" x2="12" y2="22"/></svg> },
              { label: "AI Verse Detection", desc: "Groq Llama detects quotes, references, and paraphrases automatically.",
                icon: <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg> },
              { label: "Instant Projection", desc: "Detected verses display on the congregation screen as you preach.",
                icon: <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg> },
              { label: "Bible Search", desc: "Search all 66 books and push any verse live manually.",
                icon: <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg> },
            ].map((f, i, arr) => (
              <div key={f.label} style={{ display: "flex", gap: "12px", padding: "14px 18px", background: "var(--pk-surface)", borderBottom: i < arr.length - 1 ? "1px solid var(--pk-border)" : "none" }}>
                <div style={{ width: "28px", height: "28px", borderRadius: "7px", background: "rgba(139,26,26,0.08)", border: "1px solid rgba(139,26,26,0.15)", display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0, color: "#8B1A1A" }}>
                  {f.icon}
                </div>
                <div>
                  <div style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-text)", marginBottom: "3px" }}>{f.label}</div>
                  <div style={{ fontSize: "11px", color: "var(--pk-text-3)", lineHeight: 1.6 }}>{f.desc}</div>
                </div>
              </div>
            ))}
          </div>

          {/* Right content */}
          <div style={{ flex: 1, minWidth: 0 }}>
            <p style={{ fontSize: "11px", fontWeight: 700, color: "#8B1A1A", letterSpacing: "0.1em", textTransform: "uppercase", margin: "0 0 16px" }}>
              Live Sermon Control
            </p>
            <h1 style={{ fontSize: "clamp(32px, 4vw, 48px)", fontWeight: 800, color: "var(--pk-text)", margin: "0 0 16px", letterSpacing: "-0.03em", lineHeight: 1.1 }}>
              Ready to Go Live
            </h1>
            <div style={{ height: "2px", background: "#8B1A1A", width: "48px", marginBottom: "20px" }} />
            <p style={{ fontSize: "15px", color: "var(--pk-text-2)", margin: "0 0 36px", lineHeight: 1.8, maxWidth: "480px" }}>
              Press <strong style={{ color: "var(--pk-text)" }}>Start Service</strong> and begin preaching.
              Bible verses are detected automatically and displayed on your congregation&apos;s screen within seconds.
            </p>

            {micError && (
              <div style={{ marginBottom: "24px", padding: "12px 16px", borderRadius: "8px", background: "rgba(178,34,34,0.06)", border: "1px solid rgba(178,34,34,0.2)", color: "#B22222", fontSize: "13px", lineHeight: 1.6 }}>
                {micError}
              </div>
            )}

            <button
              onClick={startService}
              style={{ display: "inline-flex", alignItems: "center", gap: "12px", padding: "0 40px", height: "52px", borderRadius: "8px", border: "none", background: "#8B1A1A", color: "#fff", fontSize: "15px", fontWeight: 700, cursor: "pointer", transition: "background 150ms ease" }}
              onMouseEnter={e => { (e.currentTarget as HTMLButtonElement).style.background = "#A01E1E"; }}
              onMouseLeave={e => { (e.currentTarget as HTMLButtonElement).style.background = "#8B1A1A"; }}
            >
              <span style={{ width: "8px", height: "8px", borderRadius: "50%", background: "#fff", opacity: 0.9, flexShrink: 0, animation: "dotPulse 1.4s ease infinite" }} />
              Start Service
            </button>
            <p style={{ marginTop: "14px", fontSize: "12px", color: "var(--pk-text-3)" }}>
              Requires microphone · Deepgram Nova-2 · Groq AI
            </p>

            <div style={{ marginTop: "48px", padding: "20px 24px", borderRadius: "10px", border: "1px solid var(--pk-border)", background: "var(--pk-surface)" }}>
              <p style={{ fontSize: "11px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 10px", letterSpacing: "0.06em", textTransform: "uppercase" }}>How It Works</p>
              <ol style={{ margin: 0, paddingLeft: "18px", display: "flex", flexDirection: "column", gap: "8px" }}>
                {[
                  "Open this page on the pastor's device and click Start Service.",
                  "Open the Projector link on the screen your congregation sees.",
                  "Preach normally — verses appear on the projector automatically.",
                  "You can also search and push any verse manually at any time.",
                ].map((step, i) => (
                  <li key={i} style={{ fontSize: "13px", color: "var(--pk-text-2)", lineHeight: 1.65 }}>{step}</li>
                ))}
              </ol>
            </div>
          </div>
        </div>
        <style>{CSS_ANIMATIONS}</style>
      </>
    );
  }

  /* ═══════════════════════════════════════════════════════════════════════════
     ENDED
  ═══════════════════════════════════════════════════════════════════════════ */
  if (svcState === "ended") {
    return (
      <>
        <div style={{ maxWidth: "480px", margin: "0 auto", textAlign: "center", padding: "60px 20px 80px" }}>
          <div style={{ width: "64px", height: "64px", borderRadius: "16px", background: "rgba(27,122,60,0.08)", border: "1px solid rgba(27,122,60,0.2)", display: "flex", alignItems: "center", justifyContent: "center", margin: "0 auto 28px", fontSize: "26px", color: GRN }}>
            ✓
          </div>
          <h2 style={{ fontSize: "clamp(26px,4vw,36px)", fontWeight: 700, color: "var(--pk-text)", marginBottom: "12px", letterSpacing: "-0.03em" }}>
            Service Complete
          </h2>
          <div style={{ display: "flex", justifyContent: "center", gap: "32px", marginBottom: "36px" }}>
            {[
              { label: "Verses Detected", val: stats.versesDetected, color: T.gold },
              { label: "Power Moments",   val: stats.powerMoments,   color: GRN   },
              { label: "Duration",        val: fmtDur(stats.duration), color: TXT3 },
            ].map(s => (
              <div key={s.label} style={{ textAlign: "center" }}>
                <div style={{ fontSize: "28px", fontWeight: 800, color: s.color, letterSpacing: "-0.03em", fontFamily: F.mono }}>{s.val}</div>
                <div style={{ fontSize: "11px", color: TXT4, marginTop: "2px", letterSpacing: "0.04em" }}>{s.label}</div>
              </div>
            ))}
          </div>
          <button
            onClick={() => { setQueue([]); setPreviewVerse(null); setLiveVerse(null); setTranscript(""); setStats({ versesDetected: 0, powerMoments: 0, duration: 0, attendees: 0 }); setSvcState("idle"); }}
            style={{ padding: "0 40px", height: "50px", borderRadius: "8px", border: "none", background: "#8B1A1A", color: "#fff", fontSize: "15px", fontWeight: 700, cursor: "pointer" }}
          >
            Start New Service
          </button>
        </div>
        <style>{CSS_ANIMATIONS}</style>
      </>
    );
  }

  /* ═══════════════════════════════════════════════════════════════════════════
     LIVE  ─ openbeam-style light dashboard
  ═══════════════════════════════════════════════════════════════════════════ */
  return (
    <div style={{
      position: "fixed", top: "64px", left: 0, right: 0, bottom: 0,
      zIndex: 500,
      background: "#FFFFFF",
      display: "flex", flexDirection: "column", overflow: "hidden",
      fontFamily: "-apple-system,'SF Pro Display','Helvetica Neue',sans-serif",
    }}>

      {/* ── TOP BAR ── */}
      <div style={{ display: "flex", alignItems: "center", gap: "10px", height: "42px", padding: "0 16px", background: "#FFFFFF", borderBottom: `1px solid ${BD}`, flexShrink: 0 }}>
        {/* Mic */}
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke={connected ? GRN : TXT4} strokeWidth="2" strokeLinecap="round">
          <path d="M12 2a3 3 0 0 1 3 3v7a3 3 0 0 1-6 0V5a3 3 0 0 1 3-3z"/>
          <path d="M19 10v2a7 7 0 0 1-14 0v-2"/>
          <line x1="12" y1="19" x2="12" y2="22"/>
        </svg>
        {/* Signal bars */}
        <div style={{ display: "flex", alignItems: "flex-end", gap: "2px", height: "14px" }}>
          {[6,10,14].map((h,i) => (
            <div key={i} style={{ width: "3px", height: `${h}px`, borderRadius: "1px", background: connected ? GRN : TXT4, opacity: connected ? 1 : 0.35 }} />
          ))}
        </div>
        {connected
          ? <Badge label="Live" color={T.red} pulse />
          : <span style={{ fontSize: "10px", color: TXT4, fontWeight: 600, letterSpacing: "0.08em" }}>OFF AIR</span>
        }
        <div style={{ width: "1px", height: "18px", background: BD }} />
        <span style={{ fontSize: "12px", color: TXT3, fontWeight: 500 }}>{serviceTitle}</span>
        <div style={{ width: "1px", height: "18px", background: BD }} />
        {[
          { label: "Detected", val: stats.versesDetected, color: T.gold },
          { label: "Duration", val: fmtDur(stats.duration), color: TXT3 },
        ].map(s => (
          <div key={s.label} style={{ display: "flex", alignItems: "baseline", gap: "4px" }}>
            <span style={{ fontSize: "13px", fontWeight: 700, color: s.color, fontFamily: F.mono }}>{s.val}</span>
            <span style={{ fontSize: "9px", color: TXT4, letterSpacing: "0.06em", textTransform: "uppercase" }}>{s.label}</span>
          </div>
        ))}
        <div style={{ flex: 1 }} />
        <button onClick={() => setAutoMode(a => !a)} style={{ display: "flex", alignItems: "center", gap: "5px", padding: "0 10px", height: "26px", borderRadius: "6px", border: `1px solid ${autoMode ? "rgba(27,122,60,0.4)" : BD}`, background: autoMode ? "rgba(27,122,60,0.07)" : "transparent", color: autoMode ? GRN : TXT3, fontSize: "10px", fontWeight: 600, cursor: "pointer", letterSpacing: "0.06em", textTransform: "uppercase" }}>
          <span style={{ width: "6px", height: "6px", borderRadius: "50%", background: autoMode ? GRN : TXT4, animation: autoMode ? "dotPulse 1.6s ease infinite" : "none" }} />
          Auto {autoMode ? "On" : "Off"}
        </button>
        <select value={translation} onChange={e => changeTranslation(e.target.value)} style={{ height: "26px", padding: "0 8px", border: `1px solid ${BD}`, borderRadius: "6px", color: TXT1, fontSize: "11px", background: "#fff", cursor: "pointer", outline: "none" }}>
          {TRANSLATIONS.map(t => <option key={t} value={t}>{t}</option>)}
        </select>
        <a href={`/live/projector?serviceId=${serviceId}`} target="_blank" rel="noreferrer" style={{ display: "flex", alignItems: "center", gap: "5px", padding: "0 10px", height: "26px", borderRadius: "6px", border: `1px solid ${BD}`, color: TXT2, fontSize: "11px", fontWeight: 500, textDecoration: "none" }}>
          <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
          Projector
        </a>
        <button onClick={endService} style={{ display: "flex", alignItems: "center", gap: "5px", padding: "0 12px", height: "26px", borderRadius: "6px", border: "1px solid rgba(178,34,34,0.3)", background: "rgba(178,34,34,0.06)", color: "#B22222", fontSize: "10px", fontWeight: 600, cursor: "pointer", letterSpacing: "0.04em", textTransform: "uppercase" }}>
          <svg width="7" height="7" viewBox="0 0 8 8" fill="currentColor"><rect width="8" height="8" rx="1"/></svg>
          End Service
        </button>
      </div>

      {/* ── PANELS ── */}
      <div style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>

        {/* TOP ROW */}
        <div style={{ display: "flex", flex: 1, overflow: "hidden", borderBottom: `1px solid ${BD}` }}>

          {/* 1 — LIVE TRANSCRIPT */}
          <div style={{ width: "220px", flexShrink: 0, borderRight: `1px solid ${BD}`, display: "flex", flexDirection: "column" }}>
            <div style={{ height: "34px", padding: "0 14px", display: "flex", alignItems: "center", gap: "8px", borderBottom: `1px solid ${BD}`, flexShrink: 0 }}>
              <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke={TXT3} strokeWidth="2.5" strokeLinecap="round"><path d="M12 2a3 3 0 0 1 3 3v7a3 3 0 0 1-6 0V5a3 3 0 0 1 3-3z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/><line x1="12" y1="19" x2="12" y2="22"/></svg>
              <span style={{ fontSize: "9.5px", fontWeight: 600, color: TXT3, letterSpacing: "0.1em", textTransform: "uppercase" }}>Live Transcript</span>
              {connected && <Badge label="Live" color={T.red} pulse />}
            </div>
            <div style={{ flex: 1, overflowY: "auto", padding: "12px 14px" }}>
              {transcript ? (
                <p style={{ fontSize: "13px", color: TXT1, lineHeight: 1.8, margin: 0 }}>{transcript}</p>
              ) : (
                <p style={{ fontSize: "12px", color: TXT4, lineHeight: 1.65, margin: 0 }}>
                  {connected ? "Start preaching — transcript appears here…" : "Click Start Service to begin"}
                </p>
              )}
            </div>
            <div style={{ padding: "10px 14px", borderTop: `1px solid ${BD}`, flexShrink: 0 }}>
              <button onClick={endService} style={{ background: "none", border: "none", color: "#B22222", fontSize: "12px", fontWeight: 600, cursor: "pointer", display: "flex", alignItems: "center", gap: "6px", padding: 0 }}>
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"><path d="M12 2a3 3 0 0 1 3 3v7a3 3 0 0 1-6 0V5a3 3 0 0 1 3-3z"/><line x1="12" y1="19" x2="12" y2="22"/></svg>
                Stop transcribing
              </button>
            </div>
          </div>

          {/* 2 — PROGRAM PREVIEW */}
          <div style={{ flex: 1, borderRight: `1px solid ${BD}`, display: "flex", flexDirection: "column" }}>
            <div style={{ height: "34px", padding: "0 14px", display: "flex", alignItems: "center", gap: "8px", borderBottom: `1px solid ${BD}`, flexShrink: 0 }}>
              <span style={{ fontSize: "9.5px", fontWeight: 600, color: TXT3, letterSpacing: "0.1em", textTransform: "uppercase" }}>Program Preview</span>
              <span style={{ marginLeft: "auto", fontSize: "9px", color: TXT4 }}>Staged — review before going live</span>
            </div>
            <div style={{ flex: 1, padding: "12px", display: "flex", flexDirection: "column", gap: "10px", overflow: "hidden" }}>
              <div style={{ flex: 1, background: SCR, borderRadius: "6px", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: "20px 24px", textAlign: "center", gap: "12px" }}>
                {previewVerse ? (
                  <>
                    <span style={{ fontSize: "10.5px", fontWeight: 700, color: T.gold, letterSpacing: "0.07em", textTransform: "uppercase" }}>
                      {previewVerse.verseRef} ({previewVerse.translation})
                    </span>
                    <p style={{ fontSize: "clamp(12px,1.2vw,15px)", fontStyle: "italic", color: "rgba(255,255,255,0.92)", lineHeight: 1.8, margin: 0 }}>
                      &ldquo;{previewVerse.verseText}&rdquo;
                    </p>
                  </>
                ) : (
                  <p style={{ fontSize: "11px", color: "rgba(255,255,255,0.15)", margin: 0, userSelect: "none" }}>No verse staged</p>
                )}
              </div>
              <button onClick={() => pushToLive()} disabled={!previewVerse} style={{ height: "38px", borderRadius: "6px", border: "none", background: previewVerse ? GRN : "#E8E8E8", color: previewVerse ? "#fff" : TXT4, fontSize: "12px", fontWeight: 700, cursor: previewVerse ? "pointer" : "default", display: "flex", alignItems: "center", justifyContent: "center", gap: "7px", flexShrink: 0 }}>
                <svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor"><polygon points="5,3 19,12 5,21"/></svg>
                Push to Live Display
              </button>
            </div>
          </div>

          {/* 3 — LIVE DISPLAY */}
          <div style={{ flex: 1, borderRight: `1px solid ${BD}`, display: "flex", flexDirection: "column" }}>
            <div style={{ height: "34px", padding: "0 14px", display: "flex", alignItems: "center", gap: "8px", borderBottom: `1px solid ${BD}`, flexShrink: 0 }}>
              <span style={{ fontSize: "9.5px", fontWeight: 600, color: TXT3, letterSpacing: "0.1em", textTransform: "uppercase" }}>Live Display</span>
              {goLive
                ? <Badge label="Live" color={GRN} pulse />
                : <span style={{ fontSize: "9px", color: TXT4, marginLeft: "auto" }}>Off Air</span>
              }
            </div>
            <div style={{ flex: 1, padding: "12px", display: "flex", flexDirection: "column", gap: "10px", overflow: "hidden" }}>
              <div style={{ flex: 1, background: SCR, borderRadius: "6px", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: "20px 24px", textAlign: "center", gap: "12px", border: goLive ? "1px solid rgba(27,122,60,0.3)" : "1px solid transparent", transition: "border-color 400ms ease" }}>
                {liveVerse && goLive ? (
                  <div style={{ animation: "fadeSlideUp 350ms cubic-bezier(0.22,1,0.36,1)" }}>
                    <span style={{ fontSize: "10.5px", fontWeight: 700, color: T.gold, letterSpacing: "0.07em", textTransform: "uppercase", display: "block", marginBottom: "10px" }}>
                      {liveVerse.verseRef} ({liveVerse.translation})
                    </span>
                    <p style={{ fontSize: "clamp(12px,1.2vw,15px)", fontStyle: "italic", color: "rgba(255,255,255,0.92)", lineHeight: 1.8, margin: 0 }}>
                      &ldquo;{liveVerse.verseText}&rdquo;
                    </p>
                  </div>
                ) : (
                  <p style={{ fontSize: "11px", color: "rgba(255,255,255,0.12)", margin: 0, userSelect: "none" }}>Projector is dark</p>
                )}
              </div>
              <div style={{ display: "flex", gap: "8px", flexShrink: 0 }}>
                <button onClick={() => pushToLive()} disabled={!previewVerse} style={{ flex: 1, height: "36px", borderRadius: "6px", border: `1px solid ${previewVerse ? GRN : BD}`, background: "transparent", color: previewVerse ? GRN : TXT4, fontSize: "11px", fontWeight: 700, cursor: previewVerse ? "pointer" : "default" }}>
                  Go Live
                </button>
                <button onClick={() => setGoLive(false)} style={{ width: "70px", height: "36px", borderRadius: "6px", border: `1px solid ${BD}`, background: "transparent", color: TXT3, fontSize: "11px", cursor: "pointer" }}>
                  Clear
                </button>
              </div>
            </div>
          </div>

          {/* 4 — QUEUE */}
          <div style={{ width: "220px", flexShrink: 0, display: "flex", flexDirection: "column" }}>
            <div style={{ height: "34px", padding: "0 14px", display: "flex", alignItems: "center", gap: "8px", borderBottom: `1px solid ${BD}`, flexShrink: 0 }}>
              <span style={{ fontSize: "9.5px", fontWeight: 600, color: TXT3, letterSpacing: "0.1em", textTransform: "uppercase" }}>Queue</span>
              <span style={{ marginLeft: "auto", fontSize: "11px", color: TXT3, fontFamily: F.mono }}>{queue.length}</span>
              {queue.length > 0 && <button onClick={() => setQueue([])} style={{ background: "none", border: "none", fontSize: "10px", color: TXT4, cursor: "pointer", padding: "0 2px" }}>Clear all</button>}
            </div>
            <div style={{ flex: 1, overflowY: "auto" }}>
              {queue.length === 0 ? (
                <p style={{ padding: "20px 14px", fontSize: "11.5px", color: TXT4, lineHeight: 1.7, margin: 0, textAlign: "center" }}>
                  Verses will appear here when detected or queued
                </p>
              ) : queue.map((v, i) => (
                <div key={v.verseRef + i} onClick={() => setPreviewVerse(v)}
                  style={{ padding: "10px 14px", borderBottom: "1px solid #F4F4F4", cursor: "pointer", borderLeft: previewVerse?.verseRef === v.verseRef ? `2px solid ${GRN}` : "2px solid transparent", background: previewVerse?.verseRef === v.verseRef ? "rgba(27,122,60,0.04)" : "transparent" }}
                  onMouseEnter={e => { (e.currentTarget as HTMLDivElement).style.background = "#F8F8F8"; }}
                  onMouseLeave={e => { (e.currentTarget as HTMLDivElement).style.background = previewVerse?.verseRef === v.verseRef ? "rgba(27,122,60,0.04)" : "transparent"; }}
                >
                  <div style={{ fontSize: "11.5px", fontWeight: 700, color: T.gold, marginBottom: "3px" }}>{v.verseRef}</div>
                  <p style={{ fontSize: "10.5px", color: TXT2, lineHeight: 1.55, margin: 0, display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical", overflow: "hidden" }}>
                    {v.verseText}
                  </p>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* BOTTOM ROW */}
        <div style={{ height: "280px", display: "flex", overflow: "hidden", flexShrink: 0 }}>

          {/* 5 — BIBLE SEARCH */}
          <div style={{ flex: 1, borderRight: `1px solid ${BD}`, display: "flex", flexDirection: "column" }}>
            <div style={{ height: "46px", padding: "0 14px", display: "flex", alignItems: "center", gap: "8px", borderBottom: `1px solid ${BD}`, flexShrink: 0 }}>
              <button style={{ height: "28px", padding: "0 10px", borderRadius: "6px", border: `1px solid ${GRN}`, background: "rgba(27,122,60,0.08)", color: GRN, fontSize: "11px", fontWeight: 600, cursor: "pointer", display: "flex", alignItems: "center", gap: "5px", flexShrink: 0 }}>
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>
                Book search
              </button>
              <div style={{ width: "1px", height: "20px", background: BD, flexShrink: 0 }} />
              <div style={{ flex: 1, position: "relative" }}>
                <input
                  ref={searchRef}
                  type="text"
                  value={searchQ}
                  onChange={e => setSearchQ(e.target.value)}
                  onKeyDown={e => { if (e.key === "Enter") runSearch(searchQ); }}
                  placeholder="Type: J → John 3:16"
                  style={{ width: "100%", height: "36px", padding: "0 12px", border: "none", outline: "none", fontSize: "13px", color: TXT1, background: "transparent" }}
                />
                {searching && (
                  <div style={{ position: "absolute", right: "12px", top: "50%", transform: "translateY(-50%)" }}>
                    <div style={{ width: "12px", height: "12px", borderRadius: "50%", border: `2px solid rgba(27,122,60,0.3)`, borderTopColor: GRN, animation: "spin 0.7s linear infinite" }} />
                  </div>
                )}
              </div>
              <select value={translation} onChange={e => changeTranslation(e.target.value)} style={{ height: "28px", padding: "0 8px", border: `1px solid ${BD}`, borderRadius: "6px", color: TXT1, fontSize: "11px", background: "#fff", cursor: "pointer", outline: "none", flexShrink: 0 }}>
                {TRANSLATIONS.map(t => <option key={t} value={t}>{t}</option>)}
              </select>
            </div>
            <div style={{ flex: 1, overflowY: "auto" }}>
              {results.length > 0 ? results.map((r, i) => (
                <div key={r.ref + i} onClick={() => stageResult(r)}
                  style={{ display: "flex", alignItems: "flex-start", gap: "12px", padding: "8px 14px", borderBottom: "1px solid #F4F4F4", cursor: "pointer" }}
                  onMouseEnter={e => { (e.currentTarget as HTMLDivElement).style.background = "#F8F8F8"; }}
                  onMouseLeave={e => { (e.currentTarget as HTMLDivElement).style.background = "transparent"; }}
                >
                  <span style={{ fontSize: "11.5px", fontWeight: 700, color: T.gold, minWidth: "80px", flexShrink: 0, paddingTop: "1px" }}>{r.ref}</span>
                  <p style={{ flex: 1, fontSize: "12px", color: TXT2, lineHeight: 1.55, margin: 0, display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical", overflow: "hidden" }}>{r.text}</p>
                  <button
                    onClick={e => { e.stopPropagation(); stageResult(r); pushToLive({ verseRef: r.ref, verseText: r.text, translation, confidence: 1, detectionMs: 0, snippetUsed: "", detectedAt: new Date().toISOString() }); }}
                    style={{ flexShrink: 0, padding: "3px 8px", borderRadius: "4px", border: `1px solid ${GRN}`, background: "rgba(27,122,60,0.08)", color: GRN, fontSize: "9px", fontWeight: 700, cursor: "pointer" }}
                  >
                    LIVE
                  </button>
                </div>
              )) : !searchQ ? (
                <div style={{ padding: "10px 14px" }}>
                  <p style={{ fontSize: "10px", color: TXT4, marginBottom: "8px", letterSpacing: "0.05em", textTransform: "uppercase" }}>Quick Access</p>
                  <div style={{ display: "flex", flexWrap: "wrap", gap: "6px" }}>
                    {QUICK_REFS.map(q => (
                      <button key={q} onClick={() => { setSearchQ(q); runSearch(q); }}
                        style={{ padding: "4px 10px", borderRadius: "5px", border: `1px solid ${BD}`, background: "#F8F8F8", color: TXT2, fontSize: "11px", cursor: "pointer" }}
                        onMouseEnter={e => { (e.currentTarget as HTMLButtonElement).style.borderColor = GRN; (e.currentTarget as HTMLButtonElement).style.color = GRN; }}
                        onMouseLeave={e => { (e.currentTarget as HTMLButtonElement).style.borderColor = BD; (e.currentTarget as HTMLButtonElement).style.color = TXT2; }}
                      >
                        {q}
                      </button>
                    ))}
                  </div>
                </div>
              ) : searchQ && !searching ? (
                <p style={{ padding: "12px 14px", fontSize: "12px", color: TXT4, margin: 0 }}>No results for &ldquo;{searchQ}&rdquo;</p>
              ) : null}
            </div>
          </div>

          {/* 6 — RECENT DETECTIONS */}
          <div style={{ width: "320px", flexShrink: 0, display: "flex", flexDirection: "column" }}>
            <div style={{ height: "46px", padding: "0 14px", display: "flex", alignItems: "center", gap: "8px", borderBottom: `1px solid ${BD}`, flexShrink: 0 }}>
              <span style={{ fontSize: "9.5px", fontWeight: 600, color: TXT3, letterSpacing: "0.1em", textTransform: "uppercase" }}>Recent Detections</span>
              {queue.length > 0 && <button onClick={() => setQueue([])} style={{ marginLeft: "auto", background: "none", border: "none", fontSize: "10px", color: TXT4, cursor: "pointer" }}>Clear all</button>}
            </div>
            <div style={{ flex: 1, overflowY: "auto" }}>
              {queue.length === 0 ? (
                <p style={{ padding: "24px 16px", fontSize: "12px", color: TXT4, lineHeight: 1.7, margin: 0, textAlign: "center" }}>
                  Verse detections will appear here during transcription
                </p>
              ) : queue.map((v, i) => (
                <div key={v.verseRef + i} style={{ padding: "12px 14px", borderBottom: "1px solid #F4F4F4", animation: i === 0 ? "fadeSlideUp 350ms cubic-bezier(0.22,1,0.36,1)" : "none" }}>
                  <div style={{ display: "flex", alignItems: "center", gap: "6px", marginBottom: "5px" }}>
                    <span style={{ width: "7px", height: "7px", borderRadius: "50%", background: GRN, flexShrink: 0 }} />
                    <span style={{ fontSize: "8.5px", fontWeight: 700, color: GRN, letterSpacing: "0.08em", textTransform: "uppercase", border: "1px solid rgba(27,122,60,0.3)", padding: "1px 5px", borderRadius: "3px" }}>
                      {v.snippetUsed ? "Direct" : "AI"}
                    </span>
                    <span style={{ fontSize: "13px", fontWeight: 700, color: TXT1 }}>{v.verseRef}</span>
                  </div>
                  <p style={{ fontSize: "12px", color: TXT2, lineHeight: 1.65, margin: "0 0 8px" }}>{v.verseText}</p>
                  <div style={{ display: "flex", gap: "7px" }}>
                    <button onClick={() => { setPreviewVerse(v); pushToLive(v); }} style={{ height: "28px", padding: "0 12px", borderRadius: "5px", border: "none", background: GRN, color: "#fff", fontSize: "11px", fontWeight: 700, cursor: "pointer", display: "flex", alignItems: "center", gap: "5px" }}>
                      <svg width="8" height="8" viewBox="0 0 24 24" fill="currentColor"><polygon points="5,3 19,12 5,21"/></svg>
                      Present
                    </button>
                    <button onClick={() => setPreviewVerse(v)} style={{ height: "28px", padding: "0 12px", borderRadius: "5px", border: `1px solid ${BD}`, background: "transparent", color: TXT2, fontSize: "11px", cursor: "pointer" }}>
                      + Queue
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      <style>{CSS_ANIMATIONS}</style>
    </div>
  );
}

/* ─────────────────────────────────────────────────────────────────────────────
   ANIMATIONS & SCROLLBAR
───────────────────────────────────────────────────────────────────────────── */
const CSS_ANIMATIONS = `
  @keyframes dotPulse    { 0%,100%{opacity:1;transform:scale(1)} 50%{opacity:0.35;transform:scale(0.65)} }
  @keyframes fadeSlideUp { from{opacity:0;transform:translateY(8px)} to{opacity:1;transform:translateY(0)} }
  @keyframes wv          { from{height:3px} to{height:18px} }
  @keyframes spin        { to{transform:translateY(-50%) rotate(360deg)} }
  *, *::before, *::after { box-sizing: border-box; }
  ::-webkit-scrollbar             { width: 4px; height: 4px; }
  ::-webkit-scrollbar-track       { background: transparent; }
  ::-webkit-scrollbar-thumb       { background: #DEDEDE; border-radius: 4px; }
  ::-webkit-scrollbar-thumb:hover { background: #CCCCCC; }
  select option { background: #FFFFFF; color: #111111; }
`;
