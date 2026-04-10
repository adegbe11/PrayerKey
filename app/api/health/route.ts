import { NextResponse } from "next/server";
import { prisma } from "@/lib/prisma";

export const dynamic = "force-dynamic";

const START_TIME = Date.now();

export async function GET() {
  const checks: Record<string, { status: "ok" | "error"; latencyMs?: number; detail?: string }> = {};

  // ── Database ──────────────────────────────────────────────────────────────
  const dbStart = Date.now();
  try {
    await prisma.$queryRaw`SELECT 1`;
    checks.database = { status: "ok", latencyMs: Date.now() - dbStart };
  } catch (err) {
    checks.database = { status: "error", latencyMs: Date.now() - dbStart, detail: String(err) };
  }

  const allOk    = Object.values(checks).every((c) => c.status === "ok");
  const uptimeSec = Math.floor((Date.now() - START_TIME) / 1000);

  const body = {
    status:    allOk ? "ok" : "degraded",
    version:   process.env.npm_package_version ?? "0.1.0",
    uptime:    uptimeSec,
    timestamp: new Date().toISOString(),
    checks,
  };

  return NextResponse.json(body, { status: allOk ? 200 : 503 });
}
