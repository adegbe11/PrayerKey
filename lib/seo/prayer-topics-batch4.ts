import type { PrayerTopic } from "./prayer-topics";

export const BATCH4_PRAYER_TOPICS: PrayerTopic[] = [
  // ── HOLIDAYS & CHURCH CALENDAR ────────────────────────────────────────────
  {
    slug: "prayer-for-good-friday",
    title: "Prayer for Good Friday",
    metaDesc: "A solemn Good Friday prayer reflecting on the cross of Christ. Honor His sacrifice, receive forgiveness, and stand in awe of His love on this holy day.",
    category: "Faith",
    keywords: ["prayer for Good Friday", "Good Friday prayer", "Good Friday meditation prayer", "prayer on Good Friday", "Good Friday devotional prayer"],
    samplePrayer: "Lord Jesus, on this Good Friday I pause to stand at the foot of Your cross. The weight of what You endured for me is more than I can fully comprehend. You bore my sin, my shame, and my separation from God — and You did it willingly, out of love. I receive again the full forgiveness that Your blood purchased. I do not take it lightly. Search my heart and let this day deepen my gratitude and my devotion to You. Let me never become so familiar with the cross that I forget what it cost. Thank You, Jesus. In Jesus' name, Amen.",
    scripture: [
      { ref: "Isaiah 53:5", text: "But he was pierced for our transgressions, he was crushed for our iniquities; the punishment that brought us peace was on him, and by his wounds we are healed." },
      { ref: "John 3:16", text: "For God so loved the world that he gave his one and only Son, that whoever believes in him shall not perish but have eternal life." }
    ],
    related: ["prayer-for-easter-sunday", "salvation", "forgiveness", "repentance", "prayer-for-palm-sunday"]
  },
  {
    slug: "prayer-for-palm-sunday",
    title: "Prayer for Palm Sunday",
    metaDesc: "A Palm Sunday prayer welcoming Jesus as King. Celebrate His triumphal entry, prepare your heart for Holy Week, and declare His lordship over your life.",
    category: "Faith",
    keywords: ["prayer for Palm Sunday", "Palm Sunday prayer", "Palm Sunday devotional", "prayer on Palm Sunday", "triumphal entry prayer"],
    samplePrayer: "King Jesus, today I join the chorus of those who cry Hosanna — save now, Lord! You rode into Jerusalem as a servant king, humble and on a donkey, yet worthy of every crown. As I enter this Holy Week, I lay down my own palm branches of praise and declare You are Lord over my life. I welcome You into the city of my heart as the true King — not just a teacher, not just a miracle worker, but my Savior and Lord. Let this week deepen my understanding of Your love and Your sacrifice. Hosanna in the highest! In Jesus' name, Amen.",
    scripture: [
      { ref: "Matthew 21:9", text: "The crowds that went ahead of him and those that followed shouted, 'Hosanna to the Son of David! Blessed is he who comes in the name of the Lord!'" },
      { ref: "Zechariah 9:9", text: "Rejoice greatly, Daughter Zion! Shout, Daughter Jerusalem! See, your king comes to you, righteous and victorious, lowly and riding on a donkey." }
    ],
    related: ["prayer-for-good-friday", "prayer-for-easter-sunday", "faith", "worship", "salvation"]
  },
  {
    slug: "prayer-for-pentecost",
    title: "Prayer for Pentecost",
    metaDesc: "A Pentecost prayer asking for a fresh outpouring of the Holy Spirit. Celebrate the birth of the church and invite God's power into your life and congregation.",
    category: "Church",
    keywords: ["prayer for Pentecost", "Pentecost prayer", "prayer for Pentecost Sunday", "prayer for Holy Spirit at Pentecost", "outpouring of the Spirit prayer"],
    samplePrayer: "Lord God, on the day of Pentecost, the room where Your disciples gathered shook with Your presence and the fire of Your Spirit fell. I stand here today, praying for that same fire. Do not let Pentecost be just a historical event but a present-day reality in our lives and in our church. Pour out Your Spirit afresh. Fill us with power to be Your witnesses. Burn away every fear and compromise. Let tongues of fire rest on each of us as we hunger for more of You. Come, Holy Spirit. Do it again. In Jesus' name, Amen.",
    scripture: [
      { ref: "Acts 2:2-4", text: "Suddenly a sound like the blowing of a violent wind came from heaven and filled the whole house where they were sitting. They saw what seemed to be tongues of fire... all of them were filled with the Holy Spirit." },
      { ref: "Joel 2:28", text: "And afterward, I will pour out my Spirit on all people. Your sons and daughters will prophesy, your old men will dream dreams, your young men will see visions." }
    ],
    related: ["prayer-for-holy-spirit", "prayer-for-revival", "church", "ministry", "prayer-for-anointing"]
  },
  {
    slug: "prayer-for-advent",
    title: "Prayer for Advent",
    metaDesc: "An Advent prayer to help you wait on God with hope and expectation. Prepare your heart for Christmas by focusing on the coming of Jesus Christ.",
    category: "Faith",
    keywords: ["prayer for Advent", "Advent prayer", "Advent season prayer", "prayer during Advent", "waiting prayer for Christmas season"],
    samplePrayer: "Lord, in this Advent season I choose to slow down and wait with expectation. The world rushes toward Christmas, but I want to linger in the anticipation — to feel the profound truth that the God of the universe came in the form of a baby because He loved us that much. Fill this waiting season with hope, joy, peace, and love — the very gifts Your Son came to bring. Let me not miss the wonder of it all in the busyness of the season. Prepare my heart as a manger — ready to receive You. Come, Lord Jesus. In Jesus' name, Amen.",
    scripture: [
      { ref: "Isaiah 9:6", text: "For to us a child is born, to us a son is given, and the government will be on his shoulders. And he will be called Wonderful Counselor, Mighty God, Everlasting Father, Prince of Peace." },
      { ref: "Luke 2:11", text: "Today in the town of David a Savior has been born to you; he is the Messiah, the Lord." }
    ],
    related: ["prayer-for-christmas", "prayer-for-christmas-dinner", "faith", "worship", "hope"]
  },
  {
    slug: "prayer-for-lent",
    title: "Prayer for Lent",
    metaDesc: "A Lenten prayer for the 40-day season of fasting and reflection before Easter. Seek God in deeper surrender, sacrifice, and spiritual renewal during Lent.",
    category: "Spiritual Life",
    keywords: ["prayer for Lent", "Lenten prayer", "prayer during Lent", "prayer for Lent season", "fasting prayer for Lent"],
    samplePrayer: "Father, as I enter this Lenten season, I come with a willing heart to sacrifice, reflect, and draw near to You. Show me what I need to lay down — not just habits, but attitudes, attachments, and things that have crept to the center where only You belong. Use this forty days to make me more like Jesus. Teach me to hunger for You more than for comfort or pleasure. In every act of fasting or giving, let my heart turn toward the cross. Strip away what is unnecessary so that what remains is what is eternal. I surrender myself to You. In Jesus' name, Amen.",
    scripture: [
      { ref: "Joel 2:12", text: "'Even now,' declares the Lord, 'return to me with all your heart, with fasting and weeping and mourning.'" },
      { ref: "Matthew 4:2", text: "After fasting forty days and forty nights, he was hungry." }
    ],
    related: ["fasting-prayer", "repentance", "spiritual-growth", "prayer-for-good-friday", "prayer-for-easter-sunday"]
  },
  {
    slug: "prayer-for-mothers-day",
    title: "Prayer for Mother's Day",
    metaDesc: "A special Mother's Day prayer honoring all mothers and asking God to bless, strengthen, and pour out His love on every mother this Mother's Day.",
    category: "Family",
    keywords: ["prayer for Mother's Day", "Mother's Day prayer", "prayer for mothers on Mother's Day", "prayer for my mom on Mother's Day", "church prayer for Mother's Day"],
    samplePrayer: "Heavenly Father, today we celebrate the mothers You have placed in our lives — and what a gift they are. We thank You for every mother who has prayed through the night, dried tears, spoken life, and sacrificed without counting the cost. Bless them richly today. For mothers who are weary, refresh them. For mothers who are grieving, comfort them. For mothers who feel unseen and underappreciated, let them feel the full weight of how precious they are to You. And for those longing to become mothers — we trust You with that ache too. In Jesus' name, Amen.",
    scripture: [
      { ref: "Proverbs 31:28", text: "Her children arise and call her blessed; her husband also, and he praises her." },
      { ref: "Isaiah 66:13", text: "As a mother comforts her child, so will I comfort you; and you will be comforted over Jerusalem." }
    ],
    related: ["mothers", "prayer-for-mom", "parents", "family", "birthday-prayer-for-sister"]
  },
  {
    slug: "prayer-for-fathers-day",
    title: "Prayer for Father's Day",
    metaDesc: "A Father's Day prayer celebrating fathers and asking God to strengthen, guide, and bless every dad. Pray for the fathers in your life on this special day.",
    category: "Family",
    keywords: ["prayer for Father's Day", "Father's Day prayer", "prayer for fathers on Father's Day", "church prayer for Father's Day", "prayer for dads"],
    samplePrayer: "Lord God, we thank You for fathers — for the men who have shown up, who have tried, who have provided, protected, and prayed over their families. On this Father's Day, we honor them and lift them before You. Give every father the wisdom of Solomon and the tenderness of David. Where they have failed, give them grace and a chance to heal. Where they have been faithful, reward their perseverance. Raise up a generation of godly fathers who reflect Your fatherheart to their children. And for those without fathers here on earth — You are the perfect Father to them all. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 103:13", text: "As a father has compassion on his children, so the Lord has compassion on those who fear him." },
      { ref: "Ephesians 6:4", text: "Fathers, do not exasperate your children; instead, bring them up in the training and instruction of the Lord." }
    ],
    related: ["fathers", "prayer-for-dad", "parents", "family", "birthday-prayer-for-husband"]
  },
  {
    slug: "prayer-for-valentines-day",
    title: "Prayer for Valentine's Day",
    metaDesc: "A Valentine's Day prayer celebrating love in all its forms. Ask God to deepen your relationships, heal your heart, and fill you with His perfect love.",
    category: "Relationships",
    keywords: ["prayer for Valentine's Day", "Valentine's Day prayer", "prayer for love on Valentine's Day", "prayer for singles on Valentine's Day", "Valentine prayer for couples"],
    samplePrayer: "Lord of love, on this Valentine's Day I come before You — the source of all true love. Thank You for loving me first, before I had anything to offer. For those of us in relationships, deepen our love and strengthen our commitment. For those who are single today, remind them that they are fully and perfectly loved by You, not incomplete or lacking. Heal anyone whose heart carries pain around love — from rejection, loss, or disappointment. Let this day be less about chocolates and flowers and more about receiving and giving Your love. Fill us with You. In Jesus' name, Amen.",
    scripture: [
      { ref: "1 John 4:19", text: "We love because he first loved us." },
      { ref: "Romans 8:38-39", text: "For I am convinced that neither death nor life... will be able to separate us from the love of God that is in Christ Jesus our Lord." }
    ],
    related: ["love", "marriage", "relationships", "prayer-for-future-spouse", "prayer-for-boyfriend"]
  },
  {
    slug: "prayer-for-new-years-eve",
    title: "Prayer for New Year's Eve",
    metaDesc: "A New Year's Eve prayer to close the year with gratitude and open the new year with hope. Surrender the past and step into what God has next for you.",
    category: "Celebration",
    keywords: ["prayer for New Year's Eve", "New Year's Eve prayer", "end of year prayer", "prayer on New Year's Eve", "prayer before midnight on New Year's"],
    samplePrayer: "Father, as the final hours of this year tick away, I pause to stand before You in gratitude and surrender. What a year it has been — with its joys, its griefs, its surprises, and its grace. Thank You for every moment You were present, even the ones where I did not feel You. I release everything I am carrying from this year into Your hands. The regrets, the unfinished things, the unanswered prayers. And I step toward midnight with hope, because You are already in the new year waiting for me. Happy New Year, Lord. May the year ahead be full of You. In Jesus' name, Amen.",
    scripture: [
      { ref: "Lamentations 3:22-23", text: "Because of the Lord's great love we are not consumed, for his compassions never fail. They are new every morning; great is your faithfulness." },
      { ref: "Revelation 21:5", text: "He who was seated on the throne said, 'I am making everything new!'" }
    ],
    related: ["new-year-prayer", "thanksgiving", "new-beginnings", "prayer-for-today", "letting-go"]
  },
  {
    slug: "prayer-for-halloween-alternative",
    title: "Prayer for a Godly Halloween Alternative",
    metaDesc: "A prayer for families seeking a Christian alternative to Halloween. Ask God to protect your children and guide your family in honoring Him this season.",
    category: "Family",
    keywords: ["prayer for Halloween alternative", "Christian Halloween prayer", "prayer for Reformation Day", "prayer for fall festival", "prayer for children on Halloween"],
    samplePrayer: "Lord, as the world around us fills with images of darkness and fear, I come before You asking for Your light to fill our home this season. Guard my children's hearts and minds from anything that glorifies what is dark. Give me wisdom as a parent to create meaningful, joyful alternatives that point them toward You. Help us to use this season to be a light in our neighborhood — loving our community and reflecting Your goodness. We are not afraid of darkness because You are the Light of the world and You live in us. Protect our family. In Jesus' name, Amen.",
    scripture: [
      { ref: "Ephesians 5:11", text: "Have nothing to do with the fruitless deeds of darkness, but rather expose them." },
      { ref: "Matthew 5:16", text: "In the same way, let your light shine before others, that they may see your good deeds and glorify your Father in heaven." }
    ],
    related: ["children", "family", "protection", "prayer-for-advent", "faith"]
  },

  // ── COUNTRIES / NATIONS ───────────────────────────────────────────────────
  {
    slug: "prayer-for-nigeria",
    title: "Prayer for Nigeria",
    metaDesc: "A prayer for Nigeria — for peace, economic stability, godly leadership, an end to insecurity, and a great spiritual revival across the nation.",
    category: "Nation",
    keywords: ["prayer for Nigeria", "Nigeria prayer", "prayer for peace in Nigeria", "prayer for Nigerian government", "prayer for Nigeria's revival"],
    samplePrayer: "Father, I lift up Nigeria before Your throne. This great nation is full of faith, potential, and people who love You. I pray for a breaking of every chain that has held back this country's full blessing — corruption, insecurity, poverty, and division. Raise up leaders who fear You and govern with integrity. Bring peace to the North and stability to every region. Protect Your people who face violence and persecution. Pour out revival on Nigeria in a way that transforms communities from the inside out. Nigeria shall be saved. In Jesus' name, Amen.",
    scripture: [
      { ref: "2 Chronicles 7:14", text: "If my people, who are called by my name, will humble themselves and pray and seek my face and turn from their wicked ways, then I will hear from heaven, and I will forgive their sin and will heal their land." },
      { ref: "Psalm 33:12", text: "Blessed is the nation whose God is the Lord, the people he chose for his inheritance." }
    ],
    related: ["nation", "government", "prayer-for-africa", "peace-in-the-world", "revival"]
  },
  {
    slug: "prayer-for-ghana",
    title: "Prayer for Ghana",
    metaDesc: "A prayer for Ghana — for peace, prosperity, strong leadership, and the continued spiritual heritage that has made Ghana a beacon of faith in Africa.",
    category: "Nation",
    keywords: ["prayer for Ghana", "Ghana prayer", "prayer for peace in Ghana", "prayer for Ghana's leaders", "prayer for Ghana nation"],
    samplePrayer: "Lord God, I pray for Ghana today — this nation that has often been called an island of peace in West Africa. Protect and sustain that peace. Raise up leaders of integrity who put the people before personal gain. Bless the economy and bring jobs and opportunity to the young generation who are the nation's future. Let the church of Ghana be a voice of righteousness and a source of compassion. Guard this nation from violence, division, and spiritual deception. Ghana belongs to You, Lord. Have Your way here. In Jesus' name, Amen.",
    scripture: [
      { ref: "Jeremiah 29:7", text: "Seek the peace and prosperity of the city to which I have carried you into exile. Pray to the Lord for it, because if it prospers, you too will prosper." },
      { ref: "Proverbs 14:34", text: "Righteousness exalts a nation, but sin condemns any people." }
    ],
    related: ["prayer-for-nigeria", "prayer-for-africa", "nation", "government", "peace-in-the-world"]
  },
  {
    slug: "prayer-for-kenya",
    title: "Prayer for Kenya",
    metaDesc: "A prayer for Kenya — for unity among tribes, stable governance, economic growth, and the continued revival of the church across this beautiful nation.",
    category: "Nation",
    keywords: ["prayer for Kenya", "Kenya prayer", "prayer for peace in Kenya", "prayer for Kenya government", "Kenya national prayer"],
    samplePrayer: "Heavenly Father, I stand in prayer for Kenya — a land of great beauty, great faith, and great potential. I pray for unity across tribal and political divides. Bind the spirit of tribalism that has too often brought pain and division. Bless the leaders You have placed in authority and give them wisdom beyond their own. Protect ordinary Kenyans who wake up every day simply trying to provide for their families. Let the church in Kenya be salt and light. May this generation see a Kenya where justice flows like a river. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 85:10", text: "Love and faithfulness meet together; righteousness and peace kiss each other." },
      { ref: "Amos 5:24", text: "But let justice roll on like a river, righteousness like a never-failing stream!" }
    ],
    related: ["prayer-for-africa", "prayer-for-nigeria", "nation", "government", "justice"]
  },
  {
    slug: "prayer-for-south-africa",
    title: "Prayer for South Africa",
    metaDesc: "A prayer for South Africa — for healing of racial wounds, safety from crime, economic justice, and God's restoration of a nation still finding its way.",
    category: "Nation",
    keywords: ["prayer for South Africa", "South Africa prayer", "prayer for healing in South Africa", "prayer for South Africa crime", "South Africa intercession prayer"],
    samplePrayer: "Lord, South Africa carries wounds that only You can fully heal. The legacy of apartheid still scars the soul of this nation, and crime, inequality, and corruption continue to steal what should be thriving. I pray for Your healing hand on South Africa — on its communities, its economy, and its relationships between people of different backgrounds. Bring true reconciliation, not just tolerance but genuine love and dignity. Raise up leaders of courage and integrity. Protect the vulnerable. And let the church of South Africa be the loudest voice for justice and the warmest hands of compassion. In Jesus' name, Amen.",
    scripture: [
      { ref: "Isaiah 61:4", text: "They will rebuild the ancient ruins and restore the places long devastated; they will renew the ruined cities that have been devastated for generations." },
      { ref: "Micah 6:8", text: "He has shown you, O mortal, what is good. And what does the Lord require of you? To act justly and to love mercy and to walk humbly with your God." }
    ],
    related: ["prayer-for-africa", "racial-healing", "justice", "nation", "government"]
  },
  {
    slug: "prayer-for-africa",
    title: "Prayer for Africa",
    metaDesc: "An intercession prayer for the continent of Africa — for peace, prosperity, freedom from poverty and corruption, and a great end-time revival across Africa.",
    category: "Nation",
    keywords: ["prayer for Africa", "prayer for the continent of Africa", "Africa prayer", "prayer for African nations", "intercession for Africa"],
    samplePrayer: "Father, I lift up the great continent of Africa before Your throne. From North Africa to the Cape, from the East coast to the West — this is a continent teeming with Your people and Your purposes. I pray against corruption, poverty, disease, and conflict that rob Africa of its dignity and potential. Let every nation's church rise in its prophetic voice and its practical care. Bring an end-time revival to Africa that shakes the nations of the earth. Pour out economic blessing. Raise up leaders who reflect Your character. Africa shall be saved, and her children shall arise. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 68:31", text: "Envoys will come from Egypt; Cush will submit her hands to God." },
      { ref: "Zephaniah 3:9", text: "Then I will purify the lips of the peoples, that all of them may call on the name of the Lord and serve him shoulder to shoulder." }
    ],
    related: ["prayer-for-nigeria", "prayer-for-kenya", "prayer-for-south-africa", "nation", "revival"]
  },
  {
    slug: "prayer-for-ukraine",
    title: "Prayer for Ukraine",
    metaDesc: "A prayer for Ukraine — for an end to war, protection of civilians, healing for the wounded, and God's peace to reign in this war-torn nation.",
    category: "Nation",
    keywords: ["prayer for Ukraine", "Ukraine prayer", "prayer for peace in Ukraine", "prayer for Ukrainian people", "prayer for war to end in Ukraine"],
    samplePrayer: "Lord of all nations, I cry out for Ukraine. Every day, lives are lost, homes are destroyed, and families are torn apart. I pray for an end to this war — not just a ceasefire, but genuine, lasting peace. Protect every civilian, every family sheltering in fear. Comfort the bereaved, the displaced, and the traumatized. Give courage to those fighting for their survival. Bring the right people to the table with the wisdom and will to forge peace. Let Your light shine in the darkest corners of this conflict. Ukraine needs You, Lord. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 46:9", text: "He makes wars cease to the ends of the earth. He breaks the bow and shatters the spear; he burns the shields with fire." },
      { ref: "Isaiah 2:4", text: "He will judge between the nations and will settle disputes for many peoples. They will beat their swords into plowshares and their spears into pruning hooks." }
    ],
    related: ["war-and-peace", "peace-in-the-world", "prayer-for-israel-peace", "nation", "military"]
  },
  {
    slug: "prayer-for-israel-peace",
    title: "Prayer for Peace in Israel",
    metaDesc: "A prayer for peace in Israel — for safety for all people, an end to conflict, and the coming of God's shalom to the Holy Land and the Middle East.",
    category: "Nation",
    keywords: ["prayer for Israel peace", "prayer for peace in Israel", "pray for peace of Jerusalem", "prayer for Israel and Palestine", "Middle East peace prayer"],
    samplePrayer: "Lord God of Abraham, Isaac, and Jacob — You chose Jerusalem as the city of Your name, and Your heart is bound to that land and its people. I pray for peace in Israel and throughout the Middle East. Protect every innocent life. Bring an end to the cycle of violence and retaliation. Give wisdom to leaders on all sides. Open eyes to see one another as image bearers of God. And according to Your promise, let Jerusalem ultimately know the peace that only comes when the Prince of Peace reigns. I pray with the Psalmist: pray for the peace of Jerusalem. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 122:6", text: "Pray for the peace of Jerusalem: 'May those who love you be secure.'" },
      { ref: "Romans 10:1", text: "Brothers and sisters, my heart's desire and prayer to God for the Israelites is that they may be saved." }
    ],
    related: ["prayer-for-ukraine", "war-and-peace", "peace-in-the-world", "nation", "government"]
  },
  {
    slug: "prayer-for-america",
    title: "Prayer for America",
    metaDesc: "A prayer for America — for unity, godly leadership, healing of division, and a spiritual awakening across the United States of America.",
    category: "Nation",
    keywords: ["prayer for America", "prayer for the United States", "prayer for America's leaders", "prayer for America's healing", "patriotic Christian prayer"],
    samplePrayer: "Almighty God, I pray for America today. This nation was founded by people who — however imperfectly — acknowledged their dependence on You. I ask for a return to that humility. Heal the deep divisions that threaten to tear this country apart. Raise up leaders who govern with wisdom, integrity, and a fear of God rather than a fear of polls. Let the church in America stop following the culture and start shaping it. Bring a genuine spiritual awakening. May one nation under God become not just a phrase on paper but a lived reality. God bless America. In Jesus' name, Amen.",
    scripture: [
      { ref: "2 Chronicles 7:14", text: "If my people, who are called by my name, will humble themselves and pray and seek my face and turn from their wicked ways, then I will hear from heaven." },
      { ref: "Psalm 33:12", text: "Blessed is the nation whose God is the Lord, the people he chose for his inheritance." }
    ],
    related: ["nation", "government", "prayer-for-our-country", "prayer-for-elections", "revival"]
  },

  // ── PROFESSIONS ───────────────────────────────────────────────────────────
  {
    slug: "prayer-for-doctors",
    title: "Prayer for Doctors",
    metaDesc: "A prayer for doctors — for wisdom, steady hands, compassion, and God's supernatural guidance in every diagnosis and treatment they provide.",
    category: "Work",
    keywords: ["prayer for doctors", "prayer for medical doctors", "prayer for physicians", "blessing prayer for doctors", "prayer for doctor's wisdom"],
    samplePrayer: "Lord, I lift up every doctor who wakes up today bearing the enormous responsibility of human life in their hands. Give them wisdom that goes beyond what they studied — supernatural intuition and clarity in every diagnosis. Grant them steady hands, sharp minds, and compassionate hearts. Protect them from burnout, especially in seasons of overwhelming patient loads. Let them feel the weight and the privilege of their calling. May every life they touch be touched by Your healing too. And in the moments when medicine reaches its limits, remind them to look to You. In Jesus' name, Amen.",
    scripture: [
      { ref: "Colossians 3:23", text: "Whatever you do, work at it with all your heart, as working for the Lord, not for human masters." },
      { ref: "Sirach 38:2", text: "From the Most High comes healing, and from the king he will receive a gift." }
    ],
    related: ["prayer-for-nurses", "hospital", "healing", "work", "prayer-for-social-workers"]
  },
  {
    slug: "prayer-for-nurses",
    title: "Prayer for Nurses",
    metaDesc: "A prayer for nurses — for strength through long shifts, compassion in difficult moments, and God's protection and blessing over their vital ministry of care.",
    category: "Work",
    keywords: ["prayer for nurses", "prayer for a nurse", "blessing prayer for nurses", "prayer for nursing staff", "prayer for nurses on night shift"],
    samplePrayer: "Father, nurses carry so much. They see pain, death, grief, and hopeful recoveries all in the span of a single shift. I pray for every nurse today. Sustain them when the hours are long and the demands are heavy. Give them the emotional and physical strength to keep caring with excellence and compassion. Protect them from illness and injury on the job. Let them feel the deep value of what they do — they are more than employees; they are ministers of comfort and healing. Thank You for their courage and their calling. Bless them generously. In Jesus' name, Amen.",
    scripture: [
      { ref: "Galatians 6:9", text: "Let us not become weary in doing good, for at the proper time we will reap a harvest if we do not give up." },
      { ref: "Matthew 25:36", text: "I was sick and you looked after me, I was in prison and you came to visit me." }
    ],
    related: ["prayer-for-doctors", "hospital", "night-shift-prayer", "work", "caregiver"]
  },
  {
    slug: "prayer-for-police-officers",
    title: "Prayer for Police Officers",
    metaDesc: "A prayer for police officers — for their safety, wisdom, integrity, and God's protection as they serve and protect our communities each day.",
    category: "Work",
    keywords: ["prayer for police officers", "prayer for law enforcement", "police prayer", "prayer for a police officer", "blessing for police officers"],
    samplePrayer: "Lord, I pray for police officers today — the men and women who run toward danger so that others can be safe. Protect every officer on duty this moment. Give them wisdom in high-pressure situations. Guard them from harm and from the moral compromises that can come with power. Protect their families who wait at home, never fully at peace until they hear the door open. Where injustice exists within law enforcement, convict hearts and bring reform. And where officers serve with integrity and sacrifice, bless them greatly. Thank You for those who protect and serve. In Jesus' name, Amen.",
    scripture: [
      { ref: "Romans 13:4", text: "For the one in authority is God's servant for your good." },
      { ref: "Psalm 91:11", text: "For he will command his angels concerning you to guard you in all your ways." }
    ],
    related: ["first-responders", "military", "protection", "work", "prayer-for-firefighters-paramedics"]
  },
  {
    slug: "prayer-for-lawyers",
    title: "Prayer for Lawyers",
    metaDesc: "A prayer for lawyers — for integrity, wisdom, God's guidance in their cases, and the courage to pursue justice even when it is costly.",
    category: "Work",
    keywords: ["prayer for lawyers", "prayer for attorneys", "prayer for a lawyer", "blessing for lawyers", "prayer for legal professionals"],
    samplePrayer: "Father, I pray for lawyers and legal professionals who work in the complex world of law. Grant them wisdom to see clearly through complicated cases. Give them the courage to pursue justice even when the easier path is compromise. Protect them from the ethical shortcuts that their industry sometimes rewards. Let their work uphold the dignity of those they represent. For the lawyers who defend the marginalized and the voiceless — bless them with supernatural favor. Let every legal professional who names Your name be known for their integrity. In Jesus' name, Amen.",
    scripture: [
      { ref: "Isaiah 1:17", text: "Learn to do right; seek justice. Defend the oppressed. Take up the cause of the fatherless; plead the case of the widow." },
      { ref: "Proverbs 21:15", text: "When justice is done, it brings joy to the righteous but terror to evildoers." }
    ],
    related: ["justice", "work", "court-case", "prayer-for-doctors", "prayer-for-social-workers"]
  },
  {
    slug: "prayer-for-firefighters-paramedics",
    title: "Prayer for Firefighters and Paramedics",
    metaDesc: "A prayer for firefighters and paramedics — for protection in dangerous situations, wisdom in emergencies, and God's covering over their courageous service.",
    category: "Work",
    keywords: ["prayer for firefighters", "prayer for paramedics", "prayer for first responders", "firefighter prayer", "prayer for EMT workers"],
    samplePrayer: "Lord, I lift up every firefighter and paramedic rushing into situations that most run away from. Cover them with Your supernatural protection as they enter burning buildings and respond to emergencies. Give paramedics the calm and skill they need in the most chaotic moments. Let their training be sharp and their hands be steady. Watch over their families who live in the tension of not knowing what each shift holds. When they witness trauma and tragedy, guard their mental and emotional health. You are their shield and their fortress. Protect them. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 91:7", text: "A thousand may fall at your side, ten thousand at your right hand, but it will not come near you." },
      { ref: "John 15:13", text: "Greater love has no one than this: to lay down one's life for one's friends." }
    ],
    related: ["first-responders", "prayer-for-police-officers", "protection", "military", "work"]
  },
  {
    slug: "prayer-for-pilots",
    title: "Prayer for Pilots",
    metaDesc: "A prayer for pilots — for safety, focus, wisdom in the cockpit, and God's protection over every flight they command and every life in their care.",
    category: "Work",
    keywords: ["prayer for pilots", "prayer for airline pilots", "pilot safety prayer", "prayer for a pilot", "blessing prayer for pilots"],
    samplePrayer: "Lord, I lift up every pilot who takes to the skies today. The lives of many rest on their skill, their focus, and the safe operation of their aircraft. I ask for Your angels to surround every cockpit. Give each pilot clarity of mind, sharp reflexes, and wisdom in every decision. Protect them from mechanical failures, dangerous weather, and anything that would threaten safe flight. Let them land every time safely, having carried their passengers as precious cargo. For pilots who are believers, let them feel Your presence at 35,000 feet. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 91:11-12", text: "For he will command his angels concerning you to guard you in all your ways; they will lift you up in their hands, so that you will not strike your foot against a stone." },
      { ref: "Isaiah 40:31", text: "But those who hope in the Lord will renew their strength. They will soar on wings like eagles." }
    ],
    related: ["travel", "prayer-before-flying", "prayer-for-safe-flight", "work", "protection"]
  },
  {
    slug: "prayer-for-social-workers",
    title: "Prayer for Social Workers",
    metaDesc: "A prayer for social workers — for strength, wisdom, and emotional resilience as they serve the vulnerable, marginalized, and overlooked in our society.",
    category: "Work",
    keywords: ["prayer for social workers", "prayer for a social worker", "blessing for social workers", "prayer for child welfare workers", "prayer for case workers"],
    samplePrayer: "Father, the work social workers do is often invisible but never unimportant. They walk into broken homes, complex trauma, and heartbreaking situations to protect the vulnerable. I pray for every social worker today. Give them wisdom in making difficult decisions. Guard their hearts from compassion fatigue. Provide them with the resources, support, and supervision they need to do their work well. When they feel the weight of impossible choices, remind them that You are the ultimate advocate for the vulnerable. Let their work be a form of ministry. In Jesus' name, Amen.",
    scripture: [
      { ref: "Proverbs 31:8", text: "Speak up for those who cannot speak for themselves, for the rights of all who are destitute." },
      { ref: "Matthew 25:40", text: "Truly I tell you, whatever you did for one of the least of these brothers and sisters of mine, you did for me." }
    ],
    related: ["work", "prayer-for-nurses", "prayer-for-counselors", "prayer-for-doctors", "justice"]
  },
  {
    slug: "prayer-for-counselors",
    title: "Prayer for Counselors and Therapists",
    metaDesc: "A prayer for counselors and therapists — for wisdom, compassion, healthy boundaries, and God's guidance as they help others heal and find wholeness.",
    category: "Work",
    keywords: ["prayer for counselors", "prayer for therapists", "prayer for mental health counselors", "prayer for Christian counselors", "blessing for counselors"],
    samplePrayer: "Lord, I pray for counselors and therapists who sit with people in their most vulnerable moments. Give them wisdom that goes beyond their training — supernatural insight into the roots of what their clients are experiencing. Guard their own mental health as they absorb the weight of others' pain. Help them to maintain healthy boundaries while remaining genuinely compassionate. For Christian counselors, let Your Spirit guide every session in ways they may not even recognize. Let their work bring real transformation and lasting wholeness. In Jesus' name, Amen.",
    scripture: [
      { ref: "Isaiah 9:6", text: "And he will be called Wonderful Counselor, Mighty God, Everlasting Father, Prince of Peace." },
      { ref: "Proverbs 11:14", text: "For lack of guidance a nation falls, but victory is won through many advisers." }
    ],
    related: ["prayer-for-social-workers", "mental-health", "work", "prayer-for-doctors", "prayer-for-nurses"]
  },

  // ── SPECIAL OCCASIONS ─────────────────────────────────────────────────────
  {
    slug: "prayer-for-baby-shower",
    title: "Prayer for a Baby Shower",
    metaDesc: "A joyful baby shower prayer celebrating the coming new life. Ask God to bless the mother, baby, and family as they prepare to welcome their new child.",
    category: "Celebration",
    keywords: ["prayer for baby shower", "baby shower prayer", "opening prayer for baby shower", "prayer at a baby shower", "blessing prayer for baby shower"],
    samplePrayer: "Lord, what a joy to gather in this moment to celebrate the precious life that is coming! Thank You for the miracle of new life and for this mama who is carrying this treasure. Bless her body with health and her spirit with peace as she prepares to welcome this child. May the baby grow strong and healthy, developing perfectly in Your perfect design. Surround this family with love, provision, and support. Let every diaper, every gift, and every prayer given today be a symbol of the village that will help raise this child in Your love. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 127:3", text: "Children are a heritage from the Lord, offspring a reward from him." },
      { ref: "Psalm 139:13", text: "For you created my inmost being; you knit me together in my mother's womb." }
    ],
    related: ["newborn-baby", "pregnancy", "healthy-pregnancy", "safe-delivery", "nicu-prayer"]
  },
  {
    slug: "prayer-for-gender-reveal",
    title: "Prayer for a Gender Reveal",
    metaDesc: "A sweet gender reveal prayer celebrating the announcement of baby's sex. Ask God to bless the baby, parents, and all who gather to celebrate this milestone.",
    category: "Celebration",
    keywords: ["prayer for gender reveal", "gender reveal prayer", "prayer before gender reveal", "baby gender reveal blessing", "prayer at gender reveal party"],
    samplePrayer: "Heavenly Father, what a joyful day this is! Soon we will know whether this precious life You have created is a boy or a girl, and our hearts can barely contain the excitement. But Lord, before we find out, we want to acknowledge that You already know — You have known this child since before the world began. Whatever the reveal shows, we receive this child as Your gift. We pray that this baby grows up knowing they are fearfully and wonderfully made. Bless this family, Lord, and let the celebration today be a testimony to Your goodness. In Jesus' name, Amen.",
    scripture: [
      { ref: "Jeremiah 1:5", text: "Before I formed you in the womb I knew you, before you were born I set you apart." },
      { ref: "Psalm 127:3", text: "Children are a heritage from the Lord, offspring a reward from him." }
    ],
    related: ["pregnancy", "prayer-for-baby-shower", "newborn-baby", "healthy-pregnancy", "thanksgiving"]
  },
  {
    slug: "prayer-for-house-warming",
    title: "Prayer for a House Warming",
    metaDesc: "A house blessing prayer for a housewarming celebration. Dedicate your new home to God and ask for His presence, peace, and protection within its walls.",
    category: "Celebration",
    keywords: ["prayer for house warming", "housewarming prayer", "house blessing prayer", "prayer for new home blessing", "prayer to dedicate a new house"],
    samplePrayer: "Lord God, we dedicate this new home to You. May Your presence fill every room, every hallway, and every corner of this house. Let peace dwell here. Let laughter ring out in these walls and let love be the foundation of everything that happens under this roof. Guard this home from every spirit that is not of You. Protect it from every danger — natural and spiritual. Bless the family who will live here with provision, joy, and safety. May this home be a place of refuge for all who enter, a house of prayer, and a testimony to Your faithfulness. In Jesus' name, Amen.",
    scripture: [
      { ref: "Joshua 24:15", text: "As for me and my household, we will serve the Lord." },
      { ref: "Psalm 127:1", text: "Unless the Lord builds the house, the builders labor in vain." }
    ],
    related: ["prayer-for-new-home", "home-protection", "blessing", "thanksgiving", "family"]
  },
  {
    slug: "prayer-for-retirement-celebration",
    title: "Prayer for a Retirement Celebration",
    metaDesc: "A prayer to honor someone's retirement and celebrate their faithful years of service. Ask God to bless their new season with purpose, rest, and abundant joy.",
    category: "Celebration",
    keywords: ["prayer for retirement celebration", "retirement party prayer", "prayer for someone retiring", "blessing prayer for retirement", "prayer to honor a retiree"],
    samplePrayer: "Lord, we come before You with gratitude to celebrate the faithful service of someone who has given so much. The years of dedication, the early mornings, the late nights, the problems solved and the lives impacted — You have seen every moment. We honor that today. Now as a new chapter opens, we ask that You fill it with joy, purpose, and the rest they have earned. Give them good health to enjoy this season. Keep them engaged in meaningful pursuits. And let them step into retirement knowing their best days are not behind them — the greatest chapters are still to come. In Jesus' name, Amen.",
    scripture: [
      { ref: "Matthew 25:21", text: "'Well done, good and faithful servant! You have been faithful with a few things; I will put you in charge of many things. Come and share your master's happiness!'" },
      { ref: "Proverbs 16:31", text: "Gray hair is a crown of splendor; it is attained in the way of righteousness." }
    ],
    related: ["prayer-for-retirement", "thanksgiving", "blessing", "celebration", "elderly"]
  },
  {
    slug: "prayer-for-farewell",
    title: "Prayer for a Farewell",
    metaDesc: "A heartfelt farewell prayer for someone leaving — a job, city, church, or season. Bless their journey and trust God with the transition ahead.",
    category: "Celebration",
    keywords: ["prayer for farewell", "farewell prayer", "prayer for someone leaving", "going away prayer", "farewell blessing prayer"],
    samplePrayer: "Lord, goodbyes are never easy, even when the change ahead is good. We gather today to send off someone we love and value, and we do so with mixed hearts — gratitude for the season we have shared and hope for what lies ahead for them. We ask You to go before them into this next chapter. Open the right doors. Surround them with good people. Let the gifts and growth from this season travel with them. And bind our hearts together across the distance, knowing that those who are in Christ are never truly separated. Bless them mightily. In Jesus' name, Amen.",
    scripture: [
      { ref: "Numbers 6:24-26", text: "The Lord bless you and keep you; the Lord make his face shine on you and be gracious to you; the Lord turn his face toward you and give you peace." },
      { ref: "Ruth 1:16-17", text: "Where you go I will go, and where you stay I will stay." }
    ],
    related: ["blessing", "moving", "thanksgiving", "travel", "new-beginnings"]
  },
  {
    slug: "prayer-for-reunion",
    title: "Prayer for a Reunion",
    metaDesc: "A prayer for a family or class reunion — for joy, healing of old wounds, gratitude for shared history, and God's blessing over the gathering.",
    category: "Celebration",
    keywords: ["prayer for reunion", "family reunion prayer", "class reunion prayer", "prayer before reunion", "opening prayer for reunion"],
    samplePrayer: "Father, what a gift it is to be gathered together again. The years have passed, we have each walked our own roads, and yet here we are — connected by family, by history, by love. We thank You for every person present and remember with love those who are no longer with us. Let this time together be one of genuine joy, open hearts, and healed relationships. Where distance — physical or emotional — has grown between us, let this reunion begin to mend it. Bless the food, the laughter, and the memories we will make. May this time together be a true gift. In Jesus' name, Amen.",
    scripture: [
      { ref: "Psalm 133:1", text: "How good and pleasant it is when God's people live together in unity!" },
      { ref: "Romans 15:5-6", text: "May the God who gives endurance and encouragement give you the same attitude of mind toward each other that Christ Jesus had." }
    ],
    related: ["family", "thanksgiving", "celebration", "friendship", "prayer-for-farewell"]
  },
  {
    slug: "prayer-for-sports-team",
    title: "Prayer for a Sports Team",
    metaDesc: "A prayer for a sports team before a game or season. Ask God for unity, excellence, sportsmanship, safety, and His glory to shine through your team.",
    category: "Youth",
    keywords: ["prayer for sports team", "sports team prayer", "prayer before a game", "team prayer", "prayer for athletes before competition"],
    samplePrayer: "Lord, we come to You as a team before we step onto the field. We acknowledge that our ability comes from You, and so we offer it back to You today. Let us play with excellence — giving everything we have with focus, skill, and heart. Guard us from injury. Keep our minds sharp and our bodies strong. When things get hard, remind us that we are in this together. Win or lose, let us compete with integrity and represent You well. Let every opponent see something different in how we play — something they cannot explain except by You. Let us play for an audience of One. In Jesus' name, Amen.",
    scripture: [
      { ref: "1 Corinthians 9:24", text: "Do you not know that in a race all the runners run, but only one gets the prize? Run in such a way as to get the prize." },
      { ref: "Colossians 3:23", text: "Whatever you do, work at it with all your heart, as working for the Lord, not for human masters." }
    ],
    related: ["prayer-for-athletes-competition", "youth-prayer", "strength", "courage", "work"]
  },
  {
    slug: "prayer-for-athletes-competition",
    title: "Prayer for Athletes Before Competition",
    metaDesc: "A prayer for athletes before a competition or big game. Ask God for peak performance, mental strength, safety, and the courage to compete with excellence.",
    category: "Youth",
    keywords: ["prayer for athletes", "prayer before competition", "athlete prayer", "prayer for a big game", "prayer for athletic performance"],
    samplePrayer: "Lord, today I step into competition, and I choose to start here — with You. You made this body, You gave me this gift, and I dedicate this performance to Your glory. Calm the nerves and sharpen the focus. Let every hour of training come alive in this moment. Give me the mental strength to push past barriers when the moment gets difficult. Guard me from injury and let my body perform at its peak. And whatever the outcome, let me walk off the field knowing I gave everything I had and honored You in how I competed. Here we go, Lord. In Jesus' name, Amen.",
    scripture: [
      { ref: "Philippians 4:13", text: "I can do all this through him who gives me strength." },
      { ref: "Isaiah 40:31", text: "But those who hope in the Lord will renew their strength. They will soar on wings like eagles; they will run and not grow weary, they will walk and not be faint." }
    ],
    related: ["prayer-for-sports-team", "strength", "courage", "youth-prayer", "work"]
  }
];
