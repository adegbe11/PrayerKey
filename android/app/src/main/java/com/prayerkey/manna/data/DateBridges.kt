package com.prayerkey.manna.data

import kotlinx.datetime.toKotlinLocalDate
import java.time.LocalDate as JLocalDate

/*
 * The shared code uses the cross-platform date (kotlinx-datetime) so it can
 * run on iPhone. The Android app still works in java.time, so these overloads
 * take a java.time date and hand the shared code its own kind.
 */
fun dailyPrayerFor(date: JLocalDate, topics: List<PrayerTopic>) = dailyPrayerFor(date.toKotlinLocalDate(), topics)
fun passageFor(date: JLocalDate) = passageFor(date.toKotlinLocalDate())
fun prayerPointsFor(date: JLocalDate, reference: String) = prayerPointsFor(date.toKotlinLocalDate(), reference)
fun quoteFor(date: JLocalDate) = quoteFor(date.toKotlinLocalDate())
fun hourPrayer(hour: String, date: JLocalDate) = hourPrayer(hour, date.toKotlinLocalDate())
