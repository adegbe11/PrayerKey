import { createMatter } from "@/lib/prayer-engine/contracts";
import { prayerBody, prayerData, prayerError, prayerSession } from "@/lib/prayer-engine/supabase";
export const dynamic = "force-dynamic";
export const runtime = "nodejs";

export async function GET(req: Request) {
  try {
    const session = await prayerSession(req);
    const raw = new URL(req.url).searchParams.get("offset") || "0";
    if (!/^\d{1,7}$/.test(raw)) return Response.json({ error: "Invalid page offset." }, { status: 400 });
    const data = await prayerData(session, `pk_matters?user_id=eq.${session.userId}&order=created_at.desc,id.desc&limit=50&offset=${Number(raw)}`);
    return Response.json({ data, limit: 50, offset: Number(raw) });
  } catch (error) { return prayerError(error); }
}
export async function POST(req: Request) {
  try {
    const session = await prayerSession(req);
    const input = createMatter.parse(await prayerBody(req));
    const data = await prayerData(session, "pk_matters", "POST", { ...input, user_id: session.userId });
    return Response.json({ data }, { status: 201 });
  } catch (error) { return prayerError(error); }
}
