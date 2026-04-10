import { NextResponse } from "next/server";
import { auth } from "@/lib/auth";
import { prisma } from "@/lib/prisma";

export const dynamic = "force-dynamic";

export async function POST(req: Request) {
  try {
    const session = await auth();
    if (!session?.user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const viewer = await prisma.user.findUnique({
      where:  { id: session.user.id as string },
      select: { churchId: true, role: true },
    });

    if (!viewer?.churchId || !["PASTOR", "CHURCH_ADMIN", "SUPER_ADMIN"].includes(viewer.role)) {
      return NextResponse.json({ error: "Forbidden" }, { status: 403 });
    }

    const { title, body, pinned = false } = await req.json() as {
      title: string; body: string; pinned?: boolean;
    };

    if (!title?.trim() || !body?.trim()) {
      return NextResponse.json({ error: "title and body required" }, { status: 400 });
    }

    const announcement = await prisma.announcement.create({
      data: {
        churchId: viewer.churchId,
        authorId: session.user.id as string,
        title:    title.trim(),
        body:     body.trim(),
        pinned,
      },
    });

    return NextResponse.json(announcement, { status: 201 });
  } catch (err) {
    console.error("[api/community/announcement POST]", err);
    return NextResponse.json({ error: "Failed to post announcement" }, { status: 500 });
  }
}
