/**
 * Unique per-book content for the 66 Bible book hub pages (/bible/[book])
 * and for enriching the 30,919 verse pages with static, non-API context.
 * Every overview is hand-written and unique — no template boilerplate.
 */

export interface BookContent {
  slug: string;
  overview: string;            // unique 50–90 word introduction
  themes: string[];            // 4–6 core themes
  keyVerses: { ref: string; display: string; note: string }[];
  relatedPrayers: string[];    // valid prayer topic slugs
}

export const BOOK_CONTENT: Record<string, BookContent> = {
  // ── OLD TESTAMENT ─────────────────────────────────────────────
  "genesis": {
    slug: "genesis",
    overview: "Genesis is the book of beginnings — creation, the fall, the flood, and the call of Abraham. It answers the oldest questions: where we came from, why the world is broken, and how God begins His rescue plan through one family. Every major theme in the Bible is planted here as a seed.",
    themes: ["Creation", "Covenant", "Faith & obedience", "God's promises", "New beginnings"],
    keyVerses: [
      { ref: "genesis-1-1",  display: "Genesis 1:1",  note: "The Bible's opening declaration: God created everything." },
      { ref: "genesis-12-2", display: "Genesis 12:2", note: "God's covenant promise to bless Abraham and all nations." },
      { ref: "genesis-50-20", display: "Genesis 50:20", note: "Joseph's testimony: what was meant for evil, God used for good." },
    ],
    relatedPrayers: ["new-beginnings", "family", "blessing", "purpose", "trust"],
  },
  "exodus": {
    slug: "exodus",
    overview: "Exodus tells the defining rescue story of the Old Testament: God hears the cry of enslaved Israel, breaks Pharaoh's grip through ten plagues, parts the Red Sea, and meets His people at Sinai. It reveals God as deliverer, covenant-maker, and the One who dwells among His people.",
    themes: ["Deliverance", "God's power", "Covenant law", "God's presence", "Freedom from bondage"],
    keyVerses: [
      { ref: "exodus-14-14", display: "Exodus 14:14", note: "The Lord will fight for you; you need only to be still." },
      { ref: "exodus-15-26", display: "Exodus 15:26", note: "God reveals Himself as Jehovah Rapha — the Lord who heals." },
      { ref: "exodus-20-2",  display: "Exodus 20:2",  note: "The Ten Commandments open with grace: God who brought you out." },
    ],
    relatedPrayers: ["deliverance", "breakthrough", "healing", "protection", "prayer-of-moses"],
  },
  "leviticus": {
    slug: "leviticus",
    overview: "Leviticus is God's holiness handbook for a people learning to live near His presence. Through sacrifices, feasts, and purity laws, it teaches that sin is costly, atonement is necessary, and God desires a set-apart people. It points forward to the perfect sacrifice of Christ.",
    themes: ["Holiness", "Atonement", "Sacrifice", "Worship", "Consecration"],
    keyVerses: [
      { ref: "leviticus-19-2",  display: "Leviticus 19:2",  note: "Be holy, because I the Lord your God am holy." },
      { ref: "leviticus-17-11", display: "Leviticus 17:11", note: "The life is in the blood — the foundation of atonement." },
      { ref: "leviticus-26-12", display: "Leviticus 26:12", note: "God's promise: I will walk among you and be your God." },
    ],
    relatedPrayers: ["repentance", "spiritual-growth", "worship", "forgiveness", "sexual-purity"],
  },
  "numbers": {
    slug: "numbers",
    overview: "Numbers follows Israel through forty years of wilderness — a journey that should have taken eleven days. It is an honest record of grumbling, unbelief, and consequence, but also of God's patient provision: manna, water, guidance by cloud and fire, and the famous priestly blessing.",
    themes: ["Wilderness seasons", "God's faithfulness", "Consequences of unbelief", "Divine guidance", "Blessing"],
    keyVerses: [
      { ref: "numbers-6-24",  display: "Numbers 6:24",  note: "The priestly blessing: the Lord bless you and keep you." },
      { ref: "numbers-23-19", display: "Numbers 23:19", note: "God is not a man that He should lie — His word stands." },
      { ref: "numbers-13-30", display: "Numbers 13:30", note: "Caleb's faith: we are well able to take the land." },
    ],
    relatedPrayers: ["waiting-on-god", "trust", "perseverance", "direction", "blessing"],
  },
  "deuteronomy": {
    slug: "deuteronomy",
    overview: "Deuteronomy is Moses' farewell sermon on the edge of the Promised Land — a passionate retelling of the law for a new generation. Its heartbeat is love: love God with all your heart, soul, and strength. Jesus quoted Deuteronomy more than almost any other book.",
    themes: ["Wholehearted love for God", "Obedience & blessing", "Remembering God's works", "Choosing life", "Covenant renewal"],
    keyVerses: [
      { ref: "deuteronomy-6-5",   display: "Deuteronomy 6:5",   note: "The Shema: love the Lord with all your heart." },
      { ref: "deuteronomy-31-6",  display: "Deuteronomy 31:6",  note: "Be strong and courageous — He will never forsake you." },
      { ref: "deuteronomy-28-13", display: "Deuteronomy 28:13", note: "The head and not the tail — blessing for obedience." },
    ],
    relatedPrayers: ["courage", "blessing", "faith", "favor", "strength"],
  },
  "joshua": {
    slug: "joshua",
    overview: "Joshua is the book of possessed promises. After Moses' death, Joshua leads Israel across the Jordan, around Jericho's walls, and into the inheritance God swore to Abraham. It is a manual for courage — God's command to be strong and courageous frames the whole conquest.",
    themes: ["Courage", "Possessing promises", "God fights for His people", "Obedience", "Choosing whom to serve"],
    keyVerses: [
      { ref: "joshua-1-9",   display: "Joshua 1:9",   note: "Be strong and courageous — the Lord is with you wherever you go." },
      { ref: "joshua-24-15", display: "Joshua 24:15", note: "As for me and my house, we will serve the Lord." },
      { ref: "joshua-21-45", display: "Joshua 21:45", note: "Not one of God's good promises failed — all came to pass." },
    ],
    relatedPrayers: ["courage", "breakthrough", "strength", "family", "new-beginnings"],
  },
  "judges": {
    slug: "judges",
    overview: "Judges chronicles Israel's darkest cycle: sin, oppression, crying out, and rescue — repeated for generations. God raises unlikely deliverers like Gideon, Deborah, and Samson, proving He responds to honest cries for help even when His people keep failing. Grace shines against a dark backdrop.",
    themes: ["God's mercy in failure", "Deliverance", "Unlikely heroes", "The cost of compromise", "Crying out to God"],
    keyVerses: [
      { ref: "judges-6-12", display: "Judges 6:12", note: "God calls fearful Gideon a mighty warrior." },
      { ref: "judges-4-14", display: "Judges 4:14", note: "Deborah's word: has not the Lord gone ahead of you?" },
      { ref: "judges-16-28", display: "Judges 16:28", note: "Samson's final prayer — God answers even at the end." },
    ],
    relatedPrayers: ["deliverance", "repentance", "strength", "prayer-when-nothing-is-working", "courage"],
  },
  "ruth": {
    slug: "ruth",
    overview: "Ruth is a short story of loyal love in a time of famine and loss. A widowed foreigner binds herself to her mother-in-law and to Israel's God — and is woven into the family line of King David and Jesus. It proves no loss is beyond God's quiet, redeeming providence.",
    themes: ["Loyalty", "Redemption", "God's providence", "Restoration after loss", "Unexpected blessing"],
    keyVerses: [
      { ref: "ruth-1-16", display: "Ruth 1:16", note: "Where you go I will go — your God will be my God." },
      { ref: "ruth-2-12", display: "Ruth 2:12", note: "A full reward under the wings of the God of Israel." },
      { ref: "ruth-4-14", display: "Ruth 4:14", note: "The Lord has not left you without a redeemer." },
    ],
    relatedPrayers: ["grief", "new-beginnings", "prayer-for-widows-widowers", "trust", "love"],
  },
  "1-samuel": {
    slug: "1-samuel",
    overview: "1 Samuel traces Israel's shift from judges to kings through three lives: Samuel the prophet, Saul the rejected king, and David the shepherd anointed in secret. It is rich with prayer — Hannah's desperate plea for a child opens the book and becomes a template for praying through tears.",
    themes: ["Answered prayer", "God looks at the heart", "Waiting for promotion", "Obedience over sacrifice", "Facing giants"],
    keyVerses: [
      { ref: "1-samuel-1-27", display: "1 Samuel 1:27", note: "Hannah: for this child I prayed, and the Lord granted it." },
      { ref: "1-samuel-16-7", display: "1 Samuel 16:7", note: "Man looks at appearance; the Lord looks at the heart." },
      { ref: "1-samuel-17-47", display: "1 Samuel 17:47", note: "The battle is the Lord's — David before Goliath." },
    ],
    relatedPrayers: ["prayer-of-hannah", "fertility-prayer", "waiting-on-god", "courage", "prayer-of-david"],
  },
  "2-samuel": {
    slug: "2-samuel",
    overview: "2 Samuel covers David's reign — his triumphs, his devastating fall with Bathsheba, and his raw, honest walk back to God. It shows that even a man after God's own heart needs mercy, and that God's covenant love survives our worst chapters.",
    themes: ["God's covenant", "Repentance", "Restoration", "Kingship", "Consequences and mercy"],
    keyVerses: [
      { ref: "2-samuel-7-16", display: "2 Samuel 7:16", note: "God's covenant: David's throne established forever." },
      { ref: "2-samuel-22-33", display: "2 Samuel 22:33", note: "It is God who arms me with strength." },
      { ref: "2-samuel-12-13", display: "2 Samuel 12:13", note: "David's confession — and God's immediate forgiveness." },
    ],
    relatedPrayers: ["repentance", "forgiveness", "prayer-of-david", "strength", "prayer-for-past-mistakes"],
  },
  "1-kings": {
    slug: "1-kings",
    overview: "1 Kings moves from Solomon's golden age — the temple, legendary wisdom, unmatched wealth — to a kingdom torn in two by pride and idolatry. Elijah bursts in with fire from heaven on Mount Carmel, proving the Lord alone is God. Wisdom sought and wisdom squandered frame the whole book.",
    themes: ["Wisdom", "The temple & God's presence", "Idolatry's cost", "Bold prophetic faith", "Answered prayer by fire"],
    keyVerses: [
      { ref: "1-kings-3-9",  display: "1 Kings 3:9",  note: "Solomon asks for a discerning heart above riches." },
      { ref: "1-kings-18-38", display: "1 Kings 18:38", note: "Fire falls — Elijah's prayer answered before all Israel." },
      { ref: "1-kings-8-56", display: "1 Kings 8:56", note: "Not one word of God's good promise has failed." },
    ],
    relatedPrayers: ["wisdom", "prayer-of-solomon", "prayer-of-elijah", "revival", "direction"],
  },
  "2-kings": {
    slug: "2-kings",
    overview: "2 Kings races through the divided kingdom's final centuries — Elisha's double-portion miracles, kings good and evil, and finally exile to Assyria and Babylon. Yet miracles keep breaking in: floating axe heads, healed lepers, resurrection. God stays powerfully present even in national decline.",
    themes: ["Miracles", "The prophetic word", "Healing", "Judgment & hope", "God's power in dark times"],
    keyVerses: [
      { ref: "2-kings-6-16", display: "2 Kings 6:16", note: "Those who are with us are more than those with them." },
      { ref: "2-kings-5-14", display: "2 Kings 5:14", note: "Naaman dips seven times and is completely healed." },
      { ref: "2-kings-20-5", display: "2 Kings 20:5", note: "I have heard your prayer and seen your tears; I will heal you." },
    ],
    relatedPrayers: ["healing", "prayer-for-miracles", "protection", "deliverance", "intercessory-prayer"],
  },
  "1-chronicles": {
    slug: "1-chronicles",
    overview: "1 Chronicles retells David's story for a generation returning from exile, anchoring their identity in genealogy, worship, and covenant. Its highlights are musical and devotional: David appoints singers, organizes temple worship, and prays one of Scripture's greatest thanksgiving prayers — including the famous prayer of Jabez.",
    themes: ["Worship & praise", "Identity & heritage", "Thanksgiving", "Preparing God's house", "Enlarged territory"],
    keyVerses: [
      { ref: "1-chronicles-4-10", display: "1 Chronicles 4:10", note: "The prayer of Jabez: bless me and enlarge my territory." },
      { ref: "1-chronicles-16-34", display: "1 Chronicles 16:34", note: "Give thanks to the Lord — His love endures forever." },
      { ref: "1-chronicles-29-11", display: "1 Chronicles 29:11", note: "Yours, Lord, is the greatness and the power and the glory." },
    ],
    relatedPrayers: ["prayer-of-jabez", "thanksgiving", "worship", "praise", "blessing"],
  },
  "2-chronicles": {
    slug: "2-chronicles",
    overview: "2 Chronicles follows the kings of Judah from Solomon's temple dedication to the Babylonian exile, spotlighting every revival along the way. Its most quoted promise — if my people humble themselves and pray — remains the Bible's clearest formula for national and personal restoration.",
    themes: ["Revival", "Humility & prayer", "God's response to seeking hearts", "The temple", "Victory through worship"],
    keyVerses: [
      { ref: "2-chronicles-7-14", display: "2 Chronicles 7:14", note: "If my people pray, I will hear, forgive, and heal their land." },
      { ref: "2-chronicles-20-15", display: "2 Chronicles 20:15", note: "Do not be afraid — the battle is not yours, but God's." },
      { ref: "2-chronicles-16-9", display: "2 Chronicles 16:9", note: "God's eyes range the earth to strengthen committed hearts." },
    ],
    relatedPrayers: ["revival", "nation", "spiritual-warfare", "fasting-prayer", "intercessory-prayer"],
  },
  "ezra": {
    slug: "ezra",
    overview: "Ezra records the first waves of return from Babylonian exile — rebuilding the altar, then the temple, against opposition and discouragement. Ezra himself arrives as a scribe devoted to studying, obeying, and teaching God's Word, modeling how revival begins with Scripture and honest confession.",
    themes: ["Restoration", "Rebuilding", "Devotion to God's Word", "Confession", "God moving leaders' hearts"],
    keyVerses: [
      { ref: "ezra-7-10", display: "Ezra 7:10", note: "Ezra devoted himself to study, obey, and teach God's Word." },
      { ref: "ezra-8-22", display: "Ezra 8:22", note: "The gracious hand of God is on everyone who looks to Him." },
      { ref: "ezra-3-11", display: "Ezra 3:11", note: "Praise and weeping mingle as the foundation is laid." },
    ],
    relatedPrayers: ["new-beginnings", "prayer-for-spiritual-renewal", "repentance", "prayer-for-our-country", "perseverance"],
  },
  "nehemiah": {
    slug: "nehemiah",
    overview: "Nehemiah is the Bible's project-management masterclass soaked in prayer. A royal cupbearer hears Jerusalem's walls are rubble, weeps, fasts, prays — then rebuilds them in fifty-two days despite ridicule and threats. Every chapter shows prayer and practical action working as one.",
    themes: ["Prayer & action", "Rebuilding what is broken", "Leadership", "Opposition & perseverance", "The joy of the Lord"],
    keyVerses: [
      { ref: "nehemiah-8-10", display: "Nehemiah 8:10", note: "The joy of the Lord is your strength." },
      { ref: "nehemiah-1-11", display: "Nehemiah 1:11", note: "Nehemiah's prayer for favour before the king." },
      { ref: "nehemiah-6-9",  display: "Nehemiah 6:9",  note: "Now strengthen my hands — prayer under pressure." },
    ],
    relatedPrayers: ["prayer-of-nehemiah", "work", "perseverance", "favor", "purpose"],
  },
  "esther": {
    slug: "esther",
    overview: "Esther never mentions God's name, yet His providence saturates every scene. A Jewish orphan becomes queen of Persia just in time to stop a genocide, risking her life with the words: if I perish, I perish. It is the book for anyone positioned by God for such a time as this.",
    themes: ["Divine providence", "Courage", "Fasting", "Purpose & timing", "Reversal of evil plans"],
    keyVerses: [
      { ref: "esther-4-14", display: "Esther 4:14", note: "Who knows — you were made queen for such a time as this." },
      { ref: "esther-4-16", display: "Esther 4:16", note: "Fast for me three days — if I perish, I perish." },
      { ref: "esther-9-22", display: "Esther 9:22", note: "Sorrow turned to joy, mourning into celebration." },
    ],
    relatedPrayers: ["courage", "purpose", "fasting-prayer", "protection-from-enemies", "favor"],
  },
  "job": {
    slug: "job",
    overview: "Job is the Bible's deepest wrestle with undeserved suffering. A blameless man loses everything, sits in ashes, and refuses easy answers — from his friends or himself. God answers from the whirlwind not with explanations but with Himself, and Job's fortunes are restored double.",
    themes: ["Suffering", "Faith under trial", "Honest lament", "God's sovereignty", "Restoration"],
    keyVerses: [
      { ref: "job-19-25", display: "Job 19:25", note: "I know that my Redeemer lives." },
      { ref: "job-42-10", display: "Job 42:10", note: "God restored Job's fortunes — twice as much as before." },
      { ref: "job-23-10", display: "Job 23:10", note: "When He has tested me, I will come forth as gold." },
    ],
    relatedPrayers: ["prayer-of-job", "grief", "perseverance", "trust", "prayer-when-god-feels-distant"],
  },
  "psalms": {
    slug: "psalms",
    overview: "Psalms is the Bible's prayer book — 150 songs covering every emotion a praying person will ever feel: praise, panic, gratitude, grief, fury, and quiet trust. For three thousand years believers have borrowed these words when their own run out. If you can feel it, there is a psalm for it.",
    themes: ["Praise & worship", "Lament", "Trust", "God as refuge", "Thanksgiving", "Honest emotion before God"],
    keyVerses: [
      { ref: "psalms-23-1",  display: "Psalm 23:1",  note: "The Lord is my shepherd — the world's most beloved prayer." },
      { ref: "psalms-91-1",  display: "Psalm 91:1",  note: "The shelter of the Most High — the great protection psalm." },
      { ref: "psalms-46-1",  display: "Psalm 46:1",  note: "God is our refuge and strength, ever-present in trouble." },
      { ref: "psalms-51-10", display: "Psalm 51:10", note: "Create in me a clean heart — the model repentance prayer." },
    ],
    relatedPrayers: ["morning", "night", "anxiety", "depression", "praise", "daily", "prayer-based-on-psalm-23", "prayer-based-on-psalm-91"],
  },
  "proverbs": {
    slug: "proverbs",
    overview: "Proverbs is distilled wisdom for ordinary decisions — money, words, work, friendship, marriage, anger, and the tongue. Its premise is simple: the fear of the Lord is the beginning of wisdom. Thirty-one chapters make it the classic one-chapter-a-day book for praying through daily choices.",
    themes: ["Wisdom", "The fear of the Lord", "Diligence & work", "Guarding the tongue", "Trust over self-reliance"],
    keyVerses: [
      { ref: "proverbs-3-5",  display: "Proverbs 3:5",  note: "Trust in the Lord with all your heart — He directs paths." },
      { ref: "proverbs-18-10", display: "Proverbs 18:10", note: "The name of the Lord is a strong tower." },
      { ref: "proverbs-16-3", display: "Proverbs 16:3", note: "Commit your work to the Lord and your plans succeed." },
    ],
    relatedPrayers: ["wisdom", "work", "direction", "marriage", "business"],
  },
  "ecclesiastes": {
    slug: "ecclesiastes",
    overview: "Ecclesiastes is the Bible's most unflinching look at life under the sun — wealth, pleasure, work, and wisdom all tested and found fleeting. Its honesty disarms cynics, then lands on bedrock: fear God, keep His commands, and receive each ordinary day as His gift.",
    themes: ["Meaning & purpose", "Seasons of life", "The gift of today", "Vanity of striving", "Fearing God"],
    keyVerses: [
      { ref: "ecclesiastes-3-1",  display: "Ecclesiastes 3:1",  note: "There is a time for everything — a season for every activity." },
      { ref: "ecclesiastes-3-11", display: "Ecclesiastes 3:11", note: "He has made everything beautiful in its time." },
      { ref: "ecclesiastes-12-13", display: "Ecclesiastes 12:13", note: "The conclusion: fear God and keep His commandments." },
    ],
    relatedPrayers: ["purpose", "contentment", "prayer-for-feeling-lost", "wisdom", "letting-go"],
  },
  "song-of-solomon": {
    slug: "song-of-solomon",
    overview: "Song of Solomon is an unashamed celebration of covenant love — poetic dialogue between a bride and bridegroom that honors desire, faithfulness, and delight. Believers across centuries have also read it as a picture of Christ's love for His people: strong as death, unquenchable by many waters.",
    themes: ["Covenant love", "Marriage", "Desire & faithfulness", "The beloved's worth", "Love's strength"],
    keyVerses: [
      { ref: "song-of-solomon-8-7", display: "Song of Solomon 8:7", note: "Many waters cannot quench love." },
      { ref: "song-of-solomon-2-16", display: "Song of Solomon 2:16", note: "My beloved is mine and I am his." },
      { ref: "song-of-solomon-8-6", display: "Song of Solomon 8:6", note: "Set me as a seal upon your heart — love strong as death." },
    ],
    relatedPrayers: ["marriage", "love", "husband", "wife", "prayer-for-future-spouse"],
  },
  "isaiah": {
    slug: "isaiah",
    overview: "Isaiah is the prince of prophets — soaring visions of God's holiness, searing warnings, and the Old Testament's clearest portraits of the coming Messiah: born of a virgin, wounded for our transgressions, a man of sorrows. Comfort and strength for the weary run through every chapter.",
    themes: ["God's holiness", "Messianic prophecy", "Comfort", "Renewed strength", "Healing by His wounds"],
    keyVerses: [
      { ref: "isaiah-40-31", display: "Isaiah 40:31", note: "Those who wait on the Lord renew their strength — wings like eagles." },
      { ref: "isaiah-53-5",  display: "Isaiah 53:5",  note: "By His wounds we are healed — the suffering servant." },
      { ref: "isaiah-41-10", display: "Isaiah 41:10", note: "Do not fear, for I am with you — I will uphold you." },
    ],
    relatedPrayers: ["healing", "strength", "hope", "comfort", "prayer-based-on-isaiah-40-31"],
  },
  "jeremiah": {
    slug: "jeremiah",
    overview: "Jeremiah preached for forty years to a nation that would not listen, weeping as Jerusalem fell. Yet from the rubble come some of Scripture's most hopeful promises: plans to prosper and not harm, a new covenant written on hearts, and a God who invites us to call and see great things.",
    themes: ["God's plans & hope", "Honest tears", "Calling on God", "New covenant", "Faithfulness in hard assignments"],
    keyVerses: [
      { ref: "jeremiah-29-11", display: "Jeremiah 29:11", note: "I know the plans I have for you — hope and a future." },
      { ref: "jeremiah-33-3",  display: "Jeremiah 33:3",  note: "Call to me and I will answer — great and unsearchable things." },
      { ref: "jeremiah-30-17", display: "Jeremiah 30:17", note: "I will restore you to health and heal your wounds." },
    ],
    relatedPrayers: ["hope", "purpose", "prayer-based-on-jeremiah-29-11", "healing", "prayer-for-fear-of-the-future"],
  },
  "lamentations": {
    slug: "lamentations",
    overview: "Lamentations is five funeral poems over fallen Jerusalem — grief given holy structure. At its exact center burns the flame the darkness never reaches: because of the Lord's great love we are not consumed; His mercies are new every morning. It teaches us to grieve with hope.",
    themes: ["Grief & lament", "New mercies every morning", "Hope in devastation", "God's faithfulness", "Waiting quietly"],
    keyVerses: [
      { ref: "lamentations-3-22", display: "Lamentations 3:22", note: "His compassions never fail — new every morning." },
      { ref: "lamentations-3-25", display: "Lamentations 3:25", note: "The Lord is good to those whose hope is in Him." },
      { ref: "lamentations-3-32", display: "Lamentations 3:32", note: "Though He brings grief, He will show compassion." },
    ],
    relatedPrayers: ["grief", "comfort", "morning", "loss-of-loved-one", "hope"],
  },
  "ezekiel": {
    slug: "ezekiel",
    overview: "Ezekiel saw heaven opened from a refugee camp in Babylon — wheels of fire, a valley of dry bones rattling to life, and a river flowing from God's temple that heals everything it touches. His message: God's glory departs from sin but returns with resurrection power and a brand-new heart.",
    themes: ["God's glory", "Resurrection power", "A new heart & spirit", "Watchman's responsibility", "Restoration"],
    keyVerses: [
      { ref: "ezekiel-36-26", display: "Ezekiel 36:26", note: "I will give you a new heart and put a new spirit in you." },
      { ref: "ezekiel-37-5",  display: "Ezekiel 37:5",  note: "Dry bones, hear the word of the Lord — I will make you live." },
      { ref: "ezekiel-22-30", display: "Ezekiel 22:30", note: "God searches for someone to stand in the gap." },
    ],
    relatedPrayers: ["prayer-for-spiritual-renewal", "intercessory-prayer", "deliverance", "new-beginnings", "revival"],
  },
  "daniel": {
    slug: "daniel",
    overview: "Daniel proves you can serve God at full integrity inside a hostile empire. Lions' den, fiery furnace, interpreted dreams, and twenty-one-day prayer battles — Daniel and his friends out-pray and out-live every decree against them, while visions unveil God's rule over all kingdoms and centuries.",
    themes: ["Faithfulness under pressure", "Persistent prayer", "God's sovereignty over nations", "Divine protection", "Wisdom & excellence"],
    keyVerses: [
      { ref: "daniel-6-22", display: "Daniel 6:22", note: "God sent His angel and shut the lions' mouths." },
      { ref: "daniel-3-17", display: "Daniel 3:17", note: "Our God is able to deliver us — and even if not, we will not bow." },
      { ref: "daniel-10-12", display: "Daniel 10:12", note: "From the first day you prayed, your words were heard." },
    ],
    relatedPrayers: ["protection", "courage", "fasting-prayer", "midnight-prayer", "exam"],
  },
  "hosea": {
    slug: "hosea",
    overview: "Hosea lived his sermon: commanded to marry an unfaithful wife and keep bringing her home, he became a walking picture of God's scandalous love for wandering Israel. The book aches with betrayal yet ends with healing for backsliders and love that will not let go.",
    themes: ["God's relentless love", "Restoration after unfaithfulness", "Returning to the Lord", "Healing backsliding", "Knowing God"],
    keyVerses: [
      { ref: "hosea-6-3",  display: "Hosea 6:3",  note: "Press on to know the Lord — He comes like spring rains." },
      { ref: "hosea-14-4", display: "Hosea 14:4", note: "I will heal their waywardness and love them freely." },
      { ref: "hosea-10-12", display: "Hosea 10:12", note: "Break up your unplowed ground — it is time to seek the Lord." },
    ],
    relatedPrayers: ["prayer-for-the-prodigal", "marriage-restoration", "repentance", "love", "prayer-for-spiritual-renewal"],
  },
  "joel": {
    slug: "joel",
    overview: "Joel turns a devastating locust plague into a call for national fasting — then pivots to one of Scripture's greatest promises: I will restore the years the locusts have eaten, and afterward I will pour out my Spirit on all people. Peter quoted it at Pentecost as fulfilled.",
    themes: ["Restoration of lost years", "Outpouring of the Spirit", "Fasting & returning", "The day of the Lord", "Calling on His name"],
    keyVerses: [
      { ref: "joel-2-25", display: "Joel 2:25", note: "I will repay you for the years the locusts have eaten." },
      { ref: "joel-2-28", display: "Joel 2:28", note: "I will pour out my Spirit on all people." },
      { ref: "joel-2-32", display: "Joel 2:32", note: "Everyone who calls on the name of the Lord will be saved." },
    ],
    relatedPrayers: ["prayer-for-holy-spirit", "fasting-prayer", "breakthrough", "new-beginnings", "revival"],
  },
  "amos": {
    slug: "amos",
    overview: "Amos was a shepherd sent to confront prosperous, religious, unjust Israel. His thunderous demand — let justice roll on like a river — still defines what God expects from worshippers: not louder songs, but fairness for the poor and honesty in the gates.",
    themes: ["Justice", "True vs. empty worship", "Concern for the poor", "Seeking God and living", "Accountability"],
    keyVerses: [
      { ref: "amos-5-24", display: "Amos 5:24", note: "Let justice roll on like a river, righteousness like a stream." },
      { ref: "amos-5-4",  display: "Amos 5:4",  note: "Seek me and live." },
      { ref: "amos-9-13", display: "Amos 9:13", note: "Days of restoration when the plowman overtakes the reaper." },
    ],
    relatedPrayers: ["justice", "nation", "racial-healing", "poverty", "repentance"],
  },
  "obadiah": {
    slug: "obadiah",
    overview: "Obadiah, the Old Testament's shortest book, is a single oracle against Edom for gloating over Jerusalem's fall. Its message stands for every generation: pride deceives, betrayal of family is judged, but on Mount Zion there will be deliverance — and the kingdom will be the Lord's.",
    themes: ["Pride's downfall", "God's justice", "Deliverance on Zion", "Betrayal judged", "The Lord's kingdom"],
    keyVerses: [
      { ref: "obadiah-1-17", display: "Obadiah 1:17", note: "On Mount Zion will be deliverance; it will be holy." },
      { ref: "obadiah-1-3",  display: "Obadiah 1:3",  note: "The pride of your heart has deceived you." },
      { ref: "obadiah-1-21", display: "Obadiah 1:21", note: "The kingdom will be the Lord's." },
    ],
    relatedPrayers: ["pride", "justice", "deliverance", "protection-from-enemies", "prayer-for-betrayal"],
  },
  "jonah": {
    slug: "jonah",
    overview: "Jonah is the prophet who ran — away from Nineveh, into a storm, and into the belly of a great fish, where he finally prayed. God's question closes the book: shouldn't I have compassion on a city of 120,000? It exposes our reluctance and God's relentless mercy to outsiders.",
    themes: ["God's mercy to all", "Running from God's call", "Prayer from the depths", "Second chances", "Compassion"],
    keyVerses: [
      { ref: "jonah-2-2", display: "Jonah 2:2", note: "In my distress I called to the Lord, and He answered me." },
      { ref: "jonah-2-7", display: "Jonah 2:7", note: "When my life was ebbing away, I remembered you, Lord." },
      { ref: "jonah-4-2", display: "Jonah 4:2", note: "You are gracious and compassionate, slow to anger." },
    ],
    relatedPrayers: ["prayer-when-nothing-is-working", "repentance", "calling", "new-beginnings", "salvation"],
  },
  "micah": {
    slug: "micah",
    overview: "Micah alternates judgment and hope with a prophet's precision — predicting both Jerusalem's fall and Bethlehem as Messiah's birthplace seven centuries early. His summary of true religion is unmatched: act justly, love mercy, walk humbly with your God.",
    themes: ["Justice, mercy, humility", "Messianic hope", "God pardons sin", "Restoration", "Walking with God"],
    keyVerses: [
      { ref: "micah-6-8", display: "Micah 6:8", note: "Act justly, love mercy, walk humbly — what God requires." },
      { ref: "micah-7-7", display: "Micah 7:7", note: "I watch in hope for the Lord; my God will hear me." },
      { ref: "micah-7-18", display: "Micah 7:18", note: "Who is a God like you, who pardons sin and delights in mercy?" },
    ],
    relatedPrayers: ["justice", "forgiveness", "hope", "spiritual-growth", "prayer-for-pride-humility"],
  },
  "nahum": {
    slug: "nahum",
    overview: "Nahum announces the fall of Nineveh — the brutal Assyrian capital that had terrorized nations for generations. Its comfort is fierce: the Lord is slow to anger but will not leave the guilty unpunished, and He is a refuge in trouble for those who trust in Him.",
    themes: ["God as refuge", "Justice against oppression", "The end of tyranny", "God's patience & power", "Good news of peace"],
    keyVerses: [
      { ref: "nahum-1-7", display: "Nahum 1:7", note: "The Lord is good, a refuge in times of trouble." },
      { ref: "nahum-1-3", display: "Nahum 1:3", note: "Slow to anger, great in power." },
      { ref: "nahum-1-15", display: "Nahum 1:15", note: "The feet of one who brings good news of peace." },
    ],
    relatedPrayers: ["protection", "justice", "peace", "prayer-against-witchcraft", "deliverance"],
  },
  "habakkuk": {
    slug: "habakkuk",
    overview: "Habakkuk dares to interrogate God — why do the wicked prosper while you stay silent? God answers with a vision worth waiting for and the line that ignited the Reformation: the righteous shall live by faith. The book ends in one of Scripture's boldest declarations of joy despite empty barns.",
    themes: ["Honest questions to God", "Living by faith", "Waiting for the vision", "Joy despite circumstances", "God's sovereignty"],
    keyVerses: [
      { ref: "habakkuk-2-3", display: "Habakkuk 2:3", note: "The vision awaits its time — though it linger, wait for it." },
      { ref: "habakkuk-3-19", display: "Habakkuk 3:19", note: "The Sovereign Lord is my strength — feet like a deer on heights." },
      { ref: "habakkuk-3-17", display: "Habakkuk 3:17", note: "Though the fig tree does not bud, yet I will rejoice in the Lord." },
    ],
    relatedPrayers: ["waiting-on-god", "doubt", "faith", "praise", "prayer-when-god-feels-distant"],
  },
  "zephaniah": {
    slug: "zephaniah",
    overview: "Zephaniah sweeps from cosmic judgment to one of the tenderest verses in the Bible: the Lord your God is with you, the Mighty Warrior who saves; He will rejoice over you with singing. The day of the Lord clears the ground so restored people can hear God sing.",
    themes: ["God rejoicing over His people", "The day of the Lord", "Humility", "Restoration", "God's saving presence"],
    keyVerses: [
      { ref: "zephaniah-3-17", display: "Zephaniah 3:17", note: "He will rejoice over you with singing — the Mighty Warrior saves." },
      { ref: "zephaniah-2-3",  display: "Zephaniah 2:3",  note: "Seek the Lord, seek righteousness, seek humility." },
      { ref: "zephaniah-3-20", display: "Zephaniah 3:20", note: "I will restore your fortunes before your very eyes." },
    ],
    relatedPrayers: ["identity", "love", "comfort", "prayer-for-worthless", "new-beginnings"],
  },
  "haggai": {
    slug: "haggai",
    overview: "Haggai delivers four dated sermons to people who had stalled rebuilding God's temple for sixteen years while paneling their own houses. His challenge — give careful thought to your ways — reorders priorities, and God's response to renewed obedience is immediate: I am with you; from this day I will bless you.",
    themes: ["Right priorities", "Finishing what God started", "God's presence in work", "From this day I will bless you", "Future glory"],
    keyVerses: [
      { ref: "haggai-2-9", display: "Haggai 2:9", note: "The glory of this present house will exceed the former." },
      { ref: "haggai-1-13", display: "Haggai 1:13", note: "I am with you, declares the Lord." },
      { ref: "haggai-2-19", display: "Haggai 2:19", note: "From this day on I will bless you." },
    ],
    relatedPrayers: ["work", "purpose", "procrastination", "blessing", "church"],
  },
  "zechariah": {
    slug: "zechariah",
    overview: "Zechariah pairs night visions with Messianic precision — the king on a donkey, thirty pieces of silver, the pierced one, the fountain opened for sin. To discouraged builders his word still stands: not by might nor by power, but by my Spirit, says the Lord Almighty.",
    themes: ["By my Spirit", "Messianic prophecy", "Small beginnings", "God's jealousy for Zion", "Cleansing fountain"],
    keyVerses: [
      { ref: "zechariah-4-6", display: "Zechariah 4:6", note: "Not by might nor by power, but by my Spirit." },
      { ref: "zechariah-4-10", display: "Zechariah 4:10", note: "Do not despise the day of small beginnings." },
      { ref: "zechariah-9-9", display: "Zechariah 9:9", note: "Your king comes to you, righteous, riding on a donkey." },
    ],
    relatedPrayers: ["prayer-for-holy-spirit", "prayer-for-small-business", "new-beginnings", "spiritual-warfare", "hope"],
  },
  "malachi": {
    slug: "malachi",
    overview: "Malachi closes the Old Testament with a courtroom dialogue — God answering a weary nation's accusations about love, honor, tithes, and justice. It contains Scripture's only invitation to test God: bring the whole tithe and watch heaven's floodgates open. Then four hundred years of silence until John the Baptist.",
    themes: ["Testing God in giving", "Honoring God", "The sun of righteousness", "Refining fire", "Preparing the way"],
    keyVerses: [
      { ref: "malachi-3-10", display: "Malachi 3:10", note: "Test me in this — see if I will not open heaven's floodgates." },
      { ref: "malachi-4-2",  display: "Malachi 4:2",  note: "The sun of righteousness will rise with healing in its rays." },
      { ref: "malachi-3-6",  display: "Malachi 3:6",  note: "I the Lord do not change." },
    ],
    relatedPrayers: ["tithing", "financial-breakthrough", "healing", "provision", "favor"],
  },

  // ── NEW TESTAMENT ─────────────────────────────────────────────
  "matthew": {
    slug: "matthew",
    overview: "Matthew presents Jesus as the promised King — Israel's Messiah who fulfils the prophets line by line. It preserves the Sermon on the Mount, the Lord's Prayer, and the Great Commission. Five great teaching blocks make it the church's first discipleship manual.",
    themes: ["The kingdom of heaven", "The Lord's Prayer", "Fulfilled prophecy", "Discipleship", "Seek first the kingdom"],
    keyVerses: [
      { ref: "matthew-6-33", display: "Matthew 6:33", note: "Seek first the kingdom — all these things will be added." },
      { ref: "matthew-11-28", display: "Matthew 11:28", note: "Come to me, all you who are weary, and I will give you rest." },
      { ref: "matthew-6-9",  display: "Matthew 6:9",  note: "The Lord's Prayer — Jesus' own pattern for praying." },
    ],
    relatedPrayers: ["prayer-for-today", "morning", "faith", "direction", "lords-prayer-meaning"],
  },
  "mark": {
    slug: "mark",
    overview: "Mark is the action Gospel — the shortest, fastest account, where Jesus heals, delivers, and teaches with immediate authority. Written for believers under Roman persecution, it presents the Son of God as the suffering servant who came not to be served, but to serve and give His life.",
    themes: ["Jesus' authority", "Faith that moves mountains", "Servanthood", "Healing & deliverance", "Taking up the cross"],
    keyVerses: [
      { ref: "mark-11-24", display: "Mark 11:24", note: "Whatever you ask in prayer, believe you have received it." },
      { ref: "mark-9-23",  display: "Mark 9:23",  note: "Everything is possible for one who believes." },
      { ref: "mark-10-45", display: "Mark 10:45", note: "The Son of Man came to serve and give His life as a ransom." },
    ],
    relatedPrayers: ["faith", "healing", "deliverance", "powerful-prayer", "prayer-for-miracles"],
  },
  "luke": {
    slug: "luke",
    overview: "Luke, the physician-historian, writes the most carefully researched Gospel — and the most tender. Outsiders take center stage: shepherds, women, Samaritans, tax collectors, the prodigal son. More than any Gospel, Luke shows Jesus praying at every turning point of His life.",
    themes: ["Jesus' prayer life", "Compassion for outsiders", "The Holy Spirit", "Joy & salvation", "The prodigal welcomed"],
    keyVerses: [
      { ref: "luke-11-9", display: "Luke 11:9", note: "Ask, seek, knock — the door will be opened." },
      { ref: "luke-18-1", display: "Luke 18:1", note: "Always pray and never give up — the persistent widow." },
      { ref: "luke-15-20", display: "Luke 15:20", note: "The father runs to embrace the returning prodigal." },
    ],
    relatedPrayers: ["intercessory-prayer", "prayer-for-the-prodigal", "perseverance", "thanksgiving", "salvation"],
  },
  "john": {
    slug: "john",
    overview: "John is the Gospel of belief — written so that you may believe Jesus is the Son of God and have life in His name. Seven I AM declarations, seven signs, and the Bible's most loved verse all live here, alongside Jesus' longest recorded prayer in chapter 17.",
    themes: ["Eternal life", "Believing in Jesus", "The I AM statements", "Abiding in Christ", "Love one another"],
    keyVerses: [
      { ref: "john-3-16",  display: "John 3:16",  note: "God so loved the world — the gospel in one sentence." },
      { ref: "john-15-7",  display: "John 15:7",  note: "Abide in me, ask whatever you wish — it will be done." },
      { ref: "john-14-27", display: "John 14:27", note: "My peace I give you — do not let your hearts be troubled." },
    ],
    relatedPrayers: ["salvation", "faith", "healing", "peace", "love"],
  },
  "acts": {
    slug: "acts",
    overview: "Acts is volume two of Luke's history: the Spirit falls at Pentecost and a praying church turns the world upside down in one generation. Prison doors open, the gospel jumps every cultural wall, and every advance begins in a prayer meeting. It is the blueprint for Spirit-filled boldness.",
    themes: ["The Holy Spirit's power", "Bold witness", "Prayer meetings that shake buildings", "The church's birth", "Unstoppable gospel"],
    keyVerses: [
      { ref: "acts-1-8",  display: "Acts 1:8",  note: "You will receive power when the Holy Spirit comes on you." },
      { ref: "acts-2-42", display: "Acts 2:42", note: "They devoted themselves to teaching, fellowship, and prayer." },
      { ref: "acts-16-31", display: "Acts 16:31", note: "Believe in the Lord Jesus and you will be saved — you and your house." },
    ],
    relatedPrayers: ["prayer-for-holy-spirit", "revival", "church", "missionaries", "courage"],
  },
  "romans": {
    slug: "romans",
    overview: "Romans is the gospel argued like a legal brief — Paul's masterwork on sin, justification by faith, life in the Spirit, and God's unbreakable love. Chapter 8 alone has carried more believers through crisis than perhaps any chapter in Scripture: no condemnation, all things working for good, nothing separating us from God's love.",
    themes: ["Justification by faith", "No condemnation", "Life in the Spirit", "All things work for good", "More than conquerors"],
    keyVerses: [
      { ref: "romans-8-28", display: "Romans 8:28", note: "In all things God works for the good of those who love Him." },
      { ref: "romans-8-1",  display: "Romans 8:1",  note: "There is now no condemnation for those in Christ Jesus." },
      { ref: "romans-10-9", display: "Romans 10:9", note: "Declare Jesus is Lord, believe, and you will be saved." },
    ],
    relatedPrayers: ["salvation", "faith", "purpose", "strength", "prayer-based-on-romans-8"],
  },
  "1-corinthians": {
    slug: "1-corinthians",
    overview: "1 Corinthians is pastoral surgery on a gifted, divided, messy church — addressing factions, immorality, lawsuits, worship chaos, and doubts about resurrection. It contains the love chapter read at countless weddings and the Bible's fullest teaching on spiritual gifts and the resurrection body.",
    themes: ["Love above all gifts", "Unity in the church", "Spiritual gifts", "Resurrection hope", "God's faithful way out"],
    keyVerses: [
      { ref: "1-corinthians-13-4", display: "1 Corinthians 13:4", note: "Love is patient, love is kind — the love chapter." },
      { ref: "1-corinthians-10-13", display: "1 Corinthians 10:13", note: "God provides a way out of every temptation." },
      { ref: "1-corinthians-15-57", display: "1 Corinthians 15:57", note: "Thanks be to God — He gives us the victory through Christ." },
    ],
    relatedPrayers: ["love", "church-unity", "marriage", "sexual-purity", "spiritual-growth"],
  },
  "2-corinthians": {
    slug: "2-corinthians",
    overview: "2 Corinthians is Paul's most personal letter — defending his ministry through a catalogue of beatings, shipwrecks, and sleepless nights, then unveiling the secret: God's power is made perfect in weakness. Treasure in jars of clay, the God of all comfort, and grace that is always sufficient.",
    themes: ["Strength in weakness", "The God of all comfort", "New creation", "Generous giving", "Treasure in jars of clay"],
    keyVerses: [
      { ref: "2-corinthians-12-9", display: "2 Corinthians 12:9", note: "My grace is sufficient — power perfected in weakness." },
      { ref: "2-corinthians-5-17", display: "2 Corinthians 5:17", note: "In Christ, the new creation has come." },
      { ref: "2-corinthians-9-8",  display: "2 Corinthians 9:8",  note: "God is able to bless you abundantly in all things." },
    ],
    relatedPrayers: ["comfort", "strength", "new-beginnings", "burnout", "prayer-for-a-weary-soul"],
  },
  "galatians": {
    slug: "galatians",
    overview: "Galatians is Paul's fiery defense of freedom — no angel, apostle, or law-keeping can add to the finished work of Christ. It gives us the fruit of the Spirit, crucifixion with Christ, and the rallying cry of every recovered legalist: it is for freedom that Christ has set us free.",
    themes: ["Freedom in Christ", "Fruit of the Spirit", "Faith vs. works", "Crucified with Christ", "Sowing and reaping"],
    keyVerses: [
      { ref: "galatians-5-22", display: "Galatians 5:22", note: "The fruit of the Spirit — love, joy, peace, patience…" },
      { ref: "galatians-2-20", display: "Galatians 2:20", note: "I have been crucified with Christ — Christ lives in me." },
      { ref: "galatians-6-9",  display: "Galatians 6:9",  note: "Do not grow weary in doing good — harvest is coming." },
    ],
    relatedPrayers: ["spiritual-growth", "perseverance", "identity", "deliverance", "addiction"],
  },
  "ephesians": {
    slug: "ephesians",
    overview: "Ephesians lifts believers into the heavenly realms — every spiritual blessing, adoption, redemption, and a seat with Christ far above all powers. Then it walks that identity into marriage, work, and warfare, closing with the church's armor: truth, righteousness, faith, salvation, the Spirit's sword, and all kinds of prayer.",
    themes: ["Identity in Christ", "Spiritual blessings", "The armor of God", "Grace through faith", "Immeasurably more"],
    keyVerses: [
      { ref: "ephesians-2-8",  display: "Ephesians 2:8",  note: "By grace you have been saved, through faith — God's gift." },
      { ref: "ephesians-3-20", display: "Ephesians 3:20", note: "Able to do immeasurably more than all we ask or imagine." },
      { ref: "ephesians-6-11", display: "Ephesians 6:11", note: "Put on the full armor of God." },
    ],
    relatedPrayers: ["identity", "spiritual-warfare", "armor-of-god-prayer", "prayer-based-on-ephesians-6", "blessing"],
  },
  "philippians": {
    slug: "philippians",
    overview: "Philippians is joy written from a prison cell. Paul, chained to Roman guards, teaches contentment in every circumstance, peace that guards hearts like a garrison, and pressing on toward the prize. Its antidote to anxiety — prayer with thanksgiving — may be the most prescribed verse in Scripture.",
    themes: ["Joy in all circumstances", "Peace over anxiety", "Contentment", "Christ-centered mindset", "I can do all things"],
    keyVerses: [
      { ref: "philippians-4-6", display: "Philippians 4:6", note: "Be anxious for nothing — pray with thanksgiving." },
      { ref: "philippians-4-13", display: "Philippians 4:13", note: "I can do all this through Him who gives me strength." },
      { ref: "philippians-1-6",  display: "Philippians 1:6",  note: "He who began a good work will carry it to completion." },
    ],
    relatedPrayers: ["anxiety", "peace", "gratitude", "prayer-based-on-philippians-4", "strength"],
  },
  "colossians": {
    slug: "colossians",
    overview: "Colossians answers every spiritual add-on with one word: Christ. He is the image of the invisible God, creator and sustainer of all things, in whom all fullness dwells — and you have been given fullness in Him. Set your mind above; let His peace rule; do everything in His name.",
    themes: ["The supremacy of Christ", "Fullness in Him", "Setting minds on things above", "Christ in you", "Working for the Lord"],
    keyVerses: [
      { ref: "colossians-3-23", display: "Colossians 3:23", note: "Whatever you do, work at it with all your heart, for the Lord." },
      { ref: "colossians-1-17", display: "Colossians 1:17", note: "In Him all things hold together." },
      { ref: "colossians-3-15", display: "Colossians 3:15", note: "Let the peace of Christ rule in your hearts." },
    ],
    relatedPrayers: ["work", "peace", "identity", "thanksgiving", "spiritual-growth"],
  },
  "1-thessalonians": {
    slug: "1-thessalonians",
    overview: "1 Thessalonians is among Paul's earliest letters, written to brand-new believers facing persecution — full of encouragement, hope in Christ's return, and comfort for those grieving the dead in Christ. Its closing commands form the shortest discipleship plan ever: rejoice always, pray continually, give thanks in everything.",
    themes: ["Christ's return", "Comfort in grief", "Pray without ceasing", "Holy living", "Encouraging one another"],
    keyVerses: [
      { ref: "1-thessalonians-5-16", display: "1 Thessalonians 5:16", note: "Rejoice always, pray continually, give thanks in all circumstances." },
      { ref: "1-thessalonians-4-17", display: "1 Thessalonians 4:17", note: "We will be with the Lord forever — comfort one another." },
      { ref: "1-thessalonians-5-24", display: "1 Thessalonians 5:24", note: "The one who calls you is faithful — He will do it." },
    ],
    relatedPrayers: ["daily", "gratitude", "grief", "hope", "comfort"],
  },
  "2-thessalonians": {
    slug: "2-thessalonians",
    overview: "2 Thessalonians steadies a church shaken by false reports that the day of the Lord had already come. Paul untangles end-times confusion, commands dignified work over idle speculation, and prays strength into anxious hearts: may the Lord direct your hearts into God's love and Christ's perseverance.",
    themes: ["Standing firm", "God's faithfulness", "Perseverance", "Work & diligence", "Protection from the evil one"],
    keyVerses: [
      { ref: "2-thessalonians-3-3", display: "2 Thessalonians 3:3", note: "The Lord is faithful — He will strengthen and protect you." },
      { ref: "2-thessalonians-2-16", display: "2 Thessalonians 2:16", note: "Eternal encouragement and good hope by grace." },
      { ref: "2-thessalonians-3-5", display: "2 Thessalonians 3:5", note: "May the Lord direct your hearts into God's love." },
    ],
    relatedPrayers: ["perseverance", "protection", "work", "hope", "strength"],
  },
  "1-timothy": {
    slug: "1-timothy",
    overview: "1 Timothy is a veteran's field manual for a young pastor in Ephesus — sound doctrine, qualified leaders, care for widows, contentment over the love of money, and fighting the good fight of faith. It urges prayers for kings and all in authority so believers can live peaceful, godly lives.",
    themes: ["Praying for leaders", "Godliness with contentment", "Fighting the good fight", "Sound teaching", "Setting an example"],
    keyVerses: [
      { ref: "1-timothy-2-1", display: "1 Timothy 2:1", note: "Pray for kings and all those in authority." },
      { ref: "1-timothy-6-12", display: "1 Timothy 6:12", note: "Fight the good fight of the faith." },
      { ref: "1-timothy-4-12", display: "1 Timothy 4:12", note: "Don't let anyone look down on you because you are young." },
    ],
    relatedPrayers: ["prayer-for-leaders", "pastor", "contentment", "youth-prayer", "government"],
  },
  "2-timothy": {
    slug: "2-timothy",
    overview: "2 Timothy is Paul's last letter, written on death row — a father's final charge to his son in the faith. Fan your gift into flame; God gave a spirit of power, love, and self-discipline, not fear. All Scripture is God-breathed. I have fought the good fight; I have kept the faith.",
    themes: ["No spirit of fear", "Finishing well", "God-breathed Scripture", "Enduring hardship", "Passing the torch"],
    keyVerses: [
      { ref: "2-timothy-1-7", display: "2 Timothy 1:7", note: "God gave us power, love, and a sound mind — not fear." },
      { ref: "2-timothy-3-16", display: "2 Timothy 3:16", note: "All Scripture is God-breathed and useful." },
      { ref: "2-timothy-4-7",  display: "2 Timothy 4:7",  note: "I have fought the good fight, finished the race, kept the faith." },
    ],
    relatedPrayers: ["fear", "courage", "perseverance", "calling", "prayer-of-paul"],
  },
  "titus": {
    slug: "titus",
    overview: "Titus got the hard posting — planting elders across Crete, an island proverbial for liars and lazy gluttons. Paul's antidote is grace that trains: the same grace that saves teaches us to say no to ungodliness and live self-controlled, upright lives while waiting for the blessed hope.",
    themes: ["Grace that trains", "Good works", "Sound leadership", "The blessed hope", "Renewal by the Spirit"],
    keyVerses: [
      { ref: "titus-2-11", display: "Titus 2:11", note: "The grace of God has appeared, offering salvation to all." },
      { ref: "titus-3-5",  display: "Titus 3:5",  note: "He saved us through the washing of rebirth and renewal." },
      { ref: "titus-2-13", display: "Titus 2:13", note: "Waiting for the blessed hope — the appearing of our Savior." },
    ],
    relatedPrayers: ["spiritual-growth", "church", "prayer-for-leaders", "hope", "discipline"],
  },
  "philemon": {
    slug: "philemon",
    overview: "Philemon is a one-page masterpiece of gospel diplomacy. Paul sends the runaway slave Onesimus back to his master — not as property but as a dear brother — and offers to pay every debt himself: charge it to me. It is the gospel of reconciliation written into a single relationship.",
    themes: ["Reconciliation", "Forgiveness", "Brotherhood in Christ", "Advocacy", "Useful to God"],
    keyVerses: [
      { ref: "philemon-1-6",  display: "Philemon 1:6",  note: "Be active in sharing your faith." },
      { ref: "philemon-1-16", display: "Philemon 1:16", note: "No longer a slave, but a dear brother." },
      { ref: "philemon-1-18", display: "Philemon 1:18", note: "If he owes you anything, charge it to me." },
    ],
    relatedPrayers: ["forgiveness", "reconciliation", "friendship", "prayer-for-estranged-family", "love"],
  },
  "hebrews": {
    slug: "hebrews",
    overview: "Hebrews argues one majestic thesis: Jesus is better — better than angels, Moses, priests, and every old-covenant shadow. He is our great High Priest who sympathizes with weakness, so we can approach the throne of grace with confidence. Faith's hall of fame in chapter 11 fuels endurance for the race.",
    themes: ["Jesus our High Priest", "Approaching the throne boldly", "Faith heroes", "Endurance", "A better covenant"],
    keyVerses: [
      { ref: "hebrews-4-16", display: "Hebrews 4:16", note: "Approach the throne of grace with confidence." },
      { ref: "hebrews-11-1", display: "Hebrews 11:1", note: "Faith is confidence in what we hope for." },
      { ref: "hebrews-13-8", display: "Hebrews 13:8", note: "Jesus Christ — the same yesterday, today, and forever." },
    ],
    relatedPrayers: ["faith", "perseverance", "powerful-prayer", "trust", "spiritual-growth"],
  },
  "james": {
    slug: "james",
    overview: "James, the Lord's brother, writes the New Testament's wisdom book — blunt, practical, allergic to empty religion. Ask God for wisdom and He gives generously; faith without works is dead; the tongue is a fire; and the prayer of a righteous person is powerful and effective.",
    themes: ["Wisdom for the asking", "Faith that works", "Taming the tongue", "Patience in trials", "Powerful prayer"],
    keyVerses: [
      { ref: "james-1-5",  display: "James 1:5",  note: "If any of you lacks wisdom, ask God — He gives generously." },
      { ref: "james-5-16", display: "James 5:16", note: "The prayer of a righteous person is powerful and effective." },
      { ref: "james-4-8",  display: "James 4:8",  note: "Come near to God and He will come near to you." },
    ],
    relatedPrayers: ["wisdom", "powerful-prayer", "healing", "perseverance", "prayer-for-anger-management"],
  },
  "1-peter": {
    slug: "1-peter",
    overview: "1 Peter is written to exiles under fire — believers scattered and slandered across Asia Minor. Peter, who once cracked under pressure, now teaches how to stand: a living hope through resurrection, holiness in a hostile culture, and casting all anxiety on the One who cares for you.",
    themes: ["Living hope", "Casting anxiety on God", "Suffering with purpose", "Chosen identity", "Standing firm"],
    keyVerses: [
      { ref: "1-peter-5-7", display: "1 Peter 5:7", note: "Cast all your anxiety on Him because He cares for you." },
      { ref: "1-peter-2-9", display: "1 Peter 2:9", note: "A chosen people, a royal priesthood — called out of darkness." },
      { ref: "1-peter-5-10", display: "1 Peter 5:10", note: "After you have suffered a little while, God will restore you." },
    ],
    relatedPrayers: ["anxiety", "identity", "persecuted-christians", "hope", "stress"],
  },
  "2-peter": {
    slug: "2-peter",
    overview: "2 Peter is a dying apostle's reminder service: God's divine power has already given us everything needed for life and godliness through precious promises. It warns against scoffers and false teachers, explains the Lord's patience — not slow but not willing that any perish — and calls us to grow in grace.",
    themes: ["Precious promises", "Everything for godliness", "God's patience", "Guarding against deception", "Growing in grace"],
    keyVerses: [
      { ref: "2-peter-1-3", display: "2 Peter 1:3", note: "His divine power has given us everything we need." },
      { ref: "2-peter-3-9", display: "2 Peter 3:9", note: "The Lord is patient — not wanting anyone to perish." },
      { ref: "2-peter-3-18", display: "2 Peter 3:18", note: "Grow in the grace and knowledge of our Lord." },
    ],
    relatedPrayers: ["spiritual-growth", "faith", "wisdom", "perseverance", "salvation"],
  },
  "1-john": {
    slug: "1-john",
    overview: "1 John is the apostle of love's late-life letter — written so believers can know they have eternal life. God is light; God is love; perfect love drives out fear. Its tests are simple and searching: walk in the light, love one another, confess sin and find Him faithful to forgive.",
    themes: ["Assurance of salvation", "God is love", "Confession & cleansing", "Perfect love casts out fear", "Overcoming the world"],
    keyVerses: [
      { ref: "1-john-1-9", display: "1 John 1:9", note: "If we confess our sins, He is faithful and just to forgive." },
      { ref: "1-john-4-19", display: "1 John 4:19", note: "We love because He first loved us." },
      { ref: "1-john-4-4",  display: "1 John 4:4",  note: "Greater is He who is in you than he who is in the world." },
    ],
    relatedPrayers: ["love", "forgiveness", "fear", "identity", "salvation"],
  },
  "2-john": {
    slug: "2-john",
    overview: "2 John is a postcard-length letter to a chosen lady and her children — walk in truth, love one another, and do not open the door to teachers who deny Christ. In thirteen verses it holds the balance every believer needs: love without compromising truth, truth without abandoning love.",
    themes: ["Walking in truth", "Love & discernment", "Guarding sound doctrine", "Faithful continuance", "Full reward"],
    keyVerses: [
      { ref: "2-john-1-6", display: "2 John 1:6", note: "This is love: that we walk in obedience to His commands." },
      { ref: "2-john-1-8", display: "2 John 1:8", note: "Watch out that you do not lose what you have worked for." },
      { ref: "2-john-1-3", display: "2 John 1:3", note: "Grace, mercy and peace will be with us in truth and love." },
    ],
    relatedPrayers: ["family", "spiritual-growth", "love", "wisdom", "children"],
  },
  "3-john": {
    slug: "3-john",
    overview: "3 John is the Bible's shortest book — a personal note praising Gaius for hospitality to traveling missionaries and confronting Diotrephes, who loved being first. Its opening prayer is beloved: that you may prosper in all things and be in health, just as your soul prospers.",
    themes: ["Prosperity of soul & body", "Hospitality", "Supporting gospel workers", "Humility vs. self-promotion", "Imitating good"],
    keyVerses: [
      { ref: "3-john-1-2", display: "3 John 1:2", note: "May you prosper and be in health, as your soul prospers." },
      { ref: "3-john-1-4", display: "3 John 1:4", note: "No greater joy than hearing my children walk in truth." },
      { ref: "3-john-1-11", display: "3 John 1:11", note: "Do not imitate what is evil but what is good." },
    ],
    relatedPrayers: ["health-discipline", "healing", "missionaries", "blessing", "provision"],
  },
  "jude": {
    slug: "jude",
    overview: "Jude planned a friendly letter about salvation but grabbed a trumpet instead: contend for the faith once delivered to the saints. In twenty-five verses he exposes infiltrating false teachers, then closes with one of Scripture's greatest benedictions — to Him who is able to keep you from stumbling.",
    themes: ["Contending for the faith", "Kept from stumbling", "Building yourselves up in prayer", "Mercy for doubters", "God's keeping power"],
    keyVerses: [
      { ref: "jude-1-24", display: "Jude 1:24", note: "To Him who is able to keep you from stumbling." },
      { ref: "jude-1-20", display: "Jude 1:20", note: "Build yourselves up — praying in the Holy Spirit." },
      { ref: "jude-1-22", display: "Jude 1:22", note: "Be merciful to those who doubt." },
    ],
    relatedPrayers: ["spiritual-warfare", "doubt", "perseverance", "prayer-for-holy-spirit", "protection"],
  },
  "revelation": {
    slug: "revelation",
    overview: "Revelation pulls back the curtain on history's finale: the risen Christ walking among His churches, the throne room of heaven, judgment on evil, and a new heaven and new earth where God wipes every tear away. Written to persecuted believers, its message is singular — the Lamb wins.",
    themes: ["Christ's ultimate victory", "Heaven's worship", "Perseverance of the saints", "Every tear wiped away", "The new creation"],
    keyVerses: [
      { ref: "revelation-21-4", display: "Revelation 21:4", note: "He will wipe every tear — no more death or mourning." },
      { ref: "revelation-3-20", display: "Revelation 3:20", note: "I stand at the door and knock." },
      { ref: "revelation-12-11", display: "Revelation 12:11", note: "They triumphed by the blood of the Lamb and their testimony." },
    ],
    relatedPrayers: ["hope", "comfort", "protection", "worship", "perseverance"],
  },
};

/** Safe lookup with sensible defaults for prayer slugs that may not exist */
const VALID_FALLBACK = ["morning", "healing", "strength", "peace", "hope", "faith"];

export function getBookContent(slug: string): BookContent | null {
  return BOOK_CONTENT[slug] ?? null;
}

/** Related prayers for a book slug — complete coverage for all 66 books */
export function getRelatedPrayersForBook(slug: string): string[] {
  return BOOK_CONTENT[slug]?.relatedPrayers ?? VALID_FALLBACK;
}
