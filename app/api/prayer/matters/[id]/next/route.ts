import { matterId } from "@/lib/prayer-engine/contracts";
import { MatterContext, MatterStage, nextPrayerAction } from "@/lib/prayer-engine/next-action";
import { PrayerBackendError, prayerData, prayerError, prayerSession } from "@/lib/prayer-engine/supabase";
export const dynamic = "force-dynamic";
export const runtime = "nodejs";
export async function GET(req: Request, { params }: { params: { id: string } }) {
 try {
  const session = await prayerSession(req);
  const id = matterId.parse(params.id);
  const owner = `user_id=eq.${session.userId}`;
  const [matter] = await prayerData<MatterContext[]>(session, `pk_matters?id=eq.${id}&${owner}`);
  if (!matter) throw new PrayerBackendError(404, "Prayer not found.");
  const [scriptures, sessions, routines, memories] = await Promise.all([
   prayerData<{ pk_scriptures: { reference: string; text: string; translation: "KJV" } }[]>(session, `pk_matter_scriptures?matter_id=eq.${id}&${owner}&select=pk_scriptures(reference,text,translation)&limit=100`),
   prayerData<{ id: string }[]>(session, `pk_sessions?matter_id=eq.${id}&${owner}&completed_at=not.is.null&select=id&limit=1`),
   prayerData<{ pk_routines: { id: string; kind: "fast" | "challenge"; status: string } }[]>(session, `pk_matter_routines?matter_id=eq.${id}&${owner}&select=pk_routines(id,kind,status)&limit=100`),
   prayerData<{ id: string; title: string; topics: string[]; stage: MatterStage; pk_updates: { body: string; kind: string }[] }[]>(session, `pk_matters?${owner}&stage=in.(answered,answered_differently)&select=id,title,topics,stage,pk_updates(body,kind)&order=updated_at.desc&limit=100`),
  ]);
  return Response.json({ data: nextPrayerAction({
   matter, scriptures: scriptures.map(item => item.pk_scriptures).filter(Boolean), completedSessions: sessions.length,
   activeRoutines: routines.map(item => item.pk_routines).filter(item => item?.status === "active"),
   memories: memories.map(item => ({ ...item, testimony: item.pk_updates.find(update => update.kind === "testimony")?.body || "" })),
  }), memorySearchLimit: 100 });
 } catch (error) { return prayerError(error); }
}
