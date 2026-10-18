import { notFound } from "next/navigation";
import "@/components/prayer/cards.css";
import "@/components/prayer/post.css";
import type { Metadata } from "next";
import PrayerPost from "@/components/prayer/PrayerPost";
import { TOPIC_MAP, ALL_SLUGS } from "@/lib/seo/prayer-topics";

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

      <PrayerPost topic={topic} base="/pray" full={false} />
    </>
  );
}
