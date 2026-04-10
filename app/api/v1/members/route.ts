import { NextRequest, NextResponse } from "next/server";
import { prisma } from "@/lib/prisma";
import { v1Auth, parsePagination } from "@/lib/v1-auth";

export const dynamic = "force-dynamic";

/**
 * GET /api/v1/members
 * Requires scope: read | members
 * Query: ?page=1&limit=20&role=MEMBER&search=john
 */
export async function GET(req: NextRequest) {
  const auth = await v1Auth(req);
  if (auth instanceof NextResponse) return auth;

  const { page, limit, skip } = parsePagination(req);
  const url    = new URL(req.url);
  const role   = url.searchParams.get("role") ?? undefined;
  const search = url.searchParams.get("search") ?? undefined;

  const where = {
    churchId: auth.churchId,
    ...(role ? { role: role as never } : {}),
    ...(search ? {
      OR: [
        { name:  { contains: search, mode: "insensitive" as const } },
        { email: { contains: search, mode: "insensitive" as const } },
      ],
    } : {}),
  };

  const [members, total] = await Promise.all([
    prisma.user.findMany({
      where,
      skip,
      take: limit,
      orderBy: { createdAt: "desc" },
      select: {
        id: true, name: true, email: true, role: true,
        prayerStreak: true, bestStreak: true, createdAt: true,
        _count: { select: { prayers: true, donations: true } },
      },
    }),
    prisma.user.count({ where }),
  ]);

  return NextResponse.json({
    data: members,
    meta: { page, limit, total, totalPages: Math.ceil(total / limit) },
  });
}
