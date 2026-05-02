import { GoogleGenerativeAI } from "@google/generative-ai";
import type { VerseDetectionResult } from "@/types/sermon";

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY!);

const SYSTEM_PROMPT = `You are a Bible verse detection AI embedded in a live church sermon system.

Analyse the following sermon transcript snippet. Detect any Bible verse being:
- Directly quoted (exact or near-exact words)
- Referenced by name (e.g. "John 3:16", "the 23rd Psalm")
- Paraphrased or alluded to (even indirectly)

Return ONLY valid JSON — no markdown, no explanation, no code fences.

Schema:
{
  "detected": boolean,
  "verseRef": "Book Chapter:Verse" or "",
  "verseText": "The verse text as it appears in the detected translation" or "",
  "translation": "NIV" | "KJV" | "ESV" | "NLT" | "NKJV",
  "confidence": number between 0 and 1,
  "snippetUsed": "the exact phrase that triggered detection"
}

Rules:
- confidence >= 0.90 → direct quote
- confidence 0.75–0.89 → strong reference
- confidence 0.60–0.74 → paraphrase / allusion
- confidence < 0.60 → do not detect (return detected: false)
- If no verse is clearly referenced, return {"detected": false, "verseRef": "", "verseText": "", "translation": "NIV", "confidence": 0, "snippetUsed": ""}
- ONLY return valid JSON. Never return markdown or prose.`;

export async function detectVerseFromText(
  transcript: string,
  translation: string = "NIV"
): Promise<VerseDetectionResult> {
  const start = Date.now();

  try {
    const model = genAI.getGenerativeModel({
      model: "gemini-2.0-flash",
      systemInstruction: SYSTEM_PROMPT,
    });

    const result = await model.generateContent(
      `Translation preference: ${translation}\n\nTranscript snippet:\n"${transcript.slice(0, 600)}"`,
    );

    const detectionMs = Date.now() - start;
    const raw = result.response.text().replace(/```json\n?|\n?```/g, "").trim();

    const parsed = JSON.parse(raw) as {
      detected:    boolean;
      verseRef:    string;
      verseText:   string;
      translation: string;
      confidence:  number;
      snippetUsed: string;
    };

    if (!parsed.detected || parsed.confidence < 0.60) {
      return {
        detected: false, verseRef: "", verseText: "",
        translation, confidence: 0, snippetUsed: "", detectionMs,
      };
    }

    return { ...parsed, detectionMs };
  } catch (err) {
    console.error("[verse-detection] Gemini error:", err);
    return {
      detected: false, verseRef: "", verseText: "",
      translation, confidence: 0, snippetUsed: "",
      detectionMs: Date.now() - start,
    };
  }
}
