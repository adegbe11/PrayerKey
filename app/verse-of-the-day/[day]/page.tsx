import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import DevotionalView from "@/components/devotional/DevotionalView";
import { devotionalBySlug, DEVOTIONALS } from "@/lib/devotionals";
import "@/components/prayer/cards.css";
import "@/components/prayer/post.css";
import "@/components/devotional/devotional.css";

const BASE_URL = "https://www.prayerkey.com";

// One page for every day of the year, so each day's verse can be found on its own.
export function generateStaticParams() {
  return DEVOTIONALS.map((d) => ({ day: d.slug }));
}

export async function generateMetadata({ params }: { params: { day: string } }): Promise<Metadata> {
  const d = devotionalBySlug(params.day);
  if (!d) return {};
  const url = `${BASE_URL}/verse-of-the-day/${d.slug}`;
  const title = `${d.dateLabel} Verse of the Day with Reflection: ${d.verse.ref} | PrayerKey`;
  const description = `The verse of the day for ${d.dateLabel}: ${d.verse.ref} (KJV), with a reflection, ${d.title}, a question to carry and a prayer.`;
  return {
    title,
    description,
    keywords: [`verse of the day ${d.dateLabel.toLowerCase()}`, `${d.dateLabel.toLowerCase()} bible verse`, "verse of the day with reflection", d.verse.ref, "daily devotional"].join(", "),
    alternates: { canonical: url },
    openGraph: { title, description, url, type: "article", images: [{ url: "/og-image.png", width: 1200, height: 630 }] },
    twitter: { card: "summary_large_image", title, description },
  };
}

export default function VerseOfTheDayDatePage({ params }: { params: { day: string } }) {
  const d = devotionalBySlug(params.day);
  if (!d) notFound();
  const url = `${BASE_URL}/verse-of-the-day/${d.slug}`;

  const jsonLd = {
    "@context": "https://schema.org",
    "@graph": [
      {
        "@type": "Article",
        headline: `${d.dateLabel} Verse of the Day: ${d.title}`,
        description: `${d.verse.ref}: ${d.verse.text}`,
        url,
        author: { "@type": "Person", name: "Collins Asein", url: `${BASE_URL}/author/collins-asein` },
        publisher: { "@type": "Organization", name: "PrayerKey", url: BASE_URL, logo: { "@type": "ImageObject", url: `${BASE_URL}/og-image.png` } },
        about: { "@type": "CreativeWork", name: d.verse.ref, text: d.verse.text },
      },
      {
        "@type": "BreadcrumbList",
        itemListElement: [
          { "@type": "ListItem", position: 1, name: "Home", item: BASE_URL },
          { "@type": "ListItem", position: 2, name: "Verse of the Day", item: `${BASE_URL}/verse-of-the-day` },
          { "@type": "ListItem", position: 3, name: d.dateLabel, item: url },
        ],
      },
    ],
  };

  return (
    <>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }} />
      <DevotionalView d={d} heading={`Verse of the Day for ${d.dateLabel}`} dateLine={d.dateLabel} url={url}>
        <p className="vd-today"><Link href="/verse-of-the-day">See today&apos;s verse of the day</Link></p>
      </DevotionalView>
    </>
  );
}
