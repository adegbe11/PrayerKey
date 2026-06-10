export const dynamic = "force-dynamic";
export const runtime = "nodejs";

import { NextRequest, NextResponse } from "next/server";
import { detectVerseInTranscript } from "@/lib/bible/kjv";

/**
 * POST /api/detect-verses  { transcript: string }
 * Live-sermon verse detection over the bundled KJV — no AI API.
 * Detects spoken references ("John chapter 3 verse 16") and quoted
 * scripture (consecutive word-sequence matching), confidence-gated so
 * ordinary preaching never triggers a false verse on the projector.
 */
export async function POST(req: NextRequest) {
  try {
    const { transcript } = (await req.json()) as { transcript?: string };
    if (!transcript || transcript.trim().length < 10) {
      return NextResponse.json({ detected: false, ref: "", text: "", confidence: 0 });
    }
    const result = detectVerseInTranscript(transcript.slice(-500));
    return NextResponse.json(result);
  } catch (err) {
    console.error("[detect-verses] Error:", err);
    return NextResponse.json({ detected: false, ref: "", text: "", confidence: 0 }, { status: 500 });
  }
}
