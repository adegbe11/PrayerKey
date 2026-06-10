import { notFound } from "next/navigation";
import type { Metadata } from "next";
import Link from "next/link";
import PrayerComposer from "@/components/PrayerComposer";
import { GUIDES, GUIDE_MAP, GUIDE_SLUGS } from "@/lib/seo/guides";

const BASE_URL = "https://www.prayerkey.com";
const AUTHOR   = "Collins Asein";

/* ── Static generation ─────────────────────────────────────────────── */
export function generateStaticParams() {
  return GUIDE_SLUGS.map((slug) => ({ slug }));
}

export async function generateMetadata(
  { params }: { params: { slug: string } }
): Promise<Metadata> {
  const guide = GUIDE_MAP.get(params.slug);
  if (!guide) return {};
  return {
    title:       `${guide.title} | PrayerKey`,
    description: guide.metaDesc,
    keywords:    guide.keywords.join(", "),
    authors:     [{ name: AUTHOR, url: `${BASE_URL}/author/collins-asein` }],
    alternates:  { canonical: `${BASE_URL}/guides/${guide.slug}` },
    openGraph: {
      title:       guide.title,
      description: guide.metaDesc,
      url:         `${BASE_URL}/guides/${guide.slug}`,
      images:      [{ url: "/og-image.png", width: 1200, height: 630 }],
      type:        "article",
    },
    twitter: { card: "summary_large_image", title: guide.title, description: guide.metaDesc },
  };
}

