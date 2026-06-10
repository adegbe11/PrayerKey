/**
 * PrayerKey client-side prayer composition engine — data.
 * No AI API, no server: keyword-scored categories stitched into a full prayer.
 * Scripture: KJV (public domain).
 */

export interface PrayerVerse {
  ref: string;
  text: string;
}

export interface PrayerCategory {
  label: string;
  keywords: string[];
  fragments: string[];
  verses: PrayerVerse[];
}

export const CATEGORIES: Record<string, PrayerCategory> = {
  healing: {
    label: "Healing",
    keywords: ["sick","sickness","ill","illness","heal","healing","health","pain","hospital","surgery","operation","disease","cancer","fever","recover","recovery","doctor","diagnosis","body","malaria","typhoid","blood pressure","diabetes"],
    fragments: [
      "You are Jehovah Rapha, the God who heals. I bring this sickness before You and ask that Your healing power flow into every affected part of the body. Touch what doctors cannot reach, restore what has been weakened, and let strength return day by day.",
      "Lord, You carried our infirmities and bore our diseases. I refuse to accept this sickness as final. Let every symptom bow to Your name, guide the hands of every doctor and nurse involved, and let complete recovery come swiftly and fully.",
      "Father, breathe Your life into this situation of ill health. Where there is pain, bring relief; where there is weakness, bring strength; where there is fear of bad news, bring a testimony of restoration.",
    ],
    verses: [
      { ref: "Jeremiah 30:17", text: "For I will restore health unto thee, and I will heal thee of thy wounds, saith the LORD." },
      { ref: "Psalm 103:2–3", text: "Bless the LORD, O my soul... who healeth all thy diseases." },
      { ref: "Isaiah 53:5", text: "...and with his stripes we are healed." },
    ],
  },
  finances: {
    label: "Provision & Finances",
    keywords: ["money","finance","financial","broke","debt","loan","rent","bills","salary","provision","provide","poverty","lack","income","fees","school fees","pay","afford"],
    fragments: [
      "You are my Provider, and You own the cattle on a thousand hills. I bring my financial situation before You. Open doors of provision that no man can shut, send help from expected and unexpected places, and let lack give way to sufficiency.",
      "Lord, You know every bill, every need, and every obligation weighing on me. Supply all my needs according to Your riches in glory. Give me wisdom to manage what You provide, and lift the burden of financial pressure from my shoulders.",
      "Father, command Your blessing on the work of my hands. Break every cycle of lack, settle every debt that towers over me, and establish me in a place of overflow — so that I may also become a blessing to others.",
    ],
    verses: [
      { ref: "Philippians 4:19", text: "But my God shall supply all your need according to his riches in glory by Christ Jesus." },
      { ref: "Psalm 34:10", text: "...they that seek the LORD shall not want any good thing." },
    ],
  },
  work: {
    label: "Work & Career",
    keywords: ["job","work","interview","career","promotion","employment","unemployed","office","boss","application","cv","resume","appointment","contract","employer","hired"],
    fragments: [
      "Lord, I commit my career into Your hands. Go before me into every interview and every room where my name is mentioned. Let me find favour with decision-makers, let my gifts make room for me, and order my steps into the position You have prepared.",
      "Father, You promote, and You open doors no man can shut. Crown my efforts with success, let my work speak excellence, and connect me with the right opportunity at the right time.",
      "I ask for divine favour in the workplace. Let me stand out for the right reasons, give me wisdom in every task and conversation, and let promotion and recognition locate me.",
    ],
    verses: [
      { ref: "Proverbs 18:16", text: "A man's gift maketh room for him, and bringeth him before great men." },
      { ref: "Psalm 75:6–7", text: "For promotion cometh neither from the east, nor from the west... God is the judge: he putteth down one, and setteth up another." },
    ],
  },
  exams: {
    label: "Exams & Studies",
    keywords: ["exam","exams","test","study","studies","school","university","grade","results","waec","jamb","neco","gcse","assignment","thesis","dissertation","course","student","admission"],
    fragments: [
      "Lord, You give wisdom generously to all who ask. I ask for a sound mind, sharp recall, and deep understanding as I prepare. In the exam hall, calm every nerve, bring back to memory everything I have studied, and let my results bring joy.",
      "Father, You gave Daniel knowledge and skill in all learning. Give me that same grace. Let understanding come easily, let my preparation be effective, and let success in these studies open doors for my future.",
    ],
    verses: [
      { ref: "James 1:5", text: "If any of you lack wisdom, let him ask of God, that giveth to all men liberally... and it shall be given him." },
      { ref: "Daniel 1:17", text: "As for these four children, God gave them knowledge and skill in all learning and wisdom." },
    ],
  },
  marriage: {
    label: "Marriage & Relationship",
    keywords: ["marriage","husband","wife","spouse","wedding","divorce","separation","relationship","partner","fiance","fiancee","engaged","restore my marriage","boyfriend","girlfriend"],
    fragments: [
      "Lord, You instituted marriage and You alone can sustain it. Pour fresh love, patience, and understanding into this relationship. Heal every wound caused by words or silence, rebuild trust where it has cracked, and let this union reflect Your covenant love.",
      "Father, where there is distance, draw hearts back together. Where there is misunderstanding, bring honest and gentle conversation. Defend this relationship against every force that seeks to divide it, and make it a source of joy again.",
    ],
    verses: [
      { ref: "Ecclesiastes 4:12", text: "...a threefold cord is not quickly broken." },
      { ref: "1 Corinthians 13:7", text: "Beareth all things, believeth all things, hopeth all things, endureth all things." },
    ],
  },
  family: {
    label: "Family",
    keywords: ["family","mother","father","mom","dad","mum","brother","sister","parents","sibling","relative","uncle","aunt","grandmother","grandfather","home"],
    fragments: [
      "Lord, I lift my family before You. Cover each one with Your protection, supply each one's needs, and let peace reign in our home. Where there is conflict, bring reconciliation; where there is distance, restore closeness.",
      "Father, You set the lonely in families. Bind us together with cords of love that cannot be broken. Watch over every member of my family, near and far, and let Your goodness follow each of us all the days of our lives.",
    ],
    verses: [
      { ref: "Joshua 24:15", text: "...but as for me and my house, we will serve the LORD." },
      { ref: "Psalm 133:1", text: "Behold, how good and how pleasant it is for brethren to dwell together in unity!" },
    ],
  },
  children: {
    label: "Children",
    keywords: ["child","children","son","daughter","kids","baby","teenager"],
    fragments: [
      "Lord, I bring my children before You. Keep them in Your ways, protect them from every harm seen and unseen, and surround them with godly influence. Let them grow in wisdom, in stature, and in favour with God and man.",
      "Father, You said our children shall be taught of the Lord and great shall be their peace. Guard their hearts, guide their choices, and fulfil every good plan You have written for their lives.",
    ],
    verses: [
      { ref: "Isaiah 54:13", text: "And all thy children shall be taught of the LORD; and great shall be the peace of thy children." },
    ],
  },
  fertility: {
    label: "Fruit of the Womb",
    keywords: ["pregnant","pregnancy","conceive","womb","fertility","barren","ivf","trying for a baby","childless","miscarriage"],
    fragments: [
      "Lord, You remembered Hannah and You remembered Sarah. Remember this home today. Let the womb that has waited carry life, let every medical report bow to Your word, and turn this season of waiting into a season of testimony.",
      "Father, You make the barren woman a joyful mother of children. We trust Your timing even as we ask boldly: open the womb, sustain conception, and bring forth a healthy child whose life will glorify You.",
    ],
    verses: [
      { ref: "Psalm 113:9", text: "He maketh the barren woman to keep house, and to be a joyful mother of children. Praise ye the LORD." },
      { ref: "Exodus 23:26", text: "There shall nothing cast their young, nor be barren, in thy land: the number of thy days I will fulfil." },
    ],
  },
  protection: {
    label: "Protection",
    keywords: ["protect","protection","safety","safe","danger","attack","enemies","enemy","evil","harm","accident","kidnap","robbery","security","cover","wicked"],
    fragments: [
      "Lord, You are my refuge and my fortress. Cover me and all that concerns me under the shadow of Your wings. Let no weapon formed against me prosper, frustrate every plan of the wicked, and surround my going out and coming in with Your angels.",
      "Father, I dwell in the secret place of the Most High. Let terror by night and arrows by day pass over me and my household. Be a wall of fire around us and keep us from every danger, seen and unseen.",
    ],
    verses: [
      { ref: "Psalm 91:11", text: "For he shall give his angels charge over thee, to keep thee in all thy ways." },
      { ref: "Isaiah 54:17", text: "No weapon that is formed against thee shall prosper..." },
    ],
  },
  travel: {
    label: "Journey Mercies",
    keywords: ["travel","journey","trip","flight","road","driving","airport","relocate","relocation","moving","abroad","japa"],
    fragments: [
      "Lord, I commit this journey into Your hands. Go before me, stand behind me, and be beside me on the way. Keep every vehicle, every road, and every flight under Your watch, and bring me to my destination safely and in peace.",
    ],
    verses: [
      { ref: "Psalm 121:8", text: "The LORD shall preserve thy going out and thy coming in from this time forth, and even for evermore." },
    ],
  },
  visa: {
    label: "Visa & Documents",
    keywords: ["visa","embassy","passport","immigration","documents","papers","green card","permit","residency","citizenship","application approved"],
    fragments: [
      "Lord, You hold the hearts of kings and officials in Your hand. I place this application before You. Give me favour with every officer who handles my file, let every document be found in order, and let the answer be a resounding yes in Your perfect timing.",
      "Father, doors that You open no man can shut. Open this door of approval, silence every voice of rejection, and let this process move faster and smoother than anyone expects.",
    ],
    verses: [
      { ref: "Proverbs 21:1", text: "The king's heart is in the hand of the LORD... he turneth it whithersoever he will." },
      { ref: "Revelation 3:8", text: "...behold, I have set before thee an open door, and no man can shut it." },
    ],
  },
  business: {
    label: "Business",
    keywords: ["business","customers","clients","sales","shop","startup","company","venture","trade","market","invest","investment","profit","entrepreneur"],
    fragments: [
      "Lord, I dedicate this business to You. Send customers and divine connections, give me ideas that set me apart, and protect the work from loss, theft, and sabotage. Let this venture grow from strength to strength and become a source of blessing to many.",
      "Father, You give power to get wealth. Anoint my hands for profitable work, give me integrity and wisdom in every decision, and let favour announce this business in places I have never been.",
    ],
    verses: [
      { ref: "Deuteronomy 8:18", text: "...it is he that giveth thee power to get wealth..." },
      { ref: "Psalm 1:3", text: "...whatsoever he doeth shall prosper." },
    ],
  },
  anxiety: {
    label: "Peace of Mind",
    keywords: ["anxious","anxiety","worry","worried","fear","afraid","scared","stress","stressed","overwhelmed","panic","peace","restless","nervous","cant sleep","can't sleep","insomnia","sleepless"],
    fragments: [
      "Lord, my mind has been heavy and my thoughts have not let me rest. I cast every anxiety on You, because You care for me. Quiet the storm inside me, replace fear with faith, and let Your peace — the kind that passes understanding — guard my heart and mind.",
      "Father, You have not given me a spirit of fear, but of power, love, and a sound mind. Take these worries I cannot carry. Tonight and every night, grant me deep, restful sleep, and let me wake with strength and a settled heart.",
    ],
    verses: [
      { ref: "Philippians 4:6–7", text: "Be careful for nothing... and the peace of God, which passeth all understanding, shall keep your hearts and minds." },
      { ref: "Psalm 4:8", text: "I will both lay me down in peace, and sleep: for thou, LORD, only makest me dwell in safety." },
    ],
  },
  grief: {
    label: "Comfort & Grief",
    keywords: ["grief","grieving","loss","lost","died","death","funeral","mourning","passed away","bereaved","late"],
    fragments: [
      "Lord, You are close to the broken-hearted. I bring my grief to You honestly — the ache, the questions, the empty space. Hold me through this valley, comfort everyone who mourns with me, and in time, gently turn this mourning into a quiet strength.",
      "Father of all comfort, wrap Your arms around this season of loss. Carry the memories tenderly, heal the heart that is torn, and remind us of the hope we have in You that death cannot take away.",
    ],
    verses: [
      { ref: "Psalm 34:18", text: "The LORD is nigh unto them that are of a broken heart; and saveth such as be of a contrite spirit." },
      { ref: "Matthew 5:4", text: "Blessed are they that mourn: for they shall be comforted." },
    ],
  },
  guidance: {
    label: "Direction & Decisions",
    keywords: ["decision","decide","direction","guidance","guide","confused","crossroads","choice","choose","path","plan","purpose","will of god","next step","clarity"],
    fragments: [
      "Lord, I stand at a crossroads and I need Your direction. I trust You with all my heart and lean not on my own understanding. Make the right path unmistakably clear, close every wrong door firmly, and give me peace as the sign of Your leading.",
      "Father, You know the end from the beginning. Order my steps in this decision. Where my own wisdom ends, let Yours begin, and let me look back on this moment and see Your hand all over it.",
    ],
    verses: [
      { ref: "Proverbs 3:5–6", text: "Trust in the LORD with all thine heart... and he shall direct thy paths." },
      { ref: "Psalm 32:8", text: "I will instruct thee and teach thee in the way which thou shalt go..." },
    ],
  },
  breakthrough: {
    label: "Breakthrough",
    keywords: ["breakthrough","stagnation","stagnant","stuck","delay","delayed","obstacle","barrier","open doors","uplift","elevation","favour","favor","blessing"],
    fragments: [
      "Lord of the breakthrough, I have waited and pushed and it feels like the door has not moved. Today I ask: break through on my behalf. Scatter every delay, remove every obstacle, and let this season of waiting end in a testimony that surprises even me.",
      "Father, You make rivers in the desert and roads in the wilderness. Do a new thing in my life. Let stagnation give way to movement, let closed doors swing open, and let Your favour go ahead of me into every room.",
    ],
    verses: [
      { ref: "Isaiah 43:19", text: "Behold, I will do a new thing... I will even make a way in the wilderness, and rivers in the desert." },
      { ref: "2 Samuel 5:20", text: "...The LORD hath broken forth upon mine enemies before me, as the breach of waters." },
    ],
  },
  forgiveness: {
    label: "Forgiveness & Repentance",
    keywords: ["forgive","forgiveness","sin","sinned","repent","repentance","guilt","guilty","ashamed","mercy","wrong","mistake"],
    fragments: [
      "Lord, I come honestly before You — I have fallen short, and I am sorry. Wash me clean by Your mercy, silence the voice of guilt and shame, and help me walk in a new direction from today. Thank You that Your mercy is new every morning.",
      "Father, You are faithful and just to forgive. I confess what weighs on my conscience and I receive Your cleansing. Restore the joy of my salvation and give me grace not to return to what You have delivered me from.",
    ],
    verses: [
      { ref: "1 John 1:9", text: "If we confess our sins, he is faithful and just to forgive us our sins, and to cleanse us from all unrighteousness." },
      { ref: "Psalm 51:10", text: "Create in me a clean heart, O God; and renew a right spirit within me." },
    ],
  },
  addiction: {
    label: "Freedom & Deliverance",
    keywords: ["addiction","addicted","habit","pornography","alcohol","drinking","drugs","smoking","gambling","betting","deliver","deliverance","bondage","free me","freedom"],
    fragments: [
      "Lord, there is a chain in my life I have not been able to break on my own. I bring it into Your light today. Whom the Son sets free is free indeed — so set me free. Break the grip of this habit, renew my mind, and put people and structures around me that keep me walking in freedom.",
      "Father, give me a hatred for what has held me and a hunger for what heals me. Strengthen me in the moment of temptation, and let every relapse pattern be broken permanently by Your power.",
    ],
    verses: [
      { ref: "John 8:36", text: "If the Son therefore shall make you free, ye shall be free indeed." },
      { ref: "1 Corinthians 10:13", text: "...God is faithful, who will not suffer you to be tempted above that ye are able..." },
    ],
  },
  loneliness: {
    label: "Companionship & Hope",
    keywords: ["lonely","loneliness","alone","isolated","no friends","single","singleness","rejected","abandoned","nobody"],
    fragments: [
      "Lord, You see the loneliness I rarely speak about. Thank You that You never leave nor forsake me. Fill the empty places with Your presence, lead me to genuine friendship and community, and in Your time, bring the right people into my life.",
      "Father, You set the solitary in families. Heal the sting of rejection I have carried, remind me of my worth in Your eyes, and open my life to relationships that are safe, honest, and life-giving.",
    ],
    verses: [
      { ref: "Psalm 68:6", text: "God setteth the solitary in families..." },
      { ref: "Deuteronomy 31:6", text: "...he will not fail thee, nor forsake thee." },
    ],
  },
  sadness: {
    label: "Joy & Strength",
    keywords: ["sad","sadness","depressed","depression","hopeless","despair","crying","tears","broken","heartbroken","tired of life","give up","discouraged","down"],
    fragments: [
      "Lord, my heart is heavy and my strength feels small. I bring You my honest tears. You are the lifter of my head — lift it now. Renew my hope, send light into this dark season, and let joy return to me, even gradually, like the morning.",
      "Father, weeping may endure for a night, but joy comes in the morning. Hold me through this night season. Surround me with people who care, give me the courage to reach out for help, and restore to me the will to keep going.",
    ],
    verses: [
      { ref: "Psalm 30:5", text: "...weeping may endure for a night, but joy cometh in the morning." },
      { ref: "Psalm 3:3", text: "But thou, O LORD, art a shield for me; my glory, and the lifter up of mine head." },
    ],
  },
  thanksgiving: {
    label: "Thanksgiving",
    keywords: ["thank","thanks","thanksgiving","grateful","gratitude","testimony","praise","celebrate","answered"],
    fragments: [
      "Lord, before I ask for anything else — thank You. Thank You for life, for mercy I did not earn, and for prayers You have already answered. Let my heart stay grateful in every season, and let this testimony encourage someone else to trust You.",
    ],
    verses: [
      { ref: "Psalm 107:1", text: "O give thanks unto the LORD, for he is good: for his mercy endureth for ever." },
    ],
  },
  legal: {
    label: "Justice & Legal Matters",
    keywords: ["court","case","lawyer","judge","legal","lawsuit","justice","police","arrested","charges","verdict"],
    fragments: [
      "Lord, You are the righteous Judge above every earthly court. I place this legal matter in Your hands. Let truth prevail, give wisdom to everyone representing me, grant me favour before the judge, and let the outcome carry Your justice and Your mercy.",
    ],
    verses: [
      { ref: "Isaiah 54:17", text: "...every tongue that shall rise against thee in judgment thou shalt condemn." },
    ],
  },
  salvation: {
    label: "Salvation of Loved Ones",
    keywords: ["salvation","saved","unbeliever","backslide","backslidden","come to christ","know god","prodigal","church again"],
    fragments: [
      "Lord, I stand in the gap for the one whose salvation I long for. Open their eyes to see You, soften every hardness in their heart, and arrange divine encounters they cannot explain away. Bring them home — and let me have the joy of seeing it.",
    ],
    verses: [
      { ref: "Acts 16:31", text: "Believe on the Lord Jesus Christ, and thou shalt be saved, and thy house." },
    ],
  },
  housing: {
    label: "Home & Accommodation",
    keywords: ["house","apartment","accommodation","landlord","eviction","mortgage","new home","own house","shelter","homeless"],
    fragments: [
      "Lord, You prepare a place for Your own. I bring my housing situation before You. Provide the right home at the right cost in the right location, give me favour with landlords and agents, and let my dwelling place be one of peace and safety.",
    ],
    verses: [
      { ref: "Isaiah 32:18", text: "And my people shall dwell in a peaceable habitation, and in sure dwellings, and in quiet resting places." },
    ],
  },
  strength: {
    label: "Strength & Faith",
    keywords: ["strength","strengthen","weak","weary","faith","believe","doubt","trust","endure","persevere","hold on"],
    fragments: [
      "Lord, my strength is running low but my eyes are on You. They that wait upon You shall renew their strength. Renew mine today. Where my faith wavers, steady it; where I am weary, carry me; and let me finish what You have given me to do.",
    ],
    verses: [
      { ref: "Isaiah 40:31", text: "But they that wait upon the LORD shall renew their strength; they shall mount up with wings as eagles..." },
    ],
  },
};

