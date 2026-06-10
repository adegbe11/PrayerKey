import type { Metadata, Viewport } from "next";
import dynamic from "next/dynamic";
import "./globals.css";
import SimpleNav from "@/components/layout/SimpleNav";
import Link from "next/link";

// Defer cursor — it runs a rAF loop; loading it after hydration keeps the
// main thread free during LCP and avoids an unnecessary server-side pass.
const Cursor = dynamic(() => import("@/components/ui/Cursor"), { ssr: false });

const BASE_URL = "https://www.prayerkey.com";

export const metadata: Metadata = {
  title: "PrayerKey — Free AI Prayer Generator, Live Sermon & Bible Search",
  description: "Instantly generate prayers with scripture, detect Bible verses during live sermons, and search all 66 books of the Bible. Free. No account needed.",
  metadataBase: new URL(BASE_URL),
  alternates: { canonical: BASE_URL },
  openGraph: {
    title:       "PrayerKey — Free AI Prayer Generator, Live Sermon & Bible Search",
    description: "Instantly generate prayers with scripture, detect Bible verses during live sermons, and search all 66 books of the Bible. Free. No account needed.",
    url:         BASE_URL,
    siteName:    "PrayerKey",
    images: [{ url: "/og-image.png", width: 1200, height: 630, alt: "PrayerKey — AI-powered church companion" }],
    locale: "en_US",
    type:   "website",
  },
  twitter: {
    card:        "summary_large_image",
    title:       "PrayerKey — Free AI Prayer Generator, Live Sermon & Bible Search",
    description: "Instantly generate prayers with scripture, detect Bible verses during live sermons, and search all 66 books of the Bible. Free. No account needed.",
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
  themeColor:   [
    { media: "(prefers-color-scheme: light)", color: "#FFFFFF" },
    { media: "(prefers-color-scheme: dark)",  color: "#060608" },
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
  description: "Free AI-powered church companion: AI prayer generator, live sermon Bible-verse detection, and Bible search.",
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
  description: "Free AI-powered church technology: live sermon Bible-verse detection, AI prayer generation, and Bible search. Used by pastors and believers worldwide.",
  founder: {
    "@type": "Person",
    name:    "Collins Omoikhudu Asein",
    url:     `${BASE_URL}/author/collins-asein`,
  },
  foundingDate: "2024",
  areaServed:   "Worldwide",
  knowsAbout:   ["Bible", "Prayer", "Church Technology", "AI", "Sermon Tools"],
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
  description: "Free AI-powered church companion — AI prayer generator, live sermon Bible-verse projector, and full Bible search with cross-references.",
  offers: {
    "@type":       "Offer",
    price:         "0",
    priceCurrency: "USD",
    availability:  "https://schema.org/InStock",
  },
  featureList: [
    "AI prayer generator with scripture",
    "Real-time live sermon Bible verse detection",
    "11 Bible translations",
    "Full Bible search by keyword, topic, or reference",
    "Cross-reference lookup",
    "No account required",
    "Free forever",
  ],
  aggregateRating: {
    "@type":       "AggregateRating",
    ratingValue:   "4.9",
    ratingCount:   "214",
    bestRating:    "5",
    worstRating:   "1",
  },
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" data-theme="light">
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
        <Cursor />
        <SimpleNav />

        <main style={{
          maxWidth: "1440px",
          margin:   "0 auto",
          padding:  "clamp(32px,5vw,64px) clamp(20px,4vw,80px) 100px",
        }}>
          {children}
        </main>

        <footer style={{
          borderTop:  "1px solid var(--pk-footer-border)",
          overflow:   "hidden",
          background: "var(--pk-surface)",
          transition: "background 250ms ease, border-color 250ms ease",
        }}>
          {/* Massive wordmark */}
          <div style={{ padding: "0", lineHeight: 0, userSelect: "none" }}>
            <svg
              width="100%"
              viewBox="0 0 1000 175"
              preserveAspectRatio="none"
              style={{ display: "block" }}
            >
              <text
                x="0"
                y="158"
                textLength="1000"
                lengthAdjust="spacingAndGlyphs"
                style={{
                  fontFamily:    "-apple-system,'SF Pro Display','Helvetica Neue',sans-serif",
                  fontWeight:    900,
                  fontSize:      "175px",
                  fill:          "var(--pk-wordmark-fill)",
                  letterSpacing: "-4px",
                  opacity:       0.18,
                }}
              >
                PRAYERKEY
              </text>
            </svg>
          </div>

          {/* Footer nav */}
          <div style={{
            borderTop:      "1px solid var(--pk-footer-border)",
            padding:        "18px clamp(20px,4vw,48px)",
            display:        "flex",
            flexWrap:       "wrap",
            alignItems:     "center",
            justifyContent: "space-between",
            gap:            "12px",
          }}>
            <span style={{ fontSize: "12px", color: "var(--pk-footer-text)", letterSpacing: "0.02em" }}>
              {new Date().getFullYear()} © PrayerKey
            </span>
            <div style={{ display: "flex", gap: "28px", flexWrap: "wrap" }}>
              {[
                { href: "/about",                    label: "About" },
                { href: "/author/collins-asein",     label: "Author" },
                { href: "/prayer",                   label: "All Prayers" },
                { href: "/guides",                   label: "Guides" },
                { href: "/bible",                    label: "Bible" },
                { href: "/docs",                     label: "Docs" },
                { href: "/terms",                    label: "Terms" },
                { href: "/privacy",                  label: "Privacy" },
              ].map((l) => (
                <Link key={l.href} href={l.href} style={{
                  fontSize:       "12px",
                  color:          "var(--pk-footer-text)",
                  textDecoration: "none",
                  letterSpacing:  "0.02em",
                  transition:     "color 150ms ease",
                }}>
                  {l.label}
                </Link>
              ))}
            </div>
          </div>
        </footer>
      </body>
    </html>
  );
}
