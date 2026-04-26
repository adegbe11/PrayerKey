import type { Metadata } from "next";
import Link from "next/link";

const BASE_URL    = "https://www.prayerkey.com";
const DOCS_URL    = `${BASE_URL}/docs`;
const PUBLISHED   = "2026-04-26";
const MODIFIED    = "2026-04-26";

export const metadata: Metadata = {
  title:       "How to Use PrayerKey — Complete Documentation & User Guide",
  description: "Step-by-step guide to every PrayerKey feature: live sermon Bible-verse detection, AI prayer generator, full-Bible search with cross-references, prayer library, and more. Free, no account required.",
  keywords: [
    "prayerkey docs", "how to use prayerkey", "prayerkey guide",
    "live sermon bible verse detector tutorial", "ai prayer generator how to",
    "bible verse search cross references", "prayerkey faq",
    "free church projector software", "sermon verse display tool",
    "real-time bible verse detection",
  ],
  alternates: { canonical: DOCS_URL },
  openGraph: {
    title:       "How to Use PrayerKey — Complete Documentation",
    description: "Everything you need to use PrayerKey: live sermon verse detection, AI prayer generation, Bible search, and 544+ scripture-grounded prayers. Free forever.",
    url:         DOCS_URL,
    type:        "article",
    siteName:    "PrayerKey",
    publishedTime: PUBLISHED,
    modifiedTime:  MODIFIED,
    authors:       ["Collins Omoikhudu Asein"],
    images: [{ url: "/og-image.png", width: 1200, height: 630, alt: "PrayerKey documentation" }],
  },
  twitter: {
    card:        "summary_large_image",
    title:       "How to Use PrayerKey — Complete Guide",
    description: "Step-by-step docs for every PrayerKey feature. Free. No account needed.",
    images:      ["/og-image.png"],
  },
  robots: {
    index: true, follow: true,
    googleBot: { index: true, follow: true, "max-image-preview": "large", "max-snippet": -1 },
  },
};

/* ── Table of contents ───────────────────────────────────────── */
const TOC = [
  { id: "quick-start",        label: "Quick Start"                         },
  { id: "what-is",            label: "What Is PrayerKey?"                  },
  { id: "live-sermon",        label: "Live Sermon Verse Detection"         },
  { id: "ai-prayer",          label: "AI Prayer Generator"                 },
  { id: "bible-search",       label: "Bible Search & Cross-References"     },
  { id: "prayer-library",     label: "Prayer Library (544+ Prayers)"       },
  { id: "translations",       label: "Bible Translations Supported"        },
  { id: "projector",          label: "Projector & Second-Screen Setup"     },
  { id: "browsers",           label: "Browser & Device Requirements"       },
  { id: "shortcuts",          label: "Keyboard Shortcuts"                  },
  { id: "privacy",            label: "Privacy, Audio & Data"               },
  { id: "troubleshooting",    label: "Troubleshooting"                     },
  { id: "denominations",      label: "Denominations & Theology"            },
  { id: "faq",                label: "Frequently Asked Questions"          },
];

