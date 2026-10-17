package com.prayerkey.manna.data

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import com.prayerkey.manna.model.DailyPrayer
import kotlinx.datetime.LocalDate

/*
 * A short prayer for each hour of the day - morning, afternoon, evening,
 * night and midnight - thirty-one of each, so a month goes by without one
 * repeating. Written for their hour: the evening prayer gives the day back,
 * the night prayer lets it go, the midnight prayer keeps watch.
 */

private val MORNING = listOf(
    DailyPrayer(ref = "Psalm 5:3", title = "Before the Day Begins", verse = "In the morning, Lord, you hear my voice.", prayer = "Lord, before the noise begins, I am here. You hear me first. Order my steps, guard my words, and let me carry You into every room today. Amen."),
    DailyPrayer(ref = "Lamentations 3:23", title = "New Mercy", verse = "His mercies are new every morning.", prayer = "Father, yesterday is finished and Your mercy is new. Let me begin clean, without dragging the old weight into this day. Amen."),
    DailyPrayer(ref = "Psalm 118:24", title = "This Is the Day", verse = "This is the day the Lord has made.", prayer = "Lord, this day is Yours before it is mine. Help me to be glad in it, even if it is ordinary. Amen."),
    DailyPrayer(ref = "Psalm 143:8", title = "Show Me the Way", verse = "Show me the way I should go.", prayer = "Father, I do not know everything this day holds. You do. Show me the way, and give me the grace to walk in it. Amen."),
    DailyPrayer(ref = "Isaiah 40:31", title = "Strength for Today", verse = "Those who hope in the Lord will renew their strength.", prayer = "Lord, I woke up tired. Renew my strength for what is in front of me today, one step at a time. Amen."),
    DailyPrayer(ref = "Matthew 6:33", title = "First Things First", verse = "Seek first the kingdom of God.", prayer = "Father, before the list, before the messages, I seek You. Put my day in its right order. Amen."),
    DailyPrayer(ref = "Psalm 141:3", title = "Guard My Mouth", verse = "Set a guard over my mouth, Lord.", prayer = "Lord, keep my words kind today. Let me build people up, and hold back what would wound them. Amen."),
    DailyPrayer(ref = "Psalm 31:15", title = "Into Your Hands", verse = "My times are in your hands.", prayer = "Father, I place today in Your hands: the meetings, the tasks, the people. Let nothing reach me that has not passed through You. Amen."),
    DailyPrayer(ref = "Ephesians 5:18", title = "Fill Me", verse = "Be filled with the Spirit.", prayer = "Holy Spirit, fill me this morning. Let what flows out of me today come from You and not from my mood. Amen."),
    DailyPrayer(ref = "James 1:5", title = "Wisdom for Today", verse = "If any of you lacks wisdom, ask God.", prayer = "Lord, I will face decisions today I cannot foresee. Give me wisdom when I need it, and humility to take it. Amen."),
    DailyPrayer(ref = "Psalm 119:105", title = "Light My Path", verse = "Your word is a lamp for my feet.", prayer = "Father, give me enough light for today's steps. I do not need to see the whole road, only the next part of it. Amen."),
    DailyPrayer(ref = "Psalm 51:12", title = "A Willing Heart", verse = "Grant me a willing spirit.", prayer = "Lord, make me willing today: to serve, to listen, to forgive, to obey when it costs me something. Amen."),
    DailyPrayer(ref = "Deuteronomy 31:8", title = "Walk With Me", verse = "The Lord himself goes before you.", prayer = "Father, You have already gone ahead into this day. Walk with me through every part of it. Amen."),
    DailyPrayer(ref = "John 14:27", title = "Be My Peace", verse = "Peace I leave with you.", prayer = "Jesus, before worry finds me, let Your peace find me first. Keep my heart steady whatever comes. Amen."),
    DailyPrayer(ref = "Nehemiah 8:10", title = "Give Me Joy", verse = "The joy of the Lord is your strength.", prayer = "Lord, let joy in You be my strength today, not the ease of the day or the approval of others. Amen."),
    DailyPrayer(ref = "Psalm 119:18", title = "Open My Eyes", verse = "Open my eyes that I may see.", prayer = "Father, open my eyes today to see You at work, and to see the people I would otherwise pass by. Amen."),
    DailyPrayer(ref = "Luke 16:10", title = "Faithful in Small Things", verse = "Whoever can be trusted with little.", prayer = "Lord, help me do the small, ordinary things of today faithfully and well, as if I were doing them for You. Amen."),
    DailyPrayer(ref = "Isaiah 41:10", title = "I Am Not Alone", verse = "Do not fear, for I am with you.", prayer = "Father, whatever I face today, I do not face it alone. Hold my right hand and steady me. Amen."),
    DailyPrayer(ref = "Genesis 12:2", title = "Make Me a Blessing", verse = "I will bless you, and you will be a blessing.", prayer = "Lord, bless me today so that I can be a blessing. Show me one person to encourage before the day ends. Amen."),
    DailyPrayer(ref = "Colossians 3:12", title = "Teach Me Patience", verse = "Clothe yourselves with patience.", prayer = "Father, dress me in patience today, especially with the person who tests it most. Amen."),
    DailyPrayer(ref = "Matthew 6:10", title = "Your Will Today", verse = "Your will be done.", prayer = "Lord, let Your will be done in my day, even where it differs from my plans. I trust Yours more. Amen."),
    DailyPrayer(ref = "Psalm 51:10", title = "Clean Heart", verse = "Create in me a clean heart.", prayer = "Father, begin this day by making my heart clean. Keep me from carrying yesterday's resentment into today. Amen."),
    DailyPrayer(ref = "Joshua 1:9", title = "Courage", verse = "Be strong and courageous.", prayer = "Lord, give me courage for the hard conversation, the difficult task, and the step I have been avoiding. Amen."),
    DailyPrayer(ref = "1 Samuel 3:10", title = "Speak, Lord", verse = "Speak, for your servant is listening.", prayer = "Father, speak to me today, and make me quiet enough to hear You above the noise. Amen."),
    DailyPrayer(ref = "Matthew 6:11", title = "Enough for Today", verse = "Give us today our daily bread.", prayer = "Lord, give me what I need for today, and keep me from worrying about what tomorrow needs. Amen."),
    DailyPrayer(ref = "Philippians 4:4", title = "Rejoice", verse = "Rejoice in the Lord always.", prayer = "Father, I choose to rejoice in You this morning, before I know how the day will go. Amen."),
    DailyPrayer(ref = "Psalm 121:8", title = "Protect My Home", verse = "The Lord will watch over your coming and going.", prayer = "Lord, watch over my family as we go out today. Keep us safe, and bring us home to one another. Amen."),
    DailyPrayer(ref = "Colossians 3:23", title = "Serve Well", verse = "Work at it with all your heart.", prayer = "Father, let my work today be honest and wholehearted, done for You and not for applause. Amen."),
    DailyPrayer(ref = "John 13:34", title = "Love Well", verse = "Love one another.", prayer = "Jesus, help me love the people in front of me today as You have loved me. Amen."),
    DailyPrayer(ref = "Proverbs 3:5", title = "Trust", verse = "Trust in the Lord with all your heart.", prayer = "Lord, I do not understand everything, and I do not have to. Today I will trust You with all of it. Amen."),
    DailyPrayer(ref = "Psalm 92:2", title = "Thank You for Morning", verse = "Proclaim your love in the morning.", prayer = "Father, thank You for waking me, for breath, and for another day to know You. Amen."),
)

