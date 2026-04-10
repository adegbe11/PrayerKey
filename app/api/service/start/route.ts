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

    const body = await req.json().catch(() => ({}));
    const { title, type = "SUNDAY_SERVICE" } = body as {
      title?: string;
      type?: string;
    };

    // Resolve church from user session
    const user = await prisma.user.findUnique({
      where: { id: session.user.id as string },
      select: { churchId: true },
    });

    if (!user?.churchId) {
      return NextResponse.json(
        { error: "User is not associated with a church" },
        { status: 400 }
      );
    }

    // Create a new Service record
    const service = await prisma.service.create({
      data: {
        churchId:  user.churchId,
        title:     title ?? `Service — ${new Date().toLocaleDateString("en-US", { weekday: "long", month: "long", day: "numeric" })}`,
        type,
        startedAt: new Date(),
        status:    "LIVE",
      },
    });

    return NextResponse.json({ serviceId: service.id, status: "LIVE" });
  } catch (err) {
    console.error("[api/service/start]", err);
    return NextResponse.json({ error: "Failed to start service" }, { status: 500 });
  }
}
