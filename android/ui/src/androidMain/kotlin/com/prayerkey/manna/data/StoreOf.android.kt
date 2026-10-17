package com.prayerkey.manna.data

import com.prayerkey.manna.platform.PlatformContext

actual fun PlatformContext.mannaStore(): MannaRepository = MannaStore.of(this)
