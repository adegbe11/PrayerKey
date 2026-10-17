package com.prayerkey.manna.platform

/** On iPhone there is no Context; the app passes this one object where Android passes its Context. */
actual abstract class PlatformContext

object AppContext : PlatformContext()
