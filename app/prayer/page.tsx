import type { Metadata } from "next";
import Link from "next/link";
import InView from "@/components/home/InView";
import PrayerLibrary from "@/components/prayer/PrayerLibrary";
import PrayerSearch from "@/components/prayer/PrayerSearch";
import { CATEGORIES, exists, lite, shelves } from "@/lib/prayer/shelves";
import { PRAYER_TOPICS } from "@/lib/seo/prayer-topics";
import "@/components/prayer/cards.css";
import "./prayer-hub.css";

const BASE_URL = "https://www.prayerkey.com";
const COUNT = PRAYER_TOPICS.length;

export const metadata: Metadata = {
  title: `Prayer for Every Situation | ${COUNT} Prayers — PrayerKey`,
  description:
    `Find a prayer for any situation in life. ${COUNT} prayers built on Scripture across 16 categories: healing, anxiety, marriage, finances, protection, grief and more.`,
  keywords:
    "prayers for every situation, prayer for healing, prayer for anxiety, prayer for strength, prayer for marriage, prayer for finances, prayer for protection, daily prayers, christian prayers",
  authors: [{ name: "Collins Asein", url: `${BASE_URL}/author/collins-asein` }],
  alternates: { canonical: `${BASE_URL}/prayer` },
  openGraph: {
    title: `Prayer for Every Situation | ${COUNT} Prayers — PrayerKey`,
    description: `Find a prayer for any situation in life. ${COUNT} prayers built on Scripture across 16 categories.`,
    url: `${BASE_URL}/prayer`,
    siteName: "PrayerKey",
    images: [{ url: "/og-image.png", width: 1200, height: 630 }],
    type: "website",
  },
  twitter: {
    card: "summary_large_image",
    title: `Prayer for Every Situation | ${COUNT} Prayers — PrayerKey`,
    description: `${COUNT} prayers built on Scripture across 16 categories: healing, anxiety, marriage, finances and more.`,
  },
};

// how someone feels, and the prayer that meets them there
const FEELINGS: [string, string][] = [
  ["Anxious", "anxiety"], ["Grieving", "grief"], ["Afraid", "fear"], ["Lonely", "loneliness"], ["Stressed", "stress"],
  ["Low", "depression"], ["Angry", "anger"], ["Unwell", "healing"], ["Hopeful", "hope"], ["Grateful", "gratitude"],
];

const HOURS = [
  { slug: "morning", kicker: "Morning", title: "Begin the day with God.", img: "/art/morning-prayer.webp" },
  { slug: "night", kicker: "Night", title: "Lay the day down.", img: "/art/evening-prayer.webp" },
  { slug: "short-prayer", kicker: "Any moment", title: "A minute is enough.", img: "/art/window-prayer.webp" },
];


export default function PrayerHubPage() {
  const groups = shelves();

  const jsonLd = {
    "@context": "https://schema.org",
    "@graph": [
      {
        "@type": "CollectionPage",
        "@id": `${BASE_URL}/prayer`,
        url: `${BASE_URL}/prayer`,
        name: "Prayer for Every Situation — PrayerKey",
        description: `${COUNT} prayers built on Scripture across ${CATEGORIES.length} categories for every situation in life.`,
        author: { "@type": "Person", name: "Collins Asein", url: `${BASE_URL}/author/collins-asein` },
        publisher: { "@type": "Organization", name: "PrayerKey", url: BASE_URL, logo: { "@type": "ImageObject", url: `${BASE_URL}/og-image.png` } },
        hasPart: PRAYER_TOPICS.map((t) => ({ "@type": "Article", name: t.title, url: `${BASE_URL}/prayer/${t.slug}` })),
      },
      {
        "@type": "BreadcrumbList",
        itemListElement: [
          { "@type": "ListItem", position: 1, name: "Home", item: BASE_URL },
          { "@type": "ListItem", position: 2, name: "Prayer", item: `${BASE_URL}/prayer` },
        ],
      },
    ],
  };

  return (
    <div className="ph">
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }} />
      <InView />

      {/* one question, one field */}
      <section className="ph-hero">
        <p className="ph-kicker">Prayers</p>
        <h1 className="ph-title"><span>A prayer for</span><span>every moment.</span></h1>
        <PrayerSearch items={PRAYER_TOPICS.map(lite)} />
      </section>

      {/* where are you right now */}
      <section className="ph-feel" data-in="">
        <h2 className="ph-h2">How are you, really?</h2>
        <div className="ph-feel__words">
          {FEELINGS.filter(([, s]) => exists(s)).map(([word, slug], i) => (
            <Link key={slug} href={`/prayer/${slug}`} style={{ "--i": i } as React.CSSProperties}>{word}</Link>
          ))}
        </div>
      </section>

      {/* the hours of the day */}
      <section className="ph-hours" data-in="">
        {HOURS.filter((h) => exists(h.slug)).map((h, i) => (
          <Link key={h.slug} href={`/prayer/${h.slug}`} className="ph-hour" style={{ "--i": i } as React.CSSProperties}>
            <img src={h.img} alt="" loading="lazy" decoding="async" width={1024} height={1536} />
            <span className="ph-hour__text">
              <span className="ph-hour__kicker">{h.kicker}</span>
              <span className="ph-hour__title">{h.title}</span>
            </span>
          </Link>
        ))}
      </section>

      {/* every prayer, one shelf at a time */}
      <section className="ph-shelf" data-in="" aria-labelledby="ph-all">
        <h2 id="ph-all" className="ph-h2">Every prayer.</h2>
        <PrayerLibrary groups={groups} />
      </section>

      {/* for everything not on the shelf */}
      <section className="ph-own pk-bleed" data-nav="dark" data-in="">
        <h2 className="ph-own__title"><span>Not here?</span><span>Write your own.</span></h2>
        <p className="ph-own__line">Tell PrayerKey what you are facing and the free prayer generator builds a prayer for it from Scripture.</p>
        <Link href="/pray" className="ph-own__cta">Open the prayer generator</Link>
      </section>
    </div>
  );
}
