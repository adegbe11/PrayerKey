"use client";

import { useEffect, useRef, useState, type CSSProperties } from "react";
import { createPortal } from "react-dom";
import { io, type Socket } from "socket.io-client";
import type {
  ServerToClientEvents,
  ClientToServerEvents,
  VerseDetectedPayload,
  ProjectorTheme,
} from "@/types/sermon";

/** shared with /live */
const PROJECTOR_CHANNEL = "prayerkey-live-projector";

const DEFAULT_THEME: ProjectorTheme = {
  background: "#030210",
  textColor:  "#FFFFFF",
  refColor:   "#B07C1F",
  scale:      "md",
  animation:  "fade",
};

// Font scale multipliers
const SCALE_MAP: Record<ProjectorTheme["scale"], number> = {
  sm: 0.8,
  md: 1,
  lg: 1.3,
  xl: 1.6,
};

// Animation variants per style
function getAnimStyles(
  visible: boolean,
  animation: ProjectorTheme["animation"]
): CSSProperties {
  if (animation === "slide") {
    return {
      opacity:    visible ? 1 : 0,
      transform:  visible ? "translateY(0)" : "translateY(20px)",
      transition: "opacity 400ms ease, transform 400ms ease",
    };
  }
  if (animation === "zoom") {
    return {
      opacity:    visible ? 1 : 0,
      transform:  visible ? "scale(1)" : "scale(0.94)",
      transition: "opacity 350ms ease, transform 350ms ease",
    };
  }
  // default: fade
  return {
    opacity:    visible ? 1 : 0,
    transition: "opacity 300ms ease",
  };
}

type ShownVerse = Pick<VerseDetectedPayload, "verseRef" | "verseText" | "translation">;

interface ProjectorPageProps {
  searchParams: { serviceId?: string };
}

/**
 * The congregation's screen. It shows exactly what the live page has on air:
 * a verse stays until the operator clears it or puts up the next one.
 * Driven from the live page on the same laptop (no server), or, for a church
 * service with an id, from the socket server.
 */
