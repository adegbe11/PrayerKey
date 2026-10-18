import type { Metadata, Viewport } from "next";
import { Inter, Lora, Playfair_Display } from "next/font/google";
import "./globals.css";
import CapsuleNav from "@/components/layout/CapsuleNav";
import SkyFooter from "@/components/layout/SkyFooter";
import "@/components/layout/sky-footer.css";
import AppBanner from "@/components/app/AppBanner";

// The app's three typefaces: Playfair for display, Lora for scripture, Inter for everything else.
const display = Playfair_Display({ subsets: ["latin"], weight: ["600", "700"], variable: "--font-display", display: "swap" });
const book = Lora({ subsets: ["latin"], weight: ["400", "500"], style: ["normal", "italic"], variable: "--font-book", display: "swap" });
const sans = Inter({ subsets: ["latin"], variable: "--font-sans", display: "swap" });

const BASE_URL = "https://www.prayerkey.com";

export const metadata: Metadata = {
  title: "PrayerKey — Daily Prayer, the Bible and a Prayer Journal",
  description: "Pray every day with ten short pulls, read the whole Bible offline, keep a private prayer journal and take sermon notes. Free. No account needed.",
  metadataBase: new URL(BASE_URL),
  alternates: { canonical: BASE_URL },
  openGraph: {
    title:       "PrayerKey — Daily Prayer, the Bible and a Prayer Journal",
    description: "Pray every day with ten short pulls, read the whole Bible offline, keep a private prayer journal and take sermon notes. Free. No account needed.",
    url:         BASE_URL,
    siteName:    "PrayerKey",
    images: [{ url: "/og-image.png", width: 1200, height: 630, alt: "PrayerKey — daily prayer and the Bible" }],
    locale: "en_US",
    type:   "website",
  },
  twitter: {
    card:        "summary_large_image",
    title:       "PrayerKey — Daily Prayer, the Bible and a Prayer Journal",
    description: "Pray every day with ten short pulls, read the whole Bible offline, keep a private prayer journal and take sermon notes. Free. No account needed.",
    images:      ["/og-image.png"],
  },
  icons: {
    shortcut: "/prayerkey-icon.png",
    icon: [
      { url: "/prayerkey-icon.png", sizes: "any",     type: "image/png" },
      { url: "/icon-32.png",        sizes: "32x32",   type: "image/png" },
      { url: "/icon-96.png",        sizes: "96x96",   type: "image/png" },
      { url: "/icon-192.png",       sizes: "192x192", type: "image/png" },
      { url: "/icon-512.png",       sizes: "512x512", type: "image/png" },
    ],
    apple: [
      { url: "/apple-touch-icon.png", sizes: "180x180", type: "image/png" },
    ],
    other: [{ rel: "mask-icon", url: "/prayerkey-icon.png" }],
  },
  manifest: "/site.webmanifest",
};

// Explicit viewport — controls mobile scaling and disables auto-zoom on inputs.
// Kept separate from metadata (Next.js 14 requirement).
export const viewport: Viewport = {
  width:        "device-width",
  initialScale: 1,
  viewportFit:  "cover", // iOS: paint behind the notch / Dynamic Island
  themeColor:   [
    { media: "(prefers-color-scheme: light)", color: "#F5F5F7" },
    { media: "(prefers-color-scheme: dark)",  color: "#160022" },
  ],
};

/* ── Global JSON-LD schemas ─────────────────────────────────────────────
   These describe the whole site and appear on every page (Google uses
   the root layout's <head> for sitewide signals):
   - WebSite      → enables Sitelinks Searchbox in Google results
   - Organization → enables Knowledge Panel with logo + social links
   - SiteNavigationElement → signals primary nav to search engines
──────────────────────────────────────────────────────────────────────── */
const websiteLd = {
  "@context": "https://schema.org",
  "@type":    "WebSite",
  name:       "PrayerKey",
  url:        BASE_URL,
  description: "A free Christian prayer app: daily prayer, the whole Bible offline, a private prayer journal and sermon notes.",
  inLanguage:  "en-US",
  potentialAction: {
    "@type":       "SearchAction",
    target: {
      "@type": "EntryPoint",
      urlTemplate: `${BASE_URL}/bible?q={search_term_string}`,
    },
    "query-input": "required name=search_term_string",
  },
};

