# PrayerKey — Play Console

Everything the Console asks for, written out. Copy each block into the matching
field. Where a question has one correct answer given what the code does, the
answer is stated rather than left open — those are the ones that get an app
suspended if they are wrong.

---

## App details

**App name** (30 characters max)

```
PrayerKey
```

**Short description** (80 characters max)

```
Pray daily, read the Bible offline, journal, and keep your sermon notes.
```

**Full description** (4000 characters max)

```
PrayerKey is a place to pray, read and remember — built to be opened every
morning, and to work whether or not you have signal.

TEN PULLS, ONE PRAYER
Each day gives you one prayer in ten movements. It opens with an address to
God, moves through thanksgiving, a verse, the day's asking, a declaration and
prayer for others, and closes with Amen. Pull each one when you have prayed it.
Stop halfway and it remembers where you were.

A DAY THAT AGREES WITH ITSELF
The devotional, the reading and the prayer are chosen together, not separately.
You pray about waiting, then read the passage where somebody waited, then the
devotional names what that waiting cost him. One subject, three ways in.

THE WHOLE BIBLE, OFFLINE
The complete King James Version is inside the app — all 66 books, 31,102
verses, with nothing to download and no connection required. Read a chapter in
ten pulls, or open the book and read the way you would read a book.

366 DAYS OF DEVOTIONALS
A written year, arranged as twelve monthly keys. Every day is written — not
generated, not a template with the date changed.

543 PRAYERS BY SITUATION
Illness, work, waiting, fear, family, protection, provision. Each one comes
with its scripture and its own prayer points.

CHURCH NOTES THAT WRITE THEMSELVES
Record the service and PrayerKey transcribes it on your device — no cloud, no
account, no upload. Scripture references are caught as they are spoken and the
sermon is arranged into a note you can actually read afterwards.

A JOURNAL THAT REMEMBERS
Write what you prayed and mark it when it is answered.

FASTING, CHALLENGES AND REMINDERS
Set a fast with a safe rhythm. Take a prayer challenge one day at a time. Get a
reminder at the hour you actually pray.

NO ACCOUNT. NO SUBSCRIPTION.
PrayerKey does not ask you to sign up and does not charge you. Your journal,
your prayers and your recordings stay on your phone.
```

---

## Categorisation

| Field | Answer |
| --- | --- |
| App or game | App |
| Category | Lifestyle |
| Tags | Faith, Journal, Audio |
| Email | support@prayerkey.com |
| Website | https://www.prayerkey.com |
| Privacy Policy | https://www.prayerkey.com/privacy |

---

## Data safety

This section is a legal declaration. Every answer below was checked against
what the code actually does, in commit efc8f01.

### Does your app collect or share any of the required user data types?

**Yes.**

### Is all of the user data collected by your app encrypted in transit?

**Yes.** Every request uses HTTPS; cleartext traffic is disabled in the
manifest.

### Do you provide a way for users to request that their data is deleted?

**Yes** — deleting content in the app deletes it, and uninstalling removes
everything stored locally. Prayer-generation requests are not retained against
a user identity.

### Data types

Declare exactly these three, and nothing else.

**1. Personal info → Other personal info**
- Collected: **Yes** · Shared: **No**
- Processed ephemerally: **Yes**
- Required or optional: **Optional**
- Purpose: **App functionality**
- Why: when the user types a prayer request, that text is sent to PrayerKey's
  server so a prayer can be written for it. The text can be personal — illness,
  family, money. It answers that one request. It is not linked to an identity,
  because the app has no accounts.

**2. App activity → Search history**
- Collected: **Yes** · Shared: **No**
- Processed ephemerally: **Yes**
- Required or optional: **Optional**
- Purpose: **App functionality**
- Why: Bible search terms are sent so results can be returned.

**3. App activity → Other actions**
- Collected: **No** · Shared: **Yes**
- Purpose: **App functionality**
- Why: looking up a verse in a translation other than the bundled KJV sends the
  verse reference to bible-api.com, a third party. Nothing else goes with it.

### What to answer NO to, and why it matters

These get declared wrongly all the time. Every one of them is **No** here:

- **Audio → Voice or sound recordings.** Sermon recordings never leave the
  device. Transcription runs on the phone. There is no upload path in the app
  at all — the socket that once existed was deleted before release.
- **Personal info → Name, Email, Phone, Address.** Never asked for.
- **Financial info.** Nothing is charged.
- **Location.** Never requested.
- **Contacts, Photos, Files, Calendar, Messages.** Never accessed.
- **App activity → App interactions / Crash logs / Diagnostics.** No analytics
  SDK, no crash reporter.
- **Device or other IDs.** None collected.

---

## Content rating questionnaire

| Question | Answer |
| --- | --- |
| Category | Reference, News or Educational |
| Violence, sexual content, profanity, drugs | No to all |
| User-generated content shared with others | **No** — the journal is private to the device |
| Users can interact or exchange content | **No** |
| Shares user location | **No** |
| Allows purchases | **No** |
| Contains ads | **No** |

Expected outcome: rated for everyone.

---

## App access

Choose **All functionality is available without special access**. There is no
sign-in anywhere in the app, so no test credentials are needed. Reviewers who
find a login wall and no credentials reject the submission; PrayerKey has none.

---

## Ads

**No, my app does not contain ads.**

---

## Government apps / Financial features / Health

**No** to all three. If asked about health claims: PrayerKey makes none, and
the privacy policy states plainly that it is not a medical, legal, financial or
emergency service.

---

## Graphics still needed

These cannot be written, only made. Sizes are what the Console rejects on if
they are wrong.

| Asset | Size | Notes |
| --- | --- | --- |
| App icon | 512 × 512 PNG, 32-bit | No transparency, no rounded corners — Play adds them |
| Feature graphic | 1024 × 500 PNG or JPG | No transparency. Shown at the top of the listing |
| Phone screenshots | at least 2, up to 8 | 16:9 or 9:16, min 320 px, max 3840 px |
| Tablet screenshots | optional | Skip unless a tablet build is being promoted |

Screenshots worth taking, in this order — the first two are what people judge
the app on:

1. The prayer session at pull 1 — "Dear Lord…" on the purple ground
2. Today, showing the devotional hero and the circle rail
3. The Bible reader open on a chapter
4. Church mid-recording with live captions
5. The journal

---

## Release

| Field | Value |
| --- | --- |
| Track | Internal testing first, then Production |
| Bundle | `PrayerKey-3.0.0-17.aab` |
| Version | 3.0.0 (versionCode 17) |
| Signature | SHA384withRSA, 2048-bit, valid to 15 Aug 2056 |

**Release notes** (500 characters max)

```
The first release of PrayerKey.

Ten pulls of prayer each morning and ten of scripture. A written year of
devotionals. The whole King James Bible, offline. 543 prayers by situation.
Church notes transcribed on your phone, never uploaded. A journal that marks
what was answered.

No account. No subscription.
```

**Go with Internal testing first.** It installs on your own phone through Play
exactly as a real user would receive it, which is the only way to find the
things that only appear in a signed, minified, store-delivered build. Promote
to Production once it opens and prays cleanly on a real device.
