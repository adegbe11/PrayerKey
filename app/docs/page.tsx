import type { Metadata } from "next";
import type { CSSProperties } from "react";
import Link from "next/link";
import InView from "@/components/home/InView";
import { ReadingHero, ReadingLayout, Section, Steps, Tip, Faq, ReadingEnd } from "@/components/reading/Reading";
import "@/components/reading/reading.css";

const BASE_URL    = "https://www.prayerkey.com";
const DOCS_URL    = `${BASE_URL}/docs`;
const PUBLISHED   = "2026-04-26";
const MODIFIED    = "2026-04-26";

export const metadata: Metadata = {
  title:       "How to Use PrayerKey — Complete Documentation & User Guide",
  description: "Step-by-step guide to every PrayerKey feature: live sermon Bible-verse detection, the free prayer generator, Bible search with cross-references, the prayer library, and more. Free, no account required.",
  keywords: [
    "prayerkey docs", "how to use prayerkey", "prayerkey guide",
    "live sermon bible verse detector tutorial", "free prayer generator how to",
    "bible verse search cross references", "prayerkey faq",
    "free church projector software", "sermon verse display tool",
    "real-time bible verse detection",
  ],
  alternates: { canonical: DOCS_URL },
  openGraph: {
    title:       "How to Use PrayerKey — Complete Documentation",
    description: "Everything you need to use PrayerKey: live sermon verse detection, prayers built on Scripture, Bible search, and 543 prayers built on Scripture. Free forever.",
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
  { id: "prayer-generator",   label: "Prayer Generator"                    },
  { id: "bible-search",       label: "Bible Search & Cross-References"     },
  { id: "prayer-library",     label: "Prayer Library"                      },
  { id: "translations",       label: "Bible Translation"                   },
  { id: "projector",          label: "Projector & Second Screen"           },
  { id: "browsers",           label: "Browsers & Devices"                  },
  { id: "shortcuts",          label: "Keyboard Shortcuts"                  },
  { id: "privacy",            label: "Privacy, Audio & Data"               },
  { id: "troubleshooting",    label: "Troubleshooting"                     },
  { id: "denominations",      label: "Denominations & Theology"            },
  { id: "faq",                label: "Questions"                           },
];

/* ── FAQ data — also serialized to JSON-LD for rich results ──── */
const FAQ: { q: string; a: string }[] = [
  { q: "Is PrayerKey really free?",
    a: "Yes. PrayerKey is 100% free with no account, no subscription, and no payment of any kind. There is no premium tier. The only thing we ask is that you tell another pastor about it." },
  { q: "Do I need to create an account to use PrayerKey?",
    a: "No. Open the website and start using any feature immediately. You do not need to register, log in, or provide an email address." },
  { q: "Which Bible translation does PrayerKey use?",
    a: "PrayerKey uses the King James Version (KJV) for Bible search, verse pages and the live sermon projector. It is the full text of all 66 books and is in the public domain, so churches can project it freely." },
  { q: "Does the live sermon detection work without internet?",
    a: "No. Live verse detection needs an internet connection: your browser's speech recognition turns the sermon into text, and that text is matched against the Bible on our servers. A stable Wi-Fi connection of at least 1 Mbps is recommended." },
  { q: "Which browsers work with PrayerKey?",
    a: "Live sermon verse detection needs Chrome or Edge, which include the speech recognition it listens with. Brave and Opera block it. Safari and Firefox can use the prayer generator and Bible search but have limited speech recognition support." },
  { q: "Does PrayerKey record or store my sermon audio?",
    a: "No. Audio is turned into text in real time by your browser's speech recognition and is never stored on our servers. Only the transcribed text is sent for verse matching, and that text is discarded after the session ends." },
  { q: "How accurate is the verse detection?",
    a: "PrayerKey listens in two ways. Direct references like 'John 3:16' are matched with near-100% accuracy. Quotes and close paraphrases are matched by comparing the words spoken against the text of the whole Bible, and only confident matches are shown. A confidence dot on each detection card shows the match strength." },
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
    a: "Yes. The prayer generator is especially useful for funerals, weddings, baby dedications, hospital visits, and any service where you need scripture-grounded words quickly." },
  { q: "Does PrayerKey support languages other than English?",
    a: "PrayerKey currently supports English. Multilingual sermon detection is on the roadmap. If you would like a specific language prioritized, please reach out through the contact link in the footer." },
  { q: "Can I download or save the prayers I generate?",
    a: "Yes. Every prayer has a 'Copy' button to copy it to your clipboard and a 'Make a card' option that creates a shareable image card." },
  { q: "Are the generated prayers theologically sound?",
    a: "Every prayer is composed from Scripture-based prayer patterns and comes with real Bible verses and their references. We recommend pastors review a prayer before using it from the pulpit, just as you would any other prayer resource." },
  { q: "How fast is the prayer generator?",
    a: "The prayer is composed on your device the moment you press Pray, then written onto the page over a few seconds. The result includes the prayer and its supporting scripture references." },
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
  description: "Complete guide to PrayerKey's free prayer generator, real-time live sermon Bible-verse detection, and full-Bible search with cross-references.",
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
    "Live Sermon Verse Detection", "Prayer Generator",
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

// stagger index for list items that enter one after another
const n = (i: number) => ({ "--i": i } as CSSProperties);

export default function DocsPage() {
  return (
    <div className="rd">
      {/* ── Schema.org JSON-LD ── */}
      <script type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(breadcrumbLd) }} />
      <script type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(articleLd) }} />
      <script type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(howToLd) }} />
      <script type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(faqLd) }} />
      <InView />

      <ReadingHero
        crumbs={[{ href: "/", label: "Home" }, { label: "Documentation" }]}
        title="How to Use PrayerKey"
        lede={<>A plain guide to every part of PrayerKey: live sermon verse detection, the prayer generator,
          Bible search with cross-references, and the library of 543 prayers. No account, no payment, no setup
          beyond opening the site.</>}
        meta={<>Last updated <time dateTime={MODIFIED}>April 26, 2026</time> · Written by{" "}
          <Link href="/author/collins-asein">Collins Omoikhudu Asein</Link></>}
      />

      <ReadingLayout toc={TOC}>

        <Section id="quick-start" title="Quick Start">
          <p>PrayerKey works without installation, login, or payment. Pick the feature you need.</p>
          <ol className="rd-starts">
            <li style={n(0)}>
              <span className="rd-starts__n" aria-hidden>1</span>
              <div>
                <h3>Generate a prayer</h3>
                <p>
                  Go to <Link href="/pray">prayerkey.com/pray</Link>, type what you&apos;re feeling or what you
                  need prayer for, choose how you feel, and press <strong>Pray</strong>. A complete prayer built
                  on Scripture is written onto the page in a few seconds.
                </p>
                <Link href="/pray" className="rd-starts__go">Open the prayer generator</Link>
              </div>
            </li>
            <li style={n(1)}>
              <span className="rd-starts__n" aria-hidden>2</span>
              <div>
                <h3>Run live sermon detection</h3>
                <p>
                  Go to <Link href="/live">prayerkey.com/live</Link>, allow microphone access, click{" "}
                  <strong>Start Listening</strong>, and open the projector window on your second screen. PrayerKey
                  transcribes the sermon and shows every Bible verse the pastor quotes, automatically.
                </p>
                <Link href="/live" className="rd-starts__go">Open the live dashboard</Link>
              </div>
            </li>
            <li style={n(2)}>
              <span className="rd-starts__n" aria-hidden>3</span>
              <div>
                <h3>Search the Bible</h3>
                <p>
                  Go to <Link href="/bible">prayerkey.com/bible</Link> and type a reference (<code>John 3:16</code>),
                  a topic (<code>do not fear</code>), or a paraphrase. Tap any result to see four to six related
                  cross-reference verses.
                </p>
                <Link href="/bible" className="rd-starts__go">Open Bible search</Link>
              </div>
            </li>
          </ol>
        </Section>

        <Section id="what-is" title="What Is PrayerKey?">
          <p>PrayerKey is a free, browser-based companion for pastors and believers. It does three things well:</p>
          <ul>
            <li>
              <strong>Listens to a live sermon</strong> and automatically pushes every quoted Bible verse to a
              projector screen in real time, with no operator needed.
            </li>
            <li>
              <strong>Composes personal prayers built on Scripture</strong> from a short description of what
              you&apos;re going through.
            </li>
            <li>
              <strong>Searches all 31,102 Bible verses</strong> by reference, keyword, or paraphrase, and returns
              four to six thematic cross-references for any verse.
            </li>
          </ul>
          <p>
            The whole platform is denomination-neutral, account-free, and free of ads, tracking, and paid tiers.
            There is no premium plan to upsell.
          </p>
        </Section>

        <Section id="live-sermon" title="Live Sermon Verse Detection">
          <p>
            The live dashboard at <Link href="/live">/live</Link> turns any laptop with a microphone into an
            automatic verse display. Plug into your church&apos;s audio feed (or use the laptop mic), press start,
            and PrayerKey uses speech recognition to follow the sermon and match what is said against the Bible.
          </p>

          <h3>Setup, in about three minutes</h3>
          <Steps>
            <li style={n(0)}>Open <code>prayerkey.com/live</code> in Chrome or Edge.</li>
            <li style={n(1)}>Tap the button to start listening, and click <strong>Allow</strong> when the browser asks for microphone access.</li>
            <li style={n(2)}>Click <strong>Projector ↗</strong> in the header. A new window opens; drag it to your second screen and click it once for full screen.</li>
            <li style={n(3)}>Press <kbd>A</kbd> or the <strong>Auto</strong> switch so verses go on the screen by themselves, or leave it off and choose each one.</li>
            <li style={n(4)}>Verses appear in King James wording, whichever translation the pastor quotes from.</li>
          </Steps>

          <h3>The dashboard layout</h3>
          <p>The live dashboard is split into two rows and a tall right sidebar:</p>
          <dl className="rd-terms rd-terms--wide">
            <div><dt>Transcript</dt><dd>The left column. Every word the pastor speaks, scrolling in real time. Verses spotted in the transcript are highlighted in gold.</dd></div>
            <div><dt>Program · Preview</dt><dd>The verse you&apos;re about to send live. Stage a verse here before pushing it to the projector.</dd></div>
            <div><dt>Live · Display</dt><dd>The verse currently visible to the congregation. Mirrors what the projector shows.</dd></div>
            <div><dt>Detections / Queue</dt><dd>The right sidebar. A list of every verse PrayerKey heard, plus a queue you can build for playback in order.</dd></div>
            <div><dt>Search</dt><dd>The bottom row, a manual fallback. Look up any verse by book and chapter, or by topic, if a verse was missed.</dd></div>
          </dl>

          <h3>Auto mode and manual mode</h3>
          <p>
            Toggle <strong>Auto</strong> in the header to make every confidently detected verse go straight to the
            live screen. Leave it off and you stay in control: each verse waits in Detections until you click{" "}
            <strong>▶ Present</strong>.
          </p>
          <Tip>
            Most operators use both: Auto on for sermons with rapid back-to-back quotes, manual for verse-by-verse
            expository preaching where pacing matters.
          </Tip>

          <h3>Building a queue</h3>
          <p>
            When the pastor names several verses ahead of time (<em>we&apos;ll look at Romans 8, then Psalm 23, then
            Isaiah 41</em>), click <strong>+Q</strong> on each detected verse to add it to the queue in order. Switch the
            right sidebar to the <strong>Queue</strong> tab and play each one when the pastor reaches it.
          </p>

          <h3>Detection confidence</h3>
          <p>Every detection card has a coloured dot:</p>
          <ul>
            <li><span className="rd-dot" style={{ "--c": "#34C759" } as CSSProperties} aria-hidden /><strong>Green</strong>: 90%+ confidence (direct reference or near-exact quote).</li>
            <li><span className="rd-dot" style={{ "--c": "#F4D03F" } as CSSProperties} aria-hidden /><strong>Yellow</strong>: 70–89% confidence (strong paraphrase).</li>
            <li><span className="rd-dot" style={{ "--c": "#FF9F0A" } as CSSProperties} aria-hidden /><strong>Orange</strong>: 60–69% confidence (loose theme match; review before sending live).</li>
          </ul>
        </Section>

        <Section id="prayer-generator" title="Prayer Generator">
          <p>
            Visit <Link href="/pray">/pray</Link> and type a few sentences about what you need prayer for. Your
            prayer is composed on your device from Scripture-based prayer patterns, so nothing you type leaves the
            page, and it is written out in a few seconds with the Bible verses it rests on.
          </p>

          <h3>Writing a good request</h3>
          <p>Better words in, a truer prayer out. Three habits help:</p>
          <ul>
            <li><strong>Be specific.</strong> <em>I&apos;m anxious about Friday&apos;s surgery</em> gives a far more focused prayer than <em>I&apos;m worried</em>.</li>
            <li><strong>Choose how you feel</strong> (Grateful, Anxious, Sad, Hopeful, Confused, Joyful, Sick, Tired). Your choice guides the tone of the prayer.</li>
            <li><strong>Include the person if relevant.</strong> <em>My daughter Maya is starting kindergarten</em> gives the prayer a name to lift up.</li>
          </ul>

          <h3>What you receive</h3>
          <ul>
            <li>A short title naming the prayer&apos;s focus.</li>
            <li>The full prayer, addressed to God.</li>
            <li>Supporting Bible verses with their references, drawn from the prayer&apos;s theme.</li>
          </ul>

          <h3>Saving and sharing</h3>
          <p>
            Each prayer has a <strong>Copy</strong> button (plain text to your clipboard) and a{" "}
            <strong>Make a card</strong> option that creates a shareable image you can text, post, or print. Where
            your browser can read aloud, <strong>Listen</strong> reads the prayer to you.
          </p>
        </Section>

        <Section id="bible-search" title="Bible Search & Cross-References">
          <p>The Bible search at <Link href="/bible">/bible</Link> understands three kinds of queries:</p>
          <dl className="rd-terms">
            <div><dt>Direct references</dt><dd><code>John 3:16</code>, <code>Ps 23</code>, <code>1 Cor 13:4-7</code></dd></div>
            <div><dt>Keywords or topics</dt><dd><code>do not fear</code>, <code>healing</code>, <code>grace</code></dd></div>
            <div><dt>Paraphrases</dt><dd><code>the verse about being still and knowing God</code></dd></div>
          </dl>

          <h3>Cross-references</h3>
          <p>
            Click any result to load four to six related verses, each with a one-line reason explaining the
            connection: the same theme, a related doctrine, or shared imagery. Useful for sermon preparation, Bible
            study, and small-group discussion.
          </p>

          <h3>Translation</h3>
          <p>
            Search results and verse text come from the King James Version (see <a href="#translations">Bible Translation</a>).
          </p>
        </Section>

        <Section id="prayer-library" title="Prayer Library, 543 Prayers">
          <p>Visit <Link href="/prayer">/prayer</Link> to browse the prayer library, organised three ways:</p>
          <dl className="rd-terms rd-terms--wide">
            <div><dt>By category (16)</dt><dd>Health, Mental Health, Daily Prayer, Family, Finance, Protection, Faith, Grief, Celebration, Education, Salvation, Purpose, Relationships, Thanksgiving, Ministry, Nation.</dd></div>
            <div><dt>By time of day (6)</dt><dd>Morning, Night, Daily, Short, Powerful, Fasting.</dd></div>
            <div><dt>By how you feel (12)</dt><dd>Anxious, Depressed, Grieving, Angry, Lonely, Afraid, Hopeful, Grateful, Stressed, Confused, Joyful, Sick.</dd></div>
          </dl>
          <p>
            Every prayer is grounded in real scripture and ready to read aloud, share, or use as a starting point
            for your own words.
          </p>
        </Section>

        <Section id="translations" title="Bible Translation">
          <p>
            PrayerKey uses the King James Version (KJV) across the website: Bible search, every verse page, the
            verse of the day and the live sermon projector. It is the complete text of all 66 books, and because
            it is in the public domain, a church can put any verse on its screen without a licence.
          </p>
        </Section>

        <Section id="projector" title="Projector & Second Screen">
          <p>
            The projector window is a separate page at <code>/live/projector</code> that mirrors only the live
            verse: no controls, no transcript, just the verse on a clean dark background suitable for any
            congregation.
          </p>
          <h3>Recommended setup</h3>
          <Steps>
            <li style={n(0)}>Connect your laptop to the projector by HDMI or wireless cast.</li>
            <li style={n(1)}>Set your displays to <strong>Extended</strong> mode (not Mirrored).</li>
            <li style={n(2)}>Open the live dashboard on your laptop screen.</li>
            <li style={n(3)}>Click <strong>Projector ↗</strong>. A new window opens.</li>
            <li style={n(4)}>Drag the new window onto the projector display and press <kbd>F11</kbd> for fullscreen.</li>
          </Steps>
        </Section>

        <Section id="browsers" title="Browsers & Devices">
          <h3 style={{ marginTop: 0 }}>Recommended</h3>
          <ul>
            <li>Google Chrome 110+ on Mac, Windows, or Chromebook.</li>
            <li>Microsoft Edge 110+.</li>
            <li>Stable internet, at least 1 Mbps, for live detection.</li>
            <li>A microphone, built-in or USB lapel, for live sermon mode.</li>
          </ul>
          <h3>Limited support</h3>
          <p>
            Safari and Firefox can use the prayer generator, prayer library, and Bible search without issues. Live
            sermon detection is restricted because their Web Speech API support is incomplete.
          </p>
          <h3>Mobile</h3>
          <p>
            Every feature except the live sermon dashboard works on iOS and Android. The live dashboard is
            desktop-first because it needs a microphone and a second display.
          </p>
        </Section>

        <Section id="shortcuts" title="Keyboard Shortcuts">
          <p>These work in the live dashboard.</p>
          <dl className="rd-terms rd-terms--keep">
            <div><dt><kbd>↵ Enter</kbd></dt><dd>Send the staged verse live</dd></div>
            <div><dt><kbd>esc</kbd></dt><dd>Clear the live screen</dd></div>
            <div><dt><kbd>A</kbd></dt><dd>Toggle Auto mode</dd></div>
            <div><dt><kbd>Q</kbd></dt><dd>Add the staged verse to the Queue</dd></div>
            <div><dt><kbd>C</kbd></dt><dd>Blank the projector (for example between sermon points)</dd></div>
          </dl>
        </Section>

        <Section id="privacy" title="Privacy, Audio & Data">
          <p>
            PrayerKey is built privacy-first. We do not record audio. We do not store sermon transcripts. We do not
            have user accounts, so there is no profile, history, or behavioural data tied to you.
          </p>
          <p>
            During a live session, audio is turned into text by your browser&apos;s speech recognition (the Web
            Speech API). Only the resulting text is sent to our servers for verse matching, and that text is
            discarded the moment the session ends. Read the full <Link href="/privacy">Privacy Policy</Link>.
          </p>
        </Section>

        <Section id="troubleshooting" title="Troubleshooting">
          <h3 style={{ marginTop: 0 }}>The microphone won&apos;t start</h3>
          <p>
            Check your browser&apos;s permission icon (left of the address bar). Click it, set Microphone to{" "}
            <strong>Allow</strong>, and reload the page. On a Mac, also confirm your browser has microphone access
            in <strong>System Settings → Privacy → Microphone</strong>.
          </p>

          <h3>Verses are detected but the wrong translation shows</h3>
          <p>The translation dropdown in the header controls all output. Switch it before going live and reload if needed.</p>

          <h3>Detection is missing verses</h3>
          <p>
            Three usual causes: background noise, distance from the speaker, or the pastor paraphrasing rather than
            quoting.
          </p>
          <Tip>
            Move the laptop closer or use a lapel mic, and use <strong>Context Search</strong> as a manual fallback
            for paraphrases.
          </Tip>

          <h3>The projector window opens behind the dashboard</h3>
          <p>
            Some browsers open new windows on the active display. Drag it to the projector, then press{" "}
            <kbd>F11</kbd> to go fullscreen. If pop-ups are blocked, click the blocked pop-up icon and
            allow <code>prayerkey.com</code>.
          </p>

          <h3>The prayer generator shows nothing</h3>
          <p>Make sure you have typed a request, then press <strong>Pray</strong> again. If it persists, refresh the page.</p>
        </Section>

        <Section id="denominations" title="Denominations & Theology">
          <p>
            PrayerKey is denomination-neutral. The Bible search returns the actual text of the translation you
            select, and the prayer generator draws on prayer patterns written for broad theological sensitivity
            rather than a single tradition.
          </p>
          <p>
            It&apos;s used by Catholic, Protestant, Pentecostal, Baptist, Anglican, Methodist, Reformed, Evangelical,
            and non-denominational churches around the world.
          </p>
        </Section>

        <Section id="faq" title="Questions">
          <Faq items={FAQ} />
        </Section>

        <ReadingEnd title="Ready to use PrayerKey?">
          <p>Pick a feature and start now. Free forever. No account required.</p>
          <div className="rd-end__row">
            <Link href="/pray" className="rd-btn">Generate a prayer</Link>
            <Link href="/live" className="rd-quiet">Start live sermon</Link>
            <Link href="/bible" className="rd-quiet">Search the Bible</Link>
          </div>
        </ReadingEnd>

      </ReadingLayout>
    </div>
  );
}
