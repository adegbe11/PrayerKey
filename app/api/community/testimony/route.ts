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

    const { title, story, tags = [], anonymous = false } = await req.json() as {
      title: string; story: string; tags?: string[]; anonymous?: boolean;
    };

    if (!title?.trim() || !story?.trim()) {
      return NextResponse.json({ error: "title and story required" }, { status: 400 });
    }

    const testimony = await prisma.testimony.create({
      data: {
        userId:    session.user.id as string,
        title:     title.trim(),
        story:     story.trim(),
        tags,
        anonymous,
        approved:  false, // requires admin approval
      },
    });

    return NextResponse.json(testimony, { status: 201 });
  } catch (err) {
    console.error("[api/community/testimony POST]", err);
    return NextResponse.json({ error: "Failed to submit testimony" }, { status: 500 });
  }
}
