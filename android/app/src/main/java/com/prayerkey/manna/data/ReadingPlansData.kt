package com.prayerkey.manna.data

/*
 * The reading plans. Written, not generated: each day is a passage, a short
 * devotion on it, a prayer point and a question. Built from JSON by a script,
 * one list per plan so no single initializer outgrows a JVM method.
 */

private val PLAN_0 = ReadingPlanDef(
    id = "new-believer",
    title = "New Believer",
    topic = "Faith",
    blurb = "A 7-day journey to build a solid foundation in your relationship with God.",
    verse = "In the beginning was the Word, and the Word was with God, and the Word was God.",
    verseRef = "John 1:1",
    art = com.prayerkey.manna.R.drawable.onboarding_read_v3,
    days = listOf(
        PlanDay("The Beginning", "Genesis", 1, 1, 5, "God Creates", "In the beginning, God spoke, and everything came to life. The same God who brought light out of darkness can bring order, beauty and purpose into your life today.", "Thank God for His creativity and for the purpose He has for your life.", "What does this passage teach you about who God is?"),
        PlanDay("Created with Purpose", "Genesis", 1, 26, 31, "Made in His Image", "You were not an accident. God made you in His image, on purpose, and looked at what He had made and called it very good.", "Ask God to show you the purpose He had in mind when He made you.", "What would change if you believed God calls you good?"),
        PlanDay("The Gift of Grace", "Ephesians", 2, 1, 10, "Saved by Grace", "You cannot earn God's love, and you do not have to. Salvation is a gift, received by faith, and it is given freely by a God who is rich in mercy.", "Thank God for grace you could never earn.", "Where have you been trying to earn what God freely gives?"),
        PlanDay("A New Life", "2 Corinthians", 5, 14, 21, "All Things New", "In Christ you are a new creation. The old has gone. This is not self-improvement; it is God making something new of you.", "Ask God to help you live as the new person He has made you.", "What old thing is God asking you to let go of?"),
        PlanDay("Walking with God", "Micah", 6, 6, 8, "What He Asks", "God does not ask for a performance. He asks you to do what is right, to love mercy, and to walk humbly with Him, one day at a time.", "Ask God to walk with you through today, step by step.", "What does walking humbly with God look like for you this week?"),
        PlanDay("Living with Faith", "Hebrews", 11, 1, 6, "Faith Pleases God", "Faith is trusting God before you can see how things will turn out. It is not the absence of questions; it is choosing to rely on Him anyway.", "Ask God to grow your faith in the place you find hardest to trust Him.", "Where do you need faith more than answers right now?"),
        PlanDay("Your Next Steps", "Matthew", 28, 16, 20, "Go and Make Disciples", "Jesus did not leave His followers alone. He sent them out with a promise: I am with you always. That promise is now yours.", "Ask God to show you your next step of following Him.", "Who could you share what God is doing in you with?"),
    ),
)

private val PLAN_1 = ReadingPlanDef(
    id = "identity",
    title = "Identity in Christ",
    topic = "Faith",
    blurb = "Seven days to discover who God says you are.",
    verse = "See what great love the Father has lavished on us, that we should be called children of God.",
    verseRef = "1 John 3:1",
    art = com.prayerkey.manna.R.drawable.onboarding_journal_v3,
    days = listOf(
        PlanDay("Chosen", "Ephesians", 1, 3, 8, "Chosen Before Time", "Before the world began, God chose you. Your worth was settled long before you did anything to prove it.", "Thank God that you are chosen, not merely tolerated.", "How would you live today if you truly believed you were chosen?"),
        PlanDay("Loved", "Romans", 8, 35, 39, "Nothing Can Separate", "No failure, fear or power can separate you from the love of God in Christ. His love is not fragile.", "Ask God to settle His love deep in your heart.", "What have you believed could separate you from God's love?"),
        PlanDay("Forgiven", "Psalms", 103, 8, 14, "As Far as the East", "God does not keep a record of what He has forgiven. He has removed your sin as far as the east is from the west.", "Thank God that you are forgiven, completely.", "What have you forgiven that you keep picking back up?"),
        PlanDay("A Child of God", "Galatians", 4, 4, 7, "No Longer a Slave", "You are not a servant hoping to earn a place. You are a child and an heir, and you can call God Father.", "Speak to God today as your Father.", "What does it mean to you that God calls you His child?"),
        PlanDay("Free", "Galatians", 5, 1, 6, "Stand Firm", "Christ set you free so that you would stay free. Do not return to what once held you.", "Ask God for strength to stand in the freedom He has given.", "Where are you tempted to walk back into what once held you?"),
        PlanDay("Made New", "Colossians", 3, 1, 10, "Set Your Mind Above", "Your life is now hidden with Christ in God. Let your thoughts be shaped by who you are in Him, not by who you used to be.", "Ask God to renew the way you think about yourself.", "What old label do you need to put off?"),
        PlanDay("Sent", "John", 15, 12, 17, "Chosen to Bear Fruit", "You did not choose Him first; He chose you and appointed you to bear fruit that lasts. Your identity comes with a purpose.", "Ask God to make your life fruitful for others.", "Where could you love someone as Christ loved you this week?"),
    ),
)

