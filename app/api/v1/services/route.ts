import { NextRequest, NextResponse } from "next/server";
import { prisma } from "@/lib/prisma";
import { v1Auth, parsePagination } from "@/lib/v1-auth";

export const dynamic = "force-dynamic";

/**
 * GET /api/v1/services
 * Requires scope: read
 * Query: ?page=1&limit=20&status=ENDED
 */
export async function GET(req: NextRequest) {
  const auth = await v1Auth(req);
  if (auth instanceof NextResponse) return auth;

  const { page, limit, skip } = parsePagination(req);
  const url    = new URL(req.url);
  const status = url.searchParams.get("status") ?? undefined;

  const where = {
    churchId: auth.churchId,
    ...(status ? { status } : {}),
  };

  const [services, total] = await Promise.all([
    prisma.service.findMany({
      where,
      skip,
      take: limit,
      orderBy: { createdAt: "desc" },
      select: {
        id: true, title: true, type: true, status: true,
        startedAt: true, endedAt: true, durationSeconds: true,
        versesDetected: true, attendanceCount: true,
        summaryAI: true, createdAt: true,
        detectedVerses: {
          select: { verseRef: true, verseText: true, translation: true, confidence: true, detectedAt: true },
          orderBy: { detectedAt: "asc" },
          take: 50,
        },
      },
    }),
    prisma.service.count({ where }),
  ]);

  return NextResponse.json({
    data: services,
    meta: { page, limit, total, totalPages: Math.ceil(total / limit) },
  });
}