/* ── FAQ data — also serialized to JSON-LD for rich results ──── */
const FAQ: { q: string; a: string }[] = [
  { q: "Is PrayerKey really free?",
    a: "Yes. PrayerKey is 100% free with no account, no subscription, and no payment of any kind. There is no premium tier. The only thing we ask is that you tell another pastor about it." },
  { q: "Do I need to create an account to use PrayerKey?",
    a: "No. Open the website and start using any feature immediately. You do not need to register, log in, or provide an email address." },
  { q: "Which Bible translations does PrayerKey support?",
    a: "PrayerKey supports 11 major translations: KJV, NIV, ESV, NKJV, NLT, NASB, AMP, CSB, MSG, CEV, and GNT. You can switch translations at any time from the dropdown in the live dashboard or Bible search." },
  { q: "Does the live sermon detection work without internet?",
    a: "No. Live verse detection requires an internet connection because the AI model that matches spoken words to scripture runs in the cloud. A stable Wi-Fi connection of at least 1 Mbps is recommended." },
  { q: "Which browsers work with PrayerKey?",
    a: "All features work in Chrome, Edge, Brave, and Opera. Live sermon verse detection requires the Web Speech API, which is fully supported in Chromium-based browsers. Safari and Firefox can use the prayer generator and Bible search but have limited speech recognition support." },
  { q: "Does PrayerKey record or store my sermon audio?",
    a: "No. Audio is processed in real time inside your browser and is never stored on our servers. Only the transcribed text is sent for verse matching, and that text is discarded after the session ends." },
  { q: "How accurate is the verse detection?",
    a: "PrayerKey uses two detection layers. Direct references like 'John 3:16' are matched with near-100% accuracy. Paraphrases and themed quotes use semantic embeddings and typically score 70–95% confidence. A confidence dot on each detection card shows the match strength." },
  { q: "Can I use PrayerKey on a phone or tablet?",
    a: "Yes. The prayer generator, Bible search, and prayer library work on any modern phone or tablet. The live sermon dashboard is optimized for laptop or desktop because it needs a microphone and a second screen for the projector." },
  { q: "How do I send a verse to the projector?",
    a: "Click '▶ Present' on any verse in the Detections sidebar to push it to the live display. Click 'Projector ↗' in the header to open the full-screen projector window on your second monitor or projector." },
  { q: "Can I queue verses to play in order?",
    a: "Yes. Click '+Q' on any detected verse to add it to the Queue. Toggle to the Queue tab in the right sidebar to see the order, then press play on each item to send it live in sequence." },
  { q: "Can I run PrayerKey for the whole service or only the sermon?",
    a: "PrayerKey is designed for the sermon portion. Start it when the pastor begins preaching and end the session when the message is over. Running it during music or announcements wastes processing and can produce false detections." },
  { q: "What if a verse isn't detected automatically?",
    a: "Use the search panel at the bottom of the live dashboard. Switch to 'Context' mode and type any phrase, theme, or partial reference. PrayerKey will return matches you can present manually with one click." },
  { q: "Is PrayerKey denomination-specific?",
    a: "No. PrayerKey is denomination-neutral. It is used by Catholic, Protestant, Pentecostal, Baptist, Anglican, Methodist, non-denominational, and independent churches around the world." },
  { q: "Can I use PrayerKey for funerals, weddings, or special services?",
    a: "Yes. The AI prayer generator is especially useful for funerals, weddings, baby dedications, hospital visits, and any service where you need scripture-grounded words quickly." },
  { q: "Does PrayerKey support languages other than English?",
    a: "PrayerKey currently supports English. Multilingual sermon detection is on the roadmap. If you would like a specific language prioritized, please reach out through the contact link in the footer." },
  { q: "Can I download or save the prayers I generate?",
    a: "Yes. Every generated prayer has a 'Copy' button to copy it to your clipboard and a 'Download' option that creates a shareable image card." },
  { q: "Is the AI-generated prayer theologically sound?",
    a: "PrayerKey uses a model tuned for theological sensitivity and grounds every prayer in real Bible verses with citations. We recommend pastors review generated content before using it from the pulpit, just as you would any other prayer resource." },
  { q: "How fast is the prayer generator?",
    a: "Most prayers are generated in 6 to 10 seconds. The result includes the prayer body, supporting scripture references, and a short encouragement note." },
  { q: "Can two people use PrayerKey at the same time on the same screen?",
    a: "PrayerKey is currently designed for a single operator per device. Multi-operator support is on the roadmap." },
  { q: "Does PrayerKey need a microphone?",
    a: "Only the live sermon feature needs a microphone. The prayer generator, Bible search, and prayer library do not require any microphone access." },
  { q: "What microphone should I use for the live sermon?",
    a: "Any clear microphone works — a USB lapel mic or the church's existing PA-feed-into-line-in setup is ideal. Avoid laptops sitting more than 10 feet from the speaker, as ambient noise reduces detection confidence." },
  { q: "Can I install PrayerKey as an app?",
    a: "Yes. PrayerKey is a Progressive Web App. Open the site in Chrome or Edge, click the install icon in the address bar, and it installs as a standalone app on your computer or phone." },
  { q: "How is PrayerKey different from Pewbeam, ProPresenter, or EasyWorship?",
    a: "PrayerKey is fully automatic and free. Traditional church software requires an operator to type or click each verse manually and costs $200–$2,000 per year. PrayerKey listens, detects, and displays verses in real time without an operator and costs nothing." },
  { q: "Who built PrayerKey?",
    a: "PrayerKey was designed and built by Collins Omoikhudu Asein, an independent Christian author and faith technologist. There are no investors, no advertisers, and no data brokers. Read more on the About page." },
  { q: "How do I report a bug or request a feature?",
    a: "Use the contact links in the site footer. Bugs are typically fixed within 24–48 hours, and feature requests from active churches are prioritized." },
];

/* ── Schema.org structured data ───────────────────────────────── */
const breadcrumbLd = {
  "@context": "https://schema.org",
  "@type":    "BreadcrumbList",
  itemListElement: [
    { "@type": "ListItem", position: 1, name: "Home", item: BASE_URL },
    { "@type": "ListItem", position: 2, name: "Documentation", item: DOCS_URL },
  ],
};

