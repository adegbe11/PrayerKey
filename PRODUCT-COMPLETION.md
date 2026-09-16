# PrayerKey product completion and QA

Status: IN PROGRESS. Not approved for public release.

## Product contract

Life situation → Scripture → Prayer → Prayer Points → Routine → Keep Praying → Journal → Answer → Testimony → Remember.

Supabase Auth and PostgreSQL will own signed-in users' connected prayer history. Android local storage remains intact; it is not silently uploaded, replaced, or cleared. Existing Prisma/NextAuth church infrastructure is unchanged. A separate identity mapping/migration decision is required before unifying church accounts with Supabase Auth.

## Implemented in this pass

- Additive Supabase SQL migrations for matters, canonical Scriptures, people, sermons, routines, prayer sessions, prayer points, journal entries, updates/testimonies and typed links.
- Per-user RLS and same-owner composite foreign keys, unique relationship edges, versioned matter edits, atomic outcome/testimony function.
- Supabase-authenticated server routes for matter creation/history, optimistic edits, linked records, preferences, reusable resources and next-action recommendations. Requests forward the user's token rather than bypassing RLS with a service-role key.
- Deterministic next-action orchestration uses saved next actions, routines, completed-session presence and user-recorded testimonies. It does not claim to know God's will. It is not an LLM or a complete theological/safety review system.
- Server input validation, bounded JSON request bodies, bounded list responses, non-verbose provider errors and fixed configured backend origin.
- Bible importer defaults to validation only. Four malformed annotation records have shared reference-specific corrections; source file remains untouched.
- Android database downgrade no longer drops prayer/journal tables. Unsupported database opens show a recovery message instead of presenting empty onboarding.
- Device speech tests updated from removed Vosk interfaces to the current Sherpa wrapper.

## Verification ledger

| Check | Result |
|---|---|
| `npx tsc --noEmit` | Passed |
| `npx tsx scripts/test-prayer-graph.ts` | Passed; mock Supabase only |
| `npx tsx scripts/seed-prayer-scriptures.ts` | Passed: 66 books, 31,102 unique references, no unresolved braces |
| Full text comparison for all 31,102 verses | Not performed; structural validation is not textual certification |
| Supabase migrations executed | NOT RUN: project connection missing |
| `supabase/tests/prayer_graph_rls.sql` | Written; NOT RUN against PostgreSQL |
| Native Supabase account flow | NOT IMPLEMENTED; current welcome links still use website auth |
| Native graph integration / sync | NOT IMPLEMENTED; current Android screens do not call the new graph routes |
| Android unit tests | 166 passed, 0 failures/errors/skips |
| Android debug app + instrumentation APK builds | Passed; obsolete speech-test imports repaired |
| Isolated emulator history-preservation regression | Passed: 1 test, in-memory database; personal history untouched |
| Speech recognition on actual audio | Not executed in this pass; compiling the test does not prove accuracy |

## Next implementation gates, in order

1. Confirm Supabase project and test environment, configure URL/publishable key, inspect its existing schema, apply migrations only to the intended project. Execute two-user and anonymous RLS tests. Import canonical Scripture in the test environment.
2. Implement native Supabase sign-up, sign-in, email confirmation, password reset, session refresh, secure token storage, sign-out, account deletion and user-scoped local caches. Website sign-in must not masquerade as native authentication.
3. Introduce durable UUID matter IDs locally, transactional outbox, retry/idempotency, explicit upload consent, conflict UI, account isolation, tombstones, backup/export and restore tests. Preserve original local IDs and the existing archive format through a versioned migration. Never infer that similarly worded matters belong together.
4. Connect Pray → Keep → Journal first. Saving must report success only after persistence. Link references to canonical Scripture; never save generated prayer text as a Bible verse. Then add Journal → Pray and Bible → Pray with the same matter ID.
5. Attach people, prayer points, morning/evening prayer, challenges, fasts and sermons. Completion must record real activity, not button taps. Do not automatically prescribe a fast from a health-related prayer.
6. Add outcome/testimony workflow and Remember When using actual recorded history, with opt-out, context and no guarantee of repeated outcomes. Never turn a model's guess into an answered prayer.
7. Make onboarding choices influence relevant suggestions, with editing/deletion controls. Do not claim personalisation merely because choices were collected.
8. Review generation and fallback content for Scripture accuracy, sensitive situations, fabricated prophecy, promises of healing/outcomes and accidental portrayal of generated words as God's voice. Bound input and preserve coroutine cancellation. Audit transmission/privacy wording.
9. Finish Stripe deployment and test-mode success/failure/cancel, receipts, recurring cancellation and reconciliation. Do not accept live donations before these pass.
10. Physical-device release QA: cold start, low memory, airplane mode, interrupted sync, killed process, rotation, large fonts, TalkBack, notification denial/reboot, microphone denial, extended sermon recording, backup recovery, ownership changes and signed release AAB.

## Release acceptance scenario

Use a dedicated test user and “My daughter's university admission.” Create a matter, attach verified Scripture and prayer points, pray and record completion, attach a challenge and a fast, write updates, restart the app offline, reconnect, sign in on a second test device, mark answered and write a testimony. Confirm all data remains linked, retries create no duplicates, a second unrelated user sees none of it, and Remember When only uses the saved testimony. Export, restore and deletion must preserve or remove the whole graph coherently.

This scenario has NOT passed yet. Do not mark the product complete until it does.

## References

- Supabase ownership/RLS: https://supabase.com/docs/guides/database/postgres/row-level-security
- Supabase user data: https://supabase.com/docs/guides/auth/managing-user-data
- Apparatus repairs retain existing KJV verse body and exclude corrupt notes/subscriptions. Cross-checks: https://www.biblegateway.com/passage/?search=1+Corinthians+16%3A24&version=KJV ; https://www.biblegateway.com/passage/?search=Hebrews+10%3A34&version=KJV ; https://biblehub.com/kjv/romans/16-27.htm . Micah 7:12 retains the existing source body before its nested marginal note.
