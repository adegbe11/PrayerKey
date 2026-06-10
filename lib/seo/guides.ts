/**
 * Head-term pillar guides — the "back door" content layer (Zapier strategy).
 * Each guide targets a high-volume head keyword and funnels authority down
 * into the programmatic pages: /prayer/[slug], /bible/[book], /bible/verse/[ref].
 */

export interface GuideSection {
  heading: string;
  body: string;                                   // paragraphs split by \n\n
  links?: { href: string; label: string }[];      // internal links rendered as a card row
}

export interface Guide {
  slug: string;
  title: string;
  metaDesc: string;
  keywords: string[];
  readMinutes: number;
  intro: string;
  sections: GuideSection[];
  faqs: { q: string; a: string }[];
}

export const GUIDES: Guide[] = [
  {
    slug: "how-to-pray",
    title: "How to Pray: A Complete Beginner's Guide",
    metaDesc: "Learn how to pray step by step — what to say, how to start, how long to pray, and what the Bible teaches. A practical guide for beginners and returners.",
    keywords: ["how to pray", "how to pray to God", "learn to pray", "prayer for beginners", "how to start praying", "what to say when praying"],
    readMinutes: 8,
    intro: "Prayer is simply talking with God — no special vocabulary, posture, or membership required. Yet most people who want to pray get stuck on the same questions: What do I say? Am I doing it right? Why does it feel awkward? This guide answers those questions practically, using the pattern Jesus Himself taught.",
    sections: [
      {
        heading: "Start smaller than you think",
        body: "The biggest barrier to prayer is the belief that it must be long, eloquent, or emotional to count. Scripture says the opposite — Jesus explicitly warned against 'many words' (Matthew 6:7) and praised a tax collector whose entire prayer was one sentence: 'God, have mercy on me, a sinner.'\n\nStart with two honest minutes. Say what is actually true: what you're grateful for, what you're worried about, what you need. God responds to honesty, not performance.",
      },
      {
        heading: "Use the pattern Jesus taught",
        body: "When the disciples asked 'teach us to pray,' Jesus gave the Lord's Prayer (Matthew 6:9–13) — not as a script to recite mindlessly, but as a structure: honor God first ('hallowed be your name'), align with His purposes ('your will be done'), ask for today's needs ('daily bread'), deal with forgiveness in both directions, and ask for protection.\n\nA simple modern version of that flow: Thank You → I need → Forgive me → Help me. Walk those four movements in your own words and you have prayed the way Jesus taught.",
        links: [
          { href: "/prayer/lords-prayer-meaning", label: "The Lord's Prayer — Meaning Line by Line" },
          { href: "/bible/matthew", label: "Read the Book of Matthew" },
        ],
      },
      {
        heading: "Pick a consistent time and place",
        body: "Jesus rose 'very early in the morning, while it was still dark' to pray (Mark 1:35), and Daniel prayed three set times a day even under threat of the lions' den. Consistency beats intensity — a fixed time and place trains your soul the way a gym schedule trains your body.\n\nMorning works best for most people because it claims the day before the day claims you. But the best time to pray is the time you will actually keep.",
        links: [
          { href: "/prayer/morning", label: "Morning Prayer" },
          { href: "/prayer/night", label: "Night Prayer" },
          { href: "/prayer/daily", label: "Daily Prayer" },
        ],
      },
      {
        heading: "Pray Scripture when your own words run out",
        body: "For three thousand years, believers have borrowed the Bible's own prayers — especially the Psalms, which voice every emotion from euphoria to despair. Praying Scripture anchors your prayer in God's promises rather than your moods.\n\nFeeling anxious? Pray Philippians 4:6–7. Need protection? Pray Psalm 91. Carrying guilt? Pray Psalm 51. The Word gives you words.",
        links: [
          { href: "/bible/psalms", label: "Explore the Book of Psalms" },
          { href: "/prayer/prayer-based-on-psalm-23", label: "Praying Psalm 23" },
          { href: "/prayer/prayer-when-you-dont-know-what-to-pray", label: "When You Don't Know What to Pray" },
        ],
      },
      {
        heading: "Expect resistance — and push through it",
        body: "Distraction during prayer is universal, not a personal defect. Your mind will wander to your to-do list; bring it back without self-condemnation, as many times as it takes. Some believers keep a notepad beside them — when a stray task interrupts, they write it down and return to prayer.\n\nDryness is also normal. Some days prayer feels electric; most days it feels ordinary. Faithfulness in the ordinary days is what builds a praying life — feelings follow practice far more often than practice follows feelings.",
      },
    ],
    faqs: [
      { q: "What should I say when I pray?", a: "Say what is true. Thank God for something specific, tell Him what you're facing, ask for what you need, and be honest about your failures. Jesus' model prayer (Matthew 6:9–13) covers honor, needs, forgiveness, and protection in about 60 words — yours can be even shorter." },
      { q: "Do I need to pray out loud?", a: "No — God hears silent prayer perfectly (Hannah prayed 'in her heart' and was answered, 1 Samuel 1:13). That said, praying aloud helps concentration and is worth trying when you're alone. Many people find whispered prayer keeps the mind from wandering." },
      { q: "How long should I pray each day?", a: "Start with 5 minutes you'll actually keep rather than 30 you'll abandon. Consistency matters more than duration — and most lifelong pray-ers report their prayer time grew naturally once the daily habit took root." },
      { q: "Does God hear me if I'm not religious?", a: "Yes. 'Everyone who calls on the name of the Lord will be saved' (Romans 10:13) — the calling comes before the qualifying. Some of the Bible's most-honored prayers came from outsiders: a Roman centurion, a Canaanite mother, a thief on a cross." },
      { q: "Why do my prayers feel like they hit the ceiling?", a: "Feelings are not the receipt of prayer — God's promise is. 'This is the confidence we have: if we ask anything according to his will, he hears us' (1 John 5:14). Keep praying through the dry season; every mature believer has walked through them." },
    ],
  },
  {
    slug: "morning-prayers",
    title: "Morning Prayers: How to Start Every Day with God",
    metaDesc: "The complete guide to morning prayer — why it works, what to pray, and short powerful morning prayers for every day of the week.",
    keywords: ["morning prayer", "morning prayers", "prayer to start the day", "short morning prayer", "powerful morning prayer", "daily morning prayer"],
    readMinutes: 7,
    intro: "What you do in the first fifteen minutes of the morning sets the trajectory of the entire day. Scripture's praying people knew this: David prayed 'in the morning, Lord, you hear my voice' (Psalm 5:3), and Jesus rose before sunrise to pray (Mark 1:35). This guide gives you everything you need to build a morning prayer habit that survives Monday.",
    sections: [
      {
        heading: "Why morning prayer changes the whole day",
        body: "Praying first is a statement of order: God gets the first word before email, news, and anxiety get theirs. Lamentations 3:22–23 says God's mercies are 'new every morning' — morning prayer is simply showing up to collect them.\n\nPractically, the morning mind is also the most shapeable. The thoughts you rehearse before 8am echo until midnight, which is exactly why Psalm 143:8 prays: 'Let the morning bring me word of your unfailing love.'",
        links: [
          { href: "/bible/lamentations", label: "The Book of Lamentations" },
          { href: "/bible/verse/psalms-143-8", label: "Psalm 143:8" },
        ],
      },
      {
        heading: "A simple 5-minute morning prayer structure",
        body: "Minute 1 — Thank God for three specifics: the night's rest, the people under your roof, the work ahead.\n\nMinutes 2–3 — Walk through today's calendar with God. Name the meeting, the deadline, the difficult conversation, and ask for what each one requires.\n\nMinute 4 — Pray for one other person: a family member, a colleague, someone struggling.\n\nMinute 5 — Put on protection and peace for the day, then close with the Lord's Prayer as your frame.",
        links: [
          { href: "/prayer/morning", label: "Full Morning Prayer" },
          { href: "/prayer/prayer-for-a-good-day", label: "Prayer for a Good Day" },
          { href: "/prayer/protection", label: "Prayer for Protection" },
        ],
      },
      {
        heading: "Morning prayers for each day of the week",
        body: "Many believers anchor each weekday with a theme — it keeps the habit fresh and ensures everything important gets prayed for weekly: Monday for the week ahead, Tuesday for family, Wednesday for work, Thursday for friends and church, Friday with thanksgiving, Saturday for rest, Sunday for worship.",
        links: [
          { href: "/prayer/monday-morning-prayer", label: "Monday Morning Prayer" },
          { href: "/prayer/tuesday-morning-prayer", label: "Tuesday Morning Prayer" },
          { href: "/prayer/friday-morning-prayer", label: "Friday Morning Prayer" },
          { href: "/prayer/saturday-morning-prayer", label: "Saturday Morning Prayer" },
          { href: "/prayer/sunday-morning-prayer", label: "Sunday Morning Prayer" },
        ],
      },
      {
        heading: "What to do when you oversleep or miss a day",
        body: "Missing a morning doesn't cancel the habit — quitting does. Grace applies to prayer schedules too. If the morning escaped you, pray at noon, or pray one sentence at a red light. The goal is a praying LIFE, not a perfect streak.\n\nIf oversleeping is chronic, move the meeting: pray in the shower, on the commute, or while the kettle boils. God is remarkably flexible about venue.",
        links: [
          { href: "/prayer/noon-prayer", label: "Noon Prayer" },
          { href: "/prayer/prayer-before-work", label: "Prayer Before Work" },
        ],
      },
    ],
    faqs: [
      { q: "What is a good short morning prayer?", a: "'Father, thank You for this new day and Your new mercies. I give You today — my work, my words, my worries. Guide my steps, guard my heart, and use me for good. In Jesus' name, Amen.' Thirty seconds, and the day is anchored." },
      { q: "Should I pray before or after reading the Bible?", a: "Both patterns work, but many find a short prayer first ('open my eyes,' Psalm 119:18), then Scripture, then prayer shaped by what they read, is the richest order. The Word gives your prayer vocabulary and direction." },
      { q: "Is 5 minutes of morning prayer enough?", a: "Five faithful minutes beat thirty abandoned ones. Start where you can sustain; depth comes with consistency. Many of history's great pray-ers — Müller, Wesley, Daniel — built long prayer lives on short beginnings." },
    ],
  },
  {
    slug: "bible-verses-about-healing",
    title: "25 Bible Verses About Healing (and How to Pray Them)",
    metaDesc: "The most powerful Bible verses about healing — for sickness, surgery, emotional wounds, and recovery — with guidance on praying each one over your situation.",
    keywords: ["bible verses about healing", "healing scriptures", "healing bible verses", "scriptures on healing", "verses for the sick", "healing verses KJV NIV"],
    readMinutes: 9,
    intro: "When sickness enters a family, believers reach for two things: a doctor and a promise. Scripture is full of healing promises — God introduces Himself as 'the Lord who heals you' (Exodus 15:26), and the Gospels record Jesus healing 'every disease and sickness' (Matthew 9:35). These are the verses generations have prayed at bedsides, with guidance on how to pray each one.",
    sections: [
      {
        heading: "The foundational healing verses",
        body: "Three verses anchor every healing prayer tradition:\n\nIsaiah 53:5 — 'By his wounds we are healed.' The prophecy of the suffering Messiah, quoted in 1 Peter 2:24, ties healing to the cross itself.\n\nJeremiah 30:17 — 'I will restore you to health and heal your wounds, declares the Lord.' A direct first-person promise.\n\nPsalm 103:2–3 — 'Praise the Lord… who forgives all your sins and heals all your diseases.' Forgiveness and healing named together as God's twin benefits.",
        links: [
          { href: "/bible/verse/isaiah-53-5", label: "Isaiah 53:5 — Meaning & Prayer" },
          { href: "/bible/verse/jeremiah-30-17", label: "Jeremiah 30:17 — Meaning & Prayer" },
          { href: "/bible/verse/psalms-103-2", label: "Psalm 103:2–3 — Meaning & Prayer" },
        ],
      },
      {
        heading: "Verses for specific situations",
        body: "Before surgery: Psalm 91:11 — 'He will command his angels concerning you' — has steadied countless patients being wheeled into theatre.\n\nFor a sick child or loved one: Mark 5:36 — 'Don't be afraid; just believe' — Jesus' words to a father whose daughter lay dying.\n\nFor chronic illness: 2 Corinthians 12:9 — 'My grace is sufficient for you' — the promise for the long road.\n\nFor emotional and mental healing: Psalm 147:3 — 'He heals the brokenhearted and binds up their wounds.'",
        links: [
          { href: "/prayer/surgery", label: "Prayer Before Surgery" },
          { href: "/prayer/sick-loved-one", label: "Prayer for a Sick Loved One" },
          { href: "/prayer/chronic-pain", label: "Prayer for Chronic Pain" },
          { href: "/prayer/mental-healing", label: "Prayer for Emotional Healing" },
        ],
      },
      {
        heading: "How to pray a healing scripture",
        body: "Praying Scripture is more than reading it — it's presenting God's own words back to Him over a specific situation, the way Hezekiah spread the enemy's letter before the Lord (2 Kings 19:14).\n\nThe pattern: Read the verse aloud. Personalize it — replace 'you' with the sick person's name: 'By Your wounds, JAMES is healed.' Thank God that the promise is already true in Christ. Then keep standing on it daily, pairing prayer with every medical means available.",
        links: [
          { href: "/prayer/healing", label: "Complete Prayer for Healing" },
          { href: "/prayer/healing-for-cancer", label: "Prayer for Healing from Cancer" },
          { href: "/prayer/recovery", label: "Prayer for Recovery" },
        ],
      },
      {
        heading: "Where to read more healing passages",
        body: "The Gospels are the densest healing territory in Scripture — Matthew 8–9 alone records ten healings. The Psalms supply the prayers of the sick themselves (Psalm 6, 30, 41, 103). James 5:14–16 gives the church's standing instructions for praying over the sick, and Exodus 15:26 carries God's covenant name of healing: Jehovah Rapha.",
        links: [
          { href: "/bible/matthew", label: "The Book of Matthew" },
          { href: "/bible/psalms", label: "The Book of Psalms" },
          { href: "/bible/james", label: "The Book of James" },
          { href: "/bible/exodus", label: "The Book of Exodus" },
        ],
      },
    ],
    faqs: [
      { q: "What is the most powerful healing verse in the Bible?", a: "Isaiah 53:5 — 'by his wounds we are healed' — is widely considered the foundational healing promise because it roots healing in Christ's finished work, and the New Testament itself quotes it that way (1 Peter 2:24). Jeremiah 30:17 and Psalm 103:2–3 stand beside it." },
      { q: "Can I claim healing verses for someone else?", a: "Yes — interceding with Scripture for the sick is the explicit New Testament pattern (James 5:14–16), and Jesus repeatedly healed people based on someone ELSE'S faith: the centurion's servant, the paralytic's friends, Jairus' daughter." },
      { q: "Should I stop treatment and rely on healing verses?", a: "No — never. Scripture honors medicine (Luke was a physician; Jesus said the sick need a doctor, Matthew 9:12). Pray the promises AND keep every appointment. God heals through both, and they have never been in competition." },
    ],
  },
  {
    slug: "prayers-for-anxiety-and-stress",
    title: "Prayers for Anxiety and Stress: Finding Peace When Your Mind Won't Stop",
    metaDesc: "How to pray when anxiety hits — practical scripture-based prayers for panic, overthinking, work stress, and sleepless nights, with the Philippians 4 method explained.",
    keywords: ["prayer for anxiety", "prayer for stress", "prayer for panic attacks", "prayer to calm the mind", "prayer for worry", "anxiety bible verses"],
    readMinutes: 8,
    intro: "Anxiety is the most common reason people search for prayer at 2am. The racing mind, the tight chest, the what-ifs on loop — Scripture takes them all seriously and answers with a concrete method, not a platitude. Philippians 4:6–7 is a practical exchange: bring God specific requests with thanksgiving, receive peace that guards the heart and mind 'like a garrison.' Here's how to actually do it.",
    sections: [
      {
        heading: "The Philippians 4 exchange, step by step",
        body: "Paul's instruction has four moving parts, and skipping any of them weakens the whole:\n\n1. 'Do not be anxious about ANYTHING' — no worry is too small or too embarrassing to pray.\n\n2. 'In every situation, by prayer and petition' — petition means specifics. Name the actual meeting, bill, symptom, or person.\n\n3. 'WITH THANKSGIVING' — the step everyone skips. Thank God for two or three things mid-anxiety; gratitude breaks worry's tunnel vision.\n\n4. 'Present your requests to God' — hand over the outcome explicitly, like luggage at check-in. It travels with Him now.",
        links: [
          { href: "/prayer/prayer-based-on-philippians-4", label: "Praying Philippians 4" },
          { href: "/prayer/anxiety", label: "Full Prayer for Anxiety" },
          { href: "/bible/philippians", label: "The Book of Philippians" },
        ],
      },
      {
        heading: "Prayers for the moment panic hits",
        body: "In acute anxiety, long prayers are impossible — use breath prayers: one phrase on the inhale, one on the exhale. 'You are with me' (in) / 'I will not fear' (out), from Psalm 23:4. Or simply the name 'Jesus' — the shortest prayer in the world and often the only one available.\n\nPair it with the body: slow the exhale, plant both feet, name five things you can see. God built the calming reflex into your nervous system; prayer and breathing work it together.",
        links: [
          { href: "/prayer/panic-attacks", label: "Prayer for Panic Attacks" },
          { href: "/prayer/fear", label: "Prayer Against Fear" },
          { href: "/bible/verse/psalms-23-4", label: "Psalm 23:4 — Meaning & Prayer" },
        ],
      },
      {
        heading: "For the anxiety that comes back every night",
        body: "Nighttime anxiety has its own scripture: 'In peace I will lie down and sleep, for you alone, Lord, make me dwell in safety' (Psalm 4:8). Pray it as you lie down, then formally hand tomorrow to God: 'These are Yours until morning.'\n\nIf you wake at 3am, don't fight the waking — use it. Pray briefly for one person, remind yourself that 'he who watches over you will not slumber' (Psalm 121:4), and let the night shift belong to the One who doesn't need sleep.",
        links: [
          { href: "/prayer/anxiety-at-night", label: "Prayer for Anxiety at Night" },
          { href: "/prayer/sleep", label: "Prayer for Sleep" },
          { href: "/prayer/prayer-against-nightmares", label: "Prayer Against Nightmares" },
        ],
      },
      {
        heading: "When to add professional help",
        body: "Prayer and therapy are allies. If anxiety is disrupting work, relationships, or health — or if panic attacks are frequent — see a doctor or counselor while you keep praying. Elijah's burnout was treated with food, sleep, AND God's voice (1 Kings 19); God's care usually arrives through multiple channels at once.\n\nTaking medication for anxiety is no more a faith failure than wearing glasses. Use every good gift; credit the same Giver.",
        links: [
          { href: "/prayer/stress", label: "Prayer for Stress" },
          { href: "/prayer/burnout", label: "Prayer for Burnout" },
          { href: "/prayer/mental-health", label: "Prayer for Mental Health" },
        ],
      },
    ],
    faqs: [
      { q: "What is the best prayer for anxiety?", a: "The Philippians 4:6–7 method prayed over your specific worry: name it precisely, thank God for two or three real things, hand over the outcome, and receive the promised peace 'that transcends understanding.' For acute moments, breath prayers from Psalm 23 work when nothing else will." },
      { q: "Why do I still feel anxious after praying?", a: "Peace often arrives gradually, like settling water — and anxiety frequently returns, which is why 1 Peter 5:7's 'casting' is a repeated practice, not a one-time event. Re-cast each time it comes back. If anxiety persists at a disruptive level, add professional help to the prayer; both are God's provision." },
      { q: "Can God heal an anxiety disorder completely?", a: "Yes — testimonies of complete freedom are real, and so are testimonies of grace within ongoing management, like Paul's thorn (2 Corinthians 12:9). Pray boldly for full healing while using every means of grace: Scripture, community, counseling, and medical care where needed." },
    ],
  },
  {
    slug: "financial-breakthrough-prayers",
    title: "Financial Breakthrough Prayers: What the Bible Actually Teaches About Praying for Money",
    metaDesc: "How to pray for financial breakthrough biblically — prayers for debt, provision, business, and open doors, plus what blocks breakthrough and what Scripture really promises.",
    keywords: ["financial breakthrough prayer", "prayer for money", "prayer for finances", "prayer for debt", "prayer for provision", "prayer for business breakthrough"],
    readMinutes: 8,
    intro: "Money is the second most common subject people pray about — and the one they feel guiltiest praying about. They shouldn't: Jesus put 'give us this day our daily bread' in the middle of the Lord's Prayer, and Scripture addresses money over 2,000 times. This guide covers how to pray for financial breakthrough the way the Bible actually teaches — without the hype and without the shame.",
    sections: [
      {
        heading: "What God actually promises about provision",
        body: "Philippians 4:19 is the anchor: 'My God will meet all your NEEDS according to the riches of his glory in Christ Jesus.' Note the word — needs, not whims. Scripture promises provision (Matthew 6:31–33), power to produce wealth (Deuteronomy 8:18), and open floodgates linked to generosity (Malachi 3:10).\n\nIt does not promise effortless luxury — Paul knew both 'plenty and want' (Philippians 4:12). Biblical provision prayer is confident about needs and surrendered about timing and amounts.",
        links: [
          { href: "/bible/verse/philippians-4-19", label: "Philippians 4:19 — Meaning & Prayer" },
          { href: "/bible/verse/malachi-3-10", label: "Malachi 3:10 — Meaning & Prayer" },
          { href: "/prayer/provision", label: "Prayer for Provision" },
        ],
      },
      {
        heading: "Praying about debt",
        body: "Debt prayer works best with full honesty: bring the actual figures before God the way Hezekiah spread the threatening letter before the Lord (2 Kings 19:14). Then pray for three things — supernatural provision, practical strategy, and the discipline to execute it.\n\n2 Kings 4 is the debt-deliverance template: a widow facing creditors is asked 'what do you HAVE?' God multiplied her one jar of oil through her own action of gathering vessels and pouring. Expect breakthrough to flow through something already in your hands.",
        links: [
          { href: "/prayer/debt", label: "Prayer for Debt" },
          { href: "/prayer/debt-freedom", label: "Prayer for Debt Freedom" },
          { href: "/prayer/prayer-for-debt-cancellation", label: "Prayer for Debt Cancellation" },
        ],
      },
      {
        heading: "Praying for business and career breakthrough",
        body: "Business prayer in Scripture centers on favor, wisdom, and integrity. Pray for divine connections ('a man's gift makes room for him,' Proverbs 18:16), wise decisions (James 1:5 — wisdom for the asking), and protection over the work of your hands (Psalm 90:17).\n\nDedicate the business to God explicitly — Proverbs 16:3: 'Commit to the Lord whatever you do, and he will establish your plans.' Many believers re-commit their work every Monday morning.",
        links: [
          { href: "/prayer/business", label: "Prayer for Business" },
          { href: "/prayer/business-breakthrough", label: "Business Breakthrough Prayer" },
          { href: "/prayer/prayer-for-work-promotion", label: "Prayer for Promotion" },
          { href: "/prayer/job", label: "Prayer for a Job" },
        ],
      },
      {
        heading: "What blocks financial breakthrough",
        body: "Scripture names several honest possibilities worth praying through: closed hands (Malachi 3:10 and Proverbs 19:17 tie generosity to provision), no plan (Proverbs 21:5 — 'the plans of the diligent lead to profit'), and wrong motives (James 4:3 — 'you ask with wrong motives, to spend on your pleasures').\n\nSometimes nothing is blocking it — provision seasons vary, and delay is preparation. Joseph's path to stewarding Egypt's economy ran through thirteen unfair years. Ask God to show you which applies, and refuse condemnation while you wait.",
        links: [
          { href: "/prayer/financial-breakthrough", label: "Financial Breakthrough Prayer" },
          { href: "/prayer/breakthrough", label: "Prayer for Breakthrough" },
          { href: "/prayer/tithing", label: "Prayer About Tithing & Giving" },
        ],
      },
    ],
    faqs: [
      { q: "Is it wrong to pray for money?", a: "No — Jesus taught us to pray for daily bread, and Philippians 4:6 says to present requests 'in EVERY situation.' What Scripture warns against is loving money (1 Timothy 6:10), not praying about it. Bring the rent, the debt, and the school fees to your Father; He already knows you need them (Matthew 6:32)." },
      { q: "How do I pray for an urgent financial miracle?", a: "Pray specifically (the exact amount and deadline), recall God's past provision aloud, act on whatever is in your hands like the widow's oil (2 Kings 4), and tell a trusted believer to stand with you — 'if two of you agree' (Matthew 18:19). Urgency in Scripture often precedes the most memorable provision." },
      { q: "Does tithing guarantee financial breakthrough?", a: "Malachi 3:10 attaches a genuine promise — and God's only 'test me' invitation — to wholehearted giving. But it's covenant generosity, not a vending machine. Give faithfully, manage diligently, pray persistently: Scripture treats all three as one braided practice." },
    ],
  },
  {
    slug: "prayers-for-marriage-and-family",
    title: "Prayers for Marriage and Family: Protecting What Matters Most",
    metaDesc: "Scripture-based prayers for your marriage, children, and home — daily couple prayers, prayers for a struggling marriage, and how to pray for your family by name.",
    keywords: ["prayer for marriage", "prayer for family", "prayer for my husband", "prayer for my wife", "prayer for children", "family prayer", "couple prayer"],
    readMinutes: 8,
    intro: "No institution is prayed over more — or attacked more — than the family. Scripture treats marriage as God's own design (Genesis 2:24) and children as His heritage (Psalm 127:3), which makes prayer the family's first line of maintenance, not its last resort. This guide covers praying for a spouse, praying as a couple, covering children, and rebuilding when the marriage is struggling.",
    sections: [
      {
        heading: "Praying for your spouse (especially when it's hard)",
        body: "The rule that changes everything: bless before you complain. Praying blessing over your spouse — their work, health, walk with God, joys — re-softens your own heart toward them, which is usually half the battle.\n\nWhen there's conflict, pray your own confession first. Even owning 10% of the problem before God dissolves the self-righteousness that keeps standoffs alive. Then ask for their good as sincerely as you ask for your own.",
        links: [
          { href: "/prayer/husband", label: "Prayer for My Husband" },
          { href: "/prayer/wife", label: "Prayer for My Wife" },
          { href: "/prayer/marriage", label: "Prayer for Marriage" },
        ],
      },
      {
        heading: "Praying together as a couple",
        body: "Couples who pray together report dramatically stronger marriages — and most couples who don't simply never started. Start embarrassingly small: one sentence each at bedtime. 'Thank You for today. Bless her sleep. Help us tomorrow.'\n\nEcclesiastes 4:12 calls a marriage with God a 'cord of three strands… not quickly broken.' Praying together is how the third strand gets woven in — it's nearly impossible to stay cold toward someone you hear praying for you.",
        links: [
          { href: "/prayer/prayer-for-newlyweds", label: "Prayer for Newlyweds" },
          { href: "/bible/verse/ecclesiastes-4-12", label: "Ecclesiastes 4:12 — Meaning" },
          { href: "/bible/song-of-solomon", label: "The Song of Solomon" },
        ],
      },
      {
        heading: "Covering your children in prayer",
        body: "Pray for children by name and by need — protection on the way to school, friendships, character, and their own faith taking root. Isaiah 54:13 is the parent's anchor promise: 'All your children will be taught by the Lord, and great will be their peace.'\n\nFor adult or wandering children, take the long view of the prodigal's father (Luke 15): keep love warm, keep the road watched, keep praying. Augustine's mother prayed seventeen years; her son became one of the faith's greatest voices.",
        links: [
          { href: "/prayer/children", label: "Prayer for My Children" },
          { href: "/prayer/prayer-for-son", label: "Prayer for My Son" },
          { href: "/prayer/prayer-for-daughter", label: "Prayer for My Daughter" },
          { href: "/prayer/wayward-child", label: "Prayer for a Wayward Child" },
        ],
      },
      {
        heading: "When the marriage is in real trouble",
        body: "A struggling marriage needs prayer AND practical help — godly counsel (Proverbs 11:14), honest conversation, sometimes professional counseling. Pray for three specific things: softened hearts (both of them), truthful gentle words, and the death of scorekeeping.\n\nGod restores marriages that looked finished — Hosea's whole book is that story. But restoration is usually a slow miracle measured in months of small mercies, not one dramatic moment. Keep watering it.",
        links: [
          { href: "/prayer/marriage-restoration", label: "Prayer for Marriage Restoration" },
          { href: "/prayer/prayer-for-separated-couple", label: "Prayer for a Separated Couple" },
          { href: "/prayer/prayer-for-unhappy-marriage", label: "Prayer for an Unhappy Marriage" },
          { href: "/bible/hosea", label: "The Book of Hosea" },
        ],
      },
    ],
    faqs: [
      { q: "How do I pray for my marriage daily?", a: "A simple daily trio: bless your spouse by name (work, health, faith), pray over your communication for the day, and thank God for one specific thing about them — especially on the days you have to hunt for it. Two minutes, compounding interest." },
      { q: "Can prayer save a marriage headed for divorce?", a: "Many restored couples testify yes — prayer changes the pray-er first, softening the pride and resentment that drive separation, and invites God's work beyond what counseling alone achieves. Pair persistent prayer with professional help; restored marriages usually used both." },
      { q: "What should I pray over my home?", a: "Pray Joshua 24:15 as the household banner — 'as for me and my house, we will serve the Lord' — plus peace over the atmosphere (Luke 10:5), protection over all who sleep there (Psalm 91), and that your home would be a place of refuge for others." },
    ],
  },
  {
    slug: "how-to-read-the-bible",
    title: "How to Read the Bible: A Beginner's Roadmap Through All 66 Books",
    metaDesc: "Where to start reading the Bible, how the 66 books fit together, which book to read first, and a simple plan that actually works for beginners.",
    keywords: ["how to read the bible", "where to start reading the bible", "bible reading plan", "books of the bible explained", "bible for beginners", "what book of the bible to read first"],
    readMinutes: 10,
    intro: "The Bible is not one book — it's a library of 66 books written across 1,500 years: history, poetry, prophecy, letters, and eyewitness biography. Starting at page one and pushing through often shipwrecks in Leviticus. This roadmap shows you how the library is organized, where to actually start, and how to build a reading habit that lasts.",
    sections: [
      {
        heading: "How the 66 books fit together",
        body: "The Old Testament (39 books) tells one story in four movements: the Law (Genesis–Deuteronomy) — creation, fall, and covenant; History (Joshua–Esther) — Israel's rise, fall, and return; Wisdom & Poetry (Job–Song of Solomon) — the prayer book and life manual; and the Prophets (Isaiah–Malachi) — God's messengers pointing toward Messiah.\n\nThe New Testament (27 books) completes it: the Gospels (Matthew–John) — four portraits of Jesus; Acts — the church's explosive birth; the Letters (Romans–Jude) — apostles coaching young churches; and Revelation — the finale where everything broken gets remade.",
        links: [
          { href: "/bible/genesis", label: "Start of the Story: Genesis" },
          { href: "/bible/isaiah", label: "Greatest of the Prophets: Isaiah" },
          { href: "/bible/revelation", label: "The Finale: Revelation" },
        ],
      },
      {
        heading: "Where to actually start (not Genesis)",
        body: "Start with the Gospel of John — written explicitly so readers would believe (John 20:31), and the clearest introduction to who Jesus is. Read a chapter a day; it's 21 days.\n\nThen Psalms alongside whatever you read next — one psalm a day teaches you to pray while you learn to read. Then Genesis and Exodus for the foundation story, Proverbs for daily wisdom, and Romans for the faith explained systematically. That five-book sequence gives you the Bible's skeleton in about three months.",
        links: [
          { href: "/bible/john", label: "The Gospel of John" },
          { href: "/bible/psalms", label: "The Book of Psalms" },
          { href: "/bible/proverbs", label: "The Book of Proverbs" },
          { href: "/bible/romans", label: "The Book of Romans" },
        ],
      },
      {
        heading: "A method that makes it stick",
        body: "Reading without a method evaporates by Friday. The simplest one that works is S.O.A.P.: Scripture (read the passage), Observation (what stands out? one sentence), Application (what does this change today? one sentence), Prayer (pray the passage back to God).\n\nKeep the unit small — a chapter or even a paragraph. Depth beats distance: one verse that reaches your actual life outweighs three chapters skimmed. And read at the same time daily; habit carries you on the days motivation doesn't.",
        links: [
          { href: "/prayer/prayer-before-eating", label: "Prayer Before Study" },
          { href: "/prayer/wisdom", label: "Prayer for Wisdom" },
          { href: "/bible", label: "Search Any Verse or Topic" },
        ],
      },
      {
        heading: "What to do with the hard parts",
        body: "Every reader hits passages that confuse or trouble them — genealogies, ancient laws, violent histories. Three rules keep you moving: read the confusing in light of the clear (the Bible's plain teachings interpret its puzzles, not vice versa); read everything in context (who wrote it, to whom, why); and keep a question list instead of stalling — many early questions answer themselves a few books later.\n\nAnd pray before you read. The Bible's own request for readers: 'Open my eyes that I may see wonderful things in your law' (Psalm 119:18).",
        links: [
          { href: "/bible/verse/psalms-119-18", label: "Psalm 119:18 — Meaning" },
          { href: "/bible/leviticus", label: "Understanding Leviticus" },
          { href: "/bible/job", label: "Understanding Job" },
        ],
      },
    ],
    faqs: [
      { q: "Which book of the Bible should I read first?", a: "The Gospel of John. It was written specifically so readers would understand and believe in Jesus (John 20:31), it requires no prior background, and at 21 chapters it's a chapter-a-day for three weeks. Follow it with Psalms, Genesis, Proverbs, and Romans." },
      { q: "How long does it take to read the whole Bible?", a: "At 15 minutes a day, about a year — the pace of most annual reading plans. But completion isn't the first goal; comprehension is. Many readers spend their first year on a dozen key books read well rather than 66 books read at a sprint." },
      { q: "What's the difference between the Old and New Testament?", a: "The Old Testament (39 books) is the story of God's covenant with Israel pointing toward a coming Messiah; the New Testament (27 books) records that Messiah's arrival — Jesus — and the worldwide family His rescue created. One story, two acts; neither makes full sense without the other." },
      { q: "Which Bible translation should a beginner use?", a: "A readable modern translation: NIV and NLT are the most beginner-friendly while staying accurate; ESV sits slightly more formal. The best translation is the one you'll actually read — you can compare translations verse by verse as you grow." },
    ],
  },
  {
    slug: "night-prayers",
    title: "Night Prayers: How to End the Day and Actually Sleep in Peace",
    metaDesc: "Bedtime prayers for adults and kids, prayers for when you can't sleep, and the biblical pattern for closing the day with God — Psalm 4:8 explained.",
    keywords: ["night prayer", "bedtime prayer", "prayer before sleep", "evening prayer", "prayer for sleep", "night prayer for protection", "prayer when you can't sleep"],
    readMinutes: 6,
    intro: "How you end the day determines how you start the next one. Scripture gives night a specific prayer posture: review the day with gratitude, release its weight, and lie down under guard — 'In peace I will lie down and sleep, for you alone, Lord, make me dwell in safety' (Psalm 4:8). This guide gives you the full nighttime toolkit.",
    sections: [
      {
        heading: "The 4-step nighttime prayer pattern",
        body: "1. Review with gratitude — thank God for three specific moments from today, however ordinary. Gratitude is the mind's natural off-ramp from rumination.\n\n2. Release the unfinished — name tomorrow's worries and formally hand them over: 'These are Yours until morning.' Unfinished business is the engine of midnight overthinking.\n\n3. Reconcile — quick accounts with God (1 John 1:9) and people ('do not let the sun go down while you are still angry,' Ephesians 4:26).\n\n4. Rest under guard — pray Psalm 4:8 aloud as the lights go out.",
        links: [
          { href: "/prayer/night", label: "Full Night Prayer" },
          { href: "/prayer/bedtime-prayer-for-adults", label: "Bedtime Prayer for Adults" },
          { href: "/bible/verse/psalms-4-8", label: "Psalm 4:8 — Meaning & Prayer" },
        ],
      },
      {
        heading: "When you can't fall asleep",
        body: "Racing thoughts respond better to redirection than suppression. Give the mind one verse to hold and slow it down: 'Be still and know that I am God' (Psalm 46:10), repeated slower each round, trimmed shorter each time — Be still and know… Be still… Be.\n\nIf a real worry keeps surfacing, keep paper by the bed: write it down, tell God it's His until morning, return to the verse. The written list closes the mental loop that keeps re-opening.",
        links: [
          { href: "/prayer/sleep", label: "Prayer for Sleep" },
          { href: "/prayer/anxiety-at-night", label: "Prayer for Anxiety at Night" },
          { href: "/bible/verse/psalms-46-10", label: "Psalm 46:10 — Meaning & Prayer" },
        ],
      },
      {
        heading: "Night prayers with children",
        body: "Bedtime is the easiest prayer habit to build into a family — children are captive, calm, and surprisingly theological at 8pm. Keep it short and participatory: each person says one thank-you and one ask. Close with a blessing spoken over them by name — Numbers 6:24–26 ('the Lord bless you and keep you') has been the parental goodnight for three thousand years.",
        links: [
          { href: "/prayer/bedtime-prayer-for-kids", label: "Bedtime Prayer for Kids" },
          { href: "/prayer/children", label: "Prayer for My Children" },
          { href: "/bible/verse/numbers-6-24", label: "Numbers 6:24 — The Blessing" },
        ],
      },
      {
        heading: "Praying through the midnight hours",
        body: "Scripture treats the late watches as prime prayer territory, not wasted insomnia: David meditated 'through the watches of the night' (Psalm 63:6), and Paul and Silas prayed at midnight until the prison shook (Acts 16:25).\n\nIf you're awake anyway — nursing a baby, working a shift, or simply sleepless — convert the hours: pray for your household by name, for one person who's struggling, for your country. Midnight prayer has a long history of answers.",
        links: [
          { href: "/prayer/midnight-prayer", label: "Midnight Prayer" },
          { href: "/prayer/night-shift-prayer", label: "Night Shift Prayer" },
          { href: "/bible/acts", label: "The Book of Acts" },
        ],
      },
    ],
    faqs: [
      { q: "What is a good prayer to say before bed?", a: "'Father, thank You for today — for [three specifics]. I hand You tomorrow and everything I can't control. Forgive me where I fell short. Watch over my home and everyone I love. In peace I will lie down and sleep, for You alone make me dwell in safety. Amen.' (built on Psalm 4:8)." },
      { q: "Why does the Bible say not to let the sun go down on your anger?", a: "Ephesians 4:26–27 warns that overnight anger 'gives the devil a foothold' — resentment hardens while you sleep. The nightly practice: release the offense to God before bed, even if full reconciliation has to wait for daylight. You'll sleep better and fight fairer tomorrow." },
      { q: "Is it good to pray at midnight?", a: "Midnight prayer has rich biblical precedent — Paul and Silas at midnight (Acts 16:25), David in the night watches (Psalm 63:6), Jesus praying all night before choosing the twelve (Luke 6:12). If you're awake, the hours are usable: quiet, undistracted, and historically associated with breakthrough." },
    ],
  },
];

export const GUIDE_MAP = new Map(GUIDES.map((g) => [g.slug, g]));
export const GUIDE_SLUGS = GUIDES.map((g) => g.slug);