const organizationLd = {
  "@context": "https://schema.org",
  "@type":    "Organization",
  "@id":      `${BASE_URL}/#organization`,
  name:       "PrayerKey",
  url:        BASE_URL,
  logo: {
    "@type":      "ImageObject",
    url:          `${BASE_URL}/og-image.png`,
    width:        1200,
    height:       630,
  },
  description: "Free church tools: Bible verses on the screen during a live sermon, prayers built on Scripture, and Bible search.",
  founder: {
    "@type": "Person",
    name:    "Collins Omoikhudu Asein",
    url:     `${BASE_URL}/author/collins-asein`,
  },
  foundingDate: "2024",
  areaServed:   "Worldwide",
  knowsAbout:   ["Bible", "Prayer", "Church Technology", "Sermon Tools"],
  sameAs: [
    "https://github.com/adegbe11/PrayerKey",
  ],
};

const navLd = {
  "@context": "https://schema.org",
  "@type":    "SiteLinksSearchBox",
  url:        BASE_URL,
  potentialAction: {
    "@type":       "SearchAction",
    target:        `${BASE_URL}/bible?q={search_term_string}`,
    "query-input": "required name=search_term_string",
  },
};

const softwareLd = {
  "@context": "https://schema.org",
  "@type":    "SoftwareApplication",
  "@id":      `${BASE_URL}/#software`,
  name:       "PrayerKey",
  url:        BASE_URL,
  applicationCategory: "LifestyleApplication",
  applicationSubCategory: "Religious & Spiritual",
  operatingSystem: "Any (browser-based)",
  description: "A free Christian companion: prayers built on Scripture, a live sermon Bible-verse projector, and full Bible search with cross-references.",
  offers: {
    "@type":       "Offer",
    price:         "0",
    priceCurrency: "USD",
    availability:  "https://schema.org/InStock",
  },
  featureList: [
    "Prayers built on Scripture",
    "Real-time live sermon Bible verse detection",
    "11 Bible translations",
    "Full Bible search by keyword, topic, or reference",
    "Cross-reference lookup",
    "No account required",
    "Free forever",
  ],
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    // suppressHydrationWarning: the anti-flash script swaps data-theme
    // from localStorage before React hydrates — expected, not a bug
    <html lang="en" data-theme="light" className={`${display.variable} ${book.variable} ${sans.variable}`} suppressHydrationWarning>
      <head>
        {/* ── Anti-flash theme script — runs before any paint ── */}
        <script
          dangerouslySetInnerHTML={{
            __html: `(function(){try{var t=localStorage.getItem('pk-theme')||'light';document.documentElement.setAttribute('data-theme',t);}catch(e){}})();`,
          }}
        />
        {/* ── Sitewide JSON-LD ── */}
        <script type="application/ld+json"
          dangerouslySetInnerHTML={{ __html: JSON.stringify(websiteLd) }} />
        <script type="application/ld+json"
          dangerouslySetInnerHTML={{ __html: JSON.stringify(organizationLd) }} />
        <script type="application/ld+json"
          dangerouslySetInnerHTML={{ __html: JSON.stringify(navLd) }} />
        <script type="application/ld+json"
          dangerouslySetInnerHTML={{ __html: JSON.stringify(softwareLd) }} />
      </head>
      <body>
        <AppBanner />
        <CapsuleNav />

        {/* the page is a sheet; at the end it lifts off the night sky */}
        <div className="pk-stage">
          <div className="pk-curtain">
            <main style={{
              maxWidth: "1440px",
              margin:   "0 auto",
              padding:  "clamp(32px,5vw,64px) clamp(20px,4vw,80px) 100px",
            }}>
              {children}
            </main>
          </div>
          <SkyFooter />
        </div>
      </body>
    </html>
  );
}
