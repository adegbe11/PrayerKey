package com.prayerkey.manna.data

import java.time.LocalDate

private val powerfulPrayerPoints = listOf(
    "Father, I thank You because Your power is greater than every battle before me.",
    "Lord, have mercy on me and wash away every sin that gives darkness a foothold in my life.",
    "By the blood of Jesus, let every accusation raised against me be silenced.",
    "Holy Spirit, fill me afresh and set every cold place in my spiritual life on fire.",
    "Every power assigned to weaken my prayer life, lose your hold over me in Jesus’ name.",
    "Every chain of fear, shame, and condemnation around my mind, break now in Jesus’ name.",
    "Lord, expose every hidden trap prepared against my progress and lead me safely around it.",
    "Every pattern of repeated failure in my life, end by the power of God in Jesus’ name.",
    "Every voice speaking defeat over my future, be silenced by the truth of God.",
    "Father, separate me from every influence that pulls me away from Your purpose.",
    "Every burden that entered my life through past mistakes, receive the mercy of God and be lifted.",
    "Lord, restore every good opportunity I lost through fear, delay, or wrong decisions.",
    "Every spirit of confusion troubling my decisions, depart from me in Jesus’ name.",
    "Father, give me clear direction and make every crooked path before me straight.",
    "Every closed door that agrees with God’s will for me, open by divine favour.",
    "Every door designed to destroy my peace or purpose, remain permanently closed.",
    "Lord, bless the work of my hands and deliver me from fruitless labour.",
    "Every power consuming my time without producing progress, lose your influence over me.",
    "Father, connect me with honest people who will strengthen Your purpose for my life.",
    "Every harmful relationship draining my faith and peace, be removed with wisdom and grace.",
    "Lord, protect my home and let no evil find a resting place around my family.",
    "Every plan of darkness against my household, scatter by the authority of Jesus.",
    "Father, place Your angels around my going out and my coming in.",
    "Every arrow of sickness, weakness, and affliction directed at me, fail in Jesus’ name.",
    "Lord, release healing into every part of my body and renew my strength.",
    "Every anxious thought stealing my sleep and peace, surrender to the peace of Christ.",
    "Father, deliver me from discouragement and restore my desire to move forward.",
    "Every seed of bitterness, anger, and unforgiveness in my heart, be uprooted.",
    "Lord, give me the grace to forgive without returning to harmful patterns.",
    "Every temptation designed to pull me backward, lose your power over my choices.",
    "Holy Spirit, strengthen my discipline and make me faithful when no one is watching.",
    "Every lie I have believed about myself, be replaced by what God says about me.",
    "Father, awaken every gift You placed inside me and teach me to use it well.",
    "Every force resisting the good work God began in me, give way in Jesus’ name.",
    "Lord, redeem my wasted years and turn painful lessons into wisdom.",
    "Every cycle of rising and falling, break; establish me in steady growth.",
    "Father, give me favour that cannot be explained by human effort alone.",
    "Every financial leak and habit of waste in my life, be exposed and corrected.",
    "Lord, provide for my needs and give me wisdom to manage every resource faithfully.",
    "Every honest effort awaiting recognition, receive visibility at the right time.",
    "Father, fight the battles I cannot see and give me wisdom for the battles I must face.",
    "Every conspiracy built on lies against me, collapse under the light of truth.",
    "Lord, let those who misunderstand me encounter the evidence of Your work in my life.",
    "Every delay that is not from God, come to an end in Jesus’ name.",
    "Father, prepare me so that the blessing I receive will not destroy my character.",
    "Every good thing in my life that has become dry, receive fresh life from God.",
    "Lord, make me spiritually alert and sensitive to Your warnings and instructions.",
    "Every dream, plan, and ambition outside God’s will, lose its attraction to me.",
    "Father, let my life produce undeniable evidence of Your mercy and faithfulness.",
    "According to Your Word today, establish me in peace, obedience, protection, and victory in Jesus’ name. Amen.",
)

/** Exactly fifty prayers, date-stable but rotated daily so each day opens differently. */
fun prayerPointsFor(date: LocalDate, reference: String): List<String> {
    val shift = Math.floorMod(date.toEpochDay().toInt() * 17, powerfulPrayerPoints.size)
    val rotated = List(powerfulPrayerPoints.size) { powerfulPrayerPoints[(it + shift) % powerfulPrayerPoints.size] }
    return rotated.mapIndexed { index, point ->
        if (index == rotated.lastIndex) point.replace("Your Word today", "Your Word in $reference") else point
    }
}
