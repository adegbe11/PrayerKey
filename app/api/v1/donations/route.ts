import { NextRequest, NextResponse } from "next/server";
import { prisma } from "@/lib/prisma";
import { v1Auth, parsePagination } from "@/lib/v1-auth";

export const dynamic = "force-dynamic";

/**
 * GET /api/v1/donations
 * Requires scope: read | donations
 * Query: ?page=1&limit=20&currency=NGN&fundId=xxx&from=2025-01-01&to=2025-12-31
 */
export async function GET(req: NextRequest) {
  const auth = await v1Auth(req);
  if (auth instanceof NextResponse) return auth;

  const { page, limit, skip } = parsePagination(req);
  const url      = new URL(req.url);
  const currency = url.searchParams.get("currency") ?? undefined;
  const fundId   = url.searchParams.get("fundId")   ?? undefined;
  const from     = url.searchParams.get("from")     ?? undefined;
  const to       = url.searchParams.get("to")       ?? undefined;

  const where = {
    churchId: auth.churchId,
    status: "COMPLETED" as const,
    ...(currency ? { currency } : {}),
    ...(fundId   ? { fundId   } : {}),
    ...(from || to ? {
      createdAt: {
        ...(from ? { gte: new Date(from) } : {}),
        ...(to   ? { lte: new Date(to)   } : {}),
      },
    } : {}),
  };

  const [donations, total, aggregate] = await Promise.all([
    prisma.donation.findMany({
      where,
      skip,
      take: limit,
      orderBy: { createdAt: "desc" },
      select: {
        id: true, amount: true, currency: true, status: true, createdAt: true,
        fund: { select: { id: true, name: true } },
        user: { select: { id: true, name: true } },
      },
    }),
    prisma.donation.count({ where }),
    prisma.donation.aggregate({ where, _sum: { amount: true } }),
  ]);

  return NextResponse.json({
    data: donations,
    meta: {
      page, limit, total,
      totalPages:   Math.ceil(total / limit),
      totalRaisedCents: aggregate._sum.amount ?? 0,
    },
  });
}
