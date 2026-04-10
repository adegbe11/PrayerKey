/**
 * Shared helper: authenticate a v1 REST request via API key.
 * Returns { churchId, scopes } or a NextResponse error to return immediately.
 */
import { NextRequest, NextResponse } from "next/server";
import { verifyApiKey, extractBearerKey } from "@/lib/api-key";

export type V1Auth = { churchId: string; scopes: string[] };

export async function v1Auth(
  req: NextRequest
): Promise<V1Auth | NextResponse> {
  const raw = extractBearerKey(req.headers.get("authorization"));
  if (!raw) {
    return NextResponse.json(
      { error: "Missing or invalid Authorization header. Use: Bearer pk_live_…" },
      { status: 401 }
    );
  }

  const result = await verifyApiKey(raw);
  if (!result) {
    return NextResponse.json(
      { error: "Invalid, expired, or revoked API key." },
      { status: 401 }
    );
  }

  return result;
}

export function requireScope(auth: V1Auth, scope: string): NextResponse | null {
  if (!auth.scopes.includes(scope) && !auth.scopes.includes("write")) {
    return NextResponse.json(
      { error: `This API key requires the '${scope}' scope.` },
      { status: 403 }
    );
  }
  return null;
}

/** Parse pagination params: ?page=1&limit=20 */
export function parsePagination(req: NextRequest) {
  const url   = new URL(req.url);
  const page  = Math.max(1, parseInt(url.searchParams.get("page")  ?? "1"));
  const limit = Math.min(100, Math.max(1, parseInt(url.searchParams.get("limit") ?? "20")));
  const skip  = (page - 1) * limit;
  return { page, limit, skip };
}
