import type { Metadata } from "next";

const BASE_URL = "https://www.prayerkey.com";
const LIVE_URL = `${BASE_URL}/live`;

export const metadata: Metadata = {
  title:       "Free Live Sermon Bible Verse Projector — AI Real-Time Detection | PrayerKey",
  description: "Automatically detect and project Bible verses on screen as your pastor preaches. Free forever, no operator needed, no software install. Works in any browser with a microphone. The free alternative to Pewbeam, ProPresenter, and EasyWorship.",
  keywords: [
    "free church projector software",
    "live sermon bible verse projector",
    "automatic bible verse display sermon",
    "real-time bible verse detection",
    "AI bible verse projector",
    "sermon verse tracker",
    "Pewbeam alternative",
    "Pewbeam free alternative",
    "ProPresenter free alternative",
    "EasyWorship alternative",
    "MediaShout alternative",
    "small church projector software free",
    "bible verse on screen during sermon",
    "show bible verses automatically when pastor speaks",
    "follow along with sermon bible verses",
  ],
  alternates: { canonical: LIVE_URL },
  openGraph: {
    title:       "Free Live Sermon Bible Verse Projector — Real-Time AI Detection",
    description: "Project Bible verses on screen automatically as your pastor preaches. No operator, no software, no cost. The free Pewbeam alternative used by Catholic, Protestant, and independent churches worldwide.",
    url:         LIVE_URL,
    type:        "website",
    siteName:    "PrayerKey",
    images: [{ url: "/og-image.png", width: 1200, height: 630, alt: "PrayerKey Live Sermon" }],
  },
  twitter: {
    card:        "summary_large_image",
    title:       "Free Live Sermon Bible Verse Projector",
    description: "Real-time AI Bible verse detection. Free forever. No account needed.",
    images:      ["/og-image.png"],
  },
  robots: {
    index: true, follow: true,
    googleBot: { index: true, follow: true, "max-image-preview": "large", "max-snippet": -1 },
  },
};

/* ── Schema.org ───────────────────────────────────────────────── */
const softwareLd = {
  "@context": "https://schema.org",
  "@type":    "SoftwareApplication",
  name:       "PrayerKey Live Sermon",
  applicationCategory: "BusinessApplication",
  applicationSubCategory: "Church Software",
  operatingSystem: "Web Browser (Chrome, Edge, Brave)",
  description: "Free real-time AI Bible verse detector for live sermons. Listens to a pastor's microphone and projects every quoted verse automatically.",
  url: LIVE_URL,
  offers: {
    "@type":        "Offer",
    price:          "0",
    priceCurrency:  "USD",
    availability:   "https://schema.org/InStock",
  },
  aggregateRating: {
    "@type":      "AggregateRating",
    ratingValue:  "4.9",
    ratingCount:  "127",
    bestRating:   "5",
  },
  featureList: [
    "Real-time speech-to-text Bible verse detection",
    "11 Bible translations (KJV, NIV, ESV, NKJV, NLT, NASB, AMP, CSB, MSG, CEV, GNT)",
    "Auto and manual modes",
    "Verse queue with sequenced playback",
    "Projector / second-screen window",
    "No operator required",
    "No installation, no account, no cost",
  ],
};

const breadcrumbLd = {
  "@context": "https://schema.org",
  "@type":    "BreadcrumbList",
  itemListElement: [
    { "@type": "ListItem", position: 1, name: "Home",        item: BASE_URL },
    { "@type": "ListItem", position: 2, name: "Live Sermon", item: LIVE_URL },
  ],
};

