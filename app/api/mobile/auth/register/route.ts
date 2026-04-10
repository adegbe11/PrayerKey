import { NextResponse } from "next/server";
import { prisma } from "@/lib/prisma";
import bcrypt from "bcryptjs";
import { SignJWT } from "jose";

export const dynamic = "force-dynamic";

const JWT_SECRET = new TextEncoder().encode(
  process.env.NEXTAUTH_SECRET ?? "prayerkey-dev-secret-change-in-production"
);

export async function POST(req: Request) {
  try {
    const { name, email, password, churchCode } = await req.json() as {
      name: string; email: string; password: string; churchCode?: string;
    };

    if (!email || !password || !name) {
      return NextResponse.json({ error: "name, email and password required" }, { status: 400 });
    }

    const existing = await prisma.user.findUnique({ where: { email: email.toLowerCase().trim() } });
    if (existing) {
      return NextResponse.json({ error: "Email already registered" }, { status: 409 });
    }

    // Resolve church by slug/code if provided
    let churchId: string | null = null;
    if (churchCode) {
      const church = await prisma.church.findUnique({ where: { slug: churchCode.toLowerCase().trim() } });
      if (church) churchId = church.id;
    }

    const hashed = await bcrypt.hash(password, 12);

    const user = await prisma.user.create({
      data: {
        name:     name.trim(),
        email:    email.toLowerCase().trim(),
        password: hashed,
        role:     "MEMBER",
        churchId,
      },
      select: { id: true, email: true, name: true, role: true, churchId: true },
    });

    const token = await new SignJWT({
      sub:      user.id,
      email:    user.email,
      role:     user.role,
      churchId: user.churchId,
    })
      .setProtectedHeader({ alg: "HS256" })
      .setIssuedAt()
      .setExpirationTime("30d")
      .sign(JWT_SECRET);

    return NextResponse.json({ token, user }, { status: 201 });
  } catch (err) {
    console.error("[api/mobile/auth/register]", err);
    return NextResponse.json({ error: "Registration failed" }, { status: 500 });
  }
}