private val PLAN_2 = ReadingPlanDef(
    id = "anxiety",
    title = "Overcoming Anxiety",
    topic = "Peace",
    blurb = "Seven days of Scripture for the anxious heart.",
    verse = "Cast all your anxiety on Him because He cares for you.",
    verseRef = "1 Peter 5:7",
    art = com.prayerkey.manna.R.drawable.onboarding_prayer,
    days = listOf(
        PlanDay("He Cares for You", "1 Peter", 5, 6, 11, "Cast Your Cares", "God does not tell you to carry your worries better. He tells you to throw them onto Him, because He cares for you.", "Name one worry and give it to God.", "What would it feel like to not carry this alone?"),
        PlanDay("Do Not Worry", "Matthew", 6, 25, 34, "Look at the Birds", "Jesus points to the birds and the flowers. Your Father feeds and clothes them, and you are worth far more to Him.", "Ask God to help you trust Him for today only.", "Which worry belongs to tomorrow, not today?"),
        PlanDay("Pray About Everything", "Philippians", 4, 4, 9, "The Peace That Guards", "Prayer with thanksgiving does something worry cannot: it invites God's peace to stand guard over your heart and mind.", "Bring your requests to God, with thanks.", "What true and lovely thing can you think on today?"),
        PlanDay("When I Am Afraid", "Psalms", 56, 1, 13, "I Will Trust", "David did not pretend he was not afraid. He said, when I am afraid, I will trust in You. Fear and faith can meet in the same moment.", "Tell God honestly what you are afraid of.", "What does trusting God look like in the middle of fear?"),
        PlanDay("Perfect Peace", "Isaiah", 26, 1, 4, "A Steady Mind", "God keeps in perfect peace the one whose mind is fixed on Him. Peace comes from where your attention rests.", "Ask God to steady your thoughts on Him.", "Where does your mind tend to wander when you are anxious?"),
        PlanDay("Be Still", "Psalms", 46, 1, 11, "God Is Our Refuge", "Even if the earth gives way, God is a very present help in trouble. Stillness is not doing nothing; it is remembering who is God.", "Spend a quiet moment remembering that He is God.", "What would it take for you to be still today?"),
        PlanDay("Peace I Leave You", "John", 14, 25, 31, "Not as the World Gives", "The peace Jesus gives does not depend on circumstances going well. It is His own peace, given to you.", "Receive the peace Jesus offers.", "How is His peace different from the peace you have been chasing?"),
    ),
)

