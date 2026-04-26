"use client";
import Link from "next/link";
import { useEffect, useState } from "react";

const ROTATING_WORDS = ["prayers", "sermons", "miracles", "blessings", "churches", "revivals"];

const FEATURES = [
  {
    href:   "/pray",
    emoji:  "🙏",
    title:  "Generate a Prayer",
    desc:   "Tell us what's on your heart and we'll write a beautiful prayer just for you.",
    color:  "#AF52DE",
    bg:     "rgba(175,82,222,0.07)",
    border: "rgba(175,82,222,0.18)",
    glow:   "rgba(175,82,222,0.25)",
  },
  {
    href:   "/live",
    emoji:  "🎙️",
    title:  "Live Sermon",
    desc:   "Start a service. Bible verses appear on the projector screen automatically.",
    color:  "#FF3B30",
    bg:     "rgba(255,59,48,0.07)",
    border: "rgba(255,59,48,0.18)",
    glow:   "rgba(255,59,48,0.25)",
  },
  {
    href:   "/bible",
    emoji:  "📖",
    title:  "Search the Bible",
    desc:   "Find any verse, topic, or keyword in seconds.",
    color:  "#B07C1F",
    bg:     "rgba(176,124,31,0.07)",
    border: "rgba(176,124,31,0.18)",
    glow:   "rgba(176,124,31,0.25)",
  },
];

