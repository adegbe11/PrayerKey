package com.prayerkey.manna.model

data class VerseCard(val reference: String, val translation: String, val verse: String, val receivedBy: String)

val DailyVerses = listOf(
    VerseCard("Psalm 23:1", "KJV", "The Lord is my shepherd;\nI shall not want.", "214,532"),
    VerseCard("Proverbs 3:5–6", "KJV", "Trust in the Lord with all thine heart; and lean not unto thine own understanding.", "188,342"),
    VerseCard("Isaiah 41:10", "KJV", "Fear thou not; for I am with thee: be not dismayed; for I am thy God.", "251,914"),
    VerseCard("Matthew 11:28", "KJV", "Come unto me, all ye that labour and are heavy laden, and I will give you rest.", "199,650"),
)
