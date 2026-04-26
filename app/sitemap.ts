import { MetadataRoute } from "next";
import { ALL_SLUGS } from "@/lib/seo/prayer-topics";
import { getAllVerseRefs, BIBLE_BOOKS } from "@/lib/seo/bible-books";

/**
 * PrayerKey Programmatic SEO Sitemap
 * ------------------------------------
 * Total pages: ~35,400+
 *   - Static pages        :     9
 *   - Author page         :     1
 *   - /prayer hub         :     1
 *   - /prayer/[slug]      :   116
 *   - /pray/[slug]        :   116
 *   - Bible verse pages   : 31,102  (ISR — generated on first request)
 *   - Bible book hubs     :    66
 */
export default function sitemap(): MetadataRoute.Sitemap {
  const base = "https://www.prayerkey.com";
  const now  = new Date();

  /* ── Static pages ── */
  const staticPages: MetadataRoute.Sitemap = [
    { url: `${base}/`,                        lastModified: now, changeFrequency: "weekly",  priority: 1.0 },
    { url: `${base}/pray`,                    lastModified: now, changeFrequency: "monthly", priority: 0.95 },
    { url: `${base}/pray/topics`,             lastModified: now, changeFrequency: "monthly", priority: 0.9 },
    { url: `${base}/bible`,                   lastModified: now, changeFrequency: "monthly", priority: 0.9 },
    { url: `${base}/live`,                    lastModified: now, changeFrequency: "monthly", priority: 0.8 },
    { url: `${base}/docs`,                    lastModified: now, changeFrequency: "monthly", priority: 0.9 },
    { url: `${base}/about`,                   lastModified: now, changeFrequency: "yearly",  priority: 0.5 },
    { url: `${base}/terms`,                   lastModified: now, changeFrequency: "yearly",  priority: 0.3 },
    { url: `${base}/privacy`,                 lastModified: now, changeFrequency: "yearly",  priority: 0.3 },
    // Author page — E-E-A-T signal
    { url: `${base}/author/collins-asein`,    lastModified: now, changeFrequency: "monthly", priority: 0.85 },
  ];

  /* ── /prayer hub ── */
  const prayerHub: MetadataRoute.Sitemap = [
    { url: `${base}/prayer`, lastModified: now, changeFrequency: "weekly" as const, priority: 0.95 },
  ];

  /* ── /prayer/[slug] — pSEO prayer pages ── */
  const prayerSlugPages: MetadataRoute.Sitemap = ALL_SLUGS.map((slug) => ({
    url:             `${base}/prayer/${slug}`,
    lastModified:    now,
    changeFrequency: "monthly" as const,
    priority:        0.85,
  }));

  /* ── /pray/[slug] — original prayer pages ── */
  const topicPages: MetadataRoute.Sitemap = ALL_SLUGS.map((slug) => ({
    url:             `${base}/pray/${slug}`,
    lastModified:    now,
    changeFrequency: "monthly" as const,
    priority:        0.8,
  }));

  /* ── Bible book hub pages ── */
  const bookPages: MetadataRoute.Sitemap = BIBLE_BOOKS.map((book) => ({
    url:             `${base}/bible`,
    lastModified:    now,
    changeFrequency: "monthly" as const,
    priority:        0.75,
  }));

  /* ── Bible verse pages — 31,102 individual verse pages ──
     These pages use ISR (revalidate = 86400) so they are
     generated on first visit and cached for 24 hours.
     We include all in the sitemap so Google can discover them. */
  const verseRefs  = getAllVerseRefs();
  const versePages: MetadataRoute.Sitemap = verseRefs.map((ref) => ({
    url:             `${base}/bible/verse/${ref}`,
    lastModified:    now,
    changeFrequency: "yearly" as const,
    priority:        0.65,
  }));

  return [
    ...staticPages,
    ...prayerHub,
    ...prayerSlugPages,
    ...topicPages,
    ...bookPages,
    ...versePages,
  ];
}
