package com.prayerkey.manna.model

import java.time.LocalDate

/**
 * Prayer of the Day — the same 31 daily prayers as prayerkey.com,
 * rotating by day-of-year. Bundled offline: instant, free, forever.
 */
data class DailyPrayer(val ref: String, val title: String, val verse: String, val prayer: String)

fun todaysPrayer(): DailyPrayer {
    val day = LocalDate.now().dayOfYear
    return DailyPrayersPool[day % DailyPrayersPool.size]
}

val DailyPrayersPool = listOf(
    DailyPrayer(
        ref = "John 3:16",
        title = "Prayer of God's Love",
        verse = "For God so loved the world that he gave his one and only Son.",
        prayer = "Heavenly Father,\n\nThank You for a love so vast that You gave Your only Son for me — not because I deserved it, but because You are love itself. Let the reality of the cross fill every corner of my heart today.\n\nWhen I feel forgotten or unloved, remind me that I am fully known and fully chosen by You. Let Your love be the foundation on which I stand, the voice that silences every lie, and the fire that keeps my faith burning.\n\nTeach me to love others with even a fraction of the love You have shown me.\n\nIn Jesus' name, Amen.",
    ),
    DailyPrayer(
        ref = "Psalm 23:1",
        title = "Prayer of Trust & Provision",
        verse = "The Lord is my shepherd, I lack nothing.",
        prayer = "Good Shepherd,\n\nYou know every need before I speak it. You see every worry I carry into this day. I come to You now with open hands, trusting You to provide exactly what I need — not always what I want, but always what is best.\n\nLead me beside still waters. Restore my soul when it grows weary. Even when I walk through dark valleys, help me feel Your rod and staff, Your comfort and guidance, at every step.\n\nI will not fear. You are with me.\n\nIn Your holy name, Amen.",
    ),
    DailyPrayer(
        ref = "Philippians 4:13",
        title = "Prayer for Strength",
        verse = "I can do all this through him who gives me strength.",
        prayer = "Lord Jesus,\n\nThe challenges before me today feel larger than my ability to face them. My own strength runs out quickly. But You are the One who strengthens me — not with a spirit of striving, but with a deep, settled power that comes from knowing You.\n\nFill me afresh. Where I am weak, be strong. Where I am afraid, give me courage. Where I am tempted to quit, remind me that I can do all things through You.\n\nLet every step I take today be taken in Your strength, not my own.\n\nAmen.",
    ),
    DailyPrayer(
        ref = "Romans 8:28",
        title = "Prayer of Surrender",
        verse = "In all things God works for the good of those who love him.",
        prayer = "Father,\n\nThere are things in my life right now that I do not understand. Circumstances that feel broken, plans that have fallen apart, prayers that seem unanswered. My heart struggles to see any good in them.\n\nBut Your Word declares that in all things — not some things, not the easy things, but all things — You are working for my good. I choose to believe that today, even when I cannot see it.\n\nI surrender what I cannot control. I release the outcome into Your hands. Work all things together according to Your perfect purpose.\n\nIn Jesus' name, Amen.",
    ),
    DailyPrayer(
        ref = "Isaiah 40:31",
        title = "Prayer for Renewal",
        verse = "Those who hope in the Lord will renew their strength.",
        prayer = "Lord,\n\nI am tired. Not just physically, but in my soul. The weight of this season has worn me down in ways I cannot always explain. I come to You not with energy, but with emptiness — trusting that You fill what is empty.\n\nRenew my strength like the eagle's. Let me rise above the heaviness that has settled on me. Restore my vision, my hope, my desire to press forward into all You have called me to.\n\nI wait on You. I put my hope in You alone. Carry me when I cannot run.\n\nIn Your name, Amen.",
    ),
    DailyPrayer(
        ref = "Jeremiah 29:11",
        title = "Prayer Over the Future",
        verse = "I know the plans I have for you, declares the Lord — plans to prosper you.",
        prayer = "Father,\n\nThe future feels uncertain. I have questions without answers and dreams that seem impossibly far away. The gap between where I am and where I want to be is wide, and sometimes I wonder if I am on the right path at all.\n\nRemind me today that You hold a plan for my life — a plan not to harm me but to give me hope and a future. You see the end from the beginning. You are not surprised by anything I am walking through.\n\nI trust Your timing. I release my anxiety about tomorrow. Lead me forward, one step at a time.\n\nIn Jesus' name, Amen.",
    ),
    DailyPrayer(
        ref = "Proverbs 3:5–6",
        title = "Prayer for Direction",
        verse = "Trust in the Lord with all your heart and lean not on your own understanding.",
        prayer = "Father,\n\nI have been leaning on my own understanding, and it has led me in circles. My wisdom is limited. My perspective is partial. I see today but You see eternity.\n\nSo I come to You now with every decision I am facing. I lay them before You — the big ones and the small ones, the urgent and the distant. I choose to trust You with all of my heart, even the parts I have kept closed.\n\nMake my paths straight. Remove confusion and replace it with clarity. Direct my steps in the way of peace.\n\nIn Your name, Amen.",
    ),
    DailyPrayer(
        ref = "Matthew 6:33",
        title = "Prayer of First Things",
        verse = "Seek first his kingdom and his righteousness.",
        prayer = "Lord,\n\nI confess that I have let lesser things take first place — worry, ambition, comfort, distraction. My attention has been scattered and my heart divided. Forgive me for chasing things that do not satisfy.\n\nReorder my priorities today. Let Your kingdom be the first thing I pursue in the morning and the last thing I think about at night. Strip away every distraction that pulls me away from Your presence.\n\nAs I seek You first, I trust that all other things will be added according to Your grace and wisdom.\n\nIn Jesus' name, Amen.",
    ),
    DailyPrayer(
        ref = "Psalm 46:1",
        title = "Prayer for Protection & Peace",
        verse = "God is our refuge and strength, an ever-present help in trouble.",
        prayer = "Heavenly Father,\n\nCover me and my family in Your divine protection today. Block every weapon formed against us and silence every voice that speaks harm. Fill our hearts with peace where there is fear, strength where there is weakness, and joy where there is heaviness.\n\nSurround us with Your light and guide our steps toward Your purpose. Let everything and everyone sent to distract, drain, or destroy be removed by Your hand. Replace it all with love, clarity, and blessings that overflow.\n\nYou are our refuge and we run to You now.\n\nIn Jesus' name, Amen.",
    ),
    DailyPrayer(
        ref = "Romans 12:2",
        title = "Prayer for a Renewed Mind",
        verse = "Be transformed by the renewing of your mind.",
        prayer = "Lord,\n\nMy mind is a battlefield. Negative thoughts, old patterns, fears and doubts — they crowd in and try to shape how I see myself, others, and You. I am tired of living according to the patterns of this world.\n\nRenew my mind with Your Word today. Let Scripture replace every lie. Let truth displace every fear. Transform the way I think until my thoughts are aligned with Yours — full of faith, hope, and love.\n\nI present myself to You as a living offering. Make me new from the inside out.\n\nIn Jesus' name, Amen.",
    ),
    DailyPrayer(
        ref = "2 Timothy 1:7",
        title = "Prayer Against Fear",
        verse = "God has not given us a spirit of fear, but of power, love and a sound mind.",
        prayer = "Father,\n\nFear has been speaking loudly in my life. It whispers that I am not enough, that things will fall apart, that the worst will happen. It has kept me small and paralyzed when You have called me to walk in freedom.\n\nI declare today that fear does not come from You. You have given me power — the same power that raised Christ from the dead. You have given me love — perfect love that drives out fear. You have given me a sound mind — steady, clear, and anchored in truth.\n\nI take authority over every fearful thought in Jesus' name. I will not shrink back.\n\nAmen.",
    ),
    DailyPrayer(
        ref = "Hebrews 11:1",
        title = "Prayer for Greater Faith",
        verse = "Now faith is confidence in what we hope for and assurance about what we do not see.",
        prayer = "Lord,\n\nI believe — but help my unbelief. There are areas of my life where faith has grown thin, where hope has flickered, where what You have promised seems too far away to be real. I am honest about my doubts.\n\nBut I choose to stand on Your Word anyway. I choose to trust what I cannot yet see. I choose to act as though Your promises are already yes and amen, because they are.\n\nGrow my faith. Let it be a faith that moves mountains, endures storms, and holds fast even when everything around me shifts.\n\nIn Jesus' name, Amen.",
    ),
    DailyPrayer(
        ref = "Matthew 11:28",
        title = "Prayer of Rest",
        verse = "Come to me, all you who are weary and burdened, and I will give you rest.",
        prayer = "Jesus,\n\nI am tired. I have been carrying things that were never mine to carry — worry about tomorrow, regret about yesterday, the weight of expectations I can never fully meet. I have been striving when You invited me to rest.\n\nSo I come to You now, just as I am — worn down, hands open, heart willing. I lay every burden at Your feet. Take the heaviness from my shoulders. Exchange it for Your yoke, which is easy, and Your burden, which is light.\n\nTeach me what it means to truly rest in You — not just physically, but in my soul.\n\nAmen.",
    ),
    DailyPrayer(
        ref = "Psalm 119:105",
        title = "Prayer for Wisdom",
        verse = "Your word is a lamp for my feet, a light on my path.",
        prayer = "Father,\n\nI cannot see what is ahead of me. The path forward is not always clear, and I have taken enough wrong turns to know that my instincts alone are not enough. I need Your light.\n\nIlluminate my way through Your Word. Speak to me through Scripture today — not just in general, but specifically, personally, clearly. Let the Bible be more than ink on a page; let it be a living lamp that guides every decision I make.\n\nWhere I am confused, give clarity. Where I am hesitant, give confidence. Let me walk in Your light today.\n\nIn Jesus' name, Amen.",
    ),
    DailyPrayer(
        ref = "Joshua 1:9",
        title = "Prayer for Courage",
        verse = "Be strong and courageous. Do not be afraid; do not be discouraged.",
        prayer = "Lord,\n\nYou commanded Joshua to be strong and courageous — not because his enemies were small, but because You were with him. I receive that same command today. My battles may be real, but You are greater than all of them.\n\nGive me the courage to step into the assignments You have placed before me. Help me to speak when silence would be easier, to move forward when fear says stay, to trust when everything around me is uncertain.\n\nYou go before me. You are behind me. You are beside me. With You, I am never alone.\n\nIn Your mighty name, Amen.",
    ),
    DailyPrayer(
        ref = "Psalm 37:4",
        title = "Prayer of Delight",
        verse = "Take delight in the Lord, and he will give you the desires of your heart.",
        prayer = "Father,\n\nTeach me to delight in You — not in what You can give me, but in who You are. Let my greatest joy be found in Your presence, my deepest satisfaction in knowing You, my highest treasure in relationship with You.\n\nAs I seek You with a whole heart, align the desires of my heart with Yours. Purify what I want until what I want is what You want. Give me holy ambitions, eternal longings, desires that bring glory to Your name.\n\nLet every good thing in my life be received as a gift from Your hand.\n\nAmen.",
    ),
    DailyPrayer(
        ref = "Isaiah 41:10",
        title = "Prayer When Afraid",
        verse = "Do not fear, for I am with you; do not be dismayed, for I am your God.",
        prayer = "God,\n\nI will not lie — I am afraid. The situation I am facing feels overwhelming and the outcome is uncertain. My heart is anxious and my thoughts are running ahead to the worst possible endings.\n\nBut You speak directly into this moment: Do not fear, for I am with you. I hold onto that promise now. You are my God. You strengthen me. You uphold me with Your righteous right hand. Nothing that comes against me is beyond Your ability to handle.\n\nSteady my heart. Calm my mind. Let Your peace, which passes all understanding, guard me today.\n\nIn Jesus' name, Amen.",
    ),
    DailyPrayer(
        ref = "Psalm 34:18",
        title = "Prayer for the Brokenhearted",
        verse = "The Lord is close to the brokenhearted and saves those who are crushed in spirit.",
        prayer = "Father,\n\nSome hearts are broken today. Broken by loss, by betrayal, by disappointment, by grief. The pain is real and it is deep, and sometimes it feels like no one truly understands.\n\nBut You are close to the brokenhearted. You do not stand at a distance from our pain — You come near. You see every tear. You understand every ache. You are moved by our suffering.\n\nCome close right now to everyone reading this who is hurting. Bind up the wounds. Breathe life into crushed spirits. Remind us that weeping may endure for a night, but joy comes in the morning.\n\nIn Jesus' name, Amen.",
    ),
    DailyPrayer(
        ref = "Romans 5:8",
        title = "Prayer of Gratitude for Grace",
        verse = "God demonstrates his own love for us in this: While we were still sinners, Christ died for us.",
        prayer = "Lord Jesus,\n\nYou did not wait for me to get it together before You loved me. You loved me at my lowest, my messiest, my most broken. You went to the cross for me not because I was worthy, but because You are grace.\n\nI am undone by that love today. Let it reshape how I see myself — not as someone who must earn Your favour, but as someone who is already fully accepted because of what You did.\n\nLet the cross be my daily meditation. Let grace be my constant declaration. Let love be what defines everything I do.\n\nThank You, Jesus. Amen.",
    ),
    DailyPrayer(
        ref = "John 14:6",
        title = "Prayer of Commitment to Christ",
        verse = "Jesus answered, I am the way and the truth and the life.",
        prayer = "Lord Jesus,\n\nIn a world full of voices claiming to have the answers, I come back to You. You alone are the way — I will not follow any other path. You alone are the truth — I will not be moved by lies. You alone are the life — apart from You I have nothing of eternal value.\n\nI recommit myself to You today. Let my choices reflect that You are Lord. Let my words declare that You are truth. Let my life be evidence that You are alive and at work in the world.\n\nGuide my steps. Correct my course when I wander. I am Yours.\n\nAmen.",
    ),
    DailyPrayer(
        ref = "Psalm 27:1",
        title = "Prayer of Confidence in God",
        verse = "The Lord is my light and my salvation — whom shall I fear?",
        prayer = "Lord,\n\nYou are my light in every dark season. When confusion closes in, You illuminate the way. When shadows fall, You are brighter than all of them. I will not fear darkness because You are in me.\n\nYou are my salvation — not just for eternity, but for today. You save me from despair, from the enemy, from my own worst impulses. There is nothing and no one I need to fear because You stand between me and everything that threatens me.\n\nI walk into this day with confidence — not in myself, but in You.\n\nIn Your name, Amen.",
    ),
    DailyPrayer(
        ref = "Lamentations 3:22–23",
        title = "Morning Prayer of New Mercies",
        verse = "His mercies are new every morning; great is your faithfulness.",
        prayer = "Father,\n\nThank You for this morning. This day is a gift — a fresh page, a new beginning, another chance to walk with You. Your mercies have not run out. Your faithfulness has not wavered. Great is Your faithfulness.\n\nI receive Your mercy afresh right now. Yesterday's failures do not follow me into today. Yesterday's grief does not have to define this morning. You make all things new.\n\nHelp me to live this day with gratitude. Let me be aware of Your presence in every moment, every conversation, every quiet moment between the busy ones.\n\nIn Jesus' name, Amen.",
    ),
    DailyPrayer(
        ref = "Nehemiah 8:10",
        title = "Prayer for Joy",
        verse = "Do not grieve, for the joy of the Lord is your strength.",
        prayer = "Lord,\n\nI want to be a person of joy — not a shallow happiness that depends on things going well, but a deep, unshakeable joy that is rooted in who You are and what You have done.\n\nFill me with Your joy today. Let it rise up in me even in the middle of hard things. Let it be a testimony to the people around me that there is something different about those who walk with You.\n\nWhere grief has settled, bring comfort. Where disappointment has dulled my heart, restore wonder. Let the joy of knowing You become my daily strength.\n\nIn Jesus' name, Amen.",
    ),
    DailyPrayer(
        ref = "Psalm 91:1",
        title = "Prayer of Divine Protection",
        verse = "Whoever dwells in the shelter of the Most High will rest in the shadow of the Almighty.",
        prayer = "Most High God,\n\nI dwell in You today. I hide myself in Your shelter — not in my own ability, not in the opinions of others, but in the shadow of Your almighty presence. No weapon formed against me shall prosper. No plague shall come near my dwelling.\n\nCover my family, my home, my health, and my mind with Your divine protection. Send Your angels to guard every path I walk today. Let Your hedge of protection surround all that belongs to me.\n\nI am safe because You are faithful. I rest because You never sleep.\n\nIn Jesus' name, Amen.",
    ),
    DailyPrayer(
        ref = "Mark 12:30",
        title = "Prayer of Wholehearted Love",
        verse = "Love the Lord your God with all your heart and with all your soul.",
        prayer = "Father,\n\nI want to love You with everything — not a divided heart, not a convenient faith, not a Sunday-only devotion. All of my heart. All of my soul. All of my mind. All of my strength.\n\nForgive me for the parts of my life I have withheld from You. The plans I have not surrendered. The areas where I have insisted on doing things my own way. I open all of it to You now.\n\nAs I love You wholeheartedly, let that love overflow outward — to my family, my neighbours, even my enemies. Let love be my defining characteristic.\n\nAmen.",
    ),
    DailyPrayer(
        ref = "1 Corinthians 13:4",
        title = "Prayer to Love Like Jesus",
        verse = "Love is patient, love is kind. It does not envy, it does not boast.",
        prayer = "Father,\n\nI want to love the way Your Word describes — patiently, kindly, without envy or pride. But if I am honest, that kind of love is beyond my natural ability. My love runs out. My patience wears thin. My kindness is often conditional.\n\nSo I ask You to love through me today. Let Your Holy Spirit produce in me the love that only You can give. In the difficult relationship, the frustrating moment, the person who makes it hard — let me respond with Your love, not my own.\n\nMake me a vessel of love in every room I walk into.\n\nIn Jesus' name, Amen.",
    ),
    DailyPrayer(
        ref = "Revelation 21:4",
        title = "Prayer of Hope & Comfort",
        verse = "He will wipe every tear from their eyes. There will be no more death or mourning.",
        prayer = "Lord,\n\nFor everyone walking through grief today — the fresh wound of loss, the lingering ache of what might have been — I pray comfort that only You can give.\n\nRemind us that this is not the end of the story. You are making all things new. Every tear that has fallen has been seen and counted by You. Every broken heart will one day be fully healed. Death does not have the final word. You do.\n\nUntil that day, be our comfort. Hold those who are barely holding on. Let the hope of eternity lighten the weight of today's pain.\n\nIn Jesus' name, Amen.",
    ),
    DailyPrayer(
        ref = "Galatians 5:22–23",
        title = "Prayer for the Fruit of the Spirit",
        verse = "The fruit of the Spirit is love, joy, peace, forbearance, kindness…",
        prayer = "Holy Spirit,\n\nI cannot produce lasting fruit on my own. What I manufacture fades quickly. What You grow in me remains. So I ask You today to bear Your fruit in my life.\n\nGrow love where selfishness has taken root. Grow joy where anxiety has crowded in. Grow peace where conflict lives. Grow patience where irritability rises. Grow kindness where indifference has settled.\n\nPrune in me everything that is not of You. Let my life be a tree that bears good fruit — not for my own glory, but so that others can taste and see that the Lord is good.\n\nAmen.",
    ),
    DailyPrayer(
        ref = "Ephesians 2:8–9",
        title = "Prayer of Gratitude for Grace",
        verse = "For it is by grace you have been saved, through faith — it is the gift of God.",
        prayer = "Father,\n\nI did not earn my way to You. I could not. Salvation is not a reward for my performance — it is a gift from Your hand, received by faith alone. I am overwhelmed when I stop to consider what that means.\n\nYou chose me. You called me. You paid the price I could never pay. And You ask nothing in return except that I receive it and live in the freedom it brings.\n\nLet the truth of grace humble every proud thought in me. Let it also silence every condemning voice that tells me I am not enough. In grace, I am fully enough — because of Jesus.\n\nThank You, Father. Amen.",
    ),
    DailyPrayer(
        ref = "Micah 6:8",
        title = "Prayer for Justice & Humility",
        verse = "Act justly, love mercy and walk humbly with your God.",
        prayer = "Lord,\n\nIn a world of complexity and noise, You have made it simple: act justly, love mercy, walk humbly with You. Three things. Let me do them well today.\n\nWhere I encounter injustice, give me courage to speak and act. Where I could show mercy to someone who does not deserve it, make me generous. And in all of it, keep me humble — aware that I am only able to do any good because of Your grace working through me.\n\nLet my life be a quiet testimony to what it looks like to walk with a just and merciful God.\n\nIn Jesus' name, Amen.",
    ),
    DailyPrayer(
        ref = "John 1:1",
        title = "Prayer Grounded in the Word",
        verse = "In the beginning was the Word, and the Word was with God, and the Word was God.",
        prayer = "Father,\n\nYour Word is not just ancient text — it is living, active, and sharper than any sword. It existed before time began. It holds the universe together. It became flesh and walked among us. It is the foundation on which all truth stands.\n\nGround me in Scripture today. When everything around me shifts, let Your Word be the one thing I return to. Let it be the voice I trust above all others, the standard by which I measure everything I am told.\n\nMay I be a person who not only reads the Word but is shaped by it — in thought, word, and deed.\n\nAmen.",
    ),)
