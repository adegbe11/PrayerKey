import type { Metadata } from "next";
import InView from "@/components/home/InView";
import GuideCover from "@/components/guides/GuideCover";
import { guideArt } from "@/lib/seo/guide-art";
import { GUIDES } from "@/lib/seo/guides";
import "@/components/guides/guides.css";

const BASE_URL = "https://www.prayerkey.com";

export const metadata: Metadata = {
  title: "Prayer Guides — How to Pray, Morning Prayers, Healing Verses & More | PrayerKey",
  description: "Free in-depth prayer guides: how to pray for beginners, morning and night prayers, Bible verses about healing, prayers for anxiety, marriage, and financial breakthrough.",
  keywords: "prayer guides, how to pray, morning prayer guide, healing scriptures, prayer for anxiety guide, bible reading plan, prayer tutorials",
  alternates: { canonical: `${BASE_URL}/guides` },
  openGraph: {
    title: "Prayer Guides | PrayerKey",
    description: "Free in-depth guides on prayer and the Bible — written for real life.",
    url: `${BASE_URL}/guides`,
    images: [{ url: "/og-image.png", width: 1200, height: 630 }],
    type: "website",
  },
};

const jsonLd = {
  "@context": "https://schema.org",
  "@type": "CollectionPage",
  name: "PrayerKey Prayer Guides",
  url: `${BASE_URL}/guides`,
  description: "In-depth guides on prayer and the Bible.",
  hasPart: GUIDES.map((g) => ({
    "@type": "Article",
    headline: g.title,
    url: `${BASE_URL}/guides/${g.slug}`,
  })),
  breadcrumb: {
    "@type": "BreadcrumbList",
    itemListElement: [
      { "@type": "ListItem", position: 1, name: "Home",   item: BASE_URL },
      { "@type": "ListItem", position: 2, name: "Guides", item: `${BASE_URL}/guides` },
    ],
  },
};

export default function GuidesHubPage() {
  const [first, ...rest] = GUIDES;
  return (
    <div className="gh">
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }} />
      <InView />

      <section className="gh-hero">
        <p className="gh-kicker">Guides</p>
        <h1 className="gh-title"><span>Learn to pray,</span><span>for real life.</span></h1>
      </section>

      {/* the guide most people start with, then the rest */}
      <section className="gh-feature" data-in="">
        <GuideCover big href={`/guides/${first.slug}`} art={guideArt(first.slug)} title={first.title} minutes={first.readMinutes} />
        <p className="gh-feature__intro">{first.intro}</p>
      </section>

      <section className="gh-grid" data-in="" aria-label="All guides">
        {rest.map((g, i) => (
          <GuideCover key={g.slug} href={`/guides/${g.slug}`} art={guideArt(g.slug)} title={g.title} minutes={g.readMinutes} style={{ "--i": i } as React.CSSProperties} />
        ))}
      </section>
    </div>
  );
}
