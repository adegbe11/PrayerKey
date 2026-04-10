export const dynamic = "force-dynamic";

import { NextRequest, NextResponse } from "next/server";
import Groq from "groq-sdk";

const groq = new Groq({ apiKey: process.env.GROQ_API_KEY });

/**
 * GET /api/bible/cross-refs?ref=<verseRef>&translation=<NIV|KJV|...>
 * Returns 4–6 cross-referenced verses for the given verse — thematically
 * or textually related, as Rhema's 340k cross-reference database does.
 */
export async function GET(req: NextRequest) {
  const ref         = req.nextUrl.searchParams.get("ref")?.trim() ?? "";
  const translation = req.nextUrl.searchParams.get("translation") ?? "NIV";

  if (!ref) return NextResponse.json({ refs: [] });

  try {
    const completion = await groq.chat.completions.create({
      model:           "llama-3.3-70b-versatile",
      max_tokens:      600,
      response_format: { type: "json_object" },
      messages: [
        {
          role:    "system",
          content: `You are a Bible cross-reference system with access to a comprehensive cross-reference database (similar to OpenBible.info's 340,000 cross-references).

Given a Bible verse reference, return 4–6 verses that are thematically, verbally, or doctrinally linked to it. Prioritise strong, well-known cross-references over obscure ones.

Return ONLY valid JSON — no markdown, no prose:
{
  "refs": [
    {
      "ref":    "Romans 8:28",
      "text":   "And we know that in all things God works for the good of those who love him...",
      "reason": "Shares theme of God's sovereignty and purpose"
    }
  ]
}

Always use the ${translation} translation for verse text.`,
        },
        {
          role:    "user",
          content: `Verse: ${ref}\nTranslation: ${translation}`,
        },
      ],
    });

    const raw    = completion.choices[0].message.content ?? '{"refs":[]}';
    const parsed = JSON.parse(raw) as {
      refs: { ref: string; text: string; reason: string }[];
    };

    return NextResponse.json({ refs: parsed.refs ?? [] });
  } catch (err) {
    console.error("[bible/cross-refs] Error:", err);
    return NextResponse.json({ refs: [] }, { status: 500 });
  }
}
