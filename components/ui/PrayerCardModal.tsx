"use client";
import { useRef, useState } from "react";

interface PrayerData {
  title:         string;
  prayer:        string;
  encouragement: string;
  verses:        { ref: string; text: string }[];
}

interface Props {
  prayer:  PrayerData;
  onClose: () => void;
}

/* ── Ornate corner SVG ─────────────────────────────────────────────── */
function Corner({ rotate = 0 }: { rotate?: number }) {
  return (
    <svg
      width="72" height="72" viewBox="0 0 72 72" fill="none"
      style={{ transform: `rotate(${rotate}deg)`, display: "block" }}
    >
      {/* Frame lines */}
      <path d="M6 6 L6 66"   stroke="#B07C1F" strokeWidth="1.2"/>
      <path d="M6 6 L66 6"   stroke="#B07C1F" strokeWidth="1.2"/>
      <path d="M14 6 L14 14" stroke="#B07C1F" strokeWidth="0.8" opacity="0.5"/>
      <path d="M6 14 L14 14" stroke="#B07C1F" strokeWidth="0.8" opacity="0.5"/>
      {/* Corner dot */}
      <circle cx="6" cy="6" r="3.5" fill="#B07C1F"/>
      {/* Vine scroll */}
      <path d="M6 6 C6 30 30 6 38 38" stroke="#B07C1F" strokeWidth="1.4" fill="none" strokeLinecap="round"/>
      <path d="M6 22 Q18 10 22 6"     stroke="#B07C1F" strokeWidth="1"   fill="none" strokeLinecap="round" opacity="0.7"/>
      <path d="M6 34 Q22 22 34 6"     stroke="#B07C1F" strokeWidth="0.8" fill="none" strokeLinecap="round" opacity="0.5"/>
      {/* Leaves */}
      <ellipse cx="14" cy="14" rx="5.5" ry="2.5" transform="rotate(45 14 14)"  fill="#B07C1F" opacity="0.45"/>
      <ellipse cx="22" cy="10" rx="4"   ry="2"   transform="rotate(20 22 10)"  fill="#B07C1F" opacity="0.3"/>
      <ellipse cx="10" cy="22" rx="4"   ry="2"   transform="rotate(70 10 22)"  fill="#B07C1F" opacity="0.3"/>
      <ellipse cx="28" cy="18" rx="4.5" ry="2"   transform="rotate(35 28 18)"  fill="#B07C1F" opacity="0.25"/>
      {/* Small bud */}
      <circle cx="38" cy="38" r="2.5" fill="#B07C1F" opacity="0.5"/>
      <circle cx="22" cy="6"  r="1.5" fill="#B07C1F" opacity="0.4"/>
      <circle cx="6"  cy="22" r="1.5" fill="#B07C1F" opacity="0.4"/>
    </svg>
  );
}

/* ── Decorative divider ────────────────────────────────────────────── */
function Divider() {
  return (
    <div style={{ display: "flex", alignItems: "center", gap: "10px", margin: "18px auto", width: "60%" }}>
      <div style={{ flex: 1, height: "1px", background: "linear-gradient(to right, transparent, #B07C1F)" }} />
      <span style={{ color: "#B07C1F", fontSize: "14px" }}>✦</span>
      <div style={{ flex: 1, height: "1px", background: "linear-gradient(to left, transparent, #B07C1F)" }} />
    </div>
  );
}

