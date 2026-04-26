import type { ReactNode } from "react";
import type { Metadata } from "next";

const BASE_URL  = "https://www.prayerkey.com";
const ABOUT_URL = `${BASE_URL}/about`;

export const metadata: Metadata = {
  title:       "About PrayerKey — Free AI Church Companion | Built by Collins Asein",
  description: "PrayerKey is a free AI-powered church companion built for every pastor and believer. Live sermon verse detection, AI prayers, and Bible search — free forever. Built by Collins Omoikhudu Asein.",
  alternates:  { canonical: ABOUT_URL },
  openGraph: {
    title:       "About PrayerKey — Free AI Church Companion",
    description: "Free AI-powered church technology for every pastor and believer. Built by Collins Omoikhudu Asein.",
    url:         ABOUT_URL,
    type:        "website",
    siteName:    "PrayerKey",
    images: [{ url: "/og-image.png", width: 1200, height: 630, alt: "About PrayerKey" }],
  },
};

const organizationLd = {
  "@context": "https://schema.org",
  "@type":    "Organization",
  name:       "PrayerKey",
  url:        BASE_URL,
  logo:       `${BASE_URL}/og-image.png`,
  description: "Free AI-powered church companion: live sermon Bible-verse detection, AI prayer generator, and Bible search.",
  founder: {
    "@type": "Person",
    name:    "Collins Omoikhudu Asein",
  },
  sameAs: [
    "https://github.com/adegbe11/PrayerKey",
  ],
};

const breadcrumbLd = {
  "@context": "https://schema.org",
  "@type":    "BreadcrumbList",
  itemListElement: [
    { "@type": "ListItem", position: 1, name: "Home",  item: BASE_URL },
    { "@type": "ListItem", position: 2, name: "About", item: ABOUT_URL },
  ],
};

