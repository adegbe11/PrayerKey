import { NextResponse } from "next/server";
import { auth } from "@/lib/auth";
import { prisma } from "@/lib/prisma";
import { UserRole } from "@prisma/client";

export const dynamic = "force-dynamic";

const ALLOWED_ROLES = ["MEMBER", "CHURCH_ADMIN", "PASTOR"];

export async function PATCH(
  req: Request,
  { params }: { params: { id: string } }
) {
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

    const { role } = await req.json() as { role: UserRole };

    if (!ALLOWED_ROLES.includes(role as string)) {
      return NextResponse.json({ error: "Invalid role" }, { status: 400 });
    }

    // Only PASTOR/SUPER_ADMIN can assign PASTOR role
    if ((role as string) === "PASTOR" && !["PASTOR", "SUPER_ADMIN"].includes(viewer.role)) {
      return NextResponse.json({ error: "Insufficient permissions" }, { status: 403 });
    }

    // Target must be in same church
    const target = await prisma.user.findUnique({
      where:  { id: params.id },
      select: { churchId: true },
    });

    if (target?.churchId !== viewer.churchId) {
      return NextResponse.json({ error: "Forbidden" }, { status: 403 });
    }

    const updated = await prisma.user.update({
      where: { id: params.id },
      data:  { role },
      select: { id: true, role: true },
    });

    return NextResponse.json(updated);
  } catch (err) {
    console.error("[api/church/members/:id/role]", err);
    return NextResponse.json({ error: "Failed to update role" }, { status: 500 });
  }
}
