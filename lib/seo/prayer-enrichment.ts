import type { PrayerTopic } from "@/lib/seo/prayer-topics";

// Written depth for prayers that search shows people want more from. Each
// entry is merged over the prayer's own data, so its page gets real questions,
// points and background instead of the general ones. Scripture is KJV.

type Extra = Partial<Pick<PrayerTopic, "metaDesc" | "context" | "prayerPoints" | "howToPray" | "faqs">> & { seoTitle?: string };

const EXTRA: Record<string, Extra> = {
  "new-month-prayer": {
    seoTitle: "New Month Prayer: Praying for a New Month",
    metaDesc: "A new month prayer for praying over the month ahead: give God the first day, release last month, and ask for His goodness in every week to come, with Bible verses to pray.",
    context: "Praying for a new month is an old habit of God's people. Israel marked the beginnings of their months before the Lord (Numbers 10:10), and the psalmist asked God to teach us to number our days. A new month prayer is not a charm for good luck. It is a way of handing the next thirty days to God before they start, and of beginning again on His mercies, which the Bible says are new every morning.",
    prayerPoints: [
      "Thank God for carrying you through last month, the parts you understood and the parts you did not.",
      "Give Him the first day of this month before you give it to anything else: your time, your plans and your worries.",
      "Release what last month left behind. Ask Him to close what should close and to heal what still hurts.",
      "Pray Lamentations 3:22–23 over the weeks ahead: his compassions fail not, they are new every morning.",
      "Ask for wisdom to number your days (Psalm 90:12), so this month is spent on what matters.",
      "Pray for the people you will meet this month, at home, at work and at church.",
      "Close by thanking God in advance: thou crownest the year with thy goodness (Psalm 65:11).",
    ],
    howToPray: [
      "Pray on the first morning of the month, before your phone and before the day starts asking for things.",
      "Look back for a minute. Name one thing God did last month and thank Him for it.",
      "Look ahead. Write down what is in the calendar, the decisions, the dates, the people, and pray over each one.",
      "Speak one verse over the month, such as Isaiah 43:19: Behold, I will do a new thing.",
      "Come back to the prayer each Sunday of the month and see what God has done with it.",
    ],
    faqs: [
      { q: "What do you say when praying for a new month?", a: "Thank God for last month, give Him the new one before it begins, release what you need to leave behind, and ask for His goodness, wisdom and protection in the weeks ahead. The prayer on this page follows that pattern and can be prayed as it is or in your own words." },
      { q: "Is there a Bible verse for a new month?", a: "Lamentations 3:22–23 says his compassions fail not; they are new every morning. Psalm 65:11 says thou crownest the year with thy goodness, and Isaiah 43:19 says behold, I will do a new thing. Numbers 10:10 shows Israel marking the beginnings of their months before the Lord." },
      { q: "When should I pray a new month prayer?", a: "Most people pray it on the first day of the month, often first thing in the morning. Some churches pray together on the last night of the month, crossing over into the new one in prayer. Any time you pray it, the point is to begin the month with God." },
      { q: "Can I pray a new month prayer for my family?", a: "Yes. Pray over each person by name: their work, school, health and safety in the coming month. You can pray it aloud together at breakfast on the first day of the month." },
    ],
  },

  "good-morning-prayer-for-him": {
    seoTitle: "Good Morning Prayer for Him (Husband or Boyfriend)",
    metaDesc: "A good morning prayer for him, your husband, boyfriend or the man you love, to pray over his day or send to him: strength for his work, peace in his mind and God going before him.",
    context: "A good morning prayer for him is one of the simplest ways to love someone well. Before he leaves the house, before the pressure of the day reaches him, you place him in God's hands. Scripture shows the morning as a time to bring our requests to God and wait for Him to answer (Psalm 5:3). You can pray this prayer quietly on your own, or send it to him so he starts the day knowing someone is praying for him.",
    prayerPoints: [
      "Thank God for him by name, and for one thing you love about him.",
      "Ask God to go before him into his day: his work, his travel and every conversation.",
      "Pray for strength in his body and peace in his mind, especially for anything weighing on him.",
      "Pray Psalm 121:8 over him: the LORD shall preserve thy going out and thy coming in.",
      "Ask God to give him wisdom in his decisions and favour with the people he works with.",
      "Pray for his walk with God, that he would hear God's voice today.",
      "Thank God in advance for bringing him home safely this evening.",
    ],
    howToPray: [
      "Pray before you see him or before you message him, so the day begins with God and not with the to-do list.",
      "Use his name. Pray for the specific things you know are ahead of him today.",
      "If you send it, keep it short: one sentence of prayer and one verse is enough to carry into his morning.",
      "Pray Psalm 90:17 over his work: establish thou the work of our hands upon us.",
      "Ask him later what came of the day, and thank God together for what He did.",
    ],
    faqs: [
      { q: "What is a good morning prayer for him?", a: "A short prayer for the man you love as his day begins: asking God to go before him, give him strength and wisdom, keep him safe and bring him home in peace. The prayer on this page can be prayed for a husband, boyfriend, fiancé or son." },
      { q: "Can I send a good morning prayer to my boyfriend or husband?", a: "Yes. Copy the prayer on this page, or write one or two lines of your own and add a verse such as Psalm 121:8 or Joshua 1:9. Sending it tells him he is not starting the day alone." },
      { q: "What Bible verse can I pray over him in the morning?", a: "Psalm 5:3: My voice shalt thou hear in the morning, O LORD. Psalm 121:8: The LORD shall preserve thy going out and thy coming in. Joshua 1:9: be strong and of a good courage; for the LORD thy God is with thee whithersoever thou goest." },
      { q: "How do I pray for my husband every morning?", a: "Keep it simple and steady. Thank God for him, pray for the day in front of him, cover his health, work and walk with God, and pray for your marriage. A few honest sentences every morning do more than a long prayer once in a while." },
    ],
  },

  "armor-of-god-prayer": {
    seoTitle: "Full Armor of God Prayer: Whole Armour of God",
    metaDesc: "A full armor of God prayer from Ephesians 6: put on the belt of truth, the breastplate of righteousness, the shield of faith, the helmet of salvation and the sword of the Spirit, piece by piece.",
    context: "The full armor of God comes from Ephesians 6:10–18, where Paul tells believers to put on the whole armour of God, that ye may be able to stand against the wiles of the devil. The King James Version, like British English, spells it armour. Each piece is a truth to stand in: truth, righteousness, the gospel of peace, faith, salvation and the word of God, held together by prayer. An armor of God prayer walks through the pieces one by one and puts each one on by faith.",
    prayerPoints: [
      "Put on the belt of truth: having your loins girt about with truth (Ephesians 6:14). Ask God to keep you honest and to expose every lie.",
      "Put on the breastplate of righteousness, trusting the righteousness of Christ, not your own record.",
      "Put on the shoes of the gospel of peace, so you carry peace into every room you enter today.",
      "Take up the shield of faith, wherewith ye shall be able to quench all the fiery darts of the wicked (Ephesians 6:16).",
      "Put on the helmet of salvation, and ask God to guard your mind and your thoughts.",
      "Take the sword of the Spirit, which is the word of God. Speak one verse aloud over today.",
      "Finish praying always with all prayer and supplication in the Spirit (Ephesians 6:18), for yourself and for others.",
    ],
    howToPray: [
      "Read Ephesians 6:10–18 slowly first, so you know what each piece of the armour is.",
      "Pray each piece by name, from the belt of truth to the sword of the Spirit, and say what it means for today.",
      "Stand as you pray if you can. Paul's word is stand, four times in this passage.",
      "Choose the verse you will carry as your sword today and say it aloud.",
      "End by praying for others, as Paul does: praying always for all saints.",
    ],
    faqs: [
      { q: "What is the full armor of God?", a: "It is the list in Ephesians 6:10–18: the belt of truth, the breastplate of righteousness, feet shod with the preparation of the gospel of peace, the shield of faith, the helmet of salvation and the sword of the Spirit, which is the word of God, all held together by prayer." },
      { q: "Is it armor or armour of God?", a: "Both are right. The King James Version and British English write armour; most modern American Bibles write armor. Ephesians 6:11 in the KJV reads: Put on the whole armour of God, that ye may be able to stand against the wiles of the devil." },
      { q: "How do you pray the armor of God?", a: "Go through each piece of the armour in Ephesians 6, asking God to clothe you in it and saying what it means for your day, then speak a verse as your sword and finish by praying for others. The prayer on this page follows that order." },
      { q: "When should I pray the armor of God prayer?", a: "Many people pray it every morning before the day begins, and again before something they know will be hard: a difficult conversation, a temptation, a fearful night. It is a prayer for standing firm, so it fits any moment you feel under attack." },
    ],
  },
};

export function enrich(topic: PrayerTopic): PrayerTopic & { seoTitle?: string } {
  const extra = EXTRA[topic.slug];
  return extra ? { ...topic, ...extra } : topic;
}
