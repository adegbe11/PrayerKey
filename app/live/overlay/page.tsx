"use client";

import { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";

/**
 * The stream overlay: add this page to OBS, vMix or Streamlabs as a Browser
 * Source (1920×1080). The background is see-through; the verse on air in the
 * church's live page appears as a lower third across the bottom of the video.
 *
 *   /live/overlay?room=K7Q2XP              the church's own room
 *   /live/overlay?room=K7Q2XP&preview=1    a sample verse, for lining it up
 */

type Verse = { verseRef: string; verseText: string; translation: string };

const SAMPLE: Verse = {
  verseRef: "Psalms 46:10",
  verseText: "Be still, and know that I am God: I will be exalted among the heathen, I will be exalted in the earth.",
  translation: "KJV",
};
const POLL_MS = 800;

// smaller type for longer verses, so the whole verse always fits the strip
function verseSize(text: string) {
  const n = text.length;
  return n > 520 ? 1.2 : n > 380 ? 1.4 : n > 260 ? 1.65 : n > 160 ? 1.9 : 2.2; // vw
}

export default function OverlayPage({ searchParams }: { searchParams: { room?: string; preview?: string } }) {
  const room = (searchParams.room ?? "").toUpperCase();
  const preview = searchParams.preview === "1";
  const [mounted, setMounted] = useState(false);
  const [shown, setShown] = useState<Verse | null>(preview ? SAMPLE : null);
  const [visible, setVisible] = useState(preview);
  const seqRef = useRef(-1);
  const swap = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => { setMounted(true); }, []);

  useEffect(() => {
    if (preview || !/^[A-Z2-9]{6}$/.test(room)) return;
    let stopped = false;
    let timer: ReturnType<typeof setTimeout>;
    const tick = async () => {
      try {
        const res = await fetch(`/api/live/onair?room=${room}`, { cache: "no-store" });
        if (res.ok) {
          const data = (await res.json()) as { verse: Verse | null; seq: number };
          if (data.seq !== seqRef.current) {
            seqRef.current = data.seq;
            // slide the old verse out, then the new one in
            if (swap.current) clearTimeout(swap.current);
            setVisible(false);
            swap.current = setTimeout(() => {
              setShown(data.verse);
              setVisible(!!data.verse);
            }, data.verse ? 450 : 0);
          }
        }
      } catch { /* a dropped request: keep what is showing and try again */ }
      if (!stopped) timer = setTimeout(tick, POLL_MS);
    };
    tick();
    return () => { stopped = true; clearTimeout(timer); };
  }, [room, preview]);

  if (!mounted) return null;

  return createPortal(
    <div id="pk-overlay" style={{ position: "fixed", inset: 0, zIndex: 2147483647, pointerEvents: "none", fontFamily: "'Inter', -apple-system, 'Segoe UI', sans-serif" }}>
      {/* see-through for OBS: nothing of the website may show behind the strip */}
      <style>{`
        html, body { background: transparent !important; background-image: none !important; overflow: hidden !important; }
        body > *:not(#pk-overlay) { display: none !important; }
        body::before, body::after, html::before, html::after { display: none !important; }
      `}</style>

      {shown && (
        <div
          style={{
            position: "absolute",
            left: "5vw",
            right: "5vw",
            bottom: "6.5vh",
            display: "flex",
            alignItems: "stretch",
            borderRadius: "0.9vw",
            overflow: "hidden",
            background: "rgba(14, 8, 32, 0.88)",
            boxShadow: "0 0.6vw 2.4vw rgba(0,0,0,0.45)",
            transform: visible ? "translateY(0)" : "translateY(140%)",
            opacity: visible ? 1 : 0,
            transition: "transform 450ms cubic-bezier(0.2, 0.8, 0.2, 1), opacity 350ms ease",
          }}
        >
          {/* reference block */}
          <div
            style={{
              flex: "0 0 auto",
              minWidth: "14vw",
              padding: "1.4vw 1.8vw",
              background: "linear-gradient(160deg, #5B2BD6, #3A1494)",
              display: "flex",
              flexDirection: "column",
              justifyContent: "center",
              gap: "0.35vw",
            }}
          >
            <span style={{ color: "#F4D03F", fontWeight: 800, fontSize: "1.75vw", letterSpacing: "0.01em", lineHeight: 1.1, whiteSpace: "nowrap" }}>
              {shown.verseRef}
            </span>
            <span style={{ color: "rgba(255,255,255,0.7)", fontWeight: 600, fontSize: "0.85vw", letterSpacing: "0.14em", textTransform: "uppercase" }}>
              {shown.translation}
            </span>
          </div>
          {/* the verse */}
          <p
            style={{
              margin: 0,
              flex: 1,
              padding: "1.3vw 2vw",
              color: "#FFFFFF",
              fontSize: `${verseSize(shown.verseText)}vw`,
              lineHeight: 1.32,
              fontWeight: 500,
              display: "flex",
              alignItems: "center",
              textShadow: "0 0.1vw 0.3vw rgba(0,0,0,0.35)",
            }}
          >
            {shown.verseText}
          </p>
        </div>
      )}
    </div>,
    document.body,
  );
}
