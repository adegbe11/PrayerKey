# PrayerKey — Claude Code Instructions

## Project overview
PrayerKey is an AI-powered church operating system. Full-stack monorepo:
- `app/` — Next.js 14 App Router web app
- `mobile/` — Expo SDK 55 React Native app
- `prisma/` — PostgreSQL schema via Prisma 7
- `lib/` — shared server utilities
- `components/` — React components (server + client)
- `server/` — Socket.io live sermon server

## Key rules

### Never break these
- Run `npx prisma generate` after any schema change
- All API routes that touch the DB must have `export const dynamic = "force-dynamic"`
- Mobile JWT auth uses `jose` (not `jsonwebtoken`) — `jwtVerify` only
- Prisma JSON fields need explicit cast: `value as object[]` or `value as Prisma.InputJsonValue`

### Design system
- Background: `#F5F5F7`, Primary text: `#1D1D1F`, Secondary: `#6E6E73`, Gold: `#B07C1F`
- Use inline styles (no Tailwind classes in core UI)
- Apple.com aesthetic: clean, spacious, no gradients
- Mobile uses `@/constants/theme` tokens — never hardcode colors

### Database
- Prisma 7 with `@prisma/adapter-pg` — NO `url` field in `datasource db` block
- `DATABASE_URL` env var is read by the pg adapter directly
- All relations must be declared on both sides or Prisma will error

### Authentication
- Web: NextAuth v5 (beta) — `auth()` from `@/lib/auth`
- Mobile: JWT via `jose` SignJWT, stored in `expo-secure-store`, 30-day expiry
- Session user shape: `{ id, email, name, role, churchId }`

### REST API v1
- All endpoints in `app/api/v1/` — require `Authorization: Bearer pk_live_…`
- Auth via `v1Auth()` from `@/lib/v1-auth`
- Response envelope: `{ data, meta: { page, limit, total, totalPages } }`

### Deepgram SDK v5 (breaking changes)
- Use `DeepgramClient` class (not `createClient`)
- `dg.listen.v1.connect(options)` returns a `V1Socket`
- Events: `"open"`, `"message"`, `"close"`, `"error"` (not `LiveTranscriptionEvents`)
- `socket.sendMedia(buffer)` not `socket.send()`
- Boolean options must be strings: `smart_format: "true"` not `smart_format: true`

### Pinecone SDK v7
- `index.upsert({ records: vectors })` — NOT `index.upsert(vectors)`

## Feature map
| Route | Description |
|-------|-------------|
| `/` | Landing / auth gate |
| `/live` | Pastor live sermon control |
| `/live/projector` | Full-screen congregation display |
| `/pray` | Prayer AI engine |
| `/community` | Testimonies + prayer wall |
| `/give` | Free donation tracking |
| `/church` | Church management dashboard |
| `/church/members` | Member roster + role management |
| `/church/events` | Events calendar |
| `/church/giving` | Giving funds + ledger |
| `/church/sermons` | Sermon archive |
| `/church/api-keys` | REST API key management |
| `/admin` | Super admin (SUPER_ADMIN only) |
| `/admin/churches` | All churches table |

## Mobile tabs
Home → Pray → Live (Socket.io) → Community → Give → Profile

## Third-party APIs used
- Groq `llama-3.3-70b-versatile` — prayer generation, verse detection (free tier)
- Deepgram nova-2 — real-time speech-to-text
- Pinecone — Bible verse vector search
- Google OAuth — social login