export const GENERAL: PrayerCategory = {
  label: "Your Request",
  keywords: [],
  fragments: [
    "You have heard every word I have written and the words I could not find. You understand this situation better than I understand it myself. Step into it with Your power and Your wisdom. Work where I cannot see, move what I cannot move, and let Your perfect will be done in this matter.",
    "Lord, I lay this request fully at Your feet. Nothing is too hard for You and nothing about my life escapes Your attention. Take charge of every detail, surprise me with Your goodness, and let the outcome bring me peace and bring You glory.",
  ],
  verses: [
    { ref: "Jeremiah 32:27", text: "Behold, I am the LORD, the God of all flesh: is there any thing too hard for me?" },
    { ref: "1 Peter 5:7", text: "Casting all your care upon him; for he careth for you." },
  ],
};

export const OPENINGS = [
  "Heavenly Father, I come before Your throne of grace with confidence, because You invited me to come.",
  "Lord God, thank You for another opportunity to call on Your name. You are faithful, and You hear me.",
  "Father, I quiet my heart in Your presence. Before I speak, You already know — yet You ask me to ask.",
  "Almighty God, I lift my eyes to You, the One from whom my help comes.",
  "Gracious Father, thank You that I never need an appointment to reach You. I come just as I am.",
];

export const PERSONAL = [
  "Today I bring this before You: {x}.",
  "You see what is on my heart — {x} — and You see everything around it that I did not write down.",
  "I lay this matter at Your feet: {x}. You know it fully, even better than I can express it.",
  "This is my prayer point today: {x}. I refuse to carry it alone any longer.",
];

export const TRANSITIONS = [
  "And Lord, I also pray —",
  "Beyond this, Father —",
  "I bring one more thing before You —",
  "And concerning the other matter on my heart —",
];

export const CLOSINGS = [
  "I thank You in advance, because I know You have heard me. Let everything I have asked be done according to Your perfect will, in Jesus' name.",
  "I leave these requests in Your faithful hands and I choose to walk in peace. Thank You, Father, for the answers already on the way. In Jesus' name I pray.",
  "Have Your way completely, Lord. Let my life carry the testimony of these prayers, and let Your name be glorified through it all. In Jesus' mighty name.",
  "I seal these prayers with faith and thanksgiving, trusting Your timing and Your love. In the name of Jesus Christ I pray.",
];
