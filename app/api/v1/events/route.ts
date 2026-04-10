import { NextRequest, NextResponse } from "next/server";
import { prisma } from "@/lib/prisma";
import { v1Auth, requireScope, parsePagination } from "@/lib/v1-auth";

export const dynamic = "force-dynamic";

/**
 * GET /api/v1/events
 * Requires scope: read
 * Query: ?page=1&limit=20&upcoming=true
 *
 * POST /api/v1/events
 * Requires scope: write
 * Body: { title, date, location?, description? }
 */
export async function GET(req: NextRequest) {
  const auth = await v1Auth(req);
  if (auth instanceof NextResponse) return auth;

  const { page, limit, skip } = parsePagination(req);
  const url      = new URL(req.url);
  const upcoming = url.searchParams.get("upcoming");

  const where = {
    churchId: auth.churchId,
    ...(upcoming === "true" ? { date: { gte: new Date() } } : {}),
  };

  const [events, total] = await Promise.all([
    prisma.event.findMany({
      where,
      skip,
      take: limit,
      orderBy: { date: "asc" },
      select: { id: true, title: true, date: true, location: true, description: true, attendees: true, createdAt: true },
    }),
    prisma.event.count({ where }),
  ]);

  return NextResponse.json({
    data: events,
    meta: { page, limit, total, totalPages: Math.ceil(total / limit) },
  });
}

export async function POST(req: NextRequest) {
  const auth = await v1Auth(req);
  if (auth instanceof NextResponse) return auth;

  const denied = requireScope(auth, "write");
  if (denied) return denied;

  const body = await req.json() as {
    title?: string; date?: string; location?: string; description?: string;
  };

  if (!body.title?.trim() || !body.date) {
    return NextResponse.json({ error: "title and date are required" }, { status: 400 });
  }

  const dateVal = new Date(body.date);
  if (isNaN(dateVal.getTime())) {
    return NextResponse.json({ error: "Invalid date format" }, { status: 400 });
  }

  const event = await prisma.event.create({
    data: {
      churchId:    auth.churchId,
      title:       body.title.trim(),
      date:        dateVal,
      location:    body.location?.trim()    ?? null,
      description: body.description?.trim() ?? null,
    },
    select: { id: true, title: true, date: true, location: true, description: true, createdAt: true },
  });

  return NextResponse.json({ data: event }, { status: 201 });
}
