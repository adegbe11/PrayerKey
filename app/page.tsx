"use client";
import Link from "next/link";
import { useEffect, useRef, useState } from "react";

const ROTATING_WORDS = ["prayers", "sermons", "miracles", "blessings", "churches", "revivals"];

/* ── Verse of the Day pool — cycles daily ─────────────────────────────── */
const DAILY_VERSES = [
  { ref: "John 3:16",          text: "For God so loved the world that he gave his one and only Son, that whoever believes in him shall not perish but have eternal life.", book: "John",        chapter: 3  },
  { ref: "Psalm 23:1",         text: "The Lord is my shepherd, I lack nothing.",                                                                                            book: "Psalms",      chapter: 23 },
  { ref: "Philippians 4:13",   text: "I can do all this through him who gives me strength.",                                                                                book: "Philippians", chapter: 4  },
  { ref: "Romans 8:28",        text: "And we know that in all things God works for the good of those who love him, who have been called according to his purpose.",          book: "Romans",      chapter: 8  },
  { ref: "Isaiah 40:31",       text: "But those who hope in the Lord will renew their strength. They will soar on wings like eagles; they will run and not grow weary, they will walk and not be faint.", book: "Isaiah", chapter: 40 },
  { ref: "Jeremiah 29:11",     text: "For I know the plans I have for you, declares the Lord, plans to prosper you and not to harm you, plans to give you hope and a future.", book: "Jeremiah", chapter: 29 },
  { ref: "Proverbs 3:5–6",     text: "Trust in the Lord with all your heart and lean not on your own understanding; in all your ways submit to him, and he will make your paths straight.", book: "Proverbs", chapter: 3 },
  { ref: "Matthew 6:33",       text: "But seek first his kingdom and his righteousness, and all these things will be given to you as well.",                                  book: "Matthew",     chapter: 6  },
  { ref: "Psalm 46:1",         text: "God is our refuge and strength, an ever-present help in trouble.",                                                                     book: "Psalms",      chapter: 46 },
  { ref: "Romans 12:2",        text: "Do not conform to the pattern of this world, but be transformed by the renewing of your mind. Then you will be able to test and approve what God's will is — his good, pleasing and perfect will.", book: "Romans", chapter: 12 },
  { ref: "2 Timothy 1:7",      text: "For God has not given us a spirit of fear, but of power and of love and of a sound mind.",                                              book: "2 Timothy",   chapter: 1  },
  { ref: "Hebrews 11:1",       text: "Now faith is confidence in what we hope for and assurance about what we do not see.",                                                   book: "Hebrews",     chapter: 11 },
  { ref: "Galatians 5:22–23",  text: "But the fruit of the Spirit is love, joy, peace, forbearance, kindness, goodness, faithfulness, gentleness and self-control.",         book: "Galatians",   chapter: 5  },
  { ref: "Ephesians 2:8–9",    text: "For it is by grace you have been saved, through faith — and this is not from yourselves, it is the gift of God — not by works, so that no one can boast.", book: "Ephesians", chapter: 2 },
  { ref: "Matthew 11:28",      text: "Come to me, all you who are weary and burdened, and I will give you rest.",                                                             book: "Matthew",     chapter: 11 },
  { ref: "Psalm 119:105",      text: "Your word is a lamp for my feet, a light on my path.",                                                                                  book: "Psalms",      chapter: 119},
  { ref: "Joshua 1:9",         text: "Have I not commanded you? Be strong and courageous. Do not be afraid; do not be discouraged, for the Lord your God will be with you wherever you go.", book: "Joshua", chapter: 1 },
  { ref: "Psalm 37:4",         text: "Take delight in the Lord, and he will give you the desires of your heart.",                                                             book: "Psalms",      chapter: 37 },
  { ref: "Isaiah 41:10",       text: "So do not fear, for I am with you; do not be dismayed, for I am your God. I will strengthen you and help you; I will uphold you with my righteous right hand.", book: "Isaiah", chapter: 41 },
  { ref: "1 Corinthians 13:4", text: "Love is patient, love is kind. It does not envy, it does not boast, it is not proud.",                                                  book: "1 Corinthians", chapter: 13 },
  { ref: "Psalm 91:1",         text: "Whoever dwells in the shelter of the Most High will rest in the shadow of the Almighty.",                                               book: "Psalms",      chapter: 91 },
  { ref: "Romans 5:8",         text: "But God demonstrates his own love for us in this: While we were still sinners, Christ died for us.",                                    book: "Romans",      chapter: 5  },
  { ref: "Lamentations 3:22–23", text: "Because of the Lord's great love we are not consumed, for his compassions never fail. They are new every morning; great is your faithfulness.", book: "Lamentations", chapter: 3 },
  { ref: "Psalm 27:1",         text: "The Lord is my light and my salvation — whom shall I fear? The Lord is the stronghold of my life — of whom shall I be afraid?",         book: "Psalms",      chapter: 27 },
  { ref: "John 14:6",          text: "Jesus answered, 'I am the way and the truth and the life. No one comes to the Father except through me.'",                              book: "John",        chapter: 14 },
  { ref: "Nehemiah 8:10",      text: "Do not grieve, for the joy of the Lord is your strength.",                                                                              book: "Nehemiah",    chapter: 8  },
  { ref: "Micah 6:8",          text: "He has shown you, O mortal, what is good. And what does the Lord require of you? To act justly and to love mercy and to walk humbly with your God.", book: "Micah", chapter: 6 },
  { ref: "John 1:1",           text: "In the beginning was the Word, and the Word was with God, and the Word was God.",                                                       book: "John",        chapter: 1  },
  { ref: "Revelation 21:4",    text: "He will wipe every tear from their eyes. There will be no more death or mourning or crying or pain, for the old order of things has passed away.", book: "Revelation", chapter: 21 },
  { ref: "Mark 12:30",         text: "Love the Lord your God with all your heart and with all your soul and with all your mind and with all your strength.",                   book: "Mark",        chapter: 12 },
  { ref: "Psalm 34:18",        text: "The Lord is close to the brokenhearted and saves those who are crushed in spirit.",                                                     book: "Psalms",      chapter: 34 },
];

function getDailyVerse() {
  const now = new Date();
  const start = new Date(now.getFullYear(), 0, 0);
  const dayOfYear = Math.floor((now.getTime() - start.getTime()) / 86_400_000);
  return DAILY_VERSES[dayOfYear % DAILY_VERSES.length];
}

