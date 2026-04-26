export const dynamic = "force-dynamic";
export const runtime = "nodejs";

import { NextRequest, NextResponse } from "next/server";
import OpenAI from "openai";

const groq = new OpenAI({
  apiKey:  process.env.GROQ_API_KEY,
  baseURL: "https://api.groq.com/openai/v1",
});

/**
 * GET /api/bible/search?q=<query>&translation=<NIV|KJV|...>
 * Fuzzy Bible verse search — matches by reference, keyword, or theme.
 * Returns up to 5 results.
 */
export async function GET(req: NextRequest) {
  const q           = req.nextUrl.searchParams.get("q")?.trim() ?? "";
  const translation = req.nextUrl.searchParams.get("translation") ?? "NIV";

  if (!q) return NextResponse.json({ results: [] });

  try {
    const completion = await groq.chat.completions.create({
      model:           "llama-3.1-8b-instant",
      max_tokens:      300,
      response_format: { type: "json_object" },
      messages: [
        {
          role:    "system",
          content: `You are a Bible verse search engine. Given a query — which may be:
- A precise reference like "John 3:16" or "Ps 23"
- A partial reference like "john 3" (return all verses in that chapter, up to 5)
- A keyword or topic like "do not fear", "faith", "healing"
- A paraphrase or theme

Return up to 5 matching Bible verses in the ${translation} translation.

Return ONLY valid JSON — no markdown, no prose:
{
  "results": [
    {
      "ref":   "John 3:16",
      "text":  "For God so loved the world...",
      "match": "direct"
    }
  ]
}

"match" values: "direct" for exact references, "semantic" for topic/keyword matches.
Return an empty results array if nothing relevant found.`,
        },
        {
          role:    "user",
          content: `Query: "${q}"\nTranslation: ${translation}`,
        },
      ],
    });

    const raw    = completion.choices[0].message.content ?? '{"results":[]}';
    const parsed = JSON.parse(raw) as {
      results: { ref: string; text: string; match: string }[];
    };

    return NextResponse.json({ results: parsed.results ?? [] });
  } catch (err) {
    console.error("[bible/search] Error:", err);
    return NextResponse.json({ results: [] }, { status: 500 });
  }
}
