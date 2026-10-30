export const dynamic = "force-dynamic";
export const runtime = "nodejs";

import { NextRequest, NextResponse } from "next/server";
import { KEY_RE, ROOM_RE, readOnAir, relayReady, writeOnAir } from "@/lib/live/relay";

/**
 * GET  /api/live/onair?room=K7Q2XP          → { verse, seq } for the stream overlay
 * POST /api/live/onair { room, key, verse }  → the live page sets what is on air
 */
const NO_STORE = { "Cache-Control": "no-store, max-age=0" };

export async function GET(req: NextRequest) {
  const room = req.nextUrl.searchParams.get("room")?.toUpperCase() ?? "";
  if (!ROOM_RE.test(room)) return NextResponse.json({ error: "room" }, { status: 400, headers: NO_STORE });
  if (!relayReady) return NextResponse.json({ error: "relay-not-set-up" }, { status: 503, headers: NO_STORE });
  try {
    const onAir = await readOnAir(room);
    return NextResponse.json(onAir ?? { verse: null, seq: 0, at: 0 }, { headers: NO_STORE });
  } catch (err) {
    console.error("[live/onair] read", err);
    return NextResponse.json({ error: "relay" }, { status: 502, headers: NO_STORE });
  }
}

export async function POST(req: NextRequest) {
  if (!relayReady) return NextResponse.json({ error: "relay-not-set-up" }, { status: 503 });
  try {
    const body = (await req.json()) as { room?: string; key?: string; verse?: { verseRef?: unknown; verseText?: unknown; translation?: unknown } | null };
    const room = (body.room ?? "").toUpperCase();
    if (!ROOM_RE.test(room) || !KEY_RE.test(body.key ?? "")) return NextResponse.json({ error: "room" }, { status: 400 });
    const v = body.verse;
    const verse = v && typeof v.verseRef === "string" && typeof v.verseText === "string"
      ? {
          verseRef: v.verseRef.slice(0, 80),
          verseText: v.verseText.slice(0, 1200),
          translation: typeof v.translation === "string" ? v.translation.slice(0, 12) : "KJV",
        }
      : null;
    const result = await writeOnAir(room, body.key!, verse);
    if (result === "forbidden") return NextResponse.json({ error: "forbidden" }, { status: 403 });
    return NextResponse.json({ ok: true });
  } catch (err) {
    console.error("[live/onair] write", err);
    return NextResponse.json({ error: "relay" }, { status: 502 });
  }
}
