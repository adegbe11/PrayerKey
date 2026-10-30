"use client";
import { useCallback, useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import LiveStory from "./LiveStory";
import { findSpokenRefs } from "@/lib/bible/spoken-refs";
import "./live.css";

interface DetectedVerse {
  verseRef: string; verseText: string; translation: string;
  confidence: number; detectedAt: string; source?: "ai" | "manual";
}
interface QueueItem extends DetectedVerse { queueId: string; }
type ServiceState = "idle" | "live" | "ended";
/** shared with /live/projector */
const PROJECTOR_CHANNEL = "prayerkey-live-projector";
type SearchMode   = "book" | "context";

// ── Design tokens — deep navy, iOS-27 / Liquid Glass ─────────────────────────
const VOID    = "#160022";                          // Deep navy
const VOID_HI = "#22093A";                          // Lifted card bg
const GLASS   = "rgba(15,34,56,0.72)";              // Frosted glass
const YELLOW  = "#F4D03F";                          // Premium pill accent
const BLUE    = "#3B82F6";                          // Action FAB
const BLUE_HI = "#5798F8";                          // Hover/lift
const GOLD    = "#F5A623";                          // Verse refs
const RED     = "#FF453A";
const GREEN   = "#34C759";
const ORANGE  = "#FF9F0A";
const BORDER  = "rgba(255,255,255,0.07)";
const HILITE  = "inset 0 1px 0 rgba(255,255,255,0.06)";
const T1      = "#FFFFFF";
const T2      = "rgba(255,255,255,0.74)";
const T3      = "rgba(255,255,255,0.48)";

// Cards: glassy navy with top highlight
const card: React.CSSProperties = {
  background:           "rgba(255,255,255,0.025)",
  backdropFilter:       "blur(28px) saturate(160%)",
  WebkitBackdropFilter: "blur(28px) saturate(160%)",
  border:               `1px solid ${BORDER}`,
  boxShadow:            `0 8px 28px rgba(0,0,0,0.40), ${HILITE}`,
};

// Timer dots like 00.34.37
function fmtSerif(s: number) {
  const h  = Math.floor(s / 3600);
  const m  = Math.floor((s % 3600) / 60);
  const ss = s % 60;
  return `${String(h).padStart(2,"0")}.${String(m).padStart(2,"0")}.${String(ss).padStart(2,"0")}`;
}
function fmt(s: number) {
  const m = Math.floor(s / 60), ss = s % 60;
  return `${String(m).padStart(2,"0")}:${String(ss).padStart(2,"0")}`;
}

// ── VerseScreen — black "power-button" card with depth ────────────────────────
function VerseScreen({
  verse, empty, isLive = false,
}: { verse: DetectedVerse | null; empty: string; isLive?: boolean }) {
  const [animKey, setAnimKey] = useState(0);
  useEffect(() => { if (verse) setAnimKey(k => k + 1); }, [verse?.verseRef]);

  return (
    <div style={{
      flex: 1, borderRadius: "20px", overflow: "hidden", position: "relative" as const,
      background: `
        radial-gradient(ellipse 75% 65% at 50% 45%, #22093A 0%, #060D18 70%, #03070F 100%)
      `,
      border: `1px solid ${verse ? "rgba(255,255,255,0.10)" : "rgba(255,255,255,0.05)"}`,
      display: "flex", alignItems: "center", justifyContent: "center",
      boxShadow: verse
        ? "0 18px 50px rgba(0,0,0,0.55), inset 0 1px 0 rgba(255,255,255,0.05), inset 0 0 100px rgba(0,30,80,0.30)"
        : "0 12px 40px rgba(0,0,0,0.40), inset 0 1px 0 rgba(255,255,255,0.03)",
      transition: "all 500ms cubic-bezier(0.22,1,0.36,1)",
    }}>
      {/* Soft directional light from top-left */}
      <div style={{ position:"absolute" as const, inset:0, borderRadius:"20px",
        background:"radial-gradient(ellipse 60% 40% at 30% 0%, rgba(120,170,255,0.10) 0%, transparent 60%)",
        pointerEvents:"none" as const }} />

      {/* Subtle dotted-globe pattern behind verse */}
      <div style={{ position:"absolute" as const, inset:0, opacity: 0.10,
        backgroundImage:`radial-gradient(rgba(180,200,255,0.18) 1px, transparent 1px)`,
        backgroundSize:"14px 14px", maskImage:"radial-gradient(ellipse 70% 70% at 50% 50%, black 30%, transparent 75%)",
        WebkitMaskImage:"radial-gradient(ellipse 70% 70% at 50% 50%, black 30%, transparent 75%)",
        pointerEvents:"none" as const }} />

      {/* Top hairline highlight — premium card edge */}
      <div style={{ position:"absolute" as const, top:0, left:"6%", right:"6%", height:"1px",
        background:"linear-gradient(90deg,transparent,rgba(255,255,255,0.18),transparent)" }} />

      {/* On-air glow ring — only when live */}
      {isLive && verse && (
        <>
          <div style={{ position:"absolute" as const, inset:0, borderRadius:"20px",
            boxShadow:`inset 0 0 0 1px ${BLUE}50, 0 0 60px ${BLUE}25`,
            pointerEvents:"none" as const }} />
          <div style={{ position:"absolute" as const, inset:0, borderRadius:"20px",
            background:`radial-gradient(ellipse 80% 100% at 50% 50%, ${BLUE}10 0%, transparent 60%)`,
            pointerEvents:"none" as const }} />
        </>
      )}

      {verse ? (
        <div key={animKey} style={{
          position:"relative" as const, textAlign:"center", padding:"22px 32px",
          maxWidth:"92%", zIndex:1, animation:"verseIn 420ms cubic-bezier(0.22,1,0.36,1) both",
        }}>
          {/* Country-pill style ref */}
          <div style={{ display:"inline-flex", alignItems:"center", gap:"8px",
            padding:"5px 14px", marginBottom:"18px",
            border:"1px solid rgba(255,255,255,0.10)", borderRadius:"100px",
            background:"rgba(255,255,255,0.04)", boxShadow:HILITE }}>
            <span style={{ width:"6px", height:"6px", borderRadius:"50%", background:GOLD,
              boxShadow:`0 0 8px ${GOLD}80` }} />
            <span style={{ fontSize:"10px", fontWeight:700, color:T1,
              letterSpacing:"0.06em" }}>
              {verse.verseRef}
            </span>
            <span style={{ fontSize:"9px", color:T3, letterSpacing:"0.10em" }}>·</span>
            <span style={{ fontSize:"9px", fontWeight:600, color:T2, letterSpacing:"0.08em" }}>
              {verse.translation}
            </span>
          </div>

          {/* Verse text — refined serif */}
          <p style={{ fontSize:"clamp(13px,1.1vw,17px)", color:"#EAF1FB",
            fontFamily:"'New York','Times New Roman',Georgia,serif",
            fontStyle:"italic", lineHeight:1.7, margin:0, fontWeight:400,
            letterSpacing:"-0.005em" }}>
            {verse.verseText}
          </p>

        </div>
      ) : (
        <div style={{ textAlign:"center", zIndex:1, opacity:0.32 }}>
          <div style={{ width:"56px", height:"56px", borderRadius:"50%",
            border:"1px dashed rgba(255,255,255,0.18)",
            display:"flex", alignItems:"center", justifyContent:"center",
            margin:"0 auto 14px" }}>
            <span style={{ fontSize:"22px", color:T3 }}>⏻</span>
          </div>
          <p style={{ fontSize:"11px", color:T3, margin:0, letterSpacing:"0.04em",
            fontFamily:"'New York',Georgia,serif", fontStyle:"italic" }}>{empty}</p>
        </div>
      )}
    </div>
  );
}

// ── Main ──────────────────────────────────────────────────────────────────────
export default function LivePage() {
  const [state,         setState]         = useState<ServiceState>("idle");
  // verses come from the bundled public-domain King James text
  const translation = "KJV";
  const [elapsed,       setElapsed]       = useState(0);
  const [transcript,    setTranscript]    = useState("");
  const [transcriptLog, setTranscriptLog] = useState<{ text: string; detected: boolean }[]>([]);
  const [listening,     setListening]     = useState(false);
  const [micError,      setMicError]      = useState("");
  const [previewVerse,  setPreviewVerse]  = useState<DetectedVerse | null>(null);
  const [liveVerse,     setLiveVerse]     = useState<DetectedVerse | null>(null);
  const [goLive,        setGoLive]        = useState(false);
  const [detections,    setDetections]    = useState<DetectedVerse[]>([]);
  const [queue,         setQueue]         = useState<QueueItem[]>([]);
  const [sidebarTab,    setSidebarTab]    = useState<"detections" | "queue">("detections");
  const [autoMode,      setAutoMode]      = useState(false);
  const [searchMode,    setSearchMode]    = useState<SearchMode>("book");
  const [searchQuery,   setSearchQuery]   = useState("");
  const [chapterVerses, setChapterVerses] = useState<{ num: number; ref: string; text: string }[]>([]);
  const [chapterTitle,  setChapterTitle]  = useState("");
  const [chapterBook,   setChapterBook]   = useState("");
  const [chapterNum,    setChapterNum]    = useState(0);
  const [ctxResults,    setCtxResults]    = useState<DetectedVerse[]>([]);
  const [searchLoading, setSearchLoading] = useState(false);

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const recognitionRef = useRef<any>(null);
  const timerRef       = useRef<ReturnType<typeof setInterval> | null>(null);
  const detectRef      = useRef<ReturnType<typeof setTimeout> | null>(null);
  const transcriptRef  = useRef("");
  const logRef         = useRef<HTMLDivElement>(null);

  // Hide site header during live broadcast
  useEffect(() => {
    const header = document.querySelector("header") as HTMLElement | null;
    if (!header) return;
    if (state === "live") {
      header.style.transition = "opacity 250ms ease";
      header.style.opacity    = "0";
      header.style.pointerEvents = "none";
      const t = setTimeout(() => { header.style.display = "none"; }, 260);
      return () => clearTimeout(t);
    } else {
      header.style.display    = "";
      header.style.opacity    = "0";
      const t = setTimeout(() => {
        header.style.opacity       = "1";
        header.style.pointerEvents = "";
      }, 20);
      return () => clearTimeout(t);
    }
  }, [state]);

  useEffect(() => {
    if (state !== "live") return;
    timerRef.current = setInterval(() => setElapsed(s => s + 1), 1000);
    return () => { if (timerRef.current) clearInterval(timerRef.current); };
  }, [state]);

  useEffect(() => {
    if (goLive && previewVerse) setLiveVerse(previewVerse);
    else if (!goLive) setLiveVerse(null);
  }, [goLive, previewVerse]);

  /* The projector window: whatever is on air here is on the projector there.
     Both windows are on the church laptop, so they talk through the browser
     itself; nothing goes through a server. A projector opened late asks what
     is showing and is told at once. */
  const projectorRef = useRef<BroadcastChannel | null>(null);
  const onAirRef = useRef<DetectedVerse | null>(null);
  useEffect(() => {
    if (typeof BroadcastChannel === "undefined") return;
    const channel = new BroadcastChannel(PROJECTOR_CHANNEL);
    projectorRef.current = channel;
    const send = () => channel.postMessage({ type: "verse", verse: onAirRef.current });
    channel.onmessage = (e) => { if (e.data?.type === "hello") send(); };
    send();
    const bye = () => channel.postMessage({ type: "bye" });
    window.addEventListener("pagehide", bye);
    return () => { window.removeEventListener("pagehide", bye); bye(); channel.close(); projectorRef.current = null; };
  }, []);
  useEffect(() => {
    const onAir = state === "live" ? liveVerse : null;
    onAirRef.current = onAir;
    projectorRef.current?.postMessage({ type: "verse", verse: onAir });
  }, [liveVerse, state]);

  useEffect(() => {
    if (logRef.current) logRef.current.scrollTop = logRef.current.scrollHeight;
  }, [transcriptLog, transcript]);

  // "John 3 16", "John three sixteen", "first Corinthians 13 4" all read as references
  function extractExplicitRefs(text: string): string[] {
    return [...new Set(findSpokenRefs(text).map(r => r.label))];
  }

  /* The last minute of verses already found: a verse is shown once, not again
     every time the sentence that named it is looked at. */
  const recentRefsRef = useRef(new Map<string, number>());
  function isNewRef(ref: string) {
    const now = Date.now(), last = recentRefsRef.current.get(ref);
    if (last && now - last < 60_000) return false;
    recentRefsRef.current.set(ref, now);
    return true;
  }

  // `recentText` is the last few seconds of speech, so a reference split across
  // two phrases ("Romans 8" ... "28") is still read whole
  const detectVerses = useCallback(async (recentText: string, newText = recentText) => {
    if (recentText.trim().length < 4) return;
    const explicitRefs = extractExplicitRefs(recentText);
    // the most recently spoken references, not the first ones in the window
    for (const ref of explicitRefs.slice(-2)) {
      try {
        const sr = await fetch(`/api/bible/search?q=${encodeURIComponent(ref)}&translation=${translation}`);
        const sd = await sr.json(); const top = (sd.results ?? [])[0]; if (!top) continue;
        if (!isNewRef(top.ref)) continue;
        const v: DetectedVerse = { verseRef: top.ref, verseText: top.text, translation, confidence: 1.0, detectedAt: new Date().toISOString(), source: "ai" };
        setDetections(prev => [v, ...prev.filter(p => p.verseRef !== v.verseRef)].slice(0, 12));
        // a spoken verse is certain: auto-project it in auto mode. A chapter on its
        // own ("Romans chapter eight") waits in preview for the verse number.
        const wholeVerse = ref.includes(":");
        setAutoMode(auto => {
          if (auto && wholeVerse) { setPreviewVerse(v); setLiveVerse(v); setGoLive(true); }
          else { setPreviewVerse(prev => prev ?? v); }
          return auto;
        });
      } catch { /* continue */ }
    }
    // a reference just said is the verse; no need to also match it as a quote
    if (extractExplicitRefs(newText).length) return;
    try {
      // Quoted-scripture detection: confidence-gated so normal preaching
      // ("God wants to bless your family") never flashes a wrong verse.
      const semanticQuery = recentText.slice(-350).trim();
      if (semanticQuery.length < 15) return;
      const dr = await fetch("/api/detect-verses", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        // references are read above; the server only matches quoted words here
        body: JSON.stringify({ transcript: semanticQuery, quotesOnly: true }),
      });
      const det = await dr.json() as { detected: boolean; ref: string; text: string; confidence: number };
      if (!det.detected || det.confidence < 0.65) return;
      if (!isNewRef(det.ref)) return;

      const v: DetectedVerse = {
        verseRef: det.ref, verseText: det.text, translation,
        confidence: det.confidence, detectedAt: new Date().toISOString(), source: "ai" as const,
      };
      setDetections(prev => [v, ...prev.filter(p => p.verseRef !== v.verseRef)].slice(0, 12));
      setAutoMode(auto => {
        // auto-project only high-confidence quotes; lower ones wait in preview
        if (auto && det.confidence >= 0.8) { setPreviewVerse(v); setLiveVerse(v); setGoLive(true); }
        else { setPreviewVerse(prev => prev ?? v); }
        return auto;
      });
    } catch { /* offline */ }
  }, [translation]);

  function startListening() {
    const Win = window as unknown as Record<string, unknown>;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const SR = (Win.SpeechRecognition ?? Win.webkitSpeechRecognition) as (new() => any) | undefined;
    if (!SR) { setMicError("Speech recognition requires Chrome or Edge."); return; }
    const rec = new SR(); rec.continuous = true; rec.interimResults = true; rec.lang = "en-US";
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    rec.onresult = (e: any) => {
      let interim = "", finalChunk = "";
      for (let i = e.resultIndex; i < e.results.length; i++) {
        const t = e.results[i][0].transcript;
        if (e.results[i].isFinal) finalChunk += t + " "; else interim += t;
      }
      if (finalChunk) {
        const before = transcriptRef.current.slice(-160);
        transcriptRef.current += finalChunk;
        const hasRef = findSpokenRefs(finalChunk).length > 0;
        setTranscriptLog(prev => [...prev, { text: finalChunk.trim(), detected: hasRef }].slice(-60));
        detectVerses(before + finalChunk, finalChunk);
      }
      setTranscript(interim);
    };
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    rec.onerror = (e: any) => { if (e.error === "not-allowed") setMicError("Microphone access denied."); setListening(false); };
    rec.onend = () => { if (recognitionRef.current === rec) { try { rec.start(); } catch { /* ended */ } } };
    rec.start(); recognitionRef.current = rec; setListening(true); setMicError("");
  }

  function stopListening() { recognitionRef.current?.stop(); recognitionRef.current = null; setListening(false); }
  function startService() {
    setMicError(""); setState("live"); setElapsed(0);
    setDetections([]); setQueue([]); setPreviewVerse(null); setLiveVerse(null); setGoLive(false);
    setTranscriptLog([]); transcriptRef.current = ""; startListening();
  }
  function endService() { stopListening(); if (timerRef.current) clearInterval(timerRef.current); setState("ended"); }
  function addToQueue(v: DetectedVerse) { setQueue(p => [...p, { ...v, queueId: `${v.verseRef}-${Date.now()}` }]); }
  function removeFromQueue(id: string) { setQueue(p => p.filter(q => q.queueId !== id)); }
  function playFromQueue(item: QueueItem) { presentVerse(item); removeFromQueue(item.queueId); }
  function presentVerse(v: DetectedVerse) { setPreviewVerse(v); setLiveVerse(v); setGoLive(true); }

  const runSearch = useCallback(async (q: string, mode: SearchMode) => {
    if (!q.trim()) { setChapterVerses([]); setCtxResults([]); setChapterTitle(""); setChapterBook(""); setChapterNum(0); return; }
    setSearchLoading(true);
    try {
      const res = await fetch(`/api/bible/search?q=${encodeURIComponent(q)}&translation=${translation}`);
      const data = await res.json(); const list = data.results ?? [];
      if (mode === "book") {
        const title = (list[0]?.ref ?? q).replace(/:\d+$/, "").trim() || q;
        setChapterTitle(title);
        const match = title.match(/^(.*?)\s+(\d+)$/);
        if (match) { setChapterBook(match[1].trim()); setChapterNum(parseInt(match[2])); }
        setChapterVerses(list.map((r: { ref: string; text: string }, i: number) => ({ num: i + 1, ref: r.ref, text: r.text })));
        setCtxResults([]);
      } else {
        setCtxResults(list.map((r: { ref: string; text: string }) => ({ verseRef: r.ref, verseText: r.text, translation, confidence: 1, detectedAt: new Date().toISOString() })));
        setChapterVerses([]);
      }
    } catch { setChapterVerses([]); setCtxResults([]); }
    finally { setSearchLoading(false); }
  }, [translation]);

  function navChapter(delta: number) {
    const next = chapterNum + delta;
    if (next < 1) return;
    setSearchQuery(`${chapterBook} ${next}`);
  }

  useEffect(() => {
    const id = setTimeout(() => runSearch(searchQuery, searchMode), 500);
    return () => clearTimeout(id);
  }, [searchQuery, searchMode, runSearch]);

  useEffect(() => {
    if (state !== "live") return;
    function onKey(e: KeyboardEvent) {
      const tag = (e.target as HTMLElement).tagName;
      if (tag === "INPUT" || tag === "TEXTAREA" || tag === "SELECT") return;
      if (e.key === "Enter" || e.key === " ") { e.preventDefault(); if (previewVerse) { setGoLive(true); setLiveVerse(previewVerse); } }
      if (e.key === "Escape") { setLiveVerse(null); setPreviewVerse(null); setGoLive(false); }
      if (e.key === "q" || e.key === "Q") { if (previewVerse) addToQueue({ ...previewVerse, source: "manual" }); }
      if (e.key === "c" || e.key === "C") { setLiveVerse(null); setGoLive(false); }
      if (e.key === "a" || e.key === "A") { setAutoMode(v => !v); }
    }
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [state, previewVerse]);

  useEffect(() => () => {
    recognitionRef.current?.stop();
    if (timerRef.current) clearInterval(timerRef.current);
    if (detectRef.current) clearTimeout(detectRef.current);
  }, []);


  const SERIF = "'New York','Times New Roman',Georgia,serif";

  // ══════════════════════════════════════════════════════════════════════════
  // IDLE — VPN-app inspired
  // ══════════════════════════════════════════════════════════════════════════
  if (state === "idle") return (
    <>
      <style>{`
        @keyframes fadeInUp{from{opacity:0;transform:translateY(20px)}to{opacity:1;transform:translateY(0)}}
        @keyframes subtlePulse{0%,100%{opacity:1}50%{opacity:0.55}}
        @keyframes ringSpin{to{transform:rotate(360deg)}}
        .pk-card-hover{transition:all 240ms cubic-bezier(0.22,1,0.36,1)}
        .pk-card-hover:hover{border-color:rgba(255,255,255,0.14)!important;background:rgba(255,255,255,0.04)!important;transform:translateY(-2px)}
        .pk-power-btn:hover .pk-power-glow{opacity:1!important}
        .pk-power-btn:hover{transform:scale(1.02)}
        .pk-power-btn:active{transform:scale(0.98)}
      `}</style>
      <section id="live-top" className="lv-hero pk-bleed" data-nav="dark" style={{ minHeight:"calc(100vh - 64px)",
        background:`
          radial-gradient(ellipse 80% 60% at 50% 30%, rgba(122,31,255,0.20) 0%, transparent 65%),
          radial-gradient(ellipse 60% 50% at 50% 100%, rgba(60,0,150,0.22) 0%, transparent 60%),
          ${VOID}
        `,
        display:"flex", flexDirection:"column" as const,
        alignItems:"center", justifyContent:"center",
        position:"relative" as const, overflow:"hidden", padding:"120px 24px 80px" }}>

        {/* faint star field */}
        <div style={{ position:"absolute" as const, inset:0, opacity:0.06,
          backgroundImage:"radial-gradient(rgba(220,200,255,0.5) 1px, transparent 1px)",
          backgroundSize:"18px 18px",
          maskImage:"radial-gradient(ellipse 60% 60% at 50% 50%, black 30%, transparent 80%)",
          WebkitMaskImage:"radial-gradient(ellipse 60% 60% at 50% 50%, black 30%, transparent 80%)" }} />

        <div style={{ textAlign:"center", maxWidth:"620px", position:"relative" as const, zIndex:1, animation:"fadeInUp 700ms cubic-bezier(.22,1,.36,1) both" }}>

          <p style={{ margin:"0 0 22px", fontSize:"13px", fontWeight:700, color:"#ffd68c", letterSpacing:"0.16em", textTransform:"uppercase" as const, fontFamily:"var(--pk-font-sans)" }}>Live Sermon</p>

          {/* the wordmark */}
          <h1 style={{ fontSize:"clamp(56px,9vw,104px)", fontWeight:600, color:T1, margin:0,
            fontFamily:"var(--pk-font-display)", letterSpacing:"-0.035em", lineHeight:0.98 }}>
            Prayer<span style={{ fontStyle:"italic", color:"#ffd68c" }}>Key</span>
          </h1>
          <p style={{ margin:"22px auto 0", maxWidth:"440px", fontSize:"clamp(17px,1.6vw,20px)", lineHeight:1.55, color:T2, fontFamily:"var(--pk-font-sans)" }}>
            Every verse the preacher quotes, on the screen by itself.
          </p>

          {/* the start button */}
          <div style={{ position:"relative" as const, width:"180px", height:"180px", margin:"44px auto 26px" }}>
            <div style={{ position:"absolute" as const, inset:0, borderRadius:"50%",
              border:"1px solid rgba(255,255,255,0.12)" }} />
            <div className="pk-power-glow" style={{ position:"absolute" as const, inset:"-20px", borderRadius:"50%",
              background:"radial-gradient(circle, rgba(122,31,255,0.35) 0%, transparent 60%)",
              opacity:0.6, transition:"opacity 300ms", pointerEvents:"none" as const }} />
            <button onClick={startService} className="pk-power-btn" aria-label="Start the live sermon" style={{
              position:"absolute" as const, inset:"22px", borderRadius:"50%",
              background:"radial-gradient(circle at 30% 30%, #3a1470 0%, #1e0638 70%, #120022 100%)",
              border:"1px solid rgba(255,255,255,0.12)",
              boxShadow:"0 14px 40px rgba(0,0,0,0.50), inset 0 1px 0 rgba(255,255,255,0.10), inset 0 -10px 20px rgba(0,0,0,0.4)",
              color:T1, cursor:"pointer",
              display:"flex", alignItems:"center", justifyContent:"center",
              transition:"transform 200ms cubic-bezier(0.22,1,0.36,1)" }}>
              <svg width="42" height="42" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" style={{ filter:"drop-shadow(0 0 8px rgba(255,214,140,0.55))" }}>
                <path d="M18.36 6.64a9 9 0 1 1-12.73 0" />
                <line x1="12" y1="2" x2="12" y2="12" />
              </svg>
            </button>
          </div>

          <p style={{ fontSize:"16px", color:T1, margin:"0 0 6px", fontWeight:600, letterSpacing:"-0.01em", fontFamily:"var(--pk-font-sans)" }}>Tap to start listening</p>
          <p style={{ fontSize:"13px", color:T3, margin:"0 0 30px", fontFamily:"var(--pk-font-sans)" }}>It listens, finds the verse, and puts it on the screen.</p>

          {micError && <div style={{ marginBottom:"20px", padding:"10px 16px", borderRadius:"10px", background:"rgba(255,69,58,0.10)", border:"1px solid rgba(255,69,58,0.30)", color:RED, fontSize:"13px" }}>{micError}</div>}

          {/* the one translation the tool shows: the public-domain King James text */}
          <div className="lv-hero__select">
            <span>Translation</span>
            <strong>King James Version</strong>
          </div>

          <p style={{ marginTop:"26px", fontSize:"12px", color:T3, letterSpacing:"0.02em", fontFamily:"var(--pk-font-sans)" }}>Works in Chrome or Edge, with microphone access.</p>
        </div>
      </section>

      <LiveStory />
    </>
  );

  // ══════════════════════════════════════════════════════════════════════════
  // ENDED
  // ══════════════════════════════════════════════════════════════════════════
  if (state === "ended") return (
    <>
      <style>{`@keyframes fadeInUp{from{opacity:0;transform:translateY(20px)}to{opacity:1;transform:translateY(0)}}`}</style>
      <div style={{ minHeight:"calc(100vh - 64px)",
        background:`radial-gradient(ellipse 70% 50% at 50% 50%, rgba(40,80,150,0.15) 0%, transparent 60%), ${VOID}`,
        display:"flex", alignItems:"center", justifyContent:"center",
        margin:"calc(-1 * clamp(32px,5vw,64px)) calc(-1 * clamp(20px,4vw,80px)) -100px", position:"relative" as const }}>
        <div style={{ textAlign:"center", animation:"fadeInUp 600ms ease both", position:"relative" as const, zIndex:1 }}>
          <div style={{ width:"76px", height:"76px", borderRadius:"50%",
            background:`radial-gradient(circle at 30% 30%, ${GREEN}40, ${GREEN}10 70%)`,
            border:`1px solid ${GREEN}50`,
            display:"flex", alignItems:"center", justifyContent:"center",
            margin:"0 auto 26px", boxShadow:`0 0 40px ${GREEN}25, ${HILITE}` }}>
            <span style={{ fontSize:"30px", color:GREEN }}>✓</span>
          </div>
          <h2 style={{ fontSize:"40px", fontWeight:700, color:T1, marginBottom:"10px", letterSpacing:"-0.03em", fontFamily:SERIF }}>Session Complete</h2>
          <p style={{ color:T2, marginBottom:"36px", fontSize:"14px" }}>
            <span style={{ color:T1, fontWeight:600 }}>{detections.length}</span> verse{detections.length!==1?"s":""} detected · <span style={{ fontFamily:"'SF Mono',monospace", color:T1 }}>{fmt(elapsed)}</span>
          </p>
          <div style={{ height:"1px", background:"linear-gradient(90deg,transparent,rgba(255,255,255,0.14),transparent)", marginBottom:"36px", width:"260px", margin:"0 auto 36px" }} />
          <button onClick={() => { setState("idle"); setElapsed(0); }} style={{
            padding:"14px 36px", borderRadius:"100px",
            border:"1px solid rgba(255,255,255,0.16)",
            background:"rgba(255,255,255,0.06)", color:T1, fontSize:"13px",
            fontWeight:600, cursor:"pointer", letterSpacing:"0.04em",
            boxShadow:HILITE, backdropFilter:"blur(18px)" }}>
            New Session
          </button>
        </div>
      </div>
    </>
  );

  // ══════════════════════════════════════════════════════════════════════════
  // LIVE DASHBOARD — iOS 27 / VPN
  // ══════════════════════════════════════════════════════════════════════════
  return createPortal(
    <div style={{ position:"fixed" as const, top:0, left:0, right:0, bottom:0, zIndex:9900,
      background:`
        radial-gradient(ellipse 70% 50% at 50% 0%, rgba(40,80,150,0.14) 0%, transparent 60%),
        radial-gradient(ellipse 50% 40% at 50% 100%, rgba(20,40,90,0.16) 0%, transparent 50%),
        ${VOID}
      `,
      display:"flex", flexDirection:"column" as const, overflow:"hidden" }}>

      {/* Subtle dotted-globe pattern across whole dashboard */}
      <div style={{ position:"absolute" as const, inset:0, opacity:0.04,
        backgroundImage:"radial-gradient(rgba(180,200,255,0.6) 1px, transparent 1px)",
        backgroundSize:"22px 22px", pointerEvents:"none" as const }} />

      {/* ── TOP BAR — frosted glass with serif timer ── */}
      <div style={{ height:"64px", flexShrink:0, display:"flex", alignItems:"center",
        padding:"0 20px", gap:"14px",
        background:GLASS, backdropFilter:"blur(40px) saturate(180%)", WebkitBackdropFilter:"blur(40px) saturate(180%)",
        boxShadow:`0 1px 0 ${BORDER}, ${HILITE}`, zIndex:2, position:"relative" as const }}>

        {/* Logo + LIVE pill */}
        <div style={{ display:"flex", alignItems:"center", gap:"12px", flexShrink:0 }}>
          <span style={{ fontSize:"15px", fontWeight:700, color:T1,
            fontFamily:SERIF, letterSpacing:"-0.02em" }}>
            Prayer<span style={{ fontStyle:"italic", color:GOLD }}>Key</span>
          </span>
          <div style={{ display:"inline-flex", alignItems:"center", gap:"6px", padding:"4px 11px",
            background:`${RED}15`, border:`1px solid ${RED}40`, borderRadius:"100px",
            boxShadow:HILITE }}>
            <span style={{ width:"5px", height:"5px", borderRadius:"50%", background:RED,
              animation:"liveDot 1.2s ease infinite", boxShadow:`0 0 6px ${RED}` }} />
            <span style={{ fontSize:"9px", fontWeight:800, color:RED, letterSpacing:"0.16em" }}>LIVE</span>
          </div>
        </div>

        {/* Center — secure-connection style timer */}
        <div style={{ flex:1, display:"flex", flexDirection:"column" as const, alignItems:"center", gap:"1px" }}>
          <span style={{ fontSize:"9px", fontWeight:600, color:T3, letterSpacing:"0.18em",
            textTransform:"uppercase" as const }}>Session</span>
          <span style={{ fontSize:"24px", fontWeight:700, color:T1, fontFamily:SERIF,
            letterSpacing:"-0.01em", lineHeight:1, fontVariantNumeric:"tabular-nums" }}>
            {fmtSerif(elapsed)}
          </span>
        </div>

        {/* Right controls */}
        <div style={{ display:"flex", alignItems:"center", gap:"8px", flexShrink:0 }}>
          {/* AUTO — yellow Premium-style when on */}
          <button onClick={() => setAutoMode(v => !v)} style={{
            display:"flex", alignItems:"center", gap:"8px", padding:"6px 12px",
            background: autoMode ? `linear-gradient(135deg, ${YELLOW}25, ${YELLOW}10)` : "rgba(255,255,255,0.04)",
            border:`1px solid ${autoMode ? `${YELLOW}55` : BORDER}`,
            borderRadius:"100px", cursor:"pointer", transition:"all 220ms",
            boxShadow: autoMode ? `0 0 18px ${YELLOW}25, ${HILITE}` : HILITE }}>
            <div style={{ width:"24px", height:"14px", borderRadius:"100px",
              background: autoMode ? YELLOW : "rgba(255,255,255,0.10)",
              position:"relative" as const, transition:"all 200ms",
              boxShadow: autoMode ? `inset 0 0 4px rgba(0,0,0,0.2)` : "none" }}>
              <div style={{ position:"absolute" as const, top:"2px", left: autoMode ? "12px" : "2px",
                width:"10px", height:"10px", borderRadius:"50%",
                background: autoMode ? "#160022" : T2, transition:"left 200ms ease",
                boxShadow:"0 1px 3px rgba(0,0,0,0.4)" }} />
            </div>
            <span style={{ fontSize:"10px", fontWeight:700,
              color: autoMode ? YELLOW : T2, letterSpacing:"0.10em" }}>AUTO</span>
          </button>

          {/* Translation pill */}
          <span title="King James Version"
            style={{ padding:"7px 12px", background:"rgba(255,255,255,0.04)", backdropFilter:"blur(12px)",
              border:`1px solid ${BORDER}`, borderRadius:"100px",
              color:T1, fontSize:"11px", fontWeight:700, boxShadow:HILITE }}>
            KJV
          </span>

          {/* Projector */}
          <a href="/live/projector" target="prayerkey-projector"
            onClick={(e) => {
              // its own window, to drag onto the projector screen; pressing again brings the same one back
              const w = window.open("/live/projector", "prayerkey-projector", "popup,width=1280,height=720");
              if (w) { e.preventDefault(); w.focus(); }
            }}
            style={{
            padding:"7px 14px", background:"rgba(255,255,255,0.04)",
            border:`1px solid ${BORDER}`, borderRadius:"100px",
            color:T2, fontSize:"10px", fontWeight:600, textDecoration:"none",
            letterSpacing:"0.04em", whiteSpace:"nowrap" as const, boxShadow:HILITE }}>
            Projector ↗
          </a>

          {/* End — refined red */}
          <button onClick={endService} style={{
            padding:"7px 16px", background:`linear-gradient(135deg, ${RED}20, ${RED}10)`,
            border:`1px solid ${RED}45`, borderRadius:"100px",
            color:RED, fontSize:"10px", fontWeight:700, cursor:"pointer",
            letterSpacing:"0.08em", boxShadow:`0 0 14px ${RED}15, ${HILITE}` }}>
            ■ END
          </button>
        </div>
      </div>

      {/* ── MAIN AREA — left column (top+bottom) + right Detections sidebar ── */}
      <div style={{ flex:1, display:"flex", overflow:"hidden", position:"relative" as const, zIndex:1, gap:"10px", padding:"10px" }}>
      <div style={{ flex:1, display:"flex", flexDirection:"column" as const, gap:"10px", overflow:"hidden", minWidth:0 }}>

      {/* ── TOP ROW ── */}
      <div style={{ height:"52%", flexShrink:0, display:"flex", overflow:"hidden",
        position:"relative" as const, zIndex:1, gap:"10px" }}>

        {/* Col 1 — Transcript */}
        <div style={{ width:"210px", flexShrink:0, display:"flex", flexDirection:"column" as const,
          ...card, borderRadius:"16px" }}>
          <div style={{ padding:"12px 14px", borderBottom:`1px solid ${BORDER}`, display:"flex", alignItems:"center", justifyContent:"space-between", flexShrink:0 }}>
            <span style={{ fontSize:"10px", fontWeight:700, color:"rgba(255,255,255,0.88)", letterSpacing:"0.14em", textTransform:"uppercase" as const }}>Transcript</span>
            {listening && (
              <div style={{ display:"flex", alignItems:"center", gap:"5px", padding:"2px 8px",
                background:`${GREEN}15`, border:`1px solid ${GREEN}30`, borderRadius:"100px" }}>
                <span style={{ width:"4px", height:"4px", borderRadius:"50%", background:GREEN, animation:"liveDot 1s ease infinite", boxShadow:`0 0 4px ${GREEN}` }} />
                <span style={{ fontSize:"8px", color:GREEN, letterSpacing:"0.08em", fontWeight:700 }}>LIVE</span>
              </div>
            )}
          </div>
          <div ref={logRef} style={{ flex:1, overflowY:"auto" as const, padding:"8px 10px", display:"flex", flexDirection:"column" as const, gap:"1px" }}>
            {transcriptLog.map((entry, i) => {
              const isRecent = i >= transcriptLog.length - 2;
              return (
                <div key={i} style={{ padding: entry.detected ? "5px 8px" : "2px 4px", borderRadius:"8px",
                  background: entry.detected ? `${GOLD}0E` : "transparent",
                  border: entry.detected ? `1px solid ${GOLD}30` : "1px solid transparent",
                  transition:"all 200ms" }}>
                  {entry.detected && (
                    <div style={{ display:"flex", alignItems:"center", gap:"5px", marginBottom:"2px" }}>
                      <span style={{ width:"4px", height:"4px", borderRadius:"50%", background:GOLD, boxShadow:`0 0 4px ${GOLD}` }} />
                      <span style={{ fontSize:"7px", fontWeight:800, color:GOLD, letterSpacing:"0.12em" }}>VERSE FOUND</span>
                    </div>
                  )}
                  <span style={{ fontSize:"11px", color: entry.detected ? "#FFE0A0" : isRecent ? T1 : T2, lineHeight:1.45, display:"block" }}>{entry.text}</span>
                </div>
              );
            })}
            {transcript && (
              <div style={{ padding:"3px 4px" }}>
                <span style={{ fontSize:"11px", color:T1, lineHeight:1.45, display:"block", fontStyle:"italic" }}>{transcript}</span>
              </div>
            )}
            {!transcriptLog.length && !transcript && (
              <div style={{ textAlign:"center", opacity:0.30, marginTop:"24px", padding:"0 10px" }}>
                <p style={{ fontSize:"10px", color:T3, margin:0, letterSpacing:"0.06em",
                  fontFamily:SERIF, fontStyle:"italic" }}>Awaiting signal…</p>
              </div>
            )}
          </div>
          <div style={{ padding:"10px 12px", borderTop:`1px solid ${BORDER}`, flexShrink:0 }}>
            <button onClick={listening ? stopListening : startListening} style={{
              width:"100%", padding:"9px", borderRadius:"100px",
              border:`1px solid ${listening ? `${RED}40` : "rgba(255,255,255,0.14)"}`,
              background: listening ? `${RED}12` : "rgba(255,255,255,0.06)",
              color: listening ? RED : T1, fontSize:"10px", fontWeight:600, cursor:"pointer",
              display:"flex", alignItems:"center", justifyContent:"center", gap:"7px",
              letterSpacing:"0.04em", boxShadow:HILITE, transition:"all 200ms" }}>
              <span style={{ width:"5px", height:"5px", borderRadius:"50%", background: listening ? RED : T2,
                animation: listening ? "liveDot 1.2s ease infinite" : "none",
                boxShadow: listening ? `0 0 6px ${RED}` : "none", flexShrink:0 }} />
              {listening ? "Stop Listening" : "Start Listening"}
            </button>
          </div>
        </div>

        {/* Col 2 — Program Preview */}
        <div style={{ flex:1, display:"flex", flexDirection:"column" as const, gap:"10px",
          ...card, borderRadius:"16px", padding:"12px" }}>
          <div style={{ display:"flex", alignItems:"center", justifyContent:"space-between", flexShrink:0 }}>
            <span style={{ fontSize:"10px", fontWeight:700, color:"rgba(255,255,255,0.88)", letterSpacing:"0.14em", textTransform:"uppercase" as const }}>Program · Preview</span>
            {previewVerse && (
              <button onClick={() => { setGoLive(true); setLiveVerse(previewVerse); }} style={{
                display:"flex", alignItems:"center", gap:"6px", padding:"5px 14px", borderRadius:"100px",
                background:`linear-gradient(135deg, ${BLUE} 0%, ${BLUE_HI} 100%)`,
                border:`1px solid ${BLUE}80`, color:"#FFFFFF",
                fontSize:"10px", fontWeight:700, cursor:"pointer", letterSpacing:"0.04em",
                boxShadow:`0 4px 14px ${BLUE}50, ${HILITE}` }}>
                <span style={{ fontSize:"7px" }}>▶</span> Send Live
              </button>
            )}
          </div>
          <VerseScreen verse={previewVerse} empty="Stage a verse to preview" isLive={false} />
        </div>

        {/* Col 3 — Live Display */}
        <div style={{ flex:1, display:"flex", flexDirection:"column" as const, gap:"10px",
          ...card, borderRadius:"16px", padding:"12px",
          ...(goLive && liveVerse ? { boxShadow: `0 8px 28px rgba(0,0,0,0.40), 0 0 0 1px ${BLUE}25, ${HILITE}` } : {}) }}>
          <div style={{ display:"flex", alignItems:"center", justifyContent:"space-between", flexShrink:0 }}>
            <div style={{ display:"flex", alignItems:"center", gap:"8px" }}>
              <span style={{ fontSize:"10px", fontWeight:700, color:"rgba(255,255,255,0.88)", letterSpacing:"0.14em", textTransform:"uppercase" as const }}>Live · Display</span>
              {goLive && liveVerse && (
                <div style={{ display:"inline-flex", alignItems:"center", gap:"5px", padding:"3px 9px",
                  background:`${GREEN}15`, border:`1px solid ${GREEN}40`, borderRadius:"100px" }}>
                  <span style={{ width:"4px", height:"4px", borderRadius:"50%", background:GREEN,
                    animation:"liveDot 1.2s ease infinite", boxShadow:`0 0 5px ${GREEN}` }} />
                  <span style={{ fontSize:"8px", fontWeight:800, color:GREEN, letterSpacing:"0.10em" }}>ON AIR</span>
                </div>
              )}
            </div>
            <div style={{ display:"flex", alignItems:"center", gap:"8px" }}>
              <span style={{ fontSize:"9px", color:T3, letterSpacing:"0.06em" }}>Go Live</span>
              <div onClick={() => setGoLive(v => !v)} style={{ width:"36px", height:"20px", borderRadius:"100px",
                background: goLive ? GREEN : "rgba(255,255,255,0.10)",
                border:`1px solid ${goLive ? GREEN : BORDER}`,
                position:"relative" as const, cursor:"pointer", transition:"all 220ms",
                boxShadow: goLive ? `0 0 14px ${GREEN}40, inset 0 0 4px rgba(0,0,0,0.2)` : "inset 0 0 4px rgba(0,0,0,0.2)" }}>
                <div style={{ position:"absolute" as const, top:"2px", left: goLive ? "17px" : "2px",
                  width:"14px", height:"14px", borderRadius:"50%", background:"#FFFFFF",
                  transition:"left 200ms ease", boxShadow:"0 1px 4px rgba(0,0,0,0.4)" }} />
              </div>
            </div>
          </div>
          <VerseScreen verse={liveVerse} empty="No verse on air" isLive={goLive && !!liveVerse} />
        </div>

      </div>

      {/* ── BOTTOM ROW ── */}
      <div style={{ flex:1, display:"flex", overflow:"hidden", position:"relative" as const, zIndex:1, gap:"10px" }}>

        {/* Bible Browser */}
        <div style={{ flex:1, display:"flex", flexDirection:"column" as const, overflow:"hidden",
          ...card, borderRadius:"16px" }}>
          <div style={{ padding:"10px 14px", display:"flex", alignItems:"center", gap:"8px", flexShrink:0,
            borderBottom:`1px solid ${BORDER}` }}>
            {(["book","context"] as SearchMode[]).map(m => (
              <button key={m} onClick={() => setSearchMode(m)} style={{
                padding:"6px 14px", borderRadius:"100px",
                border:`1px solid ${searchMode===m ? "rgba(255,255,255,0.20)" : "transparent"}`,
                background: searchMode===m ? "rgba(255,255,255,0.08)" : "transparent",
                color: searchMode===m ? T1 : T3,
                fontSize:"10px", fontWeight:600, cursor:"pointer", letterSpacing:"0.04em",
                boxShadow: searchMode===m ? HILITE : "none", transition:"all 180ms" }}>
                {m === "book" ? "Book" : "Context"}
              </button>
            ))}
            <div style={{ flex:1, display:"flex", gap:"8px", alignItems:"center" }}>
              <input value={searchQuery} onChange={e => setSearchQuery(e.target.value)}
                placeholder={searchMode==="book" ? "e.g. Psalm 32, John 3" : "Search topic or phrase..."}
                style={{ flex:1, padding:"7px 14px", background:"rgba(255,255,255,0.04)",
                  border:`1px solid ${BORDER}`, borderRadius:"100px", color:T1, fontSize:"11px", outline:"none",
                  boxShadow:HILITE }} />
              <span title="King James Version"
                style={{ padding:"7px 12px", background:"rgba(255,255,255,0.04)",
                  border:`1px solid ${BORDER}`, borderRadius:"100px", color:T1, fontSize:"11px", fontWeight:700,
                  boxShadow:HILITE }}>
                KJV
              </span>
            </div>
          </div>
          <div style={{ flex:1, overflowY:"auto" as const, padding:"8px 10px" }}>
            {searchLoading && (
              <div style={{ textAlign:"center", padding:"30px", color:T3, fontSize:"11px",
                letterSpacing:"0.04em", fontFamily:SERIF, fontStyle:"italic" }}>
                Scanning scripture…
              </div>
            )}
            {searchMode === "book" && !searchLoading && (
              <>
                {chapterTitle && (
                  <div style={{ padding:"8px 10px 12px", display:"flex", alignItems:"center", gap:"10px" }}>
                    <span style={{ fontSize:"18px", fontWeight:700, color:T1, fontFamily:SERIF, letterSpacing:"-0.02em", flex:1 }}>{chapterTitle}</span>
                    {chapterBook && (
                      <div style={{ display:"flex", gap:"6px" }}>
                        <button onClick={() => navChapter(-1)} disabled={chapterNum <= 1} style={{ width:"30px", height:"30px", borderRadius:"50%", background: chapterNum > 1 ? "rgba(255,255,255,0.06)" : "transparent", border:`1px solid ${chapterNum > 1 ? "rgba(255,255,255,0.14)" : BORDER}`, color: chapterNum > 1 ? T1 : T3, fontSize:"14px", cursor: chapterNum > 1 ? "pointer" : "default", display:"flex", alignItems:"center", justifyContent:"center", boxShadow: chapterNum > 1 ? HILITE : "none" }}>‹</button>
                        <button onClick={() => navChapter(+1)} style={{ width:"30px", height:"30px", borderRadius:"50%", background:"rgba(255,255,255,0.06)", border:"1px solid rgba(255,255,255,0.14)", color:T1, fontSize:"14px", cursor:"pointer", display:"flex", alignItems:"center", justifyContent:"center", boxShadow:HILITE }}>›</button>
                      </div>
                    )}
                  </div>
                )}
                {chapterVerses.length > 0 ? chapterVerses.map(v => {
                  const isStaged = previewVerse?.verseRef === v.ref;
                  const isLiveV  = liveVerse?.verseRef   === v.ref;
                  const verse: DetectedVerse = { verseRef:v.ref, verseText:v.text, translation, confidence:1, detectedAt:new Date().toISOString(), source:"manual" };
                  return (
                    <div key={v.ref} style={{ display:"flex", alignItems:"center", gap:"10px", padding:"7px 10px", marginBottom:"2px",
                      borderRadius:"10px",
                      background: isLiveV ? `${BLUE}10` : isStaged ? `${GOLD}08` : "transparent",
                      border: isLiveV ? `1px solid ${BLUE}30` : isStaged ? `1px solid ${GOLD}25` : "1px solid transparent",
                      transition:"all 150ms" }}>
                      <span style={{ fontSize:"10px", color:T3, width:"20px", flexShrink:0, fontFamily:"'SF Mono',monospace" }}>{v.num}</span>
                      <span onClick={() => setPreviewVerse(verse)} style={{ fontSize:"11px", color: isStaged||isLiveV ? T1 : T2, lineHeight:1.55, flex:1, cursor:"pointer" }}>{v.text}</span>
                      {isLiveV   && <span style={{ fontSize:"8px", fontWeight:800, color:BLUE, flexShrink:0, letterSpacing:"0.10em" }}>● LIVE</span>}
                      {isStaged && !isLiveV && <span style={{ fontSize:"10px", color:GOLD, flexShrink:0 }}>◆</span>}
                      <button onClick={e => { e.stopPropagation(); presentVerse(verse); }} style={{ flexShrink:0, padding:"4px 11px", borderRadius:"100px", border:`1px solid ${BLUE}70`, background:`linear-gradient(135deg, ${BLUE}, ${BLUE_HI})`, color:"#FFFFFF", fontSize:"8px", fontWeight:700, cursor:"pointer", boxShadow:`0 2px 8px ${BLUE}40, ${HILITE}` }}>▶</button>
                      <button onClick={e => { e.stopPropagation(); addToQueue(verse); }} style={{ flexShrink:0, padding:"4px 9px", borderRadius:"100px", border:`1px solid ${BORDER}`, background:"transparent", color:T3, fontSize:"8px", cursor:"pointer" }}>+Q</button>
                    </div>
                  );
                }) : !chapterTitle && (
                  <div style={{ textAlign:"center", padding:"14px 20px", opacity:0.55 }}>
                    <p style={{ fontSize:"11px", color:T3, margin:0, fontFamily:SERIF, fontStyle:"italic" }}>Enter book + chapter…</p>
                  </div>
                )}
              </>
            )}
            {searchMode === "context" && !searchLoading && (
              <>
                {ctxResults.length > 0 ? ctxResults.map((v, i) => (
                  <div key={i} style={{ padding:"11px 14px", marginBottom:"7px",
                    background:"rgba(255,255,255,0.03)",
                    border:`1px solid ${BORDER}`, borderRadius:"12px", boxShadow:HILITE }}>
                    <div style={{ display:"flex", alignItems:"center", justifyContent:"space-between", marginBottom:"7px" }}>
                      <span style={{ fontSize:"12px", fontWeight:700, color:GOLD }}>{v.verseRef}</span>
                      <div style={{ display:"flex", gap:"5px" }}>
                        <button onClick={() => presentVerse({ ...v, source:"manual" })} style={{ padding:"4px 12px", borderRadius:"100px", background:`linear-gradient(135deg, ${BLUE}, ${BLUE_HI})`, border:`1px solid ${BLUE}80`, color:"#FFFFFF", fontSize:"9px", fontWeight:700, cursor:"pointer", boxShadow:`0 2px 8px ${BLUE}40, ${HILITE}` }}>▶ Present</button>
                        <button onClick={() => addToQueue({ ...v, source:"manual" })} style={{ padding:"4px 10px", borderRadius:"100px", background:"transparent", border:`1px solid ${BORDER}`, color:T3, fontSize:"9px", cursor:"pointer" }}>+Q</button>
                      </div>
                    </div>
                    <p style={{ fontSize:"11px", color:T2, margin:0, lineHeight:1.55, fontStyle:"italic", fontFamily:SERIF }}>{v.verseText.slice(0,140)}{v.verseText.length>140?"…":""}</p>
                  </div>
                )) : (
                  <div style={{ textAlign:"center", padding:"14px 20px", opacity:0.55 }}>
                    <p style={{ fontSize:"11px", color:T3, margin:0, fontFamily:SERIF, fontStyle:"italic" }}>Search by topic or phrase…</p>
                  </div>
                )}
              </>
            )}
          </div>
        </div>

      </div>

      </div>{/* end left column */}

      {/* ── DETECTIONS / QUEUE — tall right sidebar with tab toggle ── */}
      <div style={{ width:"300px", flexShrink:0, display:"flex", flexDirection:"column" as const,
        ...card, borderRadius:"16px" }}>
        <div style={{ padding:"10px 12px", borderBottom:`1px solid ${BORDER}`, display:"flex", alignItems:"center", justifyContent:"space-between", gap:"8px", flexShrink:0 }}>
          {/* Segmented tab pill */}
          <div style={{ display:"flex", padding:"3px", background:"rgba(255,255,255,0.04)", border:`1px solid ${BORDER}`, borderRadius:"100px", boxShadow:HILITE }}>
            {(["detections","queue"] as const).map(tab => {
              const active = sidebarTab === tab;
              const count  = tab === "detections" ? detections.length : queue.length;
              const label  = tab === "detections" ? "Detections" : "Queue";
              return (
                <button key={tab} onClick={() => setSidebarTab(tab)} style={{
                  display:"flex", alignItems:"center", gap:"6px", padding:"4px 11px", borderRadius:"100px",
                  background: active ? "rgba(255,255,255,0.10)" : "transparent",
                  border: active ? "1px solid rgba(255,255,255,0.14)" : "1px solid transparent",
                  color: active ? T1 : T3, fontSize:"10px", fontWeight:700, cursor:"pointer",
                  letterSpacing:"0.06em", textTransform:"uppercase" as const,
                  boxShadow: active ? HILITE : "none", transition:"all 180ms" }}>
                  {label}
                  {count > 0 && (
                    <span style={{ fontSize:"9px", fontWeight:800, color:"#160022", background:YELLOW,
                      padding:"1px 6px", borderRadius:"100px", letterSpacing:0,
                      boxShadow: active ? `0 0 8px ${YELLOW}40` : "none" }}>{count}</span>
                  )}
                </button>
              );
            })}
          </div>
          {sidebarTab === "detections" && detections.length > 0 && (
            <button onClick={() => setDetections([])} style={{ fontSize:"8px", color:T3, background:"none", border:"none", cursor:"pointer", letterSpacing:"0.04em" }}>Clear</button>
          )}
          {sidebarTab === "queue" && queue.length > 0 && (
            <button onClick={() => setQueue([])} style={{ fontSize:"8px", color:T3, background:"none", border:"none", cursor:"pointer", letterSpacing:"0.04em" }}>Clear</button>
          )}
        </div>

        {/* Body */}
        <div style={{ flex:1, overflowY:"auto" as const, padding:"10px" }}>
          {sidebarTab === "detections" ? (
            detections.length === 0 ? (
              <div style={{ padding:"14px 12px", textAlign:"center", opacity:0.55 }}>
                <p style={{ fontSize:"11px", color:T3, margin:0, fontFamily:SERIF, fontStyle:"italic" }}>Awaiting verses…</p>
              </div>
            ) : detections.map((v, i) => {
              const confColor = v.confidence >= 0.9 ? GREEN : v.confidence >= 0.7 ? YELLOW : ORANGE;
              return (
                <div key={i} style={{ padding:"8px 10px", marginBottom:"5px",
                  background:"rgba(255,255,255,0.03)", border:`1px solid ${BORDER}`,
                  borderRadius:"10px", boxShadow:HILITE,
                  animation: i === 0 ? "slideInRight 320ms cubic-bezier(0.22,1,0.36,1)" : "none" }}>
                  <div style={{ display:"flex", alignItems:"center", gap:"7px", marginBottom:"4px" }}>
                    <div style={{ width:"5px", height:"5px", borderRadius:"50%", background:confColor, flexShrink:0, boxShadow:`0 0 4px ${confColor}` }} />
                    <span style={{ fontSize:"11px", fontWeight:700, color:GOLD, flex:1 }}>{v.verseRef}</span>
                    <button onClick={() => presentVerse(v)} style={{ padding:"3px 9px", borderRadius:"100px",
                      background:`linear-gradient(135deg, ${BLUE}, ${BLUE_HI})`, border:`1px solid ${BLUE}80`, color:"#FFFFFF", fontSize:"8px",
                      fontWeight:700, cursor:"pointer", letterSpacing:"0.04em", boxShadow:`0 2px 6px ${BLUE}40, ${HILITE}` }}>▶ Present</button>
                    <button onClick={() => addToQueue(v)} style={{ padding:"3px 8px", borderRadius:"100px",
                      background:"rgba(255,255,255,0.04)", border:`1px solid ${BORDER}`, color:T2, fontSize:"8px", cursor:"pointer", boxShadow:HILITE }}>+Q</button>
                  </div>
                  <p style={{ fontSize:"10.5px", color:T2, margin:0, lineHeight:1.45, fontStyle:"italic", fontFamily:SERIF,
                    overflow:"hidden", display:"-webkit-box", WebkitLineClamp:2, WebkitBoxOrient:"vertical" as const }}>
                    {v.verseText}
                  </p>
                </div>
              );
            })
          ) : (
            queue.length === 0 ? (
              <div style={{ padding:"14px 12px", textAlign:"center", opacity:0.55 }}>
                <p style={{ fontSize:"11px", color:T3, margin:0, lineHeight:1.6 }}>
                  <span style={{ fontFamily:SERIF, fontStyle:"italic" }}>Queue empty.</span><br/>
                  Press <kbd style={{ fontFamily:"'SF Mono',monospace", color:T2, fontSize:"8px", padding:"1px 5px",
                    background:"rgba(255,255,255,0.06)", border:"1px solid rgba(255,255,255,0.12)", borderRadius:"4px" }}>Q</kbd> on a staged verse to add
                </p>
              </div>
            ) : queue.map((item, i) => (
              <div key={item.queueId} style={{ padding:"8px 10px", marginBottom:"5px",
                background:"rgba(255,255,255,0.03)", border:`1px solid ${BORDER}`,
                borderRadius:"10px", display:"flex", alignItems:"center", gap:"8px", boxShadow:HILITE }}>
                <span style={{ fontSize:"9px", color:T3, fontFamily:"'SF Mono',monospace", width:"14px", flexShrink:0 }}>{i+1}</span>
                <div style={{ flex:1, minWidth:0 }}>
                  <div style={{ fontSize:"11px", fontWeight:700, color:GOLD, whiteSpace:"nowrap" as const,
                    overflow:"hidden", textOverflow:"ellipsis" }}>{item.verseRef}</div>
                  <div style={{ fontSize:"10px", color:T2, marginTop:"2px", whiteSpace:"nowrap" as const,
                    overflow:"hidden", textOverflow:"ellipsis", fontStyle:"italic", fontFamily:SERIF }}>{item.verseText}</div>
                </div>
                <button onClick={() => playFromQueue(item)} style={{ padding:"3px 9px", borderRadius:"100px",
                  background:`linear-gradient(135deg, ${BLUE}, ${BLUE_HI})`, border:`1px solid ${BLUE}80`, color:"#FFFFFF", fontSize:"8px",
                  fontWeight:700, cursor:"pointer", letterSpacing:"0.04em", boxShadow:`0 2px 6px ${BLUE}40, ${HILITE}`, flexShrink:0 }}>▶</button>
                <button onClick={() => removeFromQueue(item.queueId)} style={{ width:"20px", height:"20px", borderRadius:"50%",
                  background:"transparent", border:`1px solid ${BORDER}`, color:T3, fontSize:"10px",
                  cursor:"pointer", display:"flex", alignItems:"center", justifyContent:"center", flexShrink:0 }}>✕</button>
              </div>
            ))
          )}
        </div>
      </div>

      </div>{/* end main area flex-row */}

      {/* ── SHORTCUT STRIP ── */}
      <div style={{ height:"32px", flexShrink:0, display:"flex", alignItems:"center", padding:"0 20px", gap:"22px",
        background:GLASS, backdropFilter:"blur(40px) saturate(180%)", WebkitBackdropFilter:"blur(40px) saturate(180%)",
        borderTop:`1px solid ${BORDER}`, boxShadow:`0 -1px 0 rgba(0,0,0,0.4), ${HILITE}` }}>
        {[["↵","Go live"],["esc","Clear"],["A","Auto"],["Q","Queue"],["C","Blank"]].map(([k,lbl]) => (
          <span key={k} style={{ display:"flex", alignItems:"center", gap:"6px", fontSize:"9px", color:T3, letterSpacing:"0.04em" }}>
            <kbd style={{ padding:"2px 7px", borderRadius:"5px", border:"1px solid rgba(255,255,255,0.14)",
              fontFamily:"'SF Mono',monospace", fontSize:"8px", color:T1, background:"rgba(255,255,255,0.06)",
              boxShadow:HILITE, letterSpacing:0 }}>{k}</kbd>
            <span>{lbl}</span>
          </span>
        ))}
        <div style={{ marginLeft:"auto", display:"flex", alignItems:"center", gap:"18px" }}>
          <span style={{ fontSize:"9px", color: listening ? GREEN : T3, fontFamily:"'SF Mono',monospace", letterSpacing:"0.06em" }}>● MIC {listening?"ON":"OFF"}</span>
        </div>
      </div>

      <style>{`
        @keyframes liveDot{0%,100%{opacity:1;transform:scale(1)}50%{opacity:.35;transform:scale(.7)}}
        @keyframes verseIn{from{opacity:0;transform:translateY(12px) scale(0.97)}to{opacity:1;transform:translateY(0) scale(1)}}
        @keyframes slideInRight{from{opacity:0;transform:translateX(14px)}to{opacity:1;transform:translateX(0)}}
        ::-webkit-scrollbar{width:3px}
        ::-webkit-scrollbar-track{background:transparent}
        ::-webkit-scrollbar-thumb{background:rgba(255,255,255,0.10);border-radius:3px}
        ::-webkit-scrollbar-thumb:hover{background:rgba(255,255,255,0.22)}
      `}</style>
    </div>,
    document.body
  );
}
