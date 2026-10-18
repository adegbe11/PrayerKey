import type { Metadata } from "next";
import Link from "next/link";
import InView from "@/components/home/InView";
import PrayerCard from "@/components/prayer/PrayerCard";
import GuideCover from "@/components/guides/GuideCover";
import { PRAYER_TOPICS } from "@/lib/seo/prayer-topics";
import { GUIDES } from "@/lib/seo/guides";
import { guideArt } from "@/lib/seo/guide-art";
import "@/components/prayer/cards.css";
import "@/components/guides/guides.css";
import "./author.css";

const BASE_URL = "https://www.prayerkey.com";

export const metadata: Metadata = {
  title: "Collins Asein — Christian Author & Founder of PrayerKey",
  description:
    "Collins Asein is a Christian author, faith technologist, and the founder of PrayerKey, the free prayer and Bible companion serving believers worldwide.",
  alternates: { canonical: `${BASE_URL}/author/collins-asein` },
  openGraph: {
    title:       "Collins Asein — Christian Author & Founder of PrayerKey",
    description: "Collins Asein is a Christian author, faith technologist, and the founder of PrayerKey, the free prayer and Bible companion serving believers worldwide.",
    url:         `${BASE_URL}/author/collins-asein`,
    siteName:    "PrayerKey",
    images:      [{ url: "/og-image.png", width: 1200, height: 630, alt: "Collins Asein" }],
    type:        "profile",
  },
  twitter: {
    card:        "summary_large_image",
    title:       "Collins Asein — Christian Author & Founder of PrayerKey",
    description: "Collins Asein is a Christian author, faith technologist, and the founder of PrayerKey.",
  },
};

const FEATURED_PRAYERS = PRAYER_TOPICS.slice(0, 8);

// what he writes about, said as a sentence rather than a row of boxes
const SUBJECTS = [
  "prayer and intercession",
  "Bible study",
  "devotional writing",
  "sermon preparation",
  "church technology",
  "apologetics and theology",
  "carrying the Gospel to every nation",
];

// the rest of his work on PrayerKey
const MORE = [
  { label: "Write a prayer",        href: "/pray" },
  { label: "Morning prayer",        href: "/pray/morning" },
  { label: "PrayerKey Live",        href: "/live" },
  { label: "Every prayer topic",    href: "/pray/topics" },
  { label: "Search the Bible",      href: "/bible" },
];

