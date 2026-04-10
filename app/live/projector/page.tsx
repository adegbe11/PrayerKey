"use client";

import { useEffect, useRef, useState, type CSSProperties } from "react";
import { io, type Socket } from "socket.io-client";
import type {
  ServerToClientEvents,
  ClientToServerEvents,
  VerseDetectedPayload,
  ProjectorTheme,
} from "@/types/sermon";

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

interface ProjectorPageProps {
  searchParams: { serviceId?: string };
}

export default function ProjectorPage({ searchParams }: ProjectorPageProps) {
  const serviceId = searchParams.serviceId ?? "";
  const socketRef = useRef<Socket<ServerToClientEvents, ClientToServerEvents> | null>(null);

  const [currentVerse, setCurrentVerse] = useState<VerseDetectedPayload | null>(null);
  const [visible, setVisible]           = useState(false);
  const [transcript, setTranscript]     = useState("");
  const [theme, setTheme]               = useState<ProjectorTheme>(DEFAULT_THEME);
  const fadeTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  // ── Socket connection ──────────────────────────────────────────────────
  useEffect(() => {
    if (!serviceId) return;

    const socket: Socket<ServerToClientEvents, ClientToServerEvents> = io(
      process.env.NEXT_PUBLIC_SOCKET_URL ?? "http://localhost:3001",
      { transports: ["websocket", "polling"] }
    );

    socketRef.current = socket;

    socket.on("connect", () => {
      socket.emit("service:join", serviceId);
    });

    // Auto-detected verse
    socket.on("verse:detected", (payload) => showVerse(payload));

    // Manual push from pastor's queue
    socket.on("verse:display", (payload) => showVerse(payload));

    // Suggested verse — only if confidence is high enough
    socket.on("verse:suggested", (payload) => {
      if (payload.confidence >= 0.70) showVerse(payload);
    });

    socket.on("transcript:update", ({ text, isFinal }) => {
      if (isFinal) setTranscript(text);
    });

    // Live theme updates from pastor's theme designer
    socket.on("projector:theme", (incoming: ProjectorTheme) => {
      setTheme(incoming);
    });

    return () => {
      socket.disconnect();
    };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [serviceId]);

  function showVerse(payload: VerseDetectedPayload) {
    if (fadeTimer.current) clearTimeout(fadeTimer.current);

    setVisible(false);
    setTimeout(() => {
      setCurrentVerse(payload);
      setVisible(true);
      // Auto-hide after 15 seconds
      fadeTimer.current = setTimeout(() => setVisible(false), 15_000);
    }, 300);
  }

  const scale = SCALE_MAP[theme.scale];

  return (
    <div
      style={{
        position:       "fixed",
        inset:          0,
        background:     theme.background,
        display:        "flex",
        flexDirection:  "column",
        alignItems:     "center",
        justifyContent: "center",
        overflow:       "hidden",
        fontFamily:     "-apple-system, 'SF Pro Display', sans-serif",
        userSelect:     "none",
        transition:     "background 600ms ease",
      }}
    >
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
        style={{
          position:  "relative",
          maxWidth:  "860px",
          width:     "100%",
          padding:   "0 48px",
          textAlign: "center",
          ...getAnimStyles(visible, theme.animation),
        }}
      >
        {currentVerse && (
          <>
            {/* Verse text */}
            <p
              style={{
                fontSize:      `clamp(${18 * scale}px, ${3.2 * scale}vw, ${36 * scale}px)`,
                fontWeight:    400,
                fontStyle:     "italic",
                color:         theme.textColor,
                lineHeight:    1.55,
                letterSpacing: "0.01em",
                marginBottom:  "28px",
                transition:    "color 400ms ease, font-size 300ms ease",
              }}
            >
              &ldquo;{currentVerse.verseText}&rdquo;
            </p>

            {/* Reference + translation */}
            <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "12px" }}>
              <span
                style={{
                  fontSize:      `clamp(${14 * scale}px, ${1.8 * scale}vw, ${22 * scale}px)`,
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
                  width:        "4px",
                  height:       "4px",
                  borderRadius: "50%",
                  background:   `${theme.refColor}80`,
                }}
              />
              <span
                style={{
                  fontSize:      `clamp(${11 * scale}px, ${1.4 * scale}vw, ${16 * scale}px)`,
                  color:         `${theme.textColor}70`,
                  letterSpacing: "0.06em",
                  textTransform: "uppercase",
                }}
              >
                {currentVerse.translation}
              </span>
            </div>

            {/* Confidence bar */}
            <div style={{ marginTop: "32px", display: "flex", alignItems: "center", justifyContent: "center", gap: "8px" }}>
              <div
                style={{
                  width: "120px", height: "2px",
                  background: `${theme.textColor}14`,
                  borderRadius: "2px", overflow: "hidden",
                }}
              >
                <div
                  style={{
                    height: "100%",
                    width:  `${Math.round(currentVerse.confidence * 100)}%`,
                    background: currentVerse.confidence >= 0.90
                      ? "#34C759"
                      : currentVerse.confidence >= 0.75
                      ? theme.refColor
                      : "#FF9F0A",
                    transition: "width 600ms ease",
                  }}
                />
              </div>
              <span style={{ fontSize: "11px", color: `${theme.textColor}40` }}>
                {Math.round(currentVerse.confidence * 100)}%
              </span>
            </div>
          </>
        )}
      </div>

      {/* No verse state */}
      {!currentVerse && (
        <div style={{ textAlign: "center", opacity: 0.2 }}>
          <div
            style={{
              width: "64px", height: "64px", borderRadius: "50%",
              border: `1.5px solid ${theme.refColor}60`,
              display: "flex", alignItems: "center", justifyContent: "center",
              margin: "0 auto 16px",
            }}
          >
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
              <path d="M12 2L13.5 8.5L20 10L13.5 11.5L12 18L10.5 11.5L4 10L10.5 8.5L12 2Z" fill={theme.refColor} />
            </svg>
          </div>
          <p style={{ color: `${theme.textColor}80`, fontSize: "15px" }}>
            Waiting for verses&hellip;
          </p>
        </div>
      )}

      {/* Live transcript ticker */}
      {transcript && (
        <div style={{ position: "absolute", bottom: "32px", left: "48px", right: "48px", textAlign: "center" }}>
          <p
            style={{
              fontSize:     "13px",
              color:        `${theme.textColor}30`,
              fontStyle:    "italic",
              overflow:     "hidden",
              textOverflow: "ellipsis",
              whiteSpace:   "nowrap",
              maxWidth:     "100%",
            }}
          >
            {transcript}
          </p>
        </div>
      )}

      {/* LIVE indicator */}
      {serviceId && (
        <div
          style={{
            position:     "absolute",
            top:          "20px",
            right:        "24px",
            display:      "flex",
            alignItems:   "center",
            gap:          "6px",
            background:   "rgba(255,59,48,0.12)",
            border:       "0.5px solid rgba(255,59,48,0.3)",
            borderRadius: "100px",
            padding:      "4px 10px",
          }}
        >
          <span
            style={{
              width: "6px", height: "6px", borderRadius: "50%",
              background: "#FF3B30",
              animation: "livePulse 1.5s ease infinite",
            }}
          />
          <span style={{ fontSize: "11px", color: "#FF3B30", letterSpacing: "0.05em", fontWeight: 600 }}>
            LIVE
          </span>
        </div>
      )}

      <style>{`
        @keyframes livePulse {
          0%,100% { opacity: 1; }
          50%      { opacity: 0.3; }
        }
      `}</style>
    </div>
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
