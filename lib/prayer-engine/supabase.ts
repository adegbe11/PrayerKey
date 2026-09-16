import { z } from "zod";

export class PrayerBackendError extends Error {
  constructor(public status: number, message: string) { super(message); }
}
export type SupabaseSession = { userId: string; token: string; url: string; key: string };

function configuration() {
  const url = process.env.NEXT_PUBLIC_SUPABASE_URL;
  const key = process.env.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY;
  if (!url || !key) throw new PrayerBackendError(503, "Prayer sync is not configured yet.");
  // Configuration guard only, not JWT authentication: legacy public anon keys are allowed.
  let publicKey = key.startsWith("sb_publishable_");
  if (!publicKey) {
    try { publicKey = JSON.parse(Buffer.from(key.split(".")[1] || "", "base64url").toString()).role === "anon"; }
    catch { publicKey = false; }
  }
  if (!publicKey) throw new PrayerBackendError(503, "Prayer sync requires a publishable key.");
  let parsed: URL;
  try { parsed = new URL(url); } catch { throw new PrayerBackendError(503, "Prayer sync is not configured yet."); }
  if (parsed.protocol !== "https:" || parsed.username || parsed.password || parsed.search || parsed.hash || parsed.pathname !== "/") {
    throw new PrayerBackendError(503, "Prayer sync configuration is invalid.");
  }
  return { url: parsed.origin, key };
}

/** Validate with Supabase Auth; never decode an unverified JWT or use a service-role key. */
export async function prayerSession(req: Request): Promise<SupabaseSession> {
  const header = req.headers.get("authorization") || "";
  if (!/^Bearer [^\s]+$/.test(header)) throw new PrayerBackendError(401, "Sign in to sync your prayers.");
  const { url, key } = configuration();
  const token = header.slice(7);
  let response: Response;
  try {
    response = await fetch(`${url}/auth/v1/user`, {
      headers: { apikey: key, Authorization: header }, cache: "no-store", redirect: "error", signal: AbortSignal.timeout(10000),
    });
  } catch { throw new PrayerBackendError(503, "Prayer sync is temporarily unavailable."); }
  if (!response.ok) throw new PrayerBackendError(response.status >= 500 ? 503 : 401, "Please sign in again to sync your prayers.");
  const user = z.object({ id: z.string().uuid() }).safeParse(await response.json());
  if (!user.success) throw new PrayerBackendError(503, "Prayer sync returned an invalid response.");
  return { userId: user.data.id, token, url, key };
}

export async function prayerData<T>(session: SupabaseSession, path: string, method = "GET", body?: unknown): Promise<T> {
  let response: Response;
  try {
    response = await fetch(`${session.url}/rest/v1/${path}`, {
      method, cache: "no-store", redirect: "error", signal: AbortSignal.timeout(15000),
      headers: { apikey: session.key, Authorization: `Bearer ${session.token}`, "Content-Type": "application/json", Prefer: "return=representation" },
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch { throw new PrayerBackendError(503, "Prayer sync is temporarily unavailable. Your local history has not been changed."); }
  if (!response.ok) {
    // Do not return database details, tokens, or request bodies to clients/logs.
    const provider = await response.json().catch(() => ({})) as { code?: string };
    if (provider.code === "40001") throw new PrayerBackendError(409, "This prayer changed. Reload before saving.");
    if (response.status === 401) throw new PrayerBackendError(401, "Please sign in again.");
    if (response.status === 409) throw new PrayerBackendError(409, "This item already exists or has changed. Refresh before saving.");
    if (response.status === 400 || response.status === 403) throw new PrayerBackendError(400, "That change could not be saved. Check the linked prayer and try again.");
    throw new PrayerBackendError(503, "Prayer sync is temporarily unavailable.");
  }
  if (response.status === 204) return undefined as T;
  return await response.json() as T;
}

export async function prayerBody(req: Request): Promise<unknown> {
  const reader = req.body?.getReader();
  if (!reader) throw new PrayerBackendError(400, "A request body is required.");
  const chunks: Uint8Array[] = [];
  let size = 0;
  try {
    for (;;) {
      const { done, value } = await reader.read();
      if (done) break;
      size += value.byteLength;
      if (size > 65536) { await reader.cancel(); throw new PrayerBackendError(413, "This entry is too long."); }
      chunks.push(value);
    }
  } finally { reader.releaseLock(); }
  const bytes = new Uint8Array(size);
  let offset = 0;
  for (const chunk of chunks) { bytes.set(chunk, offset); offset += chunk.byteLength; }
  try { return JSON.parse(new TextDecoder().decode(bytes)); }
  catch { throw new PrayerBackendError(400, "The request is not valid JSON."); }
}

export function prayerError(error: unknown) {
  if (error instanceof z.ZodError) return Response.json({ error: "Check the submitted fields." }, { status: 400 });
  if (error instanceof PrayerBackendError) return Response.json({ error: error.message }, { status: error.status });
  return Response.json({ error: "Prayer sync is temporarily unavailable." }, { status: 503 });
}
