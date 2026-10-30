/**
 * The stream overlay relay: the verse on air right now, per room.
 *
 * The live page runs in Chrome; OBS and vMix run their own browser, so the two
 * cannot talk directly the way the projector window does. The live page writes
 * what is on air here, and the overlay reads it about once a second.
 *
 * Storage is Upstash Redis over its REST API when connected (Vercel → Storage →
 * Upstash Redis sets KV_REST_API_URL and KV_REST_API_TOKEN). Without it, a
 * memory store keeps local development working; on Vercel that would not be
 * shared between requests, so the API reports the relay as not set up instead.
 */

export interface OnAir {
  verse: { verseRef: string; verseText: string; translation: string } | null;
  /** increases with every change, so the overlay knows when to animate */
  seq: number;
  at: number;
}

const TTL_SECONDS = 12 * 60 * 60; // a service day; a new service writes again

const url = process.env.KV_REST_API_URL ?? process.env.UPSTASH_REDIS_REST_URL;
const token = process.env.KV_REST_API_TOKEN ?? process.env.UPSTASH_REDIS_REST_TOKEN;

export const relayReady = Boolean(url && token) || !process.env.VERCEL;

async function redis<T>(command: (string | number)[]): Promise<T | null> {
  const res = await fetch(url!, {
    method: "POST",
    headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
    body: JSON.stringify(command),
    cache: "no-store",
  });
  if (!res.ok) throw new Error(`relay store ${res.status}`);
  const data = (await res.json()) as { result: T | null };
  return data.result;
}

// local development only: one process, one map
const memory = (globalThis as unknown as { __pkRelay?: Map<string, string> }).__pkRelay ??= new Map<string, string>();

async function get(key: string): Promise<string | null> {
  if (url && token) return redis<string>(["GET", key]);
  return memory.get(key) ?? null;
}
async function set(key: string, value: string, onlyIfNew = false): Promise<boolean> {
  if (url && token) {
    const cmd: (string | number)[] = ["SET", key, value, "EX", TTL_SECONDS];
    if (onlyIfNew) cmd.push("NX");
    return (await redis<string>(cmd)) === "OK";
  }
  if (onlyIfNew && memory.has(key)) return false;
  memory.set(key, value);
  return true;
}

async function sha256(text: string): Promise<string> {
  const bytes = new Uint8Array(await crypto.subtle.digest("SHA-256", new TextEncoder().encode(text)));
  return Array.from(bytes, (b) => b.toString(16).padStart(2, "0")).join("");
}

export const ROOM_RE = /^[A-Z2-9]{6}$/;
export const KEY_RE = /^[A-Za-z0-9_-]{20,64}$/;

export async function readOnAir(room: string): Promise<OnAir | null> {
  const raw = await get(`pk:onair:${room}`);
  return raw ? (JSON.parse(raw) as OnAir) : null;
}

/** Only the browser that first claimed a room can change what it shows. */
export async function writeOnAir(room: string, key: string, verse: OnAir["verse"]): Promise<"ok" | "forbidden"> {
  const hash = await sha256(key);
  const owner = `pk:owner:${room}`;
  if (!(await set(owner, hash, true))) {
    if ((await get(owner)) !== hash) return "forbidden";
    await set(owner, hash); // keep the claim alive as long as the room is used
  }
  const before = await readOnAir(room);
  const next: OnAir = { verse, seq: (before?.seq ?? 0) + 1, at: Date.now() };
  await set(`pk:onair:${room}`, JSON.stringify(next));
  return "ok";
}