/* ── Prayer of the Day pool ────────────────────────────────────────────── */
const DAILY_PRAYERS: { ref: string; title: string; verse: string; prayer: string }[] = [
  { ref: "John 3:16", title: "Prayer of God's Love",
    verse: "For God so loved the world that he gave his one and only Son.",
    prayer: "Heavenly Father,\n\nThank You for a love so vast that You gave Your only Son for me — not because I deserved it, but because You are love itself. Let the reality of the cross fill every corner of my heart today.\n\nWhen I feel forgotten or unloved, remind me that I am fully known and fully chosen by You. Let Your love be the foundation on which I stand, the voice that silences every lie, and the fire that keeps my faith burning.\n\nTeach me to love others with even a fraction of the love You have shown me.\n\nIn Jesus' name, Amen." },

  { ref: "Psalm 23:1", title: "Prayer of Trust & Provision",
    verse: "The Lord is my shepherd, I lack nothing.",
    prayer: "Good Shepherd,\n\nYou know every need before I speak it. You see every worry I carry into this day. I come to You now with open hands, trusting You to provide exactly what I need — not always what I want, but always what is best.\n\nLead me beside still waters. Restore my soul when it grows weary. Even when I walk through dark valleys, help me feel Your rod and staff, Your comfort and guidance, at every step.\n\nI will not fear. You are with me.\n\nIn Your holy name, Amen." },

  { ref: "Philippians 4:13", title: "Prayer for Strength",
    verse: "I can do all this through him who gives me strength.",
    prayer: "Lord Jesus,\n\nThe challenges before me today feel larger than my ability to face them. My own strength runs out quickly. But You are the One who strengthens me — not with a spirit of striving, but with a deep, settled power that comes from knowing You.\n\nFill me afresh. Where I am weak, be strong. Where I am afraid, give me courage. Where I am tempted to quit, remind me that I can do all things through You.\n\nLet every step I take today be taken in Your strength, not my own.\n\nAmen." },

  { ref: "Romans 8:28", title: "Prayer of Surrender",
    verse: "In all things God works for the good of those who love him.",
    prayer: "Father,\n\nThere are things in my life right now that I do not understand. Circumstances that feel broken, plans that have fallen apart, prayers that seem unanswered. My heart struggles to see any good in them.\n\nBut Your Word declares that in all things — not some things, not the easy things, but all things — You are working for my good. I choose to believe that today, even when I cannot see it.\n\nI surrender what I cannot control. I release the outcome into Your hands. Work all things together according to Your perfect purpose.\n\nIn Jesus' name, Amen." },

  { ref: "Isaiah 40:31", title: "Prayer for Renewal",
    verse: "Those who hope in the Lord will renew their strength.",
    prayer: "Lord,\n\nI am tired. Not just physically, but in my soul. The weight of this season has worn me down in ways I cannot always explain. I come to You not with energy, but with emptiness — trusting that You fill what is empty.\n\nRenew my strength like the eagle's. Let me rise above the heaviness that has settled on me. Restore my vision, my hope, my desire to press forward into all You have called me to.\n\nI wait on You. I put my hope in You alone. Carry me when I cannot run.\n\nIn Your name, Amen." },

  { ref: "Jeremiah 29:11", title: "Prayer Over the Future",
    verse: "I know the plans I have for you, declares the Lord — plans to prosper you.",
    prayer: "Father,\n\nThe future feels uncertain. I have questions without answers and dreams that seem impossibly far away. The gap between where I am and where I want to be is wide, and sometimes I wonder if I am on the right path at all.\n\nRemind me today that You hold a plan for my life — a plan not to harm me but to give me hope and a future. You see the end from the beginning. You are not surprised by anything I am walking through.\n\nI trust Your timing. I release my anxiety about tomorrow. Lead me forward, one step at a time.\n\nIn Jesus' name, Amen." },

  { ref: "Proverbs 3:5–6", title: "Prayer for Direction",
    verse: "Trust in the Lord with all your heart and lean not on your own understanding.",
    prayer: "Father,\n\nI have been leaning on my own understanding, and it has led me in circles. My wisdom is limited. My perspective is partial. I see today but You see eternity.\n\nSo I come to You now with every decision I am facing. I lay them before You — the big ones and the small ones, the urgent and the distant. I choose to trust You with all of my heart, even the parts I have kept closed.\n\nMake my paths straight. Remove confusion and replace it with clarity. Direct my steps in the way of peace.\n\nIn Your name, Amen." },

  { ref: "Matthew 6:33", title: "Prayer of First Things",
    verse: "Seek first his kingdom and his righteousness.",
    prayer: "Lord,\n\nI confess that I have let lesser things take first place — worry, ambition, comfort, distraction. My attention has been scattered and my heart divided. Forgive me for chasing things that do not satisfy.\n\nReorder my priorities today. Let Your kingdom be the first thing I pursue in the morning and the last thing I think about at night. Strip away every distraction that pulls me away from Your presence.\n\nAs I seek You first, I trust that all other things will be added according to Your grace and wisdom.\n\nIn Jesus' name, Amen." },

  { ref: "Psalm 46:1", title: "Prayer for Protection & Peace",
    verse: "God is our refuge and strength, an ever-present help in trouble.",
    prayer: "Heavenly Father,\n\nCover me and my family in Your divine protection today. Block every weapon formed against us and silence every voice that speaks harm. Fill our hearts with peace where there is fear, strength where there is weakness, and joy where there is heaviness.\n\nSurround us with Your light and guide our steps toward Your purpose. Let everything and everyone sent to distract, drain, or destroy be removed by Your hand. Replace it all with love, clarity, and blessings that overflow.\n\nYou are our refuge and we run to You now.\n\nIn Jesus' name, Amen." },

  { ref: "Romans 12:2", title: "Prayer for a Renewed Mind",
    verse: "Be transformed by the renewing of your mind.",
    prayer: "Lord,\n\nMy mind is a battlefield. Negative thoughts, old patterns, fears and doubts — they crowd in and try to shape how I see myself, others, and You. I am tired of living according to the patterns of this world.\n\nRenew my mind with Your Word today. Let Scripture replace every lie. Let truth displace every fear. Transform the way I think until my thoughts are aligned with Yours — full of faith, hope, and love.\n\nI present myself to You as a living offering. Make me new from the inside out.\n\nIn Jesus' name, Amen." },

  { ref: "2 Timothy 1:7", title: "Prayer Against Fear",
    verse: "God has not given us a spirit of fear, but of power, love and a sound mind.",
    prayer: "Father,\n\nFear has been speaking loudly in my life. It whispers that I am not enough, that things will fall apart, that the worst will happen. It has kept me small and paralyzed when You have called me to walk in freedom.\n\nI declare today that fear does not come from You. You have given me power — the same power that raised Christ from the dead. You have given me love — perfect love that drives out fear. You have given me a sound mind — steady, clear, and anchored in truth.\n\nI take authority over every fearful thought in Jesus' name. I will not shrink back.\n\nAmen." },

  { ref: "Hebrews 11:1", title: "Prayer for Greater Faith",
    verse: "Now faith is confidence in what we hope for and assurance about what we do not see.",
    prayer: "Lord,\n\nI believe — but help my unbelief. There are areas of my life where faith has grown thin, where hope has flickered, where what You have promised seems too far away to be real. I am honest about my doubts.\n\nBut I choose to stand on Your Word anyway. I choose to trust what I cannot yet see. I choose to act as though Your promises are already yes and amen, because they are.\n\nGrow my faith. Let it be a faith that moves mountains, endures storms, and holds fast even when everything around me shifts.\n\nIn Jesus' name, Amen." },

  { ref: "Matthew 11:28", title: "Prayer of Rest",
    verse: "Come to me, all you who are weary and burdened, and I will give you rest.",
    prayer: "Jesus,\n\nI am tired. I have been carrying things that were never mine to carry — worry about tomorrow, regret about yesterday, the weight of expectations I can never fully meet. I have been striving when You invited me to rest.\n\nSo I come to You now, just as I am — worn down, hands open, heart willing. I lay every burden at Your feet. Take the heaviness from my shoulders. Exchange it for Your yoke, which is easy, and Your burden, which is light.\n\nTeach me what it means to truly rest in You — not just physically, but in my soul.\n\nAmen." },

  { ref: "Psalm 119:105", title: "Prayer for Wisdom",
    verse: "Your word is a lamp for my feet, a light on my path.",
    prayer: "Father,\n\nI cannot see what is ahead of me. The path forward is not always clear, and I have taken enough wrong turns to know that my instincts alone are not enough. I need Your light.\n\nIlluminate my way through Your Word. Speak to me through Scripture today — not just in general, but specifically, personally, clearly. Let the Bible be more than ink on a page; let it be a living lamp that guides every decision I make.\n\nWhere I am confused, give clarity. Where I am hesitant, give confidence. Let me walk in Your light today.\n\nIn Jesus' name, Amen." },

  { ref: "Joshua 1:9", title: "Prayer for Courage",
    verse: "Be strong and courageous. Do not be afraid; do not be discouraged.",
    prayer: "Lord,\n\nYou commanded Joshua to be strong and courageous — not because his enemies were small, but because You were with him. I receive that same command today. My battles may be real, but You are greater than all of them.\n\nGive me the courage to step into the assignments You have placed before me. Help me to speak when silence would be easier, to move forward when fear says stay, to trust when everything around me is uncertain.\n\nYou go before me. You are behind me. You are beside me. With You, I am never alone.\n\nIn Your mighty name, Amen." },

  { ref: "Psalm 37:4", title: "Prayer of Delight",
    verse: "Take delight in the Lord, and he will give you the desires of your heart.",
    prayer: "Father,\n\nTeach me to delight in You — not in what You can give me, but in who You are. Let my greatest joy be found in Your presence, my deepest satisfaction in knowing You, my highest treasure in relationship with You.\n\nAs I seek You with a whole heart, align the desires of my heart with Yours. Purify what I want until what I want is what You want. Give me holy ambitions, eternal longings, desires that bring glory to Your name.\n\nLet every good thing in my life be received as a gift from Your hand.\n\nAmen." },

  { ref: "Isaiah 41:10", title: "Prayer When Afraid",
    verse: "Do not fear, for I am with you; do not be dismayed, for I am your God.",
    prayer: "God,\n\nI will not lie — I am afraid. The situation I am facing feels overwhelming and the outcome is uncertain. My heart is anxious and my thoughts are running ahead to the worst possible endings.\n\nBut You speak directly into this moment: Do not fear, for I am with you. I hold onto that promise now. You are my God. You strengthen me. You uphold me with Your righteous right hand. Nothing that comes against me is beyond Your ability to handle.\n\nSteady my heart. Calm my mind. Let Your peace, which passes all understanding, guard me today.\n\nIn Jesus' name, Amen." },

  { ref: "Psalm 34:18", title: "Prayer for the Brokenhearted",
    verse: "The Lord is close to the brokenhearted and saves those who are crushed in spirit.",
    prayer: "Father,\n\nSome hearts are broken today. Broken by loss, by betrayal, by disappointment, by grief. The pain is real and it is deep, and sometimes it feels like no one truly understands.\n\nBut You are close to the brokenhearted. You do not stand at a distance from our pain — You come near. You see every tear. You understand every ache. You are moved by our suffering.\n\nCome close right now to everyone reading this who is hurting. Bind up the wounds. Breathe life into crushed spirits. Remind us that weeping may endure for a night, but joy comes in the morning.\n\nIn Jesus' name, Amen." },

  { ref: "Romans 5:8", title: "Prayer of Gratitude for Grace",
    verse: "God demonstrates his own love for us in this: While we were still sinners, Christ died for us.",
    prayer: "Lord Jesus,\n\nYou did not wait for me to get it together before You loved me. You loved me at my lowest, my messiest, my most broken. You went to the cross for me not because I was worthy, but because You are grace.\n\nI am undone by that love today. Let it reshape how I see myself — not as someone who must earn Your favour, but as someone who is already fully accepted because of what You did.\n\nLet the cross be my daily meditation. Let grace be my constant declaration. Let love be what defines everything I do.\n\nThank You, Jesus. Amen." },

  { ref: "John 14:6", title: "Prayer of Commitment to Christ",
    verse: "Jesus answered, I am the way and the truth and the life.",
    prayer: "Lord Jesus,\n\nIn a world full of voices claiming to have the answers, I come back to You. You alone are the way — I will not follow any other path. You alone are the truth — I will not be moved by lies. You alone are the life — apart from You I have nothing of eternal value.\n\nI recommit myself to You today. Let my choices reflect that You are Lord. Let my words declare that You are truth. Let my life be evidence that You are alive and at work in the world.\n\nGuide my steps. Correct my course when I wander. I am Yours.\n\nAmen." },

  { ref: "Psalm 27:1", title: "Prayer of Confidence in God",
    verse: "The Lord is my light and my salvation — whom shall I fear?",
    prayer: "Lord,\n\nYou are my light in every dark season. When confusion closes in, You illuminate the way. When shadows fall, You are brighter than all of them. I will not fear darkness because You are in me.\n\nYou are my salvation — not just for eternity, but for today. You save me from despair, from the enemy, from my own worst impulses. There is nothing and no one I need to fear because You stand between me and everything that threatens me.\n\nI walk into this day with confidence — not in myself, but in You.\n\nIn Your name, Amen." },

  { ref: "Lamentations 3:22–23", title: "Morning Prayer of New Mercies",
    verse: "His mercies are new every morning; great is your faithfulness.",
    prayer: "Father,\n\nThank You for this morning. This day is a gift — a fresh page, a new beginning, another chance to walk with You. Your mercies have not run out. Your faithfulness has not wavered. Great is Your faithfulness.\n\nI receive Your mercy afresh right now. Yesterday's failures do not follow me into today. Yesterday's grief does not have to define this morning. You make all things new.\n\nHelp me to live this day with gratitude. Let me be aware of Your presence in every moment, every conversation, every quiet moment between the busy ones.\n\nIn Jesus' name, Amen." },

  { ref: "Nehemiah 8:10", title: "Prayer for Joy",
    verse: "Do not grieve, for the joy of the Lord is your strength.",
    prayer: "Lord,\n\nI want to be a person of joy — not a shallow happiness that depends on things going well, but a deep, unshakeable joy that is rooted in who You are and what You have done.\n\nFill me with Your joy today. Let it rise up in me even in the middle of hard things. Let it be a testimony to the people around me that there is something different about those who walk with You.\n\nWhere grief has settled, bring comfort. Where disappointment has dulled my heart, restore wonder. Let the joy of knowing You become my daily strength.\n\nIn Jesus' name, Amen." },

  { ref: "Psalm 91:1", title: "Prayer of Divine Protection",
    verse: "Whoever dwells in the shelter of the Most High will rest in the shadow of the Almighty.",
    prayer: "Most High God,\n\nI dwell in You today. I hide myself in Your shelter — not in my own ability, not in the opinions of others, but in the shadow of Your almighty presence. No weapon formed against me shall prosper. No plague shall come near my dwelling.\n\nCover my family, my home, my health, and my mind with Your divine protection. Send Your angels to guard every path I walk today. Let Your hedge of protection surround all that belongs to me.\n\nI am safe because You are faithful. I rest because You never sleep.\n\nIn Jesus' name, Amen." },

  { ref: "Mark 12:30", title: "Prayer of Wholehearted Love",
    verse: "Love the Lord your God with all your heart and with all your soul.",
    prayer: "Father,\n\nI want to love You with everything — not a divided heart, not a convenient faith, not a Sunday-only devotion. All of my heart. All of my soul. All of my mind. All of my strength.\n\nForgive me for the parts of my life I have withheld from You. The plans I have not surrendered. The areas where I have insisted on doing things my own way. I open all of it to You now.\n\nAs I love You wholeheartedly, let that love overflow outward — to my family, my neighbours, even my enemies. Let love be my defining characteristic.\n\nAmen." },

  { ref: "1 Corinthians 13:4", title: "Prayer to Love Like Jesus",
    verse: "Love is patient, love is kind. It does not envy, it does not boast.",
    prayer: "Father,\n\nI want to love the way Your Word describes — patiently, kindly, without envy or pride. But if I am honest, that kind of love is beyond my natural ability. My love runs out. My patience wears thin. My kindness is often conditional.\n\nSo I ask You to love through me today. Let Your Holy Spirit produce in me the love that only You can give. In the difficult relationship, the frustrating moment, the person who makes it hard — let me respond with Your love, not my own.\n\nMake me a vessel of love in every room I walk into.\n\nIn Jesus' name, Amen." },

  { ref: "Revelation 21:4", title: "Prayer of Hope & Comfort",
    verse: "He will wipe every tear from their eyes. There will be no more death or mourning.",
    prayer: "Lord,\n\nFor everyone walking through grief today — the fresh wound of loss, the lingering ache of what might have been — I pray comfort that only You can give.\n\nRemind us that this is not the end of the story. You are making all things new. Every tear that has fallen has been seen and counted by You. Every broken heart will one day be fully healed. Death does not have the final word. You do.\n\nUntil that day, be our comfort. Hold those who are barely holding on. Let the hope of eternity lighten the weight of today's pain.\n\nIn Jesus' name, Amen." },

  { ref: "Galatians 5:22–23", title: "Prayer for the Fruit of the Spirit",
    verse: "The fruit of the Spirit is love, joy, peace, forbearance, kindness…",
    prayer: "Holy Spirit,\n\nI cannot produce lasting fruit on my own. What I manufacture fades quickly. What You grow in me remains. So I ask You today to bear Your fruit in my life.\n\nGrow love where selfishness has taken root. Grow joy where anxiety has crowded in. Grow peace where conflict lives. Grow patience where irritability rises. Grow kindness where indifference has settled.\n\nPrune in me everything that is not of You. Let my life be a tree that bears good fruit — not for my own glory, but so that others can taste and see that the Lord is good.\n\nAmen." },

  { ref: "Ephesians 2:8–9", title: "Prayer of Gratitude for Grace",
    verse: "For it is by grace you have been saved, through faith — it is the gift of God.",
    prayer: "Father,\n\nI did not earn my way to You. I could not. Salvation is not a reward for my performance — it is a gift from Your hand, received by faith alone. I am overwhelmed when I stop to consider what that means.\n\nYou chose me. You called me. You paid the price I could never pay. And You ask nothing in return except that I receive it and live in the freedom it brings.\n\nLet the truth of grace humble every proud thought in me. Let it also silence every condemning voice that tells me I am not enough. In grace, I am fully enough — because of Jesus.\n\nThank You, Father. Amen." },

  { ref: "Micah 6:8", title: "Prayer for Justice & Humility",
    verse: "Act justly, love mercy and walk humbly with your God.",
    prayer: "Lord,\n\nIn a world of complexity and noise, You have made it simple: act justly, love mercy, walk humbly with You. Three things. Let me do them well today.\n\nWhere I encounter injustice, give me courage to speak and act. Where I could show mercy to someone who does not deserve it, make me generous. And in all of it, keep me humble — aware that I am only able to do any good because of Your grace working through me.\n\nLet my life be a quiet testimony to what it looks like to walk with a just and merciful God.\n\nIn Jesus' name, Amen." },

  { ref: "John 1:1", title: "Prayer Grounded in the Word",
    verse: "In the beginning was the Word, and the Word was with God, and the Word was God.",
    prayer: "Father,\n\nYour Word is not just ancient text — it is living, active, and sharper than any sword. It existed before time began. It holds the universe together. It became flesh and walked among us. It is the foundation on which all truth stands.\n\nGround me in Scripture today. When everything around me shifts, let Your Word be the one thing I return to. Let it be the voice I trust above all others, the standard by which I measure everything I am told.\n\nMay I be a person who not only reads the Word but is shaped by it — in thought, word, and deed.\n\nAmen." },
];

