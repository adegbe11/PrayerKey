import { Pinecone } from "@pinecone-database/pinecone";
import OpenAI from "openai";

const INDEX_NAME = "prayerkey-bible";

let pinecone: Pinecone | null = null;
let openai: OpenAI | null = null;

function getPinecone() {
  if (!pinecone) {
    pinecone = new Pinecone({ apiKey: process.env.PINECONE_API_KEY ?? "" });
  }
  return pinecone;
}

function getOpenAI() {
  if (!openai) {
    openai = new OpenAI({ apiKey: process.env.OPENAI_API_KEY ?? "" });
  }
  return openai;
}

export interface BibleVerseMatch {
  verseRef:    string;
  verseText:   string;
  translation: string;
  score:       number;
}

/**
 * Semantic Bible verse search using OpenAI embeddings + Pinecone.
 * Used as fallback when Claude confidence is 0.60–0.74.
 */
export async function semanticVerseSearch(
  query: string,
  topK: number = 3,
  translation: string = "NIV"
): Promise<BibleVerseMatch[]> {
  try {
    const ai    = getOpenAI();
    const pc    = getPinecone();
    const index = pc.index(INDEX_NAME);

    // Generate embedding for the query text
    const embeddingRes = await ai.embeddings.create({
      model: "text-embedding-3-small",
      input: query.slice(0, 512),
    });

    const vector = embeddingRes.data[0].embedding;

    // Query Pinecone
    const results = await index.query({
      vector,
      topK,
      includeMetadata: true,
      filter: { translation: { $eq: translation } },
    });

    return (results.matches ?? []).map((m) => ({
      verseRef:    (m.metadata?.verseRef  as string) ?? "",
      verseText:   (m.metadata?.verseText as string) ?? "",
      translation: (m.metadata?.translation as string) ?? translation,
      score:       m.score ?? 0,
    }));
  } catch (err) {
    console.error("[bible-embeddings] Pinecone error:", err);
    return [];
  }
}

/**
 * One-time script to embed all 31,102 Bible verses into Pinecone.
 * Run: ts-node scripts/init-embeddings.ts
 * Estimated runtime: ~45 minutes.
 */
export async function upsertVerseBatch(
  verses: Array<{ id: string; ref: string; text: string; translation: string }>
) {
  const ai    = getOpenAI();
  const pc    = getPinecone();
  const index = pc.index(INDEX_NAME);

  const inputs  = verses.map((v) => `${v.ref}: ${v.text}`);
  const embedRes = await ai.embeddings.create({
    model: "text-embedding-3-small",
    input: inputs,
  });

  const vectors = verses.map((v, i) => ({
    id:       v.id,
    values:   embedRes.data[i].embedding,
    metadata: {
      verseRef:    v.ref,
      verseText:   v.text,
      translation: v.translation,
    },
  }));

  await index.upsert({ records: vectors });
}
