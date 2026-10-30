/**
 * Live Sermon verse detection, checked against how preaching really sounds.
 * Run: npx tsx scripts/check-live-detection.ts   (exits 1 on any failure)
 *
 * 1. Spoken references, as speech recognition writes them.
 * 2. Quoted scripture, in King James and modern wording.
 * 3. A stretch of ordinary preaching that must put nothing on the projector,
 *    read a few seconds at a time exactly as the live page reads it.
 */
import { detectVerseInTranscript, searchBible } from "@/lib/bible/kjv";
import { findSpokenRefs } from "@/lib/bible/spoken-refs";
import { MODERN_QUOTES } from "@/lib/bible/modern-quotes";

let failures = 0;
const check = (ok: boolean, label: string, detail: string) => {
  if (!ok) failures++;
  console.log(ok ? "  ok  " : "  FAIL", label.slice(0, 66).padEnd(68), detail);
};

console.log("Spoken references");
const spoken: [string, string | null][] = [
  ["John three sixteen", "John 3:16"],
  ["John 3 16", "John 3:16"],
  ["turn with me to John chapter 3 verse 16", "John 3:16"],
  ["Psalm ninety one verse one", "Psalms 91:1"],
  ["first Corinthians thirteen four", "1 Corinthians 13:4"],
  ["1st Corinthians 13 4 through 7", "1 Corinthians 13:4-7"],
  ["Romans chapter eight verses twenty eight through thirty", "Romans 8:28-30"],
  ["Psalm one hundred and nineteen verse one hundred and five", "Psalms 119:105"],
  ["second Timothy one seven", "2 Timothy 1:7"],
  ["one John four eight", "1 John 4:8"],
  ["Mark chapter 4", "Mark 4"],
  ["Psalm 23", "Psalms 23"],
  ["I have had this job 3 years", null],
  ["mark 2 people absent", null],
  ["acts 5 times a day", null],
  ["John 3 and his brother", null],
];
for (const [said, want] of spoken) {
  const got = findSpokenRefs(said).map((r) => r.label).pop() ?? null;
  check(got === want, said, `-> ${got ?? "none"}`);
}

console.log("Quoted scripture");
const quotes: [string, string][] = [
  ["and we know that all things work together for good to them that love God", "Romans 8:28"],
  ["God did not give us a spirit of fear but of power and love and a sound mind", "2 Timothy 1:7"],
  ["I can do all things through Christ which strengtheneth me", "Philippians 4:13"],
  ["for I know the plans I have for you declares the Lord plans to prosper you", "Jeremiah 29:11"],
  ["love is patient love is kind it does not envy", "1 Corinthians 13:4"],
  ["I can do all this through him who gives me strength", "Philippians 4:13"],
  ["the Bible says be still and know that I am God", "Psalms 46:10"],
  ["cast all your anxiety on him because he cares for you", "1 Peter 5:7"],
  ["in this world you will have trouble but take heart I have overcome the world", "John 16:33"],
  ["your word is a lamp for my feet a light on my path", "Psalms 119:105"],
  ["the joy of the Lord is your strength church", "Nehemiah 8:10"],
  ["for God so loved the world that he gave his one and only son", "John 3:16"],
  ["fear thou not for I am with thee be not dismayed", "Isaiah 41:10"],
  ["by his stripes we are healed", "Isaiah 53:5"],
  ["the Lord is my shepherd I shall not want", "Psalms 23:1"],
  ["Jesus wept", "John 11:35"],
  ["greater is he that is in you than he that is in the world", "1 John 4:4"],
  ["trust in the Lord with all thine heart and lean not unto thine own understanding", "Proverbs 3:5"],
];
for (const [said, want] of quotes) {
  const r = detectVerseInTranscript(said, { quotesOnly: true });
  check(r.detected && r.ref === want, said, `-> ${r.detected ? `${r.ref} (${r.confidence})` : "none"}`);
}

console.log("Ordinary preaching (nothing may be detected)");
const sermon = `Good morning church, it is so good to be in the house of the Lord today. Please be seated.
Before we begin I want to thank the choir for that beautiful song, and our ushers who came in at seven this morning.
We have three announcements. The youth picnic is next Saturday at the park on Fifth Street, starting at two in the afternoon.
The building fund has reached forty two thousand dollars, praise God, and we are believing for one hundred thousand by Christmas.
Mark your calendars for the women's conference in March. Sister Ruth is leading it this year and James is handling the sound.
I had a job interview twenty years ago and I was so nervous my hands were shaking, and the man looked at me and said son relax.
Some of you are carrying things this morning that you have not told anybody about. Your marriage is struggling. Your children are not speaking to you.
Your doctor gave you a report you did not expect. And you came to church anyway. That is faith.
God has not forgotten you. He sees you. He knows your name and he knows the number of hairs on your head, even if some of us do not have many left.
When I was a boy my grandmother would wake up at five every morning and pray for every one of her nine grandchildren by name.
She did not have much money but she had a prayer life, and that is worth more than anything in a bank.
So this morning I want to talk about three things: how to pray when you are tired, how to wait when it is slow, and how to praise before you see it.
Turn to your neighbor and say neighbor, it is not over. Now turn to the other side and say, God is still working.
God wants to bless your family and your finances this year. Give the Lord a big hand clap.
Let us stand for prayer. Father we thank you for this day and for every person in this room. Amen. You may be seated.`;
let tail = "", alarms = 0;
const phrases = sermon.split(/(?<=[.,])\s+/);
for (const phrase of phrases) {
  const window = `${tail} ${phrase}`.slice(-350);
  const refs = findSpokenRefs(window).map((r) => r.label);
  const quote = detectVerseInTranscript(window, { quotesOnly: true });
  if (refs.length || quote.detected) {
    alarms++;
    check(false, phrase, `-> ${refs.join(", ") || `${quote.ref} (${quote.confidence})`}`);
  }
  tail = window;
}
check(alarms === 0, `${phrases.length} phrases of ordinary preaching`, `${alarms} false alarms`);

console.log("Data");
const unresolved = MODERN_QUOTES.filter(([ref]) => { const r = findSpokenRefs(ref)[0]; return !r || r.verse === null; });
check(unresolved.length === 0, "every phrase-list reference is a real verse", unresolved.map(([r]) => r).join(", ") || "all real");
for (const [q, want] of [["John 3 16", "John 3:16"], ["psalm 91 verse 1", "Psalms 91:1"], ["first Corinthians 13 4", "1 Corinthians 13:4"], ["Psalm 150:6", "Psalms 150:6"]]) {
  const got = searchBible(q, 1)[0]?.ref ?? "none";
  check(got === want, `search "${q}"`, `-> ${got}`);
}

console.log(failures ? `\n${failures} failed` : "\nall passed");
process.exit(failures ? 1 : 0);