function getDailyPrayer() {
  const now   = new Date();
  const start = new Date(now.getFullYear(), 0, 0);
  const day   = Math.floor((now.getTime() - start.getTime()) / 86_400_000);
  return DAILY_PRAYERS[day % DAILY_PRAYERS.length];
}

/* ── Social media download formats ─────────────────────────────────────── */
const FORMATS = [
  { id: "instagram", label: "Instagram", sub: "1:1 Post",       width: 1080, height: 1080 },
  { id: "story",     label: "Story",     sub: "9:16 · TikTok",  width: 1080, height: 1920 },
  { id: "facebook",  label: "Facebook",  sub: "1.91:1 Post",    width: 1200, height: 630  },
  { id: "twitter",   label: "Twitter / X", sub: "16:9 Post",    width: 1200, height: 675  },
  { id: "pinterest", label: "Pinterest", sub: "2:3 Pin",         width: 1000, height: 1500 },
] as const;
type FormatId = typeof FORMATS[number]["id"];

const FEATURES = [
  {
    href:   "/pray",
    mark:   "✦",
    title:  "Generate a Prayer",
    desc:   "Tell us what's on your heart and we'll write a full, scripture-grounded prayer for you in seconds.",
    color:  "var(--pk-purple)",
    bg:     "var(--pk-purple-dim)",
    border: "var(--pk-purple-border)",
  },
  {
    href:   "/live",
    mark:   "◉",
    title:  "Live Sermon",
    desc:   "Start a service. Bible verses are detected in real time and appear on the projector screen as you preach.",
    color:  "var(--pk-red)",
    bg:     "rgba(204,34,0,0.06)",
    border: "rgba(204,34,0,0.18)",
  },
  {
    href:   "/bible",
    mark:   "◆",
    title:  "Search the Bible",
    desc:   "Search all 66 books by verse, keyword, topic, or paraphrase — with cross-references included.",
    color:  "var(--pk-gold)",
    bg:     "var(--pk-gold-dim)",
    border: "var(--pk-gold-border)",
  },
];

