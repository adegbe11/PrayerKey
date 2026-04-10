import crypto from "crypto";
import { prisma } from "@/lib/prisma";

/** Hash a raw API key for storage/comparison */
export function hashKey(raw: string): string {
  return crypto.createHash("sha256").update(raw).digest("hex");
}

/** Generate a new API key — returns the raw key (shown once) + prefix for display */
export function generateKey(): { raw: string; prefix: string; hash: string } {
  const bytes  = crypto.randomBytes(32).toString("hex");
  const raw    = `pk_live_${bytes}`;
  const prefix = raw.slice(0, 16); // "pk_live_" + 8 hex chars
  const hash   = hashKey(raw);
  return { raw, prefix, hash };
}

/** Verify an API key from a request. Returns the key record + churchId or null. */
export async function verifyApiKey(
  rawKey: string
): Promise<{ churchId: string; scopes: string[] } | null> {
  if (!rawKey?.startsWith("pk_live_")) return null;

  const hash = hashKey(rawKey);
  const key = await prisma.apiKey.findUnique({
    where: { keyHash: hash },
    select: { churchId: true, scopes: true, revokedAt: true, expiresAt: true },
  });

  if (!key)                                  return null;
  if (key.revokedAt)                         return null;
  if (key.expiresAt && key.expiresAt < new Date()) return null;

  // fire-and-forget usage stamp
  prisma.apiKey.updateMany({
    where: { keyHash: hash },
    data:  { lastUsedAt: new Date() },
  }).catch(() => {});

  return { churchId: key.churchId, scopes: key.scopes };
}

/** Extract raw key from Authorization header: "Bearer pk_live_..." */
export function extractBearerKey(authHeader: string | null): string | null {
  if (!authHeader?.startsWith("Bearer ")) return null;
  const key = authHeader.slice(7).trim();
  return key.startsWith("pk_live_") ? key : null;
}