const articleLd = {
  "@context": "https://schema.org",
  "@type":    "TechArticle",
  headline:   "How to Use PrayerKey — Complete Documentation",
  description: "Complete guide to PrayerKey's free AI prayer generator, real-time live sermon Bible-verse detection, and full-Bible search with cross-references.",
  datePublished: PUBLISHED,
  dateModified:  MODIFIED,
  author: {
    "@type": "Person",
    name:    "Collins Omoikhudu Asein",
    url:     `${BASE_URL}/author/collins-asein`,
  },
  publisher: {
    "@type": "Organization",
    name:    "PrayerKey",
    logo:   { "@type": "ImageObject", url: `${BASE_URL}/prayerkey-icon.png` },
  },
  mainEntityOfPage: { "@type": "WebPage", "@id": DOCS_URL },
  image:            `${BASE_URL}/og-image.png`,
  proficiencyLevel: "Beginner",
  about: [
    "Live Sermon Verse Detection", "AI Prayer Generation",
    "Bible Search", "Cross-References", "Church Software",
  ],
};

const howToLd = {
  "@context": "https://schema.org",
  "@type":    "HowTo",
  name:       "How to use PrayerKey for live sermon verse detection",
  description: "Set up PrayerKey to automatically detect and display Bible verses as your pastor preaches.",
  totalTime:  "PT3M",
  supply:     [{ "@type": "HowToSupply", name: "Laptop with Chrome or Edge browser" },
               { "@type": "HowToSupply", name: "Microphone (laptop built-in or USB lapel mic)" },
               { "@type": "HowToSupply", name: "Projector or second screen" }],
  step: [
    { "@type": "HowToStep", position: 1, name: "Open the live dashboard",
      text: "Visit prayerkey.com/live in Chrome or Edge.", url: `${BASE_URL}/live` },
    { "@type": "HowToStep", position: 2, name: "Allow microphone access",
      text: "Click 'Allow' when the browser asks for microphone permission." },
    { "@type": "HowToStep", position: 3, name: "Open the projector window",
      text: "Click 'Projector ↗' to open the full-screen verse display, then drag it to your projector." },
    { "@type": "HowToStep", position: 4, name: "Press Start Listening",
      text: "Click the Start Listening button. PrayerKey will transcribe the sermon and detect verses in real time." },
    { "@type": "HowToStep", position: 5, name: "Send verses live",
      text: "Click '▶ Present' on any detected verse, or enable Auto mode for hands-free operation." },
  ],
};

const faqLd = {
  "@context": "https://schema.org",
  "@type":    "FAQPage",
  mainEntity: FAQ.map(({ q, a }) => ({
    "@type": "Question",
    name:    q,
    acceptedAnswer: { "@type": "Answer", text: a },
  })),
};

/* ── Reusable inline styles ───────────────────────────────────── */
const h2: React.CSSProperties = {
  fontSize:      "clamp(24px,3vw,32px)",
  fontWeight:    800,
  letterSpacing: "-0.025em",
  color:         "var(--pk-text)",
  margin:        "56px 0 16px",
  scrollMarginTop: "84px",
};
const h3: React.CSSProperties = {
  fontSize:      "clamp(17px,2vw,20px)",
  fontWeight:    700,
  letterSpacing: "-0.01em",
  color:         "var(--pk-text)",
  margin:        "32px 0 10px",
};
const p: React.CSSProperties = {
  fontSize:   "16px",
  color:      "var(--pk-text-2)",
  lineHeight: 1.75,
  margin:     "0 0 14px",
};
const li: React.CSSProperties = { ...p, margin: "0 0 8px" };
const code: React.CSSProperties = {
  fontFamily: "'SF Mono','Menlo',monospace",
  fontSize:   "13px",
  background: "var(--pk-accent-dim)",
  border:     "1px solid var(--pk-border)",
  padding:    "2px 7px",
  borderRadius: "5px",
  color:      "var(--pk-text)",
};
const cardBox: React.CSSProperties = {
  border:       "1.5px solid var(--pk-border)",
  borderRadius: "10px",
  padding:      "20px 22px",
  background:   "var(--pk-surface)",
  boxShadow:    "3px 3px 0 0 var(--pk-border)",
  margin:       "16px 0 20px",
};

