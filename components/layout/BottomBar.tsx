"use client";

import { useState, useEffect } from "react";
import { Eye, Square } from "lucide-react";

function WaveBar({ delay }: { delay: string }) {
  return (
    <div
      className="w-[3px] rounded-sm"
      style={{
        background: "var(--pk-gold)",
        height: "16px",
        animation: `wave 1.2s cubic-bezier(.4,0,.6,1) ${delay} infinite`,
        transformOrigin: "bottom",
      }}
    />
  );
}

function LiveTimer() {
  const [seconds, setSeconds] = useState(0);

  useEffect(() => {
    const id = setInterval(() => setSeconds((s) => s + 1), 1000);
    return () => clearInterval(id);
  }, []);

  const m = Math.floor(seconds / 60).toString().padStart(2, "0");
  const s = (seconds % 60).toString().padStart(2, "0");

  return (
    <span
      style={{
        fontSize: "12px",
        color: "var(--pk-t3)",
        fontVariantNumeric: "tabular-nums",
        letterSpacing: "0.02em",
      }}
    >
      {m}:{s}
    </span>
  );
}

export function BottomBar() {
  return (
    <footer
      className="flex items-center gap-3 px-4 flex-shrink-0"
      style={{
        height: "56px",
        background: "var(--pk-nav-bg)",
        backdropFilter: "var(--pk-nav-blur)",
        WebkitBackdropFilter: "var(--pk-nav-blur)",
        borderTop: "0.5px solid var(--pk-b1)",
        position: "relative",
        zIndex: 100,
      }}
    >
      {/* Gold cross art */}
      <div
        className="flex items-center justify-center rounded-xl flex-shrink-0"
        style={{
          width: "36px",
          height: "36px",
          background: "var(--pk-gold-dim)",
          border: "0.5px solid var(--pk-gold-border)",
          boxShadow: "0 1px 2px rgba(0,0,0,0.06)",
        }}
      >
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <rect x="6.5" y="1" width="3" height="14" rx="1.5" fill="var(--pk-gold)" />
          <rect x="1" y="5.5" width="14" height="3" rx="1.5" fill="var(--pk-gold)" />
        </svg>
      </div>

      {/* Sermon info */}
      <div className="flex-1 min-w-0">
        <p
          className="truncate"
          style={{
            fontSize: "13px",
            fontWeight: 600,
            color: "var(--pk-t1)",
            letterSpacing: "-0.003em",
          }}
        >
          The Power of Faith in Adversity
        </p>
        <p className="truncate" style={{ fontSize: "11px", color: "var(--pk-t3)" }}>
          Romans 8:28 · Grace Community Church
        </p>
      </div>

      {/* Animated wave bars */}
      <div className="flex items-end gap-0.5 flex-shrink-0" style={{ height: "20px" }}>
        <WaveBar delay="0s" />
        <WaveBar delay="0.2s" />
        <WaveBar delay="0.4s" />
        <WaveBar delay="0.6s" />
        <WaveBar delay="0.8s" />
      </div>

      {/* LIVE chip */}
      <div
        className="flex items-center gap-1 px-2 py-0.5 rounded-full flex-shrink-0"
        style={{
          background: "rgba(255,59,48,0.08)",
          border: "0.5px solid rgba(255,59,48,0.2)",
        }}
      >
        <span
          className="w-1.5 h-1.5 rounded-full animate-live-pulse"
          style={{ background: "var(--pk-live)" }}
        />
        <span
          style={{
            fontSize: "10px",
            fontWeight: 700,
            color: "var(--pk-live)",
            letterSpacing: "0.06em",
          }}
        >
          LIVE
        </span>
      </div>

      {/* Timer */}
      <LiveTimer />

      {/* View Live — Apple gold CTA */}
      <button
        className="flex items-center gap-1.5 px-3 py-1.5 rounded-full flex-shrink-0 cursor-pointer"
        style={{
          background: "var(--pk-gold)",
          color: "#FFFFFF",
          fontSize: "12px",
          fontWeight: 600,
          border: "none",
          transition: "opacity var(--pk-duration) var(--pk-ease)",
          letterSpacing: "-0.003em",
        }}
        onMouseEnter={(e) => { e.currentTarget.style.opacity = "0.82"; }}
        onMouseLeave={(e) => { e.currentTarget.style.opacity = "1"; }}
      >
        <Eye size={11} strokeWidth={2} />
        View Live
      </button>

      {/* End Service — ghost destructive */}
      <button
        className="flex items-center gap-1.5 px-3 py-1.5 rounded-full flex-shrink-0 cursor-pointer"
        style={{
          background: "rgba(255,59,48,0.08)",
          color: "var(--pk-live)",
          fontSize: "12px",
          fontWeight: 500,
          border: "0.5px solid rgba(255,59,48,0.2)",
          transition: "background var(--pk-duration) var(--pk-ease)",
        }}
        onMouseEnter={(e) => { e.currentTarget.style.background = "rgba(255,59,48,0.14)"; }}
        onMouseLeave={(e) => { e.currentTarget.style.background = "rgba(255,59,48,0.08)"; }}
      >
        <Square size={9} fill="currentColor" strokeWidth={0} />
        End Service
      </button>
    </footer>
  );
}
