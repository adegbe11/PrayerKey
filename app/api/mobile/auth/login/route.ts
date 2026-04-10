/**
 * Mobile JWT login — returns a signed token the app stores in SecureStore.
 * Uses the same User/bcrypt setup as the web credentials provider.
 */
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
    const { email, password } = await req.json() as { email: string; password: string };

    if (!email || !password) {
      return NextResponse.json({ error: "Email and password required" }, { status: 400 });
    }

    const user = await prisma.user.findUnique({
      where:  { email: email.toLowerCase().trim() },
      select: { id: true, email: true, name: true, password: true, role: true, churchId: true },
    });

    if (!user?.password) {
      return NextResponse.json({ error: "Invalid credentials" }, { status: 401 });
    }

    const valid = await bcrypt.compare(password, user.password);
    if (!valid) {
      return NextResponse.json({ error: "Invalid credentials" }, { status: 401 });
    }

    // Sign a 30-day JWT
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

    return NextResponse.json({
      token,
      user: {
        id:       user.id,
        email:    user.email,
        name:     user.name,
        role:     user.role,
        churchId: user.churchId,
      },
    });
  } catch (err) {
    console.error("[api/mobile/auth/login]", err);
    return NextResponse.json({ error: "Login failed" }, { status: 500 });
  }
}