export default function HomePage() {
  const [wordIndex, setWordIndex] = useState(0);
  const [visible,   setVisible]   = useState(true);

  useEffect(() => {
    const interval = setInterval(() => {
      setVisible(false);
      setTimeout(() => {
        setWordIndex((i) => (i + 1) % ROTATING_WORDS.length);
        setVisible(true);
      }, 350);
    }, 2600);
    return () => clearInterval(interval);
  }, []);

  return (
    <div>

      {/* ══════════════════════════════════════════
          HERO — Neo-brutalist structure
      ══════════════════════════════════════════ */}
      <section style={{ paddingTop: "24px", paddingBottom: "100px", textAlign: "center", position: "relative", overflow: "hidden" }}>

        {/* Hard ruled lines — brutalist grid marks */}
        <div aria-hidden style={{ position: "absolute", top: 0, left: 0, right: 0, height: "1px", background: "var(--pk-accent-dim)" }} />
        <div aria-hidden style={{ position: "absolute", top: 0, left: "50%", width: "1px", height: "100%", background: "var(--pk-border)", transform: "translateX(-50%)", opacity: 0.4 }} />

        {/* Tag — brutalist pill */}
        <div className="animate-fadeUp" style={{
          display:      "inline-flex",
          alignItems:   "center",
          gap:          "8px",
          padding:      "5px 14px 5px 6px",
          border:       "1.5px solid var(--pk-accent-border)",
          borderRadius: "4px",
          marginBottom: "36px",
          background:   "var(--pk-accent-dim)",
          boxShadow:    "3px 3px 0 0 var(--pk-accent-border)",
        }}>
          <span style={{ background: "var(--pk-accent)", color: "#fff", fontSize: "9px", fontWeight: 900, letterSpacing: "0.1em", padding: "2px 7px", borderRadius: "2px", textTransform: "uppercase" }}>NEW</span>
          <span style={{ fontSize: "12px", color: "var(--pk-accent)", fontWeight: 600, letterSpacing: "0.05em", textTransform: "uppercase" }}>AI-Powered Church Companion</span>
        </div>

        {/* Main title — massive, bold — NO animation on LCP element */}
        <h1 style={{
          fontSize:      "clamp(52px, 9vw, 108px)",
          fontWeight:    800,
          lineHeight:    0.95,
          letterSpacing: "-0.04em",
          margin:        "0 0 6px",
          color:         "var(--pk-text)",
        }}>
          Where beautiful
        </h1>

        {/* Rotating shimmer word */}
        <div className="animate-fadeUp delay-200" style={{
          fontSize:       "clamp(52px, 9vw, 108px)",
          fontWeight:     800,
          lineHeight:     0.95,
          letterSpacing:  "-0.04em",
          margin:         "0 0 6px",
          height:         "1em",
          display:        "flex",
          alignItems:     "center",
          justifyContent: "center",
          overflow:       "hidden",
        }}>
          <span
            className="shimmer-accent"
            style={{
              opacity:    visible ? 1 : 0,
              transform:  visible ? "translateY(0)" : "translateY(16px)",
              transition: "opacity 380ms cubic-bezier(0.22,1,0.36,1), transform 380ms cubic-bezier(0.22,1,0.36,1)",
              display:    "inline-block",
              minWidth:   "clamp(280px,38vw,560px)",
              textAlign:  "center",
            }}
          >
            {ROTATING_WORDS[wordIndex]}
          </span>
        </div>

        {/* "are born." — NOT a second h1; use div styled identically */}
        <div aria-hidden className="animate-fadeUp delay-300" style={{
          fontSize:      "clamp(52px, 9vw, 108px)",
          fontWeight:    800,
          lineHeight:    0.95,
          letterSpacing: "-0.04em",
          margin:        "0 0 40px",
          color:         "var(--pk-text)",
        }}>
          are born.
        </div>

        {/* Subtitle */}
        <p className="animate-fadeUp delay-400" style={{
          fontSize:     "clamp(16px, 1.8vw, 20px)",
          color:        "var(--pk-text-2)",
          maxWidth:     "520px",
          marginInline: "auto",
          lineHeight:   1.7,
          marginBottom: "44px",
          letterSpacing: "-0.01em",
        }}>
          Type a prayer request and get a full prayer back.
          Preach a sermon and see Bible verses on the screen.
          Search any scripture in seconds. Free, forever.
        </p>

        {/* CTAs — brutalist buttons */}
        <div className="animate-fadeUp delay-500" style={{ display: "flex", justifyContent: "center", gap: "12px", flexWrap: "wrap", marginBottom: "72px" }}>
          <Link href="/pray" style={{ textDecoration: "none" }}>
            <div
              style={{
                padding:      "14px 32px",
                background:   "var(--pk-accent)",
                color:        "#fff",
                fontSize:     "15px",
                fontWeight:   800,
                letterSpacing:"-0.01em",
                border:       "2px solid var(--pk-accent)",
                borderRadius: "6px",
                boxShadow:    "4px 4px 0 0 var(--pk-accent-border)",
                transition:   "transform 150ms ease, box-shadow 150ms ease",
              }}
              onMouseEnter={e => {
                (e.currentTarget as HTMLDivElement).style.transform = "translate(-2px,-2px)";
                (e.currentTarget as HTMLDivElement).style.boxShadow = "6px 6px 0 0 var(--pk-accent-border)";
              }}
              onMouseLeave={e => {
                (e.currentTarget as HTMLDivElement).style.transform = "translate(0,0)";
                (e.currentTarget as HTMLDivElement).style.boxShadow = "4px 4px 0 0 var(--pk-accent-border)";
              }}
            >
              ✦ Generate a Prayer
            </div>
          </Link>
          <Link href="/live" style={{ textDecoration: "none" }}>
            <div
              style={{
                padding:      "14px 32px",
                background:   "transparent",
                color:        "var(--pk-text-2)",
                fontSize:     "15px",
                fontWeight:   600,
                letterSpacing:"-0.01em",
                border:       "2px solid var(--pk-border-2)",
                borderRadius: "6px",
                boxShadow:    "4px 4px 0 0 var(--pk-border)",
                transition:   "transform 150ms ease, box-shadow 150ms ease, border-color 150ms ease",
              }}
              onMouseEnter={e => {
                (e.currentTarget as HTMLDivElement).style.transform = "translate(-2px,-2px)";
                (e.currentTarget as HTMLDivElement).style.boxShadow = "6px 6px 0 0 var(--pk-border-2)";
              }}
              onMouseLeave={e => {
                (e.currentTarget as HTMLDivElement).style.transform = "translate(0,0)";
                (e.currentTarget as HTMLDivElement).style.boxShadow = "4px 4px 0 0 var(--pk-border)";
              }}
            >
              🎙 Start Live Sermon
            </div>
          </Link>
        </div>

        {/* Stats row — brutalist data */}
        <div className="animate-fadeUp delay-600 stats-row" style={{
          display:    "inline-flex",
          gap:        "0",
          border:     "1.5px solid var(--pk-border)",
          borderRadius: "8px",
          overflow:   "hidden",
          boxShadow:  "4px 4px 0 0 var(--pk-border)",
          maxWidth:   "100%",
        }}>
          {[
            { val: "11",   label: "Translations"    },
            { val: "Free", label: "Always"          },
            { val: "0",    label: "Account needed"  },
            { val: "66",   label: "Books of the Bible" },
          ].map((s, i) => (
            <div key={s.label} style={{
              padding:    "16px 28px",
              textAlign:  "center",
              borderRight: i < 3 ? "1px solid var(--pk-border)" : "none",
              background: "var(--pk-card)",
            }}>
              <div style={{ fontSize: "clamp(20px,3vw,28px)", fontWeight: 800, color: "var(--pk-accent)", letterSpacing: "-0.03em", lineHeight: 1 }}>{s.val}</div>
              <div style={{ fontSize: "11px", color: "var(--pk-text-3)", marginTop: "4px", letterSpacing: "0.04em", textTransform: "uppercase", fontWeight: 600 }}>{s.label}</div>
            </div>
          ))}
        </div>

      </section>

      {/* ══════════════════════════════════════════
          3 FEATURE CARDS — Glass panels
      ══════════════════════════════════════════ */}
      <section style={{ marginBottom: "80px" }}>

        {/* Brutalist section label */}
        <div style={{ display: "flex", alignItems: "center", gap: "16px", marginBottom: "32px" }}>
          <div style={{ height: "2px", flex: 1, background: "var(--pk-border)" }} />
          <span style={{ fontSize: "11px", fontWeight: 700, color: "var(--pk-text-3)", letterSpacing: "0.12em", textTransform: "uppercase" }}>Everything You Need</span>
          <div style={{ height: "2px", flex: 1, background: "var(--pk-border)" }} />
        </div>

        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(280px,1fr))", gap: "2px", border: "1.5px solid var(--pk-border)", borderRadius: "16px", overflow: "hidden" }}>
          {FEATURES.map((f, i) => (
            <Link key={f.href} href={f.href} style={{ textDecoration: "none" }}>
              <div
                style={{
                  padding:    "36px 28px",
                  background: "var(--pk-card)",
                  backdropFilter: "blur(12px)",
                  borderRight: i < FEATURES.length - 1 ? "1px solid var(--pk-border)" : "none",
                  transition: "background 200ms ease",
                  height:     "100%",
                }}
                onMouseEnter={e => {
                  (e.currentTarget as HTMLDivElement).style.background = `${f.bg}`;
                }}
                onMouseLeave={e => {
                  (e.currentTarget as HTMLDivElement).style.background = "var(--pk-card)";
                }}
              >
                {/* Feature number — brutalist */}
                <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: "24px" }}>
                  <span style={{ fontSize: "10px", fontWeight: 700, color: "var(--pk-text-3)", letterSpacing: "0.12em", border: "1px solid var(--pk-border)", padding: "3px 8px", borderRadius: "3px" }}>
                    0{i + 1}
                  </span>
                  <span style={{ fontSize: "28px", lineHeight: 1 }}>{f.emoji}</span>
                </div>

                <h2 style={{ fontSize: "18px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 10px", letterSpacing: "-0.02em", lineHeight: 1.25 }}>
                  {f.title}
                </h2>
                <p style={{ fontSize: "14px", color: "var(--pk-text-2)", margin: "0 0 28px", lineHeight: 1.65 }}>
                  {f.desc}
                </p>
                <span style={{
                  display:      "inline-flex",
                  alignItems:   "center",
                  gap:          "6px",
                  fontSize:     "13px",
                  fontWeight:   700,
                  color:        f.color,
                  letterSpacing:"-0.01em",
                  border:       `1.5px solid ${f.border}`,
                  padding:      "7px 16px",
                  borderRadius: "4px",
                  boxShadow:    `3px 3px 0 0 ${f.border}`,
                  transition:   "transform 150ms ease, box-shadow 150ms ease",
                }}>
                  Get started →
                </span>
              </div>
            </Link>
          ))}
        </div>
      </section>


      {/* Below-fold sections — cv-auto defers their paint until they scroll into view */}
      <div className="cv-auto"><SermonFeatureSection /></div>
      <div className="cv-auto"><PrayerFeatureSection /></div>
      <div className="cv-auto"><BibleFeatureSection /></div>
      <div className="cv-auto"><FAQ /></div>

    </div>
  );
}