export default function HomePage() {
  const [wordIndex,    setWordIndex]    = useState(0);
  const [visible,      setVisible]      = useState(true);
  const [downloading,  setDownloading]  = useState(false);
  const [activeFormat, setActiveFormat] = useState<FormatId>("instagram");
  // prayerCardRef kept for legacy (unused — canvas path now)
  const prayerCardRef = useRef<HTMLDivElement>(null);

  const todayVerse  = getDailyVerse();
  const todayPrayer = getDailyPrayer();
  const fmt         = FORMATS.find(f => f.id === activeFormat)!;

  async function downloadPrayerCard(formatId: FormatId = activeFormat) {
    if (downloading) return;
    setDownloading(true);
    try {
      const f     = FORMATS.find(f => f.id === formatId)!;
      const p     = todayPrayer;
      const W = f.width, H = f.height;
      const canvas = document.createElement("canvas");
      canvas.width  = W; canvas.height = H;
      const ctx = canvas.getContext("2d")!;

      const S  = W / 1080;
      const hr = H / W;
      const TS = hr < 0.72 ? S * (hr / 0.72) : S;
      const PAD = Math.round(W * 0.10);
      const CW  = W - PAD * 2;
      const CX  = W / 2;

      /* centered wrap helper */
      function lines(text: string, maxW: number, font: string): string[] {
        ctx.font = font;
        const words = text.split(" "); const arr: string[] = []; let cur = "";
        for (const w of words) {
          const test = cur ? `${cur} ${w}` : w;
          if (ctx.measureText(test).width > maxW && cur) { arr.push(cur); cur = w; }
          else cur = test;
        }
        if (cur) arr.push(cur);
        return arr;
      }

      // Parse ref: "John 3:16" → book="JOHN", cv="3:16"
      const refMatch = p.ref.match(/^(.*?)\s+(\d+[:\d]*)$/);
      const bookName = (refMatch ? refMatch[1] : p.ref).toUpperCase();
      const chVerse  = refMatch ? refMatch[2] : "";
      // Short prayer = first sentence only
      const shortPrayer = p.prayer.replace(/\n/g, " ").split(/(?<=[.!?])\s+/)[0];

      // ── BACKGROUND ──────────────────────────────────────────────
      ctx.fillStyle = "#F5EFE8";
      ctx.fillRect(0, 0, W, H);

      // Subtle warm vignette
      const vig = ctx.createRadialGradient(CX, H * 0.5, H * 0.2, CX, H * 0.5, H * 0.75);
      vig.addColorStop(0, "rgba(0,0,0,0)");
      vig.addColorStop(1, "rgba(155,112,64,0.07)");
      ctx.fillStyle = vig; ctx.fillRect(0, 0, W, H);

      // ── "PRAYER OF THE DAY" label — very top, centred ───────────
      const labelFS2 = Math.round(13 * S);
      ctx.font = `800 ${labelFS2}px system-ui, sans-serif`;
      ctx.fillStyle = "#9B7040"; ctx.textAlign = "center";
      ctx.fillText("✦  PRAYER OF THE DAY  ✦", CX, Math.round(PAD * 0.68));

      // ── TOP ORNAMENT: small cross ────────────────────────────────
      const crossY = Math.round(PAD * 0.95);
      const cVW = Math.round(6 * S), cVH = Math.round(22 * S), cArm = Math.round(14 * S), cAH = Math.round(5 * S);
      ctx.fillStyle = "#9B7040";
      ctx.fillRect(CX - cVW / 2, crossY, cVW, cVH);
      ctx.fillRect(CX - cArm, crossY + Math.round(7 * S), cArm * 2, cAH);

      // ── Thin line under cross ────────────────────────────────────
      const lineY = crossY + cVH + Math.round(14 * S);
      ctx.strokeStyle = "rgba(155,112,64,0.35)"; ctx.lineWidth = Math.round(1.5 * S);
      ctx.beginPath(); ctx.moveTo(CX - Math.round(60 * S), lineY); ctx.lineTo(CX + Math.round(60 * S), lineY); ctx.stroke();

      let curY = lineY + Math.round(28 * S);

      // ── BOOK NAME — huge black bold, centred ────────────────────
      const bookFS = Math.round((hr < 0.72 ? 76 : hr > 1.4 ? 118 : 100) * TS);
      const bookFont = `900 ${bookFS}px system-ui, -apple-system, Arial, sans-serif`;
      ctx.fillStyle = "#1a1a1a"; ctx.textAlign = "center";
      lines(bookName, CW, bookFont).forEach(l => {
        ctx.font = bookFont;
        ctx.fillText(l, CX, curY + bookFS * 0.8);
        curY += Math.round(bookFS * 0.95);
      });

      // ── CHAPTER:VERSE — large golden, centred ───────────────────
      const cvFS = Math.round((hr < 0.72 ? 62 : hr > 1.4 ? 98 : 82) * TS);
      const cvFont = `900 ${cvFS}px system-ui, -apple-system, Arial, sans-serif`;
      ctx.font = cvFont; ctx.fillStyle = "#9B7040";
      ctx.fillText(chVerse, CX, curY + cvFS * 0.82);
      curY += Math.round(cvFS * 0.95) + Math.round(18 * S);

      // ── ORNAMENTAL DIVIDER ───────────────────────────────────────
      ctx.strokeStyle = "rgba(155,112,64,0.5)"; ctx.lineWidth = Math.round(1.5 * S);
      ctx.beginPath(); ctx.moveTo(CX - Math.round(80 * S), curY); ctx.lineTo(CX - Math.round(16 * S), curY); ctx.stroke();
      ctx.beginPath(); ctx.moveTo(CX + Math.round(16 * S), curY); ctx.lineTo(CX + Math.round(80 * S), curY); ctx.stroke();
      ctx.beginPath(); ctx.arc(CX, curY, Math.round(5 * S), 0, Math.PI * 2);
      ctx.fillStyle = "#9B7040"; ctx.fill();
      curY += Math.round(26 * S);

      // ── VERSE TEXT — medium weight, centred ─────────────────────
      const vFS = Math.round((hr < 0.72 ? 17 : hr > 1.4 ? 24 : 21) * TS);
      const vLH = Math.round(vFS * 1.8);
      const vFont = `400 ${vFS}px Georgia, serif`;
      const verseText = `“${p.verse}”`;
      ctx.font = vFont; ctx.fillStyle = "#2a2a2a";
      lines(verseText, CW, vFont).forEach((l, i) => {
        ctx.font = vFont;
        ctx.fillText(l, CX, curY + vFS * 0.85 + vLH * i);
      });
      curY += lines(verseText, CW, vFont).length * vLH + Math.round(28 * S);

      // ── SECOND DIVIDER ───────────────────────────────────────────
      ctx.strokeStyle = "rgba(155,112,64,0.3)"; ctx.lineWidth = Math.round(1.5 * S);
      ctx.beginPath(); ctx.moveTo(CX - Math.round(50 * S), curY); ctx.lineTo(CX + Math.round(50 * S), curY); ctx.stroke();
      curY += Math.round(24 * S);

      // ── SHORT PRAYER — italic, centred ──────────────────────────
      const pFS = Math.round((hr < 0.72 ? 15 : hr > 1.4 ? 20 : 18) * TS);
      const pLH = Math.round(pFS * 1.85);
      const pFont = `italic 400 ${pFS}px Georgia, serif`;
      ctx.font = pFont; ctx.fillStyle = "#5a4020";
      lines(shortPrayer, CW, pFont).forEach((l, i) => {
        ctx.font = pFont;
        ctx.fillText(l, CX, curY + pFS * 0.85 + pLH * i);
      });

      // ── WATERMARK — sits just below prayer text ──────────────────
      const wmFS  = Math.round(15 * S);
      const wmGap = Math.round(30 * S);
      const wmLineY = curY + lines(shortPrayer, CW, `italic 400 ${Math.round((hr < 0.72 ? 15 : hr > 1.4 ? 20 : 18) * TS)}px Georgia, serif`).length * Math.round((hr < 0.72 ? 15 : hr > 1.4 ? 20 : 18) * TS * 1.85) + wmGap;
      const wmY   = wmLineY + Math.round(20 * S);
      ctx.strokeStyle = "rgba(155,112,64,0.28)"; ctx.lineWidth = Math.round(1.5 * S);
      ctx.beginPath(); ctx.moveTo(CX - Math.round(90 * S), wmLineY); ctx.lineTo(CX + Math.round(90 * S), wmLineY); ctx.stroke();
      ctx.font = `800 ${wmFS}px system-ui, sans-serif`;
      ctx.fillStyle = "#9B7040";
      ctx.fillText("PRAYERKEY.COM", CX, wmY);

      ctx.textAlign = "left";

      // ── EXPORT ──────────────────────────────────────────────────
      const link = document.createElement("a");
      link.download = `prayerkey-${formatId}-${new Date().toISOString().slice(0, 10)}.png`;
      link.href = canvas.toDataURL("image/png");
      link.click();
    } catch (err) {
      console.error("Card download error", err);
    } finally {
      setDownloading(false);
    }
  }

  useEffect(() => {
    const interval = setInterval(() => {
      setVisible(false);
      setTimeout(() => {
        setWordIndex((i) => (i + 1) % ROTATING_WORDS.length);
        setVisible(true);
      }, 350);
    }, 2600);
    return () => clearInterval(interval);
  }, []);

  return (
    <div>

      {/* ══════════════════════════════════════════
          HERO — Neo-brutalist structure
      ══════════════════════════════════════════ */}
      <section style={{ paddingTop: "24px", paddingBottom: "100px", textAlign: "center", position: "relative", overflow: "hidden" }}>

        {/* Hard ruled lines — brutalist grid marks */}
        <div aria-hidden style={{ position: "absolute", top: 0, left: 0, right: 0, height: "1px", background: "var(--pk-accent-dim)" }} />
        <div aria-hidden style={{ position: "absolute", top: 0, left: "50%", width: "1px", height: "100%", background: "var(--pk-border)", transform: "translateX(-50%)", opacity: 0.4 }} />

        {/* Tag — brutalist pill */}
        <div className="animate-fadeUp" style={{
          display:      "inline-flex",
          alignItems:   "center",
          gap:          "8px",
          padding:      "5px 14px 5px 6px",
          border:       "1.5px solid var(--pk-accent-border)",
          borderRadius: "4px",
          marginBottom: "36px",
          background:   "var(--pk-accent-dim)",
          boxShadow:    "3px 3px 0 0 var(--pk-accent-border)",
        }}>
          <span style={{ background: "var(--pk-accent)", color: "#fff", fontSize: "9px", fontWeight: 900, letterSpacing: "0.1em", padding: "2px 7px", borderRadius: "2px", textTransform: "uppercase" }}>NEW</span>
          <span style={{ fontSize: "12px", color: "var(--pk-accent)", fontWeight: 600, letterSpacing: "0.05em", textTransform: "uppercase" }}>AI-Powered Church Companion</span>
        </div>

        {/* Main title — massive, bold — NO animation on LCP element */}
        <h1 style={{
          fontSize:      "clamp(52px, 9vw, 108px)",
          fontWeight:    800,
          lineHeight:    0.95,
          letterSpacing: "-0.04em",
          margin:        "0 0 6px",
          color:         "var(--pk-text)",
        }}>
          Where beautiful
        </h1>

        {/* Rotating shimmer word */}
        <div className="animate-fadeUp delay-200" style={{
          fontSize:       "clamp(52px, 9vw, 108px)",
          fontWeight:     800,
          lineHeight:     0.95,
          letterSpacing:  "-0.04em",
          margin:         "0 0 6px",
          height:         "1em",
          display:        "flex",
          alignItems:     "center",
          justifyContent: "center",
          overflow:       "hidden",
        }}>
          <span
            className="shimmer-accent"
            style={{
              opacity:    visible ? 1 : 0,
              transform:  visible ? "translateY(0)" : "translateY(16px)",
              transition: "opacity 380ms cubic-bezier(0.22,1,0.36,1), transform 380ms cubic-bezier(0.22,1,0.36,1)",
              display:    "inline-block",
              minWidth:   "clamp(280px,38vw,560px)",
              textAlign:  "center",
            }}
          >
            {ROTATING_WORDS[wordIndex]}
          </span>
        </div>

        {/* "are born." — NOT a second h1; use div styled identically */}
        <div aria-hidden className="animate-fadeUp delay-300" style={{
          fontSize:      "clamp(52px, 9vw, 108px)",
          fontWeight:    800,
          lineHeight:    0.95,
          letterSpacing: "-0.04em",
          margin:        "0 0 40px",
          color:         "var(--pk-text)",
        }}>
          are born.
        </div>

        {/* Subtitle */}
        <p className="animate-fadeUp delay-400" style={{
          fontSize:     "clamp(16px, 1.8vw, 20px)",
          color:        "var(--pk-text-2)",
          maxWidth:     "520px",
          marginInline: "auto",
          lineHeight:   1.7,
          marginBottom: "44px",
          letterSpacing: "-0.01em",
        }}>
          Type a prayer request and get a full prayer back.
          Preach a sermon and see Bible verses on the screen.
          Search any scripture in seconds. Free, forever.
        </p>

        {/* CTAs — brutalist buttons */}
        <div className="animate-fadeUp delay-500" style={{ display: "flex", justifyContent: "center", gap: "12px", flexWrap: "wrap", marginBottom: "72px" }}>
          <Link href="/pray" style={{ textDecoration: "none" }}>
            <div
              style={{
                padding:      "14px 32px",
                background:   "var(--pk-accent)",
                color:        "#fff",
                fontSize:     "15px",
                fontWeight:   800,
                letterSpacing:"-0.01em",
                border:       "2px solid var(--pk-accent)",
                borderRadius: "6px",
                boxShadow:    "4px 4px 0 0 var(--pk-accent-border)",
                transition:   "transform 150ms ease, box-shadow 150ms ease",
              }}
              onMouseEnter={e => {
                (e.currentTarget as HTMLDivElement).style.transform = "translate(-2px,-2px)";
                (e.currentTarget as HTMLDivElement).style.boxShadow = "6px 6px 0 0 var(--pk-accent-border)";
              }}
              onMouseLeave={e => {
                (e.currentTarget as HTMLDivElement).style.transform = "translate(0,0)";
                (e.currentTarget as HTMLDivElement).style.boxShadow = "4px 4px 0 0 var(--pk-accent-border)";
              }}
            >
              ✦ Generate a Prayer
            </div>
          </Link>
          <Link href="/live" style={{ textDecoration: "none" }}>
            <div
              style={{
                padding:      "14px 32px",
                background:   "transparent",
                color:        "var(--pk-text-2)",
                fontSize:     "15px",
                fontWeight:   600,
                letterSpacing:"-0.01em",
                border:       "2px solid var(--pk-border-2)",
                borderRadius: "6px",
                boxShadow:    "4px 4px 0 0 var(--pk-border)",
                transition:   "transform 150ms ease, box-shadow 150ms ease, border-color 150ms ease",
              }}
              onMouseEnter={e => {
                (e.currentTarget as HTMLDivElement).style.transform = "translate(-2px,-2px)";
                (e.currentTarget as HTMLDivElement).style.boxShadow = "6px 6px 0 0 var(--pk-border-2)";
              }}
              onMouseLeave={e => {
                (e.currentTarget as HTMLDivElement).style.transform = "translate(0,0)";
                (e.currentTarget as HTMLDivElement).style.boxShadow = "4px 4px 0 0 var(--pk-border)";
              }}
            >
              ◉ Start Live Sermon
            </div>
          </Link>
        </div>

        {/* Stats row — brutalist data */}
        <div className="animate-fadeUp delay-600 stats-row" style={{
          display:    "inline-flex",
          gap:        "0",
          border:     "1.5px solid var(--pk-border)",
          borderRadius: "8px",
          overflow:   "hidden",
          boxShadow:  "4px 4px 0 0 var(--pk-border)",
          maxWidth:   "100%",
        }}>
          {[
            { val: "11",   label: "Translations"    },
            { val: "Free", label: "Always"          },
            { val: "0",    label: "Account needed"  },
            { val: "66",   label: "Books of the Bible" },
          ].map((s, i) => (
            <div key={s.label} style={{
              padding:    "16px 28px",
              textAlign:  "center",
              borderRight: i < 3 ? "1px solid var(--pk-border)" : "none",
              background: "var(--pk-card)",
            }}>
              <div style={{ fontSize: "clamp(20px,3vw,28px)", fontWeight: 800, color: "var(--pk-accent)", letterSpacing: "-0.03em", lineHeight: 1 }}>{s.val}</div>
              <div style={{ fontSize: "11px", color: "var(--pk-text-3)", marginTop: "4px", letterSpacing: "0.04em", textTransform: "uppercase", fontWeight: 600 }}>{s.label}</div>
            </div>
          ))}
        </div>

      </section>

      {/* ══════════════════════════════════════════
          VERSE OF THE DAY — BibleGateway-inspired
      ══════════════════════════════════════════ */}
      <section style={{ marginBottom: "72px" }}>

        {/* Section header — ruled divider with label */}
        <div style={{ display: "flex", alignItems: "center", gap: "14px", marginBottom: "24px" }}>
          <div style={{ height: "1px", flex: 1, background: "var(--pk-border)" }} />
          <span style={{
            fontSize:      "10px",
            fontWeight:    700,
            color:         "var(--pk-accent)",
            letterSpacing: "0.18em",
            textTransform: "uppercase",
          }}>
            ✞ Verse of the Day
          </span>
          <div style={{ height: "1px", flex: 1, background: "var(--pk-border)" }} />
        </div>

        {/* Verse card */}
        <div style={{
          background:   "var(--pk-card)",
          border:       "1px solid var(--pk-border)",
          borderRadius: "20px",
          padding:      "clamp(24px,4vw,48px) clamp(24px,4vw,52px)",
          position:     "relative",
          overflow:     "hidden",
          backdropFilter: "blur(12px)",
        }}>

          {/* Decorative cross watermark */}
          <div aria-hidden style={{
            position:   "absolute",
            top:        "50%",
            right:      "clamp(24px,4vw,52px)",
            transform:  "translateY(-50%)",
            fontSize:   "clamp(80px,10vw,140px)",
            color:      "var(--pk-accent)",
            opacity:    0.04,
            fontWeight: 700,
            lineHeight: 1,
            userSelect: "none",
            pointerEvents: "none",
            fontFamily: "Georgia, serif",
          }}>
            ✝
          </div>

          {/* Translation badge */}
          <div style={{ marginBottom: "20px", display: "flex", alignItems: "center", gap: "10px" }}>
            <span style={{
              fontSize:      "9px",
              fontWeight:    800,
              letterSpacing: "0.18em",
              textTransform: "uppercase",
              color:         "var(--pk-accent)",
              border:        "1.5px solid var(--pk-accent-border)",
              background:    "var(--pk-accent-dim)",
              padding:       "3px 10px",
              borderRadius:  "3px",
            }}>
              NIV
            </span>
            <span style={{ fontSize: "11px", color: "var(--pk-text-3)", letterSpacing: "0.04em" }}>
              New International Version
            </span>
          </div>

          {/* Scripture blockquote — Georgia serif, left crimson border */}
          <blockquote className="scripture-block" style={{
            marginBottom: "18px",
          }}>
            <p className="scripture-text">
              &ldquo;{todayVerse.text}&rdquo;
            </p>
          </blockquote>

          {/* Reference */}
          <p className="scripture-ref scripture-block" style={{ marginBottom: "28px" }}>
            — {todayVerse.ref}
          </p>

          {/* Action row — BibleGateway-style */}
          <div style={{
            display:    "flex",
            gap:        "20px",
            alignItems: "center",
            flexWrap:   "wrap",
            paddingTop: "20px",
            borderTop:  "1px solid var(--pk-border)",
          }}>
            <Link
              href={`/bible?q=${encodeURIComponent(todayVerse.ref)}`}
              style={{
                display:       "inline-flex",
                alignItems:    "center",
                gap:           "5px",
                fontSize:      "13px",
                fontWeight:    700,
                color:         "var(--pk-accent)",
                textDecoration:"none",
                letterSpacing: "0.01em",
                padding:       "8px 16px",
                border:        "1.5px solid var(--pk-accent-border)",
                borderRadius:  "6px",
                background:    "var(--pk-accent-dim)",
                boxShadow:     "2px 2px 0 0 var(--pk-accent-border)",
                transition:    "transform 140ms ease, box-shadow 140ms ease",
              }}
              onMouseEnter={e => {
                (e.currentTarget as HTMLAnchorElement).style.transform  = "translate(-1px,-1px)";
                (e.currentTarget as HTMLAnchorElement).style.boxShadow = "3px 3px 0 0 var(--pk-accent-border)";
              }}
              onMouseLeave={e => {
                (e.currentTarget as HTMLAnchorElement).style.transform  = "translate(0,0)";
                (e.currentTarget as HTMLAnchorElement).style.boxShadow = "2px 2px 0 0 var(--pk-accent-border)";
              }}
            >
              Read {todayVerse.book} {todayVerse.chapter} →
            </Link>

            <span style={{ color: "var(--pk-border-2)", userSelect: "none" }}>|</span>

            <Link
              href={`/pray?topic=${encodeURIComponent("prayer based on " + todayVerse.ref)}`}
              style={{
                fontSize:      "13px",
                fontWeight:    600,
                color:         "var(--pk-text-2)",
                textDecoration:"none",
                letterSpacing: "0.01em",
                display:       "inline-flex",
                alignItems:    "center",
                gap:           "5px",
                transition:    "color 140ms ease",
              }}
              onMouseEnter={e => { (e.currentTarget as HTMLAnchorElement).style.color = "var(--pk-accent)"; }}
              onMouseLeave={e => { (e.currentTarget as HTMLAnchorElement).style.color = "var(--pk-text-2)"; }}
            >
              Pray this verse →
            </Link>

            <span style={{ color: "var(--pk-border-2)", userSelect: "none" }}>|</span>

            <Link
              href="/bible"
              style={{
                fontSize:      "13px",
                fontWeight:    600,
                color:         "var(--pk-text-2)",
                textDecoration:"none",
                display:       "inline-flex",
                alignItems:    "center",
                gap:           "5px",
                transition:    "color 140ms ease",
              }}
              onMouseEnter={e => { (e.currentTarget as HTMLAnchorElement).style.color = "var(--pk-accent)"; }}
              onMouseLeave={e => { (e.currentTarget as HTMLAnchorElement).style.color = "var(--pk-text-2)"; }}
            >
              Search more verses →
            </Link>
          </div>
        </div>
      </section>

      {/* ══════════════════════════════════════════
          PRAYER OF THE DAY — Social media cards
      ══════════════════════════════════════════ */}
      <section style={{ marginBottom: "72px" }}>

        {/* Section header */}
        <div style={{ display: "flex", alignItems: "center", gap: "14px", marginBottom: "28px" }}>
          <div style={{ height: "1px", flex: 1, background: "var(--pk-border)" }} />
          <span style={{ fontSize: "10px", fontWeight: 700, color: "var(--pk-accent)", letterSpacing: "0.18em", textTransform: "uppercase" }}>
            ✦ Prayer of the Day
          </span>
          <div style={{ height: "1px", flex: 1, background: "var(--pk-border)" }} />
        </div>

        {/* Format selector pills */}
        <div style={{ display: "flex", gap: "8px", flexWrap: "wrap", marginBottom: "20px" }}>
          {FORMATS.map(f => (
            <button key={f.id} onClick={() => setActiveFormat(f.id)}
              style={{
                display:       "inline-flex",
                flexDirection: "column",
                alignItems:    "center",
                gap:           "2px",
                padding:       "8px 16px",
                borderRadius:  "10px",
                border:        activeFormat === f.id ? "1.5px solid #00D4D4" : "1.5px solid var(--pk-border)",
                background:    activeFormat === f.id ? "rgba(0,212,212,0.08)" : "var(--pk-surface)",
                cursor:        "pointer",
                transition:    "all 150ms ease",
              }}>
              <span style={{ fontSize: "12px", fontWeight: 700, color: activeFormat === f.id ? "#00B4B4" : "var(--pk-text-2)", letterSpacing: "0.01em" }}>
                {f.label}
              </span>
              <span style={{ fontSize: "10px", color: activeFormat === f.id ? "#00B4B4" : "var(--pk-text-3)", letterSpacing: "0.04em" }}>
                {f.sub}
              </span>
            </button>
          ))}
        </div>

        {/* Preview card — centred: Bible verse · short prayer · watermark */}
        {(() => {
          const isPortrait  = fmt.height > fmt.width;
          const isLandscape = fmt.width > fmt.height * 1.2;

          const refMatch = todayPrayer.ref.match(/^(.*?)\s+(\d+[:\d]*)$/);
          const bookName = (refMatch ? refMatch[1] : todayPrayer.ref).toUpperCase();
          const chVerse  = refMatch ? refMatch[2] : "";
          const shortPrayer = todayPrayer.prayer.replace(/\n/g, " ").split(/(?<=[.!?])\s+/)[0];

          const bookFS  = isPortrait ? "clamp(34px,9vw,58px)"  : isLandscape ? "clamp(26px,4.5vw,52px)" : "clamp(30px,5.5vw,54px)";
          const cvFS    = isPortrait ? "clamp(28px,7.5vw,48px)" : isLandscape ? "clamp(22px,3.8vw,44px)" : "clamp(25px,4.8vw,46px)";
          const verseFS = isPortrait ? "11px" : isLandscape ? "clamp(10px,1.3vw,15px)" : "clamp(11px,1.5vw,16px)";
          const prayFS  = isPortrait ? "10px" : isLandscape ? "clamp(9px,1.1vw,13px)"  : "clamp(10px,1.3vw,14px)";
          const wmFS    = isPortrait ? "9px"  : "clamp(8px,0.9vw,11px)";
          const pad     = isPortrait ? "20px" : isLandscape ? "clamp(14px,2.5%,32px) clamp(20px,4%,40px)" : "clamp(18px,3%,36px)";

          return (
            <div style={{
              display:        "flex",
              justifyContent: isPortrait ? "center" : "stretch",
              marginBottom:   "20px",
            }}>
              <div ref={prayerCardRef} style={{
                width:          isPortrait ? "min(320px, 100%)" : "100%",
                aspectRatio:    `${fmt.width} / ${fmt.height}`,
                background:     "#F5EFE8",
                borderRadius:   "16px",
                padding:        pad,
                position:       "relative",
                overflow:       "hidden",
                boxShadow:      "0 8px 40px rgba(0,0,0,0.12), 0 0 0 1px rgba(155,112,64,0.15)",
                display:        "flex",
                flexDirection:  "column",
                alignItems:     "center",
                justifyContent: "center",
                textAlign:      "center",
                gap:            isPortrait ? "6px" : "clamp(4px,0.8%,10px)",
              }}>

                {/* "PRAYER OF THE DAY" label — very top */}
                <p style={{
                  fontSize:      isPortrait ? "8px" : "clamp(7px,0.85vw,10px)",
                  fontWeight:    800,
                  color:         "#9B7040",
                  fontFamily:    "system-ui, sans-serif",
                  letterSpacing: "0.22em",
                  textTransform: "uppercase",
                  margin:        0,
                }}>✦ Prayer of the Day ✦</p>

                {/* Top cross ornament */}
                <div style={{ display: "flex", flexDirection: "column", alignItems: "center", marginBottom: isPortrait ? "4px" : "clamp(2px,0.5%,8px)" }}>
                  <div style={{ position: "relative", width: isPortrait ? "12px" : "clamp(9px,1.2vw,15px)", display: "flex", flexDirection: "column", alignItems: "center" }}>
                    <div style={{ width: "40%", height: isPortrait ? "18px" : "clamp(13px,2vw,20px)", background: "#9B7040", borderRadius: "1px" }} />
                    <div style={{ position: "absolute", top: "28%", left: 0, right: 0, height: "28%", background: "#9B7040", borderRadius: "1px" }} />
                  </div>
                  {/* Short line below cross */}
                  <div style={{ width: isPortrait ? "60px" : "clamp(44px,6vw,80px)", height: "1px", background: "rgba(155,112,64,0.4)", marginTop: isPortrait ? "8px" : "clamp(6px,1%,12px)" }} />
                </div>

                {/* Book name — HUGE black */}
                <p style={{
                  fontSize: bookFS, fontWeight: 900, color: "#1a1a1a",
                  fontFamily: "system-ui, -apple-system, sans-serif",
                  lineHeight: 0.92, letterSpacing: "-0.02em",
                  margin: 0,
                }}>{bookName}</p>

                {/* Chapter:verse — large golden */}
                <p style={{
                  fontSize: cvFS, fontWeight: 900, color: "#9B7040",
                  fontFamily: "system-ui, -apple-system, sans-serif",
                  lineHeight: 1.0, letterSpacing: "-0.01em",
                  margin: 0,
                }}>{chVerse}</p>

                {/* Ornamental divider — line · dot · line */}
                <div style={{ display: "flex", alignItems: "center", gap: isPortrait ? "8px" : "clamp(6px,1vw,12px)", width: "100%", justifyContent: "center", margin: isPortrait ? "4px 0" : "clamp(3px,0.6%,8px) 0" }}>
                  <div style={{ flex: 1, maxWidth: isPortrait ? "60px" : "clamp(44px,8vw,90px)", height: "1px", background: "rgba(155,112,64,0.45)" }} />
                  <div style={{ width: isPortrait ? "6px" : "clamp(5px,0.7vw,8px)", aspectRatio: "1", borderRadius: "50%", background: "#9B7040" }} />
                  <div style={{ flex: 1, maxWidth: isPortrait ? "60px" : "clamp(44px,8vw,90px)", height: "1px", background: "rgba(155,112,64,0.45)" }} />
                </div>

                {/* Bible verse text */}
                <p style={{
                  fontFamily: "Georgia, serif", fontSize: verseFS,
                  fontWeight: 400, color: "#2a2a2a",
                  lineHeight: 1.75, margin: 0,
                  maxWidth: "88%",
                }}>
                  &ldquo;{todayPrayer.verse}&rdquo;
                </p>

                {/* Second divider */}
                <div style={{ width: isPortrait ? "44px" : "clamp(32px,5vw,60px)", height: "1px", background: "rgba(155,112,64,0.3)", margin: isPortrait ? "2px 0" : "clamp(2px,0.4%,6px) 0" }} />

                {/* Short prayer */}
                <p style={{
                  fontFamily: "Georgia, serif", fontSize: prayFS,
                  fontStyle: "italic", fontWeight: 400, color: "#5a4020",
                  lineHeight: 1.8, margin: 0,
                  maxWidth: "90%",
                }}>
                  {shortPrayer}
                </p>

                {/* Watermark — flows in the flex column, right below short prayer */}
                <div style={{
                  display:       "flex",
                  flexDirection: "column",
                  alignItems:    "center",
                  gap:           "5px",
                  marginTop:     isPortrait ? "14px" : "clamp(10px,1.8%,22px)",
                }}>
                  <div style={{ width: isPortrait ? "100px" : "clamp(70px,12vw,130px)", height: "1px", background: "rgba(155,112,64,0.28)" }} />
                  <span style={{
                    fontSize:      wmFS,
                    fontWeight:    800,
                    color:         "#9B7040",
                    fontFamily:    "system-ui,sans-serif",
                    letterSpacing: "0.18em",
                    textTransform: "uppercase",
                  }}>PRAYERKEY.COM</span>
                </div>
              </div>
            </div>
          );
        })()}

        {/* Action row */}
        <div style={{ display: "flex", gap: "12px", alignItems: "center", flexWrap: "wrap" }}>

          {/* Download button */}
          <button
            onClick={() => downloadPrayerCard(activeFormat)}
            disabled={downloading}
            style={{
              display:       "inline-flex",
              alignItems:    "center",
              gap:           "7px",
              padding:       "11px 22px",
              fontSize:      "13px",
              fontWeight:    700,
              color:         "#000000",
              background:    downloading ? "rgba(0,180,180,0.5)" : "#00D4D4",
              border:        "1.5px solid transparent",
              borderRadius:  "8px",
              cursor:        downloading ? "not-allowed" : "pointer",
              boxShadow:     "3px 3px 0 0 rgba(0,180,180,0.4)",
              letterSpacing: "0.01em",
              transition:    "transform 140ms ease, box-shadow 140ms ease",
            }}
            onMouseEnter={e => { if (!downloading) { (e.currentTarget as HTMLButtonElement).style.transform = "translate(-1px,-1px)"; (e.currentTarget as HTMLButtonElement).style.boxShadow = "4px 4px 0 0 rgba(0,180,180,0.5)"; } }}
            onMouseLeave={e => { (e.currentTarget as HTMLButtonElement).style.transform = "translate(0,0)"; (e.currentTarget as HTMLButtonElement).style.boxShadow = "3px 3px 0 0 rgba(0,180,180,0.4)"; }}
          >
            {downloading ? "⏳ Saving…" : `⬇ Download for ${fmt.label}`}
          </button>

          <span style={{ color: "var(--pk-border-2)", userSelect: "none" }}>|</span>

          <Link href={`/pray?topic=${encodeURIComponent("prayer based on " + todayPrayer.ref)}`}
            style={{ fontSize: "13px", fontWeight: 600, color: "var(--pk-text-2)", textDecoration: "none", transition: "color 140ms ease" }}
            onMouseEnter={e => { (e.currentTarget as HTMLAnchorElement).style.color = "var(--pk-accent)"; }}
            onMouseLeave={e => { (e.currentTarget as HTMLAnchorElement).style.color = "var(--pk-text-2)"; }}>
            Deepen this prayer →
          </Link>

          <span style={{ color: "var(--pk-border-2)", userSelect: "none" }}>|</span>

          <Link href={`/bible?q=${encodeURIComponent(todayPrayer.ref)}`}
            style={{ fontSize: "13px", fontWeight: 600, color: "var(--pk-text-2)", textDecoration: "none", transition: "color 140ms ease" }}
            onMouseEnter={e => { (e.currentTarget as HTMLAnchorElement).style.color = "var(--pk-accent)"; }}
            onMouseLeave={e => { (e.currentTarget as HTMLAnchorElement).style.color = "var(--pk-text-2)"; }}>
            Read the verse →
          </Link>
        </div>
      </section>

      {/* ══════════════════════════════════════════
          3 FEATURE CARDS — Glass panels
      ══════════════════════════════════════════ */}
      <section style={{ marginBottom: "80px" }}>

        {/* Brutalist section label */}
        <div style={{ display: "flex", alignItems: "center", gap: "16px", marginBottom: "32px" }}>
          <div style={{ height: "2px", flex: 1, background: "var(--pk-border)" }} />
          <span style={{ fontSize: "11px", fontWeight: 700, color: "var(--pk-text-3)", letterSpacing: "0.12em", textTransform: "uppercase" }}>Everything You Need</span>
          <div style={{ height: "2px", flex: 1, background: "var(--pk-border)" }} />
        </div>

        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(280px,1fr))", gap: "2px", border: "1.5px solid var(--pk-border)", borderRadius: "16px", overflow: "hidden" }}>
          {FEATURES.map((f, i) => (
            <Link key={f.href} href={f.href} style={{ textDecoration: "none" }}>
              <div
                style={{
                  padding:    "36px 28px",
                  background: "var(--pk-card)",
                  backdropFilter: "blur(12px)",
                  borderRight: i < FEATURES.length - 1 ? "1px solid var(--pk-border)" : "none",
                  transition: "background 200ms ease",
                  height:     "100%",
                }}
                onMouseEnter={e => {
                  (e.currentTarget as HTMLDivElement).style.background = `${f.bg}`;
                }}
                onMouseLeave={e => {
                  (e.currentTarget as HTMLDivElement).style.background = "var(--pk-card)";
                }}
              >
                {/* Feature index + mark */}
                <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: "28px" }}>
                  <span style={{
                    fontSize:      "10px",
                    fontWeight:    700,
                    color:         "var(--pk-text-3)",
                    letterSpacing: "0.14em",
                    border:        "1px solid var(--pk-border)",
                    padding:       "3px 9px",
                    borderRadius:  "3px",
                    fontVariantNumeric: "tabular-nums",
                  }}>
                    0{i + 1}
                  </span>
                  <span style={{
                    fontSize:   f.mark === "◉" ? "18px" : "16px",
                    color:      f.color,
                    fontWeight: 700,
                    lineHeight: 1,
                    animation:  f.mark === "◉" ? "pulse 2s ease infinite" : "none",
                  }}>
                    {f.mark}
                  </span>
                </div>

                <h2 style={{ fontSize: "19px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 12px", letterSpacing: "-0.02em", lineHeight: 1.2 }}>
                  {f.title}
                </h2>
                <p style={{ fontSize: "14px", color: "var(--pk-text-2)", margin: "0 0 28px", lineHeight: 1.65 }}>
                  {f.desc}
                </p>
                <span style={{
                  display:      "inline-flex",
                  alignItems:   "center",
                  gap:          "6px",
                  fontSize:     "13px",
                  fontWeight:   700,
                  color:        f.color,
                  letterSpacing:"-0.01em",
                  border:       `1.5px solid ${f.border}`,
                  padding:      "7px 16px",
                  borderRadius: "4px",
                  boxShadow:    `3px 3px 0 0 ${f.border}`,
                  transition:   "transform 150ms ease, box-shadow 150ms ease",
                }}>
                  Get started →
                </span>
              </div>
            </Link>
          ))}
        </div>
      </section>


      {/* Below-fold sections — cv-auto defers their paint until they scroll into view */}
      <div className="cv-auto"><SermonFeatureSection /></div>
      <div className="cv-auto"><PrayerFeatureSection /></div>
      <div className="cv-auto"><BibleFeatureSection /></div>
      <div className="cv-auto"><FAQ /></div>

    </div>
  );
}

