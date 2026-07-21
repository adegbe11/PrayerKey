import { NextRequest, NextResponse } from "next/server";
import { PRAYER_TOPICS } from "@/lib/seo/prayer-topics";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";

/** Mobile prayer deck catalog. Content is public and requires no account. */
export async function GET(req: NextRequest) {
  const category = req.nextUrl.searchParams.get("category")?.trim().toLowerCase();
  const query = req.nextUrl.searchParams.get("q")?.trim().toLowerCase();

  const topics = PRAYER_TOPICS.filter((topic) => {
    if (category && topic.category.toLowerCase() !== category) return false;
    if (!query) return true;
    return [topic.title, topic.category, topic.metaDesc, ...topic.keywords]
      .some((value) => value.toLowerCase().includes(query));
  }).map(({ slug, title, category: topicCategory, samplePrayer, scripture, prayerPoints, howToPray }) => ({
    slug,
    title,
    category: topicCategory,
    prayer: samplePrayer,
    scripture,
    prayerPoints: prayerPoints ?? [],
    guide: howToPray ?? [],
  }));

  return NextResponse.json({
    topics,
    categories: [...new Set(PRAYER_TOPICS.map((topic) => topic.category))].sort(),
    total: topics.length,
  });
}
