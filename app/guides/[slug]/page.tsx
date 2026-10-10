import { notFound } from "next/navigation";
import "@/components/article/article.css";
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

      <article className="ar">
        <nav aria-label="Breadcrumb" className="ar-crumbs">
          <Link href="/">Home</Link><span>/</span>
          <Link href="/guides">Guides</Link>
        </nav>

        <p className="ar-eyebrow">Prayer guide · {guide.readMinutes} min read</p>
        <h1 className="ar-h1">{guide.title}</h1>
        <p className="ar-lede">{guide.intro}</p>

        <div className="ar-byline">
          <span className="ar-byline__face" aria-hidden>CA</span>
          <div>
            <Link href="/author/collins-asein">{AUTHOR}</Link>
            <br /><span>PrayerKey Editorial</span>
          </div>
        </div>

        {/* On this page: the guide's sections, numbered, so it can be read in any order */}
        <nav className="ar-toc" aria-label="On this page">
          <p className="ar-eyebrow">On this page</p>
          <ol>
            {guide.sections.map((section, i) => (
              <li key={section.heading}><a href={`#s${i + 1}`}>{section.heading}</a></li>
            ))}
          </ol>
        </nav>

        {guide.sections.map((section, i) => (
          <section key={section.heading} id={`s${i + 1}`} className="ar-section">
            <h2 className="ar-h2"><span className="ar-num">{i + 1}.</span> {section.heading}</h2>
            {section.body.split("\n\n").map((para, j) => (
              <p key={j} className="ar-p">{para}</p>
            ))}
            {section.links && section.links.length > 0 && (
              <ul className="ar-links">
                {section.links.map((l) => (
                  <li key={l.href}><Link href={l.href}>{l.label}</Link></li>
                ))}
              </ul>
            )}
          </section>
        ))}

        <section className="ar-own" aria-labelledby="own">
          <h2 id="own" className="ar-h2">Pray now</h2>
          <p>Write what is on your heart and pray it with Scripture.</p>
          <PrayerComposer compact />
        </section>

        <section className="ar-section ar-faq" aria-labelledby="faq">
          <h2 id="faq" className="ar-h2">Questions</h2>
          {guide.faqs.map((faq) => (
            <details key={faq.q}>
              <summary>{faq.q}</summary>
              <p>{faq.a}</p>
            </details>
          ))}
        </section>

        <section className="ar-section" aria-labelledby="more">
          <h2 id="more" className="ar-h2">More guides</h2>
          <ul className="ar-links">
            {otherGuides.map((g) => (
              <li key={g.slug}><Link href={`/guides/${g.slug}`}>{g.title}</Link></li>
            ))}
          </ul>
        </section>
      </article>
    </>
  );
}
