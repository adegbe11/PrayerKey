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

    const body = await req.json() as {
      title: string; date: string; location?: string; description?: string;
    };

    if (!body.title || !body.date) {
      return NextResponse.json({ error: "title and date required" }, { status: 400 });
    }

    const event = await prisma.event.create({
      data: {
        churchId:    viewer.churchId,
        title:       body.title,
        date:        new Date(body.date),
        location:    body.location,
        description: body.description,
      },
    });

    return NextResponse.json(event, { status: 201 });
  } catch (err) {
    console.error("[api/church/events POST]", err);
    return NextResponse.json({ error: "Failed to create event" }, { status: 500 });
  }
}
