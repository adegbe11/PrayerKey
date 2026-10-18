import { notFound } from "next/navigation";
import "@/components/guides/guides.css";
import type { Metadata } from "next";
import Link from "next/link";
import PrayerComposer from "@/components/PrayerComposer";
import { GUIDES, GUIDE_MAP, GUIDE_SLUGS } from "@/lib/seo/guides";
import { guideArt } from "@/lib/seo/guide-art";
import InView from "@/components/home/InView";
import GuideCover from "@/components/guides/GuideCover";
import GuideToc from "@/components/guides/GuideToc";

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

      <InView />
      <article className="gd">
        {/* the cover, opened */}
        <header className="gd-hero pk-bleed" data-nav="dark">
          <img src={guideArt(guide.slug)} alt="" className="gd-hero__art" />
          <div className="gd-hero__in">
            <nav aria-label="Breadcrumb" className="gd-crumbs">
              <Link href="/">Home</Link><span>/</span><Link href="/guides">Guides</Link>
            </nav>
            <h1 className="gd-title">{guide.title}</h1>
            <p className="gd-meta"><Link href="/author/collins-asein">{AUTHOR}</Link><span>{guide.readMinutes} min read</span></p>
          </div>
        </header>

        <div className="gd-layout">
          <GuideToc headings={guide.sections.map((s) => s.heading)} />

          <div className="gd-body">
            <p className="gd-intro">{guide.intro}</p>

            {guide.sections.map((section, i) => (
              <section key={section.heading} id={`s${i + 1}`} className="gd-section" data-in="">
                <span className="gd-num" aria-hidden>{String(i + 1).padStart(2, "0")}</span>
                <h2 className="gd-h2">{section.heading}</h2>
                {section.body.split("\n\n").map((para, j) => <p key={j} className="gd-p">{para}</p>)}
                {section.links && section.links.length > 0 && (
                  <ul className="gd-links">
                    {section.links.map((l) => <li key={l.href}><Link href={l.href}>{l.label}</Link></li>)}
                  </ul>
                )}
              </section>
            ))}

            <section className="gd-own" data-in="" aria-labelledby="own">
              <h2 id="own" className="gd-h2">Pray now</h2>
              <PrayerComposer compact />
            </section>

            <section className="gd-faq" data-in="" aria-labelledby="faq">
              <h2 id="faq" className="gd-h2">Questions</h2>
              {guide.faqs.map((faq) => (
                <details key={faq.q}>
                  <summary><span>{faq.q}</span><span aria-hidden className="gd-faq__plus" /></summary>
                  <p>{faq.a}</p>
                </details>
              ))}
            </section>
          </div>
        </div>

        <section className="gd-more" data-in="" aria-labelledby="more">
          <h2 id="more" className="gd-h2">Keep reading</h2>
          <div className="gd-more__grid">
            {otherGuides.slice(0, 3).map((g, i) => (
              <GuideCover key={g.slug} href={`/guides/${g.slug}`} art={guideArt(g.slug)} title={g.title} minutes={g.readMinutes} style={{ "--i": i } as React.CSSProperties} />
            ))}
          </div>
        </section>
      </article>
    </>
  );
}
