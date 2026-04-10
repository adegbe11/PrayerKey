#!/usr/bin/env bash
# ──────────────────────────────────────────────────────────────
#  PrayerKey — production deploy script
#  Usage: ./scripts/deploy.sh
# ──────────────────────────────────────────────────────────────
set -euo pipefail

echo "▶ PrayerKey deploy starting..."

# ── 1. Check required env vars ────────────────────────────────
REQUIRED=(DATABASE_URL NEXTAUTH_SECRET ANTHROPIC_API_KEY)
for var in "${REQUIRED[@]}"; do
  if [[ -z "${!var:-}" ]]; then
    echo "✗ Missing required env var: $var" >&2
    exit 1
  fi
done
echo "✓ Environment vars OK"

# ── 2. Install dependencies ───────────────────────────────────
echo "▶ Installing dependencies..."
npm ci --legacy-peer-deps --omit=dev
echo "✓ Dependencies installed"

# ── 3. Generate Prisma client ─────────────────────────────────
echo "▶ Generating Prisma client..."
npx prisma generate
echo "✓ Prisma client generated"

# ── 4. Run database migrations ────────────────────────────────
echo "▶ Running database migrations..."
npx prisma migrate deploy
echo "✓ Migrations applied"

# ── 5. Build Next.js ──────────────────────────────────────────
echo "▶ Building Next.js..."
npm run build
echo "✓ Build complete"

echo ""
echo "✅ Deploy complete! Start with: npm start"
