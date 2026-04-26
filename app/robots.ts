import { MetadataRoute } from "next";

export default function robots(): MetadataRoute.Robots {
  const base = "https://www.prayerkey.com";
  return {
    rules: [
      {
        userAgent: "*",
        allow:    ["/"],
        disallow: ["/admin", "/admin/", "/api/", "/(auth)/", "/_next/"],
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
