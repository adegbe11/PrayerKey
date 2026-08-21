import { readFileSync, writeFileSync } from "node:fs";

const asset = "android/app/src/main/assets/devotionals.json";
const bibleAsset = "android/app/src/main/assets/kjv.json";
const data = JSON.parse(readFileSync(asset, "utf8"));
const bible = JSON.parse(readFileSync(bibleAsset, "utf8"));
const books = new Map(bible.map((book) => [book.name, book.chapters]));

const plans = {
  5: { category: "Purpose", refs: ["Matthew 5:14","Matthew 5:16","Ephesians 2:10","Colossians 3:23","Proverbs 16:3","Proverbs 16:9","Jeremiah 1:5","Isaiah 6:8","1 Peter 4:10","Romans 12:6","1 Corinthians 10:31","Galatians 6:9","Micah 6:8","James 1:22","Philippians 2:13","Acts 20:24","Psalm 90:17","Psalm 37:23","Proverbs 3:6","Ecclesiastes 9:10","Luke 16:10","Mark 10:45","John 15:16","2 Timothy 1:9","2 Timothy 2:15","Hebrews 6:10","Romans 8:28","1 Thessalonians 5:24","Philippians 1:6","Joshua 1:8","Psalm 138:8"] },
  6: { category: "Protection", refs: ["Isaiah 41:10","Joshua 1:9","Psalm 27:1","Psalm 46:1","Psalm 18:2","Psalm 23:4","Psalm 34:17","Psalm 34:18","Psalm 55:22","Psalm 56:3","Psalm 61:2","Psalm 73:26","Psalm 91:2","Psalm 121:1","Isaiah 40:29","Isaiah 40:31","Isaiah 43:2","Nahum 1:7","Habakkuk 3:19","Matthew 11:28","John 14:27","John 16:33","Romans 8:31","Romans 8:37","1 Corinthians 16:13","2 Corinthians 4:8","2 Corinthians 12:9","Ephesians 6:10","Philippians 4:13","2 Timothy 1:7"] },
  7: { category: "Faith", refs: ["Psalm 5:3","Psalm 19:14","Psalm 27:4","Psalm 42:1","Psalm 46:10","Psalm 51:10","Psalm 63:1","Psalm 84:10","Psalm 86:11","Psalm 95:6","Psalm 100:2","Psalm 119:11","Psalm 119:18","Psalm 119:105","Psalm 143:8","Proverbs 8:17","Jeremiah 29:13","Matthew 6:6","Matthew 7:7","Mark 1:35","Luke 10:42","Luke 11:1","John 4:24","John 15:4","Romans 12:1","Romans 12:12","Ephesians 6:18","Philippians 4:6","Colossians 4:2","1 Thessalonians 5:17","James 4:8"] },
  8: { category: "Wisdom", refs: ["Proverbs 1:7","Proverbs 2:6","Proverbs 3:5","Proverbs 3:7","Proverbs 4:7","Proverbs 4:23","Proverbs 10:9","Proverbs 10:19","Proverbs 11:14","Proverbs 12:15","Proverbs 13:20","Proverbs 14:15","Proverbs 15:1","Proverbs 15:22","Proverbs 16:2","Proverbs 16:18","Proverbs 17:27","Proverbs 18:13","Proverbs 19:20","Proverbs 20:22","Proverbs 21:5","Proverbs 22:1","Proverbs 24:3","Proverbs 27:17","Ecclesiastes 3:1","Micah 6:8","Matthew 7:24","Luke 14:28","James 1:5","James 1:19","James 3:17"] },
  9: { category: "Forgiveness", refs: ["Psalm 32:1","Psalm 51:1","Psalm 51:7","Psalm 103:10","Psalm 103:12","Isaiah 1:18","Isaiah 43:25","Micah 7:18","Micah 7:19","Matthew 5:7","Matthew 5:44","Matthew 6:12","Matthew 6:14","Matthew 18:21","Matthew 18:22","Mark 11:25","Luke 6:36","Luke 6:37","Luke 7:47","Luke 15:20","John 8:11","Romans 5:8","Romans 8:1","Romans 12:18","2 Corinthians 5:17","2 Corinthians 5:19","Ephesians 1:7","Ephesians 4:32","Colossians 3:13","1 John 1:9"] },
  10: { category: "Faith", refs: [...Array(14).fill(null), "Romans 5:3","Romans 5:4","Romans 8:25","Romans 12:12","1 Corinthians 15:58","2 Corinthians 4:16","2 Corinthians 4:17","Galatians 6:9","Ephesians 6:13","Philippians 3:14","Colossians 1:11","1 Thessalonians 1:3","2 Thessalonians 3:13","Hebrews 10:23","Hebrews 10:36","Hebrews 12:1","James 1:3"] },
  11: { category: "Gratitude", refs: ["1 Chronicles 16:34","Psalm 7:17","Psalm 9:1","Psalm 28:7","Psalm 30:12","Psalm 34:1","Psalm 50:14","Psalm 69:30","Psalm 75:1","Psalm 92:1","Psalm 95:2","Psalm 100:4","Psalm 103:2","Psalm 107:1","Psalm 107:8","Psalm 116:12","Psalm 118:24","Psalm 136:1","Daniel 2:23","Jonah 2:9","Luke 17:15","John 6:11","Romans 1:8","2 Corinthians 9:15","Ephesians 5:20","Philippians 4:6","Colossians 2:7","Colossians 3:15","1 Thessalonians 5:18","Hebrews 13:15"] },
  12: { category: "Joy", refs: ["Nehemiah 8:10","Psalm 16:11","Psalm 30:5","Psalm 32:11","Psalm 47:1","Psalm 51:12","Psalm 63:7","Psalm 68:3","Psalm 71:23","Psalm 89:15","Psalm 98:4","Psalm 100:1","Psalm 118:24","Psalm 126:3","Psalm 126:5","Isaiah 9:3","Isaiah 12:3","Isaiah 35:10","Isaiah 55:12","Habakkuk 3:18","Luke 1:47","Luke 2:10","Luke 10:20","John 15:11","John 16:22","Romans 12:15","Romans 15:13","2 Corinthians 6:10","Galatians 5:22","Philippians 4:4","1 Peter 1:8"] },
};

