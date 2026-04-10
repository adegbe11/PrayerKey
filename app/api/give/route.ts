import { NextResponse } from "next/server";
import { auth } from "@/lib/auth";
import { prisma } from "@/lib/prisma";

export const dynamic = "force-dynamic";

const SUPPORTED_CURRENCIES = ["USD", "NGN", "GBP", "EUR", "CAD", "AUD", "ZAR", "GHS", "KES"];

export async function POST(req: Request) {
  try {
    const session = await auth();
    if (!session?.user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const user = await prisma.user.findUnique({
      where:  { id: session.user.id as string },
      select: { churchId: true },
    });

    if (!user?.churchId) {
      return NextResponse.json({ error: "No church associated" }, { status: 400 });
    }

    const { fundId, amount, currency = "USD", note } = await req.json() as {
      fundId: string;
      amount: number;
      currency?: string;
      note?: string;
    };

    if (!fundId || !amount || amount <= 0) {
      return NextResponse.json({ error: "fundId and a positive amount are required" }, { status: 400 });
    }

    if (!SUPPORTED_CURRENCIES.includes(currency)) {
      return NextResponse.json({ error: "Unsupported currency" }, { status: 400 });
    }

    // Verify the fund belongs to this church
    const fund = await prisma.givingFund.findUnique({
      where:  { id: fundId },
      select: { churchId: true, name: true },
    });

    if (!fund || fund.churchId !== user.churchId) {
      return NextResponse.json({ error: "Fund not found" }, { status: 404 });
    }

    // Record donation as COMPLETED — no payment gateway required
    const donation = await prisma.donation.create({
      data: {
        churchId: user.churchId,
        userId:   session.user.id as string,
        fundId,
        // amount stored in smallest unit (kobo/cents/pence)
        amount:   Math.round(amount * 100),
        currency,
        status:   "COMPLETED",
      },
      include: { fund: { select: { name: true } } },
    });

    return NextResponse.json({
      id:       donation.id,
      fundName: donation.fund.name,
      amount:   donation.amount,
      currency: donation.currency,
      status:   donation.status,
    }, { status: 201 });
  } catch (err) {
    console.error("[api/give POST]", err);
    return NextResponse.json({ error: "Failed to record donation" }, { status: 500 });
  }
}

// GET — fetch giving funds for the current user's church
export async function GET(_req: Request) {
  try {
    const session = await auth();
    if (!session?.user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const user = await prisma.user.findUnique({
      where:  { id: session.user.id as string },
      select: { churchId: true },
    });

    if (!user?.churchId) {
      return NextResponse.json([], { status: 200 });
    }

    const funds = await prisma.givingFund.findMany({
      where:   { churchId: user.churchId },
      include: {
        donations: {
          where:  { status: "COMPLETED" },
          select: { amount: true },
        },
      },
      orderBy: { createdAt: "asc" },
    });

    return NextResponse.json(funds.map((f) => ({
      id:          f.id,
      name:        f.name,
      description: f.description,
      goal:        f.goal,
      raised:      f.donations.reduce((s, d) => s + d.amount, 0),
    })));
  } catch (err) {
    console.error("[api/give GET]", err);
    return NextResponse.json({ error: "Failed to load funds" }, { status: 500 });
  }
}
