import type { Metadata } from "next";

const BASE_URL = "https://www.prayerkey.com";
const PRAY_URL = `${BASE_URL}/pray`;

export const metadata: Metadata = {
  title:       "Free Prayer Generator — A Prayer Built on Scripture for What You Face | PrayerKey",
  description: "Write what is on your heart and receive a complete prayer built on Scripture, with the Bible verses to pray it with. Free, with no account.",
  keywords: [
    "free prayer generator",
    "prayer maker",
    "bible-based prayer generator",
    "prayer with scripture",
    "personalized prayer free",
    "prayer for anxiety",
    "prayer for healing",
    "morning prayer generator",
    "no signup prayer generator",
    "prayer for my situation",
  ],
  alternates: { canonical: PRAY_URL },
  openGraph: {
    title:       "Free Prayer Generator — A Prayer Built on Scripture",
    description: "Write what you need prayer for and receive a complete prayer built on Scripture. Free, with no account.",
    url:         PRAY_URL,
    type:        "website",
    siteName:    "PrayerKey",
    images: [{ url: "/og-image.png", width: 1200, height: 630, alt: "PrayerKey Prayer Generator" }],
  },
  twitter: {
    card:        "summary_large_image",
    title:       "Free Prayer Generator — PrayerKey",
    description: "A prayer built on Scripture for whatever you are facing. Free.",
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
  name:       "PrayerKey Prayer Generator",
  applicationCategory: "Lifestyle",
  operatingSystem: "Any (browser-based)",
  description: "A free prayer generator that writes a personal prayer built on Scripture for whatever you are facing.",
  url: PRAY_URL,
  offers: { "@type": "Offer", price: "0", priceCurrency: "USD" },
};

const breadcrumbLd = {
  "@context": "https://schema.org",
  "@type":    "BreadcrumbList",
  itemListElement: [
    { "@type": "ListItem", position: 1, name: "Home",                  item: BASE_URL },
    { "@type": "ListItem", position: 2, name: "Prayer Generator",   item: PRAY_URL },
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