export default function AboutPage() {
  return (
    <article style={{ maxWidth: "760px", margin: "0 auto", padding: "0 0 100px" }}>
      <script type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(organizationLd) }} />
      <script type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(breadcrumbLd) }} />


      {/* ── Hero ── */}
      <div className="animate-fadeUp" style={{ marginBottom: "64px" }}>
        <div style={{
          display:      "inline-flex",
          alignItems:   "center",
          padding:      "4px 12px",
          border:       "1.5px solid var(--pk-accent-border)",
          borderRadius: "4px",
          marginBottom: "24px",
          background:   "var(--pk-accent-dim)",
          boxShadow:    "3px 3px 0 0 var(--pk-accent-border)",
        }}>
          <span style={{ fontSize: "10px", fontWeight: 700, color: "var(--pk-accent)", letterSpacing: "0.12em", textTransform: "uppercase" }}>
            About PrayerKey
          </span>
        </div>

        <h1 style={{
          fontSize:      "clamp(32px, 5vw, 56px)",
          fontWeight:    800,
          color:         "var(--pk-text)",
          margin:        "0 0 20px",
          letterSpacing: "-0.03em",
          lineHeight:    1.08,
        }}>
          We built PrayerKey for every church, not just the big ones.
        </h1>
        <p style={{
          fontSize:   "clamp(16px, 1.5vw, 19px)",
          color:      "var(--pk-text-2)",
          lineHeight: 1.75,
          margin:     0,
          maxWidth:   "640px",
        }}>
          PrayerKey is a free AI-powered church companion built to help pastors preach better, believers pray deeper, and congregations stay connected with the Word of God.
        </p>
      </div>

      {/* ── Sections ── */}
      <div style={{ display: "flex", flexDirection: "column", gap: "0" }}>

        <Section title="Our Mission" tag="01">
          For too long, the most powerful ministry tools have only been available to large, well-funded churches. Small congregations, home churches, and independent pastors have been left to manage Sunday services with whatever they can piece together. PrayerKey exists to change that. We believe every pastor who stands behind a pulpit deserves the same quality of tools as the biggest churches in the world, regardless of their budget or the size of their congregation. Our mission is to make AI-powered church technology completely free and accessible to anyone who needs it.
        </Section>

        <Section title="What PrayerKey Does" tag="02">
          PrayerKey is built around three core features that work together to serve the full life of a church.

          {"\n\n"}The first is Live Sermon Verse Detection. When a pastor is preaching, PrayerKey listens through the microphone and automatically identifies every Bible verse being quoted or referenced in real time. The moment a verse is detected, it appears on a connected projector screen for the congregation to read along. There is no operator required and no manual typing. The pastor simply preaches and the technology does the rest. Every verse comes with a confidence score so you always know how strong the match is, and the feature supports eleven major Bible translations including NIV, KJV, ESV, NKJV and NLT.

          {"\n\n"}The second is the AI Prayer Generator. A pastor preparing for a service, a believer going through a difficult season, or a church member who simply does not know how to put their heart into words can type what they are feeling and PrayerKey writes a full, scripture-grounded prayer tailored to exactly what they described. Every generated prayer includes relevant Bible verses and an encouragement note. The prayers are personal, meaningful and ready to use in under ten seconds.

          {"\n\n"}The third is Bible Search with Cross-References. PrayerKey lets you search the entire Bible by verse reference, keyword, topic or even a loose paraphrase of a scripture you half-remember. When you find a verse, you can instantly load four to six deeply related cross-references that share the same theme, doctrine or wording. This makes PrayerKey useful not just on Sunday mornings but throughout the week for sermon preparation, Bible study and personal devotion.
        </Section>

        <Section title="Who PrayerKey Is For" tag="03">
          PrayerKey is for pastors who want to make their sermons more accessible and engaging. It is for church leaders who want their congregation to follow along with scripture without the cost of expensive software. It is for ministers preparing a funeral, a wedding, a dedication or any special ceremony who need the right words quickly. It is for believers who pray daily and want their prayers to go deeper and be grounded in the Word. It is for Bible study leaders who need to find and connect scriptures faster. PrayerKey is denomination-neutral and works for Catholic, Protestant, Pentecostal, Baptist, Anglican, Methodist, non-denominational and all other Christian traditions.
        </Section>

        <Section title="No Account. No Payment. No Barrier." tag="04">
          PrayerKey requires no sign-up, no login and no payment of any kind. You open the website and you start using it. We made this decision intentionally because we believe a pastor in a rural village deserves the same access as a pastor in a city megachurch. Adding a subscription or a registration wall would mean that the people who need this tool the most would be the ones least likely to access it. PrayerKey is and will remain free to use.
        </Section>

        <Section title="The Technology Behind PrayerKey" tag="05">
          PrayerKey is built on modern AI infrastructure designed for reliability and speed. Verse detection uses a combination of real-time speech recognition and a semantic Bible matching engine that understands not just exact quotes but paraphrases and thematic references. The prayer generator uses a large language model trained to write with theological sensitivity, drawing from scripture to produce prayers that feel genuinely personal rather than generic. The Bible search engine understands natural language queries and connects results to a comprehensive cross-reference database covering all 66 books of the Bible.
        </Section>

        <Section title="Built by Collins Omoikhudu Asein" tag="06" last>
          PrayerKey was designed and built by Collins Omoikhudu Asein. The vision behind PrayerKey came from a simple observation: the church community around the world is full of dedicated, passionate people serving God with limited resources. Technology should be a blessing to the church, not a business model built on top of it. PrayerKey is an independent platform with no investor pressure and no agenda other than to serve pastors and believers well.
        </Section>

      </div>
    </article>
  );
}

function Section({ title, tag, children, last = false }: { title: string; tag: string; children: ReactNode; last?: boolean }) {
  return (
    <div style={{
      display:       "grid",
      gridTemplateColumns: "auto 1fr",
      gap:           "clamp(20px, 4vw, 48px)",
      padding:       "clamp(32px, 5vw, 52px) 0",
      borderBottom:  last ? "none" : "1px solid var(--pk-border)",
    }}>
      {/* Tag column */}
      <div style={{ paddingTop: "4px" }}>
        <span style={{
          display:       "block",
          fontSize:      "10px",
          fontWeight:    700,
          color:         "var(--pk-accent)",
          letterSpacing: "0.1em",
          border:        "1px solid var(--pk-accent-border)",
          padding:       "4px 8px",
          borderRadius:  "3px",
          textAlign:     "center",
          whiteSpace:    "nowrap",
          background:    "var(--pk-accent-dim)",
        }}>
          {tag}
        </span>
      </div>

      {/* Content column */}
      <div>
        <h2 style={{
          fontSize:      "clamp(18px, 2.2vw, 24px)",
          fontWeight:    700,
          color:         "var(--pk-text)",
          margin:        "0 0 16px",
          letterSpacing: "-0.02em",
          lineHeight:    1.2,
        }}>
          {title}
        </h2>
        <div style={{
          fontSize:    "clamp(14px, 1.3vw, 16px)",
          color:       "var(--pk-text-2)",
          lineHeight:  1.85,
          whiteSpace:  "pre-wrap",
        }}>
          {children}
        </div>
      </div>
    </div>
  );
}
