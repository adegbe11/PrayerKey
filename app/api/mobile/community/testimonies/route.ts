import { NextResponse } from "next/server";
import { prisma } from "@/lib/prisma";
import { jwtVerify } from "jose";

export const dynamic = "force-dynamic";

const JWT_SECRET = new TextEncoder().encode(
  process.env.NEXTAUTH_SECRET ?? "prayerkey-dev-secret-change-in-production"
);

async function getChurchIdFromToken(req: Request): Promise<string | null> {
  const auth = req.headers.get("authorization");
  if (!auth?.startsWith("Bearer ")) return null;
  try {
    const { payload } = await jwtVerify(auth.slice(7), JWT_SECRET);
    return (payload.churchId as string) ?? null;
  } catch {
    return null;
  }
}

export async function GET(req: Request) {
  const churchId = await getChurchIdFromToken(req);
  if (!churchId) return NextResponse.json({ error: "Unauthorized" }, { status: 401 });

  const testimonies = await prisma.testimony.findMany({
    where:   { user: { churchId }, approved: true },
    include: { user: { select: { name: true } } },
    orderBy: { createdAt: "desc" },
    take:    30,
  });

  return NextResponse.json(testimonies);
}
