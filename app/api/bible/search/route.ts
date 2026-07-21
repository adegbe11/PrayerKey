export const dynamic = "force-dynamic";
export const runtime = "nodejs";

import { NextRequest, NextResponse } from "next/server";
import { searchBible } from "@/lib/bible/kjv";
import { semanticVerseSearch } from "@/lib/ai/bible-embeddings";

/**
 * GET /api/bible/search?q=<query>
 * Bible verse search over the bundled public-domain KJV (31,100 verses).
 * Matches by reference ("John 3:16", "Psalm 23", "1 cor 13:4-7"),
 * keyword, or quoted phrase. No AI API — free, instant, works offline.
 */
export async function GET(req: NextRequest) {
  const q = req.nextUrl.searchParams.get("q")?.trim() ?? "";
  const translation = (req.nextUrl.searchParams.get("translation")?.trim() || "KJV").toUpperCase();
  if (!q) return NextResponse.json({ results: [] });

  try {
    if (translation !== "KJV") {
      try {
        const matches = await semanticVerseSearch(q, 10, translation);
        if (matches.length) {
          return NextResponse.json({
            results: matches.map((match) => ({ ref: match.verseRef, text: match.verseText, translation: match.translation })),
          });
        }
      } catch (semErr) {
        // Embeddings service unavailable (no key / network) — fall through to KJV
        console.error("[bible/search] semantic fallback to KJV:", semErr);
      }
    }
    const results = searchBible(q, 5);
    return NextResponse.json({ results });
  } catch (err) {
    console.error("[bible/search] Error:", err);
    return NextResponse.json({ results: [] }, { status: 500 });
  }
}
