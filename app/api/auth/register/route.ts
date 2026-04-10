import { NextResponse } from "next/server";

export const dynamic = "force-dynamic";
import bcrypt from "bcryptjs";
import { prisma } from "@/lib/prisma";

export async function POST(req: Request) {
  try {
    const body = await req.json() as {
      type: "individual" | "church";
      name?: string;
      adminName?: string;
      email: string;
      password: string;
      churchName?: string;
      city?: string;
      country?: string;
      size?: string;
    };

    const { type, email, password } = body;

    if (!email || !password) {
      return NextResponse.json({ error: "Email and password are required." }, { status: 400 });
    }

    const existing = await prisma.user.findUnique({ where: { email } });
    if (existing) {
      return NextResponse.json({ error: "An account with this email already exists." }, { status: 409 });
    }

    const hashed = await bcrypt.hash(password, 12);

    if (type === "individual") {
      await prisma.user.create({
        data: {
          email,
          name:     body.name ?? null,
          password: hashed,
          role:     "MEMBER",
        },
      });
    } else {
      // Church registration — create church + admin user
      const slug = (body.churchName ?? "church")
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, "-")
        .replace(/(^-|-$)/g, "")
        .substring(0, 50) + "-" + Date.now().toString(36);

      const church = await prisma.church.create({
        data: {
          name:    body.churchName ?? "My Church",
          slug,
          city:    body.city ?? null,
          country: body.country ?? null,
          plan:    "STARTER",
        },
      });

      await prisma.user.create({
        data: {
          email,
          name:     body.adminName ?? null,
          password: hashed,
          role:     "CHURCH_ADMIN",
          churchId: church.id,
        },
      });
    }

    return NextResponse.json({ success: true }, { status: 201 });
  } catch (err) {
    console.error("[register]", err);
    return NextResponse.json({ error: "Registration failed. Please try again." }, { status: 500 });
  }
}