/* ── Page ──────────────────────────────────────────────────────────── */
export default function GuidePage({ params }: { params: { slug: string } }) {
  const guide = GUIDE_MAP.get(params.slug);
  if (!guide) notFound();

  const otherGuides = GUIDES.filter((g) => g.slug !== guide.slug);

  const jsonLd = {
    "@context": "https://schema.org",
    "@type": "Article",
    headline: guide.title,
    description: guide.metaDesc,
    url: `${BASE_URL}/guides/${guide.slug}`,
    dateModified: new Date().toISOString(),
    datePublished: "2026-06-01T00:00:00Z",
    author: {
      "@type": "Person", name: AUTHOR,
      url: `${BASE_URL}/author/collins-asein`,
      jobTitle: "Christian Author & Faith Technologist",
    },
    publisher: {
      "@type": "Organization", name: "PrayerKey", url: BASE_URL,
      logo: { "@type": "ImageObject", url: `${BASE_URL}/og-image.png` },
    },
    mainEntityOfPage: { "@type": "WebPage", "@id": `${BASE_URL}/guides/${guide.slug}` },
    breadcrumb: {
      "@type": "BreadcrumbList",
      itemListElement: [
        { "@type": "ListItem", position: 1, name: "Home",   item: BASE_URL },
        { "@type": "ListItem", position: 2, name: "Guides", item: `${BASE_URL}/guides` },
        { "@type": "ListItem", position: 3, name: guide.title, item: `${BASE_URL}/guides/${guide.slug}` },
      ],
    },
    keywords: guide.keywords.join(", "),
    mainEntity: {
      "@type": "FAQPage",
      mainEntity: guide.faqs.map((faq) => ({
        "@type": "Question",
        name: faq.q,
        acceptedAnswer: { "@type": "Answer", text: faq.a },
      })),
    },
  };

  return (
    <>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }} />

      <div style={{ maxWidth: "780px", margin: "0 auto", padding: "0 24px 100px" }}>

        {/* ── Breadcrumb ── */}
        <nav aria-label="Breadcrumb" style={{
          marginBottom: "28px", fontSize: "13px", color: "var(--pk-text-3)",
          display: "flex", gap: "6px", alignItems: "center", flexWrap: "wrap",
        }}>
          <Link href="/" style={{ color: "var(--pk-text-3)", textDecoration: "none" }}>Home</Link>
          <span>/</span>
          <Link href="/guides" style={{ color: "var(--pk-text-3)", textDecoration: "none" }}>Guides</Link>
          <span>/</span>
          <span style={{ color: "var(--pk-accent)" }}>{guide.title}</span>
        </nav>

        {/* ── Prayer tool — above the fold (Airbnb pattern) ── */}
        <PrayerComposer compact />

        {/* ── Header ── */}
        <div style={{ marginBottom: "36px" }}>
          <div style={{
            display: "inline-block", padding: "4px 12px", borderRadius: "4px",
            border: "1.5px solid var(--pk-accent-border)", background: "var(--pk-accent-dim)",
            fontSize: "10px", fontWeight: 700, color: "var(--pk-accent)",
            letterSpacing: "0.1em", textTransform: "uppercase", marginBottom: "16px",
          }}>
            Prayer Guide · {guide.readMinutes} min read
          </div>

          <h1 style={{
            fontSize: "clamp(28px, 5vw, 46px)", fontWeight: 800,
            color: "var(--pk-text)", margin: "0 0 16px",
            letterSpacing: "-0.03em", lineHeight: 1.12,
          }}>
            {guide.title}
          </h1>

          {/* Author bar */}
          <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "18px", flexWrap: "wrap" }}>
            <div style={{
              width: "36px", height: "36px", borderRadius: "50%",
              background: "linear-gradient(135deg, var(--pk-accent), var(--pk-purple))",
              display: "flex", alignItems: "center", justifyContent: "center",
              fontSize: "13px", fontWeight: 800, color: "#fff", flexShrink: 0,
            }}>CA</div>
            <div>
              <div style={{ fontSize: "13px", fontWeight: 700, color: "var(--pk-text)" }}>
                <Link href="/author/collins-asein" style={{ color: "var(--pk-text)", textDecoration: "none" }}>{AUTHOR}</Link>
                <span style={{ color: "var(--pk-text-3)", fontWeight: 400 }}> · Christian Author & Faith Technologist</span>
              </div>
              <div style={{ fontSize: "11px", color: "var(--pk-text-3)" }}>
                Scripture-verified · PrayerKey Editorial
              </div>
            </div>
          </div>

          <p style={{ fontSize: "17px", color: "var(--pk-text-2)", lineHeight: 1.8, margin: 0 }}>
            {guide.intro}
          </p>
        </div>

        {/* ── Sections ── */}
        {guide.sections.map((section) => (
          <section key={section.heading} style={{ marginBottom: "40px" }}>
            <h2 style={{
              fontSize: "clamp(20px, 3vw, 26px)", fontWeight: 700,
              color: "var(--pk-text)", margin: "0 0 14px", letterSpacing: "-0.02em",
            }}>
              {section.heading}
            </h2>
            {section.body.split("\n\n").map((para, i) => (
              <p key={i} style={{
                fontSize: "16px", color: "var(--pk-text-2)",
                lineHeight: 1.85, margin: i === 0 ? "0 0 14px" : "0 0 14px",
              }}>
                {para}
              </p>
            ))}
            {section.links && section.links.length > 0 && (
              <div style={{ display: "flex", gap: "8px", flexWrap: "wrap", marginTop: "4px" }}>
                {section.links.map((l) => (
                  <Link key={l.href} href={l.href} style={{
                    display: "inline-block", padding: "8px 16px",
                    borderRadius: "10px", border: "1.5px solid var(--pk-accent-border)",
                    background: "var(--pk-accent-dim)", fontSize: "13px", fontWeight: 600,
                    color: "var(--pk-accent)", textDecoration: "none",
                  }}>
                    {l.label} →
                  </Link>
                ))}
              </div>
            )}
          </section>
        ))}

        {/* ── FAQs ── */}
        <section style={{ marginBottom: "48px" }}>
          <h2 style={{ fontSize: "clamp(20px, 3vw, 26px)", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 18px", letterSpacing: "-0.02em" }}>
            Frequently Asked Questions
          </h2>
          <div style={{ display: "grid", gap: "12px" }}>
            {guide.faqs.map((faq) => (
              <details key={faq.q} style={{
                background: "var(--pk-surface)", border: "1.5px solid var(--pk-border)",
                borderRadius: "12px", padding: "16px 20px",
              }}>
                <summary style={{ fontSize: "15px", fontWeight: 700, color: "var(--pk-text)", cursor: "pointer" }}>
                  {faq.q}
                </summary>
                <p style={{ fontSize: "14px", color: "var(--pk-text-2)", lineHeight: 1.75, margin: "12px 0 0" }}>
                  {faq.a}
                </p>
              </details>
            ))}
          </div>
        </section>

        {/* ── More guides ── */}
        <section>
          <h2 style={{ fontSize: "18px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 14px" }}>
            More Prayer Guides
          </h2>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(240px, 1fr))", gap: "12px" }}>
            {otherGuides.map((g) => (
              <Link key={g.slug} href={`/guides/${g.slug}`} style={{
                display: "block", padding: "16px 18px",
                background: "var(--pk-surface)", border: "1.5px solid var(--pk-border)",
                borderRadius: "12px", textDecoration: "none",
              }}>
                <div style={{ fontSize: "14px", fontWeight: 700, color: "var(--pk-text)", marginBottom: "4px", lineHeight: 1.4 }}>
                  {g.title}
                </div>
                <div style={{ fontSize: "12px", color: "var(--pk-text-3)" }}>{g.readMinutes} min read</div>
              </Link>
            ))}
          </div>
        </section>
      </div>
    </>
  );
}