private val PLAN_3 = ReadingPlanDef(
    id = "thankful",
    title = "Living a Thankful Life",
    topic = "Life",
    blurb = "Seven days to grow a grateful heart.",
    verse = "Give thanks in all circumstances; for this is God's will for you in Christ Jesus.",
    verseRef = "1 Thessalonians 5:18",
    art = com.prayerkey.manna.R.drawable.discovery_cover_4,
    days = listOf(
        PlanDay("Give Thanks", "Psalms", 100, 1, 5, "Enter with Thanks", "Thanksgiving is the way into God's presence. Before you ask for anything, remember who He is and what He has done.", "Thank God for three things before you ask for anything.", "What are you most grateful for this morning?"),
        PlanDay("In All Circumstances", "1 Thessalonians", 5, 16, 24, "Thanks in Everything", "You are not asked to be thankful for everything, but in everything. Even in hard seasons, God is still good and still near.", "Thank God for His presence in a hard situation.", "What can you thank God for even in this season?"),
        PlanDay("Remember His Benefits", "Psalms", 103, 1, 5, "Forget Not", "Gratitude is memory put to work. The psalmist tells his own soul not to forget what God has done.", "List what God has done for you and thank Him.", "Which of God's kindnesses have you forgotten?"),
        PlanDay("The One Who Returned", "Luke", 17, 11, 19, "Ten Were Healed", "Ten were healed, but only one came back to say thank you. Gratitude turns a blessing into a relationship.", "Go back to God today and thank Him for an answered prayer.", "What answered prayer have you not thanked God for?"),
        PlanDay("Content in Christ", "Philippians", 4, 10, 13, "The Secret of Contentment", "Paul learned to be content with little and with much, because his strength came from Christ and not from his circumstances.", "Ask God to teach you contentment.", "Where are you most tempted to want more?"),
        PlanDay("Every Good Gift", "James", 1, 16, 18, "From the Father of Lights", "Every good thing in your life has come from God. He does not change, and His goodness does not run out.", "Thank God for an ordinary gift you usually overlook.", "What good gift have you taken for granted?"),
        PlanDay("A Sacrifice of Praise", "Hebrews", 13, 15, 16, "Continual Praise", "Praise is an offering you can bring every day, and doing good and sharing with others pleases God as well.", "Offer God praise, and look for someone to bless today.", "How could you turn gratitude into generosity today?"),
    ),
)

private val PLAN_4 = ReadingPlanDef(
    id = "greater-faith",
    title = "Greater Faith",
    topic = "Faith",
    blurb = "Two weeks with the people of faith, and the God they trusted.",
    verse = "Now faith is confidence in what we hope for and assurance about what we do not see.",
    verseRef = "Hebrews 11:1",
    art = com.prayerkey.manna.R.drawable.discovery_cover_5,
    days = listOf(
        PlanDay("What Faith Is", "Hebrews", 11, 1, 6, "Assurance of Things Unseen", "Faith is not a feeling you work up. It is trust placed in a God who rewards those who seek Him.", "Ask God to grow your faith today.", "What are you hoping for that you cannot yet see?"),
        PlanDay("Abraham Believed", "Genesis", 15, 1, 6, "Counted as Righteous", "Abraham had no evidence but a promise and a sky full of stars. He believed God, and God counted it as righteousness.", "Ask God to help you believe His promises.", "Which promise of God are you finding hard to believe?"),
        PlanDay("Going Without Knowing", "Hebrews", 11, 8, 12, "He Went Out", "Abraham left home not knowing where he was going. Faith sometimes means the next step is all you are given.", "Ask God for courage to take the next step.", "What step is God asking you to take without the full picture?"),
        PlanDay("The Test", "Genesis", 22, 1, 14, "The Lord Will Provide", "On the mountain, Abraham discovered a name for God: the Lord will provide. Faith holds on even when it cannot see how.", "Trust God to provide what you need.", "Where do you need God to be your provider?"),
        PlanDay("Moses Chose", "Hebrews", 11, 23, 29, "Seeing the Invisible", "Moses endured because he kept his eyes on the One who is invisible. Faith changes what you choose to value.", "Ask God to set your eyes on what lasts.", "What are you choosing because you are looking at the unseen?"),
        PlanDay("The Walls Fell", "Joshua", 6, 1, 20, "Faith That Obeys", "Israel walked around Jericho for seven days with nothing to show for it. Faith kept obeying until God moved.", "Ask God for patience in obedience.", "Where are you tempted to stop obeying because nothing has happened yet?"),
        PlanDay("Small Faith, Great God", "Matthew", 17, 14, 20, "Faith Like a Mustard Seed", "Jesus did not ask for great faith, but for faith placed in a great God. Even a little is enough in the right hands.", "Bring God the small faith you have.", "What mountain are you facing that seems too big?"),
        PlanDay("Help My Unbelief", "Mark", 9, 17, 27, "I Believe; Help Me", "The father's prayer was honest: I believe, help my unbelief. Jesus answered a faith that admitted its weakness.", "Pray honestly about your doubts.", "Where is your faith mixed with doubt?"),
        PlanDay("Peter Walks", "Matthew", 14, 22, 33, "Take Courage", "Peter walked on water while he looked at Jesus and sank when he looked at the waves. Faith is where you keep your eyes.", "Ask God to keep your eyes on Jesus.", "What waves are pulling your attention away?"),
        PlanDay("The Centurion", "Luke", 7, 1, 10, "Just Say the Word", "The centurion trusted the authority of Jesus' word. He did not need to see it happen to believe it would.", "Ask God to strengthen your trust in His Word.", "Do you believe God's Word is enough?"),
        PlanDay("Daniel Prayed", "Daniel", 6, 10, 23, "Faith Under Pressure", "Daniel kept praying three times a day when it cost him everything. God shut the lions' mouths.", "Ask God for courage to stay faithful under pressure.", "What habit of faith are you tempted to drop when it costs you?"),
        PlanDay("The Fiery Furnace", "Daniel", 3, 16, 28, "Even If He Does Not", "Three young men trusted God to save them, and also decided they would trust Him even if He did not.", "Tell God you trust Him whatever the outcome.", "What would 'even if He does not' mean for you?"),
        PlanDay("Faith and Works", "James", 2, 14, 26, "Faith That Moves", "Faith without action is dead. Real trust in God always shows up in how we treat people.", "Ask God to show you where faith needs to become action.", "How could your faith become visible this week?"),
        PlanDay("Finish the Race", "Hebrews", 12, 1, 3, "Looking to Jesus", "The great cloud of witnesses finished their race. Fix your eyes on Jesus, the author and finisher of your faith.", "Thank God for growing your faith these two weeks.", "What has grown in your faith through this plan?"),
    ),
)