export default function ProjectorPage({ searchParams }: ProjectorPageProps) {
  const serviceId = searchParams.serviceId ?? "";
  const socketRef = useRef<Socket<ServerToClientEvents, ClientToServerEvents> | null>(null);

  const [mounted, setMounted]           = useState(false);
  const [currentVerse, setCurrentVerse] = useState<ShownVerse | null>(null);
  const [visible, setVisible]           = useState(false);
  const [theme, setTheme]               = useState<ProjectorTheme>(DEFAULT_THEME);
  const [linked, setLinked]             = useState(false);
  const [fullscreen, setFullscreen]     = useState(false);
  const [idle, setIdle]                 = useState(false);
  const swapTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const shownRef  = useRef<string | null>(null);

  useEffect(() => { setMounted(true); }, []);

  function show(verse: ShownVerse | null) {
    const key = verse ? `${verse.verseRef}|${verse.verseText}` : null;
    if (key === shownRef.current) return;
    shownRef.current = key;
    if (swapTimer.current) clearTimeout(swapTimer.current);
    setVisible(false);
    swapTimer.current = setTimeout(() => {
      setCurrentVerse(verse);
      setVisible(!!verse);
    }, verse ? 300 : 250);
  }

  // ── From the live page on this laptop ────────────────────────────────
  useEffect(() => {
    if (serviceId || typeof BroadcastChannel === "undefined") return;
    const channel = new BroadcastChannel(PROJECTOR_CHANNEL);
    channel.onmessage = (e) => {
      const msg = e.data as { type?: string; verse?: ShownVerse | null };
      if (msg?.type === "verse") { setLinked(true); show(msg.verse ?? null); }
      if (msg?.type === "bye") { setLinked(false); show(null); }
    };
    // opened after the service started: ask what is on air now
    channel.postMessage({ type: "hello" });
    return () => channel.close();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [serviceId]);

  // ── From the socket server, for a church service with an id ──────────
  useEffect(() => {
    if (!serviceId) return;

    const socket: Socket<ServerToClientEvents, ClientToServerEvents> = io(
      process.env.NEXT_PUBLIC_SOCKET_URL ?? "http://localhost:3001",
      { transports: ["websocket", "polling"] }
    );

    socketRef.current = socket;

    socket.on("connect", () => {
      setLinked(true);
      socket.emit("service:join", serviceId);
    });
    socket.on("disconnect", () => setLinked(false));
    socket.on("verse:detected", (payload) => show(payload));
    socket.on("verse:display", (payload) => show(payload));
    socket.on("verse:suggested", (payload) => {
      if (payload.confidence >= 0.70) show(payload);
    });
    socket.on("projector:theme", (incoming: ProjectorTheme) => {
      setTheme(incoming);
    });

    return () => {
      socket.disconnect();
    };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [serviceId]);

  // full screen on click; the pointer hides itself while the screen is still
  useEffect(() => {
    const onFs = () => setFullscreen(!!document.fullscreenElement);
    document.addEventListener("fullscreenchange", onFs);
    let t: ReturnType<typeof setTimeout>;
    const wake = () => { setIdle(false); clearTimeout(t); t = setTimeout(() => setIdle(true), 2500); };
    window.addEventListener("mousemove", wake);
    wake();
    return () => { document.removeEventListener("fullscreenchange", onFs); window.removeEventListener("mousemove", wake); clearTimeout(t); };
  }, []);
  function toggleFullscreen() {
    if (document.fullscreenElement) document.exitFullscreen?.();
    else document.documentElement.requestFullscreen?.().catch(() => {});
  }

  const scale = SCALE_MAP[theme.scale];
  if (!mounted) return null;

  return createPortal(
    <div
      id="pk-projector"
      onClick={toggleFullscreen}
      style={{
        position:       "fixed",
        inset:          0,
        zIndex:         2147483647,
        background:     theme.background,
        display:        "flex",
        flexDirection:  "column",
        alignItems:     "center",
        justifyContent: "center",
        overflow:       "hidden",
        fontFamily:     "-apple-system, 'SF Pro Display', sans-serif",
        userSelect:     "none",
        cursor:         idle ? "none" : "default",
        transition:     "background 600ms ease",
      }}
    >
      {/* the site's banner, navigation and footer never reach the projector */}
      <style>{`
        html, body { overflow: hidden !important; background: ${theme.background} !important; }
        body > *:not(#pk-projector) { display: none !important; }
        @keyframes pkPulse { 0%,100% { opacity: 1; } 50% { opacity: 0.3; } }
      `}</style>

      {/* Gold cross watermark */}
      <div
        aria-hidden
        style={{
          position:       "absolute",
          inset:          0,
          display:        "flex",
          alignItems:     "center",
          justifyContent: "center",
          pointerEvents:  "none",
          opacity:        0.04,
        }}
      >
        <CrossWatermark color={theme.refColor} />
      </div>

      {/* Ambient radial glow tinted to refColor */}
      <div
        aria-hidden
        style={{
          position:      "absolute",
          inset:         0,
          background:    `radial-gradient(ellipse 70% 50% at 50% 50%, ${theme.refColor}10 0%, transparent 70%)`,
          pointerEvents: "none",
          transition:    "background 600ms ease",
        }}
      />

      {/* Verse display */}
      <div
        aria-live="polite"
        style={{
          position:  "relative",
          maxWidth:  "1100px",
          width:     "100%",
          padding:   "0 6vw",
          textAlign: "center",
          ...getAnimStyles(visible, theme.animation),
        }}
      >
        {currentVerse && (
          <>
            <p
              style={{
                fontSize:      `clamp(${22 * scale}px, ${3.6 * scale}vw, ${64 * scale}px)`,
                fontWeight:    400,
                fontStyle:     "italic",
                color:         theme.textColor,
                lineHeight:    1.45,
                letterSpacing: "0.01em",
                margin:        "0 0 0.9em",
                transition:    "color 400ms ease, font-size 300ms ease",
              }}
            >
              &ldquo;{currentVerse.verseText}&rdquo;
            </p>

            <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "14px" }}>
              <span
                style={{
                  fontSize:      `clamp(${16 * scale}px, ${2 * scale}vw, ${34 * scale}px)`,
                  fontWeight:    600,
                  color:         theme.refColor,
                  letterSpacing: "0.04em",
                  transition:    "color 400ms ease",
                }}
              >
                {currentVerse.verseRef}
              </span>
              <span
                style={{
                  width:        "5px",
                  height:       "5px",
                  borderRadius: "50%",
                  background:   `${theme.refColor}80`,
                }}
              />
              <span
                style={{
                  fontSize:      `clamp(${12 * scale}px, ${1.4 * scale}vw, ${22 * scale}px)`,
                  color:         `${theme.textColor}70`,
                  letterSpacing: "0.06em",
                  textTransform: "uppercase",
                }}
              >
                {currentVerse.translation}
              </span>
            </div>
          </>
        )}
      </div>

      {/* Before the live page is open, say what to do; once linked, an empty screen stays empty */}
      {!linked && !currentVerse && (
        <div style={{ position: "absolute", textAlign: "center", color: `${theme.textColor}66`, fontSize: "16px", lineHeight: 1.6, padding: "0 24px" }}>
          <p style={{ margin: 0, color: `${theme.textColor}b3`, fontSize: "18px" }}>Waiting for the live page</p>
          <p style={{ margin: "6px 0 0" }}>Open prayerkey.com/live in this browser and start the service.</p>
        </div>
      )}

      {/* Corner hints for whoever sets up the screen; they fade with the pointer */}
      <div
        style={{
          position:   "absolute",
          bottom:     "18px",
          left:       0,
          right:      0,
          display:    "flex",
          justifyContent: "center",
          gap:        "18px",
          fontSize:   "12px",
          color:      `${theme.textColor}59`,
          opacity:    idle ? 0 : 1,
          transition: "opacity 400ms ease",
          pointerEvents: "none",
        }}
      >
        <span style={{ display: "inline-flex", alignItems: "center", gap: "6px" }}>
          <span style={{ width: "7px", height: "7px", borderRadius: "50%", background: linked ? "#34C759" : "#FF9F0A", animation: linked ? "none" : "pkPulse 1.5s ease infinite" }} />
          {linked ? "Connected to the live page" : "Not connected"}
        </span>
        {!fullscreen && <span>Click anywhere for full screen</span>}
      </div>
    </div>,
    document.body,
  );
}

// ── Cross watermark SVG ────────────────────────────────────────────────────
function CrossWatermark({ color }: { color: string }) {
  return (
    <svg width="480" height="480" viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect x="44" y="10" width="12" height="80" rx="4" fill={color} />
      <rect x="15" y="30" width="70" height="12" rx="4" fill={color} />
    </svg>
  );
}
