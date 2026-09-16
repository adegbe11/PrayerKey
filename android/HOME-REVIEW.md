# Home review — 11 September 2026

Implemented:

- Removed the repeated Today verse/prayer cards; retained the devotional, daily rhythm, six sections, topical Bible, Give, and final verse.
- Six section cards now open feature menus. Existing top shortcuts retain direct access.
- Daily completion is recorded by the devotional completion callback, explicit Finish prayer buttons, or the Bible session's recorded completion. Legacy tap-to-complete data is not reused.
- Named morning/evening entries open the corresponding office. Rhythm time highlighting refreshes while Home is open.
- Notification bell explains reminders and links to their settings; it no longer displays streaks as unread counts.
- Shortcut widths adapt; section and final-verse cards allow content growth. Dock labels use compact title case.
- Final verse rotates across seven KJV passages by local date. Save, Share, and Pray this use the displayed verse.
- Donation browser launch validates the Stripe checkout host and handles errors. Monthly recurrence is visible before checkout. A dedicated return page verifies Stripe payment status and no longer depends on the old church-giving page.

Verified:

- Android unit tests and debug build passed after behavior changes.
- TypeScript check passed.
- Mocked checkout checks passed for missing configuration, invalid amounts/frequency, cents conversion, recurring mode, return URLs, and safe provider errors. No payments were made.
- Emulator screenshots inspected for Home, card grid, and final verse. Pray section menu opened. Pray this prefilled Psalm 23:1 correctly.
- Enlarged text inspection exposed dock-label clipping; labels were corrected.

External work still required before accepting live donations:

- Configure the deployed server's Stripe secret and canonical public URL, deploy the new checkout and result routes, and run Stripe test-mode success/cancel/failure flows.
- Verify account wallet availability, receipts, and recurring-support cancellation management. No live Stripe transaction or deployment was performed in this review.

This is a Home improvement pass, not a certification of every app screen, all device sizes, or store-release readiness.