private val PLAN_5 = ReadingPlanDef(
    id = "healing",
    title = "Healing and Wholeness",
    topic = "Healing",
    blurb = "Two weeks with the God who heals body, heart and mind.",
    verse = "He heals the brokenhearted and binds up their wounds.",
    verseRef = "Psalm 147:3",
    art = com.prayerkey.manna.R.drawable.welcome_prayer_dawn,
    days = listOf(
        PlanDay("The Lord Who Heals", "Exodus", 15, 22, 27, "Jehovah Rapha", "At bitter water God revealed Himself as the Lord who heals. He is able to make bitter things sweet.", "Bring God what is bitter in you.", "What in your life needs God's healing touch?"),
        PlanDay("He Heals the Brokenhearted", "Psalms", 147, 1, 6, "Binding Up Wounds", "The God who counts the stars also binds up the wounds of the brokenhearted. No pain is too small for Him.", "Ask God to bind up your wounds.", "What heartbreak have you carried without telling God?"),
        PlanDay("By His Wounds", "Isaiah", 53, 3, 6, "He Carried Our Pain", "Jesus carried our griefs and sorrows. By His wounds we are healed.", "Thank Jesus for carrying what you could not.", "What pain do you need to lay on Him?"),
        PlanDay("If You Are Willing", "Mark", 1, 40, 45, "I Am Willing", "The leper asked if Jesus was willing. Jesus touched him, the untouchable, and said, I am willing.", "Ask Jesus to touch the place you feel untouchable.", "Where do you wonder if God is willing?"),
        PlanDay("Touching the Hem", "Mark", 5, 25, 34, "Your Faith Has Healed You", "After twelve years of suffering, one woman reached out in faith. Jesus stopped the crowd to see her.", "Reach out to Jesus in faith today.", "What have you been suffering with for a long time?"),
        PlanDay("Pick Up Your Mat", "John", 5, 1, 9, "Do You Want to Be Well?", "Jesus asked a man who had waited thirty-eight years if he wanted to be well. Healing sometimes begins with honest wanting.", "Tell God honestly that you want to be well.", "Do you truly want to be made well?"),
        PlanDay("Healing of the Mind", "Romans", 12, 1, 2, "A Renewed Mind", "Wholeness includes the mind. God renews our thinking so we can know His good and perfect will.", "Ask God to renew your thoughts.", "Which thought pattern needs God's healing?"),
        PlanDay("Healing of Memories", "Genesis", 50, 15, 21, "God Meant It for Good", "Joseph's brothers meant him harm, but God worked it for good. Healing does not deny the pain; it sees God in it.", "Ask God to heal a painful memory.", "Where can you see God working through past pain?"),
        PlanDay("Forgiveness Heals", "Matthew", 18, 21, 35, "Seventy Times Seven", "Unforgiveness keeps wounds open. Forgiving others releases you to receive healing yourself.", "Ask God for strength to forgive someone.", "Whom do you need to forgive?"),
        PlanDay("Rest for the Weary", "Matthew", 11, 28, 30, "Come to Me", "Jesus invites the weary to come to Him. Rest is part of healing.", "Accept Jesus' invitation to rest.", "Where do you need rest more than effort?"),
        PlanDay("Praying for Healing", "James", 5, 13, 16, "The Prayer of Faith", "God invites us to pray for the sick and for one another. Healing often comes through community and prayer.", "Pray for someone who needs healing.", "Who can you ask to pray with you?"),
        PlanDay("When Healing Waits", "2 Corinthians", 12, 7, 10, "Grace Is Sufficient", "Paul asked three times, and God answered: My grace is sufficient. Sometimes God's healing comes as strength to endure.", "Ask God for grace while you wait.", "How has God's grace carried you while you waited?"),
        PlanDay("Whole in Christ", "Colossians", 2, 6, 10, "Complete in Him", "In Christ you are complete. Your wholeness does not depend on everything being fixed.", "Thank God that you are complete in Christ.", "What would change if you believed you are whole in Him?"),
        PlanDay("All Things New", "Revelation", 21, 1, 5, "No More Tears", "One day God will wipe away every tear. Every healing now is a small preview of the healing to come.", "Thank God for the hope of complete restoration.", "What healing have you seen during this plan?"),
    ),
)

