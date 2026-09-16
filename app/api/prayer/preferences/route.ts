import { z } from "zod";
import { prayerBody, prayerData, prayerError, prayerSession } from "@/lib/prayer-engine/supabase";
export const dynamic = "force-dynamic";
export const runtime = "nodejs";
const preferences = z.object({
  goals: z.array(z.enum(["pray", "read", "fast", "grow", "church", "remember"])).max(3),
  topics: z.array(z.enum(["family", "health", "peace", "guidance", "work", "relationships", "gratitude", "church"])).max(8),
  reminder_enabled: z.boolean(), reminder_hour: z.number().int().min(0).max(23), reminder_minute: z.number().int().min(0).max(59),
  timezone: z.string().max(100).refine(value => { try { new Intl.DateTimeFormat("en", { timeZone: value }); return true; } catch { return false; } }),
}).strict();
export async function GET(req: Request) {
  try { const session = await prayerSession(req); return Response.json({ data: await prayerData(session, `pk_preferences?user_id=eq.${session.userId}`) }); }
  catch (error) { return prayerError(error); }
}
export async function POST(req: Request) {
  try {
    const session = await prayerSession(req);
    const body = preferences.parse(await prayerBody(req));
    // Atomic SQL upsert through a security-invoker function; no elevated server key.
    return Response.json({ data: await prayerData(session, "rpc/pk_save_preferences", "POST", { p_preferences: body }) });
  } catch (error) { return prayerError(error); }
}
