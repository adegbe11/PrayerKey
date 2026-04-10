import { NextRequest, NextResponse } from "next/server";
import { auth } from "@/lib/auth";
import { prisma } from "@/lib/prisma";

export const dynamic = "force-dynamic";

async function getAdminUser() {
  const session = await auth();
  const u = session?.user as { id?: string; role?: string; churchId?: string | null } | undefined;
  if (!u?.id || !u.churchId) return null;
  if (!["SUPER_ADMIN", "CHURCH_ADMIN", "PASTOR"].includes(u.role ?? "")) return null;
  return u as { id: string; role: string; churchId: string };
}

// DELETE — revoke an API key
export async function DELETE(
  req: NextRequest,
  { params }: { params: { id: string } }
) {
  const user = await getAdminUser();
  if (!user) return NextResponse.json({ error: "Unauthorized" }, { status: 401 });

  const key = await prisma.apiKey.findUnique({ where: { id: params.id } });
  if (!key || key.churchId !== user.churchId) {
    return NextResponse.json({ error: "Not found" }, { status: 404 });
  }

  await prisma.apiKey.update({
    where: { id: params.id },
    data:  { revokedAt: new Date() },
  });

  return NextResponse.json({ ok: true });
}