private val PLAN_6 = ReadingPlanDef(
    id = "peace",
    title = "Peace in Uncertain Times",
    topic = "Peace",
    blurb = "Three weeks of God's steadying Word when the future feels unclear.",
    verse = "You will keep in perfect peace those whose minds are steadfast, because they trust in you.",
    verseRef = "Isaiah 26:3",
    art = com.prayerkey.manna.R.drawable.rhythm_reflection,
    days = listOf(
        PlanDay("The Lord Is My Shepherd", "Psalms", 23, 1, 6, "I Shall Not Want", "When the future is unclear, the Shepherd is not. He leads, restores and walks with you through every valley.", "Ask the Shepherd to lead you today.", "Where do you need a shepherd right now?"),
        PlanDay("My Times Are in Your Hands", "Psalms", 31, 14, 24, "Held by God", "Your future is not floating free. Your times are in God's hands, and they are safe there.", "Place your future in God's hands.", "What part of your future feels out of control?"),
        PlanDay("Plans to Prosper", "Jeremiah", 29, 10, 14, "A Future and a Hope", "God spoke hope to people in exile. Even in seasons you did not choose, His plans for you are good.", "Thank God that His plans are good.", "How does this promise speak to your season?"),
        PlanDay("Do Not Fear", "Isaiah", 41, 8, 13, "I Will Uphold You", "God holds your right hand and says, do not fear, I will help you. You are not facing this alone.", "Ask God to hold your hand today.", "What fear do you need to hear God speak into?"),
        PlanDay("The Storm Obeys", "Mark", 4, 35, 41, "Peace, Be Still", "The disciples panicked while Jesus slept. One word from Him and the storm was still.", "Ask Jesus to speak peace into your storm.", "What storm are you asking Jesus to calm?"),
        PlanDay("Tomorrow Will Worry", "Matthew", 6, 31, 34, "Seek First", "Jesus invites you to live today, seek His kingdom first, and let tomorrow take care of itself.", "Ask God for grace for today only.", "What tomorrow-worry can you leave with God?"),
        PlanDay("A Very Present Help", "Psalms", 46, 1, 7, "Though the Earth Give Way", "Even when everything shakes, God is a refuge and strength. His help is present, not distant.", "Take refuge in God today.", "What feels like it is shaking in your life?"),
        PlanDay("Trust in the Lord", "Proverbs", 3, 1, 8, "Lean Not on Your Understanding", "You will not always understand. You can always trust. God makes the path straight for those who acknowledge Him.", "Ask God to direct your paths.", "Where are you leaning on your own understanding?"),
        PlanDay("The Peace of God", "Philippians", 4, 6, 7, "Guarding Your Heart", "Prayer and thanks invite a peace that makes no sense on paper, and it stands guard over your heart.", "Pray with thanksgiving about what worries you.", "What do you need to bring to God in prayer today?"),
        PlanDay("Unfailing Love", "Lamentations", 3, 19, 26, "New Every Morning", "In the ruins of Jerusalem, Jeremiah found hope: God's mercies are new every morning.", "Thank God for new mercy this morning.", "Where have you seen God's faithfulness in a hard time?"),
        PlanDay("Waiting on God", "Isaiah", 40, 27, 31, "Renewed Strength", "Those who wait on the Lord find their strength renewed. Waiting is not wasted with God.", "Ask God to renew your strength.", "What are you waiting on God for?"),
        PlanDay("He Goes Before You", "Deuteronomy", 31, 6, 8, "He Will Not Leave You", "The Lord goes before you and will be with you. The unknown road is not unknown to Him.", "Ask God to go before you.", "What road ahead feels uncertain?"),
        PlanDay("Joseph's Story", "Genesis", 45, 4, 8, "God Sent Me", "Joseph looked back on years of hardship and saw God's hand. Uncertainty is often where God is quietly working.", "Ask God to help you see His hand.", "Where might God be working in what you do not understand?"),
        PlanDay("God Is Faithful", "2 Timothy", 2, 11, 13, "He Remains Faithful", "Even when we are faithless, He remains faithful. His faithfulness does not depend on ours.", "Thank God for His faithfulness.", "How has God been faithful when you were not?"),
        PlanDay("Cast Your Burden", "Psalms", 55, 16, 23, "He Will Sustain You", "Cast your burden on the Lord and He will sustain you. He does not tire of carrying you.", "Give God the burden you are carrying.", "What burden are you ready to cast on Him?"),
        PlanDay("Not Forgotten", "Isaiah", 49, 13, 16, "Engraved on His Hands", "A mother may forget her child, but God will never forget you. You are written on His hands.", "Thank God that you are never forgotten.", "Where have you felt forgotten?"),
        PlanDay("Everything Works Together", "Romans", 8, 26, 30, "For Good", "God works in all things for the good of those who love Him. Not every thing is good, but God is working in every thing.", "Trust God with what you cannot understand.", "What situation do you need to trust God to work for good?"),
        PlanDay("Hope as an Anchor", "Hebrews", 6, 13, 20, "Sure and Steadfast", "Hope in God is an anchor for the soul. It holds you steady when the water is rough.", "Ask God to anchor you in hope.", "What is your hope anchored to right now?"),
        PlanDay("Be Still and Know", "Psalms", 37, 1, 7, "Rest in the Lord", "Do not fret. Rest in the Lord, commit your way to Him, and wait patiently for Him.", "Commit your way to God today.", "What does resting in the Lord look like for you?"),
        PlanDay("Peace I Give", "John", 16, 31, 33, "I Have Overcome", "Jesus promised trouble in this world and peace in Him. He has already overcome.", "Receive Jesus' peace today.", "How does knowing Jesus has overcome change your outlook?"),
        PlanDay("The God of Peace", "2 Thessalonians", 3, 16, 18, "Peace at All Times", "The Lord of peace gives peace at all times and in every way. He is with you.", "Thank God for His peace these three weeks.", "How has God's peace grown in you through this plan?"),
    ),
)

