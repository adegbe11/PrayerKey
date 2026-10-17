package com.prayerkey.manna.platform

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/** On iPhone there is no Context; the app passes this one object where Android passes its Context. */
actual abstract class PlatformContext

object AppContext : PlatformContext()

actual val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
