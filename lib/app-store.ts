// The PrayerKey Android app. Each placement tags its link so installs can be traced in Play Console.
export const PLAY_ID = "com.prayerkey.manna";

export function playUrl(source: "download" | "banner" | "nav" | "home" | "footer" | "qr" = "download") {
  return `https://play.google.com/store/apps/details?id=${PLAY_ID}&utm_source=prayerkey.com&utm_medium=${source}`;
}

/** Everything below is taken from the Play listing; nothing here is claimed that the listing does not say. */
export const APP_INCLUDES = [
  "Personal prayers for what you are facing",
  "Over 500 prayer situations",
  "Structured prayer points",
  "Guided prayer sessions",
  "5, 10, 15, 30 and 60 minute options",
  "Morning, evening and midnight prayer",
  "Warfare prayer and prayer challenges",
  "366 written devotionals",
  "Verse of the Day",
  "The whole King James Bible, offline",
  "Audio Bible",
  "Topical Bible verses",
  "Sermon notes transcribed on your phone",
  "Fasting with safe daily rhythms",
  "A prayer journal",
  "Saved prayers and prayer history",
];