export default function DocsPage() {
  return (
    <>
      {/* ── Schema.org JSON-LD ── */}
      <script type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(breadcrumbLd) }} />
      <script type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(articleLd) }} />
      <script type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(howToLd) }} />
      <script type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(faqLd) }} />

      <article style={{ maxWidth: "1100px", margin: "0 auto", padding: "0 0 100px" }}>

        {/* ── Breadcrumb ── */}
        <nav aria-label="Breadcrumb" style={{ fontSize:"13px", color:"var(--pk-text-3)", marginBottom:"24px" }}>
          <Link href="/" style={{ color:"var(--pk-text-3)", textDecoration:"none" }}>Home</Link>
          <span style={{ margin:"0 8px" }}>/</span>
          <span style={{ color:"var(--pk-text-2)" }}>Documentation</span>
        </nav>

        {/* ── Hero ── */}
        <header style={{ marginBottom: "48px" }}>
          <div style={{
            display:"inline-flex", alignItems:"center", padding:"4px 12px",
            border:"1.5px solid var(--pk-accent-border)", borderRadius:"4px",
            marginBottom:"24px", background:"var(--pk-accent-dim)",
            boxShadow:"3px 3px 0 0 var(--pk-accent-border)",
          }}>
            <span style={{ fontSize:"10px", fontWeight:700, color:"var(--pk-accent)",
              letterSpacing:"0.12em", textTransform:"uppercase" as const }}>
              Documentation · v1
            </span>
          </div>

          <h1 style={{
            fontSize:"clamp(36px,6vw,64px)", fontWeight:800, letterSpacing:"-0.03em",
            lineHeight:1.05, color:"var(--pk-text)", margin:"0 0 20px",
          }}>
            How to Use PrayerKey
          </h1>
          <p style={{
            fontSize:"clamp(17px,1.6vw,20px)", color:"var(--pk-text-2)",
            lineHeight:1.65, margin:"0 0 16px", maxWidth:"720px",
          }}>
            A complete user guide to every PrayerKey feature — live sermon Bible-verse detection,
            the AI prayer generator, full-Bible search with cross-references, and the
            544-prayer library. No account, no payment, no setup beyond opening the site.
          </p>
          <p style={{ fontSize:"13px", color:"var(--pk-text-3)", margin:0 }}>
            Last updated <time dateTime={MODIFIED}>April 26, 2026</time> · Written by{" "}
            <Link href="/author/collins-asein" style={{ color:"var(--pk-accent)", textDecoration:"none" }}>
              Collins Omoikhudu Asein
            </Link>
          </p>
        </header>

        {/* ── Layout: TOC + content ── */}
        <div style={{ display:"grid", gridTemplateColumns:"minmax(0,1fr)", gap:"40px" }} className="docs-grid">

          {/* TOC */}
          <aside aria-label="Table of contents" className="docs-toc" style={{
            border:"1.5px solid var(--pk-border)", borderRadius:"10px",
            padding:"18px 20px", background:"var(--pk-surface)",
            boxShadow:"3px 3px 0 0 var(--pk-border)",
          }}>
            <p style={{ fontSize:"10px", fontWeight:700, color:"var(--pk-accent)",
              letterSpacing:"0.14em", textTransform:"uppercase" as const, margin:"0 0 12px" }}>
              On this page
            </p>
            <ol style={{ listStyle:"none", margin:0, padding:0,
              counterReset:"toc", display:"flex", flexDirection:"column", gap:"6px" }}>
              {TOC.map(item => (
                <li key={item.id} style={{ counterIncrement:"toc" }}>
                  <a href={`#${item.id}`} style={{
                    fontSize:"14px", color:"var(--pk-text-2)", textDecoration:"none",
                    lineHeight:1.5, display:"block", padding:"3px 0",
                  }}>{item.label}</a>
                </li>
              ))}
            </ol>
          </aside>

          {/* Main content */}
          <main style={{ minWidth: 0 }}>

            {/* ── Quick Start ── */}
            <section id="quick-start">
              <h2 style={h2}>Quick Start</h2>
              <p style={p}>
                PrayerKey works without installation, login, or payment. Pick the feature you need:
              </p>
              <div style={cardBox}>
                <h3 style={{ ...h3, margin:"0 0 6px" }}>1. Generate a prayer</h3>
                <p style={p}>
                  Go to <Link href="/pray" style={{ color:"var(--pk-accent)" }}>prayerkey.com/pray</Link>,
                  type what you're feeling or what you need prayer for, pick a mood,
                  and click <strong>Generate</strong>. A complete scripture-grounded prayer
                  appears in about 8 seconds.
                </p>
              </div>
              <div style={cardBox}>
                <h3 style={{ ...h3, margin:"0 0 6px" }}>2. Run live sermon detection</h3>
                <p style={p}>
                  Go to <Link href="/live" style={{ color:"var(--pk-accent)" }}>prayerkey.com/live</Link>,
                  allow microphone access, click <strong>Start Listening</strong>, and
                  open the projector window on your second screen. PrayerKey transcribes
                  the sermon and shows every Bible verse the pastor quotes — automatically.
                </p>
              </div>
              <div style={cardBox}>
                <h3 style={{ ...h3, margin:"0 0 6px" }}>3. Search the Bible</h3>
                <p style={p}>
                  Go to <Link href="/bible" style={{ color:"var(--pk-accent)" }}>prayerkey.com/bible</Link> and
                  type a reference (<code style={code}>John 3:16</code>), a topic (<code style={code}>do not fear</code>),
                  or a paraphrase. Tap any result to see four to six related cross-reference verses.
                </p>
              </div>
            </section>

            {/* ── What Is PrayerKey ── */}
            <section id="what-is">
              <h2 style={h2}>What Is PrayerKey?</h2>
              <p style={p}>
                PrayerKey is a free, browser-based AI companion for pastors and believers.
                It does three things exceptionally well:
              </p>
              <ul style={{ ...p, paddingLeft:"22px" }}>
                <li style={li}>
                  <strong>Listens to a live sermon</strong> and automatically pushes every
                  quoted Bible verse to a projector screen in real time — no operator needed.
                </li>
                <li style={li}>
                  <strong>Generates personalized scripture-grounded prayers</strong> from a
                  short text description of what you're going through.
                </li>
                <li style={li}>
                  <strong>Searches all 31,102 Bible verses</strong> by reference, keyword,
                  or paraphrase, and returns four to six thematic cross-references for any verse.
                </li>
              </ul>
              <p style={p}>
                The whole platform is denomination-neutral, account-free, and free of ads,
                tracking, and paid tiers. There is no premium plan to upsell.
              </p>
            </section>

            {/* ── Live Sermon ── */}
            <section id="live-sermon">
              <h2 style={h2}>Live Sermon Verse Detection</h2>
              <p style={p}>
                The live dashboard at{" "}
                <Link href="/live" style={{ color:"var(--pk-accent)" }}>/live</Link> turns
                any laptop with a microphone into a fully automatic verse-display tool.
                Plug into your church's audio feed (or use the laptop mic), press start,
                and PrayerKey handles the rest.
              </p>

              <h3 style={h3}>Setup (3 minutes)</h3>
              <ol style={{ ...p, paddingLeft:"22px" }}>
                <li style={li}>Open <code style={code}>prayerkey.com/live</code> in Chrome, Edge, or Brave.</li>
                <li style={li}>Click <strong>Allow</strong> when the browser asks for microphone access.</li>
                <li style={li}>Click <strong>Projector ↗</strong> in the header. A new window opens — drag it to your second screen and press F11 for fullscreen.</li>
                <li style={li}>Choose your translation from the dropdown (KJV, NIV, ESV, NKJV, NLT, and more).</li>
                <li style={li}>When the pastor begins preaching, click <strong>Start Listening</strong>.</li>
              </ol>

              <h3 style={h3}>The Dashboard Layout</h3>
              <p style={p}>
                The live dashboard is split into two rows and a tall right sidebar:
              </p>
              <ul style={{ ...p, paddingLeft:"22px" }}>
                <li style={li}><strong>Transcript</strong> (left column) — every word the pastor speaks, scrolling in real time. Verses spotted in the transcript are highlighted in gold.</li>
                <li style={li}><strong>Program · Preview</strong> — the verse you're about to send live. Stage a verse here before pushing it to the projector.</li>
                <li style={li}><strong>Live · Display</strong> — the verse currently visible to the congregation. Mirrors what the projector shows.</li>
                <li style={li}><strong>Detections / Queue</strong> (right sidebar) — toggleable list of every verse the AI heard, plus a queue you can build for sequenced playback.</li>
                <li style={li}><strong>Search</strong> (bottom row) — manual fallback. Look up any verse by book + chapter or by topic if the AI missed something.</li>
              </ul>

              <h3 style={h3}>Auto Mode vs. Manual Mode</h3>
              <p style={p}>
                Toggle <strong>Auto</strong> in the header to make every confidently detected
                verse fly straight to the live screen. Leave it off and you stay in control —
                each verse waits in Detections until you click <strong>▶ Present</strong>.
              </p>
              <p style={p}>
                Most operators use a hybrid: Auto on for sermons with rapid back-to-back
                quotes, manual for verse-by-verse expository preaching where pacing matters.
              </p>

              <h3 style={h3}>Building a Queue</h3>
              <p style={p}>
                When the pastor flags multiple verses ahead of time ("we'll look at Romans 8,
                then Psalm 23, then Isaiah 41"), click <strong>+Q</strong> on each detected
                verse to add it to the queue in order. Switch the right sidebar to the{" "}
                <strong>Queue</strong> tab and play each one when the pastor reaches it.
              </p>

              <h3 style={h3}>Detection Confidence</h3>
              <p style={p}>
                Every detection card has a colored dot:
              </p>
              <ul style={{ ...p, paddingLeft:"22px" }}>
                <li style={li}><strong style={{ color:"#34C759" }}>Green</strong> — 90%+ confidence (direct reference or near-exact quote).</li>
                <li style={li}><strong style={{ color:"#F4D03F" }}>Yellow</strong> — 70–89% confidence (strong paraphrase).</li>
                <li style={li}><strong style={{ color:"#FF9F0A" }}>Orange</strong> — 60–69% confidence (loose theme match — review before sending live).</li>
              </ul>
            </section>

            {/* ── AI Prayer ── */}
            <section id="ai-prayer">
              <h2 style={h2}>AI Prayer Generator</h2>
              <p style={p}>
                Visit <Link href="/pray" style={{ color:"var(--pk-accent)" }}>/pray</Link> and
                type a few sentences about what you need prayer for. The generator returns
                a complete prayer in under 10 seconds, including scripture references and a
                short encouragement note.
              </p>

              <h3 style={h3}>Writing a Good Prompt</h3>
              <p style={p}>
                Better input = better prayer. Three rules:
              </p>
              <ul style={{ ...p, paddingLeft:"22px" }}>
                <li style={li}><strong>Be specific.</strong> "I'm anxious about Friday's surgery" produces a far more focused prayer than "I'm worried."</li>
                <li style={li}><strong>Pick a mood chip</strong> (Grateful, Anxious, Hopeful, Sick, Tired, etc.). The mood guides the tone of the resulting prayer.</li>
                <li style={li}><strong>Include the person if relevant.</strong> "My daughter Maya is starting kindergarten" gives the prayer a name to lift up.</li>
              </ul>

              <h3 style={h3}>What the Output Includes</h3>
              <ul style={{ ...p, paddingLeft:"22px" }}>
                <li style={li}>A short title summarizing the prayer's focus.</li>
                <li style={li}>The full prayer body, written in second-person addressed to God.</li>
                <li style={li}>2–4 supporting Bible verses with references, drawn from the prayer's theme.</li>
                <li style={li}>A 1–2 sentence encouragement note rooted in scripture.</li>
              </ul>

              <h3 style={h3}>Saving & Sharing</h3>
              <p style={p}>
                Each generated prayer has a <strong>Copy</strong> button (plain text to clipboard)
                and a <strong>Download</strong> option that creates a shareable image card you
                can text, post to social media, or print.
              </p>
            </section>

            {/* ── Bible Search ── */}
            <section id="bible-search">
              <h2 style={h2}>Bible Search & Cross-References</h2>
              <p style={p}>
                The Bible search at{" "}
                <Link href="/bible" style={{ color:"var(--pk-accent)" }}>/bible</Link> understands
                three kinds of queries:
              </p>
              <ul style={{ ...p, paddingLeft:"22px" }}>
                <li style={li}><strong>Direct references</strong> — <code style={code}>John 3:16</code>, <code style={code}>Ps 23</code>, <code style={code}>1 Cor 13:4-7</code>.</li>
                <li style={li}><strong>Keywords or topics</strong> — <code style={code}>do not fear</code>, <code style={code}>healing</code>, <code style={code}>grace</code>.</li>
                <li style={li}><strong>Paraphrases</strong> — <code style={code}>the verse about being still and knowing God</code>.</li>
              </ul>

              <h3 style={h3}>Cross-References</h3>
              <p style={p}>
                Click any result to load 4–6 related verses with a one-line "reason" explaining
                each connection — same theme, related doctrine, or shared imagery. Useful for
                sermon prep, Bible study, and small-group discussion.
              </p>

              <h3 style={h3}>Switching Translations</h3>
              <p style={p}>
                The translation dropdown affects both the search index and the verse text shown.
                Eleven translations are available (see the <a href="#translations" style={{ color:"var(--pk-accent)" }}>Translations</a> section).
              </p>
            </section>

            {/* ── Prayer Library ── */}
            <section id="prayer-library">
              <h2 style={h2}>Prayer Library — 544+ Pre-Written Prayers</h2>
              <p style={p}>
                Visit <Link href="/prayer" style={{ color:"var(--pk-accent)" }}>/prayer</Link> to
                browse the curated prayer library, organized three ways:
              </p>
              <ul style={{ ...p, paddingLeft:"22px" }}>
                <li style={li}><strong>By Category</strong> (16) — Health, Mental Health, Daily Prayer, Family, Finance, Protection, Faith, Grief, Celebration, Education, Salvation, Purpose, Relationships, Thanksgiving, Ministry, Nation.</li>
                <li style={li}><strong>By Time of Day</strong> (6) — Morning, Night, Daily, Short, Powerful, Fasting.</li>
                <li style={li}><strong>By How You Feel</strong> (12) — Anxious, Depressed, Grieving, Angry, Lonely, Afraid, Hopeful, Grateful, Stressed, Confused, Joyful, Sick.</li>
              </ul>
              <p style={p}>
                Every prayer is grounded in real scripture and ready to read aloud, share,
                or use as a starting point for your own words.
              </p>
            </section>

            {/* ── Translations ── */}
            <section id="translations">
              <h2 style={h2}>Bible Translations Supported</h2>
              <p style={p}>PrayerKey supports 11 translations across all features:</p>
              <ul style={{ ...p, paddingLeft:"22px" }}>
                <li style={li}><strong>KJV</strong> — King James Version</li>
                <li style={li}><strong>NIV</strong> — New International Version</li>
                <li style={li}><strong>ESV</strong> — English Standard Version</li>
                <li style={li}><strong>NKJV</strong> — New King James Version</li>
                <li style={li}><strong>NLT</strong> — New Living Translation</li>
                <li style={li}><strong>NASB</strong> — New American Standard Bible</li>
                <li style={li}><strong>AMP</strong> — Amplified Bible</li>
                <li style={li}><strong>CSB</strong> — Christian Standard Bible</li>
                <li style={li}><strong>MSG</strong> — The Message</li>
                <li style={li}><strong>CEV</strong> — Contemporary English Version</li>
                <li style={li}><strong>GNT</strong> — Good News Translation</li>
              </ul>
            </section>

            {/* ── Projector Setup ── */}
            <section id="projector">
              <h2 style={h2}>Projector & Second-Screen Setup</h2>
              <p style={p}>
                The projector window is a separate route at{" "}
                <code style={code}>/live/projector</code> that mirrors only the live verse — no
                controls, no transcript, just the verse on a clean dark background suitable
                for any congregation.
              </p>
              <h3 style={h3}>Recommended setup</h3>
              <ol style={{ ...p, paddingLeft:"22px" }}>
                <li style={li}>Connect your laptop to the projector via HDMI or wireless cast.</li>
                <li style={li}>Set your displays to <strong>Extended</strong> mode (not Mirrored).</li>
                <li style={li}>Open the live dashboard on your laptop screen.</li>
                <li style={li}>Click <strong>Projector ↗</strong>. A new window opens.</li>
                <li style={li}>Drag the new window onto the projector display and press <code style={code}>F11</code> for fullscreen.</li>
              </ol>
            </section>

            {/* ── Browsers ── */}
            <section id="browsers">
              <h2 style={h2}>Browser & Device Requirements</h2>
              <h3 style={h3}>Recommended</h3>
              <ul style={{ ...p, paddingLeft:"22px" }}>
                <li style={li}>Google Chrome 110+ on Mac, Windows, or Chromebook.</li>
                <li style={li}>Microsoft Edge 110+.</li>
                <li style={li}>Brave or Opera (Chromium-based).</li>
                <li style={li}>Stable internet — at least 1 Mbps for live detection.</li>
                <li style={li}>Microphone — built-in or USB lapel for live sermon mode.</li>
              </ul>
              <h3 style={h3}>Limited support</h3>
              <p style={p}>
                Safari and Firefox can use the prayer generator, prayer library, and Bible
                search without issues. Live sermon detection is restricted because their
                Web Speech API support is incomplete.
              </p>
              <h3 style={h3}>Mobile</h3>
              <p style={p}>
                Every feature except the live sermon dashboard works on iOS and Android.
                The live dashboard is desktop-first because it needs a microphone and a
                second display.
              </p>
            </section>

            {/* ── Shortcuts ── */}
            <section id="shortcuts">
              <h2 style={h2}>Keyboard Shortcuts (Live Dashboard)</h2>
              <ul style={{ ...p, paddingLeft:"22px" }}>
                <li style={li}><code style={code}>↵ Enter</code> — Send the staged verse live</li>
                <li style={li}><code style={code}>esc</code> — Clear the live screen</li>
                <li style={li}><code style={code}>A</code> — Toggle Auto mode</li>
                <li style={li}><code style={code}>Q</code> — Add the staged verse to the Queue</li>
                <li style={li}><code style={code}>C</code> — Blank the projector (e.g. between sermon points)</li>
              </ul>
            </section>

            {/* ── Privacy ── */}
            <section id="privacy">
              <h2 style={h2}>Privacy, Audio & Data</h2>
              <p style={p}>
                PrayerKey is built privacy-first. We do not record audio. We do not store
                sermon transcripts. We do not have user accounts, so there is no profile,
                history, or behavioral data tied to you.
              </p>
              <p style={p}>
                During a live session, audio is converted to text inside your browser using
                the Web Speech API. Only the resulting text is sent to our servers for
                verse matching, and that text is discarded the moment the session ends.
                Read the full <Link href="/privacy" style={{ color:"var(--pk-accent)" }}>Privacy Policy</Link>.
              </p>
            </section>

            {/* ── Troubleshooting ── */}
            <section id="troubleshooting">
              <h2 style={h2}>Troubleshooting</h2>
              <h3 style={h3}>Microphone won't start</h3>
              <p style={p}>
                Check your browser's permission icon (left of the URL bar). Click it, set
                Microphone to <strong>Allow</strong>, and reload the page. On Mac, also
                confirm your browser has microphone access in{" "}
                <strong>System Settings → Privacy → Microphone</strong>.
              </p>

              <h3 style={h3}>Verses are detected but the wrong translation shows</h3>
              <p style={p}>
                The translation dropdown in the header controls all output. Switch it
                before going live and reload if needed.
              </p>

              <h3 style={h3}>Detection is missing verses</h3>
              <p style={p}>
                Three usual causes: ambient noise, distance from the speaker, or the
                pastor paraphrasing rather than quoting. Move the laptop closer or use a
                lapel mic, and use <strong>Context Search</strong> as a manual fallback
                for paraphrases.
              </p>

              <h3 style={h3}>Projector window opens behind the dashboard</h3>
              <p style={p}>
                Some browsers open new windows on the active display. Drag it to the
                projector, then press <code style={code}>F11</code> to fullscreen. If
                pop-ups are blocked, click the blocked-pop-up icon and allow{" "}
                <code style={code}>prayerkey.com</code>.
              </p>

              <h3 style={h3}>Prayer generator returns an error</h3>
              <p style={p}>
                Usually a temporary network blip. Wait 5 seconds and click{" "}
                <strong>Generate</strong> again. If it persists, refresh the page.
              </p>
            </section>

            {/* ── Denominations ── */}
            <section id="denominations">
              <h2 style={h2}>Denominations & Theology</h2>
              <p style={p}>
                PrayerKey is denomination-neutral. The Bible search returns the actual
                text of the translation you select; the prayer generator is tuned for
                broad theological sensitivity rather than a single tradition.
              </p>
              <p style={p}>
                It's used by Catholic, Protestant, Pentecostal, Baptist, Anglican, Methodist,
                Reformed, Evangelical, and non-denominational churches around the world.
              </p>
            </section>

            {/* ── FAQ ── */}
            <section id="faq">
              <h2 style={h2}>Frequently Asked Questions</h2>
              {FAQ.map(({ q, a }, i) => (
                <details key={i} style={{
                  border:"1.5px solid var(--pk-border)", borderRadius:"8px",
                  padding:"14px 18px", marginBottom:"10px",
                  background:"var(--pk-surface)", cursor:"pointer",
                }}>
                  <summary style={{
                    fontSize:"16px", fontWeight:700, color:"var(--pk-text)",
                    listStyle:"none", outline:"none",
                  }}>{q}</summary>
                  <p style={{ ...p, marginTop:"10px", marginBottom:0 }}>{a}</p>
                </details>
              ))}
            </section>

            {/* ── Footer CTA ── */}
            <section style={{ marginTop: "80px" }}>
              <div style={{
                ...cardBox,
                textAlign: "center" as const, padding:"40px 24px",
              }}>
                <h2 style={{ ...h2, margin:"0 0 12px" }}>Ready to use PrayerKey?</h2>
                <p style={p}>
                  Pick a feature and start now. Free forever. No account required.
                </p>
                <div style={{ display:"flex", gap:"14px", justifyContent:"center", flexWrap:"wrap", marginTop:"20px" }}>
                  <Link href="/pray" style={ctaBtn(true)}>Generate a prayer</Link>
                  <Link href="/live" style={ctaBtn(false)}>Start live sermon</Link>
                  <Link href="/bible" style={ctaBtn(false)}>Search the Bible</Link>
                </div>
              </div>
            </section>

          </main>
        </div>
      </article>

      {/* ── Layout: 2-col grid on desktop, stacked on mobile ── */}
      <style>{`
        @media (min-width: 960px) {
          .docs-grid { grid-template-columns: 240px minmax(0,1fr) !important; align-items: start; }
          .docs-toc  { position: sticky; top: 84px; }
        }
        .docs-toc a:hover { color: var(--pk-accent) !important; }
        details summary::-webkit-details-marker { display: none; }
        details[open] summary { color: var(--pk-accent); }
      `}</style>
    </>
  );
}

/* ── CTA button styles ─────────────────────────────────────────── */
function ctaBtn(primary: boolean): React.CSSProperties {
  return {
    display:        "inline-flex",
    alignItems:     "center",
    padding:        "12px 22px",
    fontSize:       "14px",
    fontWeight:     700,
    letterSpacing:  "-0.005em",
    textDecoration: "none",
    borderRadius:   "8px",
    border:         primary ? "2px solid var(--pk-accent)" : "2px solid var(--pk-border)",
    background:     primary ? "var(--pk-accent)" : "var(--pk-surface)",
    color:          primary ? "#FFFFFF" : "var(--pk-text)",
    boxShadow:      primary ? "3px 3px 0 0 var(--pk-accent-border)" : "3px 3px 0 0 var(--pk-border)",
    transition:     "transform 150ms ease",
  };
}
