import { notFound } from "next/navigation";
import "@/components/article/article.css";
import type { Metadata } from "next";
import Link from "next/link";
import PrayerComposer from "@/components/PrayerComposer";
import { PRAYER_TOPICS, TOPIC_MAP, ALL_SLUGS } from "@/lib/seo/prayer-topics";

/* ── Static generation ─────────────────────────────────────────────── */
export function generateStaticParams() {
  return ALL_SLUGS.map((slug) => ({ slug }));
}

export async function generateMetadata(
  { params }: { params: { slug: string } }
): Promise<Metadata> {
  const topic = TOPIC_MAP.get(params.slug);
  if (!topic) return {};
  return {
    title:       `${topic.title} | PrayerKey`,
    description: topic.metaDesc,
    keywords:    topic.keywords.join(", "),
    openGraph: {
      title:       `${topic.title} | PrayerKey`,
      description: topic.metaDesc,
      url:         `https://www.prayerkey.com/pray/${topic.slug}`,
      images:      [{ url: "/og-image.png", width: 1200, height: 630 }],
    },
    alternates: {
      canonical: `https://www.prayerkey.com/pray/${topic.slug}`,
    },
  };
}

/* ── Page ──────────────────────────────────────────────────────────── */
export default function PrayerTopicPage({ params }: { params: { slug: string } }) {
  const topic = TOPIC_MAP.get(params.slug);
  if (!topic) notFound();

  const related = topic.related
    .map((s) => TOPIC_MAP.get(s))
    .filter(Boolean) as typeof PRAYER_TOPICS;

  // JSON-LD structured data
  const jsonLd = {
    "@context":    "https://schema.org",
    "@type":       "Article",
    "headline":    topic.title,
    "description": topic.metaDesc,
    "url":         `https://www.prayerkey.com/pray/${topic.slug}`,
    "dateModified": new Date().toISOString(),
    "author": {
      "@type": "Person",
      "name":  "Collins Asein",
      "url":   "https://www.prayerkey.com/author/collins-asein",
      "jobTitle": "Christian Author & Faith Technologist",
    },
    "publisher": {
      "@type": "Organization",
      "name":  "PrayerKey",
      "url":   "https://www.prayerkey.com",
      "logo":  { "@type": "ImageObject", "url": "https://www.prayerkey.com/og-image.png" },
    },
    "mainEntityOfPage": {
      "@type": "WebPage",
      "@id":   `https://www.prayerkey.com/pray/${topic.slug}`,
    },
    "breadcrumb": {
      "@type":           "BreadcrumbList",
      "itemListElement": [
        { "@type": "ListItem", "position": 1, "name": "Home",   "item": "https://www.prayerkey.com" },
        { "@type": "ListItem", "position": 2, "name": "Pray",   "item": "https://www.prayerkey.com/pray" },
        { "@type": "ListItem", "position": 3, "name": topic.title, "item": `https://www.prayerkey.com/pray/${topic.slug}` },
      ],
    },
    "keywords": topic.keywords.join(", "),
    "about": { "@type": "Thing", "name": topic.title },
  };

  return (
    <>
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
      />

      <article className="ar">
        <nav aria-label="Breadcrumb" className="ar-crumbs">
          <Link href="/">Home</Link><span>/</span>
          <Link href="/pray">Pray</Link>
        </nav>

        <p className="ar-eyebrow">{topic.category}</p>
        <h1 className="ar-h1">{topic.title}</h1>
        <p className="ar-lede">{topic.metaDesc}</p>

        <section aria-label={topic.title} className="ar-prayer">
          <p>{topic.samplePrayer}</p>
          <ul className="ar-verses">
            {topic.scripture.map((v) => (
              <li key={v.ref}><q>{v.text}</q><span className="ar-ref">{v.ref}</span></li>
            ))}
          </ul>
        </section>

        <section className="ar-own" aria-labelledby="own">
          <h2 id="own" className="ar-h2">Pray about your own situation</h2>
          <p>Write what you are facing, in your own words, and pray it with Scripture.</p>
          <PrayerComposer seedTopic={topic.title} compact />
        </section>

        {related.length > 0 && (
          <section className="ar-section" aria-labelledby="related">
            <h2 id="related" className="ar-h2">Related prayers</h2>
            <ul className="ar-links">
              {related.map((r) => (
                <li key={r.slug}><Link href={`/pray/${r.slug}`}>{r.title}</Link></li>
              ))}
            </ul>
          </section>
        )}

        <Link href="/pray/topics" className="ar-more">Browse all {ALL_SLUGS.length} prayer topics →</Link>
      </article>
    </>
  );
}
