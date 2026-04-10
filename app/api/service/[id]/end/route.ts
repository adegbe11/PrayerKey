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

    const { id } = params;

    // Verify the service belongs to the user's church
    const user = await prisma.user.findUnique({
      where: { id: session.user.id as string },
      select: { churchId: true },
    });

    const service = await prisma.service.findUnique({
      where: { id },
      select: { churchId: true, startedAt: true },
    });

    if (!service) {
      return NextResponse.json({ error: "Service not found" }, { status: 404 });
    }

    if (service.churchId !== user?.churchId) {
      return NextResponse.json({ error: "Forbidden" }, { status: 403 });
    }

    const endedAt = new Date();
    const durationSeconds = service.startedAt
      ? Math.floor((endedAt.getTime() - service.startedAt.getTime()) / 1000)
      : 0;

    // Count verses detected in this service
    const versesDetected = await prisma.verseDetection.count({
      where: { serviceId: id },
    });

    const updated = await prisma.service.update({
      where: { id },
      data: {
        status:          "ENDED",
        endedAt,
        durationSeconds,
        versesDetected,
      },
    });

    return NextResponse.json({
      serviceId:       updated.id,
      status:          "ENDED",
      durationSeconds,
      versesDetected,
    });
  } catch (err) {
    console.error("[api/service/:id/end]", err);
    return NextResponse.json({ error: "Failed to end service" }, { status: 500 });
  }
}
