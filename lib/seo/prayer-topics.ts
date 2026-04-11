export interface PrayerTopic {
  slug:        string;
  title:       string;       // page H1 + OG title
  metaDesc:    string;       // meta description (150 chars)
  category:    string;
  keywords:    string[];     // long-tail variants
  samplePrayer:string;       // static prayer (indexed by Google)
  scripture:   { ref: string; text: string }[];
  related:     string[];     // slugs of related topics
}

export const PRAYER_TOPICS: PrayerTopic[] = [
  // ── HEALING ──────────────────────────────────────────────────────
  {
    slug: "healing",
    title: "Prayer for Healing",
    metaDesc: "Powerful prayer for healing — physical, emotional, and spiritual. Find comfort and restoration through scripture-based prayer.",
    category: "Health",
    keywords: ["prayer for healing","healing prayer","prayer for the sick","prayer for recovery","healing scriptures"],
    samplePrayer: "Lord Jesus, You are the Great Physician, and I come before You today in need of Your healing touch. You bore my sicknesses and carried my diseases, and by Your stripes I am healed. I stand on Your Word and declare that healing flows through my body right now. Restore every cell, every tissue, every organ to perfect health. Where there is pain, let Your peace reign. Where there is weakness, let Your strength arise. I trust You, Lord, with my body, my mind, and my spirit. May this healing bring glory to Your name. In Jesus' name, Amen.",
    scripture: [
      { ref: "Jeremiah 17:14", text: "Heal me, Lord, and I will be healed; save me and I will be saved, for you are the one I praise." },
      { ref: "Isaiah 53:5", text: "But he was pierced for our transgressions, he was crushed for our iniquities; the punishment that brought us peace was on him, and by his wounds we are healed." },
    ],
    related: ["sick-loved-one","cancer","anxiety","depression","recovery"],
  },
  {
    slug: "healing-for-cancer",
    title: "Prayer for Healing from Cancer",
    metaDesc: "A heartfelt prayer for healing from cancer. Bring hope and faith to those battling cancer with this powerful scripture-based prayer.",
    category: "Health",
    keywords: ["prayer for cancer","cancer healing prayer","prayer for someone with cancer","prayer for cancer patient"],
    samplePrayer: "Heavenly Father, I come before You with a heart full of faith, trusting in Your power to heal. Cancer is no match for Your might. You spoke the world into existence and You can speak wholeness into this body. I bind every cancerous cell and command it to wither in the name of Jesus. Strengthen the treatments, guide the doctors' hands, and let Your supernatural healing flow. Give courage and peace that surpasses all understanding. You are Jehovah Rapha — the Lord who heals. I believe. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 103:2-3", text: "Praise the Lord, my soul, and forget not all his benefits — who forgives all your sins and heals all your diseases." },
      { ref: "Matthew 8:17", text: "He took up our infirmities and bore our diseases." },
    ],
    related: ["healing","sick-loved-one","recovery","strength","hope"],
  },
  {
    slug: "sick-loved-one",
    title: "Prayer for a Sick Loved One",
    metaDesc: "Pray for a sick friend or family member. This powerful prayer brings God's healing and comfort to those you love who are suffering.",
    category: "Health",
    keywords: ["prayer for sick loved one","prayer for sick friend","prayer for sick family member","prayer for someone who is ill"],
    samplePrayer: "Lord, I lift up my beloved to You right now. You see their pain, You know their struggle, and You love them even more than I do. I ask for Your healing hand to rest upon them. Surround them with Your peace, drive away fear, and fill their room with Your presence. Let the doctors and nurses be instruments of Your grace. Restore their health, renew their strength, and let this season of sickness give way to a testimony of Your faithfulness. In Jesus' mighty name, Amen.",
    scripture: [
      { ref: "James 5:15", text: "And the prayer offered in faith will make the sick person well; the Lord will raise them up." },
      { ref: "Psalm 41:3", text: "The Lord sustains them on their sickbed and restores them from their bed of illness." },
    ],
    related: ["healing","cancer","recovery","comfort","hospital"],
  },
  {
    slug: "recovery",
    title: "Prayer for Recovery",
    metaDesc: "A prayer for full recovery after surgery, illness or injury. Trust God to restore your strength and health completely.",
    category: "Health",
    keywords: ["prayer for recovery","prayer after surgery","prayer for strength during illness","recovery prayer"],
    samplePrayer: "Father God, I thank You that You are the source of all restoration. As my body recovers, I invite Your healing power to work in every part of me. Strengthen what is weak, mend what is broken, and speed this recovery beyond what doctors expect. Let every setback be a setup for a greater testimony. I choose faith over fear, and trust over worry. You hold my days in Your hands, and I rest in that truth. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 30:2", text: "Lord my God, I called to you for help, and you healed me." },
      { ref: "Isaiah 40:31", text: "But those who hope in the Lord will renew their strength." },
    ],
    related: ["healing","surgery","strength","hospital","anxiety"],
  },
  {
    slug: "hospital",
    title: "Prayer for Someone in the Hospital",
    metaDesc: "A comforting prayer for someone in the hospital. Ask God to bring healing, peace, and His presence to hospital rooms.",
    category: "Health",
    keywords: ["prayer for hospital patient","hospital prayer","prayer for someone in icu","prayer for surgery"],
    samplePrayer: "Lord Jesus, right now I pray for healing and peace over every hospital room where Your children lie. Be the Comforter, be the Healer. Guide the hands of every doctor and nurse. Let no diagnosis be final except Yours. Remove fear and replace it with faith. Restore life, restore health, restore hope. Let Your presence fill that room like sunshine. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 23:4", text: "Even though I walk through the darkest valley, I will fear no evil, for you are with me." },
    ],
    related: ["healing","sick-loved-one","surgery","recovery","strength"],
  },
  // ── EMOTIONAL / MENTAL HEALTH ───────────────────────────────────
  {
    slug: "anxiety",
    title: "Prayer for Anxiety",
    metaDesc: "Find peace through prayer for anxiety. Let go of worry and fear with this calming, scripture-based prayer for anxious hearts.",
    category: "Mental Health",
    keywords: ["prayer for anxiety","prayer to calm anxiety","prayer for worry","prayer for stress","anxiety prayer"],
    samplePrayer: "Prince of Peace, the storms in my mind are loud, but Your voice is louder. I cast every anxious thought at Your feet right now. You told me not to be anxious about anything, and I choose to believe that today. Replace my fear with faith, my worry with worship, my panic with peace. Breathe Your calm into my spirit. Quiet my racing thoughts. Still my trembling heart. You are with me, and that is enough. In Jesus' name, Amen.",
    scripture: [
      { ref: "Philippians 4:6-7", text: "Do not be anxious about anything, but in every situation, by prayer and petition, present your requests to God. And the peace of God will guard your hearts." },
      { ref: "1 Peter 5:7", text: "Cast all your anxiety on him because he cares for you." },
    ],
    related: ["depression","stress","peace","fear","mental-health"],
  },
  {
    slug: "depression",
    title: "Prayer for Depression",
    metaDesc: "A powerful prayer for depression and sadness. Find hope, strength, and God's light in your darkest moments.",
    category: "Mental Health",
    keywords: ["prayer for depression","prayer when depressed","prayer for sadness","prayer for hopelessness","depression prayer"],
    samplePrayer: "Father, the darkness feels heavy today. I can't see the way through, but I know You can. You are the lifter of my head. You promised to never leave me or forsake me, and I hold onto that promise like a lifeline. Lift this heaviness off my spirit. Remind me of Your goodness. Let joy find me again, even in small ways — a sunrise, a kind word, a reason to smile. I am not alone. You are here. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 34:18", text: "The Lord is close to the brokenhearted and saves those who are crushed in spirit." },
      { ref: "Psalm 42:11", text: "Why, my soul, are you downcast? Put your hope in God, for I will yet praise him." },
    ],
    related: ["anxiety","grief","loneliness","hope","mental-health","strength"],
  },
  {
    slug: "stress",
    title: "Prayer for Stress",
    metaDesc: "Release stress through prayer. Find rest and clarity when life feels overwhelming with this powerful prayer for stress relief.",
    category: "Mental Health",
    keywords: ["prayer for stress","prayer for stress relief","prayer when overwhelmed","prayer to reduce stress"],
    samplePrayer: "Lord, I am overwhelmed. The weight of life feels heavy right now. I surrender every deadline, every pressure, every expectation to You. You promised that Your yoke is easy and Your burden is light. I trade my heavy load for Your peace. Help me to do what I can do, trust You with what I cannot, and rest in the knowledge that You are in control. In Jesus' name, Amen.",
    scripture: [
      { ref: "Matthew 11:28", text: "Come to me, all you who are weary and burdened, and I will give you rest." },
      { ref: "Psalm 55:22", text: "Cast your cares on the Lord and he will sustain you." },
    ],
    related: ["anxiety","peace","work","rest","burnout"],
  },
  {
    slug: "mental-health",
    title: "Prayer for Mental Health",
    metaDesc: "A prayer for mental health and emotional wellbeing. Find healing for your mind through God's Word and His perfect peace.",
    category: "Mental Health",
    keywords: ["prayer for mental health","prayer for sound mind","prayer for emotional healing","mental health prayer"],
    samplePrayer: "God of all comfort, I thank You that You care about my mind as much as my body. You have not given me a spirit of fear, but of power, love, and a sound mind. I receive that sound mind today. Heal every wound in my thinking. Renew my mind with Your truth. Where lies have taken root, plant Your Word. I am not defined by my struggles — I am defined by Your love. In Jesus' name, Amen.",
    scripture: [
      { ref: "2 Timothy 1:7", text: "For the Spirit God gave us does not make us timid, but gives us power, love and self-discipline." },
      { ref: "Romans 12:2", text: "Be transformed by the renewing of your mind." },
    ],
    related: ["anxiety","depression","peace","strength","hope"],
  },
  // ── MORNING / NIGHT ─────────────────────────────────────────────
  {
    slug: "morning",
    title: "Morning Prayer",
    metaDesc: "Start your day right with a powerful morning prayer. Invite God into your day with this uplifting scripture-based morning devotion.",
    category: "Daily Prayer",
    keywords: ["morning prayer","prayer to start the day","daily morning prayer","good morning prayer","morning devotion"],
    samplePrayer: "Good morning, Lord! I give You this day before it unfolds. Every hour belongs to You. Guide my steps, guard my tongue, and lead me in the paths of righteousness. Where I will face challenges today, give me wisdom. Where I encounter people, let me reflect Your love. Fill me afresh with Your Spirit and let this day count for eternity. Thank You for the gift of another day. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 5:3", text: "In the morning, Lord, you hear my voice; in the morning I lay my requests before you and wait expectantly." },
      { ref: "Lamentations 3:22-23", text: "His compassions never fail. They are new every morning; great is your faithfulness." },
    ],
    related: ["daily","gratitude","protection","work","strength"],
  },
  {
    slug: "night",
    title: "Night Prayer Before Bed",
    metaDesc: "End your day in peace with a calming night prayer before bed. Rest in God's protection and presence as you sleep.",
    category: "Daily Prayer",
    keywords: ["night prayer","bedtime prayer","prayer before sleep","evening prayer","prayer before bed"],
    samplePrayer: "Lord, as this day comes to a close, I lay it all at Your feet. Thank You for protecting me, providing for me, and walking with me. Forgive me for where I fell short today. As I sleep, watch over me and my household. Let Your angels encamp around us. Quiet my mind and grant me restful sleep, that I might wake refreshed and ready to serve You again. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 4:8", text: "In peace I will lie down and sleep, for you alone, Lord, make me dwell in safety." },
      { ref: "Psalm 121:4", text: "He who watches over Israel will neither slumber nor sleep." },
    ],
    related: ["morning","protection","peace","rest","daily"],
  },
  {
    slug: "daily",
    title: "Daily Prayer",
    metaDesc: "A powerful daily prayer to keep you connected to God throughout the day. Perfect for a consistent morning or evening devotion.",
    category: "Daily Prayer",
    keywords: ["daily prayer","everyday prayer","simple daily prayer","prayer for today","short daily prayer"],
    samplePrayer: "Father, I come to You today with a humble heart. I need You in every moment — in my conversations, in my decisions, in my rest and in my work. Fill me with Your Spirit. Let Your will be done in my life today, not mine. I trust You with everything I am and everything I face. Be glorified in my life today. In Jesus' name, Amen.",
    scripture: [
      { ref: "Matthew 6:11", text: "Give us today our daily bread." },
      { ref: "Psalm 118:24", text: "This is the day the Lord has made; let us rejoice and be glad in it." },
    ],
    related: ["morning","night","gratitude","strength","peace"],
  },
  // ── FAMILY / RELATIONSHIPS ──────────────────────────────────────
  {
    slug: "family",
    title: "Prayer for Family",
    metaDesc: "A powerful prayer for your family — for unity, protection, love, and God's blessing over every member of your household.",
    category: "Family",
    keywords: ["prayer for family","family prayer","prayer for my family","prayer for family unity","blessing prayer for family"],
    samplePrayer: "Father, I lift my family to You. You placed us together for a purpose, and I ask that Your love would be the foundation of everything we share. Where there is division, bring unity. Where there are wounds, bring healing. Protect every member of my family — physically, spiritually, and emotionally. Let our home be a place where Your presence is felt and Your love is shown. Bless us and make us a blessing. In Jesus' name, Amen.",
    scripture: [
      { ref: "Joshua 24:15", text: "As for me and my household, we will serve the Lord." },
      { ref: "Psalm 128:3", text: "Your children will be like olive shoots around your table." },
    ],
    related: ["marriage","children","parents","unity","protection"],
  },
  {
    slug: "marriage",
    title: "Prayer for Marriage",
    metaDesc: "Strengthen your marriage with prayer. A powerful prayer for love, unity, and restoration in your marriage relationship.",
    category: "Family",
    keywords: ["prayer for marriage","marriage prayer","prayer for husband and wife","prayer to save marriage","prayer for couple"],
    samplePrayer: "Lord, I thank You for the gift of marriage. What You have joined together, no force can separate. I pray for our union — that love would always be our language, patience our practice, and forgiveness our response. Where we have hurt each other, heal those wounds. Renew the spark. Remind us why we chose each other. May our marriage be a testimony of Your faithfulness and covenant love. In Jesus' name, Amen.",
    scripture: [
      { ref: "Ecclesiastes 4:12", text: "A cord of three strands is not quickly broken." },
      { ref: "1 Corinthians 13:4-5", text: "Love is patient, love is kind. It does not envy, it does not boast." },
    ],
    related: ["family","husband","wife","unity","forgiveness","love"],
  },
  {
    slug: "husband",
    title: "Prayer for My Husband",
    metaDesc: "A loving prayer for your husband — for his strength, faith, protection, and purpose. Lift your husband to God in prayer today.",
    category: "Family",
    keywords: ["prayer for husband","prayer for my husband","wife prayer for husband","prayer for husband protection","prayer for husband strength"],
    samplePrayer: "Lord, I lift my husband to You. You know him better than I do. I pray that You would protect him wherever he goes today. Fill him with wisdom to lead, strength to persevere, and faith to trust You in every season. Let him feel Your hand on his life. Be his confidence when he doubts, his peace when he's anxious, and his joy when life is hard. Bless the work of his hands. In Jesus' name, Amen.",
    scripture: [
      { ref: "Proverbs 18:10", text: "The name of the Lord is a fortified tower; the righteous run to it and are safe." },
      { ref: "Psalm 112:1-2", text: "Blessed is the man who fears the Lord. His children will be mighty in the land." },
    ],
    related: ["marriage","wife","family","protection","strength"],
  },
  {
    slug: "wife",
    title: "Prayer for My Wife",
    metaDesc: "A beautiful prayer for your wife — for her joy, health, strength, and purpose. Honor her by lifting her to God in prayer.",
    category: "Family",
    keywords: ["prayer for wife","prayer for my wife","prayer for wife protection","husband prayer for wife","prayer for wife strength"],
    samplePrayer: "Father, I thank You for my wife — she is a gift I do not deserve. I pray that You would surround her with Your love today. Give her joy that is unshakeable, strength that is supernatural, and peace that passes understanding. Let her know how valued and loved she is — by You and by me. Protect her heart, bless her work, and fulfill every dream You have planted in her. In Jesus' name, Amen.",
    scripture: [
      { ref: "Proverbs 31:25", text: "She is clothed with strength and dignity; she can laugh at the days to come." },
      { ref: "Song of Solomon 4:7", text: "You are altogether beautiful, my darling; there is no flaw in you." },
    ],
    related: ["marriage","husband","family","love","strength"],
  },
  {
    slug: "children",
    title: "Prayer for Children",
    metaDesc: "Pray protection, wisdom, and God's blessing over your children. A powerful parental prayer for your kids every day.",
    category: "Family",
    keywords: ["prayer for children","prayer for my child","prayer for kids","parents prayer for children","prayer for my son","prayer for my daughter"],
    samplePrayer: "Lord, I place my children into Your hands — the safest place they could ever be. Protect them from every harm, seen and unseen. Guard their hearts from the corruption of this world and plant Your Word deep within them. Give them wisdom beyond their years. Let them know Your love personally and walk in Your purpose all their days. Bless them and make them a blessing. In Jesus' name, Amen.",
    scripture: [
      { ref: "Proverbs 22:6", text: "Start children off on the way they should go, and even when they are old they will not turn from it." },
      { ref: "Isaiah 54:13", text: "All your children will be taught by the Lord, and great will be their peace." },
    ],
    related: ["family","parents","school","protection","wisdom"],
  },
  {
    slug: "parents",
    title: "Prayer for Parents",
    metaDesc: "Honor your parents with prayer. A heartfelt prayer for the health, strength, and blessing of your mother and father.",
    category: "Family",
    keywords: ["prayer for parents","prayer for mother and father","prayer for aging parents","prayer for my mom and dad"],
    samplePrayer: "Lord, I honor and thank You for my parents. They have given so much of themselves for me. I lift them to You today — bless their bodies with health, their minds with clarity, and their hearts with peace. Repay every sacrifice they made with Your abundant blessing. Where they are struggling, be their strength. Where they are lonely, be their companion. Let their latter years be their greatest years. In Jesus' name, Amen.",
    scripture: [
      { ref: "Proverbs 20:29", text: "The glory of young men is their strength, gray hair the splendor of the old." },
      { ref: "Exodus 20:12", text: "Honor your father and your mother, so that you may live long in the land." },
    ],
    related: ["family","healing","elderly","gratitude","children"],
  },
  // ── FINANCES / WORK ─────────────────────────────────────────────
  {
    slug: "financial-breakthrough",
    title: "Prayer for Financial Breakthrough",
    metaDesc: "Pray for a financial breakthrough. A powerful prayer for debt cancellation, provision, and God's supernatural abundance.",
    category: "Finance",
    keywords: ["prayer for financial breakthrough","financial breakthrough prayer","prayer for money","prayer for finances","prayer for debt","prosperity prayer"],
    samplePrayer: "Jehovah Jireh, my Provider, I come before You in faith. You own the cattle on a thousand hills and every resource belongs to You. I ask for a financial breakthrough. Open doors that no one can shut. Bring unexpected income and supernatural provision. Cancel every debt and replace it with abundance. Teach me to steward what You give me well. I trust You not just for my needs, but for overflow that I can use to bless others. In Jesus' name, Amen.",
    scripture: [
      { ref: "Philippians 4:19", text: "And my God will meet all your needs according to the riches of his glory in Christ Jesus." },
      { ref: "Malachi 3:10", text: "Test me in this, says the Lord Almighty, and see if I will not throw open the floodgates of heaven." },
    ],
    related: ["job","provision","debt","abundance","blessing"],
  },
  {
    slug: "job",
    title: "Prayer for a Job",
    metaDesc: "A faith-filled prayer for employment and a new job. Trust God to open the right door and bring your dream job to you.",
    category: "Finance",
    keywords: ["prayer for a job","prayer for employment","prayer for job interview","prayer to get hired","prayer for new job"],
    samplePrayer: "Father, You know my need for work. I ask You to open a door that is perfect for the gifts You have placed in me. Prepare me for the opportunity before it arrives. Give me favor with employers and let my interviews go beyond my own ability. I refuse to operate in fear — I walk in faith. The right job is already assigned to me; I ask You to lead me there. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 75:6-7", text: "No one from the east or the west can exalt themselves. It is God who judges: He brings one down, he exalts another." },
      { ref: "Proverbs 16:3", text: "Commit to the Lord whatever you do, and he will establish your plans." },
    ],
    related: ["financial-breakthrough","work","provision","favor","wisdom"],
  },
  {
    slug: "work",
    title: "Prayer for Work",
    metaDesc: "Invite God into your workplace with this powerful prayer for work — for favor, productivity, wisdom, and God's blessing on your career.",
    category: "Finance",
    keywords: ["prayer for work","workplace prayer","prayer for career","prayer for business","prayer before work"],
    samplePrayer: "Lord, as I go to work today I invite You into my workplace. Be the wisdom behind every decision. Give me favor with colleagues, clients, and leadership. Let my work be excellent — a reflection of the God I serve. Guard my integrity, protect me from office politics, and let Your purpose be served through my career. May everything I put my hand to prosper. In Jesus' name, Amen.",
    scripture: [
      { ref: "Colossians 3:23", text: "Whatever you do, work at it with all your heart, as working for the Lord, not for human masters." },
      { ref: "Proverbs 16:3", text: "Commit to the Lord whatever you do, and he will establish your plans." },
    ],
    related: ["job","financial-breakthrough","wisdom","favor","morning"],
  },
  // ── PROTECTION ──────────────────────────────────────────────────
  {
    slug: "protection",
    title: "Prayer for Protection",
    metaDesc: "A powerful prayer for God's protection over you and your family. Declare Psalm 91 and claim divine safety today.",
    category: "Protection",
    keywords: ["prayer for protection","protection prayer","prayer for safety","prayer for God's protection","Psalm 91 prayer","prayer from danger"],
    samplePrayer: "Lord, I declare Psalm 91 over my life today. You are my refuge and my fortress, my God in whom I trust. I ask that Your angels be stationed around me and my household. No weapon formed against me shall prosper. No accident, no attack, no evil shall come near my dwelling. I walk in divine protection because I live under the shadow of the Almighty. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 91:11", text: "For he will command his angels concerning you to guard you in all your ways." },
      { ref: "Isaiah 54:17", text: "No weapon forged against you will prevail." },
    ],
    related: ["family","morning","travel","danger","children"],
  },
  {
    slug: "travel",
    title: "Prayer for Safe Travel",
    metaDesc: "A prayer for safe travel by car, plane, or any journey. Cover your trip with God's protection and arrive safely at your destination.",
    category: "Protection",
    keywords: ["prayer for safe travel","travel prayer","prayer before road trip","prayer for flight","prayer for journey"],
    samplePrayer: "Father, as I travel today, I commit this journey into Your hands. Be the Navigator who guides every turn. Protect me on the roads, in the air, and on the water. Let Your angels go before me and behind me. Prevent accidents and keep every vehicle around me under Your watch. I arrive safely, in Jesus' name. Thank You for journeying with me. Amen.",
    scripture: [
      { ref: "Psalm 121:8", text: "The Lord will watch over your coming and going both now and forevermore." },
      { ref: "Proverbs 3:23", text: "Then you will go on your way in safety, and your foot will not stumble." },
    ],
    related: ["protection","morning","family","danger"],
  },
  // ── FAITH / SPIRITUAL GROWTH ────────────────────────────────────
  {
    slug: "strength",
    title: "Prayer for Strength",
    metaDesc: "When you feel weak, pray for God's strength. A powerful prayer for inner strength, endurance, and perseverance through hard times.",
    category: "Faith",
    keywords: ["prayer for strength","strength prayer","prayer for inner strength","prayer when feeling weak","prayer for courage"],
    samplePrayer: "Lord God, I am weak but You are strong. Right now I need Your strength, not my own. I can do all things through Christ who strengthens me — and I declare that today. Infuse me with supernatural endurance. When I want to quit, remind me why I started. When my legs give out, carry me. Let Your power be made perfect in my weakness. I will not fall — for You uphold me with Your righteous right hand. In Jesus' name, Amen.",
    scripture: [
      { ref: "Philippians 4:13", text: "I can do all this through him who gives me strength." },
      { ref: "Isaiah 41:10", text: "Do not fear, for I am with you; do not be dismayed, for I am your God. I will strengthen you and help you." },
    ],
    related: ["courage","hope","healing","depression","perseverance"],
  },
  {
    slug: "hope",
    title: "Prayer for Hope",
    metaDesc: "A prayer for hope when you feel hopeless. Let God restore your hope and remind you of His faithfulness and promises.",
    category: "Faith",
    keywords: ["prayer for hope","hope prayer","prayer when hopeless","prayer for faith","prayer for encouragement"],
    samplePrayer: "God of hope, I come to You at the end of my own strength. Everything in me wants to give up, but I know You are not done. You make all things new. You bring life out of death. I ask You to reignite the hope inside me. Remind me of Your promises. Show me a glimpse of the future You have planned. Let hope rise like the morning sun — certain and unstoppable. In Jesus' name, Amen.",
    scripture: [
      { ref: "Romans 15:13", text: "May the God of hope fill you with all joy and peace as you trust in him, so that you may overflow with hope." },
      { ref: "Jeremiah 29:11", text: "For I know the plans I have for you, declares the Lord, plans to prosper you and not to harm you, plans to give you hope and a future." },
    ],
    related: ["strength","depression","faith","encouragement","peace"],
  },
  {
    slug: "faith",
    title: "Prayer for Faith",
    metaDesc: "Grow in faith through prayer. Ask God to increase your faith, remove doubt, and help you trust Him completely.",
    category: "Faith",
    keywords: ["prayer for faith","faith prayer","prayer to increase faith","prayer for belief","prayer for trust in God"],
    samplePrayer: "Lord, I believe — help my unbelief. Where doubt has crept in, shine the light of Your truth. Where my faith has grown cold, ignite it again. I want to trust You with the audacity of Abraham, the boldness of David, and the surrender of Mary. Stretch my faith beyond what is comfortable into what is miraculous. I choose to believe Your Word over my circumstances. In Jesus' name, Amen.",
    scripture: [
      { ref: "Hebrews 11:1", text: "Now faith is confidence in what we hope for and assurance about what we do not see." },
      { ref: "Mark 9:24", text: "I do believe; help me overcome my unbelief!" },
    ],
    related: ["hope","strength","prayer","trust","spiritual-growth"],
  },
  {
    slug: "forgiveness",
    title: "Prayer for Forgiveness",
    metaDesc: "A prayer for God's forgiveness and to forgive others. Release guilt, shame, and bitterness through this powerful prayer.",
    category: "Faith",
    keywords: ["prayer for forgiveness","forgiveness prayer","prayer to forgive someone","prayer for repentance","prayer for guilt"],
    samplePrayer: "Father, I come before You with a heart that needs Your mercy. I have fallen short, and I am sorry. Thank You for the blood of Jesus that cleanses every sin and removes every stain. I receive Your forgiveness completely — I will not carry this guilt any longer. And Lord, help me to forgive others as You have forgiven me. Release me from bitterness and fill that space with Your love. In Jesus' name, Amen.",
    scripture: [
      { ref: "1 John 1:9", text: "If we confess our sins, he is faithful and just and will forgive us our sins and purify us from all unrighteousness." },
      { ref: "Ephesians 4:32", text: "Be kind and compassionate to one another, forgiving each other, just as in Christ God forgave you." },
    ],
    related: ["healing","peace","reconciliation","repentance","love"],
  },
  {
    slug: "peace",
    title: "Prayer for Peace",
    metaDesc: "Find God's peace that passes understanding. A calming prayer for peace of mind, peace in relationships, and peace in the world.",
    category: "Faith",
    keywords: ["prayer for peace","peace prayer","prayer for peace of mind","prayer for inner peace","calming prayer"],
    samplePrayer: "Lord Jesus, You spoke peace to a raging storm and it obeyed. I ask You to speak that same peace over my heart today. Calm the storms inside me — the racing thoughts, the unresolved conflicts, the uncertain future. Fill me with the peace that passes all understanding. Guard my heart and mind in Christ Jesus. Let me be a carrier of Your peace everywhere I go. In Jesus' name, Amen.",
    scripture: [
      { ref: "John 14:27", text: "Peace I leave with you; my peace I give you. I do not give to you as the world gives. Do not let your hearts be troubled." },
      { ref: "Isaiah 26:3", text: "You will keep in perfect peace those whose minds are steadfast, because they trust in you." },
    ],
    related: ["anxiety","stress","hope","healing","rest"],
  },
  // ── GRIEF / LOSS ─────────────────────────────────────────────────
  {
    slug: "grief",
    title: "Prayer for Grief",
    metaDesc: "A prayer for those grieving a loss. Find comfort, healing, and God's presence in the painful journey of grief.",
    category: "Grief",
    keywords: ["prayer for grief","grief prayer","prayer for loss","prayer when grieving","prayer for bereavement"],
    samplePrayer: "God of all comfort, the pain of this loss is immense. Words fall short. But I know that You are close to the brokenhearted. Hold me in this grief. Let me cry without shame and mourn without guilt. Remind me that death does not have the final word — You do. Carry me through this valley. And in Your time, restore my joy. Until then, just be near. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 34:18", text: "The Lord is close to the brokenhearted and saves those who are crushed in spirit." },
      { ref: "Revelation 21:4", text: "He will wipe every tear from their eyes. There will be no more death or mourning or crying or pain." },
    ],
    related: ["comfort","depression","hope","loss","funeral"],
  },
  {
    slug: "loss-of-loved-one",
    title: "Prayer for Loss of a Loved One",
    metaDesc: "A comforting prayer after the loss of a loved one. Find God's peace and comfort as you grieve and remember someone dear.",
    category: "Grief",
    keywords: ["prayer for loss of loved one","prayer for someone who died","prayer after death","prayer for deceased","prayer for the departed"],
    samplePrayer: "Lord, we have lost someone so precious to us. The silence they leave behind is deafening. I thank You that they are in Your presence — where there is no more pain or sorrow. Comfort those of us still here. Bind our broken hearts together. Let the legacy of their life continue to inspire us. And give us the assurance that because of You, this goodbye is not forever. In Jesus' name, Amen.",
    scripture: [
      { ref: "1 Thessalonians 4:13-14", text: "We do not want you to grieve like the rest of mankind, who have no hope. For we believe that Jesus died and rose again." },
      { ref: "John 11:25", text: "Jesus said: I am the resurrection and the life." },
    ],
    related: ["grief","comfort","funeral","peace","hope"],
  },
  // ── SPECIAL OCCASIONS ────────────────────────────────────────────
  {
    slug: "birthday",
    title: "Birthday Prayer",
    metaDesc: "A beautiful birthday prayer for yourself or a loved one. Celebrate life and ask for God's blessing on the year ahead.",
    category: "Celebration",
    keywords: ["birthday prayer","prayer for birthday","birthday blessing prayer","prayer for someone's birthday","happy birthday prayer"],
    samplePrayer: "Lord, thank You for the gift of life and another year to walk in Your purpose. On this birthday, I celebrate not just the day I was born but the reasons You created me. I ask for Your blessing on this new year of life — let it be filled with growth, joy, fruitfulness, and Your presence. May every dream You have placed inside me come alive. Thank You for sustaining me to see this day. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 90:12", text: "Teach us to number our days, that we may gain a heart of wisdom." },
      { ref: "Jeremiah 1:5", text: "Before I formed you in the womb I knew you, before you were born I set you apart." },
    ],
    related: ["gratitude","blessing","hope","new-year","purpose"],
  },
  {
    slug: "new-year",
    title: "New Year Prayer",
    metaDesc: "Start the New Year with a powerful prayer. Dedicate the coming year to God and trust Him with your hopes and dreams.",
    category: "Celebration",
    keywords: ["new year prayer","prayer for new year","new year blessing prayer","prayer to start new year","new beginnings prayer"],
    samplePrayer: "Lord, as the new year begins I surrender it entirely to You. I don't know what lies ahead, but You do. I ask that Your hand guide me through every month. Fulfill Your promises. Open the doors that are meant for me. Let this be a year of breakthroughs, healings, and divine appointments. I lay down last year's pain and pick up fresh faith. This is a new year and Your mercies are new every morning. In Jesus' name, Amen.",
    scripture: [
      { ref: "Isaiah 43:19", text: "See, I am doing a new thing! Now it springs up; do you not perceive it?" },
      { ref: "Lamentations 3:22-23", text: "His compassions never fail. They are new every morning." },
    ],
    related: ["morning","hope","blessing","purpose","faith"],
  },
  {
    slug: "graduation",
    title: "Prayer for Graduation",
    metaDesc: "A prayer for graduation — celebrate this achievement and seek God's guidance for the next season of life.",
    category: "Celebration",
    keywords: ["prayer for graduation","graduation prayer","prayer for graduating student","prayer for new graduate","commencement prayer"],
    samplePrayer: "Lord, what a milestone! I thank You for sustaining me through every exam, every late night, every moment of doubt. I could not have made it without You. Now as I step into what comes next, I ask for Your guidance. Open the right doors. Bring the right people. Give me wisdom to apply everything I've learned. This degree is Yours — may I use it for Your glory. In Jesus' name, Amen.",
    scripture: [
      { ref: "Proverbs 4:7", text: "The beginning of wisdom is this: Get wisdom, and whatever you get, get insight." },
      { ref: "Joshua 1:9", text: "Be strong and courageous. Do not be afraid; do not be discouraged, for the Lord your God will be with you wherever you go." },
    ],
    related: ["school","wisdom","new-year","purpose","job"],
  },
  // ── SCHOOL / STUDENTS ────────────────────────────────────────────
  {
    slug: "school",
    title: "Prayer for School",
    metaDesc: "A prayer for students in school — for focus, wisdom, and God's help in studies, exams, and every school challenge.",
    category: "Education",
    keywords: ["prayer for school","student prayer","prayer for students","prayer for exams","prayer for studying","school prayer"],
    samplePrayer: "Lord, I thank You for the ability to learn. As I go to school today, sharpen my mind. Help me to focus, retain what I study, and recall it clearly when I need it. Give me wisdom that goes beyond textbooks. Let me also be a positive influence on those around me. In the stress of exams and assignments, let Your peace be my anchor. In Jesus' name, Amen.",
    scripture: [
      { ref: "James 1:5", text: "If any of you lacks wisdom, you should ask God, who gives generously to all without finding fault." },
      { ref: "Proverbs 3:5-6", text: "Trust in the Lord with all your heart and lean not on your own understanding." },
    ],
    related: ["wisdom","children","exam","graduation","morning"],
  },
  {
    slug: "exam",
    title: "Prayer Before an Exam",
    metaDesc: "A prayer before an exam for clarity, recall, and calm. Trust God to help you perform your best in any test or exam.",
    category: "Education",
    keywords: ["prayer before exam","exam prayer","prayer for test","prayer to pass exam","prayer for academic success"],
    samplePrayer: "Father, this exam is before me and I choose not to fear. I have studied, I have prepared, and now I trust You with the results. Clear my mind of anxiety. Help every piece of information I studied to come back to me clearly. Give me understanding for the questions I face. And when this exam is done, may the results glorify You. I can do all things through Christ who strengthens me. In Jesus' name, Amen.",
    scripture: [
      { ref: "Isaiah 50:4", text: "The Sovereign Lord has given me a well-instructed tongue, to know the word that sustains the weary." },
      { ref: "Philippians 4:13", text: "I can do all this through him who gives me strength." },
    ],
    related: ["school","wisdom","anxiety","morning","strength"],
  },
  // ── SALVATION / REPENTANCE ───────────────────────────────────────
  {
    slug: "salvation",
    title: "Prayer for Salvation",
    metaDesc: "The sinner's prayer for salvation. Accept Jesus Christ as Lord and Savior with this simple but powerful prayer of faith.",
    category: "Salvation",
    keywords: ["prayer for salvation","sinner's prayer","prayer to accept Jesus","salvation prayer","prayer to become Christian","born again prayer"],
    samplePrayer: "Lord Jesus, I come to You just as I am. I believe You are the Son of God, that You died for my sins and rose again on the third day. I am sorry for my sins and I ask You to forgive me. I turn away from my old life and I give You my heart. Come into my life, Lord Jesus. Be my Savior and my Lord. Write my name in Your book of life. I am Yours now and forever. In Jesus' name, Amen.",
    scripture: [
      { ref: "Romans 10:9", text: "If you declare with your mouth, 'Jesus is Lord,' and believe in your heart that God raised him from the dead, you will be saved." },
      { ref: "John 3:16", text: "For God so loved the world that he gave his one and only Son, that whoever believes in him shall not perish but have eternal life." },
    ],
    related: ["faith","repentance","forgiveness","baptism","new-life"],
  },
  {
    slug: "repentance",
    title: "Prayer of Repentance",
    metaDesc: "A sincere prayer of repentance to restore your relationship with God. Come back to God with a humble and broken heart.",
    category: "Salvation",
    keywords: ["prayer of repentance","repentance prayer","prayer for forgiveness of sins","prayer to return to God","prayer for restoration"],
    samplePrayer: "Father, I have gone my own way and I know it. I have sinned against You and I am truly sorry. I don't come with excuses — I come with a broken and contrite heart, which You will not despise. Wash me clean. Restore me to right relationship with You. I want to walk in Your ways again. Thank You for not giving up on me. Your arms are always open. I run back to You now. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 51:10", text: "Create in me a pure heart, O God, and renew a steadfast spirit within me." },
      { ref: "Luke 15:20", text: "But while he was still a long way off, his father saw him and was filled with compassion for him." },
    ],
    related: ["salvation","forgiveness","faith","healing","restoration"],
  },
  // ── PURPOSE / DIRECTION ──────────────────────────────────────────
  {
    slug: "purpose",
    title: "Prayer for Purpose",
    metaDesc: "Discover your God-given purpose through prayer. Ask God to reveal your calling and lead you into a life of meaning.",
    category: "Purpose",
    keywords: ["prayer for purpose","prayer for direction","prayer for calling","prayer for life purpose","prayer to find my purpose"],
    samplePrayer: "Lord, You created me on purpose for a purpose. I refuse to live a random life when You have a specific plan for me. Reveal my calling. Illuminate the gifts inside me. Align every circumstance with Your perfect will. When I drift, redirect me. When I doubt, remind me. Let me live the life You imagined for me before the world began. In Jesus' name, Amen.",
    scripture: [
      { ref: "Jeremiah 29:11", text: "For I know the plans I have for you, declares the Lord, plans to prosper you and not to harm you." },
      { ref: "Ephesians 2:10", text: "For we are God's handiwork, created in Christ Jesus to do good works, which God prepared in advance for us to do." },
    ],
    related: ["wisdom","direction","career","faith","new-year"],
  },
  {
    slug: "wisdom",
    title: "Prayer for Wisdom",
    metaDesc: "Ask God for wisdom, discernment, and sound judgment in every area of your life with this powerful prayer.",
    category: "Purpose",
    keywords: ["prayer for wisdom","wisdom prayer","prayer for discernment","prayer for guidance","prayer for good decisions"],
    samplePrayer: "Father of lights, from whom all wisdom comes — I need Your wisdom today. In every decision I face, large or small, guide me by Your Spirit. Give me discernment to tell the difference between good and best. Let Your Word be a lamp to my feet. I reject worldly wisdom and choose Yours. James said to ask and You give generously without finding fault — so I'm asking. In Jesus' name, Amen.",
    scripture: [
      { ref: "James 1:5", text: "If any of you lacks wisdom, you should ask God, who gives generously to all without finding fault, and it will be given to you." },
      { ref: "Proverbs 2:6", text: "For the Lord gives wisdom; from his mouth come knowledge and understanding." },
    ],
    related: ["purpose","faith","decision","school","guidance"],
  },
  // ── LOVE / RELATIONSHIPS ─────────────────────────────────────────
  {
    slug: "love",
    title: "Prayer for Love",
    metaDesc: "A prayer to receive and give love — romantic love, family love, and God's unconditional love. Open your heart through prayer.",
    category: "Relationships",
    keywords: ["prayer for love","love prayer","prayer to find love","prayer for a partner","prayer for relationship"],
    samplePrayer: "Lord, You are love — and everything I need to know about love, I find in You. Fill me with Your love so I can pour it out to others. If You have a partner for me, align our paths. If I am waiting, let me not grow bitter — help me to grow. Let love not be just a feeling I chase but a choice I make daily. May I love as You have loved me. In Jesus' name, Amen.",
    scripture: [
      { ref: "1 John 4:19", text: "We love because he first loved us." },
      { ref: "Romans 5:5", text: "God's love has been poured out into our hearts through the Holy Spirit." },
    ],
    related: ["marriage","loneliness","relationships","healing","hope"],
  },
  {
    slug: "loneliness",
    title: "Prayer for Loneliness",
    metaDesc: "A prayer when you feel alone and lonely. Be reminded that God is always with you and He sees your loneliness.",
    category: "Relationships",
    keywords: ["prayer for loneliness","lonely prayer","prayer when feeling alone","prayer for isolation","prayer for friendship"],
    samplePrayer: "Lord, loneliness has settled in and it's heavy. But You said You would never leave me or forsake me — I hold onto that promise. You see me. You know me. You chose me. Send community into my life. Open my heart to receive friendship and to give it. Let me know in the deepest part of me that with You, I am never truly alone. In Jesus' name, Amen.",
    scripture: [
      { ref: "Hebrews 13:5", text: "God has said, 'Never will I leave you; never will I forsake you.'" },
      { ref: "Psalm 68:6", text: "God sets the lonely in families." },
    ],
    related: ["depression","love","friendship","hope","grief"],
  },
  {
    slug: "enemies",
    title: "Prayer for Your Enemies",
    metaDesc: "A prayer for your enemies as Jesus commanded. Find peace by releasing bitterness and praying for those who hurt you.",
    category: "Relationships",
    keywords: ["prayer for enemies","prayer for those who hurt me","prayer for people who wronged me","pray for enemies","prayer for haters"],
    samplePrayer: "Lord, this is hard. But You said to pray for those who persecute me, so I obey. I pray for those who have hurt me. I release any bitterness and I bless them instead. I don't pray this in my own strength — only in Yours. Whatever they are going through that makes them treat people this way, bring them to a place of healing. And bring me to a place of peace. In Jesus' name, Amen.",
    scripture: [
      { ref: "Matthew 5:44", text: "But I tell you, love your enemies and pray for those who persecute you." },
      { ref: "Romans 12:20", text: "If your enemy is hungry, feed him; if he is thirsty, give him something to drink." },
    ],
    related: ["forgiveness","peace","reconciliation","love","healing"],
  },
  // ── GRATITUDE ───────────────────────────────────────────────────
  {
    slug: "gratitude",
    title: "Prayer of Gratitude",
    metaDesc: "A thanksgiving prayer to God for His goodness and blessings. Cultivate a heart of gratitude with this uplifting prayer.",
    category: "Thanksgiving",
    keywords: ["prayer of gratitude","thanksgiving prayer","prayer to thank God","grateful prayer","prayer of thanks"],
    samplePrayer: "Father, I pause today simply to say thank You. Not for what You will do — but for what You have already done. Thank You for life, for breath, for family, for salvation, for second chances. Thank You for the answered prayers I can see and the ones still on the way. You are so good and Your mercies never fail. My heart overflows with gratitude. In Jesus' name, Amen.",
    scripture: [
      { ref: "1 Thessalonians 5:18", text: "Give thanks in all circumstances; for this is God's will for you in Christ Jesus." },
      { ref: "Psalm 107:1", text: "Give thanks to the Lord, for he is good; his love endures forever." },
    ],
    related: ["morning","praise","blessing","joy","daily"],
  },
  {
    slug: "blessing",
    title: "Prayer for Blessings",
    metaDesc: "Ask God for His blessing on your life, home, and family. A powerful prayer to activate God's favor and abundance.",
    category: "Thanksgiving",
    keywords: ["prayer for blessings","blessing prayer","prayer for God's blessing","prayer for favor","prayer for abundance"],
    samplePrayer: "Lord, I pray the prayer of Jabez — enlarge my territory, let Your hand be with me, and keep me from evil. Bless everything I set my hand to. Let favor follow me wherever I go. Bless my home, my family, my work, and my ministry. You are a good Father who loves to give good gifts. I receive Your blessing today and I will use it to bless others. In Jesus' name, Amen.",
    scripture: [
      { ref: "Numbers 6:24-26", text: "The Lord bless you and keep you; the Lord make his face shine on you and be gracious to you." },
      { ref: "1 Chronicles 4:10", text: "Oh, that you would bless me and enlarge my territory!" },
    ],
    related: ["gratitude","favor","financial-breakthrough","family","provision"],
  },
];

// Fast lookup map
export const TOPIC_MAP = new Map<string, PrayerTopic>(
  PRAYER_TOPICS.map((t) => [t.slug, t])
);

// All slugs for generateStaticParams
export const ALL_SLUGS = PRAYER_TOPICS.map((t) => t.slug);
