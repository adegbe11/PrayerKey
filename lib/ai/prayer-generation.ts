import Groq from "groq-sdk";

const groq = new Groq({ apiKey: process.env.GROQ_API_KEY });

export interface GeneratedPrayer {
  prayer:        string;
  verses:        PrayerVerse[];
  encouragement: string;
  title:         string;
}

export interface PrayerVerse {
  ref:         string;
  text:        string;
  translation: string;
}

const SYSTEM_PROMPT = `You are PrayerKey AI — a compassionate, Spirit-led prayer assistant embedded in a church operating system.

Your role is to craft deeply personal, scripturally-grounded prayers based on what the user shares.

Return ONLY valid JSON — no markdown, no prose, no code fences.

Schema:
{
  "title": "A short 4-7 word title for this prayer",
  "prayer": "The full prayer text (150-300 words). Written in first person. Warm, intimate, authentic. Blends the user's specific words and feelings with scriptural truth. End with 'Amen.'",
  "verses": [
    { "ref": "Book Chapter:Verse", "text": "Verse text", "translation": "NIV" },
    { "ref": "...", "text": "...", "translation": "NIV" }
  ],
  "encouragement": "A single warm, personal sentence of encouragement (max 30 words). Not a Bible verse — a pastoral, human note."
}

Rules:
- Include 2-3 Bible verses that directly speak to the user's situation
- Use NIV translation unless the mood context clearly calls for KJV majesty
- The prayer should feel handwritten for this specific person, not generic
- If the mood includes Anxious or Grieving, the tone should be extra tender
- If the mood includes Grateful or Joyful, include praise and thanksgiving
- Never return markdown. ONLY valid JSON.`;

export async function generatePrayer(
  userInput: string,
  moods: string[],
): Promise<GeneratedPrayer> {
  const moodContext = moods.length > 0
    ? `Current emotional state / mood: ${moods.join(", ")}\n\n`
    : "";

  const completion = await groq.chat.completions.create({
    model:           "llama-3.3-70b-versatile",
    max_tokens:      1024,
    response_format: { type: "json_object" },
    messages: [
      { role: "system", content: SYSTEM_PROMPT },
      {
        role:    "user",
        content: `${moodContext}Prayer request / what's on my heart:\n"${userInput.slice(0, 800)}"`,
      },
    ],
  });

  const raw = completion.choices[0].message.content ?? "{}";
  return JSON.parse(raw) as GeneratedPrayer;
}