function BibleFeatureSection() {
  return (
    <section style={{ marginTop: "80px" }}>

      {/* Section heading */}
      <div style={{ textAlign: "center", marginBottom: "40px" }}>
        <p style={{ fontSize: "13px", fontWeight: 700, color: "var(--pk-gold)", letterSpacing: "0.08em", textTransform: "uppercase", margin: "0 0 12px" }}>
          FEATURE 03
        </p>
        <h2 style={{ fontSize: "clamp(28px,5vw,48px)", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 12px", letterSpacing: "-0.03em", lineHeight: 1.12 }}>
          #1 Bible Search<br />&amp; Cross-Reference Tool
        </h2>
        <p style={{ fontSize: "17px", color: "var(--pk-text-2)", margin: 0, maxWidth: "480px", marginInline: "auto" }}>
          The smartest way to find any verse, topic, or keyword — with related scriptures included.
        </p>
      </div>

      {/* Card — copy left, mockup right */}
      <div className="feat-grid" style={{
        background:          "var(--pk-gold-dim)",
        border:              "1px solid var(--pk-gold-border)",
        borderRadius:        "28px",
        padding:             "clamp(24px,4vw,40px)",
        display:             "grid",
        gridTemplateColumns: "1fr 1fr",
        gap:                 "48px",
        alignItems:          "center",
      }}>

        {/* Left — copy */}
        <div>
          <h3 style={{ fontSize: "22px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 12px", letterSpacing: "-0.02em" }}>
            Find Any Verse Instantly
          </h3>
          <p style={{ fontSize: "15px", color: "var(--pk-text-2)", lineHeight: 1.75, margin: "0 0 24px" }}>
            Type a reference, keyword, or topic and PrayerKey returns the most relevant verses from across the entire Bible. Click any result to instantly load 4–6 cross-referenced scriptures that share the same theme — perfect for sermon prep, Bible study, or personal devotion.
          </p>

          <div style={{ display: "flex", flexDirection: "column", gap: "10px", marginBottom: "32px" }}>
            {[
              "Search by reference, keyword, topic, or paraphrase",
              "Covers all 66 books of the Bible",
              "Cross-references show deeply related scriptures",
              "Supports 11 major translations including KJV and NIV",
              "Instant results — no waiting, no account needed",
            ].map((item) => (
              <div key={item} style={{ display: "flex", alignItems: "flex-start", gap: "10px" }}>
                <span style={{ color: "var(--pk-gold)", fontWeight: 700, fontSize: "15px", flexShrink: 0, marginTop: "1px" }}>✓</span>
                <span style={{ fontSize: "14px", color: "var(--pk-text-2)", lineHeight: 1.5 }}>{item}</span>
              </div>
            ))}
          </div>

          <a href="/bible" style={{ textDecoration: "none" }}>
            <button style={{
              padding:      "13px 28px",
              borderRadius: "100px",
              border:       "none",
              background:   "var(--pk-gold)",
              color:        "#fff",
              fontSize:     "15px",
              fontWeight:   700,
              cursor:       "pointer",
              boxShadow:    "0 6px 20px var(--pk-gold-dim)",
            }}>
              Start now →
            </button>
          </a>
        </div>

        {/* Right — mockup */}
        <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>

          {/* Search bar */}
          <div style={{ display: "flex", gap: "8px", padding: "10px 16px", background: "var(--pk-card)", border: "1.5px solid var(--pk-gold-border)", borderRadius: "100px", alignItems: "center" }}>
            <span style={{ fontSize: "14px" }}>🔍</span>
            <span style={{ fontSize: "14px", color: "var(--pk-text-2)", flex: 1 }}>do not be afraid</span>
            <span style={{ fontSize: "12px", fontWeight: 700, color: "var(--pk-gold)", background: "var(--pk-gold-dim)", padding: "4px 12px", borderRadius: "100px" }}>Search</span>
          </div>

          {/* Result 1 — selected */}
          <div style={{ padding: "16px", background: "var(--pk-gold-dim)", border: "1.5px solid var(--pk-gold-border)", borderRadius: "14px" }}>
            <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: "6px" }}>
              <span style={{ fontSize: "13px", fontWeight: 700, color: "var(--pk-gold)" }}>Isaiah 41:10</span>
              <span style={{ fontSize: "10px", color: "var(--pk-text-3)", background: "var(--pk-card)", padding: "2px 8px", borderRadius: "100px" }}>Exact match</span>
            </div>
            <p style={{ fontSize: "13px", color: "var(--pk-text)", lineHeight: 1.65, margin: 0 }}>
              &ldquo;So do not fear, for I am with you; do not be dismayed, for I am your God...&rdquo;
            </p>
          </div>

          {/* Result 2 */}
          <div style={{ padding: "14px 16px", background: "var(--pk-card)", border: "1px solid var(--pk-border)", borderRadius: "12px" }}>
            <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: "5px" }}>
              <span style={{ fontSize: "12px", fontWeight: 700, color: "var(--pk-gold)" }}>Joshua 1:9</span>
              <span style={{ fontSize: "10px", color: "var(--pk-text-3)" }}>Related</span>
            </div>
            <p style={{ fontSize: "12px", color: "var(--pk-text-2)", lineHeight: 1.6, margin: 0 }}>
              &ldquo;Be strong and courageous. Do not be afraid; do not be discouraged...&rdquo;
            </p>
          </div>

          {/* Cross-refs panel */}
          <div style={{ padding: "14px 16px", background: "var(--pk-gold-dim)", border: "1px solid var(--pk-gold-border)", borderRadius: "12px" }}>
            <p style={{ fontSize: "11px", fontWeight: 700, color: "var(--pk-gold)", letterSpacing: "0.06em", textTransform: "uppercase", margin: "0 0 10px" }}>
              Related Verses for Isaiah 41:10
            </p>
            <div style={{ display: "flex", flexDirection: "column", gap: "7px" }}>
              {[
                { ref: "Psalm 46:1",        snippet: "God is our refuge and strength, an ever-present help..." },
                { ref: "Romans 8:31",       snippet: "If God is for us, who can be against us?" },
                { ref: "2 Timothy 1:7",     snippet: "For God has not given us a spirit of fear, but of power..." },
              ].map((c) => (
                <div key={c.ref} style={{ display: "flex", gap: "10px", alignItems: "flex-start" }}>
                  <span style={{ fontSize: "11px", fontWeight: 700, color: "var(--pk-gold)", flexShrink: 0, minWidth: "90px" }}>{c.ref}</span>
                  <span style={{ fontSize: "11px", color: "var(--pk-text-3)", lineHeight: 1.5 }}>{c.snippet}</span>
                </div>
              ))}
            </div>
          </div>

        </div>
      </div>
    </section>
  );
}

