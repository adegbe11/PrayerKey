import { notFound } from "next/navigation";
import "@/components/article/article.css";
import type { Metadata } from "next";
import Link from "next/link";
import PrayerComposer from "@/components/PrayerComposer";
import { PRAYER_TOPICS, TOPIC_MAP, ALL_SLUGS, type PrayerTopic } from "@/lib/seo/prayer-topics";

const BASE_URL   = "https://www.prayerkey.com";
const REVIEW_DATE = "April 2026";
const AUTHOR      = "Collins Asein";

/* ── Helpers ────────────────────────────────────────────────────────── */
function refToSlug(ref: string) {
  return ref.toLowerCase().replace(/[\s:]/g, "-").replace(/[^a-z0-9-]/g, "");
}

function getPrayerPoints(topic: PrayerTopic): string[] {
  if (topic.prayerPoints?.length) return topic.prayerPoints;
  const title = topic.title.toLowerCase();
  const s0    = topic.scripture[0];
  const s1    = topic.scripture[1] ?? topic.scripture[0];
  return [
    `Thank God that He hears every prayer for ${title} and that His Word promises He will answer.`,
    `Declare ${s0.ref} over this situation: "${s0.text.slice(0, 60)}…"`,
    `Ask God to intervene directly in every specific detail surrounding your need for ${title}.`,
    `Pray for perseverance and faith to keep believing even before you see the answer.`,
    `Intercede for others who are also praying for ${title} right now — you are not alone.`,
    `Declare ${s1.ref}: "${s1.text.slice(0, 60)}…" — receive it as a personal promise today.`,
    `Close by giving thanks in advance, trusting that God has already begun to move.`,
  ];
}

function getHowToPray(topic: PrayerTopic): string[] {
  if (topic.howToPray?.length) return topic.howToPray;
  const title = topic.title.toLowerCase();
  return [
    `Find a quiet space — even two minutes of stillness before God is enough to begin your prayer for ${title}.`,
    `Open with gratitude. Acknowledge who God is before bringing your request. He is already working.`,
    `Pray the prayer above aloud or from your heart, personalising it with the specific names and details of your situation.`,
    `Declare the scriptures listed. Speak them out — faith comes by hearing, and hearing by the Word of God (Romans 10:17).`,
    `Close with an act of faith. Release the outcome to God, thank Him in advance, and resist anxiety by choosing trust.`,
  ];
}

