import type { Metadata } from "next";

const BASE_URL = "https://www.prayerkey.com";
const PRAY_URL = `${BASE_URL}/pray`;

export const metadata: Metadata = {
  title:       "Free AI Prayer Generator — Personalized Scripture-Grounded Prayers | PrayerKey",
  description: "Type what you're feeling and get a complete Bible-based prayer in under 10 seconds. Free, no account needed. Includes scripture references and an encouragement note. Used by pastors and believers worldwide.",
  keywords: [
    "free ai prayer generator",
    "ai prayer maker",
    "bible-based prayer generator",
    "prayer with scripture",
    "personalized prayer free",
    "prayer for anxiety",
    "prayer for healing",
    "morning prayer generator",
    "no signup prayer generator",
    "scripture-grounded prayer ai",
  ],
  alternates: { canonical: PRAY_URL },
  openGraph: {
    title:       "Free AI Prayer Generator — Personalized Scripture-Grounded Prayers",
    description: "Type what you need prayer for. Get a complete Bible-based prayer in under 10 seconds. Free forever, no account required.",
    url:         PRAY_URL,
    type:        "website",
    siteName:    "PrayerKey",
    images: [{ url: "/og-image.png", width: 1200, height: 630, alt: "PrayerKey AI Prayer Generator" }],
  },
  twitter: {
    card:        "summary_large_image",
    title:       "Free AI Prayer Generator — PrayerKey",
    description: "Personalized scripture-grounded prayers in 10 seconds. Free forever.",
    images:      ["/og-image.png"],
  },
  robots: {
    index: true, follow: true,
    googleBot: { index: true, follow: true, "max-image-preview": "large", "max-snippet": -1 },
  },
};

const softwareLd = {
  "@context": "https://schema.org",
  "@type":    "WebApplication",
  name:       "PrayerKey AI Prayer Generator",
  applicationCategory: "Lifestyle",
  operatingSystem: "Any (browser-based)",
  description: "Free AI-powered prayer generator that produces personalized, scripture-grounded prayers in under 10 seconds.",
  url: PRAY_URL,
  offers: { "@type": "Offer", price: "0", priceCurrency: "USD" },
};

const breadcrumbLd = {
  "@context": "https://schema.org",
  "@type":    "BreadcrumbList",
  itemListElement: [
    { "@type": "ListItem", position: 1, name: "Home",                  item: BASE_URL },
    { "@type": "ListItem", position: 2, name: "AI Prayer Generator",   item: PRAY_URL },
  ],
};

export default function PrayLayout({ children }: { children: React.ReactNode }) {
  return (
    <>
      <script type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(softwareLd) }} />
      <script type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(breadcrumbLd) }} />
      {children}
    </>
  );
}
