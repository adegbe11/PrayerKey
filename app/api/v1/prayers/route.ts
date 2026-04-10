import { NextRequest, NextResponse } from "next/server";
import { prisma } from "@/lib/prisma";
import { v1Auth, parsePagination } from "@/lib/v1-auth";

export const dynamic = "force-dynamic";

/**
 * GET /api/v1/prayers
 * Requires scope: read | prayers
 * Query: ?page=1&limit=20&bookmarked=true
 */
export async function GET(req: NextRequest) {
  const auth = await v1Auth(req);
  if (auth instanceof NextResponse) return auth;

  const { page, limit, skip } = parsePagination(req);
  const url        = new URL(req.url);
  const bookmarked = url.searchParams.get("bookmarked");

  const where = {
    user: { churchId: auth.churchId },
    ...(bookmarked === "true" ? { isBookmarked: true } : {}),
  };

  const [prayers, total] = await Promise.all([
    prisma.prayer.findMany({
      where,
      skip,
      take: limit,
      orderBy: { createdAt: "desc" },
      select: {
        id: true, mood: true, userInput: true,
        generatedPrayer: true, verses: true,
        encouragement: true, isBookmarked: true, createdAt: true,
        user: { select: { id: true, name: true } },
      },
    }),
    prisma.prayer.count({ where }),
  ]);

  return NextResponse.json({
    data: prayers,
    meta: { page, limit, total, totalPages: Math.ceil(total / limit) },
  });
}