export default function CollinsAseinPage() {
  const jsonLd = {
    "@context":   "https://schema.org",
    "@graph": [
      {
        "@type":       "Person",
        "@id":         `${BASE_URL}/author/collins-asein`,
        "name":        "Collins Asein",
        "url":         `${BASE_URL}/author/collins-asein`,
        "jobTitle":    "Christian Author & Faith Technologist",
        "description": "Collins Asein is a Christian author, faith technologist, and the founder of PrayerKey, a free prayer and Bible study platform serving believers worldwide.",
        "knowsAbout":  ["Christian Prayer", "Bible Study", "Faith Technology", "Church Technology", "Worship", "Sermon Preparation"],
        "worksFor": {
          "@type": "Organization",
          "name":  "PrayerKey",
          "url":   BASE_URL,
        },
        "sameAs": [BASE_URL],
      },
      {
        "@type":        "ProfilePage",
        "@id":          `${BASE_URL}/author/collins-asein#profilepage`,
        "url":          `${BASE_URL}/author/collins-asein`,
        "name":         "Collins Asein — Author Profile",
        "mainEntity":   { "@id": `${BASE_URL}/author/collins-asein` },
        "breadcrumb": {
          "@type":           "BreadcrumbList",
          "itemListElement": [
            { "@type": "ListItem", "position": 1, "name": "Home",   "item": BASE_URL },
            { "@type": "ListItem", "position": 2, "name": "Author", "item": `${BASE_URL}/author` },
            { "@type": "ListItem", "position": 3, "name": "Collins Asein", "item": `${BASE_URL}/author/collins-asein` },
          ],
        },
      },
    ],
  };

  return (
    <div className="au">
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
      />
      <InView />

      {/* the name, and who he is */}
      <header className="au-hero">
        <nav className="au-crumbs" aria-label="Breadcrumb">
          <Link href="/">Home</Link><span>/</span><span>Author</span>
        </nav>
        <img className="au-hero__mark" src="/pk-mark.png" alt="" width={44} height={80} />
        <h1 className="au-hero__name"><span>Collins</span><span>Asein</span></h1>
        <p className="au-hero__role">Christian author, faith technologist, and the founder of <Link href="/">PrayerKey</Link>.</p>
      </header>

      {/* his story */}
      <section className="au-story" data-in="" aria-label="About Collins">
        <p className="au-story__lead">
          Collins built PrayerKey from a personal conviction: prayer is the most powerful force available to a
          believer, and technology should make it more accessible, not less meaningful.
        </p>
        <div className="au-story__body">
          <p>
            As a Christian who has seen prayer change lives, families and communities, he wanted a tool that meets
            people where they are, whether a new believer who does not yet know how to pray, or a pastor preparing a
            sermon at midnight.
          </p>
          <p>
            PrayerKey grew from that vision: a place where every prayer is grounded in Scripture and where technology
            bows to theology. Collins writes on faith, prayer, and how the Christian life meets the modern world.
          </p>
        </div>
      </section>

      {/* what he believes */}
      <section className="au-belief" data-in="" aria-label="His conviction">
        <p className="au-belief__text">
          <span>The Gospel does not need technology to be powerful.</span>{" "}
          <span>But technology, when surrendered to God, can carry the Gospel further than we ever imagined.</span>
        </p>
      </section>

      {/* what he writes about */}
      <section className="au-subjects" data-in="" aria-labelledby="au-subjects-title">
        <h2 id="au-subjects-title" className="au-kicker">He writes about</h2>
        <p className="au-subjects__text">
          {SUBJECTS.map((s, i) => (
            <span key={s} style={{ "--i": i } as React.CSSProperties}>
              {s}{i < SUBJECTS.length - 2 ? ", " : i === SUBJECTS.length - 2 ? ", and " : "."}
            </span>
          ))}
        </p>
      </section>

      {/* prayers he has written */}
      <section className="au-prayers" data-in="" aria-labelledby="au-prayers-title">
        <div className="au-head">
          <h2 id="au-prayers-title" className="au-h2">Prayers by Collins.</h2>
          <p className="au-head__line">Each one rooted in Scripture and written with pastoral care.</p>
        </div>
        <div className="au-prayers__grid">
          {FEATURED_PRAYERS.map((t, i) => (
            <PrayerCard key={t.slug} slug={t.slug} title={t.title} href={`/pray/${t.slug}`} style={{ "--i": i } as React.CSSProperties} />
          ))}
        </div>
        <Link href="/prayer" className="au-more">See every prayer</Link>
      </section>

      {/* guides he has written */}
      <section className="au-guides" data-in="" aria-labelledby="au-guides-title">
        <div className="au-head">
          <h2 id="au-guides-title" className="au-h2">Guides by Collins.</h2>
          <p className="au-head__line">Longer reads on prayer and the Bible, for beginners and for those coming back.</p>
        </div>
        <div className="au-guides__grid">
          {GUIDES.map((g, i) => (
            <GuideCover key={g.slug} href={`/guides/${g.slug}`} art={guideArt(g.slug)} title={g.title} minutes={g.readMinutes} style={{ "--i": i } as React.CSSProperties} />
          ))}
        </div>
        <Link href="/guides" className="au-more">All guides</Link>
      </section>

      {/* a quiet ending */}
      <section className="au-end" data-in="" aria-labelledby="au-end-title">
        <h2 id="au-end-title" className="au-end__title">Pray with PrayerKey.</h2>
        <p className="au-end__line">Free, built on Scripture, and no account needed.</p>
        <nav className="au-end__links" aria-label="More from PrayerKey">
          {MORE.map((m) => <Link key={m.href} href={m.href}>{m.label}</Link>)}
        </nav>
      </section>
    </div>
  );
}
