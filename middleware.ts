import NextAuth from "next-auth";
import { authConfig } from "@/lib/auth.config";
import { NextResponse } from "next/server";
import type { NextAuthRequest } from "next-auth";

const { auth } = NextAuth(authConfig);

const PUBLIC = [
  "/login",
  "/register",
  "/pricing",
  "/about",
  "/privacy",
  "/terms",
  "/forgot-password",
  "/api/auth",
  "/api/health",
  "/api/v1",
  "/api/mobile",
];

const CHURCH_ROLES = ["CHURCH_ADMIN", "PASTOR", "SUPER_ADMIN"];

export default auth(function middleware(req: NextAuthRequest) {
  const { pathname } = req.nextUrl;

  if (
    PUBLIC.some((p) => pathname.startsWith(p)) ||
    pathname.startsWith("/_next") ||
    pathname.startsWith("/favicon")
  ) {
    return NextResponse.next();
  }

  const session = req.auth;

  if (!session?.user) {
    const url = new URL("/login", req.url);
    url.searchParams.set("callbackUrl", pathname);
    return NextResponse.redirect(url);
  }

  if (pathname.startsWith("/church") || pathname.startsWith("/admin")) {
    const role = (session.user as { role?: string }).role ?? "";
    if (!CHURCH_ROLES.includes(role) && role !== "SUPER_ADMIN") {
      return NextResponse.redirect(new URL("/", req.url));
    }
  }

  return NextResponse.next();
});

export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon.ico).*)"],
};
