package com.prayerkey.manna.data

import com.prayerkey.manna.platform.PlatformContext

private var store: JsonMannaStore? = null

actual fun PlatformContext.mannaStore(): MannaRepository = store ?: JsonMannaStore(this).also { store = it }
