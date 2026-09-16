import { NextRequest, NextResponse } from "next/server";

export const dynamic = "force-dynamic";

const ALLOWED_AMOUNTS = new Set([5, 10, 25, 50]);

export async function POST(request: NextRequest) {
  const secretKey = process.env.STRIPE_SECRET_KEY;
  if (!secretKey) {
    return NextResponse.json(
      { error: "Stripe is not configured yet." },
      { status: 503 },
    );
  }

  try {
    const body = (await request.json()) as {
      amount?: number;
      frequency?: "once" | "monthly";
    };
    const amount = Number(body.amount);
    if (body.frequency !== "once" && body.frequency !== "monthly") {
      return NextResponse.json({ error: "Choose once or monthly." }, { status: 400 });
    }
    const frequency = body.frequency;

    if (!Number.isInteger(amount) || amount < 1 || amount > 10_000) {
      return NextResponse.json({ error: "Choose an amount between $1 and $10,000." }, { status: 400 });
    }

    // Presets are documented here for auditability; custom whole-dollar gifts
    // are intentionally accepted as well.
    const isPreset = ALLOWED_AMOUNTS.has(amount);
    const origin = process.env.NEXT_PUBLIC_APP_URL || "https://www.prayerkey.com";
    const form = new URLSearchParams({
      mode: frequency === "monthly" ? "subscription" : "payment",
      "line_items[0][quantity]": "1",
      "line_items[0][price_data][currency]": "usd",
      "line_items[0][price_data][unit_amount]": String(amount * 100),
      "line_items[0][price_data][product_data][name]":
        frequency === "monthly" ? "PrayerKey monthly support" : "PrayerKey support",
      "line_items[0][price_data][product_data][description]":
        "Help keep prayer available to everyone.",
      success_url: `${origin}/give/result?session_id={CHECKOUT_SESSION_ID}`,
      cancel_url: `${origin}/give/result?cancelled=1`,
      "metadata[source]": "android_home",
      "metadata[frequency]": frequency,
      "metadata[amount_preset]": String(isPreset),
      "custom_text[submit][message]": frequency === "monthly" ? "Your support repeats every month until cancelled. Contact support through PrayerKey to manage your contribution." : "Your support is optional and does not unlock app features.",
    });
    if (frequency === "monthly") {
      form.set("line_items[0][price_data][recurring][interval]", "month");
    }

    const stripeResponse = await fetch("https://api.stripe.com/v1/checkout/sessions", {
      method: "POST",
      headers: {
        Authorization: `Bearer ${secretKey}`,
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: form,
      cache: "no-store",
      signal: AbortSignal.timeout(15000),
    });
    const stripe = (await stripeResponse.json()) as { url?: string; error?: { message?: string } };

    if (!stripeResponse.ok || !stripe.url) {
      return NextResponse.json(
        { error: "Secure giving is temporarily unavailable. Please try again later." },
        { status: 502 },
      );
    }

    return NextResponse.json({ url: stripe.url });
  } catch {
    return NextResponse.json({ error: "Unable to start secure checkout." }, { status: 400 });
  }
}
