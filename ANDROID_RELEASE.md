# Android release handoff

## Completed locally

- Debug unit tests
- Android lint
- Release compilation and R8 minification
- Notification permission is optional
- Notification deep links support cold and warm starts
- Reminders restore after reboot and app replacement
- Cleartext traffic is disabled in release builds
- Local data backup is disabled
- Prayer, Bible, journal, church, and microphone permissions are declared

## Required before Play Console upload

- Supply the PrayerKey release keystore through `PRAYERKEY_STORE_FILE`, `PRAYERKEY_STORE_PASSWORD`, `PRAYERKEY_KEY_ALIAS`, and `PRAYERKEY_KEY_PASSWORD`.
- Build the signed Android App Bundle with `gradlew :app:bundleRelease`.
- Publish `PRIVACY_POLICY.md` at a public HTTPS address and enter that address in Play Console.
- Complete the Play Data safety form using the behavior described in the privacy policy.
- Upload phone screenshots captured from the signed release candidate.
- Run a closed test on physical phones before production rollout.
