import assert from "node:assert/strict";
import { POST as create, GET as list } from "../app/api/prayer/matters/route";
import { PATCH as revise, POST as append } from "../app/api/prayer/matters/[id]/route";
import { nextPrayerAction, PrayerContext } from "../lib/prayer-engine/next-action";
import { prayerBody, PrayerBackendError } from "../lib/prayer-engine/supabase";
const user = "11111111-1111-4111-8111-111111111111";
const id = "22222222-2222-4222-8222-222222222222";
const request = (body: unknown, authenticated = true, method = "POST") => new Request("https://example.test/api/prayer/matters", {
 method, headers: authenticated ? { authorization: "Bearer test-token" } : {}, body: method === "GET" ? undefined : JSON.stringify(body),
});
async function main() {
 const originalFetch = global.fetch;
 const oldUrl = process.env.NEXT_PUBLIC_SUPABASE_URL;
 const oldKey = process.env.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY;
 let calls: { url: string; init?: RequestInit }[] = [];
 let databaseStatus = 200;
 let databaseBody: unknown = [];
 let authStatus = 200;
 global.fetch = async (url, init) => {
  calls.push({ url: String(url), init });
  return String(url).includes("/auth/v1/user")
   ? Response.json({ id: user }, { status: authStatus })
   : Response.json(databaseBody, { status: databaseStatus });
 };
 try {
  delete process.env.NEXT_PUBLIC_SUPABASE_URL;
  delete process.env.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY;
  assert.equal((await create(request({ id, title: "Family", intention: "My daughter" }))).status, 503);
  assert.equal(calls.length, 0);
  process.env.NEXT_PUBLIC_SUPABASE_URL = "https://example.supabase.co";
  process.env.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY = "sb_publishable_test";
  assert.equal((await create(request({}, false))).status, 401);
  assert.equal(calls.length, 0);
  assert.equal((await create(request({ id, title: "Family", intention: "My daughter", user_id: id }))).status, 400);
  assert.equal(calls.length, 1); // Auth only: rejected client-supplied ownership.
  calls = [];
  databaseBody = [{ id, user_id: user }];
  assert.equal((await create(request({ id, title: "Family", intention: "My daughter" }))).status, 201);
  assert.equal(JSON.parse(String(calls[1].init?.body)).user_id, user);
  assert.equal((calls[1].init?.headers as Record<string,string>).Authorization, "Bearer test-token");
  assert.equal(calls[1].init?.redirect, "error");
  databaseStatus = 409;
  assert.equal((await create(request({ id, title: "Family", intention: "My daughter" }))).status, 409);
  databaseStatus = 200; databaseBody = [];
  assert.equal((await revise(request({ version: 1, stage: "waiting" }, true, "PATCH"), { params: { id } })).status, 409);
  assert(calls.at(-1)?.url.includes("version=eq.1"));
  assert.equal((await revise(request({ version: 1, stage: "answered" }, true, "PATCH"), { params: { id } })).status, 400);
  assert.equal((await append(request({ kind: "outcome", version: 1, stage: "answered", testimony: "" }), { params: { id } })).status, 400);
  assert.equal((await append(request({ kind: "scripture", reference: "Psalm 23:1", text: "Invented verse" }), { params: { id } })).status, 400);
  databaseStatus = 500; databaseBody = { code: "40001", message: "Internal SQL details" };
  assert.equal((await append(request({ kind: "outcome", version: 1, stage: "answered", testimony: "A recorded answer" }), { params: { id } })).status, 409);
  databaseStatus = 500; databaseBody = { error: "private database information" };
  const failure = await list(request(null, true, "GET"));
  assert.equal(failure.status, 503);
  assert(!JSON.stringify(await failure.json()).includes("private database"));
  authStatus = 401;
  calls = [];
  assert.equal((await list(request(null, true, "GET"))).status, 401);
  assert.equal(calls.length, 1);
  process.env.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY = "sb_secret_not_for_clients";
  calls = [];
  assert.equal((await list(request(null, true, "GET"))).status, 503);
  assert.equal(calls.length, 0);
  await assert.rejects(() => prayerBody(request({ text: "x".repeat(70000) })), error => error instanceof PrayerBackendError && error.status === 413);
  const context: PrayerContext = {
   matter: { id, intention: "My daughter", topics: ["family"], stage: "praying", next_action: "" },
   scriptures: [], completedSessions: 2, activeRoutines: [],
   memories: [
    { id: "old", title: "Last year", topics: ["family"], stage: "answered", testimony: "She settled into school." },
    { id: "not-answered", title: "Still waiting", topics: ["family"], stage: "waiting", testimony: "No update." },
    { id: "empty", title: "No account", topics: ["family"], stage: "answered", testimony: "" },
   ],
  };
  assert.equal(nextPrayerAction(context).action.label, "Keep praying");
  assert.deepEqual(nextPrayerAction(context).memories.map(x => x.matterId), ["old"]);
  assert.equal(nextPrayerAction({ ...context, matter: { ...context.matter, next_action: "Call my daughter" } }).action.kind, "action");
  assert.equal(nextPrayerAction({ ...context, matter: { ...context.matter, stage: "released" } }).action.kind, "reflect");
  assert.equal(nextPrayerAction({ ...context, matter: { ...context.matter, stage: "answered" } }).action.kind, "remember");
  console.log("PASS: graph API auth, ownership, validation, conflicts, error privacy, body limits, and deterministic next actions (mock backend).");
 } finally {
  global.fetch = originalFetch;
  if (oldUrl === undefined) delete process.env.NEXT_PUBLIC_SUPABASE_URL; else process.env.NEXT_PUBLIC_SUPABASE_URL = oldUrl;
  if (oldKey === undefined) delete process.env.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY; else process.env.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY = oldKey;
 }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
