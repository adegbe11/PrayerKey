import type { PrayerTopic } from "./prayer-topics";

export const BATCH3_PRAYER_TOPICS: PrayerTopic[] = [
  // ── DAYS OF THE WEEK ──────────────────────────────────────────────────────
  {
    slug: "monday-morning-prayer",
    title: "Monday Morning Prayer",
    metaDesc: "Start your week strong with this Monday morning prayer. Invite God's presence, peace, and purpose into every moment of your week ahead.",
    category: "Daily Prayer",
    keywords: ["monday morning prayer", "prayer for monday morning", "monday prayer", "start of week prayer", "monday blessing prayer"],
    samplePrayer: "Lord, thank You for this Monday morning and the gift of a brand-new week. As I step into what lies ahead, I ask for Your grace to cover every appointment, every challenge, and every conversation. Let Your wisdom guide my decisions and Your peace guard my heart when pressure rises. Protect me from distraction and discouragement. Renew my strength like the eagle's, and help me to honor You in my work, my words, and my attitude. May this week be fruitful and full of Your blessing. In Jesus' name, Amen.",
    scripture: [
      { ref: "Lamentations 3:22-23", text: "Because of the Lord's great love we are not consumed, for his compassions never fail. They are new every morning; great is your faithfulness." },
      { ref: "Psalm 90:12", text: "Teach us to number our days, that we may gain a heart of wisdom." }
    ],
    related: ["morning", "daily", "tuesday-morning-prayer", "prayer-for-a-good-day", "wednesday-prayer"]
  },
  {
    slug: "tuesday-morning-prayer",
    title: "Tuesday Morning Prayer",
    metaDesc: "Anchor your Tuesday in faith with this heartfelt morning prayer. Ask God for focus, endurance, and His presence through the middle of your week.",
    category: "Daily Prayer",
    keywords: ["tuesday morning prayer", "prayer for tuesday morning", "tuesday prayer", "midweek morning prayer", "tuesday blessing"],
    samplePrayer: "Heavenly Father, I come to You this Tuesday morning with a grateful heart. Thank You for bringing me safely through Monday and into this new day. I ask that You fill me with renewed energy and clear focus as I continue the work of this week. Where yesterday left things unfinished, give me perseverance. Where yesterday was discouraging, restore my hope. Let Your Holy Spirit be my constant companion today, reminding me of Your promises when the afternoon grows long and my spirit grows weary. I trust You with this day. In Jesus' name, Amen.",
    scripture: [
      { ref: "Isaiah 40:29", text: "He gives strength to the weary and increases the power of the weak." },
      { ref: "Philippians 4:13", text: "I can do all this through him who gives me strength." }
    ],
    related: ["monday-morning-prayer", "wednesday-prayer", "morning", "daily", "prayer-for-a-good-day"]
  },
  {
    slug: "wednesday-prayer",
    title: "Wednesday Prayer — Midweek Strength",
    metaDesc: "This Wednesday prayer helps you find God's strength at the midpoint of your week. Reset, refocus, and press forward in faith and purpose.",
    category: "Daily Prayer",
    keywords: ["wednesday prayer", "wednesday morning prayer", "midweek prayer", "prayer for wednesday", "hump day prayer"],
    samplePrayer: "Lord God, here I am at the middle of the week, and I need Your strength to keep going. Some days feel longer than others, and this Wednesday is one of those days I need to know You are near. Thank You for carrying me this far. Revive my spirit and rekindle the fire within me. Help me to push past fatigue and finish strong. Let Your joy be my fuel and Your Word be my anchor. Whatever the second half of this week holds, I face it with You. In Jesus' name, Amen.",
    scripture: [
      { ref: "Nehemiah 8:10", text: "Do not grieve, for the joy of the Lord is your strength." },
      { ref: "2 Corinthians 12:9", text: "My grace is sufficient for you, for my power is made perfect in weakness." }
    ],
    related: ["tuesday-morning-prayer", "thursday-morning-prayer", "morning", "strength", "daily"]
  },
  {
    slug: "thursday-morning-prayer",
    title: "Thursday Morning Prayer",
    metaDesc: "A Thursday morning prayer to carry you through the final stretch of the week. Ask God for patience, productivity, and His covering over your day.",
    category: "Daily Prayer",
    keywords: ["thursday morning prayer", "prayer for thursday morning", "thursday prayer", "thursday blessing prayer", "almost friday prayer"],
    samplePrayer: "Father, I wake this Thursday morning choosing to trust You. The week is almost done, and yet there is still much to do. I pray for endurance and a spirit that does not give up. Help me to finish the commitments I began on Monday with excellence and integrity. Guard my words in every conversation today. Bless the work of my hands and the plans of my heart that align with Your will. I am grateful for the progress You have helped me make. Carry me through today with grace. In Jesus' name, Amen.",
    scripture: [
      { ref: "Galatians 6:9", text: "Let us not become weary in doing good, for at the proper time we will reap a harvest if we do not give up." },
      { ref: "Proverbs 16:3", text: "Commit to the Lord whatever you do, and he will establish your plans." }
    ],
    related: ["wednesday-prayer", "friday-morning-prayer", "morning", "daily", "perseverance"]
  },
  {
    slug: "friday-morning-prayer",
    title: "Friday Morning Prayer",
    metaDesc: "End the workweek right with this Friday morning prayer. Thank God for the week's blessings and ask for peace and joy going into the weekend.",
    category: "Daily Prayer",
    keywords: ["friday morning prayer", "prayer for friday morning", "friday prayer", "end of week prayer", "friday blessing prayer"],
    samplePrayer: "Lord, thank You for this Friday morning — the finish line of another week. I am grateful for every day You have brought me through, every challenge You have helped me face, and every blessing You have poured out. As I approach the end of this workweek, I pray for a heart full of gratitude rather than exhaustion. Help me to finish today with the same energy and focus I began with on Monday. Bless my family as we head into the weekend together. Let Your peace fill our home. I give this day to You. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 118:24", text: "This is the day the Lord has made; we will rejoice and be glad in it." },
      { ref: "Deuteronomy 28:6", text: "You will be blessed when you come in and blessed when you go out." }
    ],
    related: ["thursday-morning-prayer", "saturday-morning-prayer", "morning", "thanksgiving", "daily"]
  },
  {
    slug: "saturday-morning-prayer",
    title: "Saturday Morning Prayer",
    metaDesc: "Begin your Saturday with this restful, joy-filled morning prayer. Invite God into your day of rest, family, and weekend refreshment.",
    category: "Daily Prayer",
    keywords: ["saturday morning prayer", "prayer for saturday morning", "saturday prayer", "weekend prayer", "saturday blessing prayer"],
    samplePrayer: "Good morning, Lord. Thank You for Saturday — a day to breathe, rest, and be refreshed. I don't want to rush past this gift of a slower morning without first spending time with You. Fill my heart with joy and my home with Your peace today. Guide me in how I spend these hours — whether in rest, service, or celebration. Help me to be fully present with the people I love. Protect our travels and our leisure. And even as I rest my body, let my spirit remain anchored in You. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 23:2-3", text: "He makes me lie down in green pastures, he leads me beside quiet waters, he refreshes my soul." },
      { ref: "Mark 2:27", text: "The Sabbath was made for man, not man for the Sabbath." }
    ],
    related: ["friday-morning-prayer", "sunday-morning-prayer", "morning", "peace", "daily"]
  },
  {
    slug: "sunday-morning-prayer",
    title: "Sunday Morning Prayer",
    metaDesc: "Open your Sunday with this uplifting morning prayer. Prepare your heart for worship, the Word, and the presence of God as you gather with believers.",
    category: "Daily Prayer",
    keywords: ["sunday morning prayer", "prayer for sunday morning", "sunday prayer", "prayer before church", "sunday worship prayer"],
    samplePrayer: "Lord, what a joy to wake up on Sunday knowing that today is Your day. Thank You for the gift of worship and community. As I prepare to gather with Your people, prepare my heart to receive every word, song, and moment You have planned. Silence the distractions of the week behind me and the week ahead. Let me enter Your house with thanksgiving and leave transformed. Speak to me today through Your Word. Fill the church with Your Spirit. May every person who walks through those doors encounter Your love. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 122:1", text: "I rejoiced with those who said to me, 'Let us go to the house of the Lord.'" },
      { ref: "Hebrews 10:25", text: "Not giving up meeting together, as some are in the habit of doing, but encouraging one another." }
    ],
    related: ["saturday-morning-prayer", "monday-morning-prayer", "morning", "church", "worship"]
  },

  // ── SPECIFIC FAMILY MEMBERS ───────────────────────────────────────────────
  {
    slug: "prayer-for-mom",
    title: "Prayer for Mom",
    metaDesc: "A heartfelt prayer for your mom — for her health, happiness, strength, and God's blessing over every season of her life. Honor her with prayer.",
    category: "Family",
    keywords: ["prayer for mom", "prayer for my mother", "prayer for mothers", "prayer for my mom", "blessing prayer for mom"],
    samplePrayer: "Father, I lift up my mom to You with a heart full of love and gratitude. Thank You for the gift of her life in mine. Bless her with good health, long life, and overflowing joy. Where she is tired, renew her strength. Where she carries worry, replace it with Your peace. Reward her faithfulness, her sacrifice, and her love. Protect her from every danger and every sickness. Let her know each day that she is deeply valued and loved — by You and by me. Cover her with Your grace and surround her with good people. In Jesus' name, Amen.",
    scripture: [
      { ref: "Proverbs 31:25", text: "She is clothed with strength and dignity; she can laugh at the days to come." },
      { ref: "Exodus 20:12", text: "Honor your father and your mother, so that you may live long in the land the Lord your God is giving you." }
    ],
    related: ["mothers", "parents", "prayer-for-dad", "family", "prayer-for-grandma"]
  },
  {
    slug: "prayer-for-dad",
    title: "Prayer for Dad",
    metaDesc: "Pray for your father with this sincere prayer for dads. Ask God to strengthen, protect, and bless your dad with health, wisdom, and lasting joy.",
    category: "Family",
    keywords: ["prayer for dad", "prayer for my father", "prayer for my dad", "blessing prayer for dad", "prayer for fathers"],
    samplePrayer: "Lord, I come before You on behalf of my father. Thank You for the gift of a dad — for the sacrifices he has made and the love he has shown, even imperfectly. Bless him with strong health and a sound mind. Strengthen him in his body and in his spirit. Where the years have been hard on him, restore what has been worn down. Give him joy in this season, purpose in his days, and peace in his heart. Help me to honor him well, and let him know how much I love him. Guard his life, Lord. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 103:13", text: "As a father has compassion on his children, so the Lord has compassion on those who fear him." },
      { ref: "Proverbs 17:6", text: "Children's children are a crown to the aged, and parents are the pride of their children." }
    ],
    related: ["fathers", "parents", "prayer-for-mom", "family", "prayer-for-grandpa"]
  },
  {
    slug: "prayer-for-grandparents",
    title: "Prayer for Grandparents",
    metaDesc: "A touching prayer for grandparents — for their health, comfort, protection, and joy. Bless the precious elders who have shaped your family's legacy.",
    category: "Family",
    keywords: ["prayer for grandparents", "prayer for my grandparents", "prayer for grandma and grandpa", "blessing for grandparents", "prayer for elderly grandparents"],
    samplePrayer: "Gracious Father, I lift up my grandparents to You. Thank You for their lives, their legacy, and the love they have poured into our family. Bless them with good health and the comfort of Your presence in their older years. Where their bodies ache, bring healing and relief. Where they feel forgotten, remind them they are treasured. Surround them with people who care for them well. Fill their days with joy, laughter, and the peace that only You can give. May they finish their race strong and with deep faith. In Jesus' name, Amen.",
    scripture: [
      { ref: "Proverbs 16:31", text: "Gray hair is a crown of splendor; it is attained in the way of righteousness." },
      { ref: "Psalm 92:14", text: "They will still bear fruit in old age, they will stay fresh and green." }
    ],
    related: ["prayer-for-grandma", "prayer-for-grandpa", "elderly", "parents", "prayer-for-mom"]
  },
  {
    slug: "prayer-for-grandma",
    title: "Prayer for Grandma",
    metaDesc: "A loving prayer for grandma — for her health, peace, and God's comfort. Honor your grandmother by lifting her before the throne of grace.",
    category: "Family",
    keywords: ["prayer for grandma", "prayer for my grandmother", "prayer for grandmother", "blessing for grandma", "prayer for grandma's health"],
    samplePrayer: "Lord, I bring my grandma before You with so much love. She has been such a steady presence in my life — her prayers, her wisdom, her warmth. I ask that You bless her abundantly. Protect her health and grant her comfort in every ache and pain. Keep her mind sharp and her spirit cheerful. Let her feel surrounded by Your love and the love of family. Give her peaceful nights and joyful days. Reward her faithfulness, Lord, and let her know that her life has been a profound gift to us. In Jesus' name, Amen.",
    scripture: [
      { ref: "Titus 2:3", text: "Likewise, teach the older women to be reverent in the way they live, not to be slanderers or addicted to much wine, but to teach what is good." },
      { ref: "Psalm 71:18", text: "Even when I am old and gray, do not forsake me, my God, till I declare your power to the next generation." }
    ],
    related: ["prayer-for-grandparents", "prayer-for-grandpa", "elderly", "prayer-for-mom", "parents"]
  },
  {
    slug: "prayer-for-grandpa",
    title: "Prayer for Grandpa",
    metaDesc: "Pray for your grandfather with this warm, sincere prayer. Ask God to strengthen, protect, and bring deep joy to the patriarch of your family.",
    category: "Family",
    keywords: ["prayer for grandpa", "prayer for my grandfather", "prayer for grandfather", "blessing for grandpa", "prayer for grandpa's health"],
    samplePrayer: "Heavenly Father, I come to You with a heart full of love for my grandpa. He has walked through so much and shown me what faithfulness looks like. Today I ask that You honor him with good health, strong days, and a spirit that stays young in You. Ease any pain that troubles his body. Guard his mind and keep it clear. Let the joy of the Lord be his daily companion. Surround him with people who love and honor him. And Lord, let him know — truly know — how much he means to this family and to me. In Jesus' name, Amen.",
    scripture: [
      { ref: "Job 12:12", text: "Is not wisdom found among the aged? Does not long life bring understanding?" },
      { ref: "Proverbs 20:29", text: "The glory of young men is their strength, gray hair the splendor of the old." }
    ],
    related: ["prayer-for-grandparents", "prayer-for-grandma", "elderly", "prayer-for-dad", "fathers"]
  },
  {
    slug: "prayer-for-sister",
    title: "Prayer for Sister",
    metaDesc: "A heartfelt prayer for your sister — for her peace, success, health, and God's protection over her life. Lift your sister up in prayer today.",
    category: "Family",
    keywords: ["prayer for sister", "prayer for my sister", "blessing prayer for sister", "prayer for a sister", "short prayer for sister"],
    samplePrayer: "Lord, I bring my sister before You today with love. She means so much to me, and I want Your best for her life. Bless her in her work, her relationships, and her dreams. Protect her from harm, from toxic situations, and from anything that would pull her away from You. In moments when she is struggling, be her comfort and strength. When she is thriving, be her anchor so that pride does not take root. Bind us together in genuine love and support. Help me to be a good sibling to her — present, kind, and faithful. In Jesus' name, Amen.",
    scripture: [
      { ref: "Ruth 1:16", text: "Where you go I will go, and where you stay I will stay. Your people will be my people and your God my God." },
      { ref: "Proverbs 17:17", text: "A friend loves at all times, and a brother is born for a time of adversity." }
    ],
    related: ["birthday-prayer-for-sister", "family", "prayer-for-brother", "prayer-for-mom", "friendship"]
  },
  {
    slug: "prayer-for-brother",
    title: "Prayer for Brother",
    metaDesc: "A sincere prayer for your brother — for his protection, success, faith, and God's guidance over his life. Stand in the gap for your brother today.",
    category: "Family",
    keywords: ["prayer for brother", "prayer for my brother", "blessing prayer for brother", "prayer for a brother", "prayer for brother's success"],
    samplePrayer: "Father, I stand in prayer for my brother today. I love him more than words can say, and I bring him before You. Protect him physically and spiritually. Guard his steps and keep him from paths that lead to harm. Bless the work of his hands and give him favor in his career. Where he is struggling, be his strength. Where he has gone astray, draw him back with cords of love. Help us to be close — to look out for each other as brothers should. Let Your light shine in his life. In Jesus' name, Amen.",
    scripture: [
      { ref: "Genesis 4:9", text: "Then the Lord said to Cain, 'Where is your brother Abel?'" },
      { ref: "1 Samuel 18:1", text: "After David had finished talking with Saul, Jonathan became one in spirit with David, and he loved him as himself." }
    ],
    related: ["birthday-prayer-for-brother", "family", "prayer-for-sister", "prayer-for-dad", "fathers"]
  },
  {
    slug: "prayer-for-grandchildren",
    title: "Prayer for Grandchildren",
    metaDesc: "A grandparent's prayer for grandchildren — for their salvation, safety, purpose, and lifelong walk with God. Pray a legacy of faith over them.",
    category: "Family",
    keywords: ["prayer for grandchildren", "prayer for my grandchildren", "grandparent prayer for grandchildren", "blessing prayer for grandchildren", "prayer over grandkids"],
    samplePrayer: "Lord, my grandchildren are the greatest treasure You have placed in my life in this season. I pray over each one of them by name before Your throne. Protect them from the dangers of this world — seen and unseen. Draw them close to Your heart from a young age. Let the faith I have tried to model leave a lasting mark on their souls. Give them wisdom, godly friends, and a burning love for Your Word. Bless their health, their futures, and every dream they carry. May each one grow to know and love You personally. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 128:6", text: "May you live to see your children's children — peace be on Israel." },
      { ref: "Proverbs 22:6", text: "Start children off on the way they should go, and even when they are old they will not turn from it." }
    ],
    related: ["prayer-for-grandparents", "children", "parents", "prayer-for-grandma", "family"]
  },
  {
    slug: "prayer-for-niece-nephew",
    title: "Prayer for Niece or Nephew",
    metaDesc: "Pray for your niece or nephew with this loving prayer. Ask God to protect, bless, and guide the precious young person in your extended family.",
    category: "Family",
    keywords: ["prayer for niece", "prayer for nephew", "prayer for niece and nephew", "blessing prayer for niece", "prayer for my nephew"],
    samplePrayer: "Father, I come before You on behalf of my niece and nephew, the ones who make me smile just thinking of them. Protect them in every environment — at school, with friends, and in the digital world. Give them a strong sense of identity rooted in You. When they face peer pressure, give them courage to stand firm. Bless their education and open the right doors for their futures. May they always feel the love of this family wrapped around them like Your arms. Draw them to Yourself, Lord, in a way they cannot ignore. In Jesus' name, Amen.",
    scripture: [
      { ref: "Mark 10:14", text: "Let the little children come to me, and do not hinder them, for the kingdom of God belongs to such as these." },
      { ref: "3 John 1:4", text: "I have no greater joy than to hear that my children are walking in the truth." }
    ],
    related: ["children", "family", "prayer-for-sister", "prayer-for-brother", "youth-prayer"]
  },

  // ── HEALTH CONDITIONS ─────────────────────────────────────────────────────
  {
    slug: "prayer-for-arthritis",
    title: "Prayer for Arthritis",
    metaDesc: "A prayer for relief from arthritis pain. Ask God for healing, reduced inflammation, strength in your joints, and peace in the midst of daily pain.",
    category: "Health",
    keywords: ["prayer for arthritis", "prayer for arthritis pain relief", "healing prayer for arthritis", "prayer for joint pain", "prayer for rheumatoid arthritis"],
    samplePrayer: "Lord Jesus, You are the Healer, and I bring this arthritis before You. The pain in my joints is real, and some days it weighs heavily on my spirit as much as my body. I ask for Your healing touch to move through every inflamed joint and ease this suffering. Give me strength on the hard days and grace to receive help from others. Let the treatments I receive be effective. I trust that healing belongs to me, bought at the cross. Remove the inflammation and restore mobility to my body. I receive Your healing by faith. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 103:3", text: "Who forgives all your sins and heals all your diseases." },
      { ref: "Jeremiah 30:17", text: "'But I will restore you to health and heal your wounds,' declares the Lord." }
    ],
    related: ["healing", "chronic-pain", "prayer-for-fibromyalgia", "recovery", "prayer-for-migraines"]
  },
  {
    slug: "prayer-for-lupus",
    title: "Prayer for Lupus",
    metaDesc: "A prayer for those living with lupus. Ask God for healing, strength through flare-ups, peace of mind, and supernatural restoration of your body.",
    category: "Health",
    keywords: ["prayer for lupus", "prayer for someone with lupus", "healing prayer for lupus", "lupus prayer", "prayer for autoimmune disease"],
    samplePrayer: "Father, this lupus diagnosis did not catch You by surprise, even if it caught me off guard. I bring every symptom, every flare, and every fear before You right now. You made my body and You can restore it. I pray for Your supernatural healing to work alongside every treatment and medication. Grant my doctors wisdom to manage this well. On the hard days, remind me that Your grace is sufficient and that I am not fighting alone. Protect my organs, calm the inflammation, and strengthen my immune system according to Your design. I am Yours, Lord. Heal me. In Jesus' name, Amen.",
    scripture: [
      { ref: "Isaiah 53:5", text: "But he was pierced for our transgressions, he was crushed for our iniquities; the punishment that brought us peace was on him, and by his wounds we are healed." },
      { ref: "Romans 8:11", text: "And if the Spirit of him who raised Jesus from the dead is living in you, he who raised Christ from the dead will also give life to your mortal bodies." }
    ],
    related: ["healing", "autoimmune", "chronic-pain", "prayer-for-arthritis", "recovery"]
  },
  {
    slug: "prayer-for-fibromyalgia",
    title: "Prayer for Fibromyalgia",
    metaDesc: "A prayer for fibromyalgia sufferers. Bring your chronic pain and exhaustion to God and ask for His healing, rest, and peace that passes understanding.",
    category: "Health",
    keywords: ["prayer for fibromyalgia", "fibromyalgia prayer", "prayer for chronic pain", "prayer for fibro pain", "healing prayer for fibromyalgia"],
    samplePrayer: "Loving Father, fibromyalgia is an invisible battle that others cannot always see, but You see every ache, every sleepless night, and every exhausted morning. I come to You because I need You. I ask for Your healing hand to move through my nervous system and bring order and relief. Give me restful sleep and days with less pain. Help me to pace myself wisely and not give in to discouragement. Surround me with people who understand and support me. I believe You are a God who heals, and I ask You to heal me now, for Your glory. In Jesus' name, Amen.",
    scripture: [
      { ref: "Matthew 11:28", text: "Come to me, all you who are weary and burdened, and I will give you rest." },
      { ref: "Psalm 34:18", text: "The Lord is close to the brokenhearted and saves those who are crushed in spirit." }
    ],
    related: ["chronic-pain", "prayer-for-arthritis", "healing", "sleep", "prayer-for-lupus"]
  },
  {
    slug: "prayer-for-parkinsons",
    title: "Prayer for Parkinson's Disease",
    metaDesc: "A prayer for Parkinson's disease — for the person facing tremors, movement challenges, and uncertainty. Ask God for healing, peace, and daily strength.",
    category: "Health",
    keywords: ["prayer for Parkinson's", "prayer for Parkinson's disease", "Parkinson's healing prayer", "prayer for someone with Parkinson's", "prayer for tremors"],
    samplePrayer: "Lord, I bring before You this battle with Parkinson's disease. The tremors, the slow movements, the uncertainty about what lies ahead — all of this I lay at Your feet. You are the God who holds my body and my future in Your hands. I ask for Your miraculous healing to begin even now. Give the doctors wisdom in managing this condition. Grant peace that quiets the anxiety about tomorrow. Let me live fully in each day, not defined by a diagnosis but defined by who You say I am. I trust You, Lord. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 46:1", text: "God is our refuge and strength, an ever-present help in trouble." },
      { ref: "2 Corinthians 4:16", text: "Therefore we do not lose heart. Though outwardly we are wasting away, yet inwardly we are being renewed day by day." }
    ],
    related: ["healing", "chronic-pain", "prayer-for-dementia", "alzheimers", "recovery"]
  },
  {
    slug: "prayer-for-multiple-sclerosis",
    title: "Prayer for Multiple Sclerosis",
    metaDesc: "A prayer for those with multiple sclerosis (MS). Ask God for healing of the nervous system, strength through relapses, and daily courage to press on.",
    category: "Health",
    keywords: ["prayer for multiple sclerosis", "prayer for MS", "prayer for someone with MS", "MS healing prayer", "prayer for neurological disease"],
    samplePrayer: "Father God, You are the God who created every nerve in my body, and nothing about multiple sclerosis is beyond Your power to heal. I bring this disease before Your throne. In the moments when my body does not cooperate, remind me that my identity and my hope are not in my physical ability but in You. I pray for remission, for fewer relapses, and for the treatments to work effectively. Give me emotional resilience when I feel I cannot go on. Strengthen my faith. And Lord, I ask boldly — touch my nervous system and restore it. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 30:2", text: "Lord my God, I called to you for help, and you healed me." },
      { ref: "Isaiah 41:10", text: "So do not fear, for I am with you; do not be dismayed, for I am your God. I will strengthen you and help you." }
    ],
    related: ["healing", "chronic-pain", "disability", "prayer-for-lupus", "recovery"]
  },
  {
    slug: "prayer-for-sickle-cell",
    title: "Prayer for Sickle Cell Disease",
    metaDesc: "A prayer for sickle cell disease — for those in crisis, for their families, and for God's healing touch on blood, bones, and every organ affected.",
    category: "Health",
    keywords: ["prayer for sickle cell", "prayer for sickle cell disease", "sickle cell healing prayer", "prayer for sickle cell crisis", "prayer for blood disorder"],
    samplePrayer: "Lord Jesus, You turned water into wine, so I know You can change what is in my blood. I come before You on behalf of everyone fighting sickle cell disease — the painful crises, the hospital visits, the fear of the unknown. Be near to each one. I ask for Your supernatural touch on the hemoglobin in my blood. Prevent sickle cell crises and ease the pain in my bones and organs. Give my doctors new insights and treatments. Lord, I believe in miracles, and I am asking for one. Turn this disease into a testimony of Your healing power. In Jesus' name, Amen.",
    scripture: [
      { ref: "Exodus 15:26", text: "I am the Lord, who heals you." },
      { ref: "Psalm 107:20", text: "He sent out his word and healed them; he rescued them from the grave." }
    ],
    related: ["healing", "hospital", "chronic-pain", "recovery", "prayer-for-lupus"]
  },
  {
    slug: "prayer-for-dementia",
    title: "Prayer for Dementia",
    metaDesc: "A prayer for those with dementia and their caregivers. Ask God for dignity, peace, moments of clarity, and the comfort of His presence through it all.",
    category: "Health",
    keywords: ["prayer for dementia", "prayer for someone with dementia", "dementia healing prayer", "prayer for dementia patient", "prayer for memory loss"],
    samplePrayer: "Precious Lord, I bring before You my loved one who is walking through dementia. Even when memory fails and confusion comes, You know them by name. You have not forgotten them, and neither have I. I pray for peaceful days and gentle moments for them. Protect their dignity and surround them with patient, loving care. Grant me as a caregiver the endurance and compassion this journey requires. Let them still feel Your presence even when they cannot express it. And Lord, if it is Your will, bring moments of clarity that remind us of who they truly are. In Jesus' name, Amen.",
    scripture: [
      { ref: "Isaiah 46:4", text: "Even to your old age and gray hairs I am he, I am he who will sustain you. I have made you and I will carry you." },
      { ref: "Psalm 23:4", text: "Even though I walk through the darkest valley, I will fear no evil, for you are with me." }
    ],
    related: ["alzheimers", "healing", "elderly", "prayer-for-grandparents", "caregiver"]
  },
  {
    slug: "prayer-for-liver-disease",
    title: "Prayer for Liver Disease",
    metaDesc: "A healing prayer for liver disease, cirrhosis, or hepatitis. Bring your diagnosis before God and ask for supernatural restoration of your liver and health.",
    category: "Health",
    keywords: ["prayer for liver disease", "prayer for liver healing", "prayer for cirrhosis", "prayer for hepatitis", "liver healing prayer"],
    samplePrayer: "Father, You formed every organ in my body, including my liver, and You know exactly what is happening there right now. I bring this diagnosis before You, trusting that nothing is too hard for You to heal. I pray for the regeneration of healthy liver tissue. Give my doctors wisdom in treatment. Guard me from complications and let every test result move in a better direction. Help me to make the lifestyle changes that support healing. But above all, I trust in Your supernatural power. Touch my liver and make it whole again, Lord. In Jesus' name, Amen.",
    scripture: [
      { ref: "3 John 1:2", text: "Dear friend, I pray that you may enjoy good health and that all may go well with you, even as your soul is getting along well." },
      { ref: "Jeremiah 17:14", text: "Heal me, Lord, and I will be healed; save me and I will be saved, for you are the one I praise." }
    ],
    related: ["healing", "hospital", "recovery", "surgery", "chronic-pain"]
  },
  {
    slug: "prayer-for-eczema",
    title: "Prayer for Eczema",
    metaDesc: "A prayer for eczema healing — for relief from itching, inflammation, and skin discomfort. Ask God to restore your skin and bring lasting relief.",
    category: "Health",
    keywords: ["prayer for eczema", "prayer for skin healing", "prayer for eczema healing", "eczema prayer", "prayer for skin condition"],
    samplePrayer: "Lord, the discomfort of eczema is more than skin deep — it affects my confidence, my sleep, and my daily life. I come before You and ask for healing from the inside out. Calm the inflammation and reduce the flare-ups. Help me to identify and avoid the triggers that make it worse. Give the doctors and dermatologists wisdom in prescribing what will truly help. Restore my skin to health and wholeness. Let this struggle draw me closer to You rather than push me toward discouragement. I trust Your healing Word over my body. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 103:3", text: "Who forgives all your sins and heals all your diseases." },
      { ref: "2 Kings 20:5", text: "This is what the Lord, the God of your father David, says: I have heard your prayer and seen your tears; I will heal you." }
    ],
    related: ["healing", "recovery", "prayer-for-lupus", "prayer-for-arthritis", "chronic-pain"]
  },
  {
    slug: "prayer-for-hair-loss",
    title: "Prayer for Hair Loss",
    metaDesc: "A prayer for hair loss and alopecia — for healing, confidence, and God's comfort during a season that affects both body and self-image.",
    category: "Health",
    keywords: ["prayer for hair loss", "prayer for alopecia", "prayer to stop hair loss", "prayer for hair growth", "prayer for hair thinning"],
    samplePrayer: "Lord, I know hair loss might seem small compared to other battles, but it has affected how I see myself, and I bring it honestly to You. You know my confidence, my appearance, and the grief I feel about this change in my body. I ask for healing — whether through medical treatment, lifestyle changes, or Your miraculous touch. Help me to find my identity in You, not in my appearance. And Lord, if it is Your will, restore my hair. But more than that, restore my peace and confidence in knowing I am fearfully and wonderfully made. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 139:14", text: "I praise you because I am fearfully and wonderfully made; your works are wonderful, I know that full well." },
      { ref: "Luke 12:7", text: "Indeed, the very hairs of your head are all numbered. Don't be afraid; you are worth more than many sparrows." }
    ],
    related: ["healing", "identity", "prayer-for-eczema", "recovery", "prayer-for-lupus"]
  },
  {
    slug: "prayer-for-migraines",
    title: "Prayer for Migraines",
    metaDesc: "A prayer for migraine relief — for the debilitating pain, nausea, and sensitivity that migraines bring. Ask God to heal and protect you from future attacks.",
    category: "Health",
    keywords: ["prayer for migraines", "prayer for migraine relief", "prayer for headache", "migraine healing prayer", "prayer for severe headache"],
    samplePrayer: "Lord, this migraine pain is overwhelming. I cannot function, I can barely open my eyes, and I need You right now. Touch my head with Your healing hands. Ease this pressure, quiet the pounding, and reduce the sensitivity to light and sound. Help the medication to work quickly and effectively. In this painful moment, be my peace. And Lord, I ask not just for relief today but for protection from future attacks. Reveal any triggers I need to address. Heal my body and help me to rest. You are my healer. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 6:2", text: "Have mercy on me, Lord, for I am faint; heal me, Lord, for my bones are in agony." },
      { ref: "Isaiah 40:29", text: "He gives strength to the weary and increases the power of the weak." }
    ],
    related: ["healing", "chronic-pain", "prayer-for-arthritis", "recovery", "prayer-for-fibromyalgia"]
  },
  {
    slug: "prayer-for-nausea",
    title: "Prayer for Nausea",
    metaDesc: "A short prayer for nausea and upset stomach. Ask God for swift relief from nausea, whether from illness, pregnancy, anxiety, or treatment side effects.",
    category: "Health",
    keywords: ["prayer for nausea", "prayer for upset stomach", "prayer for nausea relief", "prayer for vomiting", "healing prayer for nausea"],
    samplePrayer: "Lord, I feel sick right now, and I ask for Your healing touch on my stomach and my body. You commanded storms to be still — I ask You to still this nausea. Whether it comes from illness, pregnancy, medication, or anxiety, I believe You can bring quick relief. Settle my stomach, ease the discomfort, and help me to rest and recover. If there is something my body is telling me that I need to address with a doctor, give me wisdom to do so. Thank You for caring even about the small, uncomfortable things. In Jesus' name, Amen.",
    scripture: [
      { ref: "Mark 4:39", text: "He got up, rebuked the wind and said to the waves, 'Quiet! Be still!' Then the wind died down and it was completely calm." },
      { ref: "Psalm 41:3", text: "The Lord sustains them on their sickbed and restores them from their bed of illness." }
    ],
    related: ["healing", "recovery", "hospital", "prayer-for-migraines", "healthy-pregnancy"]
  },
  {
    slug: "prayer-for-kidney-stones",
    title: "Prayer for Kidney Stones",
    metaDesc: "A prayer for kidney stone pain and passage. Ask God for swift relief, quick healing, and protection from future kidney stones and complications.",
    category: "Health",
    keywords: ["prayer for kidney stones", "prayer for kidney stone pain", "prayer to pass kidney stone", "kidney stone healing prayer", "prayer for kidney health"],
    samplePrayer: "Father, the pain of a kidney stone is severe, and I come to You in great discomfort. You spoke the world into being, and I ask You now to speak peace and healing into my body. Help this stone to pass quickly and completely. Reduce the pain and ease the process. Protect my kidneys from damage. Give my doctors and medical team clear direction in their care for me. And Lord, help me to make the diet and lifestyle changes that prevent this from happening again. I trust Your healing, and I thank You for carrying me through this. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 107:19-20", text: "Then they cried to the Lord in their trouble, and he saved them from their distress. He sent out his word and healed them." },
      { ref: "Jeremiah 30:17", text: "'But I will restore you to health and heal your wounds,' declares the Lord." }
    ],
    related: ["healing", "hospital", "kidney-disease", "recovery", "surgery"]
  },

  // ── LIFE SITUATIONS ───────────────────────────────────────────────────────
  {
    slug: "prayer-for-retirement",
    title: "Prayer for Retirement",
    metaDesc: "A prayer for retirement — for the new season of life, purpose in rest, financial peace, and God's direction for your next chapter after work.",
    category: "Purpose",
    keywords: ["prayer for retirement", "prayer for someone retiring", "retirement prayer", "prayer for new season after work", "prayer for retired person"],
    samplePrayer: "Lord, I have worked for many years, and now I stand at the threshold of retirement. I am grateful for every season of labor that brought me here. As I step into this new chapter, I ask for Your guidance and purpose. This is not the end — it is a beginning. Show me how to invest this time, this wisdom, and this energy in ways that still honor You. Protect my financial security. Bless my health so I can enjoy what I have worked for. Fill my days with meaning, joy, and connection. Let this be the most fruitful season of my life. In Jesus' name, Amen.",
    scripture: [
      { ref: "Ecclesiastes 3:1", text: "There is a time for everything, and a season for every activity under the heavens." },
      { ref: "Psalm 92:14", text: "They will still bear fruit in old age, they will stay fresh and green." }
    ],
    related: ["purpose", "calling", "thanksgiving", "elderly", "prayer-for-grandparents"]
  },
  {
    slug: "prayer-for-empty-nest",
    title: "Prayer for Empty Nest",
    metaDesc: "A prayer for the empty nest season — for parents adjusting after children leave home. Ask God to bring purpose, peace, and joy to this new chapter.",
    category: "Family",
    keywords: ["prayer for empty nest", "empty nest prayer", "prayer for parents when kids leave", "prayer for empty nester", "prayer for loneliness after kids leave"],
    samplePrayer: "Father, the house feels quiet in a way I was not fully prepared for. My children have grown and gone, and while I am proud of the adults they are becoming, I grieve the season that has passed. I bring this ache to You. Help me to embrace this new chapter rather than mourn the last one. Reveal the purpose and freedom You have placed in this season. Renew my marriage and my friendships. Show me how to invest in others the way I invested in my children. Fill the silence with Your peace, not loneliness. Thank You for the beautiful season behind me and the one ahead. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 71:18", text: "Even when I am old and gray, do not forsake me, my God, till I declare your power to the next generation." },
      { ref: "Isaiah 43:19", text: "See, I am doing a new thing! Now it springs up; do you not perceive it?" }
    ],
    related: ["parents", "family", "purpose", "prayer-for-retirement", "loneliness"]
  },
  {
    slug: "prayer-for-long-distance-relationship",
    title: "Prayer for a Long Distance Relationship",
    metaDesc: "A prayer for couples or families in a long distance relationship. Ask God to sustain love, build trust, ease loneliness, and guide the path forward.",
    category: "Relationships",
    keywords: ["prayer for long distance relationship", "long distance relationship prayer", "prayer for long distance couple", "prayer for missing someone far away", "prayer for long distance love"],
    samplePrayer: "Lord, the miles between us are real, and some days they feel heavier than others. I come before You asking You to bridge the distance in ways that technology cannot. Keep our hearts knit together in love and trust. Protect us both from loneliness, temptation, and the doubts that distance can sow. Speak encouragement to our hearts in the quiet moments. Give us wisdom about the future and clear guidance about when and how we will close this gap. Let this season of distance strengthen what we have, not erode it. I trust You with this relationship. In Jesus' name, Amen.",
    scripture: [
      { ref: "Song of Solomon 8:7", text: "Many waters cannot quench love; rivers cannot sweep it away." },
      { ref: "Philippians 1:3", text: "I thank my God every time I remember you." }
    ],
    related: ["marriage", "friendship", "prayer-for-ex", "loneliness", "relationships"]
  },
  {
    slug: "prayer-for-ex",
    title: "Prayer for an Ex",
    metaDesc: "A prayer for an ex-partner — whether for healing, forgiveness, or their wellbeing. Release the past to God and trust Him with your heart and theirs.",
    category: "Relationships",
    keywords: ["prayer for ex", "prayer for ex boyfriend", "prayer for ex girlfriend", "prayer for ex husband", "prayer for my ex"],
    samplePrayer: "Lord, I bring my ex before You. This is not easy, because there is still pain connected to that name, that memory. But I choose today to release them into Your hands rather than keep carrying what I cannot fix. I pray for their wellbeing — that they would find healing, purpose, and peace. I pray that bitterness would not take root in my heart. Help me to forgive completely, not because they deserve it, but because I need to be free. And Lord, if there is any restoration in Your plan, I trust You to make it clear. If not, heal my heart for the next chapter. In Jesus' name, Amen.",
    scripture: [
      { ref: "Matthew 6:14", text: "For if you forgive other people when they sin against you, your heavenly Father will also forgive you." },
      { ref: "Psalm 147:3", text: "He heals the brokenhearted and binds up their wounds." }
    ],
    related: ["prayer-for-broken-heart", "forgiveness", "letting-go", "divorce", "reconciliation"]
  },
  {
    slug: "prayer-for-broken-heart",
    title: "Prayer for a Broken Heart",
    metaDesc: "A prayer for a broken heart — for the pain of loss, rejection, or a relationship ending. Let God heal what is shattered and restore your hope and joy.",
    category: "Grief",
    keywords: ["prayer for broken heart", "prayer for heartbreak", "prayer for healing a broken heart", "prayer after breakup", "prayer for heartache"],
    samplePrayer: "Father, my heart is broken. The pain I feel right now is real and it is deep, and I do not want to pretend it is not. I come to You because You are the mender of broken things. Heal what has been shattered inside me. Take the sharp pieces of grief and grief-given anger, and gently put them back together in a new way. Do not let bitterness take root where love once grew. I choose to grieve honestly before You, and I trust that weeping endures for a night but joy will come again. Restore my hope, Lord. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 34:18", text: "The Lord is close to the brokenhearted and saves those who are crushed in spirit." },
      { ref: "Psalm 30:5", text: "Weeping may stay for the night, but rejoicing comes in the morning." }
    ],
    related: ["prayer-for-ex", "grief", "healing", "hope", "prayer-for-rejection"]
  },
  {
    slug: "prayer-for-rejection",
    title: "Prayer for Rejection",
    metaDesc: "A prayer for dealing with rejection — from a job, person, or opportunity. Ask God to heal the wound of rejection and reveal His greater purpose for you.",
    category: "Mental Health",
    keywords: ["prayer for rejection", "prayer after being rejected", "prayer for rejection pain", "prayer for feeling rejected", "prayer for job rejection"],
    samplePrayer: "Lord, rejection stings. Whether it came from a person, a job, a dream, or a community — the message of 'you are not enough' has cut deep. I come to You with that wound. Remind me of who You say I am. You chose me before the foundation of the world — that cannot be undone by any human no. Heal the shame that rejection has left behind. Help me to see this closed door not as a verdict on my worth but as redirection toward Your better plan. Rebuild my confidence in You, not in others' approval. I am Yours. In Jesus' name, Amen.",
    scripture: [
      { ref: "1 Peter 2:4", text: "As you come to him, the living Stone — rejected by humans but chosen by God and precious to him." },
      { ref: "Psalm 27:10", text: "Though my father and mother forsake me, the Lord will receive me." }
    ],
    related: ["identity", "prayer-for-broken-heart", "hope", "prayer-for-feeling-lost", "courage"]
  },
  {
    slug: "prayer-for-feeling-lost",
    title: "Prayer for Feeling Lost",
    metaDesc: "A prayer for when you feel lost in life — without direction, purpose, or a sense of where to go next. Ask God to lead you and light your path forward.",
    category: "Purpose",
    keywords: ["prayer for feeling lost", "prayer when you feel lost in life", "prayer for losing your way", "prayer for no direction", "prayer when confused about life"],
    samplePrayer: "Lord, I feel lost. I do not know which direction to go, and the uncertainty is heavy. Every path looks unclear, and every option seems uncertain. I need You to be my compass right now. I do not ask You to show me the whole map — just the next step. Quiet the noise of too many opinions and too many fears long enough for me to hear Your voice. Help me to trust that You know where I am even when I do not. Lead me by the hand, step by step, out of this fog. I am listening, Lord. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 32:8", text: "I will instruct you and teach you in the way you should go; I will counsel you with my loving eye on you." },
      { ref: "Proverbs 3:5-6", text: "Trust in the Lord with all your heart and lean not on your own understanding; in all your ways submit to him, and he will make your paths straight." }
    ],
    related: ["direction", "guidance", "purpose", "hope", "prayer-for-hopelessness"]
  },
  {
    slug: "prayer-for-hopelessness",
    title: "Prayer for Hopelessness",
    metaDesc: "A prayer when you feel hopeless — when you cannot see a way forward. Ask God to restore hope, renew your vision, and remind you that He is still working.",
    category: "Mental Health",
    keywords: ["prayer for hopelessness", "prayer when feeling hopeless", "prayer for loss of hope", "prayer for despair", "prayer when you have no hope"],
    samplePrayer: "God, I am struggling to feel hope right now. The future looks dark, and I am tired of trying to stay positive when everything around me says nothing will get better. But I come to You anyway, because You are the God of hope. Would You breathe hope back into me? Not a shallow optimism, but a deep, rooted confidence that You are still working even when I cannot see it. Remind me of the times You came through. Remind me of Your promises. Help me to hold on for one more day. You are the anchor of my soul. In Jesus' name, Amen.",
    scripture: [
      { ref: "Romans 15:13", text: "May the God of hope fill you with all joy and peace as you trust in him, so that you may overflow with hope by the power of the Holy Spirit." },
      { ref: "Lamentations 3:21-23", text: "Yet this I call to mind and therefore I have hope: Because of the Lord's great love we are not consumed, for his compassions never fail." }
    ],
    related: ["hope", "depression", "prayer-for-feeling-lost", "prayer-for-rejection", "suicidal-thoughts"]
  },

  // ── SPIRITUAL ─────────────────────────────────────────────────────────────
  {
    slug: "prayer-for-speaking-in-tongues",
    title: "Prayer for Speaking in Tongues",
    metaDesc: "A prayer to receive the gift of speaking in tongues. Seek the Holy Spirit's fullness and ask God to release this powerful gift of the Spirit in your life.",
    category: "Spiritual Life",
    keywords: ["prayer for speaking in tongues", "prayer to speak in tongues", "prayer for gift of tongues", "prayer for tongues", "prayer for Holy Spirit baptism and tongues"],
    samplePrayer: "Holy Spirit, I come before You with an open and hungry heart. Your Word tells me that the gift of tongues is available, and I desire all that You have for me. I surrender my tongue, my mind, and my fear of what I do not fully understand. Fill me to overflowing. I open my mouth in faith, believing that You will give me utterance that goes beyond my natural language. Let this gift build up my spirit and deepen my communion with You. I receive this gift by faith, not by feeling. Come, Holy Spirit — have all of me. In Jesus' name, Amen.",
    scripture: [
      { ref: "Acts 2:4", text: "All of them were filled with the Holy Spirit and began to speak in other tongues as the Spirit enabled them." },
      { ref: "1 Corinthians 14:2", text: "For anyone who speaks in a tongue does not speak to people but to God. Indeed, no one understands them; they utter mysteries by the Spirit." }
    ],
    related: ["prayer-for-holy-spirit", "prayer-for-anointing", "spiritual-growth", "baptism", "faith"]
  },
  {
    slug: "prayer-for-holy-spirit",
    title: "Prayer for the Holy Spirit",
    metaDesc: "A prayer to be filled with the Holy Spirit. Invite God's Spirit to fill, lead, and empower you for life, ministry, and every challenge you face.",
    category: "Spiritual Life",
    keywords: ["prayer for the Holy Spirit", "prayer to be filled with the Holy Spirit", "prayer for Holy Spirit's presence", "prayer for Holy Spirit baptism", "prayer for more of the Holy Spirit"],
    samplePrayer: "Father, I come before You hungry for more of Your Holy Spirit. I do not want to live on yesterday's experience — I need a fresh filling today. Fill every room of my heart with Your Spirit. Let Him lead me in every decision, restrain me from every wrong path, and empower me to do what I could never do in my own strength. I yield my will, my tongue, my time, and my plans to His leading. Holy Spirit, You are welcome here. Come in fullness, come in power, come in peace. I receive You now. In Jesus' name, Amen.",
    scripture: [
      { ref: "Luke 11:13", text: "If you then, though you are evil, know how to give good gifts to your children, how much more will your Father in heaven give the Holy Spirit to those who ask him!" },
      { ref: "Ephesians 5:18", text: "Do not get drunk on wine, which leads to debauchery. Instead, be filled with the Spirit." }
    ],
    related: ["prayer-for-speaking-in-tongues", "prayer-for-anointing", "spiritual-growth", "revival", "faith"]
  },
  {
    slug: "prayer-for-anointing",
    title: "Prayer for Anointing",
    metaDesc: "A prayer for God's anointing — for ministers, leaders, and all believers. Ask God for His special empowerment to fulfill your calling with His power.",
    category: "Spiritual Life",
    keywords: ["prayer for anointing", "prayer for God's anointing", "prayer for anointing oil", "prayer for fresh anointing", "prayer for God's power and anointing"],
    samplePrayer: "Lord, I desire Your anointing more than any gift, position, or ability. Without the anointing of Your Spirit, I am just performing — but with it, I can make a difference that lasts for eternity. I ask for a fresh anointing right now. Break the yoke of powerlessness in my ministry and my daily life. Let Your Spirit rest upon me like the oil ran down Aaron's beard. Give me words when I stand before people. Give me clarity when I pray. Let those I serve feel Your touch through mine. I consecrate myself to You afresh. In Jesus' name, Amen.",
    scripture: [
      { ref: "Isaiah 61:1", text: "The Spirit of the Sovereign Lord is on me, because the Lord has anointed me to proclaim good news to the poor." },
      { ref: "1 John 2:27", text: "As for you, the anointing you received from him remains in you." }
    ],
    related: ["prayer-for-holy-spirit", "ministry", "pastor", "before-preaching", "spiritual-growth"]
  },
  {
    slug: "prayer-for-sign-from-god",
    title: "Prayer for a Sign from God",
    metaDesc: "A sincere prayer asking God for a sign or confirmation. When you are at a crossroads, seek His clear direction and trust Him to guide your decision.",
    category: "Faith",
    keywords: ["prayer for a sign from God", "prayer for God's sign", "prayer for confirmation from God", "prayer for God's direction", "prayer asking God for a sign"],
    samplePrayer: "Lord, I am standing at a crossroads and I genuinely do not know which way to go. I do not want to move in presumption or fear. I ask humbly for a clear sign or confirmation from You. You guided Gideon with a fleece, You spoke to Abraham, You directed Joseph through dreams — and I believe You are the same God today. Speak clearly to me through Your Word, through circumstances, through peace, through wise counsel, or however You choose. I commit to obey what You reveal. I am listening and waiting. In Jesus' name, Amen.",
    scripture: [
      { ref: "Judges 6:37", text: "Look, I will place a wool fleece on the threshing floor. If there is dew only on the fleece and all the ground is dry, then I will know that you will save Israel by my hand." },
      { ref: "James 1:5", text: "If any of you lacks wisdom, you should ask God, who gives generously to all without finding fault, and it will be given to you." }
    ],
    related: ["guidance", "direction", "hearing-god", "faith", "prayer-for-divine-direction"]
  },
  {
    slug: "prayer-for-divine-direction",
    title: "Prayer for Divine Direction",
    metaDesc: "A powerful prayer for divine direction from God. When the path is unclear, trust Him to order your steps and guide you into His perfect will.",
    category: "Purpose",
    keywords: ["prayer for divine direction", "prayer for God's direction", "prayer for direction in life", "prayer for divine guidance", "prayer for clarity and direction"],
    samplePrayer: "Sovereign Lord, I acknowledge today that my life is not my own and my wisdom alone is not enough. I need Your divine direction for this season. Order my steps. Close doors that are not from You, no matter how attractive they appear. Open doors that are, even when they seem unlikely. Give me ears to hear Your voice above every other competing voice. Let Your Word be a lamp to my feet — not a floodlight showing the whole journey, but enough light for the next right step. I submit my plans to Your purposes. Lead me, Lord. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 37:23", text: "The Lord makes firm the steps of the one who delights in him." },
      { ref: "Isaiah 30:21", text: "Whether you turn to the right or to the left, your ears will hear a voice behind you, saying, 'This is the way; walk in it.'" }
    ],
    related: ["guidance", "direction", "purpose", "calling", "prayer-for-sign-from-god"]
  }
];
