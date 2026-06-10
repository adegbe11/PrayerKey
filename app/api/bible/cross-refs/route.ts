export const dynamic = "force-dynamic";
export const runtime = "nodejs";

import { NextRequest, NextResponse } from "next/server";
import { getCrossRefs, parseQueryRef } from "@/lib/bible/kjv";
import { BIBLE_BOOKS } from "@/lib/seo/bible-books";

/**
 * GET /api/bible/cross-refs?ref=<verseRef>
 * Cross-references computed from the bundled KJV by shared key themes.
 * No AI API — free, instant, deterministic.
 */
export async function GET(req: NextRequest) {
  const ref = req.nextUrl.searchParams.get("ref")?.trim() ?? "";
  if (!ref) return NextResponse.json({ refs: [] });

  try {
    // Resolve "John 3:16" → book + chapter + verse via the shared parser
    const hits = parseQueryRef(ref);
    if (!hits?.length) return NextResponse.json({ refs: [] });

    const m = hits[0].ref.match(/^(.*)\s+(\d+):(\d+)$/);
    if (!m) return NextResponse.json({ refs: [] });
    const book = BIBLE_BOOKS.find((b) => b.name === m[1]);
    if (!book) return NextResponse.json({ refs: [] });

    const refs = getCrossRefs(book.slug, parseInt(m[2], 10), parseInt(m[3], 10), 5);
    return NextResponse.json({ refs });
  } catch (err) {
    console.error("[bible/cross-refs] Error:", err);
    return NextResponse.json({ refs: [] }, { status: 500 });
  }
}
