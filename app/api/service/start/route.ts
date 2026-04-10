import { NextResponse } from "next/server";

export const dynamic = "force-dynamic";

export async function POST(req: Request) {
  try {
    const body = await req.json().catch(() => ({}));
    const { title, type = "SUNDAY_SERVICE" } = body as {
      title?: string;
      type?: string;
    };

    const serviceId = crypto.randomUUID();
    const serviceTitle = title ?? `Service — ${new Date().toLocaleDateString("en-US", { weekday: "long", month: "long", day: "numeric" })}`;

    return NextResponse.json({ serviceId, title: serviceTitle, type, status: "LIVE" });
  } catch (err) {
    console.error("[api/service/start]", err);
    return NextResponse.json({ error: "Failed to start service" }, { status: 500 });
  }
}
