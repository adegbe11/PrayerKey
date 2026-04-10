"use client";

import { useState } from "react";
import type { PrayerVerse } from "@/lib/ai/prayer-generation";

// ── Types ─────────────────────────────────────────────────────────────────────

interface GeneratedResult {
  id:           string;
  title:        string;
  prayer:       string;
  verses:       PrayerVerse[];
  encouragement: string;
}

// ── Mood config ────────────────────────────────────────────────────────────────

const MOODS = [
  { label: "Grateful",  color: "#34C759", icon: "✦" },
  { label: "Anxious",   color: "#FF9F0A", icon: "◈" },
  { label: "Hopeful",   color: "#0071E3", icon: "◎" },
  { label: "Grieving",  color: "#AF52DE", icon: "◇" },
  { label: "Seeking",   color: "#B07C1F", icon: "◉" },
  { label: "Joyful",    color: "#FF2D55", icon: "✿" },
  { label: "Weary",     color: "#6E6E73", icon: "◌" },
  { label: "Thankful",  color: "#00C7BE", icon: "✺" },
];

// ── Component ──────────────────────────────────────────────────────────────────

export default function PrayerComposer() {
  const [selectedMoods, setSelectedMoods] = useState<string[]>([]);
  const [userInput, setUserInput]         = useState("");
  const [loading, setLoading]             = useState(false);
  const [result, setResult]               = useState<GeneratedResult | null>(null);
  const [error, setError]                 = useState("");
  const [bookmarked, setBookmarked]       = useState(false);
  const [bookmarkLoading, setBookmarkLoading] = useState(false);

  function toggleMood(label: string) {
    setSelectedMoods((prev) =>
      prev.includes(label) ? prev.filter((m) => m !== label) : [...prev, label]
    );
  }

  async function handleGenerate() {
    if (!userInput.trim()) return;
    setLoading(true);
    setError("");
    setResult(null);
    setBookmarked(false);

    try {
      const res = await fetch("/api/prayer/generate", {
        method:  "POST",
        headers: { "Content-Type": "application/json" },
        body:    JSON.stringify({ userInput, moods: selectedMoods }),
      });

      if (!res.ok) {
        const data = await res.json().catch(() => ({})) as { error?: string };
        throw new Error(data.error ?? "Failed to generate prayer");
      }

      const data = await res.json() as GeneratedResult;
      setResult(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong");
    } finally {
      setLoading(false);
    }
  }

  async function handleBookmark() {
    if (!result) return;
    setBookmarkLoading(true);
    try {
      const res = await fetch(`/api/prayer/${result.id}/bookmark`, { method: "POST" });
      if (res.ok) setBookmarked((b) => !b);
    } finally {
      setBookmarkLoading(false);
    }
  }

  function handleReset() {
    setResult(null);
    setUserInput("");
    setSelectedMoods([]);
    setError("");
    setBookmarked(false);
  }

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: "32px" }}>

      {/* ── Composer panel ────────────────────────────────────────────── */}
      {!result && (
        <div
          style={{
            background:   "#FFFFFF",
            borderRadius: "18px",
            padding:      "32px",
            boxShadow:    "var(--pk-shadow-md)",
          }}
        >
          {/* How are you feeling? */}
          <p style={{ fontSize: "13px", fontWeight: 600, color: "var(--pk-t3)", letterSpacing: "0.05em", textTransform: "uppercase", marginBottom: "14px" }}>
            How are you feeling?
          </p>
          <div style={{ display: "flex", flexWrap: "wrap", gap: "8px", marginBottom: "28px" }}>
            {MOODS.map((mood) => {
              const active = selectedMoods.includes(mood.label);
              return (
                <button
                  key={mood.label}
                  onClick={() => toggleMood(mood.label)}
                  style={{
                    display:      "flex",
                    alignItems:   "center",
                    gap:          "6px",
                    padding:      "7px 14px",
                    borderRadius: "100px",
                    border:       active ? `1.5px solid ${mood.color}` : "1px solid var(--pk-b1)",
                    background:   active ? `${mood.color}14` : "#FAFAFA",
                    color:        active ? mood.color : "var(--pk-t2)",
                    fontSize:     "13px",
                    fontWeight:   active ? 600 : 400,
                    cursor:       "pointer",
                    transition:   "all var(--pk-duration) var(--pk-ease)",
                  }}
                >
                  <span style={{ fontSize: "11px" }}>{mood.icon}</span>
                  {mood.label}
                </button>
              );
            })}
          </div>

          {/* Prayer request textarea */}
          <p style={{ fontSize: "13px", fontWeight: 600, color: "var(--pk-t3)", letterSpacing: "0.05em", textTransform: "uppercase", marginBottom: "10px" }}>
            What&apos;s on your heart?
          </p>
          <textarea
            value={userInput}
            onChange={(e) => setUserInput(e.target.value)}
            placeholder="Share what you'd like to bring before God. The more specific you are, the more personal your prayer will be…"
            rows={5}
            style={{
              width:        "100%",
              padding:      "14px 16px",
              borderRadius: "12px",
              border:       "1px solid var(--pk-b1)",
              background:   "#FAFAFA",
              color:        "var(--pk-t1)",
              fontSize:     "15px",
              lineHeight:   1.6,
              resize:       "vertical",
              outline:      "none",
              fontFamily:   "inherit",
              boxSizing:    "border-box",
              transition:   "border-color var(--pk-duration) var(--pk-ease)",
            }}
            onFocus={(e) => (e.target.style.borderColor = "var(--pk-gold)")}
            onBlur={(e) => (e.target.style.borderColor = "var(--pk-b1)")}
          />

          {/* Char count */}
          <p style={{ fontSize: "12px", color: "var(--pk-t3)", textAlign: "right", marginTop: "6px" }}>
            {userInput.length} / 800
          </p>

          {error && (
            <p style={{ fontSize: "13px", color: "#FF3B30", marginTop: "8px" }}>{error}</p>
          )}

          {/* Generate button */}
          <button
            onClick={handleGenerate}
            disabled={loading || !userInput.trim()}
            style={{
              marginTop:    "20px",
              width:        "100%",
              padding:      "14px 24px",
              borderRadius: "980px",
              border:       "none",
              background:   loading || !userInput.trim() ? "var(--pk-b1)" : "var(--pk-t1)",
              color:        loading || !userInput.trim() ? "var(--pk-t3)" : "#FFFFFF",
              fontSize:     "15px",
              fontWeight:   600,
              cursor:       loading || !userInput.trim() ? "not-allowed" : "pointer",
              transition:   "all var(--pk-duration) var(--pk-ease)",
              display:      "flex",
              alignItems:   "center",
              justifyContent: "center",
              gap:          "8px",
            }}
          >
            {loading ? (
              <>
                <SpinnerIcon />
                Crafting your prayer…
              </>
            ) : (
              <>
                <CrossIcon />
                Generate Prayer
              </>
            )}
          </button>
        </div>
      )}

      {/* ── Generated prayer result ───────────────────────────────────── */}
      {result && (
        <div style={{ display: "flex", flexDirection: "column", gap: "20px" }}>

          {/* Encouragement bar */}
          <div
            style={{
              background:   "rgba(176,124,31,0.06)",
              border:       "0.5px solid rgba(176,124,31,0.2)",
              borderRadius: "12px",
              padding:      "14px 18px",
              display:      "flex",
              alignItems:   "center",
              gap:          "10px",
            }}
          >
            <span style={{ fontSize: "18px" }}>✦</span>
            <p style={{ fontSize: "14px", color: "var(--pk-t1)", lineHeight: 1.5, margin: 0 }}>
              {result.encouragement}
            </p>
          </div>

          {/* Prayer card */}
          <div
            style={{
              background:   "#FFFFFF",
              borderRadius: "18px",
              padding:      "32px",
              boxShadow:    "var(--pk-shadow-md)",
              position:     "relative",
            }}
          >
            {/* Title + actions */}
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: "20px" }}>
              <div>
                <p style={{ fontSize: "11px", color: "var(--pk-gold)", fontWeight: 600, letterSpacing: "0.06em", textTransform: "uppercase", marginBottom: "4px" }}>
                  Your Prayer
                </p>
                <h2 style={{ fontSize: "20px", fontWeight: 600, color: "var(--pk-t1)", margin: 0 }}>
                  {result.title}
                </h2>
              </div>
              <button
                onClick={handleBookmark}
                disabled={bookmarkLoading}
                title={bookmarked ? "Remove bookmark" : "Bookmark prayer"}
                style={{
                  background:   bookmarked ? "rgba(176,124,31,0.10)" : "transparent",
                  border:       bookmarked ? "1px solid rgba(176,124,31,0.3)" : "1px solid var(--pk-b1)",
                  borderRadius: "10px",
                  padding:      "8px 10px",
                  cursor:       "pointer",
                  color:        bookmarked ? "var(--pk-gold)" : "var(--pk-t3)",
                  fontSize:     "18px",
                  lineHeight:   1,
                  transition:   "all var(--pk-duration) var(--pk-ease)",
                }}
              >
                {bookmarked ? "★" : "☆"}
              </button>
            </div>

            {/* Mood chips (if any) */}
            {selectedMoods.length > 0 && (
              <div style={{ display: "flex", gap: "6px", flexWrap: "wrap", marginBottom: "20px" }}>
                {selectedMoods.map((m) => {
                  const mood = MOODS.find((x) => x.label === m);
                  return (
                    <span
                      key={m}
                      style={{
                        fontSize:     "11px",
                        padding:      "3px 10px",
                        borderRadius: "100px",
                        background:   `${mood?.color ?? "#B07C1F"}14`,
                        color:        mood?.color ?? "var(--pk-gold)",
                        fontWeight:   600,
                      }}
                    >
                      {m}
                    </span>
                  );
                })}
              </div>
            )}

            {/* Divider */}
            <div style={{ height: "1px", background: "var(--pk-b1)", marginBottom: "20px" }} />

            {/* Prayer text */}
            <p
              style={{
                fontSize:   "16px",
                lineHeight: 1.8,
                color:      "var(--pk-t1)",
                fontStyle:  "italic",
                whiteSpace: "pre-wrap",
                margin:     0,
              }}
            >
              {result.prayer}
            </p>

            {/* Verses */}
            {result.verses.length > 0 && (
              <div style={{ marginTop: "28px", display: "flex", flexDirection: "column", gap: "12px" }}>
                <p style={{ fontSize: "11px", fontWeight: 600, color: "var(--pk-t3)", letterSpacing: "0.05em", textTransform: "uppercase", marginBottom: "4px" }}>
                  Scripture
                </p>
                {result.verses.map((v, i) => (
                  <div
                    key={i}
                    style={{
                      background:   "#FAFAFA",
                      borderRadius: "10px",
                      padding:      "12px 16px",
                      borderLeft:   "3px solid var(--pk-gold)",
                    }}
                  >
                    <p style={{ fontSize: "13px", fontStyle: "italic", color: "var(--pk-t2)", lineHeight: 1.6, margin: "0 0 4px" }}>
                      &ldquo;{v.text}&rdquo;
                    </p>
                    <p style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-gold)", margin: 0 }}>
                      {v.ref} &middot; {v.translation}
                    </p>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Actions row */}
          <div style={{ display: "flex", gap: "12px" }}>
            <button
              onClick={handleReset}
              style={{
                flex:         1,
                padding:      "12px 20px",
                borderRadius: "980px",
                border:       "1px solid var(--pk-b1)",
                background:   "#FFFFFF",
                color:        "var(--pk-t1)",
                fontSize:     "14px",
                fontWeight:   500,
                cursor:       "pointer",
                transition:   "all var(--pk-duration) var(--pk-ease)",
              }}
            >
              New Prayer
            </button>
            <button
              onClick={() => {
                navigator.clipboard?.writeText(result.prayer);
              }}
              style={{
                flex:         1,
                padding:      "12px 20px",
                borderRadius: "980px",
                border:       "none",
                background:   "var(--pk-t1)",
                color:        "#FFFFFF",
                fontSize:     "14px",
                fontWeight:   600,
                cursor:       "pointer",
                transition:   "all var(--pk-duration) var(--pk-ease)",
              }}
            >
              Copy Prayer
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

// ── Mini SVG icons ─────────────────────────────────────────────────────────────

function SpinnerIcon() {
  return (
    <svg
      width="16" height="16" viewBox="0 0 16 16"
      style={{ animation: "spin 0.8s linear infinite" }}
    >
      <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
      <circle cx="8" cy="8" r="6" stroke="currentColor" strokeWidth="2" fill="none" strokeDasharray="20" strokeDashoffset="10" />
    </svg>
  );
}

function CrossIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
      <rect x="6" y="1" width="2" height="12" rx="1" fill="currentColor" />
      <rect x="1" y="4" width="12" height="2" rx="1" fill="currentColor" />
    </svg>
  );
}
