"use client";

import { useState, useRef } from "react";
import { composePrayer, type ComposedPrayer } from "@/lib/prayer-engine/compose";

/* ── Lampstand palette ─────────────────────────────────────────── */
const C = {
  pine:      "#11241C",
  panel:     "#1B342A",
  panel2:    "#234134",
  ivory:     "#F2EBDC",
  ivoryDim:  "#CFC8B6",
  brass:     "#C9A24B",
  brassSoft: "#E0C685",
  sage:      "#6FA287",
  line:      "rgba(242,235,220,.14)",
  brassLine: "rgba(201,162,75,.35)",
};

const DEFAULT_CHIPS = [
  "Healing for my father",
  "My visa interview next week",
  "Financial breakthrough for my business",
  "Peace. I can't sleep, too much worry",
  "My final exams",
  "Restore my marriage",
];

interface Props {
  /** Pre-seed the textarea placeholder & chips for a specific topic page */
  seedTopic?: string;
  /** Tighter spacing for embedding above article content */
  compact?: boolean;
}

/**
 * Client-side prayer composer placed above the fold on every prayer page.
 * No API, no server: composition runs entirely in the browser.
 */
export default function PrayerComposer({ seedTopic, compact }: Props) {
  const [input, setInput]   = useState("");
  const [result, setResult] = useState<ComposedPrayer | null>(null);
  const [copied, setCopied] = useState(false);
  const lastInput           = useRef("");
  const cardRef             = useRef<HTMLDivElement>(null);

  function generate(text?: string) {
    const raw = (text ?? input).trim();
    if (!raw) return;
    lastInput.current = raw;
    if (text) setInput(text);
    setResult(composePrayer(raw));
    setCopied(false);
    setTimeout(() => cardRef.current?.scrollIntoView({ behavior: "smooth", block: "nearest" }), 60);
  }

  async function copy() {
    if (!result) return;
    try {
      await navigator.clipboard.writeText(result.prayer + "\n\nAmen.");
      setCopied(true);
      setTimeout(() => setCopied(false), 1600);
    } catch {}
  }

  const placeholder = seedTopic
    ? `e.g. I need prayer about ${seedTopic.toLowerCase().replace(/^prayer (for|of|to|when|over|against)\s*/i, "")}. Describe your situation…`
    : "e.g. I have a job interview on Friday and my mother is in hospital. I need God's favour and her healing.";

  return (
    <div style={{ marginBottom: compact ? "36px" : "48px" }}>
      {/* Composer panel */}
      <div style={{
        background:   C.panel,
        border:       `1px solid ${C.line}`,
        borderRadius: "14px",
        padding:      compact ? "18px" : "22px",
      }}>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: "10px", marginBottom: "12px", flexWrap: "wrap" }}>
          <p style={{
            fontSize: "11px", fontWeight: 800, color: C.brass,
            letterSpacing: "0.14em", textTransform: "uppercase", margin: 0,
          }}>
            ✦ Type your prayer points. Receive a prayer.
          </p>
          <span style={{
            fontSize: "10px", fontWeight: 600, color: C.sage,
            border: `1px solid ${C.line}`, borderRadius: "999px", padding: "3px 10px",
            letterSpacing: "0.08em", textTransform: "uppercase",
          }}>
            Instant · Private · No account
          </span>
        </div>

        <textarea
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => { if (e.key === "Enter" && (e.ctrlKey || e.metaKey)) generate(); }}
          placeholder={placeholder}
          aria-label="Your prayer points"
          rows={compact ? 2 : 3}
          style={{
            width: "100%", background: "transparent", border: "none", outline: "none",
            resize: "vertical", color: C.ivory, fontFamily: "inherit",
            fontSize: "16px", lineHeight: 1.6, minHeight: compact ? "56px" : "76px",
          }}
        />

        <div style={{
          display: "flex", alignItems: "center", justifyContent: "space-between",
          gap: "12px", marginTop: "8px", borderTop: `1px solid ${C.line}`,
          paddingTop: "12px", flexWrap: "wrap",
        }}>
          <span style={{ fontSize: "12px", color: C.ivoryDim }}>
            Composed on your device. Nothing you type leaves this page.
          </span>
          <button
            onClick={() => generate()}
            style={{
              background: C.brass, color: "#241B07", fontWeight: 600,
              fontSize: "14px", padding: "12px 22px", borderRadius: "10px",
              border: "none", cursor: "pointer",
              transition: "transform 120ms ease, box-shadow 120ms ease",
            }}
            onMouseEnter={(e) => { e.currentTarget.style.transform = "translateY(-1px)"; e.currentTarget.style.boxShadow = "0 6px 18px rgba(201,162,75,.3)"; }}
            onMouseLeave={(e) => { e.currentTarget.style.transform = "none"; e.currentTarget.style.boxShadow = "none"; }}
          >
            Compose my prayer
          </button>
        </div>
      </div>

      {/* Example chips */}
      <div style={{ display: "flex", gap: "8px", flexWrap: "wrap", marginTop: "12px" }} aria-label="Example prayer points">
        {(seedTopic ? [seedTopic, ...DEFAULT_CHIPS.slice(0, 4)] : DEFAULT_CHIPS).map((chip) => (
          <button
            key={chip}
            onClick={() => generate(chip)}
            style={{
              background: C.panel, border: `1px solid ${C.line}`,
              color: C.ivoryDim, fontSize: "12px", padding: "7px 13px",
              borderRadius: "999px", cursor: "pointer", transition: "all 120ms ease",
            }}
            onMouseEnter={(e) => { e.currentTarget.style.borderColor = C.brass; e.currentTarget.style.color = C.brassSoft; }}
            onMouseLeave={(e) => { e.currentTarget.style.borderColor = C.line; e.currentTarget.style.color = C.ivoryDim; }}
          >
            {chip}
          </button>
        ))}
      </div>

      {/* Result card */}
      {result && (
        <div
          ref={cardRef}
          aria-live="polite"
          style={{
            marginTop: "20px",
            background: `linear-gradient(170deg, ${C.panel2}, ${C.panel})`,
            border: `1px solid ${C.brassLine}`,
            borderRadius: "16px",
            padding: "clamp(26px,4vw,36px)",
            position: "relative",
            boxShadow: "inset 0 0 0 1px rgba(201,162,75,.12)",
          }}
        >
          <p style={{
            fontSize: "10px", fontWeight: 800, color: C.brass,
            letterSpacing: "0.2em", textTransform: "uppercase",
            textAlign: "center", margin: "0 0 4px",
          }}>
            A Prayer For You
          </p>
          <p style={{ fontSize: "12px", color: C.sage, textAlign: "center", margin: "0 0 20px" }}>
            Praying over: {result.topics.join(" · ")}
          </p>

          {result.prayer.split("\n\n").map((para, i) => (
            <p key={i} style={{
              fontFamily: "Georgia, serif", fontSize: "16px", lineHeight: 1.8,
              color: C.ivory, margin: i === 0 ? 0 : "14px 0 0",
            }}>
              {para}
            </p>
          ))}
          <p style={{
            fontFamily: "Georgia, serif", fontStyle: "italic", textAlign: "center",
            color: C.brassSoft, margin: "16px 0 0", fontSize: "16px",
          }}>
            Amen.
          </p>

          {/* Verses */}
          <div style={{ marginTop: "22px", borderTop: `1px solid ${C.line}`, paddingTop: "16px" }}>
            {result.verses.map((v) => (
              <div key={v.ref} style={{ marginBottom: "12px" }}>
                <q style={{ fontFamily: "Georgia, serif", fontStyle: "italic", fontSize: "14px", color: C.ivoryDim }}>
                  {v.text}
                </q>
                <span style={{
                  display: "block", fontSize: "11px", letterSpacing: "0.12em",
                  textTransform: "uppercase", color: C.brass, marginTop: "3px",
                }}>
                  {v.ref}
                </span>
              </div>
            ))}
          </div>

          {/* Actions */}
          <div style={{ display: "flex", gap: "10px", justifyContent: "center", marginTop: "22px", flexWrap: "wrap" }}>
            <button
              onClick={() => generate(lastInput.current)}
              style={{
                background: "transparent", border: `1px solid ${C.line}`,
                color: C.ivory, fontSize: "13px", padding: "10px 18px",
                borderRadius: "9px", cursor: "pointer", transition: "border-color 120ms ease",
              }}
              onMouseEnter={(e) => { e.currentTarget.style.borderColor = C.brass; }}
              onMouseLeave={(e) => { e.currentTarget.style.borderColor = C.line; }}
            >
              ↻ Pray it differently
            </button>
            <button
              onClick={copy}
              style={{
                background: "transparent", border: `1px solid ${C.line}`,
                color: C.ivory, fontSize: "13px", padding: "10px 18px",
                borderRadius: "9px", cursor: "pointer", transition: "border-color 120ms ease",
              }}
              onMouseEnter={(e) => { e.currentTarget.style.borderColor = C.brass; }}
              onMouseLeave={(e) => { e.currentTarget.style.borderColor = C.line; }}
            >
              {copied ? "✓ Copied" : "⧉ Copy prayer"}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
