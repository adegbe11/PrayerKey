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

    const user = await prisma.user.findUnique({
      where:  { id: session.user.id as string },
      select: { churchId: true },
    });

    if (!user?.churchId) {
      return NextResponse.json({ error: "No church associated" }, { status: 400 });
    }

    const { title, body, anonymous = false } = await req.json() as {
      title: string; body: string; anonymous?: boolean;
    };

    if (!title?.trim() || !body?.trim()) {
      return NextResponse.json({ error: "title and body required" }, { status: 400 });
    }

    const request = await prisma.prayerRequest.create({
      data: {
        userId:    session.user.id as string,
        churchId:  user.churchId,
        title:     title.trim(),
        body:      body.trim(),
        anonymous,
      },
    });

    return NextResponse.json(request, { status: 201 });
  } catch (err) {
    console.error("[api/community/prayer-request POST]", err);
    return NextResponse.json({ error: "Failed to submit prayer request" }, { status: 500 });
  }
}
