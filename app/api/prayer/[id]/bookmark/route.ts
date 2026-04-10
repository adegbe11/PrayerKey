import { NextResponse } from "next/server";
import { auth } from "@/lib/auth";
import { prisma } from "@/lib/prisma";

export const dynamic = "force-dynamic";

export async function POST(
  _req: Request,
  { params }: { params: { id: string } }
) {
  try {
    const session = await auth();
    if (!session?.user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const prayer = await prisma.prayer.findUnique({
      where: { id: params.id },
      select: { userId: true, isBookmarked: true },
    });

    if (!prayer) {
      return NextResponse.json({ error: "Prayer not found" }, { status: 404 });
    }

    if (prayer.userId !== (session.user.id as string)) {
      return NextResponse.json({ error: "Forbidden" }, { status: 403 });
    }

    const updated = await prisma.prayer.update({
      where: { id: params.id },
      data:  { isBookmarked: !prayer.isBookmarked },
      select: { id: true, isBookmarked: true },
    });

    return NextResponse.json(updated);
  } catch (err) {
    console.error("[api/prayer/:id/bookmark]", err);
    return NextResponse.json({ error: "Failed to update bookmark" }, { status: 500 });
  }
}