function PrayerFeatureSection() {
  return (
    <section style={{ marginTop: "80px" }}>

      {/* Section heading */}
      <div style={{ textAlign: "center", marginBottom: "40px" }}>
        <p style={{ fontSize: "13px", fontWeight: 700, color: "var(--pk-purple)", letterSpacing: "0.08em", textTransform: "uppercase", margin: "0 0 12px" }}>
          FEATURE 02
        </p>
        <h2 style={{ fontSize: "clamp(28px,5vw,48px)", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 12px", letterSpacing: "-0.03em", lineHeight: 1.12 }}>
          #1 AI Prayer<br />Generator for Churches
        </h2>
        <p style={{ fontSize: "17px", color: "var(--pk-text-2)", margin: 0, maxWidth: "480px", marginInline: "auto" }}>
          The most personal way to write scripture-grounded prayers in seconds — for any situation.
        </p>
      </div>

      {/* Card — flipped: mockup left, copy right */}
      <div className="feat-grid feat-grid-reverse" style={{
        background:          "var(--pk-purple-dim)",
        border:              "1px solid var(--pk-purple-border)",
        borderRadius:        "28px",
        padding:             "clamp(24px,4vw,40px)",
        display:             "grid",
        gridTemplateColumns: "1fr 1fr",
        gap:                 "48px",
        alignItems:          "center",
      }}>

        {/* Left — mockup */}
        <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>

          {/* Input box */}
          <div style={{ padding: "14px 16px", background: "var(--pk-card)", border: "1.5px solid var(--pk-purple-border)", borderRadius: "14px" }}>
            <p style={{ fontSize: "13px", color: "var(--pk-text-2)", margin: 0, lineHeight: 1.6, fontStyle: "italic" }}>
              &ldquo;Lord, I am anxious about my job interview tomorrow. I need peace and confidence...&rdquo;
            </p>
          </div>

          {/* Mood chips */}
          <div style={{ display: "flex", gap: "6px", flexWrap: "wrap" }}>
            {["Anxious", "Hopeful", "Grateful"].map((m, i) => (
              <span key={m} style={{
                padding: "5px 12px", borderRadius: "100px", fontSize: "12px", fontWeight: 600,
                background: i === 0 ? "var(--pk-purple-dim)" : "var(--pk-card)",
                color: i === 0 ? "var(--pk-purple)" : "var(--pk-text-3)",
                border: i === 0 ? "1px solid var(--pk-purple-border)" : "1px solid var(--pk-border)",
              }}>{m}</span>
            ))}
          </div>

          {/* Generated prayer */}
          <div style={{ padding: "20px", background: "var(--pk-purple-dim)", border: "1.5px solid var(--pk-purple-border)", borderRadius: "16px" }}>
            <p style={{ fontSize: "11px", fontWeight: 700, color: "var(--pk-purple)", letterSpacing: "0.08em", textTransform: "uppercase", margin: "0 0 10px" }}>
              ✨ Generated Prayer
            </p>
            <p style={{ fontSize: "13px", color: "var(--pk-text)", lineHeight: 1.75, margin: "0 0 14px", fontStyle: "italic" }}>
              &ldquo;Heavenly Father, I come before you with a heart full of uncertainty. You know the path laid before me. Grant me the peace that surpasses all understanding, and let your confidence rest upon me...&rdquo;
            </p>

            {/* Verse tag */}
            <div style={{ padding: "8px 12px", background: "var(--pk-gold-dim)", borderRadius: "8px", display: "inline-block" }}>
              <span style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-gold)" }}>Philippians 4:6–7 </span>
              <span style={{ fontSize: "12px", color: "var(--pk-text-3)" }}>· Do not be anxious about anything...</span>
            </div>
          </div>

          {/* Encouragement strip */}
          <div style={{ padding: "12px 16px", background: "var(--pk-card)", borderRadius: "10px", borderLeft: "3px solid var(--pk-purple)" }}>
            <p style={{ fontSize: "12px", color: "var(--pk-text-2)", margin: 0, fontStyle: "italic", lineHeight: 1.6 }}>
              &ldquo;God&apos;s plan for your life is greater than any interview result. Walk in boldly.&rdquo;
            </p>
          </div>

          {/* Copy button */}
          <div style={{ display: "flex", gap: "8px" }}>
            <div style={{ padding: "8px 18px", borderRadius: "100px", background: "var(--pk-card)", border: "1px solid var(--pk-border)", fontSize: "12px", color: "var(--pk-text-2)", cursor: "pointer" }}>
              📋 Copy Prayer
            </div>
            <div style={{ padding: "8px 18px", borderRadius: "100px", background: "var(--pk-card)", border: "1px solid var(--pk-border)", fontSize: "12px", color: "var(--pk-text-2)", cursor: "pointer" }}>
              🔄 Regenerate
            </div>
          </div>
        </div>

        {/* Right — copy */}
        <div>
          <h3 style={{ fontSize: "22px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 12px", letterSpacing: "-0.02em" }}>
            Your Personal Prayer Writer
          </h3>
          <p style={{ fontSize: "15px", color: "var(--pk-text-2)", lineHeight: 1.75, margin: "0 0 24px" }}>
            Tell PrayerKey what you&apos;re going through and it writes a full, heartfelt prayer grounded in scripture — personalised to your exact words, mood, and situation. Every prayer includes relevant Bible verses and an encouragement note.
          </p>

          <div style={{ display: "flex", flexDirection: "column", gap: "10px", marginBottom: "32px" }}>
            {[
              "Works for healing, grief, anxiety, finances, marriage and more",
              "Includes scripture-backed Bible verses automatically",
              "Choose your mood to personalise the tone",
              "Copy and share in seconds",
              "No account, no limit, completely free",
            ].map((item) => (
              <div key={item} style={{ display: "flex", alignItems: "flex-start", gap: "10px" }}>
                <span style={{ color: "var(--pk-purple)", fontWeight: 700, fontSize: "15px", flexShrink: 0, marginTop: "1px" }}>✓</span>
                <span style={{ fontSize: "14px", color: "var(--pk-text-2)", lineHeight: 1.5 }}>{item}</span>
              </div>
            ))}
          </div>

          <a href="/pray" style={{ textDecoration: "none" }}>
            <button style={{
              padding:   "13px 28px",
              borderRadius: "100px",
              border:    "none",
              background: "var(--pk-purple)",
              color:     "#fff",
              fontSize:  "15px",
              fontWeight: 700,
              cursor:    "pointer",
              boxShadow: "0 6px 20px var(--pk-purple-dim)",
            }}>
              Start now →
            </button>
          </a>
        </div>

      </div>
    </section>
  );
}

function SermonFeatureSection() {
  return (
    <section style={{ marginTop: "80px" }}>

      {/* Section heading */}
      <div style={{ textAlign: "center", marginBottom: "40px" }}>
        <p style={{ fontSize: "13px", fontWeight: 700, color: "var(--pk-red)", letterSpacing: "0.08em", textTransform: "uppercase", margin: "0 0 12px" }}>
          FEATURE 01
        </p>
        <h2 style={{ fontSize: "clamp(28px,5vw,48px)", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 12px", letterSpacing: "-0.03em", lineHeight: 1.12 }}>
          #1 Live Sermon<br />Verse Detection Tool
        </h2>
        <p style={{ fontSize: "17px", color: "var(--pk-text-2)", margin: 0, maxWidth: "480px", marginInline: "auto" }}>
          The fastest way to display Bible verses on your projector — automatically, as you preach.
        </p>
      </div>

      {/* Card */}
      <div className="feat-grid" style={{
        background:   "rgba(255,59,48,0.04)",
        border:       "1px solid rgba(255,59,48,0.15)",
        borderRadius: "28px",
        padding:      "clamp(24px,4vw,40px)",
        display:      "grid",
        gridTemplateColumns: "1fr 1fr",
        gap:          "48px",
        alignItems:   "center",
      }}>

        {/* Left — copy */}
        <div>
          <h3 style={{ fontSize: "22px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 12px", letterSpacing: "-0.02em" }}>
            Hands-Free Verse Display
          </h3>
          <p style={{ fontSize: "15px", color: "var(--pk-text-2)", lineHeight: 1.75, margin: "0 0 24px" }}>
            Just preach. PrayerKey listens through your microphone, detects every Bible verse you quote or reference in real time, and displays it on the projector screen for your whole congregation — no operator, no typing, no delay.
          </p>

          <div style={{ display: "flex", flexDirection: "column", gap: "10px", marginBottom: "32px" }}>
            {[
              "Detects verses automatically as you speak",
              "Displays on projector with zero manual input",
              "Works with 11 Bible translations",
              "Shows confidence score for every verse",
              "Pastor controls everything from one screen",
            ].map((item) => (
              <div key={item} style={{ display: "flex", alignItems: "flex-start", gap: "10px" }}>
                <span style={{ color: "var(--pk-red)", fontWeight: 700, fontSize: "15px", flexShrink: 0, marginTop: "1px" }}>✓</span>
                <span style={{ fontSize: "14px", color: "var(--pk-text-2)", lineHeight: 1.5 }}>{item}</span>
              </div>
            ))}
          </div>

          <a href="/live" style={{ textDecoration: "none" }}>
            <button style={{
              padding:      "13px 28px",
              borderRadius: "100px",
              border:       "none",
              background:   "var(--pk-red)",
              color:        "#fff",
              fontSize:     "15px",
              fontWeight:   700,
              cursor:       "pointer",
              boxShadow:    "0 6px 20px rgba(255,59,48,0.25)",
            }}>
              Start now →
            </button>
          </a>
        </div>

        {/* Right — live mockup */}
        <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>

          {/* Status bar */}
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "10px 16px", background: "rgba(255,59,48,0.08)", borderRadius: "12px", border: "1px solid rgba(255,59,48,0.2)" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
              <span style={{ width: "7px", height: "7px", borderRadius: "50%", background: "#FF3B30", display: "inline-block", animation: "pulse 1.5s ease infinite" }} />
              <span style={{ fontSize: "13px", fontWeight: 600, color: "#FF3B30" }}>LIVE — Listening</span>
            </div>
            <span style={{ fontSize: "12px", color: "var(--pk-text-3)" }}>00:14</span>
          </div>

          {/* Transcript */}
          <div style={{ padding: "10px 14px", background: "var(--pk-card)", borderRadius: "10px" }}>
            <p style={{ fontSize: "12px", color: "var(--pk-text-3)", margin: 0, fontStyle: "italic", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
              🎤 &ldquo;...for God so loved the world that he gave his only begotten son...&rdquo;
            </p>
          </div>

          {/* Detected verse card */}
          <div style={{ padding: "20px", background: "var(--pk-gold-dim)", border: "1.5px solid var(--pk-gold-border)", borderRadius: "16px" }}>
            <span style={{ fontSize: "10px", fontWeight: 700, color: "var(--pk-gold)", letterSpacing: "0.08em", textTransform: "uppercase", display: "block", marginBottom: "8px" }}>
              ✦ Just Detected
            </span>
            <p style={{ fontSize: "14px", color: "var(--pk-text)", lineHeight: 1.65, margin: "0 0 12px", fontStyle: "italic" }}>
              &ldquo;For God so loved the world that he gave his one and only Son, that whoever believes in him shall not perish but have eternal life.&rdquo;
            </p>
            <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
              <span style={{ fontSize: "13px", fontWeight: 700, color: "var(--pk-gold)" }}>John 3:16</span>
              <span style={{ fontSize: "11px", color: "var(--pk-text-3)" }}>NIV</span>
              <div style={{ marginLeft: "auto", display: "flex", alignItems: "center", gap: "6px" }}>
                <div style={{ width: "60px", height: "3px", background: "var(--pk-border)", borderRadius: "2px", overflow: "hidden" }}>
                  <div style={{ width: "98%", height: "100%", background: "#34C759", borderRadius: "2px" }} />
                </div>
                <span style={{ fontSize: "11px", color: "#34C759", fontWeight: 600 }}>98%</span>
              </div>
            </div>
          </div>

          {/* Previous verse */}
          <div style={{ padding: "14px 16px", background: "var(--pk-card)", border: "1px solid var(--pk-border)", borderRadius: "12px" }}>
            <p style={{ fontSize: "12px", color: "var(--pk-text-2)", margin: "0 0 6px", fontStyle: "italic" }}>
              &ldquo;I can do all things through Christ who strengthens me.&rdquo;
            </p>
            <div style={{ display: "flex", gap: "8px" }}>
              <span style={{ fontSize: "11px", fontWeight: 600, color: "var(--pk-gold)" }}>Philippians 4:13</span>
              <span style={{ fontSize: "11px", color: "var(--pk-text-3)" }}>· 94%</span>
            </div>
          </div>

          {/* Projector button */}
          <div style={{ padding: "10px 16px", background: "var(--pk-card)", borderRadius: "10px", display: "flex", alignItems: "center", justifyContent: "space-between" }}>
            <span style={{ fontSize: "12px", color: "var(--pk-text-3)" }}>📺 Projector screen open on Display 2</span>
            <span style={{ fontSize: "11px", color: "#34C759", fontWeight: 600 }}>● Connected</span>
          </div>

        </div>
      </div>
    </section>
  );
}

const FAQS = [
  {
    q: "What is PrayerKey?",
    a: "PrayerKey is a free AI-powered church companion that helps pastors, ministers, and believers generate personalised prayers, automatically detect Bible verses during live sermons, and search the entire Bible by keyword, topic, or reference — all without creating an account.",
  },
  {
    q: "How does AI prayer generation work?",
    a: "You type what's on your heart — a worry, a praise, a need — and PrayerKey's AI writes a full, scripture-grounded prayer tailored to your words. It includes relevant Bible verses and an encouragement note. The whole process takes under 10 seconds.",
  },
  {
    q: "Is PrayerKey free to use?",
    a: "Yes. PrayerKey is completely free. There are no subscriptions, no paywalls, and no hidden fees. You can generate prayers, run live sermon sessions, and search the Bible without paying anything.",
  },
  {
    q: "Do I need to create an account or sign in?",
    a: "No. PrayerKey requires no account, no sign-up, and no login. Open the website and start using it immediately. Your privacy is respected — we don't track personal data.",
  },
  {
    q: "What is the Live Sermon feature?",
    a: "The Live Sermon feature listens to the preacher through the microphone and automatically detects Bible verses being quoted or referenced in real time. Detected verses are displayed on a connected projector screen for the congregation to follow along — with no manual typing required.",
  },
  {
    q: "How do I show Bible verses on a projector during a sermon?",
    a: "Start a Live Sermon session, then click 'Open Projector' to open the projector screen in a second browser window or on a second display. As you preach, verses are detected automatically and appear on the projector screen instantly.",
  },
  {
    q: "How accurate is the automatic Bible verse detection?",
    a: "PrayerKey uses advanced AI speech recognition combined with a semantic Bible matching engine. It shows a confidence score (0–100%) for every detected verse. Verses above 90% confidence are highlighted in green. You can set the threshold to only show high-confidence matches.",
  },
  {
    q: "What Bible translations does PrayerKey support?",
    a: "PrayerKey supports 11 major Bible translations including NIV, KJV, ESV, NKJV, NLT, AMP, CSB, NASB, MSG, CEV, and GNT. You can switch translations at any time during a live session.",
  },
  {
    q: "Can I use PrayerKey on my phone or tablet?",
    a: "Yes. PrayerKey is a web application that works on any modern browser — iPhone, Android, tablet, laptop, or desktop. No app download is needed. Simply open the website in your browser.",
  },
  {
    q: "What denominations is PrayerKey for?",
    a: "PrayerKey is denomination-neutral. It is designed to serve all Christian traditions — Catholic, Protestant, Pentecostal, Baptist, Anglican, Methodist, non-denominational, and more. The AI draws from the full canon of scripture without theological bias.",
  },
  {
    q: "Can PrayerKey write prayers for specific situations?",
    a: "Yes. PrayerKey generates prayers for healing, anxiety, grief, marriage, finances, thanksgiving, guidance, protection, strength, forgiveness, and any other situation you describe. The more detail you provide, the more personal and relevant the prayer will be.",
  },
  {
    q: "How does Bible search work?",
    a: "Type a verse reference like 'John 3:16', a keyword like 'faith', or a topic like 'do not fear' into the search box. PrayerKey returns the most relevant Bible verses instantly. You can also click any result to see related cross-reference verses.",
  },
  {
    q: "What are cross-references in PrayerKey?",
    a: "Cross-references are verses from different parts of the Bible that share the same theme, doctrine, or wording as a verse you are reading. PrayerKey automatically shows 4–6 of the strongest cross-references for any verse you look up, helping you study scripture more deeply.",
  },
  {
    q: "Can I copy and share the prayers PrayerKey generates?",
    a: "Yes. Every generated prayer has a 'Copy Prayer' button. You can paste it into a message, bulletin, social media post, or read it aloud during a service. The prayers are yours to use freely.",
  },
  {
    q: "Does PrayerKey work without an internet connection?",
    a: "PrayerKey requires an internet connection for AI prayer generation, live sermon verse detection, and Bible search — since these features use cloud-based AI models. However, the website itself loads quickly on any standard connection including mobile data.",
  },
  {
    q: "How many people can use PrayerKey during a live church service?",
    a: "There is no limit. The pastor controls the live session from one device, and the projector screen can be opened on any number of displays simultaneously. Congregation members can also follow along on their own phones by visiting the projector link.",
  },
  {
    q: "Is PrayerKey suitable for home churches and small groups?",
    a: "Absolutely. PrayerKey works just as well for a home Bible study of 5 people as it does for a church of 5,000. There are no minimum size requirements. The live sermon and prayer features scale to any gathering.",
  },
  {
    q: "What language are the prayers generated in?",
    a: "PrayerKey currently generates prayers in English. The quality and tone reflect the input you provide — so if you write your request in a formal style, the prayer will match. Support for additional languages is planned for future updates.",
  },
  {
    q: "How is PrayerKey different from a regular Bible app?",
    a: "Standard Bible apps let you read and search scripture. PrayerKey goes further — it actively listens during sermons and auto-displays verses on a projector, writes original AI prayers from your personal requests, and connects Bible search with intelligent cross-references. It is a live ministry tool, not just a reading app.",
  },
  {
    q: "Is PrayerKey better than PewBeam?",
    a: "PrayerKey and PewBeam both display Bible verses during sermons, but PrayerKey goes significantly further. PewBeam focuses on manual verse display — a pastor or operator selects verses to push to the screen. PrayerKey does this automatically: it listens to the sermon and detects verses in real time with no manual input. On top of that, PrayerKey adds AI prayer generation, full Bible search with cross-references, 11 translations, and a live projector designer — all completely free with no account required. For churches that want a hands-free, all-in-one tool, PrayerKey is the stronger choice.",
  },
  {
    q: "Can I use PrayerKey for funerals, weddings, or special church ceremonies?",
    a: "Yes. PrayerKey is ideal for any faith ceremony — not just Sunday services. For funerals, the prayer generator can write a compassionate prayer of comfort and remembrance in seconds. For weddings, it can compose a prayer of blessing over the couple. For baptisms, dedications, or special services, just describe the occasion and PrayerKey tailors the prayer to it. The live sermon feature also works during any spoken ceremony where scripture is referenced.",
  },
  {
    q: "Who built PrayerKey?",
    a: "PrayerKey was built by Collins Omoikhudu Asein to give every pastor and church access to AI tools that were previously only available to large, well-resourced ministries. It is an independent platform focused on making technology serve the church — not the other way around.",
  },
];

function FAQ() {
  return (
    <section style={{ marginTop: "80px", textAlign: "left" }}>

      {/* Section header */}
      <div style={{ textAlign: "center", marginBottom: "48px" }}>
        <h2 style={{
          fontSize:      "clamp(26px, 4vw, 40px)",
          fontWeight:    700,
          color:         "var(--pk-text)",
          margin:        "0 0 12px",
          letterSpacing: "-0.02em",
          lineHeight:    1.2,
        }}>
          Frequently Asked Questions
        </h2>
        <p style={{ fontSize: "17px", color: "var(--pk-text-2)", margin: 0 }}>
          Your questions, answered.
        </p>
      </div>

      {/* Two-column grid */}
      <div style={{
        display:             "grid",
        gridTemplateColumns: "repeat(auto-fit, minmax(420px, 1fr))",
        gap:                 "2px",
      }}>
        {FAQS.map((f, i) => (
          <FAQItem key={i} q={f.q} a={f.a} />
        ))}
      </div>
    </section>
  );
}

function FAQItem({ q, a }: { q: string; a: string }) {
  const [open, setOpen] = useState(false);
  return (
    <div
      style={{
        borderBottom:  "1px solid var(--pk-border)",
        padding:       "0",
      }}
    >
      <button
        onClick={() => setOpen((v) => !v)}
        style={{
          width:          "100%",
          textAlign:      "left",
          background:     "none",
          border:         "none",
          cursor:         "pointer",
          padding:        "22px 4px",
          display:        "flex",
          alignItems:     "center",
          justifyContent: "space-between",
          gap:            "16px",
        }}
      >
        <span style={{
          fontSize:      "15px",
          fontWeight:    600,
          color:         "var(--pk-text)",
          lineHeight:    1.4,
          letterSpacing: "-0.01em",
        }}>
          {q}
        </span>
        <span style={{
          fontSize:   "18px",
          color:      "var(--pk-gold)",
          flexShrink: 0,
          transform:  open ? "rotate(45deg)" : "rotate(0deg)",
          transition: "transform 220ms ease",
          lineHeight: 1,
        }}>
          +
        </span>
      </button>

      {open && (
        <p style={{
          fontSize:    "14px",
          color:       "var(--pk-text-2)",
          lineHeight:  1.75,
          margin:      "0",
          padding:     "0 4px 22px",
          animation:   "faqOpen 220ms ease",
        }}>
          {a}
        </p>
      )}
    </div>
  );
}