function getFaqs(topic: PrayerTopic): { q: string; a: string }[] {
  if (topic.faqs?.length) return topic.faqs;
  const title = topic.title.toLowerCase();
  const s0    = topic.scripture[0];
  return [
    {
      q: `What is the best prayer for ${title}?`,
      a: `${topic.metaDesc} The prayer above — written and reviewed by ${AUTHOR} — is scripture-based, specific, and designed to be prayed aloud. Personalise it with your exact situation for maximum impact.`,
    },
    {
      q: `Which Bible verse should I use when praying for ${title}?`,
      a: `${s0.ref} is one of the most powerful scriptures for this: "${s0.text}" Praying scripture back to God anchors your prayer in His promises rather than your feelings.`,
    },
    {
      q: `How do I pray for ${title} effectively?`,
      a: `Begin in stillness, acknowledge God's power and faithfulness, then pray specifically. Use the prayer above as your guide. Declare the Bible verses listed, personalise them for your situation, and close by releasing the outcome to God with thanksgiving.`,
    },
    {
      q: `How often should I pray for ${title}?`,
      a: `Scripture says to pray without ceasing (1 Thessalonians 5:17). You can pray this prayer daily, or every time the need arises. Many people set a consistent time — morning or night — to pray until they see the breakthrough.`,
    },
    {
      q: `Can God really answer a prayer for ${title}?`,
      a: `Yes. John 16:24 says: "Ask and you will receive." No prayer prayed in faith, according to God's will, goes unanswered. He may answer differently than expected, but He always responds. Your prayer for ${title} is not too small or too big for Him.`,
    },
  ];
}

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
    title:       `${topic.title} — Scripture-Based Prayer & Guide | PrayerKey`,
    description: topic.metaDesc,
    keywords:    topic.keywords.join(", "),
    authors:     [{ name: AUTHOR, url: `${BASE_URL}/author/collins-asein` }],
    alternates:  { canonical: `${BASE_URL}/prayer/${topic.slug}` },
    openGraph: {
      title:       `${topic.title} | PrayerKey`,
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
  const topic = TOPIC_MAP.get(params.slug);
  if (!topic) notFound();

  const related      = topic.related.map((s) => TOPIC_MAP.get(s)).filter(Boolean) as typeof PRAYER_TOPICS;
  const prayerPoints = getPrayerPoints(topic);
  const howToPray    = getHowToPray(topic);
  const faqs         = getFaqs(topic);

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

      {/* One article column: the title first, then the prayer, then what helps you pray it. */}
      <article className="ar">
        <nav aria-label="Breadcrumb" className="ar-crumbs">
          <Link href="/">Home</Link><span>/</span>
          <Link href="/prayer">Prayer</Link><span>/</span>
          <Link href={`/prayer?category=${encodeURIComponent(topic.category)}`}>{topic.category}</Link>
        </nav>

        <p className="ar-eyebrow">{topic.category}</p>
        <h1 className="ar-h1">{topic.title}</h1>
        <p className="ar-lede">{topic.metaDesc}</p>

        <div className="ar-byline">
          <span className="ar-byline__face" aria-hidden>CA</span>
          <div>
            <Link href="/author/collins-asein">{AUTHOR}</Link>
            <br /><span>Reviewed {REVIEW_DATE} · 2 min prayer</span>
          </div>
        </div>

        <section aria-label={topic.title} className="ar-prayer">
          <p>{topic.samplePrayer}</p>
          <ul className="ar-verses">
            {topic.scripture.map((v) => (
              <li key={v.ref}>
                <q>{v.text}</q>
                <Link href={`/bible/verse/${refToSlug(v.ref)}`}>{v.ref}</Link>
              </li>
            ))}
          </ul>
        </section>

        <section className="ar-section" aria-labelledby="points">
          <h2 id="points" className="ar-h2">Prayer points</h2>
          <ol className="ar-steps">
            {prayerPoints.map((point, i) => <li key={i}>{point}</li>)}
          </ol>
        </section>

        <section className="ar-section" aria-labelledby="how">
          <h2 id="how" className="ar-h2">How to pray it</h2>
          <ol className="ar-steps">
            {howToPray.map((step, i) => <li key={i}>{step}</li>)}
          </ol>
        </section>

        <section className="ar-section" aria-labelledby="why">
          <h2 id="why" className="ar-h2">Why people pray this</h2>
          {topic.context ? (
            <p className="ar-p">{topic.context}</p>
          ) : (
            <>
              <p className="ar-p">
                {topic.title} is one of the most deeply felt needs people bring before God. Across every culture and every season of life, people cry out to Him here, not because they have no other options, but because they have learned that He hears.
              </p>
              <p className="ar-p">
                The Bible is full of people who prayed this kind of prayer and found God faithful. From the Psalms of David to the letters of Paul, Scripture says the same thing again and again: God hears, God cares, and God acts.
              </p>
            </>
          )}
        </section>

        <section className="ar-own" aria-labelledby="own">
          <h2 id="own" className="ar-h2">Pray about your own situation</h2>
          <p>Write what you are facing, in your own words, and pray it with Scripture.</p>
          <PrayerComposer seedTopic={topic.title} compact />
        </section>

        <section className="ar-section ar-faq" aria-labelledby="faq">
          <h2 id="faq" className="ar-h2">Questions</h2>
          {faqs.map((faq, i) => (
            <details key={i}>
              <summary>{faq.q}</summary>
              <p>{faq.a}</p>
            </details>
          ))}
        </section>

        {related.length > 0 && (
          <section className="ar-section" aria-labelledby="related">
            <h2 id="related" className="ar-h2">Related prayers</h2>
            <ul className="ar-links">
              {related.map((r) => (
                <li key={r.slug}><Link href={`/prayer/${r.slug}`}>{r.title}</Link></li>
              ))}
            </ul>
          </section>
        )}

        <p className="ar-note">
          Written by <Link href="/author/collins-asein">{AUTHOR}</Link> and reviewed by the PrayerKey editorial team in {REVIEW_DATE}. Every prayer on PrayerKey is built on Scripture.
          <br />
          <Link href="/prayer" className="ar-more">Browse all {ALL_SLUGS.length} prayers →</Link>
        </p>
      </article>
    </>
  );
}