private val AFTERNOON = listOf(
    DailyPrayer(ref = "Psalm 55:17", title = "A Pause at Midday", verse = "Evening, morning and noon I cry out.", prayer = "Lord, I stop in the middle of this day to remember You. Steady me for the hours that are left. Amen."),
    DailyPrayer(ref = "Isaiah 40:29", title = "Renewed Strength", verse = "He gives strength to the weary.", prayer = "Father, my energy is running low. Renew me for the rest of this day. Amen."),
    DailyPrayer(ref = "Psalm 139:23", title = "Reset My Heart", verse = "Search me, God, and know my heart.", prayer = "Lord, if this morning has soured my heart, reset it now. Give me a fresh start for the afternoon. Amen."),
    DailyPrayer(ref = "Galatians 5:22", title = "Patience Again", verse = "The fruit of the Spirit is patience.", prayer = "Father, my patience is thinner than it was this morning. Fill it up again, for the people still ahead of me today. Amen."),
    DailyPrayer(ref = "Psalm 16:8", title = "Right Where I Am", verse = "I keep my eyes always on the Lord.", prayer = "Lord, in the middle of the work, I turn my eyes to You. Keep me aware that You are near. Amen."),
    DailyPrayer(ref = "1 Peter 5:7", title = "Carry It for Me", verse = "Cast all your anxiety on him.", prayer = "Father, I hand You what has been building up since this morning. Carry it, so I can keep going. Amen."),
    DailyPrayer(ref = "Proverbs 2:6", title = "Wisdom for the Rest", verse = "The Lord gives wisdom.", prayer = "Lord, give me wisdom for the decisions still waiting this afternoon. Amen."),
    DailyPrayer(ref = "Proverbs 15:1", title = "Gentle Words", verse = "A gentle answer turns away wrath.", prayer = "Father, if I am tired and short with people, soften my words before they leave my mouth. Amen."),
    DailyPrayer(ref = "Galatians 6:9", title = "Finish Well", verse = "Let us not become weary in doing good.", prayer = "Lord, help me finish today's work well, not just finish it. Amen."),
    DailyPrayer(ref = "Matthew 28:20", title = "You Are Here", verse = "I am with you always.", prayer = "Jesus, You are with me in this afternoon as much as in the quiet of the morning. Thank You. Amen."),
    DailyPrayer(ref = "Psalm 46:10", title = "Breathe", verse = "Be still, and know that I am God.", prayer = "Father, for this moment I stop and breathe. You are God, and I do not have to hold everything together. Amen."),
    DailyPrayer(ref = "Colossians 3:2", title = "Focus", verse = "Set your minds on things above.", prayer = "Lord, my attention is scattered. Gather it and set it on what matters for the rest of today. Amen."),
    DailyPrayer(ref = "1 John 1:9", title = "Mercy for Mistakes", verse = "He is faithful to forgive.", prayer = "Father, I have already got things wrong today. Forgive me, and let me carry on without the weight of it. Amen."),
    DailyPrayer(ref = "Ephesians 4:32", title = "Kind to Someone", verse = "Be kind and compassionate.", prayer = "Lord, show me someone this afternoon who needs a kind word, and give me the words. Amen."),
    DailyPrayer(ref = "2 Corinthians 12:9", title = "Enough", verse = "My grace is sufficient for you.", prayer = "Father, I feel stretched thin. Your grace is enough for the rest of this day. Amen."),
    DailyPrayer(ref = "Psalm 121:7", title = "Protect the Road Home", verse = "The Lord will keep you from all harm.", prayer = "Lord, keep everyone I love safe on their way through today and on the road home. Amen."),
    DailyPrayer(ref = "1 Thessalonians 5:18", title = "Thankful at Noon", verse = "Give thanks in all circumstances.", prayer = "Father, halfway through this day, I stop to thank You for what has already gone right. Amen."),
    DailyPrayer(ref = "Philippians 4:8", title = "Guard My Mind", verse = "Whatever is true, think about such things.", prayer = "Lord, keep my thoughts clean and true this afternoon, and away from worry and comparison. Amen."),
    DailyPrayer(ref = "Isaiah 26:3", title = "Steady Me", verse = "You will keep in perfect peace.", prayer = "Father, the afternoon is busy and my heart is racing. Keep me in Your peace. Amen."),
    DailyPrayer(ref = "Psalm 90:17", title = "For My Work", verse = "Establish the work of our hands.", prayer = "Lord, establish the work of my hands today. Let something good come of it. Amen."),
    DailyPrayer(ref = "Joshua 1:9", title = "Courage to Continue", verse = "Be strong and courageous.", prayer = "Father, give me courage for what is still left undone today. Amen."),
    DailyPrayer(ref = "Luke 16:10", title = "Faithful in the Middle", verse = "Faithful with little.", prayer = "Lord, the middle of the day is where faithfulness is tested. Keep me faithful in the small things. Amen."),
    DailyPrayer(ref = "Matthew 11:28", title = "Rest in You", verse = "Come to me, all you who are weary.", prayer = "Jesus, I come to You for rest in the middle of the day. Refresh me. Amen."),
    DailyPrayer(ref = "1 Corinthians 13:4", title = "Love at Work", verse = "Love is patient, love is kind.", prayer = "Father, let love show up in how I treat the people I work beside this afternoon. Amen."),
    DailyPrayer(ref = "Philippians 2:3", title = "Humble Me", verse = "In humility value others above yourselves.", prayer = "Lord, keep me humble this afternoon, quick to listen and slow to prove myself right. Amen."),
    DailyPrayer(ref = "Psalm 27:14", title = "Wait on You", verse = "Wait for the Lord.", prayer = "Father, some of what I hoped for today has not come. Help me wait on You without anxiety. Amen."),
    DailyPrayer(ref = "John 8:12", title = "Light in the Afternoon", verse = "I am the light of the world.", prayer = "Jesus, if the day has grown heavy, be my light through the rest of it. Amen."),
    DailyPrayer(ref = "Psalm 91:11", title = "Hold My Family", verse = "He will command his angels concerning you.", prayer = "Lord, wherever my family is this afternoon, watch over them and keep them. Amen."),
    DailyPrayer(ref = "Psalm 16:11", title = "Joy Again", verse = "You fill me with joy in your presence.", prayer = "Father, restore my joy for the rest of the day. Let it come from You and not from how things are going. Amen."),
    DailyPrayer(ref = "John 14:15", title = "Obey Now", verse = "If you love me, keep my commands.", prayer = "Lord, if there is something You asked of me this morning that I have not done, give me the will to do it now. Amen."),
    DailyPrayer(ref = "Psalm 100:3", title = "Still Yours", verse = "We are his.", prayer = "Father, however this day is going, I am still Yours. Let that steady me until evening. Amen."),
)

