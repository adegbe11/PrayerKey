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

    const updated = await prisma.prayerRequest.update({
      where: { id: params.id },
      data:  { prayCount: { increment: 1 } },
      select: { id: true, prayCount: true },
    });

    return NextResponse.json(updated);
  } catch (err) {
    console.error("[api/community/prayer-request/:id/pray]", err);
    return NextResponse.json({ error: "Failed" }, { status: 500 });
  }
}
