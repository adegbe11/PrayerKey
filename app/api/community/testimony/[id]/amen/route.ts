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

    const testimony = await prisma.testimony.findUnique({
      where:  { id: params.id },
      select: { amenCount: true },
    });

    if (!testimony) {
      return NextResponse.json({ error: "Not found" }, { status: 404 });
    }

    const updated = await prisma.testimony.update({
      where: { id: params.id },
      data:  { amenCount: { increment: 1 } },
      select: { id: true, amenCount: true },
    });

    return NextResponse.json(updated);
  } catch (err) {
    console.error("[api/community/testimony/:id/amen]", err);
    return NextResponse.json({ error: "Failed" }, { status: 500 });
  }
}