private val EVENING = listOf(
    DailyPrayer(ref = "Psalm 141:2", title = "As the Day Closes", verse = "May my prayer be set before you like incense.", prayer = "Lord, as this day closes, I bring it back to You: what went well, what did not, and everything in between. Amen."),
    DailyPrayer(ref = "Psalm 92:1", title = "Thank You for Today", verse = "It is good to praise the Lord.", prayer = "Father, thank You for carrying me through today. I did not do it alone. Amen."),
    DailyPrayer(ref = "1 John 1:9", title = "What I Got Wrong", verse = "He is faithful and just to forgive.", prayer = "Lord, forgive what I got wrong today. Let me not carry it into tomorrow. Amen."),
    DailyPrayer(ref = "Psalm 34:8", title = "Where I Saw You", verse = "Taste and see that the Lord is good.", prayer = "Father, open my eyes to where You were today, in the ordinary moments I nearly missed. Amen."),
    DailyPrayer(ref = "Psalm 55:22", title = "Release the Day", verse = "Cast your cares on the Lord.", prayer = "Lord, I release this day into Your hands. What is unfinished can wait until morning. Amen."),
    DailyPrayer(ref = "Psalm 127:1", title = "Home", verse = "Unless the Lord builds the house.", prayer = "Father, bless this home tonight. Let peace settle over everyone under this roof. Amen."),
    DailyPrayer(ref = "Ephesians 4:32", title = "Forgive Them Too", verse = "Forgiving each other.", prayer = "Lord, if someone hurt me today, help me forgive them before I sleep. Amen."),
    DailyPrayer(ref = "John 14:27", title = "Peace Tonight", verse = "Do not let your hearts be troubled.", prayer = "Jesus, quiet my heart this evening. Let Your peace replace the noise of the day. Amen."),
    DailyPrayer(ref = "Lamentations 3:22", title = "Faithful Again", verse = "His compassions never fail.", prayer = "Father, You were faithful again today. Thank You that Your compassion did not run out. Amen."),
    DailyPrayer(ref = "Matthew 11:28", title = "Rest for the Weary", verse = "I will give you rest.", prayer = "Jesus, I am tired. Give me rest tonight, in body and in soul. Amen."),
    DailyPrayer(ref = "Psalm 113:3", title = "Evening Praise", verse = "From the rising of the sun to its going down.", prayer = "Lord, from morning to now, You are worthy of praise. I end the day by praising You. Amen."),
    DailyPrayer(ref = "Philippians 4:8", title = "The Good in Today", verse = "Whatever is lovely, think about such things.", prayer = "Father, help me remember the good things from today, not only the hard ones. Amen."),
    DailyPrayer(ref = "Matthew 6:34", title = "Tomorrow Is Yours", verse = "Do not worry about tomorrow.", prayer = "Lord, tomorrow belongs to You. I will not borrow its worries tonight. Amen."),
    DailyPrayer(ref = "Psalm 128:3", title = "For My Family", verse = "Your children will be like olive shoots.", prayer = "Father, bless my family tonight. Heal what is strained, and draw us closer to You. Amen."),
    DailyPrayer(ref = "Psalm 147:3", title = "Heal the Hurt", verse = "He heals the brokenhearted.", prayer = "Lord, if something wounded me today, begin to heal it tonight. Amen."),
    DailyPrayer(ref = "Psalm 90:12", title = "Lessons Learned", verse = "Teach us to number our days.", prayer = "Father, teach me something from today, so that tomorrow I live a little wiser. Amen."),
    DailyPrayer(ref = "Psalm 86:11", title = "Undivided", verse = "Give me an undivided heart.", prayer = "Lord, I was pulled in many directions today. Gather my heart back to You tonight. Amen."),
    DailyPrayer(ref = "Psalm 139:12", title = "Light in Darkness", verse = "Darkness is as light to you.", prayer = "Father, as the light fades, remind me that darkness is not dark to You. Amen."),
    DailyPrayer(ref = "Psalm 131:2", title = "A Quiet Heart", verse = "I have calmed and quieted myself.", prayer = "Lord, quiet my heart like a child at rest. I do not need to solve everything tonight. Amen."),
    DailyPrayer(ref = "Psalm 139:24", title = "Evening Confession", verse = "Lead me in the way everlasting.", prayer = "Father, show me anything in me today that grieved You, and lead me back to Your way. Amen."),
    DailyPrayer(ref = "Philippians 1:3", title = "Thank You for People", verse = "I thank my God every time I remember you.", prayer = "Lord, thank You for the people I shared today with. Bless each of them tonight. Amen."),
    DailyPrayer(ref = "2 Corinthians 9:8", title = "Enough Was Enough", verse = "God is able to bless you abundantly.", prayer = "Father, You gave me enough for today. Thank You for Your provision. Amen."),
    DailyPrayer(ref = "Hebrews 12:1", title = "Lay It Down", verse = "Throw off everything that hinders.", prayer = "Lord, I lay down what I have been carrying all day. I will not take it to bed. Amen."),
    DailyPrayer(ref = "Psalm 91:1", title = "Protection Tonight", verse = "Rest in the shadow of the Almighty.", prayer = "Father, cover my home and my family tonight. Let us rest under Your shadow. Amen."),
    DailyPrayer(ref = "Colossians 3:15", title = "Grateful Heart", verse = "Be thankful.", prayer = "Lord, end my day with gratitude instead of complaint. Thank You for today. Amen."),
    DailyPrayer(ref = "Isaiah 41:10", title = "Still Held", verse = "I will uphold you.", prayer = "Father, whatever today was, You held me through it. Thank You. Amen."),
    DailyPrayer(ref = "Ezekiel 36:26", title = "Soft Heart", verse = "A heart of flesh.", prayer = "Lord, if today hardened me, soften my heart again before I sleep. Amen."),
    DailyPrayer(ref = "Psalm 16:11", title = "Your Presence", verse = "In your presence is fullness of joy.", prayer = "Father, let me end this day aware of Your presence, and rest in it. Amen."),
    DailyPrayer(ref = "Colossians 3:16", title = "Word to Carry", verse = "Let the word of Christ dwell in you.", prayer = "Lord, let one word from You stay with me through the night. Amen."),
    DailyPrayer(ref = "Romans 12:1", title = "Evening Offering", verse = "A living sacrifice.", prayer = "Father, I offer You today, all of it, as imperfect as it was. Receive it. Amen."),
    DailyPrayer(ref = "Psalm 4:8", title = "Good Night, Father", verse = "In peace I will lie down.", prayer = "Lord, I end this day with You. Watch over the night, and meet me in the morning. Amen."),
)

