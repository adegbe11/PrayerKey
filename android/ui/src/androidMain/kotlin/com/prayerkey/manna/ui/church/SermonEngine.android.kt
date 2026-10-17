package com.prayerkey.manna.ui.church

/** Android installs its SermonService engine at launch (AndroidSermonEngine); this is the stand-in before that. */
actual fun defaultSermonEngine(): SermonEngine = SilentSermonEngine
