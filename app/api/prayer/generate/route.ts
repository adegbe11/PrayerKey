import { NextResponse } from "next/server";
import { generatePrayer } from "@/lib/ai/prayer-generation";

export const dynamic = "force-dynamic";

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

    const generated = await generatePrayer(userInput.trim(), moods);

    return NextResponse.json({
      id:            crypto.randomUUID(),
      title:         generated.title,
      prayer:        generated.prayer,
      verses:        generated.verses,
      encouragement: generated.encouragement,
      createdAt:     new Date(),
    });
  } catch (err) {
    console.error("[api/prayer/generate]", err);
    return NextResponse.json({ error: "Failed to generate prayer" }, { status: 500 });
  }
}
