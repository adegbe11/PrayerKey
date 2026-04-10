"use client";

import { useState, useEffect, useRef, useCallback } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { io, type Socket } from "socket.io-client";
import {
  Mic, MicOff, Monitor, Square, BookOpen,
  Wifi, WifiOff, Search, List, Palette, Plus,
  Send, X, ChevronDown, ChevronRight, GripVertical,
  ArrowUpFromLine, RefreshCw,
} from "lucide-react";
import type {
  ServerToClientEvents,
  ClientToServerEvents,
  VerseDetectedPayload,
  ServiceStatsPayload,
  TranscriptUpdatePayload,
  ProjectorTheme,
} from "@/types/sermon";

const SOCKET_URL = process.env.NEXT_PUBLIC_SOCKET_URL ?? "http://localhost:3001";

const TRANSLATIONS = [
  "NIV", "KJV", "ESV", "NLT", "NKJV", "NASB", "AMP", "MSG",
  "RVR1960",  // Spanish — Reina-Valera 1960
  "LSG",      // French  — Louis Segond
  "NVI-PT",   // Portuguese — Nova Versão Internacional
] as const;

const DEFAULT_THEME: ProjectorTheme = {
  background: "#030210",
  textColor:  "#FFFFFF",
  refColor:   "#B07C1F",
  scale:      "md",
  animation:  "fade",
};

type RightTab = "verses" | "queue" | "search" | "theme";

interface CrossRef { ref: string; text: string; reason: string; }
interface SearchResult { ref: string; text: string; match: string; }

// ── Audio level meter ────────────────────────────────────────────────
function AudioMeter({ level, active }: { level: number; active: boolean }) {
  const bars = 16;
  return (
    <div className="flex items-end gap-0.5 h-5" title="Microphone level">
      {Array.from({ length: bars }, (_, i) => {
        const threshold = i / bars;
        const lit = active && level > threshold;
        const color = i < bars * 0.6 ? "#34C759" : i < bars * 0.85 ? "#FF9F0A" : "#FF3B30";
        return (
          <div
            key={i}
            style={{
              width:        "3px",
              height:       `${40 + i * 3.5}%`,
              borderRadius: "1px",
              background:   lit ? color : "var(--pk-b2)",
              transition:   "background 60ms linear",
            }}
          />
        );
      })}
    </div>
  );
}

