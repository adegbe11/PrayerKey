export const dynamic = "force-dynamic";
export const runtime = "nodejs";

import { NextRequest, NextResponse } from "next/server";
import { GoogleGenerativeAI } from "@google/generative-ai";

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY!);

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
    const model = genAI.getGenerativeModel({
      model: "gemini-2.0-flash",
      systemInstruction: `You are a Bible verse search engine. Given a query — which may be:
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
    });

    const result = await model.generateContent(
      `Query: "${q}"\nTranslation: ${translation}`,
    );

    const raw    = result.response.text().replace(/```json\n?|\n?```/g, "").trim();
    const parsed = JSON.parse(raw) as {
      results: { ref: string; text: string; match: string }[];
    };

    return NextResponse.json({ results: parsed.results ?? [] });
  } catch (err) {
    console.error("[bible/search] Error:", err);
    return NextResponse.json({ results: [] }, { status: 500 });
  }
}
