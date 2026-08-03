package com.prayerkey.manna

import com.prayerkey.manna.ui.screens.PILGRIMAGES
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PilgrimageCatalogTest {
    @Test fun `catalog includes short medium and long journeys`() {
        assertTrue(PILGRIMAGES.count { it.days.size == 7 } >= 3)
        assertEquals(21, PILGRIMAGES.first { it.id == "return" }.days.size)
        assertEquals(40, PILGRIMAGES.first { it.id == "jesus40" }.days.size)
    }

    @Test fun `every pilgrimage day is actionable and scriptural`() {
        PILGRIMAGES.flatMap { it.days }.forEach {
            assertTrue(it.reference.isNotBlank())
            assertTrue(it.reflection.length > 30)
            assertTrue(it.action.endsWith("."))
        }
    }
}
