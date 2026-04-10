import { NextRequest, NextResponse } from "next/server";
import { prisma } from "@/lib/prisma";
import { v1Auth, parsePagination } from "@/lib/v1-auth";

export const dynamic = "force-dynamic";

/**
 * GET /api/v1/testimonies
 * Requires scope: read
 * Query: ?page=1&limit=20&tag=Healing&approved=true
 */
export async function GET(req: NextRequest) {
  const auth = await v1Auth(req);
  if (auth instanceof NextResponse) return auth;

  const { page, limit, skip } = parsePagination(req);
  const url      = new URL(req.url);
  const tag      = url.searchParams.get("tag") ?? undefined;
  const approved = url.searchParams.get("approved");

  const where = {
    user: { churchId: auth.churchId },
    ...(approved !== null ? { approved: approved !== "false" } : { approved: true }),
    ...(tag ? { tags: { has: tag } } : {}),
  };

  const [testimonies, total] = await Promise.all([
    prisma.testimony.findMany({
      where,
      skip,
      take: limit,
      orderBy: { createdAt: "desc" },
      select: {
        id: true, title: true, story: true, tags: true,
        anonymous: true, amenCount: true, approved: true, createdAt: true,
        user: { select: { id: true, name: true } },
      },
    }),
    prisma.testimony.count({ where }),
  ]);

  // Mask anonymous authors
  const masked = testimonies.map((t) => ({
    ...t,
    user: t.anonymous ? null : t.user,
  }));

  return NextResponse.json({
    data: masked,
    meta: { page, limit, total, totalPages: Math.ceil(total / limit) },
  });
}