function BibleFeatureSection() {
  return (
    <section style={{ marginTop: "80px" }}>

      {/* Section heading */}
      <div style={{ textAlign: "center", marginBottom: "40px" }}>
        <p style={{ fontSize: "13px", fontWeight: 700, color: "var(--pk-gold)", letterSpacing: "0.08em", textTransform: "uppercase", margin: "0 0 12px" }}>
          FEATURE 03
        </p>
        <h2 style={{ fontSize: "clamp(28px,5vw,48px)", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 12px", letterSpacing: "-0.03em", lineHeight: 1.12 }}>
          #1 Bible Search<br />&amp; Cross-Reference Tool
        </h2>
        <p style={{ fontSize: "17px", color: "var(--pk-text-2)", margin: 0, maxWidth: "480px", marginInline: "auto" }}>
          The smartest way to find any verse, topic, or keyword — with related scriptures included.
        </p>
      </div>

      {/* Card — copy left, mockup right */}
      <div className="feat-grid" style={{
        background:          "var(--pk-gold-dim)",
        border:              "1px solid var(--pk-gold-border)",
        borderRadius:        "28px",
        padding:             "clamp(24px,4vw,40px)",
        display:             "grid",
        gridTemplateColumns: "1fr 1fr",
        gap:                 "48px",
        alignItems:          "center",
      }}>

        {/* Left — copy */}
        <div>
          <h3 style={{ fontSize: "22px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 12px", letterSpacing: "-0.02em" }}>
            Find Any Verse Instantly
          </h3>
          <p style={{ fontSize: "15px", color: "var(--pk-text-2)", lineHeight: 1.75, margin: "0 0 24px" }}>
            Type a reference, keyword, or topic and PrayerKey returns the most relevant verses from across the entire Bible. Click any result to instantly load 4–6 cross-referenced scriptures that share the same theme — perfect for sermon prep, Bible study, or personal devotion.
          </p>

          <div style={{ display: "flex", flexDirection: "column", gap: "10px", marginBottom: "32px" }}>
            {[
              "Search by reference, keyword, topic, or paraphrase",
              "Covers all 66 books of the Bible",
              "Cross-references show deeply related scriptures",
              "Supports 11 major translations including KJV and NIV",
              "Instant results — no waiting, no account needed",
            ].map((item) => (
              <div key={item} style={{ display: "flex", alignItems: "flex-start", gap: "10px" }}>
                <span style={{ color: "var(--pk-gold)", fontWeight: 700, fontSize: "15px", flexShrink: 0, marginTop: "1px" }}>✓</span>
                <span style={{ fontSize: "14px", color: "var(--pk-text-2)", lineHeight: 1.5 }}>{item}</span>
              </div>
            ))}
          </div>

          <a href="/bible" style={{ textDecoration: "none" }}>
            <button style={{
              padding:      "13px 28px",
              borderRadius: "100px",
              border:       "none",
              background:   "var(--pk-gold)",
              color:        "#fff",
              fontSize:     "15px",
              fontWeight:   700,
              cursor:       "pointer",
              boxShadow:    "0 6px 20px var(--pk-gold-dim)",
            }}>
              Start now →
            </button>
          </a>
        </div>

        {/* Right — mockup */}
        <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>

          {/* Search bar */}
          <div style={{ display: "flex", gap: "8px", padding: "10px 16px", background: "var(--pk-card)", border: "1.5px solid var(--pk-gold-border)", borderRadius: "100px", alignItems: "center" }}>
            <span style={{ fontSize: "12px", fontWeight: 700, color: "var(--pk-gold)" }}>◆</span>
            <span style={{ fontSize: "14px", color: "var(--pk-text-2)", flex: 1 }}>do not be afraid</span>
            <span style={{ fontSize: "12px", fontWeight: 700, color: "var(--pk-gold)", background: "var(--pk-gold-dim)", padding: "4px 12px", borderRadius: "100px" }}>Search</span>
          </div>

          {/* Result 1 — selected */}
          <div style={{ padding: "16px", background: "var(--pk-gold-dim)", border: "1.5px solid var(--pk-gold-border)", borderRadius: "14px" }}>
            <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: "6px" }}>
              <span style={{ fontSize: "13px", fontWeight: 700, color: "var(--pk-gold)" }}>Isaiah 41:10</span>
              <span style={{ fontSize: "10px", color: "var(--pk-text-3)", background: "var(--pk-card)", padding: "2px 8px", borderRadius: "100px" }}>Exact match</span>
            </div>
            <p style={{ fontSize: "13px", color: "var(--pk-text)", lineHeight: 1.65, margin: 0 }}>
              &ldquo;So do not fear, for I am with you; do not be dismayed, for I am your God...&rdquo;
            </p>
          </div>

          {/* Result 2 */}
          <div style={{ padding: "14px 16px", background: "var(--pk-card)", border: "1px solid var(--pk-border)", borderRadius: "12px" }}>
            <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: "5px" }}>
              <span style={{ fontSize: "12px", fontWeight: 700, color: "var(--pk-gold)" }}>Joshua 1:9</span>
              <span style={{ fontSize: "10px", color: "var(--pk-text-3)" }}>Related</span>
            </div>
            <p style={{ fontSize: "12px", color: "var(--pk-text-2)", lineHeight: 1.6, margin: 0 }}>
              &ldquo;Be strong and courageous. Do not be afraid; do not be discouraged...&rdquo;
            </p>
          </div>

          {/* Cross-refs panel */}
          <div style={{ padding: "14px 16px", background: "var(--pk-gold-dim)", border: "1px solid var(--pk-gold-border)", borderRadius: "12px" }}>
            <p style={{ fontSize: "11px", fontWeight: 700, color: "var(--pk-gold)", letterSpacing: "0.06em", textTransform: "uppercase", margin: "0 0 10px" }}>
              Related Verses for Isaiah 41:10
            </p>
            <div style={{ display: "flex", flexDirection: "column", gap: "7px" }}>
              {[
                { ref: "Psalm 46:1",        snippet: "God is our refuge and strength, an ever-present help..." },
                { ref: "Romans 8:31",       snippet: "If God is for us, who can be against us?" },
                { ref: "2 Timothy 1:7",     snippet: "For God has not given us a spirit of fear, but of power..." },
              ].map((c) => (
                <div key={c.ref} style={{ display: "flex", gap: "10px", alignItems: "flex-start" }}>
                  <span style={{ fontSize: "11px", fontWeight: 700, color: "var(--pk-gold)", flexShrink: 0, minWidth: "90px" }}>{c.ref}</span>
                  <span style={{ fontSize: "11px", color: "var(--pk-text-3)", lineHeight: 1.5 }}>{c.snippet}</span>
                </div>
              ))}
            </div>
          </div>

        </div>
      </div>
    </section>
  );
}

