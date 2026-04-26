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

/* ── Brand tokens (hardcoded for html-to-image) ─────────────────────── */
const T = {
  gold:        "#7A5010",
  goldDim:     "rgba(122,80,16,0.07)",
  goldBorder:  "rgba(122,80,16,0.20)",
  text:        "#1A1108",
  text2:       "#5A4530",
  text3:       "#9A886A",
  bg:          "#ffffff",
  green:       "#34C759",
};

/* ── Corner SVG ornament ────────────────────────────────────────────── */
function Corner({ rotate = 0 }: { rotate?: number }) {
  return (
    <svg
      width="56" height="56" viewBox="0 0 72 72" fill="none"
      style={{ transform: `rotate(${rotate}deg)`, display: "block" }}
    >
      <path d="M6 6 L6 66"   stroke={T.gold} strokeWidth="1.2"/>
      <path d="M6 6 L66 6"   stroke={T.gold} strokeWidth="1.2"/>
      <path d="M14 6 L14 14" stroke={T.gold} strokeWidth="0.8" opacity="0.5"/>
      <path d="M6 14 L14 14" stroke={T.gold} strokeWidth="0.8" opacity="0.5"/>
      <circle cx="6" cy="6" r="3.5" fill={T.gold}/>
      <path d="M6 6 C6 30 30 6 38 38" stroke={T.gold} strokeWidth="1.4" fill="none" strokeLinecap="round"/>
      <path d="M6 22 Q18 10 22 6"     stroke={T.gold} strokeWidth="1"   fill="none" strokeLinecap="round" opacity="0.7"/>
      <path d="M6 34 Q22 22 34 6"     stroke={T.gold} strokeWidth="0.8" fill="none" strokeLinecap="round" opacity="0.5"/>
      <ellipse cx="14" cy="14" rx="5.5" ry="2.5" transform="rotate(45 14 14)"  fill={T.gold} opacity="0.4"/>
      <ellipse cx="22" cy="10" rx="4"   ry="2"   transform="rotate(20 22 10)"  fill={T.gold} opacity="0.28"/>
      <ellipse cx="10" cy="22" rx="4"   ry="2"   transform="rotate(70 10 22)"  fill={T.gold} opacity="0.28"/>
      <circle  cx="38" cy="38" r="2.5"  fill={T.gold} opacity="0.45"/>
    </svg>
  );
}