// ── Detected verse card ───────────────────────────────────────────────
function VerseCard({
  v,
  index,
  translation,
  onAddToQueue,
  onPush,
}: {
  v: VerseDetectedPayload;
  index: number;
  translation: string;
  onAddToQueue: (v: VerseDetectedPayload) => void;
  onPush: (v: VerseDetectedPayload) => void;
}) {
  const [expanded, setExpanded]   = useState(false);
  const [crossRefs, setCrossRefs] = useState<CrossRef[]>([]);
  const [loading, setLoading]     = useState(false);

  const pct   = Math.round(v.confidence * 100);
  const color = pct >= 90 ? "var(--pk-teal)" : pct >= 75 ? "var(--pk-gold)" : "var(--pk-t3)";

  async function loadCrossRefs() {
    if (crossRefs.length > 0) { setExpanded((e) => !e); return; }
    setExpanded(true);
    setLoading(true);
    try {
      const res  = await fetch(`/api/bible/cross-refs?ref=${encodeURIComponent(v.verseRef)}&translation=${translation}`);
      const data = await res.json() as { refs: CrossRef[] };
      setCrossRefs(data.refs ?? []);
    } catch {
      setCrossRefs([]);
    } finally {
      setLoading(false);
    }
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.24, ease: [0.4, 0, 0.6, 1], delay: index * 0.04 }}
      className="rounded-xl overflow-hidden"
      style={{ background: "#FFFFFF", boxShadow: "var(--pk-shadow-sm)", borderLeft: `3px solid ${color}` }}
    >
      {/* Main verse row */}
      <div className="p-3">
        <div className="flex items-center justify-between mb-1">
          <span style={{ fontSize: "12px", fontWeight: 700, color, letterSpacing: "-0.003em" }}>
            {v.verseRef}
          </span>
          <div className="flex items-center gap-2">
            <span style={{ fontSize: "10px", color: "var(--pk-t3)" }}>{v.translation}</span>
            <span
              className="px-1.5 py-0.5 rounded-full"
              style={{ fontSize: "10px", fontWeight: 700, color, background: `${color}14` }}
            >
              {pct}%
            </span>
            <span style={{ fontSize: "10px", color: "var(--pk-t3)" }}>{v.detectionMs}ms</span>
          </div>
        </div>

        <p style={{ fontSize: "12px", color: "var(--pk-t1)", lineHeight: 1.5, letterSpacing: "-0.003em" }}>
          {v.verseText}
        </p>

        {v.snippetUsed && (
          <p style={{ fontSize: "10px", color: "var(--pk-t3)", marginTop: "4px" }}>
            Triggered by: &ldquo;{v.snippetUsed}&rdquo;
          </p>
        )}

        {/* Actions */}
        <div className="flex items-center gap-2 mt-2.5">
          <button
            onClick={() => onPush(v)}
            className="flex items-center gap-1 px-2.5 py-1 rounded-lg cursor-pointer"
            style={{ background: "var(--pk-deep)", border: "0.5px solid var(--pk-b1)", fontSize: "10px", fontWeight: 600, color: "var(--pk-t2)" }}
            title="Push directly to projector"
          >
            <ArrowUpFromLine size={10} />
            Push
          </button>
          <button
            onClick={() => onAddToQueue(v)}
            className="flex items-center gap-1 px-2.5 py-1 rounded-lg cursor-pointer"
            style={{ background: "var(--pk-deep)", border: "0.5px solid var(--pk-b1)", fontSize: "10px", fontWeight: 600, color: "var(--pk-t2)" }}
            title="Add to verse queue"
          >
            <Plus size={10} />
            Queue
          </button>
          <button
            onClick={loadCrossRefs}
            className="flex items-center gap-1 px-2.5 py-1 rounded-lg cursor-pointer ml-auto"
            style={{ background: "transparent", border: "none", fontSize: "10px", fontWeight: 600, color: "var(--pk-t3)", cursor: "pointer" }}
            title="Show cross-references"
          >
            Cross-refs
            <ChevronDown
              size={10}
              style={{ transform: expanded ? "rotate(180deg)" : "rotate(0deg)", transition: "transform 200ms" }}
            />
          </button>
        </div>
      </div>

      {/* Cross-references panel */}
      <AnimatePresence>
        {expanded && (
          <motion.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: "auto", opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            transition={{ duration: 0.2 }}
            style={{ borderTop: "0.5px solid var(--pk-b1)", overflow: "hidden" }}
          >
            <div className="p-3" style={{ background: "var(--pk-deep)" }}>
              <p style={{ fontSize: "10px", fontWeight: 700, color: "var(--pk-t3)", letterSpacing: "0.06em", textTransform: "uppercase", marginBottom: "8px" }}>
                Cross-References
              </p>
              {loading ? (
                <div className="flex items-center gap-2">
                  <RefreshCw size={10} style={{ color: "var(--pk-t3)", animation: "spin 1s linear infinite" }} />
                  <span style={{ fontSize: "11px", color: "var(--pk-t3)" }}>Loading…</span>
                </div>
              ) : crossRefs.length === 0 ? (
                <p style={{ fontSize: "11px", color: "var(--pk-t3)" }}>No cross-references found.</p>
              ) : (
                <div className="flex flex-col gap-2">
                  {crossRefs.map((cr) => (
                    <div key={cr.ref} className="rounded-lg p-2" style={{ background: "#FFFFFF", boxShadow: "var(--pk-shadow-sm)" }}>
                      <p style={{ fontSize: "11px", fontWeight: 700, color: "var(--pk-gold)", marginBottom: "2px" }}>{cr.ref}</p>
                      <p style={{ fontSize: "11px", color: "var(--pk-t1)", lineHeight: 1.4 }}>{cr.text}</p>
                      <p style={{ fontSize: "10px", color: "var(--pk-t3)", marginTop: "2px", fontStyle: "italic" }}>{cr.reason}</p>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </motion.div>
  );
}

// ── Stat chip ─────────────────────────────────────────────────────────
function StatChip({ label, value, color }: { label: string; value: string | number; color: string }) {
  return (
    <div className="flex flex-col items-center px-4 py-2 rounded-xl"
      style={{ background: "#FFFFFF", boxShadow: "var(--pk-shadow-sm)", minWidth: "80px" }}>
      <span style={{ fontSize: "20px", fontWeight: 700, color, letterSpacing: "-0.003em" }}>{value}</span>
      <span style={{ fontSize: "10px", color: "var(--pk-t3)", marginTop: "1px" }}>{label}</span>
    </div>
  );
}

// ── Main ──────────────────────────────────────────────────────────────
export default function LiveSermonPage() {
  // ── Service state ────────────────────────────────────────────────
  const [connected, setConnected]       = useState(false);
  const [isLive, setIsLive]             = useState(false);
  const [serviceId, setServiceId]       = useState("");
  const [translation, setTranslation]   = useState<string>("NIV");
  const [stats, setStats]               = useState<ServiceStatsPayload>({ versesDetected: 0, powerMoments: 0, duration: 0, attendees: 0 });

  // ── Transcript state ─────────────────────────────────────────────
  const [transcript, setTranscript]     = useState<string[]>([]);
  const [liveText, setLiveText]         = useState("");
  const [manualInput, setManualInput]   = useState("");

  // ── Verse state ──────────────────────────────────────────────────
  const [currentVerse, setCurrentVerse] = useState<VerseDetectedPayload | null>(null);
  const [verseHistory, setVerseHistory] = useState<VerseDetectedPayload[]>([]);

  // ── Mic + audio metering ─────────────────────────────────────────
  const [micActive, setMicActive]       = useState(false);
  const [audioLevel, setAudioLevel]     = useState(0);

  // ── Right panel ──────────────────────────────────────────────────
  const [rightTab, setRightTab]         = useState<RightTab>("verses");

  // ── Verse queue ──────────────────────────────────────────────────
  const [queue, setQueue]               = useState<VerseDetectedPayload[]>([]);
  const [dragIdx, setDragIdx]           = useState<number | null>(null);
  const [dragOverIdx, setDragOverIdx]   = useState<number | null>(null);

  // ── Fuzzy search ─────────────────────────────────────────────────
  const [searchQuery, setSearchQuery]   = useState("");
  const [searchResults, setSearchResults] = useState<SearchResult[]>([]);
  const [isSearching, setIsSearching]   = useState(false);

  // ── Projector theme ──────────────────────────────────────────────
  const [theme, setTheme]               = useState<ProjectorTheme>(DEFAULT_THEME);

  // ── Refs ─────────────────────────────────────────────────────────
  const socketRef        = useRef<Socket<ServerToClientEvents, ClientToServerEvents> | null>(null);
  const mediaRef         = useRef<MediaStream | null>(null);
  const processorRef     = useRef<ScriptProcessorNode | null>(null);
  const levelRef         = useRef(0);
  const animFrameRef     = useRef<number>(0);
  const transcriptEndRef = useRef<HTMLDivElement>(null);

  // ── Socket.io connection ──────────────────────────────────────────
  useEffect(() => {
    const socket: Socket<ServerToClientEvents, ClientToServerEvents> = io(SOCKET_URL, {
      transports: ["websocket"],
      autoConnect: false,
    });

    socket.on("connect",    () => setConnected(true));
    socket.on("disconnect", () => setConnected(false));

    socket.on("verse:detected", (payload) => {
      setCurrentVerse(payload);
      setVerseHistory((h) => [payload, ...h].slice(0, 30));
    });

    socket.on("verse:suggested", (payload) => {
      setVerseHistory((h) => [{ ...payload, confidence: payload.confidence * 0.9 }, ...h].slice(0, 30));
    });

    socket.on("transcript:update", ({ text, isFinal }: TranscriptUpdatePayload) => {
      if (isFinal) {
        setTranscript((t) => [...t, text].slice(-50));
        setLiveText("");
      } else {
        setLiveText(text);
      }
    });

    socket.on("service:stats", (s: ServiceStatsPayload) => setStats(s));

    socket.connect();
    socketRef.current = socket;

    return () => { socket.disconnect(); };
  }, []);

  // Auto-scroll transcript
  useEffect(() => {
    transcriptEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [transcript, liveText]);

  // ── Start service ─────────────────────────────────────────────────
  const startService = useCallback(async () => {
    const res  = await fetch("/api/service/start", { method: "POST" });
    const data = await res.json() as { serviceId: string };
    setServiceId(data.serviceId);
    socketRef.current?.emit("service:join", data.serviceId);
    socketRef.current?.emit("service:translation", translation, data.serviceId);
    setIsLive(true);
  }, [translation]);

  // ── End service ───────────────────────────────────────────────────
  const endService = useCallback(async () => {
    if (!serviceId) return;
    stopMic();
    await fetch(`/api/service/${serviceId}/end`, { method: "POST" });
    socketRef.current?.emit("service:leave", serviceId);
    setIsLive(false);
    setCurrentVerse(null);
    setServiceId("");
    setQueue([]);
  }, [serviceId]); // eslint-disable-line react-hooks/exhaustive-deps

  // ── Translation change ────────────────────────────────────────────
  function handleTranslationChange(t: string) {
    setTranslation(t);
    if (isLive && serviceId) {
      socketRef.current?.emit("service:translation", t, serviceId);
    }
  }

  // ── Microphone ────────────────────────────────────────────────────
  async function startMic() {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const ctx    = new AudioContext({ sampleRate: 16000 });
      const source = ctx.createMediaStreamSource(stream);
      const proc   = ctx.createScriptProcessor(4096, 1, 1);

      proc.onaudioprocess = (e) => {
        const raw = e.inputBuffer.getChannelData(0);

        // Compute peak level for meter
        let peak = 0;
        for (let i = 0; i < raw.length; i++) {
          const abs = Math.abs(raw[i]);
          if (abs > peak) peak = abs;
        }
        levelRef.current = peak;

        // Encode and send to server
        const int16 = new Int16Array(raw.length);
        for (let i = 0; i < raw.length; i++) {
          int16[i] = Math.max(-32768, Math.min(32767, raw[i] * 32768));
        }
        socketRef.current?.emit("audio:chunk", int16.buffer);
      };

      source.connect(proc);
      proc.connect(ctx.destination);
      mediaRef.current    = stream;
      processorRef.current = proc;
      setMicActive(true);

      // RAF loop to update meter at ~60fps
      function rafLoop() {
        setAudioLevel(levelRef.current);
        animFrameRef.current = requestAnimationFrame(rafLoop);
      }
      animFrameRef.current = requestAnimationFrame(rafLoop);
    } catch {
      alert("Microphone access denied. Please allow microphone access.");
    }
  }

  function stopMic() {
    cancelAnimationFrame(animFrameRef.current);
    processorRef.current?.disconnect();
    mediaRef.current?.getTracks().forEach((t) => t.stop());
    mediaRef.current    = null;
    processorRef.current = null;
    levelRef.current    = 0;
    setAudioLevel(0);
    setMicActive(false);
  }

  // ── Manual transcript submit ──────────────────────────────────────
  function sendManual() {
    if (!manualInput.trim() || !serviceId) return;
    socketRef.current?.emit("transcript:send", manualInput.trim(), serviceId);
    setManualInput("");
  }

  // ── Push verse to projector ───────────────────────────────────────
  function pushToProjector(v: VerseDetectedPayload) {
    if (!serviceId) return;
    socketRef.current?.emit("verse:push", v, serviceId);
  }

  // ── Verse queue ───────────────────────────────────────────────────
  function addToQueue(v: VerseDetectedPayload) {
    setQueue((q) => {
      if (q.some((x) => x.verseRef === v.verseRef)) return q;
      return [...q, v];
    });
    setRightTab("queue");
  }

  function removeFromQueue(idx: number) {
    setQueue((q) => q.filter((_, i) => i !== idx));
  }

  // Drag-and-drop reorder
  function onDragStart(i: number) { setDragIdx(i); }
  function onDragOver(e: React.DragEvent, i: number) { e.preventDefault(); setDragOverIdx(i); }
  function onDrop(i: number) {
    if (dragIdx === null || dragIdx === i) { setDragIdx(null); setDragOverIdx(null); return; }
    const next = [...queue];
    const [item] = next.splice(dragIdx, 1);
    next.splice(i, 0, item);
    setQueue(next);
    setDragIdx(null);
    setDragOverIdx(null);
  }

  // ── Fuzzy Bible search ────────────────────────────────────────────
  async function runSearch() {
    if (!searchQuery.trim()) return;
    setIsSearching(true);
    setSearchResults([]);
    try {
      const res  = await fetch(`/api/bible/search?q=${encodeURIComponent(searchQuery)}&translation=${translation}`);
      const data = await res.json() as { results: SearchResult[] };
      setSearchResults(data.results ?? []);
    } catch {
      setSearchResults([]);
    } finally {
      setIsSearching(false);
    }
  }

  // ── Projector theme ───────────────────────────────────────────────
  function updateTheme(patch: Partial<ProjectorTheme>) {
    const next = { ...theme, ...patch };
    setTheme(next);
    if (serviceId) socketRef.current?.emit("projector:theme", next, serviceId);
  }

  // Duration formatter
  const dur = `${String(Math.floor(stats.duration / 60)).padStart(2, "0")}:${String(stats.duration % 60).padStart(2, "0")}`;

  const RIGHT_TABS: { id: RightTab; label: string; icon: React.ReactNode }[] = [
    { id: "verses", label: "Detected",  icon: <BookOpen size={12} /> },
    { id: "queue",  label: `Queue${queue.length ? ` (${queue.length})` : ""}`, icon: <List size={12} /> },
    { id: "search", label: "Search",    icon: <Search size={12} /> },
    { id: "theme",  label: "Theme",     icon: <Palette size={12} /> },
  ];

  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: "var(--pk-panel)" }}>

      {/* ── Top bar ─────────────────────────────────────────────── */}
      <div className="flex items-center justify-between px-6 py-4 flex-shrink-0"
        style={{ borderBottom: "0.5px solid var(--pk-b1)" }}>
        <div>
          <h1 style={{ fontSize: "22px", fontWeight: 700, color: "var(--pk-t1)", letterSpacing: "-0.003em" }}>
            Live Sermon
          </h1>
          <div className="flex items-center gap-2 mt-0.5">
            {connected
              ? <><Wifi size={11} style={{ color: "var(--pk-teal)" }} /><span style={{ fontSize: "12px", color: "var(--pk-teal)", fontWeight: 500 }}>Connected</span></>
              : <><WifiOff size={11} style={{ color: "var(--pk-live)" }} /><span style={{ fontSize: "12px", color: "var(--pk-live)" }}>Disconnected</span></>}
          </div>
        </div>

        <div className="flex items-center gap-3">
          {/* Translation selector — now includes Spanish, French, Portuguese */}
          <select
            value={translation}
            onChange={(e) => handleTranslationChange(e.target.value)}
            className="rounded-lg px-3 py-1.5 cursor-pointer"
            style={{
              background: "var(--pk-deep)", border: "0.5px solid var(--pk-b2)",
              fontSize: "13px", color: "var(--pk-t1)", fontWeight: 500,
            }}
          >
            {TRANSLATIONS.map((t) => <option key={t}>{t}</option>)}
          </select>

          {/* Projector link */}
          <a
            href={`/live/projector${serviceId ? `?serviceId=${serviceId}` : ""}`}
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-full cursor-pointer"
            style={{
              background: "var(--pk-deep)", border: "0.5px solid var(--pk-b2)",
              fontSize: "12px", color: "var(--pk-t2)", textDecoration: "none",
            }}
          >
            <Monitor size={12} style={{ color: "var(--pk-t3)" }} />
            Open Projector
          </a>

          {/* Start / End */}
          {!isLive ? (
            <button
              onClick={startService}
              disabled={!connected}
              className="flex items-center gap-2 px-4 py-2 rounded-full cursor-pointer"
              style={{
                background: connected ? "#1D1D1F" : "var(--pk-b1)",
                color: "#FFFFFF", border: "none", fontSize: "13px", fontWeight: 600,
              }}
            >
              <span className="w-2 h-2 rounded-full" style={{ background: "var(--pk-live)" }} />
              Start Service
            </button>
          ) : (
            <button
              onClick={endService}
              className="flex items-center gap-2 px-4 py-2 rounded-full cursor-pointer"
              style={{
                background: "rgba(255,59,48,0.08)", color: "var(--pk-live)",
                border: "0.5px solid rgba(255,59,48,0.25)", fontSize: "13px", fontWeight: 600,
              }}
            >
              <Square size={10} fill="currentColor" strokeWidth={0} />
              End Service
            </button>
          )}
        </div>
      </div>

      {/* ── Body ────────────────────────────────────────────────── */}
      <div className="flex flex-1 overflow-hidden">

        {/* ── Left: Transcript + Controls ───────────────────────── */}
        <div className="flex flex-col overflow-hidden" style={{ flex: "0 0 420px", borderRight: "0.5px solid var(--pk-b1)" }}>

          {/* Projector preview — current verse */}
          <div className="flex-shrink-0 flex flex-col items-center justify-center"
            style={{ background: "#050310", minHeight: "180px", padding: "24px 28px" }}>
            <AnimatePresence mode="wait">
              {currentVerse ? (
                <motion.div
                  key={currentVerse.verseRef}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -10 }}
                  transition={{ duration: 0.3 }}
                  className="text-center"
                >
                  <p style={{ fontSize: "17px", fontStyle: "italic", color: "#FFFFFF", lineHeight: 1.6, letterSpacing: "-0.003em", maxWidth: "360px" }}>
                    &ldquo;{currentVerse.verseText}&rdquo;
                  </p>
                  <p style={{ fontSize: "13px", color: "var(--pk-gold)", marginTop: "8px", fontWeight: 600 }}>
                    — {currentVerse.verseRef} ({currentVerse.translation})
                  </p>
                  <p style={{ fontSize: "11px", color: "#2DD4A0", marginTop: "4px" }}>
                    AI detected · {Math.round(currentVerse.confidence * 100)}% · {currentVerse.detectionMs}ms
                  </p>
                </motion.div>
              ) : (
                <motion.div key="empty" initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="text-center">
                  <p style={{ fontSize: "14px", color: "rgba(255,255,255,0.25)" }}>
                    {isLive ? "Listening for Bible verses…" : "Start a service to begin verse detection"}
                  </p>
                </motion.div>
              )}
            </AnimatePresence>
          </div>

          {/* Live transcript */}
          <div className="flex-1 overflow-y-auto p-4" style={{ background: "var(--pk-deep)" }}>
            <p className="pk-label mb-3">Live Transcript</p>
            {transcript.map((line, i) => (
              <p key={i} style={{ fontSize: "13px", color: "var(--pk-t2)", lineHeight: 1.6, marginBottom: "4px", letterSpacing: "-0.003em" }}>
                {line}
              </p>
            ))}
            {liveText && (
              <p style={{ fontSize: "13px", color: "var(--pk-t3)", lineHeight: 1.6, fontStyle: "italic" }}>
                {liveText}
              </p>
            )}
            <div ref={transcriptEndRef} />
          </div>

          {/* Controls */}
          <div className="flex-shrink-0 p-4 flex flex-col gap-2.5"
            style={{ borderTop: "0.5px solid var(--pk-b1)", background: "#FFFFFF" }}>

            {/* Audio meter */}
            <div className="flex items-center gap-3 px-1">
              <span style={{ fontSize: "10px", color: "var(--pk-t3)", fontWeight: 600, textTransform: "uppercase", letterSpacing: "0.06em", whiteSpace: "nowrap" }}>
                MIC LEVEL
              </span>
              <AudioMeter level={audioLevel} active={micActive} />
            </div>

            {/* Mic toggle */}
            <button
              onClick={() => micActive ? stopMic() : startMic()}
              disabled={!isLive}
              className="flex items-center justify-center gap-2 w-full rounded-full py-2.5 cursor-pointer"
              style={{
                background: micActive ? "rgba(255,59,48,0.08)" : (isLive ? "#1D1D1F" : "var(--pk-b1)"),
                color: micActive ? "var(--pk-live)" : (isLive ? "#FFFFFF" : "var(--pk-t3)"),
                border: micActive ? "0.5px solid rgba(255,59,48,0.25)" : "none",
                fontSize: "13px", fontWeight: 600,
              }}
            >
              {micActive ? <><MicOff size={14} /> Stop Microphone</> : <><Mic size={14} /> Start Microphone</>}
            </button>

            {/* Manual input fallback */}
            <div className="flex gap-2">
              <input
                value={manualInput}
                onChange={(e) => setManualInput(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && sendManual()}
                placeholder="Or paste transcript text…"
                disabled={!isLive}
                className="flex-1 rounded-xl px-3 py-2"
                style={{
                  background: "var(--pk-deep)", border: "0.5px solid var(--pk-b1)",
                  fontSize: "12px", color: "var(--pk-t1)", outline: "none",
                }}
              />
              <button
                onClick={sendManual}
                disabled={!isLive || !manualInput.trim()}
                className="px-3 py-2 rounded-xl cursor-pointer flex-shrink-0"
                style={{ background: "#1D1D1F", color: "#FFFFFF", border: "none", fontSize: "12px", fontWeight: 600 }}
              >
                <ChevronRight size={14} />
              </button>
            </div>
          </div>
        </div>

        {/* ── Right panel ───────────────────────────────────────── */}
        <div className="flex flex-col flex-1 overflow-hidden">

          {/* Stats row */}
          {isLive && (
            <div className="flex items-center gap-3 px-5 pt-4 pb-3 flex-shrink-0">
              <div className="flex items-center gap-2 px-3 py-1.5 rounded-full flex-shrink-0"
                style={{ background: "rgba(255,59,48,0.08)", border: "0.5px solid rgba(255,59,48,0.2)" }}>
                <span className="w-2 h-2 rounded-full animate-live-pulse" style={{ background: "var(--pk-live)" }} />
                <span style={{ fontSize: "12px", fontWeight: 700, color: "var(--pk-live)", letterSpacing: "0.05em" }}>LIVE</span>
                <span style={{ fontSize: "12px", color: "var(--pk-t3)", fontVariantNumeric: "tabular-nums" }}>{dur}</span>
              </div>
              <StatChip label="Verses"        value={stats.versesDetected} color="var(--pk-gold)"   />
              <StatChip label="Power Moments" value={stats.powerMoments}   color="var(--pk-purple)" />
              <StatChip label="Attendees"     value={stats.attendees}       color="var(--pk-teal)"   />
            </div>
          )}

          {/* Tab bar */}
          <div className="flex items-center gap-1 px-5 pt-3 pb-3 flex-shrink-0"
            style={{ borderBottom: "0.5px solid var(--pk-b1)" }}>
            {RIGHT_TABS.map((tab) => (
              <button
                key={tab.id}
                onClick={() => setRightTab(tab.id)}
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg cursor-pointer transition-all"
                style={{
                  background: rightTab === tab.id ? "#1D1D1F" : "transparent",
                  color: rightTab === tab.id ? "#FFFFFF" : "var(--pk-t3)",
                  border: "none",
                  fontSize: "12px", fontWeight: 600,
                }}
              >
                {tab.icon}
                {tab.label}
              </button>
            ))}
          </div>

          {/* Tab content */}
          <div className="flex-1 overflow-y-auto p-5">

            {/* ── DETECTED VERSES tab ── */}
            {rightTab === "verses" && (
              <>
                <div className="flex items-center justify-between mb-3">
                  <p style={{ fontSize: "15px", fontWeight: 600, color: "var(--pk-t1)", letterSpacing: "-0.003em" }}>
                    Detected Verses
                  </p>
                  {verseHistory.length > 0 && (
                    <span style={{ fontSize: "12px", color: "var(--pk-t3)" }}>
                      {verseHistory.length} this session
                    </span>
                  )}
                </div>

                {verseHistory.length === 0 ? (
                  <div className="rounded-2xl flex flex-col items-center justify-center"
                    style={{ background: "var(--pk-deep)", border: "0.5px dashed var(--pk-b2)", minHeight: "200px", padding: "40px" }}>
                    <BookOpen size={32} style={{ color: "var(--pk-t3)", marginBottom: "12px" }} strokeWidth={1.2} />
                    <p style={{ fontSize: "14px", color: "var(--pk-t2)", textAlign: "center" }}>No verses detected yet.</p>
                    <p style={{ fontSize: "12px", color: "var(--pk-t3)", marginTop: "4px", textAlign: "center" }}>
                      Start a service and turn on the microphone.
                    </p>
                  </div>
                ) : (
                  <div className="flex flex-col gap-2">
                    {verseHistory.map((v, i) => (
                      <VerseCard
                        key={`${v.verseRef}-${i}`}
                        v={v}
                        index={i}
                        translation={translation}
                        onAddToQueue={addToQueue}
                        onPush={pushToProjector}
                      />
                    ))}
                  </div>
                )}
              </>
            )}

            {/* ── QUEUE tab ── */}
            {rightTab === "queue" && (
              <>
                <div className="flex items-center justify-between mb-3">
                  <p style={{ fontSize: "15px", fontWeight: 600, color: "var(--pk-t1)", letterSpacing: "-0.003em" }}>
                    Verse Queue
                  </p>
                  {queue.length > 0 && (
                    <button
                      onClick={() => setQueue([])}
                      className="flex items-center gap-1 cursor-pointer"
                      style={{ background: "transparent", border: "none", fontSize: "11px", color: "var(--pk-t3)", cursor: "pointer" }}
                    >
                      <X size={10} /> Clear all
                    </button>
                  )}
                </div>

                {queue.length === 0 ? (
                  <div className="rounded-2xl flex flex-col items-center justify-center"
                    style={{ background: "var(--pk-deep)", border: "0.5px dashed var(--pk-b2)", minHeight: "200px", padding: "40px" }}>
                    <List size={32} style={{ color: "var(--pk-t3)", marginBottom: "12px" }} strokeWidth={1.2} />
                    <p style={{ fontSize: "14px", color: "var(--pk-t2)", textAlign: "center" }}>Queue is empty.</p>
                    <p style={{ fontSize: "12px", color: "var(--pk-t3)", marginTop: "4px", textAlign: "center" }}>
                      Click &ldquo;Queue&rdquo; on detected verses or search results to add them here.
                    </p>
                  </div>
                ) : (
                  <div className="flex flex-col gap-2">
                    {queue.map((v, i) => (
                      <div
                        key={`${v.verseRef}-${i}`}
                        draggable
                        onDragStart={() => onDragStart(i)}
                        onDragOver={(e) => onDragOver(e, i)}
                        onDrop={() => onDrop(i)}
                        onDragEnd={() => { setDragIdx(null); setDragOverIdx(null); }}
                        className="flex items-center gap-3 px-3 py-3 rounded-xl"
                        style={{
                          background: dragOverIdx === i ? "var(--pk-b1)" : "#FFFFFF",
                          boxShadow: "var(--pk-shadow-sm)",
                          border: dragOverIdx === i ? "1.5px dashed var(--pk-gold)" : "0.5px solid var(--pk-b1)",
                          opacity: dragIdx === i ? 0.5 : 1,
                          cursor: "grab",
                          transition: "border 150ms, background 150ms",
                        }}
                      >
                        <GripVertical size={14} style={{ color: "var(--pk-t3)", flexShrink: 0 }} />

                        <div className="flex-1 min-w-0">
                          <p style={{ fontSize: "12px", fontWeight: 700, color: "var(--pk-gold)", letterSpacing: "-0.003em" }}>
                            {v.verseRef}
                          </p>
                          <p className="truncate" style={{ fontSize: "11px", color: "var(--pk-t2)", marginTop: "1px" }}>
                            {v.verseText}
                          </p>
                        </div>

                        <div className="flex items-center gap-1.5 flex-shrink-0">
                          <button
                            onClick={() => pushToProjector(v)}
                            disabled={!isLive}
                            className="flex items-center gap-1 px-2.5 py-1.5 rounded-lg cursor-pointer"
                            style={{
                              background: isLive ? "#1D1D1F" : "var(--pk-b1)",
                              color: isLive ? "#FFFFFF" : "var(--pk-t3)",
                              border: "none", fontSize: "10px", fontWeight: 700,
                            }}
                            title="Push to projector"
                          >
                            <Send size={10} />
                          </button>
                          <button
                            onClick={() => removeFromQueue(i)}
                            className="flex items-center px-2 py-1.5 rounded-lg cursor-pointer"
                            style={{ background: "transparent", border: "0.5px solid var(--pk-b1)", color: "var(--pk-t3)", fontSize: "10px" }}
                          >
                            <X size={10} />
                          </button>
                        </div>
                      </div>
                    ))}

                    <p style={{ fontSize: "10px", color: "var(--pk-t3)", textAlign: "center", marginTop: "4px" }}>
                      Drag rows to reorder · Press <Send size={9} style={{ display: "inline", verticalAlign: "middle" }} /> to push to projector
                    </p>
                  </div>
                )}
              </>
            )}

            {/* ── SEARCH tab ── */}
            {rightTab === "search" && (
              <>
                <p style={{ fontSize: "15px", fontWeight: 600, color: "var(--pk-t1)", letterSpacing: "-0.003em", marginBottom: "12px" }}>
                  Bible Verse Search
                </p>

                <div className="flex gap-2 mb-4">
                  <input
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    onKeyDown={(e) => e.key === "Enter" && runSearch()}
                    placeholder="Search by reference, keyword, or topic…"
                    className="flex-1 rounded-xl px-3 py-2.5"
                    style={{
                      background: "var(--pk-deep)", border: "0.5px solid var(--pk-b1)",
                      fontSize: "13px", color: "var(--pk-t1)", outline: "none",
                    }}
                  />
                  <button
                    onClick={runSearch}
                    disabled={!searchQuery.trim() || isSearching}
                    className="flex items-center gap-1.5 px-4 py-2.5 rounded-xl cursor-pointer flex-shrink-0"
                    style={{
                      background: "#1D1D1F", color: "#FFFFFF",
                      border: "none", fontSize: "12px", fontWeight: 600,
                      opacity: !searchQuery.trim() ? 0.4 : 1,
                    }}
                  >
                    {isSearching
                      ? <RefreshCw size={12} style={{ animation: "spin 1s linear infinite" }} />
                      : <Search size={12} />}
                    Search
                  </button>
                </div>

                <p style={{ fontSize: "11px", color: "var(--pk-t3)", marginBottom: "12px" }}>
                  Try &ldquo;John 3&rdquo;, &ldquo;do not fear&rdquo;, &ldquo;healing&rdquo;, or &ldquo;Ps 23&rdquo; · Using {translation}
                </p>

                {isSearching && (
                  <div className="flex items-center gap-2 py-8 justify-center">
                    <RefreshCw size={14} style={{ color: "var(--pk-t3)", animation: "spin 1s linear infinite" }} />
                    <span style={{ fontSize: "13px", color: "var(--pk-t3)" }}>Searching…</span>
                  </div>
                )}

                {!isSearching && searchResults.length > 0 && (
                  <div className="flex flex-col gap-2">
                    {searchResults.map((r, i) => (
                      <div key={i} className="rounded-xl p-3"
                        style={{ background: "#FFFFFF", boxShadow: "var(--pk-shadow-sm)", borderLeft: `3px solid ${r.match === "direct" ? "var(--pk-teal)" : "var(--pk-purple)"}` }}>
                        <div className="flex items-center justify-between mb-1">
                          <span style={{ fontSize: "12px", fontWeight: 700, color: r.match === "direct" ? "var(--pk-teal)" : "var(--pk-purple)" }}>
                            {r.ref}
                          </span>
                          <span className="px-1.5 py-0.5 rounded-full"
                            style={{ fontSize: "9px", fontWeight: 700, color: r.match === "direct" ? "var(--pk-teal)" : "var(--pk-purple)", background: r.match === "direct" ? "rgba(45,212,160,0.12)" : "rgba(167,139,250,0.12)", textTransform: "uppercase", letterSpacing: "0.06em" }}>
                            {r.match}
                          </span>
                        </div>
                        <p style={{ fontSize: "12px", color: "var(--pk-t1)", lineHeight: 1.5 }}>{r.text}</p>
                        <div className="flex gap-2 mt-2.5">
                          <button
                            onClick={() => {
                              const payload: VerseDetectedPayload = {
                                verseRef: r.ref, verseText: r.text, translation,
                                confidence: 1, detectionMs: 0, snippetUsed: "manual search", detectedAt: new Date().toISOString(),
                              };
                              pushToProjector(payload);
                            }}
                            disabled={!isLive}
                            className="flex items-center gap-1 px-2.5 py-1 rounded-lg cursor-pointer"
                            style={{ background: isLive ? "#1D1D1F" : "var(--pk-b1)", color: isLive ? "#FFFFFF" : "var(--pk-t3)", border: "none", fontSize: "10px", fontWeight: 600 }}
                          >
                            <ArrowUpFromLine size={10} /> Push
                          </button>
                          <button
                            onClick={() => {
                              const payload: VerseDetectedPayload = {
                                verseRef: r.ref, verseText: r.text, translation,
                                confidence: 1, detectionMs: 0, snippetUsed: "manual search", detectedAt: new Date().toISOString(),
                              };
                              addToQueue(payload);
                            }}
                            className="flex items-center gap-1 px-2.5 py-1 rounded-lg cursor-pointer"
                            style={{ background: "var(--pk-deep)", border: "0.5px solid var(--pk-b1)", fontSize: "10px", fontWeight: 600, color: "var(--pk-t2)" }}
                          >
                            <Plus size={10} /> Queue
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {!isSearching && searchQuery && searchResults.length === 0 && (
                  <div className="text-center py-8">
                    <p style={{ fontSize: "13px", color: "var(--pk-t3)" }}>No results found. Try a different query.</p>
                  </div>
                )}
              </>
            )}

            {/* ── THEME tab ── */}
            {rightTab === "theme" && (
              <>
                <p style={{ fontSize: "15px", fontWeight: 600, color: "var(--pk-t1)", letterSpacing: "-0.003em", marginBottom: "4px" }}>
                  Projector Theme Designer
                </p>
                <p style={{ fontSize: "12px", color: "var(--pk-t3)", marginBottom: "20px" }}>
                  Changes broadcast live to all connected projector screens.
                </p>

                <div className="flex flex-col gap-5">
                  {/* Background color */}
                  <div>
                    <p style={{ fontSize: "11px", fontWeight: 700, color: "var(--pk-t2)", textTransform: "uppercase", letterSpacing: "0.06em", marginBottom: "8px" }}>
                      Background Color
                    </p>
                    <div className="flex items-center gap-3">
                      <input
                        type="color"
                        value={theme.background}
                        onChange={(e) => updateTheme({ background: e.target.value })}
                        className="rounded-lg cursor-pointer"
                        style={{ width: "40px", height: "32px", border: "0.5px solid var(--pk-b1)", padding: "2px" }}
                      />
                      <div className="flex gap-2">
                        {["#030210", "#000000", "#1a0533", "#003366", "#0d2818"].map((c) => (
                          <button
                            key={c}
                            onClick={() => updateTheme({ background: c })}
                            className="rounded-lg cursor-pointer"
                            style={{
                              width: "28px", height: "28px", background: c,
                              border: theme.background === c ? "2px solid var(--pk-gold)" : "1px solid var(--pk-b1)",
                            }}
                          />
                        ))}
                      </div>
                    </div>
                  </div>

                  {/* Text color */}
                  <div>
                    <p style={{ fontSize: "11px", fontWeight: 700, color: "var(--pk-t2)", textTransform: "uppercase", letterSpacing: "0.06em", marginBottom: "8px" }}>
                      Verse Text Color
                    </p>
                    <div className="flex items-center gap-3">
                      <input
                        type="color"
                        value={theme.textColor}
                        onChange={(e) => updateTheme({ textColor: e.target.value })}
                        className="rounded-lg cursor-pointer"
                        style={{ width: "40px", height: "32px", border: "0.5px solid var(--pk-b1)", padding: "2px" }}
                      />
                      <div className="flex gap-2">
                        {["#FFFFFF", "#F5F5DC", "#E8D5B7", "#D4E8FF", "#E8FFD4"].map((c) => (
                          <button
                            key={c}
                            onClick={() => updateTheme({ textColor: c })}
                            className="rounded-lg cursor-pointer"
                            style={{
                              width: "28px", height: "28px", background: c,
                              border: theme.textColor === c ? "2px solid var(--pk-gold)" : "1px solid var(--pk-b1)",
                            }}
                          />
                        ))}
                      </div>
                    </div>
                  </div>

                  {/* Reference color */}
                  <div>
                    <p style={{ fontSize: "11px", fontWeight: 700, color: "var(--pk-t2)", textTransform: "uppercase", letterSpacing: "0.06em", marginBottom: "8px" }}>
                      Reference Color
                    </p>
                    <div className="flex items-center gap-3">
                      <input
                        type="color"
                        value={theme.refColor}
                        onChange={(e) => updateTheme({ refColor: e.target.value })}
                        className="rounded-lg cursor-pointer"
                        style={{ width: "40px", height: "32px", border: "0.5px solid var(--pk-b1)", padding: "2px" }}
                      />
                      <div className="flex gap-2">
                        {["#B07C1F", "#FFD700", "#60A5FA", "#34D399", "#F87171"].map((c) => (
                          <button
                            key={c}
                            onClick={() => updateTheme({ refColor: c })}
                            className="rounded-lg cursor-pointer"
                            style={{
                              width: "28px", height: "28px", background: c,
                              border: theme.refColor === c ? "2px solid var(--pk-t1)" : "1px solid var(--pk-b1)",
                            }}
                          />
                        ))}
                      </div>
                    </div>
                  </div>

                  {/* Font scale */}
                  <div>
                    <p style={{ fontSize: "11px", fontWeight: 700, color: "var(--pk-t2)", textTransform: "uppercase", letterSpacing: "0.06em", marginBottom: "8px" }}>
                      Font Scale
                    </p>
                    <div className="flex gap-2">
                      {(["sm", "md", "lg", "xl"] as ProjectorTheme["scale"][]).map((s) => (
                        <button
                          key={s}
                          onClick={() => updateTheme({ scale: s })}
                          className="flex-1 py-2 rounded-lg cursor-pointer"
                          style={{
                            background: theme.scale === s ? "#1D1D1F" : "var(--pk-deep)",
                            color: theme.scale === s ? "#FFFFFF" : "var(--pk-t2)",
                            border: "0.5px solid var(--pk-b1)",
                            fontSize: "11px", fontWeight: 600,
                          }}
                        >
                          {s.toUpperCase()}
                        </button>
                      ))}
                    </div>
                  </div>

                  {/* Animation style */}
                  <div>
                    <p style={{ fontSize: "11px", fontWeight: 700, color: "var(--pk-t2)", textTransform: "uppercase", letterSpacing: "0.06em", marginBottom: "8px" }}>
                      Transition Animation
                    </p>
                    <div className="flex gap-2">
                      {(["fade", "slide", "zoom"] as ProjectorTheme["animation"][]).map((a) => (
                        <button
                          key={a}
                          onClick={() => updateTheme({ animation: a })}
                          className="flex-1 py-2 rounded-lg cursor-pointer capitalize"
                          style={{
                            background: theme.animation === a ? "#1D1D1F" : "var(--pk-deep)",
                            color: theme.animation === a ? "#FFFFFF" : "var(--pk-t2)",
                            border: "0.5px solid var(--pk-b1)",
                            fontSize: "11px", fontWeight: 600,
                          }}
                        >
                          {a}
                        </button>
                      ))}
                    </div>
                  </div>

                  {/* Reset button */}
                  <button
                    onClick={() => updateTheme(DEFAULT_THEME)}
                    className="flex items-center justify-center gap-2 py-2.5 rounded-xl cursor-pointer"
                    style={{ background: "var(--pk-deep)", border: "0.5px solid var(--pk-b1)", fontSize: "12px", color: "var(--pk-t3)", fontWeight: 600 }}
                  >
                    <RefreshCw size={12} /> Reset to Default
                  </button>

                  {/* Live preview swatch */}
                  <div className="rounded-2xl overflow-hidden" style={{ border: "0.5px solid var(--pk-b1)" }}>
                    <p style={{ fontSize: "10px", fontWeight: 700, color: "var(--pk-t3)", textTransform: "uppercase", letterSpacing: "0.06em", padding: "8px 12px", borderBottom: "0.5px solid var(--pk-b1)" }}>
                      Preview
                    </p>
                    <div className="flex flex-col items-center justify-center p-6"
                      style={{ background: theme.background, minHeight: "100px" }}>
                      <p style={{ color: theme.textColor, fontSize: "13px", fontStyle: "italic", textAlign: "center", lineHeight: 1.5 }}>
                        &ldquo;For God so loved the world that he gave his one and only Son…&rdquo;
                      </p>
                      <p style={{ color: theme.refColor, fontSize: "11px", fontWeight: 700, marginTop: "8px" }}>
                        John 3:16 · {translation}
                      </p>
                    </div>
                  </div>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
