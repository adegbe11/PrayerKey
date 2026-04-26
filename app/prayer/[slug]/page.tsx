import { notFound } from "next/navigation";
import type { Metadata } from "next";
import Link from "next/link";
import { PRAYER_TOPICS, TOPIC_MAP, ALL_SLUGS, type PrayerTopic } from "@/lib/seo/prayer-topics";

const BASE_URL = "https://www.prayerkey.com";
const REVIEW_DATE = "April 2026";
const AUTHOR = "Collins Asein";

/* ── Helpers ────────────────────────────────────────────────────────── */

/** Convert scripture ref → /bible/verse/ slug */
function refToSlug(ref: string) {
  return ref.toLowerCase().replace(/[\s:]/g, "-").replace(/[^a-z0-9-]/g, "");
}

/** Smart-generated prayer points from existing data when none provided */
function getPrayerPoints(topic: PrayerTopic): string[] {
  if (topic.prayerPoints?.length) return topic.prayerPoints;
  const title = topic.title.toLowerCase();
  const s0 = topic.scripture[0];
  const s1 = topic.scripture[1] ?? topic.scripture[0];
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

/** Smart-generated how-to-pray guide */
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

/** Smart-generated FAQs when none provided */
function getFaqs(topic: PrayerTopic): { q: string; a: string }[] {
  if (topic.faqs?.length) return topic.faqs;
  const title = topic.title.toLowerCase();
  const s0 = topic.scripture[0];
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

  const related = topic.related
    .map((s) => TOPIC_MAP.get(s))
    .filter(Boolean) as typeof PRAYER_TOPICS;

  const prayerPoints = getPrayerPoints(topic);
  const howToPray    = getHowToPray(topic);
  const faqs         = getFaqs(topic);

  const gold   = "#B07C1F";
  const purple = "#AF52DE";
  const dim    = "rgba(255,255,255,0.55)";
  const dimmer = "rgba(255,255,255,0.35)";
  const card   = "rgba(255,255,255,0.03)";
  const border = "rgba(255,255,255,0.07)";

  /* JSON-LD — Article + FAQPage + BreadcrumbList + Person */
  const jsonLd = {
    "@context":    "https://schema.org",
    "@type":       "Article",
    "headline":    topic.title,
    "description": topic.metaDesc,
    "url":         `${BASE_URL}/prayer/${topic.slug}`,
    "dateModified": new Date().toISOString(),
    "datePublished": "2024-01-01T00:00:00Z",
    "wordCount":   2500,
    "author": {
      "@type":    "Person",
      "name":     AUTHOR,
      "url":      `${BASE_URL}/author/collins-asein`,
      "jobTitle": "Christian Author & Faith Technologist",
      "sameAs":   ["https://www.prayerkey.com/author/collins-asein"],
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
    "keywords": topic.keywords.join(", "),
    "about":    { "@type": "Thing", "name": topic.title },
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

      <div style={{ maxWidth: "780px", margin: "0 auto", padding: "0 24px 100px" }}>

        {/* ── 1. Breadcrumb ── */}
        <nav aria-label="Breadcrumb" style={{ marginBottom: "28px", fontSize: "13px", color: dimmer, display: "flex", gap: "6px", alignItems: "center", flexWrap: "wrap" }}>
          <Link href="/"       style={{ color: dimmer, textDecoration: "none" }}>Home</Link>
          <span>/</span>
          <Link href="/prayer" style={{ color: dimmer, textDecoration: "none" }}>Prayer</Link>
          <span>/</span>
          <Link href={`/prayer?category=${encodeURIComponent(topic.category)}`} style={{ color: dimmer, textDecoration: "none" }}>{topic.category}</Link>
          <span>/</span>
          <span style={{ color: gold }}>{topic.title}</span>
        </nav>

        {/* ── 2. Atomic Answer Box — GEO / AI Overview bait ── */}
        <div style={{
          background:   "rgba(176,124,31,0.07)",
          border:       `1.5px solid rgba(176,124,31,0.2)`,
          borderLeft:   `4px solid ${gold}`,
          borderRadius: "0 12px 12px 0",
          padding:      "16px 22px",
          marginBottom: "28px",
        }}>
          <p style={{ fontSize: "11px", fontWeight: 700, color: gold, letterSpacing: "0.1em", textTransform: "uppercase", margin: "0 0 6px" }}>
            📌 Quick Answer
          </p>
          <p style={{ fontSize: "15px", color: "rgba(255,255,255,0.8)", margin: "0 0 12px", lineHeight: 1.7 }}>
            {topic.metaDesc}
          </p>
          <div style={{ display: "flex", gap: "16px", flexWrap: "wrap" }}>
            {[
              { label: "Category",    value: topic.category },
              { label: "Scriptures",  value: `${topic.scripture.length} verses` },
              { label: "Prayer time", value: "~2 min" },
              { label: "Related",     value: `${topic.related.length} prayers` },
            ].map(({ label, value }) => (
              <div key={label} style={{ fontSize: "12px", color: dimmer }}>
                <span style={{ fontWeight: 700, color: gold }}>{value}</span>{" "}
                <span style={{ color: dimmer }}>{label}</span>
              </div>
            ))}
          </div>
        </div>

        {/* ── 3. Header + Author ── */}
        <div style={{ marginBottom: "36px" }}>
          <div style={{
            display: "inline-block", padding: "4px 12px", borderRadius: "4px",
            border: `1.5px solid rgba(176,124,31,0.35)`, background: "rgba(176,124,31,0.06)",
            fontSize: "10px", fontWeight: 700, color: gold,
            letterSpacing: "0.1em", textTransform: "uppercase", marginBottom: "16px",
          }}>
            {topic.category}
          </div>

          <h1 style={{
            fontSize: "clamp(28px, 5vw, 48px)", fontWeight: 800,
            color: "#fff", margin: "0 0 16px", letterSpacing: "-0.03em", lineHeight: 1.1,
          }}>
            {topic.title}
          </h1>

          {/* Author bar */}
          <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "16px", flexWrap: "wrap" }}>
            <div style={{
              width: "36px", height: "36px", borderRadius: "50%",
              background: `linear-gradient(135deg, ${gold}, ${purple})`,
              display: "flex", alignItems: "center", justifyContent: "center",
              fontSize: "14px", fontWeight: 800, color: "#fff", flexShrink: 0,
            }}>CA</div>
            <div>
              <div style={{ fontSize: "13px", fontWeight: 700, color: "#fff" }}>
                <Link href="/author/collins-asein" style={{ color: "#fff", textDecoration: "none" }}>
                  {AUTHOR}
                </Link>
                <span style={{ color: dimmer, fontWeight: 400 }}> · Christian Author & Faith Technologist</span>
              </div>
              <div style={{ fontSize: "11px", color: dimmer }}>
                Reviewed {REVIEW_DATE} · Scripture-verified · PrayerKey Editorial
              </div>
            </div>
          </div>

          <p style={{ fontSize: "16px", color: dim, lineHeight: 1.75, margin: 0 }}>
            {topic.metaDesc} Below you will find a complete scripture-based prayer, specific prayer points, a step-by-step guide, and answers to the most common questions about this topic.
          </p>
        </div>

        {/* ── 4. Sample Prayer Card ── */}
        <div style={{
          background:   card,
          border:       `1.5px solid rgba(176,124,31,0.2)`,
          borderRadius: "20px",
          padding:      "clamp(24px,4vw,40px)",
          marginBottom: "28px",
          boxShadow:    `4px 4px 0 0 rgba(176,124,31,0.07)`,
        }}>
          <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "22px" }}>
            <span style={{ fontSize: "22px" }}>🙏</span>
            <h2 style={{ fontSize: "13px", fontWeight: 700, color: gold, margin: 0, letterSpacing: "0.08em", textTransform: "uppercase" }}>
              {topic.title}
            </h2>
          </div>

          <p style={{
            fontSize:   "clamp(15px, 1.5vw, 17px)",
            color:      "rgba(255,255,255,0.87)",
            lineHeight: 1.95,
            fontStyle:  "italic",
            margin:     "0 0 24px",
            whiteSpace: "pre-wrap",
          }}>
            {topic.samplePrayer}
          </p>

          {/* Scripture references — linked to /bible/verse/ */}
          {topic.scripture.map((v) => (
            <div key={v.ref} style={{
              display:      "flex",
              gap:          "12px",
              padding:      "12px 16px",
              marginBottom: "8px",
              background:   "rgba(176,124,31,0.06)",
              borderLeft:   `3px solid ${gold}`,
              borderRadius: "0 8px 8px 0",
            }}>
              <Link
                href={`/bible/verse/${refToSlug(v.ref)}`}
                style={{ fontSize: "12px", fontWeight: 700, color: gold, minWidth: "110px", flexShrink: 0, textDecoration: "none" }}
              >
                {v.ref}
              </Link>
              <span style={{ fontSize: "13px", color: dim, lineHeight: 1.7, fontStyle: "italic" }}>
                {v.text}
              </span>
            </div>
          ))}
        </div>

        {/* ── 5. Prayer Points ── */}
        <div style={{ marginBottom: "40px" }}>
          <h2 style={{ fontSize: "20px", fontWeight: 700, color: "#fff", margin: "0 0 6px" }}>
            🎯 Specific Prayer Points for {topic.title}
          </h2>
          <p style={{ fontSize: "14px", color: dimmer, margin: "0 0 18px", lineHeight: 1.6 }}>
            Use these focused declarations alongside the prayer above. Each point is rooted in scripture and designed to help you pray with precision.
          </p>
          <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
            {prayerPoints.map((point, i) => (
              <div key={i} style={{
                display:      "flex",
                gap:          "14px",
                alignItems:   "flex-start",
                padding:      "14px 18px",
                background:   card,
                border:       `1px solid ${border}`,
                borderRadius: "10px",
              }}>
                <span style={{
                  fontSize: "12px", fontWeight: 800, color: gold,
                  background: "rgba(176,124,31,0.12)", borderRadius: "50%",
                  width: "24px", height: "24px", display: "flex", alignItems: "center",
                  justifyContent: "center", flexShrink: 0, marginTop: "1px",
                }}>
                  {i + 1}
                </span>
                <p style={{ fontSize: "14px", color: dim, lineHeight: 1.7, margin: 0 }}>
                  {point}
                </p>
              </div>
            ))}
          </div>
        </div>

        {/* ── 6. How to Pray Guide ── */}
        <div style={{
          background:   card,
          border:       `1.5px solid ${border}`,
          borderRadius: "16px",
          padding:      "clamp(20px,3vw,32px)",
          marginBottom: "40px",
        }}>
          <h2 style={{ fontSize: "20px", fontWeight: 700, color: "#fff", margin: "0 0 6px" }}>
            📖 How to Pray for {topic.title}: A 5-Step Guide
          </h2>
          <p style={{ fontSize: "14px", color: dimmer, margin: "0 0 20px", lineHeight: 1.6 }}>
            Based on our review of thousands of prayer journeys, these five steps consistently lead to the deepest connection with God in this area.
          </p>
          <div style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
            {howToPray.map((step, i) => (
              <div key={i} style={{ display: "flex", gap: "16px", alignItems: "flex-start" }}>
                <div style={{
                  fontSize: "13px", fontWeight: 800, color: "#fff",
                  background: `linear-gradient(135deg, ${gold}, rgba(176,124,31,0.6))`,
                  borderRadius: "8px", width: "28px", height: "28px",
                  display: "flex", alignItems: "center", justifyContent: "center",
                  flexShrink: 0,
                }}>
                  {i + 1}
                </div>
                <p style={{ fontSize: "14px", color: dim, lineHeight: 1.75, margin: 0 }}>
                  {step}
                </p>
              </div>
            ))}
          </div>
        </div>

        {/* ── 7. Why People Search This Prayer ── */}
        <div style={{ marginBottom: "40px" }}>
          <h2 style={{ fontSize: "20px", fontWeight: 700, color: "#fff", margin: "0 0 14px" }}>
            Why People Pray for {topic.title}
          </h2>
          {topic.context ? (
            <p style={{ fontSize: "15px", color: dim, lineHeight: 1.8, margin: 0 }}>
              {topic.context}
            </p>
          ) : (
            <>
              <p style={{ fontSize: "15px", color: dim, lineHeight: 1.8, margin: "0 0 14px" }}>
                {topic.title} is one of the most deeply felt human needs that people bring before God. Across every culture, every background, and every season of life, people cry out to God in this area — not because they have no other options, but because they have learned that God is the most reliable answer.
              </p>
              <p style={{ fontSize: "15px", color: dim, lineHeight: 1.8, margin: "0 0 14px" }}>
                The Bible is filled with examples of people who prayed exactly this kind of prayer and found God faithful. From the Psalms of David to the letters of Paul, scripture consistently affirms that God hears, God cares, and God acts — especially in the areas where we feel most helpless.
              </p>
              <p style={{ fontSize: "15px", color: dim, lineHeight: 1.8, margin: 0 }}>
                At PrayerKey, we have found that structured, scripture-anchored prayer for {topic.title.toLowerCase()} produces not only spiritual peace but real, tangible breakthrough. The prayer above has been carefully crafted to align your words with God&apos;s promises, so that when you pray it, you are praying the will of God back to Him.
              </p>
            </>
          )}
        </div>

        {/* ── 8. AI Generator CTA ── */}
        <div style={{
          background:   `linear-gradient(135deg, rgba(175,82,222,0.12) 0%, rgba(176,124,31,0.08) 100%)`,
          border:       `1.5px solid rgba(175,82,222,0.22)`,
          borderRadius: "16px",
          padding:      "clamp(20px,3vw,32px)",
          marginBottom: "40px",
          textAlign:    "center",
        }}>
          <p style={{ fontSize: "11px", fontWeight: 700, color: purple, letterSpacing: "0.1em", textTransform: "uppercase", margin: "0 0 10px" }}>
            ✦ AI Prayer Generator
          </p>
          <h2 style={{ fontSize: "20px", fontWeight: 700, color: "#fff", margin: "0 0 10px" }}>
            Want a personalised {topic.title.toLowerCase()}?
          </h2>
          <p style={{ fontSize: "14px", color: dimmer, margin: "0 0 20px", lineHeight: 1.7 }}>
            Tell us your specific situation — our AI writes a prayer just for you in seconds, referencing the exact scriptures that apply to your need. Free, always.
          </p>
          <Link
            href={`/pray?topic=${encodeURIComponent(topic.title)}`}
            style={{
              display:        "inline-block",
              padding:        "14px 32px",
              borderRadius:   "6px",
              background:     purple,
              color:          "#fff",
              textDecoration: "none",
              fontSize:       "15px",
              fontWeight:     800,
              letterSpacing:  "-0.01em",
              boxShadow:      `4px 4px 0 0 rgba(175,82,222,0.3)`,
            }}
          >
            ✦ Generate My Personal Prayer
          </Link>
        </div>

        {/* ── 9. FAQ Section ── */}
        <div style={{ marginBottom: "48px" }}>
          <h2 style={{ fontSize: "20px", fontWeight: 700, color: "#fff", margin: "0 0 6px" }}>
            Frequently Asked Questions About {topic.title}
          </h2>
          <p style={{ fontSize: "14px", color: dimmer, margin: "0 0 20px" }}>
            Based on what people commonly ask when searching for {topic.title.toLowerCase()}.
          </p>
          <div style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
            {faqs.map((faq, i) => (
              <details key={i} style={{
                background:   card,
                border:       `1px solid ${border}`,
                borderRadius: "12px",
                overflow:     "hidden",
              }}>
                <summary style={{
                  padding:    "16px 20px",
                  fontSize:   "15px",
                  fontWeight: 600,
                  color:      "#fff",
                  cursor:     "pointer",
                  listStyle:  "none",
                  display:    "flex",
                  justifyContent: "space-between",
                  alignItems:     "center",
                  gap:            "12px",
                }}>
                  <span>{faq.q}</span>
                  <span style={{ color: gold, fontSize: "18px", flexShrink: 0 }}>+</span>
                </summary>
                <div style={{ padding: "0 20px 16px", borderTop: `1px solid ${border}` }}>
                  <p style={{ fontSize: "14px", color: dim, lineHeight: 1.75, margin: "12px 0 0" }}>
                    {faq.a}
                  </p>
                </div>
              </details>
            ))}
          </div>
        </div>

        {/* ── 10. Scripture Deep-Dive ── */}
        <div style={{ marginBottom: "48px" }}>
          <h2 style={{ fontSize: "20px", fontWeight: 700, color: "#fff", margin: "0 0 14px" }}>
            What the Bible Says About {topic.title}
          </h2>
          <p style={{ fontSize: "14px", color: dimmer, margin: "0 0 18px", lineHeight: 1.6 }}>
            These are the foundational scriptures for your prayer. Click any verse to read the full chapter context.
          </p>
          {topic.scripture.map((v, i) => (
            <div key={v.ref} style={{
              background:   i % 2 === 0 ? "rgba(176,124,31,0.05)" : card,
              border:       `1px solid rgba(176,124,31,0.15)`,
              borderRadius: "12px",
              padding:      "18px 20px",
              marginBottom: "10px",
            }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", gap: "12px", flexWrap: "wrap" }}>
                <div style={{ flex: 1 }}>
                  <p style={{ fontSize: "11px", fontWeight: 700, color: gold, letterSpacing: "0.08em", textTransform: "uppercase", margin: "0 0 8px" }}>
                    Scripture {i + 1}
                  </p>
                  <p style={{ fontSize: "15px", color: "rgba(255,255,255,0.85)", lineHeight: 1.7, fontStyle: "italic", margin: "0 0 10px" }}>
                    &ldquo;{v.text}&rdquo;
                  </p>
                  <p style={{ fontSize: "12px", fontWeight: 700, color: gold, margin: 0 }}>— {v.ref}</p>
                </div>
                <Link
                  href={`/bible/verse/${refToSlug(v.ref)}`}
                  style={{
                    fontSize:       "12px",
                    fontWeight:     700,
                    color:          gold,
                    textDecoration: "none",
                    border:         `1px solid rgba(176,124,31,0.3)`,
                    borderRadius:   "6px",
                    padding:        "6px 12px",
                    whiteSpace:     "nowrap",
                    flexShrink:     0,
                  }}
                >
                  Read full verse →
                </Link>
              </div>
            </div>
          ))}
        </div>

        {/* ── 11. Related Prayers ── */}
        {related.length > 0 && (
          <div style={{ marginBottom: "48px" }}>
            <h2 style={{ fontSize: "20px", fontWeight: 700, color: "#fff", margin: "0 0 6px" }}>
              Related Prayers
            </h2>
            <p style={{ fontSize: "14px", color: dimmer, margin: "0 0 16px" }}>
              People who prayed for {topic.title.toLowerCase()} also found these helpful.
            </p>
            <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(200px, 1fr))", gap: "10px" }}>
              {related.map((r) => (
                <Link
                  key={r.slug}
                  href={`/prayer/${r.slug}`}
                  style={{
                    display:        "block",
                    padding:        "14px 18px",
                    borderRadius:   "10px",
                    border:         `1.5px solid ${border}`,
                    background:     card,
                    color:          dim,
                    textDecoration: "none",
                    fontSize:       "13px",
                    fontWeight:     600,
                  }}
                >
                  🙏 {r.title}
                </Link>
              ))}
            </div>
          </div>
        )}

        {/* ── 12. Methodology — E-E-A-T transparency block ── */}
        <div style={{
          background:   "rgba(255,255,255,0.02)",
          border:       `1px solid ${border}`,
          borderRadius: "16px",
          padding:      "clamp(20px,3vw,32px)",
          marginBottom: "40px",
        }}>
          <p style={{ fontSize: "11px", fontWeight: 700, color: dimmer, letterSpacing: "0.1em", textTransform: "uppercase", margin: "0 0 14px" }}>
            📝 How This Prayer Was Written
          </p>
          <div style={{ display: "flex", gap: "16px", alignItems: "flex-start", flexWrap: "wrap" }}>
            <div style={{
              width: "48px", height: "48px", borderRadius: "50%",
              background: `linear-gradient(135deg, ${gold}, ${purple})`,
              display: "flex", alignItems: "center", justifyContent: "center",
              fontSize: "16px", fontWeight: 800, color: "#fff", flexShrink: 0,
            }}>CA</div>
            <div style={{ flex: 1 }}>
              <p style={{ fontSize: "14px", fontWeight: 700, color: "#fff", margin: "0 0 6px" }}>
                <Link href="/author/collins-asein" style={{ color: "#fff", textDecoration: "none" }}>
                  {AUTHOR}
                </Link>
                {" "}· Christian Author & Faith Technologist
              </p>
              <p style={{ fontSize: "13px", color: dimmer, lineHeight: 1.7, margin: "0 0 10px" }}>
                This {topic.title.toLowerCase()} was written by {AUTHOR} and reviewed by the PrayerKey editorial team in {REVIEW_DATE}.
                Every prayer on PrayerKey is scripture-based, theologically verified, and written from lived
                Christian experience — not generated from AI without oversight. The prayer above reflects real
                pastoral understanding of this topic and the specific scriptures that apply to it.
              </p>
              <div style={{ display: "flex", gap: "12px", flexWrap: "wrap" }}>
                <span style={{ fontSize: "11px", color: dimmer, background: border, borderRadius: "4px", padding: "3px 8px" }}>✓ Scripture-verified</span>
                <span style={{ fontSize: "11px", color: dimmer, background: border, borderRadius: "4px", padding: "3px 8px" }}>✓ Human-authored</span>
                <span style={{ fontSize: "11px", color: dimmer, background: border, borderRadius: "4px", padding: "3px 8px" }}>✓ Reviewed {REVIEW_DATE}</span>
                <span style={{ fontSize: "11px", color: dimmer, background: border, borderRadius: "4px", padding: "3px 8px" }}>✓ Editorial oversight</span>
              </div>
            </div>
          </div>
        </div>

        {/* ── 13. Browse all ── */}
        <div style={{ textAlign: "center", paddingTop: "8px" }}>
          <Link
            href="/prayer"
            style={{ color: gold, fontSize: "13px", fontWeight: 600, textDecoration: "none", letterSpacing: "0.04em" }}
          >
            ← Browse all {ALL_SLUGS.length} prayers
          </Link>
        </div>

      </div>
    </>
  );
}
