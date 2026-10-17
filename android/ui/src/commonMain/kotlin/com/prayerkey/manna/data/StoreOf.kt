package com.prayerkey.manna.data

import com.prayerkey.manna.platform.PlatformContext

/** The one store for the process: Android's SQLite MannaStore, or the iPhone's JsonMannaStore. */
expect fun PlatformContext.mannaStore(): MannaRepository
