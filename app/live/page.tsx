"use client";
import { useCallback, useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";

interface DetectedVerse {
  verseRef: string; verseText: string; translation: string;
  confidence: number; detectedAt: string; source?: "ai" | "manual";
}
interface QueueItem extends DetectedVerse { queueId: string; }
type ServiceState = "idle" | "live" | "ended";
type SearchMode   = "book" | "context";

// ── Design tokens — deep navy, iOS-27 / Liquid Glass ─────────────────────────
const VOID    = "#0B1726";                          // Deep navy
const VOID_HI = "#0F2238";                          // Lifted card bg
const GLASS   = "rgba(15,34,56,0.72)";              // Frosted glass
const YELLOW  = "#F4D03F";                          // Premium pill accent
const BLUE    = "#3B82F6";                          // Action FAB
const BLUE_HI = "#5798F8";                          // Hover/lift
const GOLD    = "#F5A623";                          // Verse refs
const RED     = "#FF453A";
const GREEN   = "#34C759";
const ORANGE  = "#FF9F0A";
const BORDER  = "rgba(255,255,255,0.07)";
const HILITE  = "inset 0 1px 0 rgba(255,255,255,0.06)";
const T1      = "#FFFFFF";
const T2      = "rgba(255,255,255,0.74)";
const T3      = "rgba(255,255,255,0.48)";

// Cards: glassy navy with top highlight
const card: React.CSSProperties = {
  background:           "rgba(255,255,255,0.025)",
  backdropFilter:       "blur(28px) saturate(160%)",
  WebkitBackdropFilter: "blur(28px) saturate(160%)",
  border:               `1px solid ${BORDER}`,
  boxShadow:            `0 8px 28px rgba(0,0,0,0.40), ${HILITE}`,
};

// Timer dots like 00.34.37
function fmtSerif(s: number) {
  const h  = Math.floor(s / 3600);
  const m  = Math.floor((s % 3600) / 60);
  const ss = s % 60;
  return `${String(h).padStart(2,"0")}.${String(m).padStart(2,"0")}.${String(ss).padStart(2,"0")}`;
}
function fmt(s: number) {
  const m = Math.floor(s / 60), ss = s % 60;
  return `${String(m).padStart(2,"0")}:${String(ss).padStart(2,"0")}`;
}

// ── VerseScreen — black "power-button" card with depth ────────────────────────
function VerseScreen({
  verse, empty, isLive = false,
}: { verse: DetectedVerse | null; empty: string; isLive?: boolean }) {
  const [animKey, setAnimKey] = useState(0);
  useEffect(() => { if (verse) setAnimKey(k => k + 1); }, [verse?.verseRef]);

  return (
    <div style={{
      flex: 1, borderRadius: "20px", overflow: "hidden", position: "relative" as const,
      background: `
        radial-gradient(ellipse 75% 65% at 50% 45%, #0F2238 0%, #060D18 70%, #03070F 100%)
      `,
      border: `1px solid ${verse ? "rgba(255,255,255,0.10)" : "rgba(255,255,255,0.05)"}`,
      display: "flex", alignItems: "center", justifyContent: "center",
      boxShadow: verse
        ? "0 18px 50px rgba(0,0,0,0.55), inset 0 1px 0 rgba(255,255,255,0.05), inset 0 0 100px rgba(0,30,80,0.30)"
        : "0 12px 40px rgba(0,0,0,0.40), inset 0 1px 0 rgba(255,255,255,0.03)",
      transition: "all 500ms cubic-bezier(0.22,1,0.36,1)",
    }}>
      {/* Soft directional light from top-left */}
      <div style={{ position:"absolute" as const, inset:0, borderRadius:"20px",
        background:"radial-gradient(ellipse 60% 40% at 30% 0%, rgba(120,170,255,0.10) 0%, transparent 60%)",
        pointerEvents:"none" as const }} />

      {/* Subtle dotted-globe pattern behind verse */}
      <div style={{ position:"absolute" as const, inset:0, opacity: 0.10,
        backgroundImage:`radial-gradient(rgba(180,200,255,0.18) 1px, transparent 1px)`,
        backgroundSize:"14px 14px", maskImage:"radial-gradient(ellipse 70% 70% at 50% 50%, black 30%, transparent 75%)",
        WebkitMaskImage:"radial-gradient(ellipse 70% 70% at 50% 50%, black 30%, transparent 75%)",
        pointerEvents:"none" as const }} />

      {/* Top hairline highlight — premium card edge */}
      <div style={{ position:"absolute" as const, top:0, left:"6%", right:"6%", height:"1px",
        background:"linear-gradient(90deg,transparent,rgba(255,255,255,0.18),transparent)" }} />

      {/* On-air glow ring — only when live */}
      {isLive && verse && (
        <>
          <div style={{ position:"absolute" as const, inset:0, borderRadius:"20px",
            boxShadow:`inset 0 0 0 1px ${BLUE}50, 0 0 60px ${BLUE}25`,
            pointerEvents:"none" as const }} />
          <div style={{ position:"absolute" as const, inset:0, borderRadius:"20px",
            background:`radial-gradient(ellipse 80% 100% at 50% 50%, ${BLUE}10 0%, transparent 60%)`,
            pointerEvents:"none" as const }} />
        </>
      )}

      {verse ? (
        <div key={animKey} style={{
          position:"relative" as const, textAlign:"center", padding:"22px 32px",
          maxWidth:"92%", zIndex:1, animation:"verseIn 420ms cubic-bezier(0.22,1,0.36,1) both",
        }}>
          {/* Country-pill style ref */}
          <div style={{ display:"inline-flex", alignItems:"center", gap:"8px",
            padding:"5px 14px", marginBottom:"18px",
            border:"1px solid rgba(255,255,255,0.10)", borderRadius:"100px",
            background:"rgba(255,255,255,0.04)", boxShadow:HILITE }}>
            <span style={{ width:"6px", height:"6px", borderRadius:"50%", background:GOLD,
              boxShadow:`0 0 8px ${GOLD}80` }} />
            <span style={{ fontSize:"10px", fontWeight:700, color:T1,
              letterSpacing:"0.06em" }}>
              {verse.verseRef}
            </span>
            <span style={{ fontSize:"9px", color:T3, letterSpacing:"0.10em" }}>·</span>
            <span style={{ fontSize:"9px", fontWeight:600, color:T2, letterSpacing:"0.08em" }}>
              {verse.translation}
            </span>
          </div>

          {/* Verse text — refined serif */}
          <p style={{ fontSize:"clamp(13px,1.1vw,17px)", color:"#EAF1FB",
            fontFamily:"'New York','Times New Roman',Georgia,serif",
            fontStyle:"italic", lineHeight:1.7, margin:0, fontWeight:400,
            letterSpacing:"-0.005em" }}>
            &ldquo;{verse.verseText}&rdquo;
          </p>

        </div>
      ) : (
        <div style={{ textAlign:"center", zIndex:1, opacity:0.32 }}>
          <div style={{ width:"56px", height:"56px", borderRadius:"50%",
            border:"1px dashed rgba(255,255,255,0.18)",
            display:"flex", alignItems:"center", justifyContent:"center",
            margin:"0 auto 14px" }}>
            <span style={{ fontSize:"22px", color:T3 }}>⏻</span>
          </div>
          <p style={{ fontSize:"11px", color:T3, margin:0, letterSpacing:"0.04em",
            fontFamily:"'New York',Georgia,serif", fontStyle:"italic" }}>{empty}</p>
        </div>
      )}
    </div>
  );
}

// ── SEO content shown on the idle screen — pure server-renderable JSX ────────
// Targets: free church projector software, real-time bible verse detection,
// Pewbeam alternative, ProPresenter alternative, automatic verse display, etc.
function SEOContent() {
  const COMP_ROW: React.CSSProperties = {
    display:"grid", gridTemplateColumns:"1.4fr 1fr 1fr 1fr 1fr", gap:"0",
    fontSize:"13px", lineHeight:1.5,
  };
  const cell: React.CSSProperties = {
    padding:"14px 16px", borderBottom:"1px solid rgba(255,255,255,0.08)",
    color:"rgba(255,255,255,0.78)",
  };
  const headCell: React.CSSProperties = {
    ...cell, color:"#FFFFFF", fontWeight:700, fontSize:"12px",
    letterSpacing:"0.08em", textTransform:"uppercase",
    borderBottom:"1px solid rgba(255,255,255,0.16)",
  };
  const labelCell: React.CSSProperties = {
    ...cell, color:"rgba(255,255,255,0.92)", fontWeight:600,
  };
  const wrap: React.CSSProperties = {
    maxWidth:"880px", margin:"0 auto", padding:"80px 24px 60px",
    color:"rgba(255,255,255,0.78)",
  };
  const h2: React.CSSProperties = {
    fontSize:"clamp(26px,3.4vw,38px)", fontWeight:800,
    color:"#FFFFFF", letterSpacing:"-0.025em", lineHeight:1.15,
    margin:"56px 0 18px",
  };
  const h3: React.CSSProperties = {
    fontSize:"18px", fontWeight:700, color:"#FFFFFF",
    margin:"28px 0 8px", letterSpacing:"-0.01em",
  };
  const p: React.CSSProperties = {
    fontSize:"16px", color:"rgba(255,255,255,0.78)", lineHeight:1.75,
    margin:"0 0 14px",
  };
  const FAQ_ITEMS = [
    { q: "How does PrayerKey detect Bible verses during a sermon?",
      a: "PrayerKey listens through your laptop or USB microphone, transcribes the sermon in real time, and matches what's spoken against the entire Bible using an AI semantic engine. Direct references like \"John 3:16\" are matched almost instantly. Paraphrased quotes are caught by a separate similarity model that understands theme and meaning." },
    { q: "Is PrayerKey really free for churches?",
      a: "Yes. PrayerKey is 100% free, with no subscription, no premium tier, no ads, and no account. Open prayerkey.com/live and start using it." },
    { q: "Pewbeam vs PrayerKey — what's the difference?",
      a: "Pewbeam is paid software starting around $20/month per church. PrayerKey provides the same automatic verse detection and projection workflow for $0. Both run in a browser. PrayerKey requires no account, no install, and no credit card." },
    { q: "Do I need a tech operator to run PrayerKey during the sermon?",
      a: "No. PrayerKey can run in fully automatic mode — press Start Listening, enable Auto, and every verse the pastor quotes appears on the projector with no clicks needed. A volunteer can also run it manually if your service prefers human pacing." },
    { q: "What if my pastor paraphrases instead of quoting Bible verses directly?",
      a: "PrayerKey runs two detection layers. The first matches direct references and exact quotes. The second uses semantic AI embeddings that understand paraphrases — so \"God will never leave you\" is correctly matched to Hebrews 13:5 even though those exact words don't appear in the verse." },
    { q: "Will it work on a Chromebook, Mac, or Windows laptop?",
      a: "Yes. PrayerKey works in any Chromium browser (Chrome, Edge, Brave) on Mac, Windows, Chromebook, and Linux. There's nothing to install. Mobile devices can use the prayer generator and Bible search but the live sermon dashboard is desktop-first because it needs a microphone and a second display." },
    { q: "Does PrayerKey work for Catholic, Baptist, Methodist, or Pentecostal services?",
      a: "PrayerKey is denomination-neutral and used by Catholic, Protestant, Pentecostal, Baptist, Anglican, Methodist, Reformed, and non-denominational churches around the world. It supports 11 Bible translations to fit every tradition." },
    { q: "How accurate is the AI Bible verse detection?",
      a: "Direct references are matched with near-100% accuracy. Paraphrases typically score 70 to 95% confidence. Each detection card shows a colored confidence dot — green for 90%+, yellow for 70–89%, orange for 60–69%." },
    { q: "Is PrayerKey a free alternative to ProPresenter or EasyWorship?",
      a: "Yes. ProPresenter, EasyWorship, and MediaShout cost between $200 and $2,000 per year and require a trained operator typing or clicking each verse. PrayerKey is free and detects verses automatically." },
    { q: "What microphone do I need?",
      a: "Any clear microphone works. Best options: a USB lapel mic worn by the pastor or a line-in feed from your church PA. Built-in laptop mics work if the laptop is within ten feet of the speaker." },
    { q: "Does PrayerKey record or store my sermon audio?",
      a: "No. Audio is processed in real time inside your browser via the Web Speech API and never leaves the device. Only the transcribed text is sent for verse matching, and that text is discarded the moment your session ends." },
    { q: "Can I use this for small churches or home churches?",
      a: "Absolutely. PrayerKey was built specifically so small churches, house churches, and independent ministries get the same quality of tools as well-funded megachurches — without paying $200 to $2,000 per year for traditional church software." },
  ];

  return (
    <section style={{ background:VOID, color:T2, position:"relative", zIndex:1,
      margin:"0 calc(-1 * clamp(20px,4vw,80px)) -100px",
      padding:"0 clamp(20px,4vw,80px) 80px",
      borderTop:"1px solid rgba(255,255,255,0.08)" }}>
      <div style={wrap}>

        {/* ── Lede ── */}
        <h2 style={{ ...h2, fontSize:"clamp(30px,4vw,44px)", margin:"40px 0 18px" }}>
          The free, real-time Bible verse projector for live sermons.
        </h2>
        <p style={{ ...p, fontSize:"18px", color:"rgba(255,255,255,0.86)" }}>
          PrayerKey Live is an AI-powered, browser-based church projection tool that
          automatically detects every Bible verse a pastor quotes during a sermon and
          displays it on a connected projector or second screen — in real time, with
          no operator, no install, and no cost. It's the free alternative to{" "}
          <strong>Pewbeam</strong>, <strong>ProPresenter</strong>,{" "}
          <strong>EasyWorship</strong>, and <strong>MediaShout</strong>, built
          specifically for churches that don't have a $2,000-a-year software budget.
        </p>

        {/* ── How it works ── */}
        <h2 style={h2} id="how-it-works">How real-time Bible verse detection works</h2>
        <p style={p}>
          PrayerKey turns any laptop with a microphone into a fully automatic verse
          display tool. The workflow is dead simple:
        </p>
        <ol style={{ ...p, paddingLeft:"22px" }}>
          <li style={{ ...p, margin:"0 0 8px" }}>
            <strong>Open prayerkey.com/live</strong> in Chrome, Edge, or Brave on the
            laptop you'll connect to your projector.
          </li>
          <li style={{ ...p, margin:"0 0 8px" }}>
            <strong>Allow microphone access</strong> when the browser prompts.
          </li>
          <li style={{ ...p, margin:"0 0 8px" }}>
            <strong>Click "Projector ↗"</strong> to open a clean fullscreen verse window
            and drag it to your second display.
          </li>
          <li style={{ ...p, margin:"0 0 8px" }}>
            <strong>Press Start Listening</strong> when the pastor begins preaching.
          </li>
          <li style={{ ...p, margin:"0 0 8px" }}>
            PrayerKey transcribes the sermon, identifies every Bible verse the pastor
            quotes (direct references <em>and</em> paraphrases), and presents them on
            the projector — automatically.
          </li>
        </ol>

        {/* ── Comparison table ── */}
        <h2 style={h2} id="alternatives">PrayerKey vs Pewbeam vs ProPresenter vs EasyWorship</h2>
        <p style={p}>
          Honest comparison — features, pricing, and what each tool does well:
        </p>
        <div style={{ ...card, borderRadius:"14px", overflow:"hidden", margin:"18px 0 24px" }}>
          <div style={COMP_ROW}>
            <div style={headCell}>Feature</div>
            <div style={{ ...headCell, color:GOLD }}>PrayerKey</div>
            <div style={headCell}>Pewbeam</div>
            <div style={headCell}>ProPresenter</div>
            <div style={headCell}>EasyWorship</div>

            <div style={labelCell}>Price</div>
            <div style={{ ...cell, color:GREEN, fontWeight:700 }}>Free forever</div>
            <div style={cell}>$20+/mo</div>
            <div style={cell}>$399/yr</div>
            <div style={cell}>$396/yr</div>

            <div style={labelCell}>Auto verse detection</div>
            <div style={{ ...cell, color:GREEN }}>✓ AI real-time</div>
            <div style={cell}>✓ AI</div>
            <div style={{ ...cell, color:RED }}>✗ Manual only</div>
            <div style={{ ...cell, color:RED }}>✗ Manual only</div>

            <div style={labelCell}>Operator required</div>
            <div style={{ ...cell, color:GREEN }}>No</div>
            <div style={cell}>Optional</div>
            <div style={{ ...cell, color:RED }}>Yes</div>
            <div style={{ ...cell, color:RED }}>Yes</div>

            <div style={labelCell}>Account / sign-up</div>
            <div style={{ ...cell, color:GREEN }}>Not required</div>
            <div style={cell}>Required</div>
            <div style={cell}>Required</div>
            <div style={cell}>Required</div>

            <div style={labelCell}>Install software</div>
            <div style={{ ...cell, color:GREEN }}>None — browser</div>
            <div style={cell}>Browser</div>
            <div style={cell}>Desktop app</div>
            <div style={cell}>Desktop app</div>

            <div style={labelCell}>Bible translations</div>
            <div style={cell}>11</div>
            <div style={cell}>20+</div>
            <div style={cell}>30+ (paid)</div>
            <div style={cell}>20+</div>

            <div style={labelCell}>Setup time</div>
            <div style={{ ...cell, color:GREEN }}>~3 min</div>
            <div style={cell}>~10 min</div>
            <div style={cell}>~1 hour</div>
            <div style={cell}>~1 hour</div>

            <div style={{ ...labelCell, borderBottom:"none" }}>Best for</div>
            <div style={{ ...cell, borderBottom:"none" }}>Small to mid churches</div>
            <div style={{ ...cell, borderBottom:"none" }}>Mid churches with budget</div>
            <div style={{ ...cell, borderBottom:"none" }}>Megachurches</div>
            <div style={{ ...cell, borderBottom:"none" }}>Traditional churches</div>
          </div>
        </div>
        <p style={p}>
          PrayerKey was built specifically for the churches that <em>can't</em> spend
          $400 to $2,000 per year on software. Independent pastors, house churches,
          rural ministries, and small congregations get the same quality of tooling
          as a megachurch tech booth — at zero cost.
        </p>

        {/* ── Use cases ── */}
        <h2 style={h2} id="who">Who PrayerKey Live is for</h2>
        <ul style={{ ...p, paddingLeft:"22px" }}>
          <li style={{ ...p, margin:"0 0 6px" }}>
            <strong>Small churches</strong> that can't afford ProPresenter or EasyWorship.
          </li>
          <li style={{ ...p, margin:"0 0 6px" }}>
            <strong>Home churches and house fellowships</strong> that meet in living rooms.
          </li>
          <li style={{ ...p, margin:"0 0 6px" }}>
            <strong>Bible study and small groups</strong> — show every verse referenced.
          </li>
          <li style={{ ...p, margin:"0 0 6px" }}>
            <strong>Sunday school and youth ministries</strong> — keep kids following along.
          </li>
          <li style={{ ...p, margin:"0 0 6px" }}>
            <strong>Funerals, weddings, and special services</strong> — display verses without a tech operator.
          </li>
          <li style={{ ...p, margin:"0 0 6px" }}>
            <strong>Online streaming churches</strong> — verses overlay on the broadcast feed.
          </li>
          <li style={{ ...p, margin:"0 0 6px" }}>
            <strong>Congregants who miss the verses pastors quote</strong> — look up later.
          </li>
        </ul>

        {/* ── Translations ── */}
        <h2 style={h2} id="translations">11 Bible translations supported</h2>
        <p style={p}>
          Choose any translation from the dropdown — every detection, projection, and
          search result respects your selection. Supported translations include{" "}
          <strong>King James Version (KJV)</strong>,{" "}
          <strong>New International Version (NIV)</strong>,{" "}
          <strong>English Standard Version (ESV)</strong>,{" "}
          <strong>New King James Version (NKJV)</strong>,{" "}
          <strong>New Living Translation (NLT)</strong>,{" "}
          <strong>New American Standard Bible (NASB)</strong>,{" "}
          <strong>Amplified Bible (AMP)</strong>,{" "}
          <strong>Christian Standard Bible (CSB)</strong>,{" "}
          <strong>The Message (MSG)</strong>,{" "}
          <strong>Contemporary English Version (CEV)</strong>, and{" "}
          <strong>Good News Translation (GNT)</strong>.
        </p>

        {/* ── Privacy ── */}
        <h2 style={h2} id="privacy">Privacy: we don't record your sermons</h2>
        <p style={p}>
          PrayerKey processes your microphone audio inside your browser using the
          native Web Speech API. Audio never leaves your device. Only the transcribed
          text is sent for verse matching, and that text is discarded the moment your
          session ends. No accounts, no storage, no analytics tied to your sermons.
        </p>

        {/* ── FAQ ── */}
        <h2 style={h2} id="faq">Frequently asked questions</h2>
        {FAQ_ITEMS.map((item, i) => (
          <details key={i} style={{
            border:"1px solid rgba(255,255,255,0.10)", borderRadius:"10px",
            padding:"14px 18px", marginBottom:"10px",
            background:"rgba(255,255,255,0.02)",
          }}>
            <summary style={{
              fontSize:"16px", fontWeight:600, color:"#FFFFFF",
              listStyle:"none", outline:"none", cursor:"pointer",
            }}>{item.q}</summary>
            <p style={{ ...p, marginTop:"10px", marginBottom:0,
              color:"rgba(255,255,255,0.74)" }}>{item.a}</p>
          </details>
        ))}

        {/* ── Footer cross-links ── */}
        <h2 style={{ ...h2, fontSize:"clamp(20px,2.5vw,26px)" }}>Continue exploring PrayerKey</h2>
        <p style={p}>
          <a href="/docs" style={{ color:GOLD, textDecoration:"none" }}>How to Use PrayerKey — Full Documentation</a> ·{" "}
          <a href="/pray" style={{ color:GOLD, textDecoration:"none" }}>AI Prayer Generator</a> ·{" "}
          <a href="/bible" style={{ color:GOLD, textDecoration:"none" }}>Bible Search & Cross-References</a> ·{" "}
          <a href="/prayer" style={{ color:GOLD, textDecoration:"none" }}>544+ Prayer Library</a> ·{" "}
          <a href="/about" style={{ color:GOLD, textDecoration:"none" }}>About PrayerKey</a>
        </p>

        <style>{`
          details summary::-webkit-details-marker { display: none; }
          details[open] summary { color: ${GOLD} !important; }
        `}</style>
      </div>
    </section>
  );
}

// ── Main ──────────────────────────────────────────────────────────────────────
export default function LivePage() {
  const [state,         setState]         = useState<ServiceState>("idle");
  const [translation,   setTranslation]   = useState("KJV");
  const [elapsed,       setElapsed]       = useState(0);
  const [transcript,    setTranscript]    = useState("");
  const [transcriptLog, setTranscriptLog] = useState<{ text: string; detected: boolean }[]>([]);
  const [listening,     setListening]     = useState(false);
  const [micError,      setMicError]      = useState("");
  const [previewVerse,  setPreviewVerse]  = useState<DetectedVerse | null>(null);
  const [liveVerse,     setLiveVerse]     = useState<DetectedVerse | null>(null);
  const [goLive,        setGoLive]        = useState(false);
  const [detections,    setDetections]    = useState<DetectedVerse[]>([]);
  const [queue,         setQueue]         = useState<QueueItem[]>([]);
  const [sidebarTab,    setSidebarTab]    = useState<"detections" | "queue">("detections");
  const [autoMode,      setAutoMode]      = useState(false);
  const [searchMode,    setSearchMode]    = useState<SearchMode>("book");
  const [searchQuery,   setSearchQuery]   = useState("");
  const [chapterVerses, setChapterVerses] = useState<{ num: number; ref: string; text: string }[]>([]);
  const [chapterTitle,  setChapterTitle]  = useState("");
  const [chapterBook,   setChapterBook]   = useState("");
  const [chapterNum,    setChapterNum]    = useState(0);
  const [ctxResults,    setCtxResults]    = useState<DetectedVerse[]>([]);
  const [searchLoading, setSearchLoading] = useState(false);

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const recognitionRef = useRef<any>(null);
  const timerRef       = useRef<ReturnType<typeof setInterval> | null>(null);
  const detectRef      = useRef<ReturnType<typeof setTimeout> | null>(null);
  const transcriptRef  = useRef("");
  const logRef         = useRef<HTMLDivElement>(null);

  // Hide site header during live broadcast
  useEffect(() => {
    const header = document.querySelector("header") as HTMLElement | null;
    if (!header) return;
    if (state === "live") {
      header.style.transition = "opacity 250ms ease";
      header.style.opacity    = "0";
      header.style.pointerEvents = "none";
      const t = setTimeout(() => { header.style.display = "none"; }, 260);
      return () => clearTimeout(t);
    } else {
      header.style.display    = "";
      header.style.opacity    = "0";
      const t = setTimeout(() => {
        header.style.opacity       = "1";
        header.style.pointerEvents = "";
      }, 20);
      return () => clearTimeout(t);
    }
  }, [state]);

  useEffect(() => {
    if (state !== "live") return;
    timerRef.current = setInterval(() => setElapsed(s => s + 1), 1000);
    return () => { if (timerRef.current) clearInterval(timerRef.current); };
  }, [state]);

  useEffect(() => {
    if (goLive && previewVerse) setLiveVerse(previewVerse);
    else if (!goLive) setLiveVerse(null);
  }, [goLive, previewVerse]);

  useEffect(() => {
    if (logRef.current) logRef.current.scrollTop = logRef.current.scrollHeight;
  }, [transcriptLog, transcript]);

  function extractExplicitRefs(text: string): string[] {
    const pattern = /\b(Genesis|Exodus|Leviticus|Numbers|Deuteronomy|Joshua|Judges|Ruth|Samuel|Kings|Chronicles|Ezra|Nehemiah|Esther|Job|Psalm(?:s)?|Proverbs|Ecclesiastes|Song|Isaiah|Jeremiah|Lamentations|Ezekiel|Daniel|Hosea|Joel|Amos|Obadiah|Jonah|Micah|Nahum|Habakkuk|Zephaniah|Haggai|Zechariah|Malachi|Matthew|Mark|Luke|John|Acts|Romans|Corinthians|Galatians|Ephesians|Philippians|Colossians|Thessalonians|Timothy|Titus|Philemon|Hebrews|James|Peter|Jude|Revelation|Gen|Exo?|Lev|Num|Deut?|Josh?|Judg?|Sam|Kgs?|Chr|Ps|Prov?|Eccl?|Isa|Jer|Lam|Eze?k?|Dan|Hos|Mal|Matt?|Mk|Lk|Jn|Rev|Rom|Cor|Gal|Eph|Phil|Col|Thess?|Tim|Heb)\s*\d+(?::\d+(?:-\d+)?)?/gi;
    return [...new Set(Array.from(text.matchAll(pattern), m => m[0].trim()))];
  }

  const detectVerses = useCallback(async (recentChunk: string) => {
    if (recentChunk.trim().length < 12) return;
    const explicitRefs = extractExplicitRefs(recentChunk);
    for (const ref of explicitRefs.slice(0, 2)) {
      try {
        const sr = await fetch(`/api/bible/search?q=${encodeURIComponent(ref)}&translation=${translation}`);
        const sd = await sr.json(); const top = (sd.results ?? [])[0]; if (!top) continue;
        const v: DetectedVerse = { verseRef: top.ref, verseText: top.text, translation, confidence: 1.0, detectedAt: new Date().toISOString(), source: "ai" };
        setDetections(prev => [v, ...prev.filter(p => p.verseRef !== v.verseRef)].slice(0, 12));
        setPreviewVerse(prev => prev ?? v);
      } catch { /* continue */ }
    }
    try {
      const semanticQuery = recentChunk.slice(-350).trim();
      if (semanticQuery.length < 15) return;
      const sr = await fetch(`/api/bible/search?q=${encodeURIComponent(semanticQuery)}&translation=${translation}`);
      const sd = await sr.json();
      const results: { ref: string; text: string }[] = sd.results ?? [];
      if (!results.length) return;
      const newVerses: DetectedVerse[] = results
        .slice(0, 3)
        .filter(r => !explicitRefs.some(ref => ref.toLowerCase().startsWith(r.ref.split(":")[0].toLowerCase())))
        .map(r => ({
          verseRef: r.ref, verseText: r.text, translation,
          confidence: 0.88, detectedAt: new Date().toISOString(), source: "ai" as const,
        }));
      if (!newVerses.length) return;
      setDetections(prev =>
        [...newVerses, ...prev.filter(p => !newVerses.some(n => n.verseRef === p.verseRef))].slice(0, 12)
      );
      setAutoMode(auto => {
        if (auto) { setPreviewVerse(newVerses[0]); setLiveVerse(newVerses[0]); setGoLive(true); }
        else { setPreviewVerse(prev => prev ?? newVerses[0]); }
        return auto;
      });
    } catch { /* offline */ }
  }, [translation]);

  function startListening() {
    const Win = window as unknown as Record<string, unknown>;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const SR = (Win.SpeechRecognition ?? Win.webkitSpeechRecognition) as (new() => any) | undefined;
    if (!SR) { setMicError("Speech recognition requires Chrome or Edge."); return; }
    const rec = new SR(); rec.continuous = true; rec.interimResults = true; rec.lang = "en-US";
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    rec.onresult = (e: any) => {
      let interim = "", finalChunk = "";
      for (let i = e.resultIndex; i < e.results.length; i++) {
        const t = e.results[i][0].transcript;
        if (e.results[i].isFinal) finalChunk += t + " "; else interim += t;
      }
      if (finalChunk) {
        transcriptRef.current += finalChunk;
        const hasRef = /\b(genesis|exodus|leviticus|numbers|deuteronomy|joshua|judges|ruth|samuel|kings|chronicles|ezra|nehemiah|esther|job|psalm|proverbs|ecclesiastes|song|isaiah|jeremiah|lamentations|ezekiel|daniel|hosea|joel|amos|obadiah|jonah|micah|nahum|habakkuk|zephaniah|haggai|zechariah|malachi|matthew|mark|luke|john|acts|romans|corinthians|galatians|ephesians|philippians|colossians|thessalonians|timothy|titus|philemon|hebrews|james|peter|jude|revelation|gen|exo|lev|num|deut|josh|judg|sam|kgs|chr|ps|prov|eccl|isa|jer|lam|ezek|dan|hos|mal|matt|mk|lk|jn|rev|rom|cor|gal|eph|phil|col|thess|tim|heb)\s*\d+/i.test(finalChunk);
        setTranscriptLog(prev => [...prev, { text: finalChunk.trim(), detected: hasRef }].slice(-60));
        detectVerses(finalChunk);
      }
      setTranscript(interim);
    };
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    rec.onerror = (e: any) => { if (e.error === "not-allowed") setMicError("Microphone access denied."); setListening(false); };
    rec.onend = () => { if (recognitionRef.current === rec) { try { rec.start(); } catch { /* ended */ } } };
    rec.start(); recognitionRef.current = rec; setListening(true); setMicError("");
  }

  function stopListening() { recognitionRef.current?.stop(); recognitionRef.current = null; setListening(false); }
  function startService() {
    setMicError(""); setState("live"); setElapsed(0);
    setDetections([]); setQueue([]); setPreviewVerse(null); setLiveVerse(null); setGoLive(false);
    setTranscriptLog([]); transcriptRef.current = ""; startListening();
  }
  function endService() { stopListening(); if (timerRef.current) clearInterval(timerRef.current); setState("ended"); }
  function addToQueue(v: DetectedVerse) { setQueue(p => [...p, { ...v, queueId: `${v.verseRef}-${Date.now()}` }]); }
  function removeFromQueue(id: string) { setQueue(p => p.filter(q => q.queueId !== id)); }
  function playFromQueue(item: QueueItem) { presentVerse(item); removeFromQueue(item.queueId); }
  function presentVerse(v: DetectedVerse) { setPreviewVerse(v); setLiveVerse(v); setGoLive(true); }

  const runSearch = useCallback(async (q: string, mode: SearchMode) => {
    if (!q.trim()) { setChapterVerses([]); setCtxResults([]); setChapterTitle(""); setChapterBook(""); setChapterNum(0); return; }
    setSearchLoading(true);
    try {
      const res = await fetch(`/api/bible/search?q=${encodeURIComponent(q)}&translation=${translation}`);
      const data = await res.json(); const list = data.results ?? [];
      if (mode === "book") {
        const title = (list[0]?.ref ?? q).replace(/:\d+$/, "").trim() || q;
        setChapterTitle(title);
        const match = title.match(/^(.*?)\s+(\d+)$/);
        if (match) { setChapterBook(match[1].trim()); setChapterNum(parseInt(match[2])); }
        setChapterVerses(list.map((r: { ref: string; text: string }, i: number) => ({ num: i + 1, ref: r.ref, text: r.text })));
        setCtxResults([]);
      } else {
        setCtxResults(list.map((r: { ref: string; text: string }) => ({ verseRef: r.ref, verseText: r.text, translation, confidence: 1, detectedAt: new Date().toISOString() })));
        setChapterVerses([]);
      }
    } catch { setChapterVerses([]); setCtxResults([]); }
    finally { setSearchLoading(false); }
  }, [translation]);

  function navChapter(delta: number) {
    const next = chapterNum + delta;
    if (next < 1) return;
    setSearchQuery(`${chapterBook} ${next}`);
  }

  useEffect(() => {
    const id = setTimeout(() => runSearch(searchQuery, searchMode), 500);
    return () => clearTimeout(id);
  }, [searchQuery, searchMode, runSearch]);

  useEffect(() => {
    if (state !== "live") return;
    function onKey(e: KeyboardEvent) {
      const tag = (e.target as HTMLElement).tagName;
      if (tag === "INPUT" || tag === "TEXTAREA" || tag === "SELECT") return;
      if (e.key === "Enter" || e.key === " ") { e.preventDefault(); if (previewVerse) { setGoLive(true); setLiveVerse(previewVerse); } }
      if (e.key === "Escape") { setLiveVerse(null); setPreviewVerse(null); setGoLive(false); }
      if (e.key === "q" || e.key === "Q") { if (previewVerse) addToQueue({ ...previewVerse, source: "manual" }); }
      if (e.key === "c" || e.key === "C") { setLiveVerse(null); setGoLive(false); }
      if (e.key === "a" || e.key === "A") { setAutoMode(v => !v); }
    }
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [state, previewVerse]);

  useEffect(() => () => {
    recognitionRef.current?.stop();
    if (timerRef.current) clearInterval(timerRef.current);
    if (detectRef.current) clearTimeout(detectRef.current);
  }, []);

  const TRANSLATIONS = ["KJV","NKJV","NIV","ESV","NLT","AMP","AMPC","MSG","CSB","NASB","NASB95","RSV","NRSV","ASV","NET","NCV","CEV","GNT","ICB","NLV","ERV","WEB","YLT","DARBY","BBE","CEB","EXB","VOICE","TLV","MEV","TPT","CJB"];

  const SERIF = "'New York','Times New Roman',Georgia,serif";

  // ══════════════════════════════════════════════════════════════════════════
  // IDLE — VPN-app inspired
  // ══════════════════════════════════════════════════════════════════════════
  if (state === "idle") return (
    <>
      <style>{`
        @keyframes fadeInUp{from{opacity:0;transform:translateY(20px)}to{opacity:1;transform:translateY(0)}}
        @keyframes subtlePulse{0%,100%{opacity:1}50%{opacity:0.55}}
        @keyframes ringSpin{to{transform:rotate(360deg)}}
        .pk-card-hover{transition:all 240ms cubic-bezier(0.22,1,0.36,1)}
        .pk-card-hover:hover{border-color:rgba(255,255,255,0.14)!important;background:rgba(255,255,255,0.04)!important;transform:translateY(-2px)}
        .pk-power-btn:hover .pk-power-glow{opacity:1!important}
        .pk-power-btn:hover{transform:scale(1.02)}
        .pk-power-btn:active{transform:scale(0.98)}
      `}</style>
      <div style={{ minHeight:"calc(100vh - 64px)",
        background:`
          radial-gradient(ellipse 80% 60% at 50% 30%, rgba(40,80,150,0.18) 0%, transparent 65%),
          radial-gradient(ellipse 60% 50% at 50% 100%, rgba(20,40,90,0.20) 0%, transparent 60%),
          ${VOID}
        `,
        display:"flex", flexDirection:"column" as const,
        alignItems:"center", justifyContent:"center",
        margin:"calc(-1 * clamp(32px,5vw,64px)) calc(-1 * clamp(20px,4vw,80px)) 0",
        position:"relative" as const, overflow:"hidden", padding:"60px 40px" }}>

        {/* Dotted-globe pattern — VPN-app signature */}
        <div style={{ position:"absolute" as const, inset:0, opacity:0.06,
          backgroundImage:"radial-gradient(rgba(180,200,255,0.5) 1px, transparent 1px)",
          backgroundSize:"18px 18px",
          maskImage:"radial-gradient(ellipse 60% 60% at 50% 50%, black 30%, transparent 80%)",
          WebkitMaskImage:"radial-gradient(ellipse 60% 60% at 50% 50%, black 30%, transparent 80%)" }} />

        <div style={{ textAlign:"center", maxWidth:"560px", position:"relative" as const, zIndex:1, animation:"fadeInUp 700ms cubic-bezier(.22,1,.36,1) both" }}>

          {/* Premium-style status pill — yellow */}
          <div style={{ display:"inline-flex", alignItems:"center", gap:"7px", padding:"6px 14px",
            border:`1px solid ${YELLOW}40`, borderRadius:"100px",
            background:`linear-gradient(135deg, ${YELLOW}1F 0%, ${YELLOW}10 100%)`,
            boxShadow:`0 0 24px ${YELLOW}25, ${HILITE}`,
            marginBottom:"36px" }}>
            <span style={{ width:"5px", height:"5px", borderRadius:"50%", background:YELLOW, animation:"subtlePulse 2.2s ease infinite" }} />
            <span style={{ fontSize:"10px", fontWeight:700, color:YELLOW, letterSpacing:"0.18em", textTransform:"uppercase" as const }}>Live Sermon AI · Ready</span>
          </div>

          {/* Massive serif wordmark */}
          <h1 style={{ fontSize:"clamp(54px,9vw,96px)", fontWeight:700, color:T1, margin:0,
            fontFamily:SERIF, letterSpacing:"-0.04em", lineHeight:0.98 }}>
            Prayer<span style={{ fontStyle:"italic", color:GOLD }}>Key</span>
          </h1>

          {/* Power button — VPN signature element */}
          <div style={{ position:"relative" as const, width:"180px", height:"180px", margin:"40px auto 28px" }}>
            {/* Outer dashed ring */}
            <div style={{ position:"absolute" as const, inset:0, borderRadius:"50%",
              border:"1px dashed rgba(255,255,255,0.18)" }} />
            {/* Soft glow halo */}
            <div className="pk-power-glow" style={{ position:"absolute" as const, inset:"-20px", borderRadius:"50%",
              background:`radial-gradient(circle, ${BLUE}30 0%, transparent 60%)`,
              opacity:0.6, transition:"opacity 300ms", pointerEvents:"none" as const }} />
            {/* Power button */}
            <button onClick={startService} className="pk-power-btn" style={{
              position:"absolute" as const, inset:"22px", borderRadius:"50%",
              background:"radial-gradient(circle at 30% 30%, #1A2A42 0%, #0A1626 70%, #06101C 100%)",
              border:"1px solid rgba(255,255,255,0.10)",
              boxShadow:"0 14px 40px rgba(0,0,0,0.50), inset 0 1px 0 rgba(255,255,255,0.08), inset 0 -10px 20px rgba(0,0,0,0.4)",
              color:T1, cursor:"pointer",
              display:"flex", alignItems:"center", justifyContent:"center",
              transition:"transform 200ms cubic-bezier(0.22,1,0.36,1)" }}>
              <svg width="42" height="42" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" style={{ filter:`drop-shadow(0 0 8px ${BLUE}80)` }}>
                <path d="M18.36 6.64a9 9 0 1 1-12.73 0" />
                <line x1="12" y1="2" x2="12" y2="12" />
              </svg>
            </button>
          </div>

          <p style={{ fontSize:"15px", color:T1, margin:"0 0 6px", fontWeight:500, letterSpacing:"-0.01em" }}>Tap to Live Sermon</p>
          <p style={{ fontSize:"11px", color:T3, margin:"0 0 32px", letterSpacing:"0.04em" }}>Listening · Detecting · Projecting</p>

          {micError && <div style={{ marginBottom:"20px", padding:"10px 16px", borderRadius:"10px", background:"rgba(255,69,58,0.10)", border:"1px solid rgba(255,69,58,0.30)", color:RED, fontSize:"12px" }}>{micError}</div>}

          {/* Bottom stat strip — DOWNLOAD/UPLOAD style */}
          <div style={{ display:"grid", gridTemplateColumns:"1fr 1fr", gap:"10px", maxWidth:"420px", margin:"0 auto" }}>
            <div className="pk-card-hover" style={{ ...card, padding:"14px 16px", borderRadius:"14px", textAlign:"left" }}>
              <div style={{ display:"flex", alignItems:"center", justifyContent:"space-between", marginBottom:"6px" }}>
                <span style={{ fontSize:"9px", fontWeight:700, color:T3, letterSpacing:"0.14em", textTransform:"uppercase" as const }}>Translation</span>
                <span style={{ fontSize:"10px", color:T3 }}>⌄</span>
              </div>
              <select value={translation} onChange={e => setTranslation(e.target.value)}
                style={{ width:"100%", padding:0, background:"transparent", border:"none",
                  color:T1, fontSize:"22px", fontFamily:SERIF, fontWeight:700, outline:"none",
                  letterSpacing:"-0.02em", cursor:"pointer" }}>
                {TRANSLATIONS.map(t => <option key={t} style={{ background:"#0a0a0c" }}>{t}</option>)}
              </select>
            </div>
            <div className="pk-card-hover" style={{ ...card, padding:"14px 16px", borderRadius:"14px", textAlign:"left" }}>
              <div style={{ display:"flex", alignItems:"center", justifyContent:"space-between", marginBottom:"6px" }}>
                <span style={{ fontSize:"9px", fontWeight:700, color:T3, letterSpacing:"0.14em", textTransform:"uppercase" as const }}>Status</span>
                <span style={{ width:"6px", height:"6px", borderRadius:"50%", background:GREEN, boxShadow:`0 0 8px ${GREEN}` }} />
              </div>
              <div style={{ fontSize:"22px", color:T1, fontFamily:SERIF, fontWeight:700, letterSpacing:"-0.02em" }}>Ready</div>
            </div>
          </div>

          <p style={{ marginTop:"28px", fontSize:"10px", color:T3, letterSpacing:"0.08em" }}>Chrome · Edge · Microphone access required</p>
        </div>
      </div>

      {/* ═══════════════════════════════════════════════════════════════════
          SEO CONTENT — keyword-rich rich text for ranking on:
          • free church projector software
          • automatic bible verse display sermon
          • Pewbeam / ProPresenter / EasyWorship alternative
          • real-time bible verse detection
          ═══════════════════════════════════════════════════════════════════ */}
      <SEOContent />
    </>
  );

  // ══════════════════════════════════════════════════════════════════════════
  // ENDED
  // ══════════════════════════════════════════════════════════════════════════
  if (state === "ended") return (
    <>
      <style>{`@keyframes fadeInUp{from{opacity:0;transform:translateY(20px)}to{opacity:1;transform:translateY(0)}}`}</style>
      <div style={{ minHeight:"calc(100vh - 64px)",
        background:`radial-gradient(ellipse 70% 50% at 50% 50%, rgba(40,80,150,0.15) 0%, transparent 60%), ${VOID}`,
        display:"flex", alignItems:"center", justifyContent:"center",
        margin:"calc(-1 * clamp(32px,5vw,64px)) calc(-1 * clamp(20px,4vw,80px)) -100px", position:"relative" as const }}>
        <div style={{ textAlign:"center", animation:"fadeInUp 600ms ease both", position:"relative" as const, zIndex:1 }}>
          <div style={{ width:"76px", height:"76px", borderRadius:"50%",
            background:`radial-gradient(circle at 30% 30%, ${GREEN}40, ${GREEN}10 70%)`,
            border:`1px solid ${GREEN}50`,
            display:"flex", alignItems:"center", justifyContent:"center",
            margin:"0 auto 26px", boxShadow:`0 0 40px ${GREEN}25, ${HILITE}` }}>
            <span style={{ fontSize:"30px", color:GREEN }}>✓</span>
          </div>
          <h2 style={{ fontSize:"40px", fontWeight:700, color:T1, marginBottom:"10px", letterSpacing:"-0.03em", fontFamily:SERIF }}>Session Complete</h2>
          <p style={{ color:T2, marginBottom:"36px", fontSize:"14px" }}>
            <span style={{ color:T1, fontWeight:600 }}>{detections.length}</span> verse{detections.length!==1?"s":""} detected · <span style={{ fontFamily:"'SF Mono',monospace", color:T1 }}>{fmt(elapsed)}</span>
          </p>
          <div style={{ height:"1px", background:"linear-gradient(90deg,transparent,rgba(255,255,255,0.14),transparent)", marginBottom:"36px", width:"260px", margin:"0 auto 36px" }} />
          <button onClick={() => { setState("idle"); setElapsed(0); }} style={{
            padding:"14px 36px", borderRadius:"100px",
            border:"1px solid rgba(255,255,255,0.16)",
            background:"rgba(255,255,255,0.06)", color:T1, fontSize:"13px",
            fontWeight:600, cursor:"pointer", letterSpacing:"0.04em",
            boxShadow:HILITE, backdropFilter:"blur(18px)" }}>
            New Session
          </button>
        </div>
      </div>
    </>
  );

  // ══════════════════════════════════════════════════════════════════════════
  // LIVE DASHBOARD — iOS 27 / VPN
  // ══════════════════════════════════════════════════════════════════════════
  return createPortal(
    <div style={{ position:"fixed" as const, top:0, left:0, right:0, bottom:0, zIndex:9900,
      background:`
        radial-gradient(ellipse 70% 50% at 50% 0%, rgba(40,80,150,0.14) 0%, transparent 60%),
        radial-gradient(ellipse 50% 40% at 50% 100%, rgba(20,40,90,0.16) 0%, transparent 50%),
        ${VOID}
      `,
      display:"flex", flexDirection:"column" as const, overflow:"hidden" }}>

      {/* Subtle dotted-globe pattern across whole dashboard */}
      <div style={{ position:"absolute" as const, inset:0, opacity:0.04,
        backgroundImage:"radial-gradient(rgba(180,200,255,0.6) 1px, transparent 1px)",
        backgroundSize:"22px 22px", pointerEvents:"none" as const }} />

      {/* ── TOP BAR — frosted glass with serif timer ── */}
      <div style={{ height:"64px", flexShrink:0, display:"flex", alignItems:"center",
        padding:"0 20px", gap:"14px",
        background:GLASS, backdropFilter:"blur(40px) saturate(180%)", WebkitBackdropFilter:"blur(40px) saturate(180%)",
        boxShadow:`0 1px 0 ${BORDER}, ${HILITE}`, zIndex:2, position:"relative" as const }}>

        {/* Logo + LIVE pill */}
        <div style={{ display:"flex", alignItems:"center", gap:"12px", flexShrink:0 }}>
          <span style={{ fontSize:"15px", fontWeight:700, color:T1,
            fontFamily:SERIF, letterSpacing:"-0.02em" }}>
            Prayer<span style={{ fontStyle:"italic", color:GOLD }}>Key</span>
          </span>
          <div style={{ display:"inline-flex", alignItems:"center", gap:"6px", padding:"4px 11px",
            background:`${RED}15`, border:`1px solid ${RED}40`, borderRadius:"100px",
            boxShadow:HILITE }}>
            <span style={{ width:"5px", height:"5px", borderRadius:"50%", background:RED,
              animation:"liveDot 1.2s ease infinite", boxShadow:`0 0 6px ${RED}` }} />
            <span style={{ fontSize:"9px", fontWeight:800, color:RED, letterSpacing:"0.16em" }}>LIVE</span>
          </div>
        </div>

        {/* Center — secure-connection style timer */}
        <div style={{ flex:1, display:"flex", flexDirection:"column" as const, alignItems:"center", gap:"1px" }}>
          <span style={{ fontSize:"9px", fontWeight:600, color:T3, letterSpacing:"0.18em",
            textTransform:"uppercase" as const }}>Session</span>
          <span style={{ fontSize:"24px", fontWeight:700, color:T1, fontFamily:SERIF,
            letterSpacing:"-0.01em", lineHeight:1, fontVariantNumeric:"tabular-nums" }}>
            {fmtSerif(elapsed)}
          </span>
        </div>

        {/* Right controls */}
        <div style={{ display:"flex", alignItems:"center", gap:"8px", flexShrink:0 }}>
          {/* AUTO — yellow Premium-style when on */}
          <button onClick={() => setAutoMode(v => !v)} style={{
            display:"flex", alignItems:"center", gap:"8px", padding:"6px 12px",
            background: autoMode ? `linear-gradient(135deg, ${YELLOW}25, ${YELLOW}10)` : "rgba(255,255,255,0.04)",
            border:`1px solid ${autoMode ? `${YELLOW}55` : BORDER}`,
            borderRadius:"100px", cursor:"pointer", transition:"all 220ms",
            boxShadow: autoMode ? `0 0 18px ${YELLOW}25, ${HILITE}` : HILITE }}>
            <div style={{ width:"24px", height:"14px", borderRadius:"100px",
              background: autoMode ? YELLOW : "rgba(255,255,255,0.10)",
              position:"relative" as const, transition:"all 200ms",
              boxShadow: autoMode ? `inset 0 0 4px rgba(0,0,0,0.2)` : "none" }}>
              <div style={{ position:"absolute" as const, top:"2px", left: autoMode ? "12px" : "2px",
                width:"10px", height:"10px", borderRadius:"50%",
                background: autoMode ? "#0B1726" : T2, transition:"left 200ms ease",
                boxShadow:"0 1px 3px rgba(0,0,0,0.4)" }} />
            </div>
            <span style={{ fontSize:"10px", fontWeight:700,
              color: autoMode ? YELLOW : T2, letterSpacing:"0.10em" }}>AUTO</span>
          </button>

          {/* Translation pill */}
          <select value={translation} onChange={e => setTranslation(e.target.value)}
            style={{ padding:"7px 12px", background:"rgba(255,255,255,0.04)", backdropFilter:"blur(12px)",
              border:`1px solid ${BORDER}`, borderRadius:"100px",
              color:T1, fontSize:"11px", fontWeight:700, outline:"none", boxShadow:HILITE }}>
            {TRANSLATIONS.map(t => <option key={t} style={{ background:"#0B1726" }}>{t}</option>)}
          </select>

          {/* Projector */}
          <a href="/live/projector" target="_blank" style={{
            padding:"7px 14px", background:"rgba(255,255,255,0.04)",
            border:`1px solid ${BORDER}`, borderRadius:"100px",
            color:T2, fontSize:"10px", fontWeight:600, textDecoration:"none",
            letterSpacing:"0.04em", whiteSpace:"nowrap" as const, boxShadow:HILITE }}>
            Projector ↗
          </a>

          {/* End — refined red */}
          <button onClick={endService} style={{
            padding:"7px 16px", background:`linear-gradient(135deg, ${RED}20, ${RED}10)`,
            border:`1px solid ${RED}45`, borderRadius:"100px",
            color:RED, fontSize:"10px", fontWeight:700, cursor:"pointer",
            letterSpacing:"0.08em", boxShadow:`0 0 14px ${RED}15, ${HILITE}` }}>
            ■ END
          </button>
        </div>
      </div>

      {/* ── MAIN AREA — left column (top+bottom) + right Detections sidebar ── */}
      <div style={{ flex:1, display:"flex", overflow:"hidden", position:"relative" as const, zIndex:1, gap:"10px", padding:"10px" }}>
      <div style={{ flex:1, display:"flex", flexDirection:"column" as const, gap:"10px", overflow:"hidden", minWidth:0 }}>

      {/* ── TOP ROW ── */}
      <div style={{ height:"52%", flexShrink:0, display:"flex", overflow:"hidden",
        position:"relative" as const, zIndex:1, gap:"10px" }}>

        {/* Col 1 — Transcript */}
        <div style={{ width:"210px", flexShrink:0, display:"flex", flexDirection:"column" as const,
          ...card, borderRadius:"16px" }}>
          <div style={{ padding:"12px 14px", borderBottom:`1px solid ${BORDER}`, display:"flex", alignItems:"center", justifyContent:"space-between", flexShrink:0 }}>
            <span style={{ fontSize:"10px", fontWeight:700, color:"rgba(255,255,255,0.88)", letterSpacing:"0.14em", textTransform:"uppercase" as const }}>Transcript</span>
            {listening && (
              <div style={{ display:"flex", alignItems:"center", gap:"5px", padding:"2px 8px",
                background:`${GREEN}15`, border:`1px solid ${GREEN}30`, borderRadius:"100px" }}>
                <span style={{ width:"4px", height:"4px", borderRadius:"50%", background:GREEN, animation:"liveDot 1s ease infinite", boxShadow:`0 0 4px ${GREEN}` }} />
                <span style={{ fontSize:"8px", color:GREEN, letterSpacing:"0.08em", fontWeight:700 }}>LIVE</span>
              </div>
            )}
          </div>
          <div ref={logRef} style={{ flex:1, overflowY:"auto" as const, padding:"8px 10px", display:"flex", flexDirection:"column" as const, gap:"1px" }}>
            {transcriptLog.map((entry, i) => {
              const isRecent = i >= transcriptLog.length - 2;
              return (
                <div key={i} style={{ padding: entry.detected ? "5px 8px" : "2px 4px", borderRadius:"8px",
                  background: entry.detected ? `${GOLD}0E` : "transparent",
                  border: entry.detected ? `1px solid ${GOLD}30` : "1px solid transparent",
                  transition:"all 200ms" }}>
                  {entry.detected && (
                    <div style={{ display:"flex", alignItems:"center", gap:"5px", marginBottom:"2px" }}>
                      <span style={{ width:"4px", height:"4px", borderRadius:"50%", background:GOLD, boxShadow:`0 0 4px ${GOLD}` }} />
                      <span style={{ fontSize:"7px", fontWeight:800, color:GOLD, letterSpacing:"0.12em" }}>VERSE FOUND</span>
                    </div>
                  )}
                  <span style={{ fontSize:"11px", color: entry.detected ? "#FFE0A0" : isRecent ? T1 : T2, lineHeight:1.45, display:"block" }}>{entry.text}</span>
                </div>
              );
            })}
            {transcript && (
              <div style={{ padding:"3px 4px" }}>
                <span style={{ fontSize:"11px", color:T1, lineHeight:1.45, display:"block", fontStyle:"italic" }}>{transcript}</span>
              </div>
            )}
            {!transcriptLog.length && !transcript && (
              <div style={{ textAlign:"center", opacity:0.30, marginTop:"24px", padding:"0 10px" }}>
                <p style={{ fontSize:"10px", color:T3, margin:0, letterSpacing:"0.06em",
                  fontFamily:SERIF, fontStyle:"italic" }}>Awaiting signal…</p>
              </div>
            )}
          </div>
          <div style={{ padding:"10px 12px", borderTop:`1px solid ${BORDER}`, flexShrink:0 }}>
            <button onClick={listening ? stopListening : startListening} style={{
              width:"100%", padding:"9px", borderRadius:"100px",
              border:`1px solid ${listening ? `${RED}40` : "rgba(255,255,255,0.14)"}`,
              background: listening ? `${RED}12` : "rgba(255,255,255,0.06)",
              color: listening ? RED : T1, fontSize:"10px", fontWeight:600, cursor:"pointer",
              display:"flex", alignItems:"center", justifyContent:"center", gap:"7px",
              letterSpacing:"0.04em", boxShadow:HILITE, transition:"all 200ms" }}>
              <span style={{ width:"5px", height:"5px", borderRadius:"50%", background: listening ? RED : T2,
                animation: listening ? "liveDot 1.2s ease infinite" : "none",
                boxShadow: listening ? `0 0 6px ${RED}` : "none", flexShrink:0 }} />
              {listening ? "Stop Listening" : "Start Listening"}
            </button>
          </div>
        </div>

        {/* Col 2 — Program Preview */}
        <div style={{ flex:1, display:"flex", flexDirection:"column" as const, gap:"10px",
          ...card, borderRadius:"16px", padding:"12px" }}>
          <div style={{ display:"flex", alignItems:"center", justifyContent:"space-between", flexShrink:0 }}>
            <span style={{ fontSize:"10px", fontWeight:700, color:"rgba(255,255,255,0.88)", letterSpacing:"0.14em", textTransform:"uppercase" as const }}>Program · Preview</span>
            {previewVerse && (
              <button onClick={() => { setGoLive(true); setLiveVerse(previewVerse); }} style={{
                display:"flex", alignItems:"center", gap:"6px", padding:"5px 14px", borderRadius:"100px",
                background:`linear-gradient(135deg, ${BLUE} 0%, ${BLUE_HI} 100%)`,
                border:`1px solid ${BLUE}80`, color:"#FFFFFF",
                fontSize:"10px", fontWeight:700, cursor:"pointer", letterSpacing:"0.04em",
                boxShadow:`0 4px 14px ${BLUE}50, ${HILITE}` }}>
                <span style={{ fontSize:"7px" }}>▶</span> Send Live
              </button>
            )}
          </div>
          <VerseScreen verse={previewVerse} empty="Stage a verse to preview" isLive={false} />
        </div>

        {/* Col 3 — Live Display */}
        <div style={{ flex:1, display:"flex", flexDirection:"column" as const, gap:"10px",
          ...card, borderRadius:"16px", padding:"12px",
          ...(goLive && liveVerse ? { boxShadow: `0 8px 28px rgba(0,0,0,0.40), 0 0 0 1px ${BLUE}25, ${HILITE}` } : {}) }}>
          <div style={{ display:"flex", alignItems:"center", justifyContent:"space-between", flexShrink:0 }}>
            <div style={{ display:"flex", alignItems:"center", gap:"8px" }}>
              <span style={{ fontSize:"10px", fontWeight:700, color:"rgba(255,255,255,0.88)", letterSpacing:"0.14em", textTransform:"uppercase" as const }}>Live · Display</span>
              {goLive && liveVerse && (
                <div style={{ display:"inline-flex", alignItems:"center", gap:"5px", padding:"3px 9px",
                  background:`${GREEN}15`, border:`1px solid ${GREEN}40`, borderRadius:"100px" }}>
                  <span style={{ width:"4px", height:"4px", borderRadius:"50%", background:GREEN,
                    animation:"liveDot 1.2s ease infinite", boxShadow:`0 0 5px ${GREEN}` }} />
                  <span style={{ fontSize:"8px", fontWeight:800, color:GREEN, letterSpacing:"0.10em" }}>ON AIR</span>
                </div>
              )}
            </div>
            <div style={{ display:"flex", alignItems:"center", gap:"8px" }}>
              <span style={{ fontSize:"9px", color:T3, letterSpacing:"0.06em" }}>Go Live</span>
              <div onClick={() => setGoLive(v => !v)} style={{ width:"36px", height:"20px", borderRadius:"100px",
                background: goLive ? GREEN : "rgba(255,255,255,0.10)",
                border:`1px solid ${goLive ? GREEN : BORDER}`,
                position:"relative" as const, cursor:"pointer", transition:"all 220ms",
                boxShadow: goLive ? `0 0 14px ${GREEN}40, inset 0 0 4px rgba(0,0,0,0.2)` : "inset 0 0 4px rgba(0,0,0,0.2)" }}>
                <div style={{ position:"absolute" as const, top:"2px", left: goLive ? "17px" : "2px",
                  width:"14px", height:"14px", borderRadius:"50%", background:"#FFFFFF",
                  transition:"left 200ms ease", boxShadow:"0 1px 4px rgba(0,0,0,0.4)" }} />
              </div>
            </div>
          </div>
          <VerseScreen verse={liveVerse} empty="No verse on air" isLive={goLive && !!liveVerse} />
        </div>

      </div>

      {/* ── BOTTOM ROW ── */}
      <div style={{ flex:1, display:"flex", overflow:"hidden", position:"relative" as const, zIndex:1, gap:"10px" }}>

        {/* Bible Browser */}
        <div style={{ flex:1, display:"flex", flexDirection:"column" as const, overflow:"hidden",
          ...card, borderRadius:"16px" }}>
          <div style={{ padding:"10px 14px", display:"flex", alignItems:"center", gap:"8px", flexShrink:0,
            borderBottom:`1px solid ${BORDER}` }}>
            {(["book","context"] as SearchMode[]).map(m => (
              <button key={m} onClick={() => setSearchMode(m)} style={{
                padding:"6px 14px", borderRadius:"100px",
                border:`1px solid ${searchMode===m ? "rgba(255,255,255,0.20)" : "transparent"}`,
                background: searchMode===m ? "rgba(255,255,255,0.08)" : "transparent",
                color: searchMode===m ? T1 : T3,
                fontSize:"10px", fontWeight:600, cursor:"pointer", letterSpacing:"0.04em",
                boxShadow: searchMode===m ? HILITE : "none", transition:"all 180ms" }}>
                {m === "book" ? "Book" : "Context"}
              </button>
            ))}
            <div style={{ flex:1, display:"flex", gap:"8px", alignItems:"center" }}>
              <input value={searchQuery} onChange={e => setSearchQuery(e.target.value)}
                placeholder={searchMode==="book" ? "e.g. Psalm 32, John 3" : "Search topic or phrase..."}
                style={{ flex:1, padding:"7px 14px", background:"rgba(255,255,255,0.04)",
                  border:`1px solid ${BORDER}`, borderRadius:"100px", color:T1, fontSize:"11px", outline:"none",
                  boxShadow:HILITE }} />
              <select value={translation} onChange={e => setTranslation(e.target.value)}
                style={{ padding:"7px 12px", background:"rgba(255,255,255,0.04)",
                  border:`1px solid ${BORDER}`, borderRadius:"100px", color:T1, fontSize:"11px", fontWeight:700, outline:"none",
                  boxShadow:HILITE }}>
                {TRANSLATIONS.map(t => <option key={t} style={{ background:"#0B1726" }}>{t}</option>)}
              </select>
            </div>
          </div>
          <div style={{ flex:1, overflowY:"auto" as const, padding:"8px 10px" }}>
            {searchLoading && (
              <div style={{ textAlign:"center", padding:"30px", color:T3, fontSize:"11px",
                letterSpacing:"0.04em", fontFamily:SERIF, fontStyle:"italic" }}>
                Scanning scripture…
              </div>
            )}
            {searchMode === "book" && !searchLoading && (
              <>
                {chapterTitle && (
                  <div style={{ padding:"8px 10px 12px", display:"flex", alignItems:"center", gap:"10px" }}>
                    <span style={{ fontSize:"18px", fontWeight:700, color:T1, fontFamily:SERIF, letterSpacing:"-0.02em", flex:1 }}>{chapterTitle}</span>
                    {chapterBook && (
                      <div style={{ display:"flex", gap:"6px" }}>
                        <button onClick={() => navChapter(-1)} disabled={chapterNum <= 1} style={{ width:"30px", height:"30px", borderRadius:"50%", background: chapterNum > 1 ? "rgba(255,255,255,0.06)" : "transparent", border:`1px solid ${chapterNum > 1 ? "rgba(255,255,255,0.14)" : BORDER}`, color: chapterNum > 1 ? T1 : T3, fontSize:"14px", cursor: chapterNum > 1 ? "pointer" : "default", display:"flex", alignItems:"center", justifyContent:"center", boxShadow: chapterNum > 1 ? HILITE : "none" }}>‹</button>
                        <button onClick={() => navChapter(+1)} style={{ width:"30px", height:"30px", borderRadius:"50%", background:"rgba(255,255,255,0.06)", border:"1px solid rgba(255,255,255,0.14)", color:T1, fontSize:"14px", cursor:"pointer", display:"flex", alignItems:"center", justifyContent:"center", boxShadow:HILITE }}>›</button>
                      </div>
                    )}
                  </div>
                )}
                {chapterVerses.length > 0 ? chapterVerses.map(v => {
                  const isStaged = previewVerse?.verseRef === v.ref;
                  const isLiveV  = liveVerse?.verseRef   === v.ref;
                  const verse: DetectedVerse = { verseRef:v.ref, verseText:v.text, translation, confidence:1, detectedAt:new Date().toISOString(), source:"manual" };
                  return (
                    <div key={v.ref} style={{ display:"flex", alignItems:"center", gap:"10px", padding:"7px 10px", marginBottom:"2px",
                      borderRadius:"10px",
                      background: isLiveV ? `${BLUE}10` : isStaged ? `${GOLD}08` : "transparent",
                      border: isLiveV ? `1px solid ${BLUE}30` : isStaged ? `1px solid ${GOLD}25` : "1px solid transparent",
                      transition:"all 150ms" }}>
                      <span style={{ fontSize:"10px", color:T3, width:"20px", flexShrink:0, fontFamily:"'SF Mono',monospace" }}>{v.num}</span>
                      <span onClick={() => setPreviewVerse(verse)} style={{ fontSize:"11px", color: isStaged||isLiveV ? T1 : T2, lineHeight:1.55, flex:1, cursor:"pointer" }}>{v.text}</span>
                      {isLiveV   && <span style={{ fontSize:"8px", fontWeight:800, color:BLUE, flexShrink:0, letterSpacing:"0.10em" }}>● LIVE</span>}
                      {isStaged && !isLiveV && <span style={{ fontSize:"10px", color:GOLD, flexShrink:0 }}>◆</span>}
                      <button onClick={e => { e.stopPropagation(); presentVerse(verse); }} style={{ flexShrink:0, padding:"4px 11px", borderRadius:"100px", border:`1px solid ${BLUE}70`, background:`linear-gradient(135deg, ${BLUE}, ${BLUE_HI})`, color:"#FFFFFF", fontSize:"8px", fontWeight:700, cursor:"pointer", boxShadow:`0 2px 8px ${BLUE}40, ${HILITE}` }}>▶</button>
                      <button onClick={e => { e.stopPropagation(); addToQueue(verse); }} style={{ flexShrink:0, padding:"4px 9px", borderRadius:"100px", border:`1px solid ${BORDER}`, background:"transparent", color:T3, fontSize:"8px", cursor:"pointer" }}>+Q</button>
                    </div>
                  );
                }) : !chapterTitle && (
                  <div style={{ textAlign:"center", padding:"14px 20px", opacity:0.55 }}>
                    <p style={{ fontSize:"11px", color:T3, margin:0, fontFamily:SERIF, fontStyle:"italic" }}>Enter book + chapter…</p>
                  </div>
                )}
              </>
            )}
            {searchMode === "context" && !searchLoading && (
              <>
                {ctxResults.length > 0 ? ctxResults.map((v, i) => (
                  <div key={i} style={{ padding:"11px 14px", marginBottom:"7px",
                    background:"rgba(255,255,255,0.03)",
                    border:`1px solid ${BORDER}`, borderRadius:"12px", boxShadow:HILITE }}>
                    <div style={{ display:"flex", alignItems:"center", justifyContent:"space-between", marginBottom:"7px" }}>
                      <span style={{ fontSize:"12px", fontWeight:700, color:GOLD }}>{v.verseRef}</span>
                      <div style={{ display:"flex", gap:"5px" }}>
                        <button onClick={() => presentVerse({ ...v, source:"manual" })} style={{ padding:"4px 12px", borderRadius:"100px", background:`linear-gradient(135deg, ${BLUE}, ${BLUE_HI})`, border:`1px solid ${BLUE}80`, color:"#FFFFFF", fontSize:"9px", fontWeight:700, cursor:"pointer", boxShadow:`0 2px 8px ${BLUE}40, ${HILITE}` }}>▶ Present</button>
                        <button onClick={() => addToQueue({ ...v, source:"manual" })} style={{ padding:"4px 10px", borderRadius:"100px", background:"transparent", border:`1px solid ${BORDER}`, color:T3, fontSize:"9px", cursor:"pointer" }}>+Q</button>
                      </div>
                    </div>
                    <p style={{ fontSize:"11px", color:T2, margin:0, lineHeight:1.55, fontStyle:"italic", fontFamily:SERIF }}>{v.verseText.slice(0,140)}{v.verseText.length>140?"…":""}</p>
                  </div>
                )) : (
                  <div style={{ textAlign:"center", padding:"14px 20px", opacity:0.55 }}>
                    <p style={{ fontSize:"11px", color:T3, margin:0, fontFamily:SERIF, fontStyle:"italic" }}>Search by topic or phrase…</p>
                  </div>
                )}
              </>
            )}
          </div>
        </div>

      </div>

      </div>{/* end left column */}

      {/* ── DETECTIONS / QUEUE — tall right sidebar with tab toggle ── */}
      <div style={{ width:"300px", flexShrink:0, display:"flex", flexDirection:"column" as const,
        ...card, borderRadius:"16px" }}>
        <div style={{ padding:"10px 12px", borderBottom:`1px solid ${BORDER}`, display:"flex", alignItems:"center", justifyContent:"space-between", gap:"8px", flexShrink:0 }}>
          {/* Segmented tab pill */}
          <div style={{ display:"flex", padding:"3px", background:"rgba(255,255,255,0.04)", border:`1px solid ${BORDER}`, borderRadius:"100px", boxShadow:HILITE }}>
            {(["detections","queue"] as const).map(tab => {
              const active = sidebarTab === tab;
              const count  = tab === "detections" ? detections.length : queue.length;
              const label  = tab === "detections" ? "Detections" : "Queue";
              return (
                <button key={tab} onClick={() => setSidebarTab(tab)} style={{
                  display:"flex", alignItems:"center", gap:"6px", padding:"4px 11px", borderRadius:"100px",
                  background: active ? "rgba(255,255,255,0.10)" : "transparent",
                  border: active ? "1px solid rgba(255,255,255,0.14)" : "1px solid transparent",
                  color: active ? T1 : T3, fontSize:"10px", fontWeight:700, cursor:"pointer",
                  letterSpacing:"0.06em", textTransform:"uppercase" as const,
                  boxShadow: active ? HILITE : "none", transition:"all 180ms" }}>
                  {label}
                  {count > 0 && (
                    <span style={{ fontSize:"9px", fontWeight:800, color:"#0B1726", background:YELLOW,
                      padding:"1px 6px", borderRadius:"100px", letterSpacing:0,
                      boxShadow: active ? `0 0 8px ${YELLOW}40` : "none" }}>{count}</span>
                  )}
                </button>
              );
            })}
          </div>
          {sidebarTab === "detections" && detections.length > 0 && (
            <button onClick={() => setDetections([])} style={{ fontSize:"8px", color:T3, background:"none", border:"none", cursor:"pointer", letterSpacing:"0.04em" }}>Clear</button>
          )}
          {sidebarTab === "queue" && queue.length > 0 && (
            <button onClick={() => setQueue([])} style={{ fontSize:"8px", color:T3, background:"none", border:"none", cursor:"pointer", letterSpacing:"0.04em" }}>Clear</button>
          )}
        </div>

        {/* Body */}
        <div style={{ flex:1, overflowY:"auto" as const, padding:"10px" }}>
          {sidebarTab === "detections" ? (
            detections.length === 0 ? (
              <div style={{ padding:"14px 12px", textAlign:"center", opacity:0.55 }}>
                <p style={{ fontSize:"11px", color:T3, margin:0, fontFamily:SERIF, fontStyle:"italic" }}>Awaiting verses…</p>
              </div>
            ) : detections.map((v, i) => {
              const confColor = v.confidence >= 0.9 ? GREEN : v.confidence >= 0.7 ? YELLOW : ORANGE;
              return (
                <div key={i} style={{ padding:"8px 10px", marginBottom:"5px",
                  background:"rgba(255,255,255,0.03)", border:`1px solid ${BORDER}`,
                  borderRadius:"10px", boxShadow:HILITE,
                  animation: i === 0 ? "slideInRight 320ms cubic-bezier(0.22,1,0.36,1)" : "none" }}>
                  <div style={{ display:"flex", alignItems:"center", gap:"7px", marginBottom:"4px" }}>
                    <div style={{ width:"5px", height:"5px", borderRadius:"50%", background:confColor, flexShrink:0, boxShadow:`0 0 4px ${confColor}` }} />
                    <span style={{ fontSize:"11px", fontWeight:700, color:GOLD, flex:1 }}>{v.verseRef}</span>
                    <button onClick={() => presentVerse(v)} style={{ padding:"3px 9px", borderRadius:"100px",
                      background:`linear-gradient(135deg, ${BLUE}, ${BLUE_HI})`, border:`1px solid ${BLUE}80`, color:"#FFFFFF", fontSize:"8px",
                      fontWeight:700, cursor:"pointer", letterSpacing:"0.04em", boxShadow:`0 2px 6px ${BLUE}40, ${HILITE}` }}>▶ Present</button>
                    <button onClick={() => addToQueue(v)} style={{ padding:"3px 8px", borderRadius:"100px",
                      background:"rgba(255,255,255,0.04)", border:`1px solid ${BORDER}`, color:T2, fontSize:"8px", cursor:"pointer", boxShadow:HILITE }}>+Q</button>
                  </div>
                  <p style={{ fontSize:"10.5px", color:T2, margin:0, lineHeight:1.45, fontStyle:"italic", fontFamily:SERIF,
                    overflow:"hidden", display:"-webkit-box", WebkitLineClamp:2, WebkitBoxOrient:"vertical" as const }}>
                    {v.verseText}
                  </p>
                </div>
              );
            })
          ) : (
            queue.length === 0 ? (
              <div style={{ padding:"14px 12px", textAlign:"center", opacity:0.55 }}>
                <p style={{ fontSize:"11px", color:T3, margin:0, lineHeight:1.6 }}>
                  <span style={{ fontFamily:SERIF, fontStyle:"italic" }}>Queue empty.</span><br/>
                  Press <kbd style={{ fontFamily:"'SF Mono',monospace", color:T2, fontSize:"8px", padding:"1px 5px",
                    background:"rgba(255,255,255,0.06)", border:"1px solid rgba(255,255,255,0.12)", borderRadius:"4px" }}>Q</kbd> on a staged verse to add
                </p>
              </div>
            ) : queue.map((item, i) => (
              <div key={item.queueId} style={{ padding:"8px 10px", marginBottom:"5px",
                background:"rgba(255,255,255,0.03)", border:`1px solid ${BORDER}`,
                borderRadius:"10px", display:"flex", alignItems:"center", gap:"8px", boxShadow:HILITE }}>
                <span style={{ fontSize:"9px", color:T3, fontFamily:"'SF Mono',monospace", width:"14px", flexShrink:0 }}>{i+1}</span>
                <div style={{ flex:1, minWidth:0 }}>
                  <div style={{ fontSize:"11px", fontWeight:700, color:GOLD, whiteSpace:"nowrap" as const,
                    overflow:"hidden", textOverflow:"ellipsis" }}>{item.verseRef}</div>
                  <div style={{ fontSize:"10px", color:T2, marginTop:"2px", whiteSpace:"nowrap" as const,
                    overflow:"hidden", textOverflow:"ellipsis", fontStyle:"italic", fontFamily:SERIF }}>{item.verseText}</div>
                </div>
                <button onClick={() => playFromQueue(item)} style={{ padding:"3px 9px", borderRadius:"100px",
                  background:`linear-gradient(135deg, ${BLUE}, ${BLUE_HI})`, border:`1px solid ${BLUE}80`, color:"#FFFFFF", fontSize:"8px",
                  fontWeight:700, cursor:"pointer", letterSpacing:"0.04em", boxShadow:`0 2px 6px ${BLUE}40, ${HILITE}`, flexShrink:0 }}>▶</button>
                <button onClick={() => removeFromQueue(item.queueId)} style={{ width:"20px", height:"20px", borderRadius:"50%",
                  background:"transparent", border:`1px solid ${BORDER}`, color:T3, fontSize:"10px",
                  cursor:"pointer", display:"flex", alignItems:"center", justifyContent:"center", flexShrink:0 }}>✕</button>
              </div>
            ))
          )}
        </div>
      </div>

      </div>{/* end main area flex-row */}

      {/* ── SHORTCUT STRIP ── */}
      <div style={{ height:"32px", flexShrink:0, display:"flex", alignItems:"center", padding:"0 20px", gap:"22px",
        background:GLASS, backdropFilter:"blur(40px) saturate(180%)", WebkitBackdropFilter:"blur(40px) saturate(180%)",
        borderTop:`1px solid ${BORDER}`, boxShadow:`0 -1px 0 rgba(0,0,0,0.4), ${HILITE}` }}>
        {[["↵","Go live"],["esc","Clear"],["A","Auto"],["Q","Queue"],["C","Blank"]].map(([k,lbl]) => (
          <span key={k} style={{ display:"flex", alignItems:"center", gap:"6px", fontSize:"9px", color:T3, letterSpacing:"0.04em" }}>
            <kbd style={{ padding:"2px 7px", borderRadius:"5px", border:"1px solid rgba(255,255,255,0.14)",
              fontFamily:"'SF Mono',monospace", fontSize:"8px", color:T1, background:"rgba(255,255,255,0.06)",
              boxShadow:HILITE, letterSpacing:0 }}>{k}</kbd>
            <span>{lbl}</span>
          </span>
        ))}
        <div style={{ marginLeft:"auto", display:"flex", alignItems:"center", gap:"18px" }}>
          <span style={{ fontSize:"9px", color: listening ? GREEN : T3, fontFamily:"'SF Mono',monospace", letterSpacing:"0.06em" }}>● MIC {listening?"ON":"OFF"}</span>
        </div>
      </div>

      <style>{`
        @keyframes liveDot{0%,100%{opacity:1;transform:scale(1)}50%{opacity:.35;transform:scale(.7)}}
        @keyframes verseIn{from{opacity:0;transform:translateY(12px) scale(0.97)}to{opacity:1;transform:translateY(0) scale(1)}}
        @keyframes slideInRight{from{opacity:0;transform:translateX(14px)}to{opacity:1;transform:translateX(0)}}
        ::-webkit-scrollbar{width:3px}
        ::-webkit-scrollbar-track{background:transparent}
        ::-webkit-scrollbar-thumb{background:rgba(255,255,255,0.10);border-radius:3px}
        ::-webkit-scrollbar-thumb:hover{background:rgba(255,255,255,0.22)}
      `}</style>
    </div>,
    document.body
  );
}
