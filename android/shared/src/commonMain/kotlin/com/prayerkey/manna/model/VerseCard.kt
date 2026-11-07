package com.prayerkey.manna.model

data class VerseCard(val reference: String, val translation: String, val verse: String, val receivedBy: String)

/** Whole verses, word for word from the KJV, so every card can be quoted as it stands. */
val DailyVerses = listOf(
    VerseCard("Psalm 23:1", "KJV", "The LORD is my shepherd; I shall not want.", ""),
    VerseCard("Proverbs 3:5-6", "KJV", "Trust in the LORD with all thine heart; and lean not unto thine own understanding. In all thy ways acknowledge him, and he shall direct thy paths.", ""),
    VerseCard("Isaiah 41:10", "KJV", "Fear thou not; for I am with thee: be not dismayed; for I am thy God: I will strengthen thee; yea, I will help thee; yea, I will uphold thee with the right hand of my righteousness.", ""),
    VerseCard("Matthew 11:28", "KJV", "Come unto me, all ye that labour and are heavy laden, and I will give you rest.", ""),
    VerseCard("Philippians 4:13", "KJV", "I can do all things through Christ which strengtheneth me.", ""),
    VerseCard("Jeremiah 29:11", "KJV", "For I know the thoughts that I think toward you, saith the LORD, thoughts of peace, and not of evil, to give you an expected end.", ""),
    VerseCard("Psalm 46:1", "KJV", "God is our refuge and strength, a very present help in trouble.", ""),
    VerseCard("Romans 8:28", "KJV", "And we know that all things work together for good to them that love God, to them who are the called according to his purpose.", ""),
    VerseCard("Isaiah 40:31", "KJV", "But they that wait upon the LORD shall renew their strength; they shall mount up with wings as eagles; they shall run, and not be weary; and they shall walk, and not faint.", ""),
    VerseCard("Joshua 1:9", "KJV", "Have not I commanded thee? Be strong and of a good courage; be not afraid, neither be thou dismayed: for the LORD thy God is with thee whithersoever thou goest.", ""),
    VerseCard("Psalm 118:24", "KJV", "This is the day which the LORD hath made; we will rejoice and be glad in it.", ""),
    VerseCard("Lamentations 3:22-23", "KJV", "It is of the LORD'S mercies that we are not consumed, because his compassions fail not. They are new every morning: great is thy faithfulness.", ""),
    VerseCard("2 Timothy 1:7", "KJV", "For God hath not given us the spirit of fear; but of power, and of love, and of a sound mind.", ""),
    VerseCard("Psalm 91:1", "KJV", "He that dwelleth in the secret place of the most High shall abide under the shadow of the Almighty.", ""),
    VerseCard("John 3:16", "KJV", "For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life.", ""),
    VerseCard("Psalm 121:1-2", "KJV", "I will lift up mine eyes unto the hills, from whence cometh my help. My help cometh from the LORD, which made heaven and earth.", ""),
    VerseCard("Philippians 4:6-7", "KJV", "Be careful for nothing; but in every thing by prayer and supplication with thanksgiving let your requests be made known unto God. And the peace of God, which passeth all understanding, shall keep your hearts and minds through Christ Jesus.", ""),
    VerseCard("Psalm 27:1", "KJV", "The LORD is my light and my salvation; whom shall I fear? the LORD is the strength of my life; of whom shall I be afraid?", ""),
    VerseCard("Psalm 34:18", "KJV", "The LORD is nigh unto them that are of a broken heart; and saveth such as be of a contrite spirit.", ""),
    VerseCard("Isaiah 43:2", "KJV", "When thou passest through the waters, I will be with thee; and through the rivers, they shall not overflow thee: when thou walkest through the fire, thou shalt not be burned; neither shall the flame kindle upon thee.", ""),
    VerseCard("Matthew 6:33", "KJV", "But seek ye first the kingdom of God, and his righteousness; and all these things shall be added unto you.", ""),
    VerseCard("Psalm 30:5", "KJV", "For his anger endureth but a moment; in his favour is life: weeping may endure for a night, but joy cometh in the morning.", ""),
    VerseCard("Romans 8:31", "KJV", "What shall we then say to these things? If God be for us, who can be against us?", ""),
    VerseCard("Zephaniah 3:17", "KJV", "The LORD thy God in the midst of thee is mighty; he will save, he will rejoice over thee with joy; he will rest in his love, he will joy over thee with singing.", ""),
)