const lenses = {
  5: ["Purpose is usually practised before it is recognised.", "Calling becomes visible through faithful service.", "God's direction never excuses today's obedience."],
  6: ["Courage tells the truth about danger without giving danger the final word.", "Strength in Scripture is received before it is displayed.", "God's presence does not make weakness shameful; it makes weakness a meeting place."],
  7: ["Devotion is attention returned to God again and again.", "Prayer is not performance; it is honest presence before God.", "A deep life with God is built in ordinary, unobserved moments."],
  8: ["Wisdom begins when being teachable matters more than appearing certain.", "A faithful decision joins truth, timing, and humility.", "The wise heart pauses long enough to hear correction."],
  9: ["Grace names the wrong truthfully and refuses to let it own the future.", "Forgiveness releases revenge; it does not erase wisdom or healthy boundaries.", "Mercy received becomes mercy practised."],
  10: ["Endurance is faithfulness after the first strength has passed.", "Waiting with God is not wasted time.", "Perseverance keeps doing the next faithful thing without demanding immediate proof."],
  11: ["Gratitude pays attention to grace without denying grief.", "Thanksgiving turns a gift into worship.", "A grateful heart remembers the Giver while receiving the gift."],
  12: ["Biblical joy can live beside tears because its source is deeper than circumstance.", "Joy grows when hope becomes specific.", "Celebration is one way a community remembers God's goodness together."],
};

const openings = [
  (theme) => `There are days when ${theme.toLowerCase()} sounds larger than anything an ordinary person can practise before breakfast. Scripture brings it down to one sentence and one response.`,
  (theme) => `We often wait to feel ready before living with ${theme.toLowerCase()}. This passage begins somewhere firmer than a feeling.`,
  (theme) => `${theme} is easily reduced to an idea. The Bible repeatedly turns it into a way of walking through an actual day.`,
  (theme) => `A hurried reading can make a familiar verse disappear. Read this one slowly enough to notice what it asks you to trust.`,
  (theme) => `The strongest words in Scripture are often quiet enough to miss. Today's reading gives ${theme.toLowerCase()} a clear centre.`,
];

const applications = [
  "Do not try to carry the whole month today. Choose the one response this verse makes clear and practise it before the day closes.",
  "Let the passage correct the sentence you have been repeating to yourself. Then make one decision that agrees with the corrected sentence.",
  "Return to this verse at midday. Read it once as information, then once as a word you are willing to obey.",
  "Take the pressure off a dramatic response. Quiet faithfulness in one conversation or decision is enough work for today.",
  "Write the reference somewhere you will meet it again. Repetition is not a failure to understand; it is how truth becomes available under pressure.",
];

const questions = {
  5: "What faithful responsibility is already in your hands?", 6: "Where do you need received strength rather than forced confidence?",
  7: "What has been competing for your attention before God?", 8: "Which decision needs a slower and more teachable heart?",
  9: "What would grace change in the way you remember this?", 10: "What is the next faithful thing while you wait?",
  11: "What specific grace have you overlooked today?", 12: "Where can you make room for honest joy today?",
};

