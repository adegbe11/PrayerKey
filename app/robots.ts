import { MetadataRoute } from "next";

export default function robots(): MetadataRoute.Robots {
  const base = "https://www.prayerkey.com";
  return {
    rules: [
      {
        userAgent: "*",
        allow:    ["/"],
        // Not /_next/: that is where every page's scripts and styles live, and
        // Google has to load them to see the page the way a reader does.
        disallow: ["/admin", "/admin/", "/api/"],
      },
      // Give the major AI crawlers explicit access — important for 2026 AI
      // Overviews and answer-engine surfaces.
      { userAgent: "GPTBot",          allow: "/" },
      { userAgent: "Google-Extended", allow: "/" },
      { userAgent: "ClaudeBot",       allow: "/" },
      { userAgent: "PerplexityBot",   allow: "/" },
      { userAgent: "CCBot",           allow: "/" },
    ],
    sitemap: `${base}/sitemap.xml`,
    host:    base,
  };
}