function PrayerFeatureSection() {
  return (
    <section style={{ marginTop: "80px" }}>

      {/* Section heading */}
      <div style={{ textAlign: "center", marginBottom: "40px" }}>
        <p style={{ fontSize: "13px", fontWeight: 700, color: "var(--pk-purple)", letterSpacing: "0.08em", textTransform: "uppercase", margin: "0 0 12px" }}>
          FEATURE 02
        </p>
        <h2 style={{ fontSize: "clamp(28px,5vw,48px)", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 12px", letterSpacing: "-0.03em", lineHeight: 1.12 }}>
          #1 AI Prayer<br />Generator for Churches
        </h2>
        <p style={{ fontSize: "17px", color: "var(--pk-text-2)", margin: 0, maxWidth: "480px", marginInline: "auto" }}>
          The most personal way to write scripture-grounded prayers in seconds — for any situation.
        </p>
      </div>

      {/* Card — flipped: mockup left, copy right */}
      <div className="feat-grid feat-grid-reverse" style={{
        background:          "var(--pk-purple-dim)",
        border:              "1px solid var(--pk-purple-border)",
        borderRadius:        "28px",
        padding:             "clamp(24px,4vw,40px)",
        display:             "grid",
        gridTemplateColumns: "1fr 1fr",
        gap:                 "48px",
        alignItems:          "center",
      }}>

        {/* Left — mockup */}
        <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>

          {/* Input box */}
          <div style={{ padding: "14px 16px", background: "var(--pk-card)", border: "1.5px solid var(--pk-purple-border)", borderRadius: "14px" }}>
            <p style={{ fontSize: "13px", color: "var(--pk-text-2)", margin: 0, lineHeight: 1.6, fontStyle: "italic" }}>
              &ldquo;Lord, I am anxious about my job interview tomorrow. I need peace and confidence...&rdquo;
            </p>
          </div>

          {/* Mood chips */}
          <div style={{ display: "flex", gap: "6px", flexWrap: "wrap" }}>
            {["Anxious", "Hopeful", "Grateful"].map((m, i) => (
              <span key={m} style={{
                padding: "5px 12px", borderRadius: "100px", fontSize: "12px", fontWeight: 600,
                background: i === 0 ? "var(--pk-purple-dim)" : "var(--pk-card)",
                color: i === 0 ? "var(--pk-purple)" : "var(--pk-text-3)",
                border: i === 0 ? "1px solid var(--pk-purple-border)" : "1px solid var(--pk-border)",
              }}>{m}</span>
            ))}
          </div>

          {/* Generated prayer */}
          <div style={{ padding: "20px", background: "var(--pk-purple-dim)", border: "1.5px solid var(--pk-purple-border)", borderRadius: "16px" }}>
            <p style={{ fontSize: "11px", fontWeight: 700, color: "var(--pk-purple)", letterSpacing: "0.08em", textTransform: "uppercase", margin: "0 0 10px" }}>
              ✨ Generated Prayer
            </p>
            <p style={{ fontSize: "13px", color: "var(--pk-text)", lineHeight: 1.75, margin: "0 0 14px", fontStyle: "italic" }}>
              &ldquo;Heavenly Father, I come before you with a heart full of uncertainty. You know the path laid before me. Grant me the peace that surpasses all understanding, and let your confidence rest upon me...&rdquo;
            </p>

            {/* Verse tag */}
            <div style={{ padding: "8px 12px", background: "var(--pk-gold-dim)", borderRadius: "8px", display: "inline-block" }}>
              <span style={{ fontSize: "12px", fontWeight: 600, color: "var(--pk-gold)" }}>Philippians 4:6–7 </span>
              <span style={{ fontSize: "12px", color: "var(--pk-text-3)" }}>· Do not be anxious about anything...</span>
            </div>
          </div>

          {/* Encouragement strip */}
          <div style={{ padding: "12px 16px", background: "var(--pk-card)", borderRadius: "10px", borderLeft: "3px solid var(--pk-purple)" }}>
            <p style={{ fontSize: "12px", color: "var(--pk-text-2)", margin: 0, fontStyle: "italic", lineHeight: 1.6 }}>
              &ldquo;God&apos;s plan for your life is greater than any interview result. Walk in boldly.&rdquo;
            </p>
          </div>

          {/* Copy button */}
          <div style={{ display: "flex", gap: "8px" }}>
            <div style={{ padding: "8px 18px", borderRadius: "100px", background: "var(--pk-card)", border: "1px solid var(--pk-border)", fontSize: "12px", color: "var(--pk-text-2)", cursor: "pointer" }}>
              Copy Prayer
            </div>
            <div style={{ padding: "8px 18px", borderRadius: "100px", background: "var(--pk-card)", border: "1px solid var(--pk-border)", fontSize: "12px", color: "var(--pk-text-2)", cursor: "pointer" }}>
              Regenerate
            </div>
          </div>
        </div>

        {/* Right — copy */}
        <div>
          <h3 style={{ fontSize: "22px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 12px", letterSpacing: "-0.02em" }}>
            Your Personal Prayer Writer
          </h3>
          <p style={{ fontSize: "15px", color: "var(--pk-text-2)", lineHeight: 1.75, margin: "0 0 24px" }}>
            Tell PrayerKey what you&apos;re going through and it writes a full, heartfelt prayer grounded in scripture — personalised to your exact words, mood, and situation. Every prayer includes relevant Bible verses and an encouragement note.
          </p>

          <div style={{ display: "flex", flexDirection: "column", gap: "10px", marginBottom: "32px" }}>
            {[
              "Works for healing, grief, anxiety, finances, marriage and more",
              "Includes scripture-backed Bible verses automatically",
              "Choose your mood to personalise the tone",
              "Copy and share in seconds",
              "No account, no limit, completely free",
            ].map((item) => (
              <div key={item} style={{ display: "flex", alignItems: "flex-start", gap: "10px" }}>
                <span style={{ color: "var(--pk-purple)", fontWeight: 700, fontSize: "15px", flexShrink: 0, marginTop: "1px" }}>✓</span>
                <span style={{ fontSize: "14px", color: "var(--pk-text-2)", lineHeight: 1.5 }}>{item}</span>
              </div>
            ))}
          </div>

          <a href="/pray" style={{ textDecoration: "none" }}>
            <button style={{
              padding:   "13px 28px",
              borderRadius: "100px",
              border:    "none",
              background: "var(--pk-purple)",
              color:     "#fff",
              fontSize:  "15px",
              fontWeight: 700,
              cursor:    "pointer",
              boxShadow: "0 6px 20px var(--pk-purple-dim)",
            }}>
              Start now →
            </button>
          </a>
        </div>

      </div>
    </section>
  );
}

function SermonFeatureSection() {
  return (
    <section style={{ marginTop: "80px" }}>

      {/* Section heading */}
      <div style={{ textAlign: "center", marginBottom: "40px" }}>
        <p style={{ fontSize: "13px", fontWeight: 700, color: "var(--pk-red)", letterSpacing: "0.08em", textTransform: "uppercase", margin: "0 0 12px" }}>
          FEATURE 01
        </p>
        <h2 style={{ fontSize: "clamp(28px,5vw,48px)", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 12px", letterSpacing: "-0.03em", lineHeight: 1.12 }}>
          #1 Live Sermon<br />Verse Detection Tool
        </h2>
        <p style={{ fontSize: "17px", color: "var(--pk-text-2)", margin: 0, maxWidth: "480px", marginInline: "auto" }}>
          The fastest way to display Bible verses on your projector — automatically, as you preach.
        </p>
      </div>

      {/* Card */}
      <div className="feat-grid" style={{
        background:   "rgba(255,59,48,0.04)",
        border:       "1px solid rgba(255,59,48,0.15)",
        borderRadius: "28px",
        padding:      "clamp(24px,4vw,40px)",
        display:      "grid",
        gridTemplateColumns: "1fr 1fr",
        gap:          "48px",
        alignItems:   "center",
      }}>

        {/* Left — copy */}
        <div>
          <h3 style={{ fontSize: "22px", fontWeight: 700, color: "var(--pk-text)", margin: "0 0 12px", letterSpacing: "-0.02em" }}>
            Hands-Free Verse Display
          </h3>
          <p style={{ fontSize: "15px", color: "var(--pk-text-2)", lineHeight: 1.75, margin: "0 0 24px" }}>
            Just preach. PrayerKey listens through your microphone, detects every Bible verse you quote or reference in real time, and displays it on the projector screen for your whole congregation — no operator, no typing, no delay.
          </p>

          <div style={{ display: "flex", flexDirection: "column", gap: "10px", marginBottom: "32px" }}>
            {[
              "Detects verses automatically as you speak",
              "Displays on projector with zero manual input",
              "Works with 11 Bible translations",
              "Shows confidence score for every verse",
              "Pastor controls everything from one screen",
            ].map((item) => (
              <div key={item} style={{ display: "flex", alignItems: "flex-start", gap: "10px" }}>
                <span style={{ color: "var(--pk-red)", fontWeight: 700, fontSize: "15px", flexShrink: 0, marginTop: "1px" }}>✓</span>
                <span style={{ fontSize: "14px", color: "var(--pk-text-2)", lineHeight: 1.5 }}>{item}</span>
              </div>
            ))}
          </div>

          <a href="/live" style={{ textDecoration: "none" }}>
            <button style={{
              padding:      "13px 28px",
              borderRadius: "100px",
              border:       "none",
              background:   "var(--pk-red)",
              color:        "#fff",
              fontSize:     "15px",
              fontWeight:   700,
              cursor:       "pointer",
              boxShadow:    "0 6px 20px rgba(255,59,48,0.25)",
            }}>
              Start now →
            </button>
          </a>
        </div>

        {/* Right — live mockup */}
        <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>

          {/* Status bar */}
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "10px 16px", background: "rgba(255,59,48,0.08)", borderRadius: "12px", border: "1px solid rgba(255,59,48,0.2)" }}>
            <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
              <span style={{ width: "7px", height: "7px", borderRadius: "50%", background: "#FF3B30", display: "inline-block", animation: "pulse 1.5s ease infinite" }} />
              <span style={{ fontSize: "13px", fontWeight: 600, color: "#FF3B30" }}>LIVE — Listening</span>
            </div>
            <span style={{ fontSize: "12px", color: "var(--pk-text-3)" }}>00:14</span>
          </div>

          {/* Transcript */}
          <div style={{ padding: "10px 14px", background: "var(--pk-card)", borderRadius: "10px" }}>
            <p style={{ fontSize: "12px", color: "var(--pk-text-3)", margin: 0, fontStyle: "italic", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
              &ldquo;...for God so loved the world that he gave his only begotten son...&rdquo;
            </p>
          </div>

          {/* Detected verse card */}
          <div style={{ padding: "20px", background: "var(--pk-gold-dim)", border: "1.5px solid var(--pk-gold-border)", borderRadius: "16px" }}>
            <span style={{ fontSize: "10px", fontWeight: 700, color: "var(--pk-gold)", letterSpacing: "0.08em", textTransform: "uppercase", display: "block", marginBottom: "8px" }}>
              ✦ Just Detected
            </span>
            <p style={{ fontSize: "14px", color: "var(--pk-text)", lineHeight: 1.65, margin: "0 0 12px", fontStyle: "italic" }}>
              &ldquo;For God so loved the world that he gave his one and only Son, that whoever believes in him shall not perish but have eternal life.&rdquo;
            </p>
            <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
              <span style={{ fontSize: "13px", fontWeight: 700, color: "var(--pk-gold)" }}>John 3:16</span>
              <span style={{ fontSize: "11px", color: "var(--pk-text-3)" }}>NIV</span>
              <div style={{ marginLeft: "auto", display: "flex", alignItems: "center", gap: "6px" }}>
                <div style={{ width: "60px", height: "3px", background: "var(--pk-border)", borderRadius: "2px", overflow: "hidden" }}>
                  <div style={{ width: "98%", height: "100%", background: "#34C759", borderRadius: "2px" }} />
                </div>
                <span style={{ fontSize: "11px", color: "#34C759", fontWeight: 600 }}>98%</span>
              </div>
            </div>
          </div>

          {/* Previous verse */}
          <div style={{ padding: "14px 16px", background: "var(--pk-card)", border: "1px solid var(--pk-border)", borderRadius: "12px" }}>
            <p style={{ fontSize: "12px", color: "var(--pk-text-2)", margin: "0 0 6px", fontStyle: "italic" }}>
              &ldquo;I can do all things through Christ who strengthens me.&rdquo;
            </p>
            <div style={{ display: "flex", gap: "8px" }}>
              <span style={{ fontSize: "11px", fontWeight: 600, color: "var(--pk-gold)" }}>Philippians 4:13</span>
              <span style={{ fontSize: "11px", color: "var(--pk-text-3)" }}>· 94%</span>
            </div>
          </div>

          {/* Projector button */}
          <div style={{ padding: "10px 16px", background: "var(--pk-card)", borderRadius: "10px", display: "flex", alignItems: "center", justifyContent: "space-between" }}>
            <span style={{ fontSize: "12px", color: "var(--pk-text-3)" }}>Projector — Display 2</span>
            <span style={{ fontSize: "11px", color: "#34C759", fontWeight: 600 }}>● Connected</span>
          </div>

        </div>
      </div>
    </section>
  );
}

const FAQS = [
  {
    q: "What is PrayerKey?",
    a: "PrayerKey is a free AI-powered church companion that helps pastors, ministers, and believers generate personalised prayers, automatically detect Bible verses during live sermons, and search the entire Bible by keyword, topic, or reference — all without creating an account.",
  },
  {
    q: "How does AI prayer generation work?",
    a: "You type what's on your heart — a worry, a praise, a need — and PrayerKey's AI writes a full, scripture-grounded prayer tailored to your words. It includes relevant Bible verses and an encouragement note. The whole process takes under 10 seconds.",
  },
  {
    q: "Is PrayerKey free to use?",
    a: "Yes. PrayerKey is completely free. There are no subscriptions, no paywalls, and no hidden fees. You can generate prayers, run live sermon sessions, and search the Bible without paying anything.",
  },
  {
    q: "Do I need to create an account or sign in?",
    a: "No. PrayerKey requires no account, no sign-up, and no login. Open the website and start using it immediately. Your privacy is respected — we don't track personal data.",
  },
  {
    q: "What is the Live Sermon feature?",
    a: "The Live Sermon feature listens to the preacher through the microphone and automatically detects Bible verses being quoted or referenced in real time. Detected verses are displayed on a connected projector screen for the congregation to follow along — with no manual typing required.",
  },
  {
    q: "How do I show Bible verses on a projector during a sermon?",
    a: "Start a Live Sermon session, then click 'Open Projector' to open the projector screen in a second browser window or on a second display. As you preach, verses are detected automatically and appear on the projector screen instantly.",
  },
  {
    q: "How accurate is the automatic Bible verse detection?",
    a: "PrayerKey uses advanced AI speech recognition combined with a semantic Bible matching engine. It shows a confidence score (0–100%) for every detected verse. Verses above 90% confidence are highlighted in green. You can set the threshold to only show high-confidence matches.",
  },
  {
    q: "What Bible translations does PrayerKey support?",
    a: "PrayerKey supports 11 major Bible translations including NIV, KJV, ESV, NKJV, NLT, AMP, CSB, NASB, MSG, CEV, and GNT. You can switch translations at any time during a live session.",
  },
  {
    q: "Can I use PrayerKey on my phone or tablet?",
    a: "Yes. PrayerKey is a web application that works on any modern browser — iPhone, Android, tablet, laptop, or desktop. No app download is needed. Simply open the website in your browser.",
  },
  {
    q: "What denominations is PrayerKey for?",
    a: "PrayerKey is denomination-neutral. It is designed to serve all Christian traditions — Catholic, Protestant, Pentecostal, Baptist, Anglican, Methodist, non-denominational, and more. The AI draws from the full canon of scripture without theological bias.",
  },
  {
    q: "Can PrayerKey write prayers for specific situations?",
    a: "Yes. PrayerKey generates prayers for healing, anxiety, grief, marriage, finances, thanksgiving, guidance, protection, strength, forgiveness, and any other situation you describe. The more detail you provide, the more personal and relevant the prayer will be.",
  },
  {
    q: "How does Bible search work?",
    a: "Type a verse reference like 'John 3:16', a keyword like 'faith', or a topic like 'do not fear' into the search box. PrayerKey returns the most relevant Bible verses instantly. You can also click any result to see related cross-reference verses.",
  },
  {
    q: "What are cross-references in PrayerKey?",
    a: "Cross-references are verses from different parts of the Bible that share the same theme, doctrine, or wording as a verse you are reading. PrayerKey automatically shows 4–6 of the strongest cross-references for any verse you look up, helping you study scripture more deeply.",
  },
  {
    q: "Can I copy and share the prayers PrayerKey generates?",
    a: "Yes. Every generated prayer has a 'Copy Prayer' button. You can paste it into a message, bulletin, social media post, or read it aloud during a service. The prayers are yours to use freely.",
  },
  {
    q: "Does PrayerKey work without an internet connection?",
    a: "PrayerKey requires an internet connection for AI prayer generation, live sermon verse detection, and Bible search — since these features use cloud-based AI models. However, the website itself loads quickly on any standard connection including mobile data.",
  },
  {
    q: "How many people can use PrayerKey during a live church service?",
    a: "There is no limit. The pastor controls the live session from one device, and the projector screen can be opened on any number of displays simultaneously. Congregation members can also follow along on their own phones by visiting the projector link.",
  },
  {
    q: "Is PrayerKey suitable for home churches and small groups?",
    a: "Absolutely. PrayerKey works just as well for a home Bible study of 5 people as it does for a church of 5,000. There are no minimum size requirements. The live sermon and prayer features scale to any gathering.",
  },
  {
    q: "What language are the prayers generated in?",
    a: "PrayerKey currently generates prayers in English. The quality and tone reflect the input you provide — so if you write your request in a formal style, the prayer will match. Support for additional languages is planned for future updates.",
  },
  {
    q: "How is PrayerKey different from a regular Bible app?",
    a: "Standard Bible apps let you read and search scripture. PrayerKey goes further — it actively listens during sermons and auto-displays verses on a projector, writes original AI prayers from your personal requests, and connects Bible search with intelligent cross-references. It is a live ministry tool, not just a reading app.",
  },
  {
    q: "Is PrayerKey better than PewBeam?",
    a: "PrayerKey and PewBeam both display Bible verses during sermons, but PrayerKey goes significantly further. PewBeam focuses on manual verse display — a pastor or operator selects verses to push to the screen. PrayerKey does this automatically: it listens to the sermon and detects verses in real time with no manual input. On top of that, PrayerKey adds AI prayer generation, full Bible search with cross-references, 11 translations, and a live projector designer — all completely free with no account required. For churches that want a hands-free, all-in-one tool, PrayerKey is the stronger choice.",
  },
  {
    q: "Can I use PrayerKey for funerals, weddings, or special church ceremonies?",
    a: "Yes. PrayerKey is ideal for any faith ceremony — not just Sunday services. For funerals, the prayer generator can write a compassionate prayer of comfort and remembrance in seconds. For weddings, it can compose a prayer of blessing over the couple. For baptisms, dedications, or special services, just describe the occasion and PrayerKey tailors the prayer to it. The live sermon feature also works during any spoken ceremony where scripture is referenced.",
  },
  {
    q: "Who built PrayerKey?",
    a: "PrayerKey was built by Collins Omoikhudu Asein to give every pastor and church access to AI tools that were previously only available to large, well-resourced ministries. It is an independent platform focused on making technology serve the church — not the other way around.",
  },
];

function FAQ() {
  return (
    <section style={{ marginTop: "80px", textAlign: "left" }}>

      {/* Section header */}
      <div style={{ textAlign: "center", marginBottom: "48px" }}>
        <h2 style={{
          fontSize:      "clamp(26px, 4vw, 40px)",
          fontWeight:    700,
          color:         "var(--pk-text)",
          margin:        "0 0 12px",
          letterSpacing: "-0.02em",
          lineHeight:    1.2,
        }}>
          Frequently Asked Questions
        </h2>
        <p style={{ fontSize: "17px", color: "var(--pk-text-2)", margin: 0 }}>
          Your questions, answered.
        </p>
      </div>

      {/* Two-column grid */}
      <div style={{
        display:             "grid",
        gridTemplateColumns: "repeat(auto-fit, minmax(420px, 1fr))",
        gap:                 "2px",
      }}>
        {FAQS.map((f, i) => (
          <FAQItem key={i} q={f.q} a={f.a} />
        ))}
      </div>
    </section>
  );
}

function FAQItem({ q, a }: { q: string; a: string }) {
  const [open, setOpen] = useState(false);
  return (
    <div
      style={{
        borderBottom:  "1px solid var(--pk-border)",
        padding:       "0",
      }}
    >
      <button
        onClick={() => setOpen((v) => !v)}
        style={{
          width:          "100%",
          textAlign:      "left",
          background:     "none",
          border:         "none",
          cursor:         "pointer",
          padding:        "22px 4px",
          display:        "flex",
          alignItems:     "center",
          justifyContent: "space-between",
          gap:            "16px",
        }}
      >
        <span style={{
          fontSize:      "15px",
          fontWeight:    600,
          color:         "var(--pk-text)",
          lineHeight:    1.4,
          letterSpacing: "-0.01em",
        }}>
          {q}
        </span>
        <span style={{
          fontSize:   "18px",
          color:      "var(--pk-gold)",
          flexShrink: 0,
          transform:  open ? "rotate(45deg)" : "rotate(0deg)",
          transition: "transform 220ms ease",
          lineHeight: 1,
        }}>
          +
        </span>
      </button>

      {open && (
        <p style={{
          fontSize:    "14px",
          color:       "var(--pk-text-2)",
          lineHeight:  1.75,
          margin:      "0",
          padding:     "0 4px 22px",
          animation:   "faqOpen 220ms ease",
        }}>
          {a}
        </p>
      )}
    </div>
  );
}
