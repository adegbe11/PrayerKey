import { notFound } from "next/navigation";
import "@/components/prayer/cards.css";
import "@/components/prayer/post.css";
import type { Metadata } from "next";
import PrayerPost, { AUTHOR, faqsOf } from "@/components/prayer/PrayerPost";
import { TOPIC_MAP, ALL_SLUGS } from "@/lib/seo/prayer-topics";
import { enrich } from "@/lib/seo/prayer-enrichment";

// The search title leads with exactly what people type ("Prayer for Healing"),
// then says what the page gives them, while it still fits in a search result.
function titleOf(topic: ReturnType<typeof enrich>) {
  if (topic.seoTitle) return `${topic.seoTitle} | PrayerKey`;
  return topic.title.length <= 30 ? `${topic.title}: With Bible Verses | PrayerKey` : `${topic.title} | PrayerKey`;
}

const BASE_URL   = "https://www.prayerkey.com";

/* ── Static generation ─────────────────────────────────────────────── */
export function generateStaticParams() {
  return ALL_SLUGS.map((slug) => ({ slug }));
}

export async function generateMetadata(
  { params }: { params: { slug: string } }
): Promise<Metadata> {
  const found = TOPIC_MAP.get(params.slug);
  if (!found) return {};
  const topic = enrich(found);
  return {
    title:       titleOf(topic),
    description: topic.metaDesc,
    keywords:    topic.keywords.join(", "),
    authors:     [{ name: AUTHOR, url: `${BASE_URL}/author/collins-asein` }],
    alternates:  { canonical: `${BASE_URL}/prayer/${topic.slug}` },
    openGraph: {
      title:       titleOf(topic),
      description: topic.metaDesc,
      url:         `${BASE_URL}/prayer/${topic.slug}`,
      images:      [{ url: "/og-image.png", width: 1200, height: 630 }],
      type:        "article",
    },
    twitter: {
      card:        "summary_large_image",
      title:       `${topic.title} | PrayerKey`,
      description: topic.metaDesc,
    },
  };
}

/* ── Page ──────────────────────────────────────────────────────────── */
export default function PrayerSlugPage({ params }: { params: { slug: string } }) {
  const found = TOPIC_MAP.get(params.slug);
  if (!found) notFound();
  const topic = enrich(found);

  const faqs = faqsOf(topic);

  const jsonLd = {
    "@context":     "https://schema.org",
    "@type":        "Article",
    "headline":     topic.title,
    "description":  topic.metaDesc,
    "url":          `${BASE_URL}/prayer/${topic.slug}`,
    "dateModified": new Date().toISOString(),
    "datePublished":"2024-01-01T00:00:00Z",
    "wordCount":    2500,
    "author": {
      "@type":    "Person",
      "name":     AUTHOR,
      "url":      `${BASE_URL}/author/collins-asein`,
      "jobTitle": "Christian Author & Faith Technologist",
      "sameAs":   [`${BASE_URL}/author/collins-asein`],
    },
    "publisher": {
      "@type": "Organization",
      "name":  "PrayerKey",
      "url":   BASE_URL,
      "logo":  { "@type": "ImageObject", "url": `${BASE_URL}/og-image.png` },
    },
    "mainEntityOfPage": { "@type": "WebPage", "@id": `${BASE_URL}/prayer/${topic.slug}` },
    "breadcrumb": {
      "@type": "BreadcrumbList",
      "itemListElement": [
        { "@type": "ListItem", "position": 1, "name": "Home",   "item": BASE_URL },
        { "@type": "ListItem", "position": 2, "name": "Prayer", "item": `${BASE_URL}/prayer` },
        { "@type": "ListItem", "position": 3, "name": topic.title, "item": `${BASE_URL}/prayer/${topic.slug}` },
      ],
    },
    "keywords":   topic.keywords.join(", "),
    "about":      { "@type": "Thing", "name": topic.title },
    "mainEntity": {
      "@type": "FAQPage",
      "mainEntity": faqs.map((faq) => ({
        "@type": "Question",
        "name":  faq.q,
        "acceptedAnswer": { "@type": "Answer", "text": faq.a },
      })),
    },
  };

  return (
    <>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }} />

      <PrayerPost topic={topic} base="/prayer" full />
    </>
  );
}
