import { NextResponse } from "next/server";
import { auth } from "@/lib/auth";
import { prisma } from "@/lib/prisma";
import { generatePrayer } from "@/lib/ai/prayer-generation";

export const dynamic = "force-dynamic";

export async function POST(req: Request) {
  try {
    const session = await auth();
    if (!session?.user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const body = await req.json();
    const { userInput, moods = [] } = body as {
      userInput: string;
      moods: string[];
    };

    if (!userInput?.trim()) {
      return NextResponse.json({ error: "Prayer request is required" }, { status: 400 });
    }

    // Generate prayer via Claude
    const generated = await generatePrayer(userInput.trim(), moods);

    // Persist to DB
    const prayer = await prisma.prayer.create({
      data: {
        userId:          session.user.id as string,
        userInput:       userInput.trim(),
        mood:            moods,
        generatedPrayer: generated.prayer,
        verses:          generated.verses as object[],
        encouragement:   generated.encouragement,
      },
    });

    return NextResponse.json({
      id:           prayer.id,
      title:        generated.title,
      prayer:       generated.prayer,
      verses:       generated.verses,
      encouragement: generated.encouragement,
      createdAt:    prayer.createdAt,
    });
  } catch (err) {
    console.error("[api/prayer/generate]", err);
    return NextResponse.json({ error: "Failed to generate prayer" }, { status: 500 });
  }
}