/* ── Main component ────────────────────────────────────────────────── */
export default function PrayerCardModal({ prayer, onClose }: Props) {
  const cardRef = useRef<HTMLDivElement>(null);
  const [busy, setBusy] = useState<"png" | "pdf" | null>(null);

  async function downloadPNG() {
    if (!cardRef.current) return;
    setBusy("png");
    try {
      const { toPng } = await import("html-to-image");
      const dataUrl = await toPng(cardRef.current, {
        pixelRatio: 3,
        backgroundColor: "#ffffff",
        style: { borderRadius: "0" },
      });
      const a = document.createElement("a");
      a.href     = dataUrl;
      a.download = `${prayer.title.replace(/\s+/g, "-").toLowerCase()}-prayerkey.png`;
      a.click();
    } finally {
      setBusy(null);
    }
  }

  function printPDF() {
    setBusy("pdf");
    setTimeout(() => {
      window.print();
      setBusy(null);
    }, 100);
  }

  async function copyText() {
    const full = `${prayer.title}\n\n${prayer.prayer}\n\n${prayer.encouragement}\n\n${prayer.verses.map(v => `${v.ref} — ${v.text}`).join("\n")}`;
    await navigator.clipboard.writeText(full);
  }

  return (
    <>
      {/* ── Print CSS (injected inline so no globals needed) ── */}
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400;0,700;1,400;1,600&family=Lora:ital,wght@0,400;0,500;1,400;1,500&family=Great+Vibes&display=swap');
        @media print {
          body > * { display: none !important; }
          #pk-prayer-print { display: block !important; position: fixed; inset: 0; z-index: 99999; }
          @page { size: A4; margin: 0; }
        }
        @keyframes cardIn {
          from { opacity: 0; transform: scale(0.92) translateY(24px); }
          to   { opacity: 1; transform: scale(1) translateY(0); }
        }
        #pk-prayer-print { display: none; }
      `}</style>

      {/* ── Backdrop ── */}
      <div
        onClick={onClose}
        style={{
          position:   "fixed", inset: 0,
          background: "rgba(0,0,0,0.82)",
          backdropFilter: "blur(6px)",
          zIndex:     1000,
          display:    "flex",
          alignItems: "center",
          justifyContent: "center",
          padding:    "20px",
          overflowY:  "auto",
        }}
      >
        <div onClick={e => e.stopPropagation()} style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: "16px", maxHeight: "100vh", overflowY: "auto" }}>

          {/* ── Action buttons ── */}
          <div style={{ display: "flex", gap: "10px", flexWrap: "wrap", justifyContent: "center" }}>
            {[
              { label: busy === "png" ? "Saving…" : "⬇ Download PNG", action: downloadPNG, color: "#B07C1F" },
              { label: busy === "pdf" ? "Opening…" : "🖨 Download PDF", action: printPDF,    color: "#5856D6" },
              { label: "📋 Copy Text", action: copyText, color: "#34C759" },
              { label: "✕ Close",     action: onClose,   color: "rgba(255,255,255,0.3)" },
            ].map(btn => (
              <button
                key={btn.label}
                onClick={btn.action}
                style={{
                  padding:      "10px 22px",
                  borderRadius: "6px",
                  border:       `1.5px solid ${btn.color}`,
                  background:   "rgba(255,255,255,0.06)",
                  color:        btn.color,
                  fontSize:     "13px",
                  fontWeight:   700,
                  cursor:       "pointer",
                  backdropFilter: "blur(4px)",
                  transition:   "all 150ms",
                  letterSpacing: "0.02em",
                }}
              >
                {btn.label}
              </button>
            ))}
          </div>

          {/* ── Prayer Card ── */}
          <div
            ref={cardRef}
            style={{
              width:       "595px",
              minHeight:   "842px",
              background:  "#ffffff",
              position:    "relative",
              padding:     "60px 64px 56px",
              boxSizing:   "border-box",
              fontFamily:  "'Lora', Georgia, serif",
              animation:   "cardIn 420ms cubic-bezier(0.22,1,0.36,1)",
              boxShadow:   "0 32px 80px rgba(0,0,0,0.5)",
              flexShrink:  0,
            }}
          >
            {/* Outer border */}
            <div style={{
              position:     "absolute", inset: "12px",
              border:       "1.5px solid #B07C1F",
              pointerEvents:"none",
            }} />
            {/* Inner border */}
            <div style={{
              position:     "absolute", inset: "18px",
              border:       "0.5px solid rgba(176,124,31,0.35)",
              pointerEvents:"none",
            }} />

            {/* Corner ornaments */}
            <div style={{ position: "absolute", top: "8px",  left: "8px"  }}><Corner rotate={0}   /></div>
            <div style={{ position: "absolute", top: "8px",  right: "8px" }}><Corner rotate={90}  /></div>
            <div style={{ position: "absolute", bottom: "8px", right: "8px" }}><Corner rotate={180} /></div>
            <div style={{ position: "absolute", bottom: "8px", left: "8px"  }}><Corner rotate={270} /></div>

            {/* Content */}
            <div style={{ position: "relative", zIndex: 1, textAlign: "center" }}>

              {/* Praying hands */}
              <div style={{ fontSize: "52px", lineHeight: 1, marginBottom: "6px" }}>🙏</div>

              {/* Subtitle tag */}
              <p style={{
                fontSize:      "9px",
                fontWeight:    700,
                letterSpacing: "0.18em",
                textTransform: "uppercase",
                color:         "#B07C1F",
                margin:        "0 0 14px",
                fontFamily:    "Arial, sans-serif",
              }}>
                PrayerKey · AI Generated Prayer
              </p>

              {/* Title */}
              <h1 style={{
                fontFamily:    "'Great Vibes', 'Playfair Display', Georgia, serif",
                fontSize:      "42px",
                fontWeight:    400,
                color:         "#1a1a1a",
                margin:        "0 0 4px",
                lineHeight:    1.2,
              }}>
                {prayer.title}
              </h1>

              <Divider />

              {/* Prayer text */}
              <p style={{
                fontFamily:  "'Lora', Georgia, serif",
                fontSize:    "14.5px",
                lineHeight:  2,
                color:       "#2a2a2a",
                fontStyle:   "italic",
                margin:      "0 0 4px",
                whiteSpace:  "pre-wrap",
                textAlign:   "center",
              }}>
                {prayer.prayer}
              </p>

              <Divider />

              {/* Encouragement */}
              <p style={{
                fontFamily:  "'Lora', Georgia, serif",
                fontSize:    "12.5px",
                color:       "#6b5b2e",
                fontStyle:   "italic",
                lineHeight:  1.8,
                margin:      "0 auto 0",
                maxWidth:    "88%",
              }}>
                "{prayer.encouragement}"
              </p>

              {/* Scripture */}
              {prayer.verses?.length > 0 && (
                <div style={{ marginTop: "20px", textAlign: "left" }}>
                  <p style={{
                    fontSize:      "8px",
                    fontWeight:    700,
                    letterSpacing: "0.16em",
                    textTransform: "uppercase",
                    color:         "#B07C1F",
                    margin:        "0 0 10px",
                    fontFamily:    "Arial, sans-serif",
                    textAlign:     "center",
                  }}>
                    Scripture
                  </p>
                  {prayer.verses.map(v => (
                    <div key={v.ref} style={{
                      display:      "flex",
                      gap:          "10px",
                      padding:      "8px 12px",
                      marginBottom: "6px",
                      background:   "rgba(176,124,31,0.05)",
                      borderLeft:   "2.5px solid #B07C1F",
                      borderRadius: "0 4px 4px 0",
                    }}>
                      <span style={{ fontSize: "11px", fontWeight: 700, color: "#B07C1F", minWidth: "90px", fontFamily: "Arial, sans-serif", flexShrink: 0 }}>
                        {v.ref}
                      </span>
                      <span style={{ fontSize: "11.5px", color: "#444", lineHeight: 1.65, fontStyle: "italic", fontFamily: "'Lora', Georgia, serif" }}>
                        {v.text}
                      </span>
                    </div>
                  ))}
                </div>
              )}

              {/* Amen */}
              <div style={{ marginTop: "22px", textAlign: "center" }}>
                <span style={{
                  fontFamily: "'Great Vibes', 'Playfair Display', Georgia, serif",
                  fontSize:   "36px",
                  color:      "#1a1a1a",
                  fontWeight: 400,
                }}>
                  Amen.
                </span>
                <div style={{ marginTop: "8px", color: "#B07C1F", fontSize: "14px", letterSpacing: "6px" }}>
                  ✦ ✦ ✦
                </div>
              </div>

              {/* Footer */}
              <p style={{
                marginTop:     "24px",
                fontSize:      "8px",
                letterSpacing: "0.14em",
                textTransform: "uppercase",
                color:         "rgba(176,124,31,0.5)",
                fontFamily:    "Arial, sans-serif",
              }}>
                prayerkey.com · Your prayer, beautifully written
              </p>
            </div>
          </div>

        </div>
      </div>

      {/* ── Print target (hidden normally, shown on print) ── */}
      <div id="pk-prayer-print" ref={null}>
        {/* Reuses cardRef content via CSS — window.print() reveals it */}
      </div>
    </>
  );
}
