import { NextRequest, NextResponse } from "next/server";
import { jwtVerify } from "jose";
import { prisma } from "@/lib/prisma";

export const dynamic = "force-dynamic";

async function getUser(req: NextRequest) {
  const auth = req.headers.get("authorization");
  if (!auth?.startsWith("Bearer ")) return null;
  const token = auth.slice(7);
  const secret = new TextEncoder().encode(process.env.NEXTAUTH_SECRET ?? "");
  try {
    const { payload } = await jwtVerify(token, secret);
    return payload as { sub: string; churchId: string };
  } catch {
    return null;
  }
}

export async function GET(req: NextRequest) {
  const jwt = await getUser(req);
  if (!jwt) return NextResponse.json({ error: "Unauthorized" }, { status: 401 });

  const userId = jwt.sub;

  const [prayerCount, amenCount, donations] = await Promise.all([
    prisma.prayer.count({ where: { userId } }),
    prisma.testimony.count({ where: { churchId: jwt.churchId } }), // amens sent (approximation)
    prisma.donation.aggregate({ where: { userId }, _sum: { amount: true } }),
  ]);

  // Compute streak: consecutive days with at least one prayer
  const prayers = await prisma.prayer.findMany({
    where: { userId },
    orderBy: { createdAt: "desc" },
    select: { createdAt: true },
    take: 365,
  });

  let streak = 0;
  if (prayers.length > 0) {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const days = new Set(prayers.map((p) => {
      const d = new Date(p.createdAt);
      d.setHours(0, 0, 0, 0);
      return d.getTime();
    }));

    let cursor = today.getTime();
    while (days.has(cursor)) {
      streak++;
      cursor -= 86400000;
    }
  }

  return NextResponse.json({
    prayerCount,
    prayerStreak:  streak,
    amenCount,
    donationTotal: donations._sum.amount ?? 0,
  });
}
