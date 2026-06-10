export const dynamic = "force-dynamic";
export const runtime = "nodejs";

import { NextRequest, NextResponse } from "next/server";
import { searchBible } from "@/lib/bible/kjv";

/**
 * GET /api/bible/search?q=<query>
 * Bible verse search over the bundled public-domain KJV (31,100 verses).
 * Matches by reference ("John 3:16", "Psalm 23", "1 cor 13:4-7"),
 * keyword, or quoted phrase. No AI API — free, instant, works offline.
 */
export async function GET(req: NextRequest) {
  const q = req.nextUrl.searchParams.get("q")?.trim() ?? "";
  if (!q) return NextResponse.json({ results: [] });

  try {
    const results = searchBible(q, 5);
    return NextResponse.json({ results });
  } catch (err) {
    console.error("[bible/search] Error:", err);
    return NextResponse.json({ results: [] }, { status: 500 });
  }
}
