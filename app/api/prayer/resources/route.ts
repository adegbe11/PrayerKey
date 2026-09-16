import { z } from "zod";
import { matterId } from "@/lib/prayer-engine/contracts";
import { prayerBody, prayerData, prayerError, prayerSession } from "@/lib/prayer-engine/supabase";
export const dynamic = "force-dynamic";
export const runtime = "nodejs";
const body = z.discriminatedUnion("type", [
 z.object({ type: z.literal("person"), id: matterId, name: z.string().trim().min(1).max(200), relationship: z.string().max(200).default("") }).strict(),
 z.object({ type: z.literal("sermon"), id: matterId, title: z.string().trim().min(1).max(300), notes: z.string().max(30000), preached_at: z.string().datetime({ offset: true }).optional() }).strict(),
 z.object({ type: z.literal("routine"), id: matterId, kind: z.enum(["fast", "challenge"]), title: z.string().trim().min(1).max(300), starts_on: z.string().date(), ends_on: z.string().date(), status: z.enum(["planned", "active", "completed", "stopped"]).default("planned") }).strict(),
]);
const tables = { person: "pk_people", sermon: "pk_sermons", routine: "pk_routines" } as const;
export async function POST(req: Request) {
 try {
  const session = await prayerSession(req);
  const input = body.parse(await prayerBody(req));
  if (input.type === "routine" && input.ends_on < input.starts_on) return Response.json({ error: "End date must follow start date." }, { status: 400 });
  const { type, ...data } = input;
  return Response.json({ data: await prayerData(session, tables[type], "POST", { ...data, user_id: session.userId }) }, { status: 201 });
 } catch (error) { return prayerError(error); }
}
export async function GET(req: Request) {
 try {
  const session = await prayerSession(req);
  const type = z.enum(["person", "sermon", "routine"]).parse(new URL(req.url).searchParams.get("type"));
  return Response.json({ data: await prayerData(session, `${tables[type]}?user_id=eq.${session.userId}&order=created_at.desc&limit=100`), limit: 100 });
 } catch (error) { return prayerError(error); }
}
