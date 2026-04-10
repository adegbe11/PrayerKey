import type { NextAuthConfig } from "next-auth";
import Credentials from "next-auth/providers/credentials";
import Google from "next-auth/providers/google";

// Edge-safe auth config — NO Prisma adapter, NO pg imports
// Used only by middleware for JWT validation
export const authConfig: NextAuthConfig = {
  pages: {
    signIn: "/login",
    error:  "/login",
  },
  providers: [
    Google({
      clientId:     process.env.GOOGLE_CLIENT_ID     ?? "",
      clientSecret: process.env.GOOGLE_CLIENT_SECRET ?? "",
    }),
    // Credentials provider listed here so NextAuth knows about it,
    // but actual DB verification only runs in lib/auth.ts (Node.js)
    Credentials({ credentials: {}, authorize: async () => null }),
  ],
  callbacks: {
    async jwt({ token, user }) {
      if (user) {
        const u = user as { role?: string; churchId?: string | null };
        token.role     = u.role;
        token.churchId = u.churchId ?? null;
      }
      return token;
    },
    async session({ session, token }) {
      if (session.user) {
        const u = session.user as { id?: string; role?: string; churchId?: string | null };
        u.id       = token.sub ?? "";
        u.role     = (token.role as string) ?? "MEMBER";
        u.churchId = (token.churchId as string | null) ?? null;
      }
      return session;
    },
    authorized({ auth }) {
      return !!auth?.user;
    },
  },
};
