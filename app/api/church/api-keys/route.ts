import { NextRequest, NextResponse } from "next/server";
import { auth } from "@/lib/auth";
import { prisma } from "@/lib/prisma";
import { generateKey } from "@/lib/api-key";

export const dynamic = "force-dynamic";

async function getAdminUser(req: NextRequest) {
  const session = await auth();
  const u = session?.user as { id?: string; role?: string; churchId?: string | null } | undefined;
  if (!u?.id || !u.churchId) return null;
  if (!["SUPER_ADMIN", "CHURCH_ADMIN", "PASTOR"].includes(u.role ?? "")) return null;
  return u as { id: string; role: string; churchId: string };
}

// GET — list all API keys for the church (hashes hidden)
export async function GET(req: NextRequest) {
  const user = await getAdminUser(req);
  if (!user) return NextResponse.json({ error: "Unauthorized" }, { status: 401 });

  const keys = await prisma.apiKey.findMany({
    where: { churchId: user.churchId },
    orderBy: { createdAt: "desc" },
    select: {
      id: true, name: true, keyPrefix: true, scopes: true,
      lastUsedAt: true, expiresAt: true, revokedAt: true, createdAt: true,
    },
  });

  return NextResponse.json(keys);
}

// POST — create a new API key
export async function POST(req: NextRequest) {
  const user = await getAdminUser(req);
  if (!user) return NextResponse.json({ error: "Unauthorized" }, { status: 401 });

  const body = await req.json() as { name?: string; scopes?: string[]; expiresInDays?: number };
  const name   = (body.name ?? "").trim();
  const scopes = Array.isArray(body.scopes) && body.scopes.length > 0 ? body.scopes : ["read"];
  if (!name) return NextResponse.json({ error: "Name is required" }, { status: 400 });

  const { raw, prefix, hash } = generateKey();

  const expiresAt = body.expiresInDays
    ? new Date(Date.now() + body.expiresInDays * 86400000)
    : null;

  await prisma.apiKey.create({
    data: { churchId: user.churchId, name, keyHash: hash, keyPrefix: prefix, scopes, expiresAt },
  });

  // Return the raw key ONCE — never stored in plain text
  return NextResponse.json({ key: raw, prefix, name, scopes }, { status: 201 });
}
