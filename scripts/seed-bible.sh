#!/usr/bin/env bash
# ──────────────────────────────────────────────────────────────
#  Seed Bible verse embeddings into Pinecone
#  Usage: ./scripts/seed-bible.sh
#  Requires: PINECONE_API_KEY, PINECONE_INDEX, ANTHROPIC_API_KEY
# ──────────────────────────────────────────────────────────────
set -euo pipefail

echo "▶ Seeding Bible verse embeddings..."

REQUIRED=(PINECONE_API_KEY PINECONE_INDEX ANTHROPIC_API_KEY)
for var in "${REQUIRED[@]}"; do
  if [[ -z "${!var:-}" ]]; then
    echo "✗ Missing required env var: $var" >&2
    exit 1
  fi
done

npx ts-node --compiler-options '{"module":"CommonJS"}' lib/ai/bible-embeddings.ts

echo "✅ Bible embeddings seeded!"
