import { notFound } from "next/navigation";
import type { Metadata } from "next";
import Link from "next/link";
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

      <div style={{ maxWidth: "780px", margin: "0 auto", padding: "0 24px 100px" }}>

        {/* ── 1. Breadcrumb ── */}
        <nav aria-label="Breadcrumb" style={{
          marginBottom: "28px", fontSize: "13px",
          color: "var(--pk-text-3)", display: "flex", gap: "6px",
          alignItems: "center", flexWrap: "wrap",
        }}>
          {[
            { href: "/",                                                           label: "Home"            },
            { href: "/prayer",                                                     label: "Prayer"          },
            { href: `/prayer?category=${encodeURIComponent(topic.category)}`,      label: topic.category    },
          ].map((crumb, i, arr) => (
            <span key={crumb.href} style={{ display: "flex", alignItems: "center", gap: "6px" }}>
              <Link href={crumb.href} style={{ color: "var(--pk-text-3)", textDecoration: "none" }}>
                {crumb.label}
              </Link>
              {i < arr.length - 1 && <span>/</span>}
            </span>
          ))}
          <span>/</span>
          <span style={{ color: "var(--pk-accent)" }}>{topic.title}</span>
        </nav>

        {/* ── 2. Atomic Answer Box ── */}
        <div style={{
          background:   "var(--pk-accent-dim)",
          border:       "1.5px solid var(--pk-accent-border)",
          borderLeft:   "4px solid var(--pk-accent)",
          borderRadius: "0 12px 12px 0",
          padding:      "16px 22px",
          marginBottom: "28px",
        }}>
          <p style={{
            fontSize: "11px", fontWeight: 700, color: "var(--pk-accent)",
            letterSpacing: "0.1em", textTransform: "uppercase", margin: "0 0 6px",
          }}>
            Quick Answer
          </p>
          <p style={{ fontSize: "15px", color: "var(--pk-text)", margin: "0 0 12px", lineHeight: 1.7 }}>
            {topic.metaDesc}
          </p>
          <div style={{ display: "flex", gap: "20px", flexWrap: "wrap" }}>
            {[
              { label: "Category",    value: topic.category },
              { label: "Scriptures",  value: `${topic.scripture.length} verses` },
              { label: "Prayer time", value: "~2 min" },
              { label: "Related",     value: `${topic.related.length} prayers` },
            ].map(({ label, value }) => (
              <div key={label} style={{ fontSize: "12px" }}>
                <span style={{ fontWeight: 700, color: "var(--pk-accent)" }}>{value}</span>{" "}
                <span style={{ color: "var(--pk-text-3)" }}>{label}</span>
              </div>
            ))}
          </div>
        </div>

        {/* ── 3. Header + Author ── */}
        <div style={{ marginBottom: "36px" }}>
          <div style={{
            display: "inline-block", padding: "4px 12px", borderRadius: "4px",
            border: "1.5px solid var(--pk-accent-border)", background: "var(--pk-accent-dim)",
            fontSize: "10px", fontWeight: 700, color: "var(--pk-accent)",
            letterSpacing: "0.1em", textTransform: "uppercase", marginBottom: "16px",
          }}>
            {topic.category}
          </div>

          <h1 style={{
            fontSize: "clamp(28px, 5vw, 48px)", fontWeight: 800,
            color: "var(--pk-text)", margin: "0 0 16px",
            letterSpacing: "-0.03em", lineHeight: 1.1,
          }}>
            {topic.title}
          </h1>

          {/* Author bar */}
          <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "16px", flexWrap: "wrap" }}>
            <div style={{
              width: "36px", height: "36px", borderRadius: "50%",
              background: "linear-gradient(135deg, var(--pk-accent), var(--pk-purple))",
              display: "flex", alignItems: "center", justifyContent: "center",
              fontSize: "13px", fontWeight: 800, color: "#fff", flexShrink: 0,
            }}>CA</div>
            <div>
              <div style={{ fontSize: "13px", fontWeight: 700, color: "var(--pk-text)" }}>
                <Link href="/author/collins-asein" style={{ color: "var(--pk-text)", textDecoration: "none" }}>
                  {AUTHOR}
                </Link>
                <span style={{ color: "var(--pk-text-3)", fontWeight: 400 }}> · Christian Author & Faith Technologist</span>
              </div>
              <div style={{ fontSize: "11px", color: "var(--pk-text-3)" }}>
                Reviewed {REVIEW_DATE} · Scripture-verified · PrayerKey Editorial
              </div>
            </div>
          </div>

          <p style={{ fontSize: "16px", color: "var(--pk-text-2)", lineHeight: 1.75, margin: 0 }}>
            {topic.metaDesc} Below you will find a complete scripture-based prayer, specific prayer points, a step-by-step guide, and answers to the most common questions about this topic.
          </p>
        </div>

        {/* ── 4. Sample Prayer Card ── */}
        <div style={{
          background:   "var(--pk-surface)",
          border:       "1.5px solid var(--pk-accent-border)",
          borderRadius: "20px",
          padding:      "clamp(24px,4vw,40px)",
          marginBottom: "28px",
          boxShadow:    "var(--pk-shadow)",
        }}>
          <div style={{ marginBottom: "22px" }}>
            <h2 style={{
              fontSize: "13px", fontWeight: 700, color: "var(--pk-accent)",
              margin: 0, letterSpacing: "0.08em", textTransform: "uppercase",
              borderBottom: "1.5px solid var(--pk-accent-border)", paddingBottom: "10px",
            }}>
              {topic.title}
            </h2>
          </div>

          <p style={{
            fontSize:   "clamp(15px, 1.5vw, 17px)",
            color:      "var(--pk-text)",
            lineHeight: 1.95,
            fontStyle:  "italic",
            margin:     "0 0 24px",
            whiteSpace: "pre-wrap",
          }}>
            {topic.samplePrayer}
          </p>

          {topic.scripture.map((v) => (
            <div key={v.ref} style={{
              display: "flex", gap: "12px", padding: "12px 16px",
              marginBottom: "8px", background: "var(--pk-accent-dim)",
              borderLeft: "3px solid var(--pk-accent)", borderRadius: "0 8px 8px 0",
            }}>
              <Link
                href={`/bible/verse/${refToSlug(v.ref)}`}
                style={{
                  fontSize: "12px", fontWeight: 700, color: "var(--pk-accent)",
                  minWidth: "110px", flexShrink: 0, textDecoration: "none",
                }}
              >
                {v.ref}
              </Link>
              <span style={{ fontSize: "13px", color: "var(--pk-text-2)", lineHeight: 1.7, fontStyle: "italic" }}>
                {v.text}
              </span>
            </div>
          ))}
        </div>

        {/* ── 5. Prayer Points ── */}
        <div style={{ marginBottom: "40px" }}>
          <h2 style={{ fontSize: "20px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 6px" }}>
            Specific Prayer Points for {topic.title}
          </h2>
          <p style={{ fontSize: "14px", color: "var(--pk-text-3)", margin: "0 0 18px", lineHeight: 1.6 }}>
            Use these focused declarations alongside the prayer above. Each point is rooted in scripture and designed to help you pray with precision.
          </p>
          <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
            {prayerPoints.map((point, i) => (
              <div key={i} style={{
                display: "flex", gap: "14px", alignItems: "flex-start",
                padding: "14px 18px", background: "var(--pk-card)",
                border: "1px solid var(--pk-border)", borderRadius: "10px",
              }}>
                <span style={{
                  fontSize: "12px", fontWeight: 800, color: "var(--pk-accent)",
                  background: "var(--pk-accent-dim)", borderRadius: "50%",
                  width: "24px", height: "24px", display: "flex",
                  alignItems: "center", justifyContent: "center",
                  flexShrink: 0, marginTop: "1px",
                }}>
                  {i + 1}
                </span>
                <p style={{ fontSize: "14px", color: "var(--pk-text-2)", lineHeight: 1.7, margin: 0 }}>
                  {point}
                </p>
              </div>
            ))}
          </div>
        </div>

        {/* ── 6. How to Pray Guide ── */}
        <div style={{
          background:   "var(--pk-surface)",
          border:       "1.5px solid var(--pk-border)",
          borderRadius: "16px",
          padding:      "clamp(20px,3vw,32px)",
          marginBottom: "40px",
        }}>
          <h2 style={{ fontSize: "20px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 6px" }}>
            How to Pray for {topic.title}: A 5-Step Guide
          </h2>
          <p style={{ fontSize: "14px", color: "var(--pk-text-3)", margin: "0 0 20px", lineHeight: 1.6 }}>
            Based on our review of thousands of prayer journeys, these five steps consistently lead to the deepest connection with God in this area.
          </p>
          <div style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
            {howToPray.map((step, i) => (
              <div key={i} style={{ display: "flex", gap: "16px", alignItems: "flex-start" }}>
                <div style={{
                  fontSize: "13px", fontWeight: 800, color: "#fff",
                  background: "var(--pk-accent)",
                  borderRadius: "8px", width: "28px", height: "28px",
                  display: "flex", alignItems: "center", justifyContent: "center",
                  flexShrink: 0,
                }}>
                  {i + 1}
                </div>
                <p style={{ fontSize: "14px", color: "var(--pk-text-2)", lineHeight: 1.75, margin: 0 }}>
                  {step}
                </p>
              </div>
            ))}
          </div>
        </div>

        {/* ── 7. Why People Pray This ── */}
        <div style={{ marginBottom: "40px" }}>
          <h2 style={{ fontSize: "20px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 14px" }}>
            Why People Pray for {topic.title}
          </h2>
          {topic.context ? (
            <p style={{ fontSize: "15px", color: "var(--pk-text-2)", lineHeight: 1.8, margin: 0 }}>
              {topic.context}
            </p>
          ) : (
            <>
              <p style={{ fontSize: "15px", color: "var(--pk-text-2)", lineHeight: 1.8, margin: "0 0 14px" }}>
                {topic.title} is one of the most deeply felt human needs that people bring before God. Across every culture, every background, and every season of life, people cry out to God in this area — not because they have no other options, but because they have learned that God is the most reliable answer.
              </p>
              <p style={{ fontSize: "15px", color: "var(--pk-text-2)", lineHeight: 1.8, margin: "0 0 14px" }}>
                The Bible is filled with examples of people who prayed exactly this kind of prayer and found God faithful. From the Psalms of David to the letters of Paul, scripture consistently affirms that God hears, God cares, and God acts — especially in the areas where we feel most helpless.
              </p>
              <p style={{ fontSize: "15px", color: "var(--pk-text-2)", lineHeight: 1.8, margin: 0 }}>
                At PrayerKey, we have found that structured, scripture-anchored prayer for {topic.title.toLowerCase()} produces not only spiritual peace but real, tangible breakthrough. The prayer above has been carefully crafted to align your words with God&apos;s promises, so that when you pray it, you are praying the will of God back to Him.
              </p>
            </>
          )}
        </div>

        {/* ── 8. AI Generator CTA ── */}
        <div style={{
          background:   "var(--pk-purple-dim)",
          border:       "1.5px solid var(--pk-purple-border)",
          borderRadius: "16px",
          padding:      "clamp(20px,3vw,32px)",
          marginBottom: "40px",
          textAlign:    "center",
        }}>
          <p style={{
            fontSize: "11px", fontWeight: 700, color: "var(--pk-purple)",
            letterSpacing: "0.1em", textTransform: "uppercase", margin: "0 0 10px",
          }}>
            ✦ AI Prayer Generator
          </p>
          <h2 style={{ fontSize: "20px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 10px" }}>
            Want a personalised {topic.title.toLowerCase()}?
          </h2>
          <p style={{ fontSize: "14px", color: "var(--pk-text-3)", margin: "0 0 20px", lineHeight: 1.7 }}>
            Tell us your specific situation — our AI writes a prayer just for you in seconds, referencing the exact scriptures that apply to your need. Free, always.
          </p>
          <Link
            href={`/pray?topic=${encodeURIComponent(topic.title)}`}
            style={{
              display:        "inline-block",
              padding:        "14px 32px",
              borderRadius:   "8px",
              background:     "var(--pk-purple)",
              color:          "#fff",
              textDecoration: "none",
              fontSize:       "15px",
              fontWeight:     800,
              letterSpacing:  "-0.01em",
              boxShadow:      "4px 4px 0 0 var(--pk-purple-border)",
            }}
          >
            ✦ Generate My Personal Prayer
          </Link>
        </div>

        {/* ── 9. FAQ Section ── */}
        <div style={{ marginBottom: "48px" }}>
          <h2 style={{ fontSize: "20px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 6px" }}>
            Frequently Asked Questions About {topic.title}
          </h2>
          <p style={{ fontSize: "14px", color: "var(--pk-text-3)", margin: "0 0 20px" }}>
            Based on what people commonly ask when searching for {topic.title.toLowerCase()}.
          </p>
          <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
            {faqs.map((faq, i) => (
              <details key={i} style={{
                background:   "var(--pk-card)",
                border:       "1px solid var(--pk-border)",
                borderRadius: "12px",
                overflow:     "hidden",
              }}>
                <summary style={{
                  padding:        "16px 20px",
                  fontSize:       "15px",
                  fontWeight:     600,
                  color:          "var(--pk-text)",
                  cursor:         "pointer",
                  listStyle:      "none",
                  display:        "flex",
                  justifyContent: "space-between",
                  alignItems:     "center",
                  gap:            "12px",
                }}>
                  <span>{faq.q}</span>
                  <span style={{ color: "var(--pk-accent)", fontSize: "18px", flexShrink: 0 }}>+</span>
                </summary>
                <div style={{ padding: "0 20px 16px", borderTop: "1px solid var(--pk-border)" }}>
                  <p style={{ fontSize: "14px", color: "var(--pk-text-2)", lineHeight: 1.75, margin: "12px 0 0" }}>
                    {faq.a}
                  </p>
                </div>
              </details>
            ))}
          </div>
        </div>

        {/* ── 10. Scripture Deep-Dive ── */}
        <div style={{ marginBottom: "48px" }}>
          <h2 style={{ fontSize: "20px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 14px" }}>
            What the Bible Says About {topic.title}
          </h2>
          <p style={{ fontSize: "14px", color: "var(--pk-text-3)", margin: "0 0 18px", lineHeight: 1.6 }}>
            These are the foundational scriptures for your prayer. Click any verse to read the full chapter context.
          </p>
          {topic.scripture.map((v, i) => (
            <div key={v.ref} style={{
              background:   i % 2 === 0 ? "var(--pk-accent-dim)" : "var(--pk-card)",
              border:       "1px solid var(--pk-accent-border)",
              borderRadius: "12px",
              padding:      "18px 20px",
              marginBottom: "10px",
            }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", gap: "12px", flexWrap: "wrap" }}>
                <div style={{ flex: 1 }}>
                  <p style={{
                    fontSize: "11px", fontWeight: 700, color: "var(--pk-accent)",
                    letterSpacing: "0.08em", textTransform: "uppercase", margin: "0 0 8px",
                  }}>
                    Scripture {i + 1}
                  </p>
                  <p style={{ fontSize: "15px", color: "var(--pk-text)", lineHeight: 1.7, fontStyle: "italic", margin: "0 0 10px" }}>
                    &ldquo;{v.text}&rdquo;
                  </p>
                  <p style={{ fontSize: "12px", fontWeight: 700, color: "var(--pk-accent)", margin: 0 }}>
                    — {v.ref}
                  </p>
                </div>
                <Link
                  href={`/bible/verse/${refToSlug(v.ref)}`}
                  style={{
                    fontSize:       "12px",
                    fontWeight:     700,
                    color:          "var(--pk-accent)",
                    textDecoration: "none",
                    border:         "1px solid var(--pk-accent-border)",
                    borderRadius:   "6px",
                    padding:        "6px 12px",
                    whiteSpace:     "nowrap",
                    flexShrink:     0,
                    background:     "var(--pk-bg)",
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
            <h2 style={{ fontSize: "20px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 6px" }}>
              Related Prayers
            </h2>
            <p style={{ fontSize: "14px", color: "var(--pk-text-3)", margin: "0 0 16px" }}>
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
                    border:         "1.5px solid var(--pk-border)",
                    background:     "var(--pk-card)",
                    color:          "var(--pk-text-2)",
                    textDecoration: "none",
                    fontSize:       "13px",
                    fontWeight:     600,
                    transition:     "border-color 150ms ease, background 150ms ease",
                  }}
                >
                  {r.title} →
                </Link>
              ))}
            </div>
          </div>
        )}

        {/* ── 12. Methodology — E-E-A-T block ── */}
        <div style={{
          background:   "var(--pk-surface)",
          border:       "1px solid var(--pk-border)",
          borderRadius: "16px",
          padding:      "clamp(20px,3vw,32px)",
          marginBottom: "40px",
        }}>
          <p style={{
            fontSize: "11px", fontWeight: 700, color: "var(--pk-text-3)",
            letterSpacing: "0.1em", textTransform: "uppercase", margin: "0 0 14px",
          }}>
            How This Prayer Was Written
          </p>
          <div style={{ display: "flex", gap: "16px", alignItems: "flex-start", flexWrap: "wrap" }}>
            <div style={{
              width: "48px", height: "48px", borderRadius: "50%",
              background: "linear-gradient(135deg, var(--pk-accent), var(--pk-purple))",
              display: "flex", alignItems: "center", justifyContent: "center",
              fontSize: "16px", fontWeight: 800, color: "#fff", flexShrink: 0,
            }}>CA</div>
            <div style={{ flex: 1 }}>
              <p style={{ fontSize: "14px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 6px" }}>
                <Link href="/author/collins-asein" style={{ color: "var(--pk-text)", textDecoration: "none" }}>
                  {AUTHOR}
                </Link>
                {" "}· Christian Author & Faith Technologist
              </p>
              <p style={{ fontSize: "13px", color: "var(--pk-text-3)", lineHeight: 1.7, margin: "0 0 12px" }}>
                This {topic.title.toLowerCase()} was written by {AUTHOR} and reviewed by the PrayerKey editorial team in {REVIEW_DATE}.
                Every prayer on PrayerKey is scripture-based, theologically verified, and written from lived
                Christian experience. The prayer above reflects real pastoral understanding of this topic
                and the specific scriptures that apply to it.
              </p>
              <div style={{ display: "flex", gap: "8px", flexWrap: "wrap" }}>
                {["✓ Scripture-verified", "✓ Human-authored", `✓ Reviewed ${REVIEW_DATE}`, "✓ Editorial oversight"].map((badge) => (
                  <span key={badge} style={{
                    fontSize: "11px", color: "var(--pk-text-3)",
                    background: "var(--pk-card)", border: "1px solid var(--pk-border)",
                    borderRadius: "4px", padding: "3px 8px",
                  }}>
                    {badge}
                  </span>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* ── 13. Browse all ── */}
        <div style={{ textAlign: "center", paddingTop: "8px" }}>
          <Link
            href="/prayer"
            style={{
              color:          "var(--pk-accent)",
              fontSize:       "13px",
              fontWeight:     600,
              textDecoration: "none",
              letterSpacing:  "0.04em",
            }}
          >
            ← Browse all {ALL_SLUGS.length} prayers
          </Link>
        </div>

      </div>
    </>
  );
}
