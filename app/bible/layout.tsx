import type { Metadata } from "next";

const BASE_URL  = "https://www.prayerkey.com";
const BIBLE_URL = `${BASE_URL}/bible`;

export const metadata: Metadata = {
  title:       "Free Bible Search — Keyword, Verse & Cross-Reference Lookup | PrayerKey",
  description: "Search the entire Bible by keyword, phrase, or reference. Get instant cross-references and contextual verses across 11 translations. Free, no account required.",
  keywords: [
    "free bible search",
    "bible search engine",
    "bible search by keyword",
    "bible verse cross references",
    "bible verse lookup",
    "search bible by phrase",
    "bible concordance online",
    "free online bible",
    "kjv niv esv search",
    "bible cross reference tool",
  ],
  alternates: { canonical: BIBLE_URL },
  openGraph: {
    title:       "Free Bible Search — Keyword, Verse & Cross-Reference Lookup",
    description: "Search the entire Bible by keyword, phrase, or reference. Instant cross-references across 11 translations. Free, no account required.",
    url:         BIBLE_URL,
    type:        "website",
    siteName:    "PrayerKey",
    images: [{ url: "/og-image.png", width: 1200, height: 630, alt: "PrayerKey Bible Search" }],
  },
  twitter: {
    card:        "summary_large_image",
    title:       "Free Bible Search — PrayerKey",
    description: "Search the entire Bible by keyword, phrase, or reference. Free, no account.",
    images:      ["/og-image.png"],
  },
  robots: {
    index: true, follow: true,
    googleBot: { index: true, follow: true, "max-image-preview": "large", "max-snippet": -1 },
  },
};

const websiteLd = {
  "@context": "https://schema.org",
  "@type":    "WebApplication",
  name:       "PrayerKey Bible Search",
  applicationCategory: "ReferenceApplication",
  operatingSystem: "Any (browser-based)",
  description: "Free Bible search engine with keyword, phrase, and reference lookup plus AI-powered cross-references across 11 translations.",
  url: BIBLE_URL,
  offers: { "@type": "Offer", price: "0", priceCurrency: "USD" },
};

const breadcrumbLd = {
  "@context": "https://schema.org",
  "@type":    "BreadcrumbList",
  itemListElement: [
    { "@type": "ListItem", position: 1, name: "Home",          item: BASE_URL },
    { "@type": "ListItem", position: 2, name: "Bible Search",  item: BIBLE_URL },
  ],
};

export default function BibleLayout({ children }: { children: React.ReactNode }) {
  return (
    <>
      <script type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(websiteLd) }} />
      <script type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(breadcrumbLd) }} />
      {children}
    </>
  );
}
