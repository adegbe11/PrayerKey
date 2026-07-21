import { NextResponse } from "next/server";
import { composePrayer } from "@/lib/prayer-engine/compose";

export const dynamic = "force-dynamic";
export const runtime = "nodejs";
export const maxDuration = 30;

/**
 * POST /api/prayer/generate  { userInput, moods?: string[] }
 * Composes a full scripture-grounded prayer using the free on-server
 * prayer engine (25 keyword-scored categories + KJV verses).
 * No AI API, no key, no rate limits — powers the Manna "Pray for me" card.
 */
export async function POST(req: Request) {
  try {
    const body = await req.json();
    const { userInput, moods = [] } = body as {
      userInput: string;
      moods: string[];
    };

    if (!userInput?.trim()) {
      return NextResponse.json({ error: "Prayer request is required" }, { status: 400 });
    }

    // Fold moods into the input so the keyword engine can score them.
    const moodText = moods.length ? ` I am feeling ${moods.join(" and ").toLowerCase()}.` : "";
    const result   = composePrayer(userInput.trim() + moodText);

    return NextResponse.json({
      id:            crypto.randomUUID(),
      title:         `A Prayer for ${result.topics.join(" & ")}`,
      prayer:        result.prayer + "\n\nAmen.",
      verses:        result.verses,
      encouragement: "Composed from scripture-based prayer patterns. Pray it aloud and make it your own.",
      createdAt:     new Date(),
    });
  } catch (err) {
    const msg = err instanceof Error ? err.message : String(err);
    console.error("[api/prayer/generate]", msg);
    return NextResponse.json({ error: msg || "Failed to generate prayer" }, { status: 500 });
  }
}