private val PLAN_7 = ReadingPlanDef(
    id = "prayer-life",
    title = "Prayer Life",
    topic = "Prayer",
    blurb = "Three weeks to learn to pray from the prayers of Scripture.",
    verse = "Call to me and I will answer you and tell you great and unsearchable things you do not know.",
    verseRef = "Jeremiah 33:3",
    art = com.prayerkey.manna.R.drawable.onboarding_pray_v3,
    days = listOf(
        PlanDay("Lord, Teach Us to Pray", "Luke", 11, 1, 4, "The First Request", "The disciples watched Jesus pray and asked Him to teach them. Prayer is learned by staying close to Jesus.", "Ask Jesus to teach you to pray.", "What do you want to learn about prayer?"),
        PlanDay("Our Father", "Matthew", 6, 9, 13, "A Pattern for Prayer", "The Lord's Prayer begins with God, His name and His kingdom, before it turns to our needs.", "Pray the Lord's Prayer slowly.", "Which line of the Lord's Prayer speaks to you today?"),
        PlanDay("In Secret", "Matthew", 6, 5, 8, "The Secret Place", "Prayer is not a performance. God sees what is done in secret and meets you there.", "Find a quiet place to pray today.", "Where is your secret place of prayer?"),
        PlanDay("Ask, Seek, Knock", "Luke", 11, 5, 13, "Keep Asking", "Jesus encourages persistent prayer. Your Father loves to give good gifts to those who ask.", "Ask God boldly for something you need.", "What have you stopped asking God for?"),
        PlanDay("Hannah's Prayer", "1 Samuel", 1, 9, 18, "Pouring Out Your Soul", "Hannah poured out her soul to God in deep distress. God welcomes honest, tearful prayer.", "Pour out your heart honestly to God.", "What do you need to pour out before God?"),
        PlanDay("Solomon Asked for Wisdom", "1 Kings", 3, 5, 14, "An Understanding Heart", "Solomon could have asked for anything and asked for wisdom. God was pleased and gave him more besides.", "Ask God for wisdom.", "What would you ask God for if you could ask for anything?"),
        PlanDay("Elijah Prayed", "James", 5, 13, 18, "The Prayer of the Righteous", "Elijah was a person just like us, and his prayers changed things. Your prayers matter too.", "Pray with expectation today.", "Do you believe your prayers make a difference?"),
        PlanDay("Daniel's Habit", "Daniel", 6, 6, 11, "Three Times a Day", "Daniel had a rhythm of prayer he kept even under threat. Faithful habits carry us in hard times.", "Set a time to pray each day.", "What rhythm of prayer could you build?"),
        PlanDay("Nehemiah's Quick Prayer", "Nehemiah", 2, 1, 8, "Praying on the Spot", "Before answering the king, Nehemiah prayed silently. You can pray in the middle of any moment.", "Practise short prayers through your day.", "When could you pray quick prayers today?"),
        PlanDay("Praying Scripture", "Psalms", 51, 1, 12, "Create in Me", "David's prayer of confession shows us how to pray God's Word back to Him.", "Pray Psalm 51 in your own words.", "Which verse of this psalm can you pray today?"),
        PlanDay("Praise First", "Psalms", 145, 1, 13, "Great Is the Lord", "Praise shifts our focus from our problems to God's greatness.", "Spend time praising God before you ask anything.", "What about God can you praise Him for today?"),
        PlanDay("Thanksgiving", "Philippians", 4, 4, 7, "With Thanksgiving", "Thanksgiving is woven into Paul's call to prayer. Gratitude opens the way for peace.", "Thank God for ten specific things.", "How does thanksgiving change your prayers?"),
        PlanDay("Confession", "1 John", 1, 5, 10, "He Is Faithful to Forgive", "Confession clears the way. God is faithful and just to forgive when we are honest.", "Confess what you have been avoiding.", "What do you need to bring into the light?"),
        PlanDay("Intercession", "1 Timothy", 2, 1, 6, "Praying for All People", "God calls us to pray for everyone, including leaders and those in authority.", "Pray for three people by name.", "Who has God put on your heart to pray for?"),
        PlanDay("Jesus Prays for Us", "John", 17, 20, 26, "That They May Be One", "Jesus prayed for you before you were born. He still intercedes for you now.", "Thank Jesus for praying for you.", "How does it feel to know Jesus prays for you?"),
        PlanDay("Gethsemane", "Matthew", 26, 36, 46, "Not My Will", "In agony, Jesus prayed honestly and surrendered: not my will but Yours.", "Surrender a hard situation to God's will.", "Where do you need to pray 'not my will but Yours'?"),
        PlanDay("The Spirit Helps", "Romans", 8, 26, 28, "When Words Fail", "When you do not know what to pray, the Spirit prays for you with groanings too deep for words.", "Rest in the Spirit's help when words run out.", "What do you not know how to pray about?"),
        PlanDay("Praying Together", "Acts", 4, 23, 31, "They Raised Their Voices", "The early church prayed together, and the place was shaken. There is power in praying with others.", "Ask someone to pray with you this week.", "Who could you pray with regularly?"),
        PlanDay("Listening Prayer", "1 Samuel", 3, 1, 10, "Speak, Lord", "Samuel learned to say, speak, for your servant is listening. Prayer is also listening.", "Spend five minutes in silence before God.", "What might God be saying that you have not heard?"),
        PlanDay("Pray Without Ceasing", "1 Thessalonians", 5, 16, 18, "A Life of Prayer", "Praying continually means living aware of God all day, not just at set times.", "Talk to God throughout today.", "What would it look like to pray without ceasing?"),
        PlanDay("Call to Me", "Jeremiah", 33, 1, 9, "Great and Mighty Things", "God invites you to call on Him and promises to answer. Keep praying.", "Thank God for all He has taught you about prayer.", "How has your prayer life grown in these three weeks?"),
    ),
)

val READING_PLANS: List<ReadingPlanDef> = listOf(PLAN_0, PLAN_1, PLAN_2, PLAN_3, PLAN_4, PLAN_5, PLAN_6, PLAN_7)
