# PrayerKey Supabase backend

This is an additive, undeployed prayer-graph foundation. It does not replace the existing Prisma church schema, and the native app is not yet connected to these routes.

1. Confirm a test Supabase project and inspect existing tables before applying migrations.
2. Apply `migrations/202609110001_prayer_graph.sql`, then `202609110002_preferences.sql` in order.
3. On a local/test Supabase database run `psql -v ON_ERROR_STOP=1 -f supabase/tests/prayer_graph_rls.sql`. The test uses two temporary auth users and rolls back all fixtures. Do not treat static SQL inspection as a passed isolation test.
4. Run `npx tsx scripts/seed-prayer-scriptures.ts` to validate the bundled source. Only use `--apply` after securely configuring `SUPABASE_DB_URL` for the confirmed target. No database credentials should be pasted into source control or client code.
5. Set `NEXT_PUBLIC_SUPABASE_URL` and `NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY` on the server. Configure Android's matching `PRAYERKEY_SUPABASE_*` build settings when native auth/sync is implemented.

## API contract

All new endpoints require a Supabase access token as `Authorization: Bearer …`. Existing NextAuth/legacy mobile tokens are not interchangeable with Supabase sessions.

- `GET/POST /api/prayer/matters`: list/create; client supplies stable UUID for retry detection. Duplicate create returns 409 instead of silently duplicating.
- `GET/PATCH /api/prayer/matters/{id}`: history/optimistic edit. PATCH requires the last known `version`; stale writes return 409.
- `POST /api/prayer/matters/{id}`: append a session/journal/point/update, link Scripture/person/sermon/routine, or record an outcome. Outcomes require a version and user-written testimony.
- `GET /api/prayer/matters/{id}/next`: deterministic suggestion and optional recorded memories. Search currently bounded to 100 recent answered matters; full-history search/pagination remains to implement.
- `GET/POST /api/prayer/preferences`: persist onboarding preferences; native UI does not yet consume synced preferences.
- `GET/POST /api/prayer/resources`: reusable people, sermons and routines. Routine update/completion endpoints remain to implement.

The migration's `pk_scriptures` table is client-read-only. Scripture links reference its keys, not generated text. Personal tables use RLS; same-owner composite foreign keys also prevent cross-account links.

## Not yet complete

Native authentication, secure session lifecycle, offline outbox, cache account isolation, all screen handoffs, complete history pagination, deletion/export RPCs, deployment, production monitoring and physical-device QA. See `../PRODUCT-COMPLETION.md` for release gates. Do not advertise cloud sync from the existence of these server files.
