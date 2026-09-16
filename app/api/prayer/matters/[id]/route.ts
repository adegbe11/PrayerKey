import { appendMatter, matterId, reviseMatter } from "@/lib/prayer-engine/contracts";
import { PrayerBackendError, prayerBody, prayerData, prayerError, prayerSession } from "@/lib/prayer-engine/supabase";
export const dynamic = "force-dynamic";
export const runtime = "nodejs";
type Route = { params: { id: string } };

export async function GET(req: Request, { params }: Route) {
  try {
    const session = await prayerSession(req);
    const id = matterId.parse(params.id);
    const rows = await prayerData<unknown[]>(session, `pk_matters?id=eq.${id}&user_id=eq.${session.userId}`);
    if (!rows.length) throw new PrayerBackendError(404, "Prayer not found.");
    const tables = ["pk_sessions", "pk_journal_entries", "pk_prayer_points", "pk_updates", "pk_matter_scriptures", "pk_matter_people", "pk_matter_sermons", "pk_matter_routines"];
    const history = await Promise.all(tables.map(table => prayerData(session, `${table}?matter_id=eq.${id}&user_id=eq.${session.userId}&order=created_at.desc&limit=100`)));
    return Response.json({ data: rows[0], history: Object.fromEntries(tables.map((table, index) => [table, history[index]])), historyLimitPerType: 100 });
  } catch (error) { return prayerError(error); }
}
export async function PATCH(req: Request, { params }: Route) {
  try {
    const session = await prayerSession(req);
    const id = matterId.parse(params.id);
    const { version, ...input } = reviseMatter.parse(await prayerBody(req));
    const rows = await prayerData<unknown[]>(session, `pk_matters?id=eq.${id}&user_id=eq.${session.userId}&version=eq.${version}`, "PATCH", input);
    if (!rows.length) throw new PrayerBackendError(409, "This prayer changed. Reload before saving.");
    return Response.json({ data: rows[0] });
  } catch (error) { return prayerError(error); }
}
export async function POST(req: Request, { params }: Route) {
  try {
    const session = await prayerSession(req);
    const id = matterId.parse(params.id);
    const input = appendMatter.parse(await prayerBody(req));
    const owner = { user_id: session.userId, matter_id: id };
    let table: string;
    let body: unknown;
    switch (input.kind) {
      case "outcome": table = "rpc/pk_record_outcome"; body = { p_matter_id: id, p_version: input.version, p_stage: input.stage, p_testimony: input.testimony }; break;
      case "session": { const { kind, ...rest } = input; table = "pk_sessions"; body = { ...rest, ...owner }; break; }
      case "journal": table = "pk_journal_entries"; body = { ...owner, id: input.id, body: input.body }; break;
      case "point": table = "pk_prayer_points"; body = { ...owner, id: input.id, body: input.body }; break;
      case "update": table = "pk_updates"; body = { ...owner, id: input.id, kind: "reflection", body: input.body }; break;
      case "scripture": table = "pk_matter_scriptures"; body = { ...owner, reference: input.reference }; break;
      case "person": table = "pk_matter_people"; body = { ...owner, target_id: input.target_id }; break;
      case "sermon": table = "pk_matter_sermons"; body = { ...owner, target_id: input.target_id }; break;
      case "routine": table = "pk_matter_routines"; body = { ...owner, target_id: input.target_id }; break;
    }
    const data = await prayerData(session, table, "POST", body);
    return Response.json({ data }, { status: 201 });
  } catch (error) { return prayerError(error); }
}
