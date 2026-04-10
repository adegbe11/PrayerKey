import { NextResponse } from "next/server";

export const dynamic = "force-dynamic";

export async function POST(
  _req: Request,
  { params }: { params: { id: string } }
) {
  return NextResponse.json({
    serviceId:       params.id,
    status:          "ENDED",
    durationSeconds: 0,
    versesDetected:  0,
  });
}
