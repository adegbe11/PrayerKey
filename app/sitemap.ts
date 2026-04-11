import { MetadataRoute } from "next";
import { ALL_SLUGS } from "@/lib/seo/prayer-topics";

export default function sitemap(): MetadataRoute.Sitemap {
  const base = "https://www.prayerkey.com";
  const now  = new Date();

  const staticPages: MetadataRoute.Sitemap = [
    { url: `${base}/`,             lastModified: now, changeFrequency: "weekly",  priority: 1.0 },
    { url: `${base}/pray`,         lastModified: now, changeFrequency: "monthly", priority: 0.9 },
    { url: `${base}/pray/topics`,  lastModified: now, changeFrequency: "monthly", priority: 0.85 },
    { url: `${base}/bible`,        lastModified: now, changeFrequency: "monthly", priority: 0.8 },
    { url: `${base}/live`,         lastModified: now, changeFrequency: "monthly", priority: 0.8 },
    { url: `${base}/about`,        lastModified: now, changeFrequency: "yearly",  priority: 0.5 },
    { url: `${base}/terms`,        lastModified: now, changeFrequency: "yearly",  priority: 0.3 },
    { url: `${base}/privacy`,      lastModified: now, changeFrequency: "yearly",  priority: 0.3 },
  ];

  const topicPages: MetadataRoute.Sitemap = ALL_SLUGS.map((slug) => ({
    url:             `${base}/pray/${slug}`,
    lastModified:    now,
    changeFrequency: "monthly" as const,
    priority:        0.8,
  }));

  return [...staticPages, ...topicPages];
}