/* ── Main component ─────────────────────────────────────────────────── */
export default function PrayerCardModal({ prayer, onClose }: Props) {
  const cardRef = useRef<HTMLDivElement>(null);
  const [busy, setBusy] = useState<"png" | "pdf" | null>(null);
  const [copied, setCopied] = useState(false);

  const now    = new Date();
  const dateStr = now.toLocaleDateString("en-GB", { day: "2-digit", month: "short", year: "numeric" });
  const timeStr = now.toLocaleTimeString("en-GB", { hour: "2-digit", minute: "2-digit", second: "2-digit" });
  const longDate = now.toLocaleDateString("en-GB", { day: "2-digit", month: "long", year: "numeric" });

  async function downloadPNG() {
    if (!cardRef.current) return;
    setBusy("png");
    try {
      const { toPng } = await import("html-to-image");
      const dataUrl = await toPng(cardRef.current, {
        pixelRatio:      3,
        backgroundColor: "#ffffff",
        style:           { borderRadius: "0" },
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
    setTimeout(() => { window.print(); setBusy(null); }, 100);
  }

  async function copyText() {
    const full = `${prayer.title}\n\n${prayer.prayer}\n\n${prayer.encouragement}\n\n${prayer.verses.map(v => `${v.ref} — ${v.text}`).join("\n")}`;
    await navigator.clipboard.writeText(full);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  }

  return (
    <>
      <style>{`
        @import url('https://fonts.googleapis.com/css2?family=Lora:ital,wght@0,400;0,600;1,400;1,600&display=swap');
        @media print {
          body > * { display: none !important; }
          #pk-prayer-print { display: block !important; position: fixed; inset: 0; z-index: 99999; }
          @page { size: A4; margin: 0; }
        }
        @keyframes modalIn {
          from { opacity: 0; transform: translateY(24px) scale(0.97); }
          to   { opacity: 1; transform: translateY(0) scale(1); }
        }
        #pk-prayer-print { display: none; }
        .pk-modal-btn:hover { opacity: 0.85; }
      `}</style>

      {/* ── Backdrop ── */}
      <div
        onClick={onClose}
        style={{
          position:       "fixed",
          inset:          0,
          background:     "rgba(10,6,2,0.88)",
          backdropFilter: "blur(8px)",
          zIndex:         1000,
          display:        "flex",
          alignItems:     "flex-start",
          justifyContent: "center",
          padding:        "32px 20px 40px",
          overflowY:      "auto",
        }}
      >
        <div
          onClick={e => e.stopPropagation()}
          style={{
            display:       "flex",
            flexDirection: "column",
            alignItems:    "center",
            gap:           "20px",
            animation:     "modalIn 360ms cubic-bezier(0.22,1,0.36,1)",
            width:         "100%",
            maxWidth:      "640px",
          }}
        >

          {/* ── Action buttons ── */}
          <div style={{ display: "flex", gap: "10px", flexWrap: "wrap", justifyContent: "center", width: "100%" }}>
            {[
              {
                label:  busy === "png" ? "Saving…" : "⬇ Download PNG",
                action: downloadPNG,
                gold:   true,
              },
              {
                label:  busy === "pdf" ? "Opening…" : "Download PDF",
                action: printPDF,
                gold:   false,
              },
              {
                label:  copied ? "✓ Copied" : "Copy Text",
                action: copyText,
                gold:   false,
              },
              {
                label:  "✕ Close",
                action: onClose,
                gold:   false,
              },
            ].map(btn => (
              <button
                key={btn.label}
                className="pk-modal-btn"
                onClick={btn.action}
                style={{
                  padding:       "10px 22px",
                  minHeight:     "42px",
                  borderRadius:  "6px",
                  border:        btn.gold ? `1.5px solid ${T.gold}` : "1.5px solid rgba(255,255,255,0.18)",
                  background:    btn.gold ? T.goldDim : "rgba(255,255,255,0.06)",
                  color:         btn.gold ? T.gold : "rgba(255,255,255,0.7)",
                  fontSize:      "13px",
                  fontWeight:    700,
                  cursor:        "pointer",
                  letterSpacing: "0.02em",
                  transition:    "opacity 150ms",
                  boxShadow:     btn.gold ? `2px 2px 0 0 ${T.goldBorder}` : "none",
                }}
              >
                {btn.label}
              </button>
            ))}
          </div>

          {/* ══════════════════════════════════════════════════════════ */}
          {/* ── Prayer Card (rendered for download) ─────────────────── */}
          {/* ══════════════════════════════════════════════════════════ */}
          <div
            ref={cardRef}
            style={{
              width:      "100%",
              maxWidth:   "600px",
              background: T.bg,
              position:   "relative",
              boxSizing:  "border-box",
              boxShadow:  "0 40px 100px rgba(0,0,0,0.6)",
              flexShrink: 0,
              fontFamily: "Georgia, 'Times New Roman', serif",
            }}
          >
            {/* Outer gold border frame */}
            <div style={{ position: "absolute", inset: "10px", border: `1.5px solid ${T.gold}`, pointerEvents: "none", zIndex: 2 }} />
            <div style={{ position: "absolute", inset: "16px", border: `0.5px solid ${T.goldBorder}`, pointerEvents: "none", zIndex: 2 }} />

            {/* Corner ornaments */}
            <div style={{ position: "absolute", top: "6px",    left: "6px"   }}><Corner rotate={0}   /></div>
            <div style={{ position: "absolute", top: "6px",    right: "6px"  }}><Corner rotate={90}  /></div>
            <div style={{ position: "absolute", bottom: "6px", right: "6px"  }}><Corner rotate={180} /></div>
            <div style={{ position: "absolute", bottom: "6px", left: "6px"   }}><Corner rotate={270} /></div>

            {/* ── Content ── */}
            <div style={{ position: "relative", zIndex: 1, padding: "48px 56px 44px" }}>

              {/* ── Card Header: brand + datetime ── */}
              <div style={{
                display:        "flex",
                justifyContent: "space-between",
                alignItems:     "center",
                marginBottom:   "20px",
                paddingBottom:  "16px",
                borderBottom:   `1px solid ${T.goldBorder}`,
              }}>
                <span style={{ fontSize: "13px", fontWeight: 700, color: T.gold, letterSpacing: "0.06em", fontFamily: "Arial, sans-serif" }}>
                  ✦ PrayerKey
                </span>
                <span style={{ fontSize: "10px", color: T.text3, fontFamily: "Arial, sans-serif", letterSpacing: "0.04em" }}>
                  {dateStr} · {timeStr}
                </span>
              </div>

              {/* ── Title block ── */}
              <div style={{ textAlign: "center", marginBottom: "16px" }}>
                <h2 style={{ fontSize: "24px", fontWeight: 700, color: T.text, margin: "0 0 5px", letterSpacing: "-0.01em", fontFamily: "Georgia, serif" }}>
                  Prayer Receipt
                </h2>
                <p style={{ fontSize: "11px", color: T.text3, margin: 0, fontFamily: "Arial, sans-serif", letterSpacing: "0.04em" }}>
                  Generated from PrayerKey on {longDate}
                </p>
              </div>

              {/* ── Meta rows ── */}
              <div style={{ border: `1px solid ${T.goldBorder}`, borderRadius: "4px", overflow: "hidden", marginBottom: "24px" }}>
                {[
                  { label: "Topic",  value: prayer.title },
                  { label: "Type",   value: "Scripture-Based Prayer" },
                  { label: "Status", value: "Prayer Generated  ✓", green: true },
                ].map(({ label, value, green }, i, arr) => (
                  <div key={label} style={{
                    display:      "flex",
                    gap:          "12px",
                    padding:      "11px 16px",
                    borderBottom: i < arr.length - 1 ? `1px solid ${T.goldBorder}` : "none",
                    background:   i % 2 === 0 ? T.goldDim : T.bg,
                    alignItems:   "center",
                  }}>
                    <span style={{ fontSize: "10px", fontWeight: 700, color: T.gold, minWidth: "60px", letterSpacing: "0.08em", textTransform: "uppercase", fontFamily: "Arial, sans-serif", flexShrink: 0 }}>
                      {label}
                    </span>
                    <span style={{ fontSize: "13px", color: green ? T.green : T.text, fontWeight: green ? 700 : 400, fontFamily: "Arial, sans-serif" }}>
                      {value}
                    </span>
                  </div>
                ))}
              </div>

              {/* ── Section label: Your Prayer ── */}
              <div style={{ display: "flex", alignItems: "center", gap: "12px", marginBottom: "18px" }}>
                <div style={{ flex: 1, height: "1px", background: T.goldBorder }} />
                <span style={{ fontSize: "9px", fontWeight: 700, color: T.gold, letterSpacing: "0.18em", textTransform: "uppercase", fontFamily: "Arial, sans-serif", flexShrink: 0 }}>
                  Your Prayer
                </span>
                <div style={{ flex: 1, height: "1px", background: T.goldBorder }} />
              </div>

              {/* ── Prayer title ── */}
              <h3 style={{
                fontSize:      "20px",
                fontWeight:    700,
                color:         T.text,
                margin:        "0 0 16px",
                textAlign:     "center",
                letterSpacing: "-0.01em",
                lineHeight:    1.25,
                fontFamily:    "Georgia, serif",
              }}>
                {prayer.title}
              </h3>

              {/* ── Prayer body ── */}
              <p style={{
                fontSize:   "14px",
                color:      T.text,
                lineHeight: 1.95,
                whiteSpace: "pre-wrap",
                margin:     "0 0 20px",
                fontStyle:  "italic",
                fontFamily: "Georgia, serif",
              }}>
                {prayer.prayer}
              </p>

              {/* ── Encouragement ── */}
              <div style={{
                padding:    "14px 18px",
                background: T.goldDim,
                borderLeft: `3px solid ${T.gold}`,
                marginBottom: "24px",
              }}>
                <p style={{ fontSize: "12px", color: T.text2, margin: 0, lineHeight: 1.75, fontStyle: "italic", fontFamily: "Georgia, serif" }}>
                  {prayer.encouragement}
                </p>
              </div>

              {/* ── Scripture rows ── */}
              {prayer.verses?.length > 0 && (
                <div style={{ border: `1px solid ${T.goldBorder}`, borderRadius: "4px", overflow: "hidden", marginBottom: "24px" }}>

                  {/* Label row */}
                  <div style={{ padding: "9px 16px", background: T.goldDim, borderBottom: `1px solid ${T.goldBorder}` }}>
                    <span style={{ fontSize: "9px", fontWeight: 700, color: T.gold, letterSpacing: "0.14em", textTransform: "uppercase", fontFamily: "Arial, sans-serif" }}>
                      Scripture
                    </span>
                  </div>

                  {/* Verse rows */}
                  {prayer.verses.map((v, i) => (
                    <div
                      key={v.ref}
                      style={{
                        display:      "flex",
                        borderBottom: i < prayer.verses.length - 1 ? `1px solid ${T.goldBorder}` : "none",
                        alignItems:   "stretch",
                      }}
                    >
                      {/* Ref */}
                      <div style={{
                        padding:     "12px 14px 12px 16px",
                        minWidth:    "100px",
                        flexShrink:  0,
                        borderRight: `1px solid ${T.goldBorder}`,
                        background:  i % 2 === 0 ? T.goldDim : T.bg,
                        display:     "flex",
                        alignItems:  "center",
                      }}>
                        <span style={{ fontSize: "11px", fontWeight: 700, color: T.gold, fontFamily: "Arial, sans-serif", lineHeight: 1.3 }}>
                          {v.ref}
                        </span>
                      </div>
                      {/* Text */}
                      <div style={{ padding: "12px 16px", flex: 1, background: i % 2 === 0 ? T.bg : T.goldDim }}>
                        <span style={{ fontSize: "12px", color: T.text2, lineHeight: 1.7, fontStyle: "italic", fontFamily: "Georgia, serif" }}>
                          {v.text}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              )}

              {/* ── Footer ── */}
              <div style={{
                paddingTop:    "16px",
                borderTop:     `1px solid ${T.goldBorder}`,
                textAlign:     "center",
              }}>
                <div style={{ fontSize: "11px", color: T.gold, letterSpacing: "6px", marginBottom: "6px" }}>
                  ✦ ✦ ✦
                </div>
                <p style={{ fontSize: "9px", color: T.text3, margin: 0, letterSpacing: "0.12em", textTransform: "uppercase", fontFamily: "Arial, sans-serif" }}>
                  prayerkey.com · Your prayer, beautifully written
                </p>
              </div>

            </div>
          </div>
          {/* ══════════════════════════════════════════════════════════ */}

        </div>
      </div>

      <div id="pk-prayer-print" />
    </>
  );
}