private val NIGHT = listOf(
    DailyPrayer(ref = "Psalm 4:8", title = "Before I Sleep", verse = "In peace I will lie down and sleep.", prayer = "Lord, I lie down in peace tonight, because You keep watch. Let me rest. Amen."),
    DailyPrayer(ref = "Psalm 121:4", title = "He Does Not Sleep", verse = "He who watches over you will not slumber.", prayer = "Father, You do not sleep, so I can. Watch over my home tonight. Amen."),
    DailyPrayer(ref = "1 Peter 5:7", title = "Hand It Over", verse = "Cast all your anxiety on him.", prayer = "Lord, before I sleep, I give You the worry I would otherwise take to bed. Amen."),
    DailyPrayer(ref = "Isaiah 26:3", title = "Quiet My Mind", verse = "Perfect peace.", prayer = "Father, my mind keeps running. Quiet it, and give me perfect peace tonight. Amen."),
    DailyPrayer(ref = "Proverbs 3:24", title = "Sweet Sleep", verse = "Your sleep will be sweet.", prayer = "Lord, give me sweet sleep tonight, and let me wake refreshed. Amen."),
    DailyPrayer(ref = "Psalm 91:4", title = "Safe in You", verse = "He will cover you with his feathers.", prayer = "Father, cover me tonight. I am safe under Your wings. Amen."),
    DailyPrayer(ref = "Matthew 11:28", title = "Rest for My Body", verse = "I will give you rest.", prayer = "Jesus, my body is tired and my mind is full. Give me real rest tonight. Amen."),
    DailyPrayer(ref = "Psalm 91:5", title = "No Fear Tonight", verse = "You will not fear the terror of night.", prayer = "Lord, keep fear away from me tonight. You are with me in the dark. Amen."),
    DailyPrayer(ref = "Job 33:15", title = "Dreams in Your Hands", verse = "In a dream, in a vision of the night.", prayer = "Father, guard my dreams tonight, and speak to me if You choose. Amen."),
    DailyPrayer(ref = "Matthew 6:34", title = "Tomorrow Can Wait", verse = "Tomorrow will worry about itself.", prayer = "Lord, tomorrow can wait until tomorrow. Tonight I rest in You. Amen."),
    DailyPrayer(ref = "Psalm 103:12", title = "Forgiven Tonight", verse = "As far as the east is from the west.", prayer = "Father, I go to sleep forgiven. Thank You that nothing stands between us tonight. Amen."),
    DailyPrayer(ref = "Psalm 127:2", title = "Guard My Family", verse = "He grants sleep to those he loves.", prayer = "Lord, give sleep to everyone I love tonight, and guard them while they rest. Amen."),
    DailyPrayer(ref = "Psalm 46:10", title = "Stillness", verse = "Be still, and know that I am God.", prayer = "Father, I am still now. You are God, and I can let go of everything. Amen."),
    DailyPrayer(ref = "Psalm 139:11", title = "Held in the Dark", verse = "Darkness will not be dark to you.", prayer = "Lord, the dark is not dark to You. Hold me through the night. Amen."),
    DailyPrayer(ref = "Psalm 30:5", title = "Heavy Heart", verse = "Joy comes in the morning.", prayer = "Father, my heart is heavy tonight. Hold it until the morning brings joy. Amen."),
    DailyPrayer(ref = "Psalm 3:5", title = "Thank You for Rest", verse = "I wake again, because the Lord sustains me.", prayer = "Lord, thank You for the gift of rest. Sustain me through the night. Amen."),
    DailyPrayer(ref = "Isaiah 32:18", title = "Lie Down in Peace", verse = "My people will live in peaceful dwelling places.", prayer = "Father, let peace fill this room and this home as we sleep. Amen."),
    DailyPrayer(ref = "Philippians 3:13", title = "Let It Go", verse = "Forgetting what is behind.", prayer = "Lord, I let go of today. Whatever was left undone, I leave with You. Amen."),
    DailyPrayer(ref = "Numbers 6:24", title = "Watch Over Me", verse = "The Lord bless you and keep you.", prayer = "Father, bless me and keep me tonight. Make Your face shine on me. Amen."),
    DailyPrayer(ref = "Mark 10:16", title = "The Children", verse = "He took the children in his arms.", prayer = "Lord, hold the children I love tonight. Give them safe and peaceful sleep. Amen."),
    DailyPrayer(ref = "Psalm 63:6", title = "For the Sleepless", verse = "On my bed I remember you.", prayer = "Father, if I wake in the night, let my mind turn to You, not to worry. Amen."),
    DailyPrayer(ref = "Psalm 62:1", title = "Only You", verse = "My soul finds rest in God alone.", prayer = "Lord, my soul finds rest in You alone. Let me rest there tonight. Amen."),
    DailyPrayer(ref = "2 Corinthians 12:9", title = "Enough Today", verse = "My grace is sufficient.", prayer = "Father, today was enough. Your grace carried me, and it will carry me through the night. Amen."),
    DailyPrayer(ref = "Psalm 16:7", title = "Night Watch", verse = "Even at night my heart instructs me.", prayer = "Lord, teach me even in the night, and keep my heart close to You. Amen."),
    DailyPrayer(ref = "Psalm 4:8", title = "Home Protected", verse = "You alone make me dwell in safety.", prayer = "Father, You alone keep us safe. Protect every door and every heart in this home. Amen."),
    DailyPrayer(ref = "Zephaniah 3:17", title = "Tired and Loved", verse = "He will quiet you with his love.", prayer = "Lord, quiet me with Your love tonight. I am tired, and I am loved. Amen."),
    DailyPrayer(ref = "Isaiah 40:31", title = "Tomorrow's Strength", verse = "They will renew their strength.", prayer = "Father, renew my strength while I sleep, for whatever tomorrow asks. Amen."),
    DailyPrayer(ref = "Hebrews 4:9", title = "Rest from Striving", verse = "There remains a Sabbath-rest.", prayer = "Lord, let me stop striving tonight and rest in what You have already done. Amen."),
    DailyPrayer(ref = "Mark 4:39", title = "Peace, Be Still", verse = "Peace, be still.", prayer = "Jesus, speak peace over the storm in my mind, as You spoke to the sea. Amen."),
    DailyPrayer(ref = "Psalm 134:1", title = "Good Night", verse = "Praise the Lord, all you servants who minister by night.", prayer = "Father, I end my day praising You. Keep me through the night. Amen."),
    DailyPrayer(ref = "Luke 23:46", title = "Into Your Hands", verse = "Into your hands I commit my spirit.", prayer = "Lord, into Your hands I commit my spirit tonight. Amen."),
)

