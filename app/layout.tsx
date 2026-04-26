import type { Metadata } from "next";
import "./globals.css";
import SimpleNav from "@/components/layout/SimpleNav";
import Cursor from "@/components/ui/Cursor";
import Link from "next/link";

const BASE_URL = "https://prayerkey.com";

export const metadata: Metadata = {
  title: "PrayerKey — Free AI Prayer Generator, Live Sermon & Bible Search",
  description: "Instantly generate prayers with scripture, detect Bible verses during live sermons, and search all 66 books of the Bible. Free. No account needed.",
  metadataBase: new URL(BASE_URL),
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
    icon: [
      { url: "/icon-48.png",  sizes: "48x48",   type: "image/png" },
      { url: "/icon-72.png",  sizes: "72x72",   type: "image/png" },
      { url: "/icon-96.png",  sizes: "96x96",   type: "image/png" },
      { url: "/icon-192.png", sizes: "192x192", type: "image/png" },
    ],
    apple: [
      { url: "/icon-120.png", sizes: "120x120", type: "image/png" },
      { url: "/icon-152.png", sizes: "152x152", type: "image/png" },
      { url: "/icon-167.png", sizes: "167x167", type: "image/png" },
      { url: "/icon-180.png", sizes: "180x180", type: "image/png" },
      { url: "/apple-touch-icon.png" },
    ],
    other: [{ rel: "mask-icon", url: "/og-image.png" }],
  },
  manifest: "/site.webmanifest",
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
                { href: "/bible",                    label: "Bible" },
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