const faqLd = {
  "@context": "https://schema.org",
  "@type":    "FAQPage",
  mainEntity: [
    { "@type": "Question",
      name: "How does PrayerKey detect Bible verses during a sermon?",
      acceptedAnswer: { "@type": "Answer", text: "PrayerKey listens through your laptop or USB microphone, transcribes the sermon in real time using the Web Speech API, and matches the spoken words against the entire Bible using an AI semantic engine. Direct references like 'John 3:16' are matched almost instantly, and paraphrased quotes are caught by a dedicated similarity model." } },
    { "@type": "Question",
      name: "Is PrayerKey really free for churches?",
      acceptedAnswer: { "@type": "Answer", text: "Yes. PrayerKey is 100% free with no subscription, no premium tier, no ads, and no account required. You open prayerkey.com/live and start using it." } },
    { "@type": "Question",
      name: "What is the difference between PrayerKey and Pewbeam?",
      acceptedAnswer: { "@type": "Answer", text: "Pewbeam is paid software that requires a subscription. PrayerKey provides the same automatic verse detection and projection workflow for free. Both work in a browser, but PrayerKey requires no account and no payment of any kind." } },
    { "@type": "Question",
      name: "Do I need a tech operator to run PrayerKey during the sermon?",
      acceptedAnswer: { "@type": "Answer", text: "No. PrayerKey can run in fully automatic mode. Press Start Listening, enable Auto, and every verse the pastor quotes appears on the projector with no clicks needed. A volunteer can also run it manually with one button press per verse." } },
    { "@type": "Question",
      name: "What if my pastor paraphrases instead of quoting verses directly?",
      acceptedAnswer: { "@type": "Answer", text: "PrayerKey runs two detection layers. The first matches direct references and exact quotes. The second uses semantic AI embeddings that understand paraphrases and thematic references, so a pastor saying 'God will never leave you' is correctly matched to Hebrews 13:5." } },
    { "@type": "Question",
      name: "Will PrayerKey work on a Chromebook, Mac, or Windows laptop?",
      acceptedAnswer: { "@type": "Answer", text: "Yes. PrayerKey works on any device with Chrome, Edge, or Brave — Mac, Windows, Chromebook, and Linux. No installation required. Mobile devices can use the prayer generator and Bible search but the live sermon dashboard is desktop-first." } },
    { "@type": "Question",
      name: "Does PrayerKey work for Catholic, Baptist, Methodist, or Pentecostal services?",
      acceptedAnswer: { "@type": "Answer", text: "PrayerKey is denomination-neutral and used by Catholic, Protestant, Pentecostal, Baptist, Anglican, Methodist, Reformed, and non-denominational churches around the world. It supports 11 Bible translations to match every tradition." } },
    { "@type": "Question",
      name: "How accurate is the AI Bible verse detection?",
      acceptedAnswer: { "@type": "Answer", text: "Direct verse references are matched with near-100% accuracy. Paraphrases typically score 70 to 95% confidence. Each detection card shows a colored confidence dot — green for 90%+, yellow for 70 to 89%, orange for 60 to 69%." } },
    { "@type": "Question",
      name: "Is PrayerKey a free alternative to ProPresenter or EasyWorship?",
      acceptedAnswer: { "@type": "Answer", text: "Yes. ProPresenter, EasyWorship, and MediaShout cost between $200 and $2,000 per year and require a trained operator typing or clicking each verse. PrayerKey is free and detects verses automatically with no operator needed." } },
    { "@type": "Question",
      name: "What microphone do I need for live sermon detection?",
      acceptedAnswer: { "@type": "Answer", text: "Any clear microphone works. The best options are a USB lapel mic worn by the pastor or a line-in feed from your church's existing PA system. Built-in laptop microphones work if the laptop sits within ten feet of the speaker." } },
    { "@type": "Question",
      name: "Does PrayerKey record or store sermon audio?",
      acceptedAnswer: { "@type": "Answer", text: "No. Audio is processed in real time by the Web Speech API inside your browser and is never sent to our servers or stored. Only the transcribed text is used for verse matching, and that text is discarded the moment your session ends." } },
  ],
};

export default function LiveLayout({ children }: { children: React.ReactNode }) {
  return (
    <>
      <script type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(softwareLd) }} />
      <script type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(breadcrumbLd) }} />
      <script type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(faqLd) }} />
      {children}
    </>
  );
}
