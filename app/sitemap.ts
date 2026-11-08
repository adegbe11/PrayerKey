import { DEVOTIONALS } from "@/lib/devotionals";
import { MetadataRoute } from "next";
import { ALL_SLUGS } from "@/lib/seo/prayer-topics";
import { BIBLE_BOOKS } from "@/lib/seo/bible-books";
import { GUIDE_SLUGS } from "@/lib/seo/guides";
import { INDEXED_VERSES } from "@/lib/seo/indexed-verses";

/**
 * PrayerKey sitemap: the pages we ask Google to index.
 *
 *   - Static pages, author, hubs
 *   - /prayer/[slug]       one page per prayer. /pray/[slug] showed the same
 *                          prayer at a second address; it now redirects here.
 *   - Pillar guides
 *   - Bible book hubs      66
 *   - Bible verse pages    only the verses in lib/seo/indexed-verses.ts, not
 *                          all 31,102 (see that file for why)
 *   - Verse of the day     today, and one page for each day of the year
 *
 * No lastModified, except on today's verse of the day, which really does
 * change every day. Stamping every page with "now" told Google that 35,000
 * pages changed on every crawl, which it learns to ignore.
 */
export default function sitemap(): MetadataRoute.Sitemap {
  const base = "https://www.prayerkey.com";

  /* ── Static pages ── */
  const staticPages: MetadataRoute.Sitemap = [
    { url: `${base}/`,                        changeFrequency: "weekly",  priority: 1.0 },
    { url: `${base}/pray`,                    changeFrequency: "monthly", priority: 0.95 },
    { url: `${base}/pray/topics`,             changeFrequency: "monthly", priority: 0.9 },
    { url: `${base}/bible`,                   changeFrequency: "monthly", priority: 0.9 },
    { url: `${base}/live`,                    changeFrequency: "monthly", priority: 0.8 },
    { url: `${base}/docs`,                    changeFrequency: "monthly", priority: 0.9 },
    { url: `${base}/about`,                   changeFrequency: "yearly",  priority: 0.5 },
    { url: `${base}/terms`,                   changeFrequency: "yearly",  priority: 0.3 },
    { url: `${base}/privacy`,                 changeFrequency: "yearly",  priority: 0.3 },
    { url: `${base}/author/collins-asein`,    changeFrequency: "monthly", priority: 0.85 },
  ];

  /* ── /prayer hub and one page per prayer ── */
  const prayerPages: MetadataRoute.Sitemap = [
    { url: `${base}/prayer`, changeFrequency: "weekly" as const, priority: 0.95 },
    ...ALL_SLUGS.map((slug) => ({
      url:             `${base}/prayer/${slug}`,
      changeFrequency: "monthly" as const,
      priority:        0.85,
    })),
  ];

  /* ── Pillar guides ── */
  const guidePages: MetadataRoute.Sitemap = [
    { url: `${base}/guides`, changeFrequency: "weekly" as const, priority: 0.9 },
    ...GUIDE_SLUGS.map((slug) => ({
      url:             `${base}/guides/${slug}`,
      changeFrequency: "monthly" as const,
      priority:        0.88,
    })),
  ];

  /* ── Bible book hubs ── */
  const bookPages: MetadataRoute.Sitemap = BIBLE_BOOKS.map((book) => ({
    url:             `${base}/bible/${book.slug}`,
    changeFrequency: "monthly" as const,
    priority:        0.8,
  }));

  /* ── The verse pages we ask Google to index ── */
  const versePages: MetadataRoute.Sitemap = Array.from(INDEXED_VERSES, (ref) => ({
    url:             `${base}/bible/verse/${ref}`,
    changeFrequency: "yearly" as const,
    priority:        0.65,
  }));

  /* ── Verse of the day: today's page, and one page for each day of the year ── */
  const dailyPages: MetadataRoute.Sitemap = [
    { url: `${base}/verse-of-the-day`, lastModified: new Date(), changeFrequency: "daily" as const, priority: 0.95 },
    ...DEVOTIONALS.map((d) => ({ url: `${base}/verse-of-the-day/${d.slug}`, changeFrequency: "yearly" as const, priority: 0.7 })),
  ];

  return [
    ...staticPages,
    ...dailyPages,
    ...prayerPages,
    ...guidePages,
    ...bookPages,
    ...versePages,
  ];
}