private val MIDNIGHT = listOf(
    DailyPrayer(ref = "Acts 16:25", title = "The Midnight Hour", verse = "About midnight Paul and Silas were praying.", prayer = "Lord, at midnight Paul and Silas prayed and sang. In this hour, I pray and praise You too. Amen."),
    DailyPrayer(ref = "Psalm 119:62", title = "Awake With You", verse = "At midnight I rise to give you thanks.", prayer = "Father, I am awake at this hour, and I choose to spend it with You. Thank You. Amen."),
    DailyPrayer(ref = "Psalm 63:6", title = "When I Cannot Sleep", verse = "I think of you through the watches of the night.", prayer = "Lord, sleep has not come. Let my thoughts turn to You instead of to worry. Amen."),
    DailyPrayer(ref = "Acts 16:26", title = "Breakthrough", verse = "Suddenly there was a great earthquake.", prayer = "Father, as the prison doors opened at midnight, open what is shut in my life. Amen."),
    DailyPrayer(ref = "Matthew 26:41", title = "Watch and Pray", verse = "Watch and pray.", prayer = "Jesus, You prayed in the night in Gethsemane. Teach me to watch and pray in mine. Amen."),
    DailyPrayer(ref = "Psalm 34:4", title = "Deliver Me", verse = "He delivered me from all my fears.", prayer = "Lord, deliver me from the fears that grow bigger in the dark. Amen."),
    DailyPrayer(ref = "Psalm 91:5", title = "Stand Guard", verse = "The terror of night.", prayer = "Father, stand guard over my home and my mind tonight. I will not fear. Amen."),
    DailyPrayer(ref = "1 Timothy 2:1", title = "For Someone Else", verse = "Intercession for all people.", prayer = "Lord, in this quiet hour I pray for someone who is struggling tonight. Hold them. Amen."),
    DailyPrayer(ref = "Psalm 130:1", title = "Cry Out", verse = "Out of the depths I cry to you.", prayer = "Father, from the depths of this night, I cry to You. Hear me. Amen."),
    DailyPrayer(ref = "Isaiah 60:1", title = "Light Rising", verse = "Arise, shine, for your light has come.", prayer = "Lord, the night will not last. Let Your light rise over what feels dark. Amen."),
    DailyPrayer(ref = "Genesis 32:26", title = "Wrestling", verse = "I will not let you go unless you bless me.", prayer = "Father, like Jacob in the night, I will not let go until You bless me. Amen."),
    DailyPrayer(ref = "Job 35:10", title = "Songs in the Night", verse = "God my Maker, who gives songs in the night.", prayer = "Lord, give me a song in this night, and turn my worry into worship. Amen."),
    DailyPrayer(ref = "Psalm 130:6", title = "Waiting for Morning", verse = "More than watchmen wait for the morning.", prayer = "Father, I wait for You more than the watchman waits for dawn. Amen."),
    DailyPrayer(ref = "2 Corinthians 12:10", title = "Strength in Weakness", verse = "When I am weak, then I am strong.", prayer = "Lord, I feel weak at this hour. Be my strength. Amen."),
    DailyPrayer(ref = "Ephesians 6:12", title = "Spiritual Battle", verse = "Our struggle is not against flesh and blood.", prayer = "Father, I stand against every darkness that wars against my peace. You have already won. Amen."),
    DailyPrayer(ref = "Isaiah 54:13", title = "Guard My Children", verse = "All your children will be taught by the Lord.", prayer = "Lord, guard my children through this night, in body, mind and spirit. Amen."),
    DailyPrayer(ref = "Galatians 6:2", title = "The Burden I Carry", verse = "Carry each other's burdens.", prayer = "Father, I bring You the burden that keeps me awake. Carry it for me. Amen."),
    DailyPrayer(ref = "Hebrews 11:1", title = "Faith in the Dark", verse = "Assurance about what we do not see.", prayer = "Lord, I cannot see how this will turn out, but I trust You in the dark. Amen."),
    DailyPrayer(ref = "1 Kings 19:12", title = "Holy Quiet", verse = "A gentle whisper.", prayer = "Father, in this silent hour, let me hear Your gentle whisper. Amen."),
    DailyPrayer(ref = "John 8:36", title = "Freedom", verse = "If the Son sets you free.", prayer = "Jesus, set me free from what binds me. Let this night be the turning point. Amen."),
    DailyPrayer(ref = "Philippians 4:7", title = "Peace That Guards", verse = "The peace of God will guard your hearts.", prayer = "Lord, let Your peace stand guard over my heart and mind through this night. Amen."),
    DailyPrayer(ref = "Jeremiah 17:14", title = "Healing Tonight", verse = "Heal me, Lord, and I will be healed.", prayer = "Father, heal what is hurting in me or in someone I love, even tonight. Amen."),
    DailyPrayer(ref = "Habakkuk 3:18", title = "Praise Anyway", verse = "Yet I will rejoice in the Lord.", prayer = "Lord, even now, even here, I will rejoice in You. Amen."),
    DailyPrayer(ref = "Daniel 10:12", title = "The Answer Is Coming", verse = "Your words were heard.", prayer = "Father, my words have been heard. I will keep praying until the answer comes. Amen."),
    DailyPrayer(ref = "Psalm 139:12", title = "Nothing Hidden", verse = "The night will shine like the day.", prayer = "Lord, nothing is hidden from You tonight. See what I am facing, and act. Amen."),
    DailyPrayer(ref = "Psalm 116:7", title = "Rest Returns", verse = "Return to your rest, my soul.", prayer = "Father, let my soul return to rest now. I have given it all to You. Amen."),
    DailyPrayer(ref = "Psalm 23:4", title = "Shepherd in the Night", verse = "I will fear no evil.", prayer = "Shepherd, walk with me through this dark hour. I will fear no evil, for You are with me. Amen."),
    DailyPrayer(ref = "Psalm 67:1", title = "For the Nations", verse = "May God be gracious to us.", prayer = "Lord, while much of the world sleeps, be gracious to my city and my nation. Amen."),
    DailyPrayer(ref = "Psalm 5:11", title = "Covered", verse = "Spread your protection over them.", prayer = "Father, spread Your protection over everyone I love tonight. Amen."),
    DailyPrayer(ref = "Romans 13:12", title = "The Morning Is Near", verse = "The night is nearly over; the day is almost here.", prayer = "Lord, the night is nearly over. Let me rise with hope in the morning. Amen."),
    DailyPrayer(ref = "Jude 1:24", title = "Keep Me", verse = "Him who is able to keep you from stumbling.", prayer = "Father, You are able to keep me. Keep me tonight, and always. Amen."),
)

/** Today's prayer for this hour of the day: "morning", "afternoon", "evening", "night" or "midnight". */
fun hourPrayer(hour: String, date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): DailyPrayer {
    val list = when (hour) {
        "afternoon" -> AFTERNOON
        "evening" -> EVENING
        "night" -> NIGHT
        "midnight" -> MIDNIGHT
        else -> MORNING
    }
    return list[(date.dayOfMonth - 1) % list.size]
}
