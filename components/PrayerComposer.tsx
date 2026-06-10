"use client";

import { useState, useRef } from "react";
import { composePrayer, type ComposedPrayer } from "@/lib/prayer-engine/compose";

const DEFAULT_CHIPS = [
  "Healing for my father",
  "My visa interview next week",
  "Financial breakthrough for my business",
  "Peace — I can't sleep, too much worry",
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
 * Client-side prayer composer — Airbnb-style tool placed above the fold
 * on every prayer page. No API, no server: composition runs in the browser.
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
    ? `e.g. I need prayer about ${seedTopic.toLowerCase().replace(/^prayer (for|of|to|when|over|against)\s*/i, "")} — describe your situation…`
    : "e.g. I have a job interview on Friday and my mother is in hospital. I need God's favour and her healing.";

  return (
    <div style={{ marginBottom: compact ? "36px" : "48px" }}>
      {/* Composer panel */}
      <div style={{
        background:   "var(--pk-surface)",
        border:       "1.5px solid var(--pk-accent-border)",
        borderRadius: "16px",
        padding:      compact ? "18px" : "22px",
        boxShadow:    "var(--pk-shadow)",
      }}>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", gap: "10px", marginBottom: "12px", flexWrap: "wrap" }}>
          <p style={{
            fontSize: "11px", fontWeight: 800, color: "var(--pk-accent)",
            letterSpacing: "0.14em", textTransform: "uppercase", margin: 0,
          }}>
            ✦ Type your prayer points — receive a prayer
          </p>
          <span style={{
            fontSize: "10px", fontWeight: 600, color: "var(--pk-text-3)",
            border: "1px solid var(--pk-border)", borderRadius: "999px", padding: "3px 10px",
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
            resize: "vertical", color: "var(--pk-text)", fontFamily: "inherit",
            fontSize: "15px", lineHeight: 1.6, minHeight: compact ? "56px" : "76px",
          }}
        />

        <div style={{
          display: "flex", alignItems: "center", justifyContent: "space-between",
          gap: "12px", marginTop: "8px", borderTop: "1px solid var(--pk-border)",
          paddingTop: "12px", flexWrap: "wrap",
        }}>
          <span style={{ fontSize: "12px", color: "var(--pk-text-3)" }}>
            Composed on your device — nothing you type leaves this page.
          </span>
          <button
            onClick={() => generate()}
            style={{
              background: "var(--pk-accent)", color: "#fff", fontWeight: 700,
              fontSize: "14px", padding: "11px 22px", borderRadius: "10px",
              border: "none", cursor: "pointer",
              boxShadow: "3px 3px 0 0 var(--pk-accent-border)",
              transition: "transform 120ms ease",
            }}
          >
            ✦ Compose my prayer
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
              background: "transparent", border: "1px solid var(--pk-border)",
              color: "var(--pk-text-3)", fontSize: "12px", padding: "6px 13px",
              borderRadius: "999px", cursor: "pointer", transition: "all 120ms ease",
            }}
            onMouseEnter={(e) => { e.currentTarget.style.borderColor = "var(--pk-accent)"; e.currentTarget.style.color = "var(--pk-accent)"; }}
            onMouseLeave={(e) => { e.currentTarget.style.borderColor = "var(--pk-border)"; e.currentTarget.style.color = "var(--pk-text-3)"; }}
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
            background: "var(--pk-surface)",
            border: "1.5px solid var(--pk-accent-border)",
            borderRadius: "16px",
            padding: "clamp(22px,4vw,34px)",
            position: "relative",
            boxShadow: "var(--pk-shadow)",
          }}
        >
          <p style={{
            fontSize: "10px", fontWeight: 800, color: "var(--pk-accent)",
            letterSpacing: "0.2em", textTransform: "uppercase",
            textAlign: "center", margin: "0 0 4px",
          }}>
            A Prayer For You
          </p>
          <p style={{ fontSize: "12px", color: "var(--pk-text-3)", textAlign: "center", margin: "0 0 20px" }}>
            Praying over: {result.topics.join(" · ")}
          </p>

          {result.prayer.split("\n\n").map((para, i) => (
            <p key={i} style={{
              fontFamily: "Georgia, serif", fontSize: "16px", lineHeight: 1.8,
              color: "var(--pk-text)", margin: i === 0 ? 0 : "14px 0 0",
            }}>
              {para}
            </p>
          ))}
          <p style={{
            fontFamily: "Georgia, serif", fontStyle: "italic", textAlign: "center",
            color: "var(--pk-accent)", margin: "16px 0 0", fontSize: "16px",
          }}>
            Amen.
          </p>

          {/* Verses */}
          <div style={{ marginTop: "22px", borderTop: "1px solid var(--pk-border)", paddingTop: "16px" }}>
            {result.verses.map((v) => (
              <div key={v.ref} style={{ marginBottom: "12px" }}>
                <q style={{ fontFamily: "Georgia, serif", fontStyle: "italic", fontSize: "14px", color: "var(--pk-text-2)" }}>
                  {v.text}
                </q>
                <span style={{
                  display: "block", fontSize: "11px", letterSpacing: "0.12em",
                  textTransform: "uppercase", color: "var(--pk-accent)", marginTop: "3px",
                }}>
                  {v.ref}
                </span>
              </div>
            ))}
          </div>

          {/* Actions */}
          <div style={{ display: "flex", gap: "10px", justifyContent: "center", marginTop: "20px", flexWrap: "wrap" }}>
            <button
              onClick={() => generate(lastInput.current)}
              style={{
                background: "transparent", border: "1px solid var(--pk-border)",
                color: "var(--pk-text)", fontSize: "13px", padding: "10px 18px",
                borderRadius: "9px", cursor: "pointer",
              }}
            >
              ↻ Pray it differently
            </button>
            <button
              onClick={copy}
              style={{
                background: "transparent", border: "1px solid var(--pk-border)",
                color: "var(--pk-text)", fontSize: "13px", padding: "10px 18px",
                borderRadius: "9px", cursor: "pointer",
              }}
            >
              {copied ? "✓ Copied" : "⧉ Copy prayer"}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
