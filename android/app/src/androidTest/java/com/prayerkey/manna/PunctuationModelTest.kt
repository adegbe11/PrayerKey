package com.prayerkey.manna

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.prayerkey.manna.ui.church.SermonPunctuator
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The punctuation model, on a device, against the kind of thing a recogniser
 * actually emits: lowercase, unpunctuated, one long breath.
 */
@RunWith(AndroidJUnit4::class)
class PunctuationModelTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val samples = listOf(
        "and jesus said unto peter i am the way the truth and the life no man cometh unto the father but by me",
        "now turn with me to romans chapter eight and let us read together",
        "who do you say that i am",
        "but god had another plan for israel and he was not finished with them",
        "the holy spirit will teach you all things and bring all things to your remembrance",
        "when you are in trouble when the money is gone when the doctor has spoken god is still god",
    )

    @Test fun the_model_loads() {
        SermonPunctuator.prepare(context)
        assertTrue("the punctuation model did not load", SermonPunctuator.isReady)
    }

    @Test fun it_punctuates_inside_the_sentence() {
        SermonPunctuator.prepare(context)
        samples.forEach { raw ->
            val out = SermonPunctuator.punctuate(context, raw)
            println("RAW: $raw")
            println("OUT: $out")
            assertTrue(out, out.first().isUpperCase())
            assertTrue(out, out.last() in ".!?")
        }
    }

    @Test fun the_names_still_get_capitalised() {
        SermonPunctuator.prepare(context)
        val out = SermonPunctuator.punctuate(context, "and jesus said unto peter follow me")
        assertTrue(out, out.contains("Jesus"))
        assertTrue(out, out.contains("Peter"))
    }

    @Test fun a_question_stays_a_question() {
        SermonPunctuator.prepare(context)
        val out = SermonPunctuator.punctuate(context, "who do you say that i am")
        assertTrue(out, out.contains("I"))
        assertTrue(out, out.endsWith("?"))
    }
}
