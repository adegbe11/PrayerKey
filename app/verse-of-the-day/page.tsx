import type { Metadata } from "next";
import Link from "next/link";
import DevotionalView from "@/components/devotional/DevotionalView";
import { devotionalFor, DEVOTIONALS, MONTHS_INDEX } from "@/lib/devotionals";
import "@/components/prayer/cards.css";
import "@/components/prayer/post.css";
import "@/components/devotional/devotional.css";

const BASE_URL = "https://www.prayerkey.com";
const URL = `${BASE_URL}/verse-of-the-day`;

// Today's verse changes at midnight (UTC); the page is rebuilt every hour.
export const revalidate = 3600;

export async function generateMetadata(): Promise<Metadata> {
  const d = devotionalFor(new Date());
  const title = `Verse of the Day with Reflection: ${d.verse.ref} | PrayerKey`;
  const description = `Today's Bible verse, ${d.verse.ref} (KJV), with a short reflection, ${d.title}, a question to carry and a prayer. A new verse and reflection every day.`;
  return {
    title,
    description,
    keywords: ["verse of the day", "verse of the day with reflection", "bible verse of the day", "verse of the day kjv", "daily bible verse", "today's bible verse", "daily devotional"].join(", "),
    alternates: { canonical: URL },
    openGraph: { title, description, url: URL, type: "article", images: [{ url: "/og-image.png", width: 1200, height: 630 }] },
    twitter: { card: "summary_large_image", title, description },
  };
}

export default function VerseOfTheDayPage() {
  const now = new Date();
  const d = devotionalFor(now);
  const weekday = now.toLocaleDateString("en-GB", { weekday: "long", timeZone: "UTC" });

  const jsonLd = {
    "@context": "https://schema.org",
    "@graph": [
      {
        "@type": "Article",
        headline: `Verse of the Day with Reflection: ${d.title}`,
        description: `${d.verse.ref}: ${d.verse.text}`,
        url: URL,
        datePublished: now.toISOString().slice(0, 10),
        dateModified: now.toISOString().slice(0, 10),
        author: { "@type": "Person", name: "Collins Asein", url: `${BASE_URL}/author/collins-asein` },
        publisher: { "@type": "Organization", name: "PrayerKey", url: BASE_URL, logo: { "@type": "ImageObject", url: `${BASE_URL}/og-image.png` } },
        about: { "@type": "CreativeWork", name: d.verse.ref, text: d.verse.text },
      },
      {
        "@type": "BreadcrumbList",
        itemListElement: [
          { "@type": "ListItem", position: 1, name: "Home", item: BASE_URL },
          { "@type": "ListItem", position: 2, name: "Verse of the Day", item: URL },
        ],
      },
    ],
  };

  return (
    <>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }} />
      <DevotionalView d={d} heading="Verse of the Day with Reflection" dateLine={`${weekday}, ${d.dateLabel}`} url={URL}>
        {/* every day of the year */}
        <section className="vd-year" data-in="" aria-labelledby="vd-year">
          <h2 id="vd-year" className="vd-h2">Every day of the year</h2>
          <div className="vd-year__months">
            {MONTHS_INDEX.map((m) => (
              <details key={m.month} open={m.month === d.month}>
                <summary><span>{m.name}</span><small>{m.series}</small></summary>
                <ol>
                  {DEVOTIONALS.filter((x) => x.month === m.month).map((x) => (
                    <li key={x.slug} className={x.slug === d.slug ? "is-today" : ""}>
                      <Link href={`/verse-of-the-day/${x.slug}`}><b>{x.day}</b>{x.title}</Link>
                    </li>
                  ))}
                </ol>
              </details>
            ))}
          </div>
        </section>
      </DevotionalView>
    </>
  );
}
