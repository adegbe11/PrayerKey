import { NextResponse } from "next/server";

export const dynamic = "force-dynamic";

export async function POST(
  _req: Request,
  { params }: { params: { id: string } }
) {
  return NextResponse.json({ id: params.id, isBookmarked: true });
}