const titleTurns = [
  "Before You Feel Ready", "In the Next Step", "When the Way Is Quiet", "Without an Audience",
  "For an Ordinary Tuesday", "When Plans Change", "In the Work at Hand", "When You Need Clarity",
  "One Choice at a Time", "Under Pressure", "After the Excitement Fades", "In a Difficult Conversation",
  "When Progress Is Slow", "At the Point of Decision", "In the Waiting Room", "When You Feel Small",
  "Through an Open Door", "When the Answer Is No", "In What You Already Have", "When Nobody Notices",
  "With Both Feet on the Ground", "When You Must Begin Again", "In the Middle of the Work", "When Fear Speaks First",
  "With an Open Hand", "When You Cannot See Far", "In the Honest Answer", "When the Cost Is Real",
  "For the Long Road", "At the End of the Day", "For What Comes Next",
];
const titleNouns = { 5: "Purpose", 6: "Courage", 7: "Prayer", 8: "Wisdom", 9: "Grace", 10: "Endurance", 11: "Gratitude", 12: "Joy" };

const clean = (text) => text.replace(/\{[^}]*}/g, "").replace(/\s+/g, " ").trim();
const parseRef = (label) => {
  const match = label.match(/^(.+) (\d+):(\d+)$/);
  if (!match) throw new Error(`Bad reference: ${label}`);
  const book = match[1] === "Psalm" ? "Psalms" : match[1];
  return { book, chapter: Number(match[2]), verse: Number(match[3]) };
};
const verseAt = (ref) => {
  const chapters = books.get(ref.book);
  const verse = chapters?.[ref.chapter - 1]?.[ref.verse - 1];
  if (!verse) throw new Error(`Missing KJV verse: ${ref.book} ${ref.chapter}:${ref.verse}`);
  return clean(verse);
};
const keyWords = (verse) => verse
  .replace(/[^A-Za-z' ]/g, " ").split(/\s+/)
  .filter((word) => word.length > 3 && !["that","with","from","your","shall","unto","have","this","they","them","their","will","lord","which","when","then"].includes(word.toLowerCase()));
const titleFor = (month, day) => `${titleNouns[month]} ${titleTurns[day - 1]}`;

for (const key of data.keys) {
  const plan = plans[key.month];
  if (!plan) continue;
  // Months 5–9 and 11–12 had no original entries. October's first fourteen
  // are the existing long-form series and must remain untouched.
  key.written = key.month === 10 ? key.written.filter((day) => day.day <= 14) : [];
  const byDay = new Map(key.written.map((day) => [day.day, day]));
  for (let day = 1; day <= key.days; day++) {
    if (byDay.has(day)) continue;
    const index = day - 1;
    const label = plan.refs[index];
    if (!label) throw new Error(`No reading for month ${key.month}, day ${day}`);
    const ref = parseRef(label);
    const verse = verseAt(ref);
    const lens = lenses[key.month][index % lenses[key.month].length];
    const opening = `${openings[index % openings.length](key.theme)} Today’s reading, ${label}, is worth hearing without rushing past its claim.`;
    const application = applications[(index * 2 + key.month) % applications.length];
    const title = titleFor(key.month, day);
    byDay.set(day, {
      day, title, minutes: 3 + (day % 2),
      read: { ...ref, from: Math.max(1, ref.verse - 1), to: Math.min(books.get(ref.book)[ref.chapter - 1].length, ref.verse + 1) },
      body: [
        { p: opening },
        { q: ref },
        { p: `Read ${label} as a whole. It does not leave ${key.theme.toLowerCase()} as a vague religious mood. It gives the heart something true to remember and the hands something faithful to do.` },
        { p: lens },
        { p: `The verse belongs to a larger chapter, not to a collection of slogans. Hold its promise together with its call to trust, obey, wait, speak, or act. Scripture forms mature faith by keeping comfort and responsibility in the same room.` },
        { p: `You do not need to manufacture a dramatic spiritual moment. Let the truth of ${label} become precise enough to enter your schedule, your tone of voice, and the decision directly in front of you.` },
        { p: application },
      ],
      line: { a: lens, b: `Live the next faithful response to ${label}.` },
      after: [application, `Carry ${label} with you and let it shape one real choice.`],
      prayer: `Father, plant the truth of ${label} deeply in me. Give me grace to receive it without resistance and courage to live it without delay. Let ${key.theme.toLowerCase()} become visible in my words, choices, and relationships today. In Jesus' name, Amen.`,
      addLabel: `Add your prayer from ${label}`,
      question: `As you sit with ${label}, ${questions[key.month].replace(/^./, (letter) => letter.toLowerCase())}`,
      prayerCategory: plan.category,
    });
  }
  key.written = [...byDay.values()].sort((a, b) => a.day - b.day);
}

writeFileSync(asset, JSON.stringify(data));
console.log(`Completed ${data.keys.reduce((sum, key) => sum + key.written.length, 0)} devotional days.`);
