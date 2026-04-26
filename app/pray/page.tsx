"use client";
import { useState } from "react";
import dynamic from "next/dynamic";

const PrayerCardModal = dynamic(() => import("@/components/ui/PrayerCardModal"), { ssr: false });

const MOODS = ["Grateful", "Anxious", "Sad", "Hopeful", "Confused", "Joyful", "Sick", "Tired"];

export default function PrayPage() {
  const [input,     setInput]     = useState("");
  const [moods,     setMoods]     = useState<string[]>([]);
  const [prayer,    setPrayer]    = useState<{ title: string; prayer: string; encouragement: string; verses: { ref: string; text: string }[] } | null>(null);
  const [showCard,  setShowCard]  = useState(false);
  const [loading,   setLoading]   = useState(false);
  const [error,     setError]     = useState("");
  const [copied,    setCopied]    = useState(false);

  function toggleMood(m: string) {
    setMoods((prev) => prev.includes(m) ? prev.filter((x) => x !== m) : [...prev, m]);
  }

  async function generate() {
    if (!input.trim()) return;
    setLoading(true);
    setError("");
    setPrayer(null);
    try {
      const res  = await fetch("/api/prayer/generate", {
        method:  "POST",
        headers: { "Content-Type": "application/json" },
        body:    JSON.stringify({ userInput: input, moods }),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || "Something went wrong");
      setPrayer(data);
      setShowCard(true);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Something went wrong");
    } finally {
      setLoading(false);
    }
  }

  async function copy() {
    if (!prayer) return;
    await navigator.clipboard.writeText(prayer.prayer);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  }

  return (
    <div style={{ maxWidth: "680px", margin: "0 auto", padding: "0 0 80px" }}>

      {/* ── Page header ── */}
      <div className="animate-fadeUp" style={{ textAlign: "center", marginBottom: "48px" }}>
        {/* Label */}
        <div style={{
          display:      "inline-flex",
          alignItems:   "center",
          gap:          "8px",
          padding:      "4px 12px",
          border:       "1.5px solid var(--pk-purple-border)",
          borderRadius: "4px",
          marginBottom: "20px",
          background:   "var(--pk-purple-dim)",
          boxShadow:    "3px 3px 0 0 var(--pk-purple-border)",
        }}>
          <span style={{ fontSize: "10px", fontWeight: 700, color: "var(--pk-purple)", letterSpacing: "0.12em", textTransform: "uppercase" }}>
            AI Prayer Generator
          </span>
        </div>

        <h1 style={{
          fontSize:      "clamp(32px, 6vw, 56px)",
          fontWeight:    800,
          color:         "var(--pk-text)",
          margin:        "0 0 14px",
          letterSpacing: "-0.03em",
          lineHeight:    1.05,
        }}>
          Tell me what to<br />pray about.
        </h1>
        <p style={{
          fontSize:    "clamp(15px, 1.4vw, 17px)",
          color:       "var(--pk-text-2)",
          margin:      0,
          lineHeight:  1.65,
          maxWidth:    "420px",
          marginInline: "auto",
        }}>
          Type anything — a worry, a thank you, a situation — and we&apos;ll write a full prayer for you in seconds.
        </p>
      </div>

      {/* ── Input card ── */}
      <div className="animate-fadeUp delay-100" style={{
        background:   "var(--pk-card)",
        border:       "1.5px solid var(--pk-border)",
        borderRadius: "20px",
        padding:      "clamp(20px,4vw,32px)",
        marginBottom: "16px",
        boxShadow:    "var(--pk-shadow-sm)",
      }}>

        {/* Textarea */}
        <textarea
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="e.g. I'm worried about my job. Please pray for my family. I'm thankful for healing..."
          rows={5}
          style={{
            width:        "100%",
            background:   "transparent",
            border:       "none",
            padding:      "0",
            fontSize:     "clamp(15px, 1.4vw, 17px)",
            color:        "var(--pk-text)",
            resize:       "none",
            outline:      "none",
            boxSizing:    "border-box",
            fontFamily:   "inherit",
            lineHeight:   1.7,
          }}
        />

        <div style={{ height: "1px", background: "var(--pk-border)", margin: "16px 0" }} />

        {/* Mood chips */}
        <div>
          <p style={{
            fontSize:     "11px",
            fontWeight:   700,
            letterSpacing:"0.08em",
            textTransform:"uppercase",
            color:        "var(--pk-text-3)",
            marginBottom: "10px",
          }}>
            How are you feeling? (optional)
          </p>
          <div style={{ display: "flex", flexWrap: "wrap", gap: "8px" }}>
            {MOODS.map((m) => {
              const active = moods.includes(m);
              return (
                <button
                  key={m}
                  onClick={() => toggleMood(m)}
                  style={{
                    padding:      "8px 16px",
                    minHeight:    "36px",
                    borderRadius: "4px",
                    border:       active ? "1.5px solid var(--pk-purple)" : "1.5px solid var(--pk-border)",
                    background:   active ? "var(--pk-purple-dim)" : "var(--pk-card)",
                    color:        active ? "var(--pk-purple)" : "var(--pk-text-3)",
                    fontSize:     "12px",
                    fontWeight:   active ? 700 : 500,
                    cursor:       "pointer",
                    transition:   "all 150ms ease",
                    boxShadow:    active ? "2px 2px 0 0 var(--pk-purple-border)" : "none",
                    letterSpacing:"-0.01em",
                  }}
                >
                  {m}
                </button>
              );
            })}
          </div>
        </div>
      </div>

      {/* ── Generate button ── */}
      <button
        onClick={generate}
        disabled={loading || !input.trim()}
        style={{
          width:        "100%",
          padding:      "16px 28px",
          minHeight:    "56px",
          borderRadius: "6px",
          border:       "2px solid",
          borderColor:  loading || !input.trim() ? "var(--pk-purple-border)" : "var(--pk-purple)",
          background:   loading || !input.trim() ? "var(--pk-purple-dim)" : "var(--pk-purple)",
          color:        loading || !input.trim() ? "var(--pk-text-3)" : "#fff",
          fontSize:     "16px",
          fontWeight:   800,
          cursor:       loading || !input.trim() ? "not-allowed" : "pointer",
          transition:   "all 200ms ease",
          letterSpacing:"-0.01em",
          boxShadow:    loading || !input.trim() ? "none" : "4px 4px 0 0 var(--pk-purple-border)",
        }}
        onMouseEnter={e => {
          if (loading || !input.trim()) return;
          (e.currentTarget as HTMLButtonElement).style.transform = "translate(-2px,-2px)";
          (e.currentTarget as HTMLButtonElement).style.boxShadow = "6px 6px 0 0 var(--pk-purple-border)";
        }}
        onMouseLeave={e => {
          (e.currentTarget as HTMLButtonElement).style.transform = "translate(0,0)";
          (e.currentTarget as HTMLButtonElement).style.boxShadow = loading || !input.trim() ? "none" : "4px 4px 0 0 var(--pk-purple-border)";
        }}
      >
        {loading ? (
          <span style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "10px" }}>
            <span style={{ width: "16px", height: "16px", border: "2px solid var(--pk-purple-border)", borderTopColor: "#fff", borderRadius: "50%", display: "inline-block", animation: "spin 700ms linear infinite" }} />
            Writing your prayer...
          </span>
        ) : "✦ Generate Prayer"}
      </button>

      {/* ── Error ── */}
      {error && (
        <div style={{ marginTop: "16px", padding: "14px 18px", borderRadius: "8px", background: "rgba(255,59,48,0.08)", border: "1.5px solid rgba(255,59,48,0.25)", color: "var(--pk-red)", fontSize: "14px", boxShadow: "3px 3px 0 0 rgba(255,59,48,0.15)" }}>
          {error}
        </div>
      )}

      {/* ── Prayer result ── */}
      {prayer && (
        <div style={{ marginTop: "32px", animation: "prayerIn 480ms cubic-bezier(0.22,1,0.36,1)" }}>

          {/* Title bar */}
          <div style={{
            display:        "flex",
            alignItems:     "center",
            justifyContent: "space-between",
            padding:        "14px 20px",
            background:     "var(--pk-purple-dim)",
            border:         "1.5px solid var(--pk-purple-border)",
            borderRadius:   "12px 12px 0 0",
            borderBottom:   "none",
          }}>
            <h2 style={{ fontSize: "15px", fontWeight: 700, color: "var(--pk-purple)", margin: 0, letterSpacing: "-0.01em" }}>
              {prayer.title}
            </h2>
            <span style={{ fontSize: "11px", color: "var(--pk-text-3)", fontWeight: 600, letterSpacing: "0.06em", textTransform: "uppercase" }}>
              ✦ Generated
            </span>
          </div>

          {/* Prayer body */}
          <div style={{
            background:   "var(--pk-purple-dim)",
            border:       "1.5px solid var(--pk-purple-border)",
            borderTop:    "none",
            borderRadius: "0 0 0 0",
            padding:      "clamp(20px,4vw,32px)",
          }}>
            <p style={{
              fontSize:    "clamp(15px, 1.4vw, 17px)",
              color:       "var(--pk-text)",
              lineHeight:  1.85,
              whiteSpace:  "pre-wrap",
              margin:      0,
              fontStyle:   "italic",
            }}>
              {prayer.prayer}
            </p>
          </div>

          {/* Encouragement */}
          <div style={{
            padding:      "18px 24px",
            background:   "var(--pk-gold-dim)",
            border:       "1.5px solid var(--pk-gold-border)",
            borderTop:    "none",
            borderRadius: "0",
            borderLeft:   "4px solid var(--pk-gold)",
          }}>
            <p style={{ fontSize: "14px", color: "var(--pk-text-2)", margin: 0, lineHeight: 1.7 }}>
              {prayer.encouragement}
            </p>
          </div>

          {/* Verses */}
          {prayer.verses?.length > 0 && (
            <div style={{
              padding:      "clamp(16px,3vw,24px)",
              background:   "var(--pk-card)",
              border:       "1.5px solid var(--pk-border)",
              borderTop:    "none",
              borderRadius: "0",
            }}>
              <p style={{ fontSize: "10px", fontWeight: 700, color: "var(--pk-gold)", letterSpacing: "0.1em", textTransform: "uppercase", margin: "0 0 12px" }}>
                Scripture
              </p>
              <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
                {prayer.verses.map((v) => (
                  <div key={v.ref} style={{
                    padding:    "12px 16px",
                    background: "var(--pk-gold-dim)",
                    border:     "1px solid var(--pk-gold-border)",
                    borderRadius:"8px",
                    display:    "flex",
                    gap:        "12px",
                    alignItems: "flex-start",
                  }}>
                    <span style={{ fontSize: "12px", fontWeight: 700, color: "var(--pk-gold)", flexShrink: 0, minWidth: "80px" }}>{v.ref}</span>
                    <span style={{ fontSize: "13px", color: "var(--pk-text-2)", lineHeight: 1.6 }}>{v.text}</span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Action bar */}
          <div style={{
            display:      "flex",
            gap:          "10px",
            padding:      "16px 20px",
            background:   "var(--pk-card)",
            border:       "1.5px solid var(--pk-border)",
            borderTop:    "none",
            borderRadius: "0 0 16px 16px",
            flexWrap:     "wrap",
          }}>
            {/* View Card button */}
            <button
              onClick={() => setShowCard(true)}
              style={{
                padding:      "10px 20px",
                minHeight:    "44px",
                borderRadius: "6px",
                border:       "1.5px solid var(--pk-gold-border)",
                background:   "var(--pk-gold-dim)",
                color:        "var(--pk-gold)",
                fontSize:     "13px",
                fontWeight:   700,
                cursor:       "pointer",
                transition:   "all 180ms ease",
                display:      "flex",
                alignItems:   "center",
                gap:          "6px",
                boxShadow:    "2px 2px 0 0 var(--pk-gold-border)",
              }}
            >
              🎴 View &amp; Download Card
            </button>

            <button
              onClick={copy}
              style={{
                padding:      "10px 20px",
                minHeight:    "44px",
                borderRadius: "6px",
                border:       "1.5px solid var(--pk-border)",
                background:   copied ? "rgba(52,199,89,0.1)" : "var(--pk-card)",
                color:        copied ? "#34C759" : "var(--pk-text-2)",
                fontSize:     "13px",
                fontWeight:   600,
                cursor:       "pointer",
                transition:   "all 180ms ease",
                display:      "flex",
                alignItems:   "center",
                gap:          "6px",
              }}
            >
              {copied ? "✓ Copied!" : "📋 Copy"}
            </button>
            <button
              onClick={() => { setPrayer(null); setInput(""); setMoods([]); }}
              style={{
                padding:      "10px 20px",
                minHeight:    "44px",
                borderRadius: "6px",
                border:       "1.5px solid var(--pk-border)",
                background:   "transparent",
                color:        "var(--pk-text-3)",
                fontSize:     "13px",
                cursor:       "pointer",
                transition:   "all 180ms ease",
              }}
            >
              New Prayer
            </button>
          </div>
        </div>
      )}

      {/* ── Prayer Card Modal ── */}
      {showCard && prayer && (
        <PrayerCardModal prayer={prayer} onClose={() => setShowCard(false)} />
      )}

      <style>{`
        textarea:focus { border-color: var(--pk-purple-border) !important; }
        textarea::placeholder { color: var(--pk-text-3); }
        @keyframes spin    { to { transform: rotate(360deg); } }
        @keyframes prayerIn {
          from { opacity: 0; transform: translateY(20px); }
          to   { opacity: 1; transform: translateY(0); }
        }
      `}</style>
    </div>
  );
}
