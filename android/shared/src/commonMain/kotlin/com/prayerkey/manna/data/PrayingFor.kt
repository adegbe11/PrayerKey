package com.prayerkey.manna.data

import com.prayerkey.manna.platform.PlatformContext
import com.prayerkey.manna.platform.keyValues
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * The people someone has promised to pray for.
 *
 * "I'll pray for you" is said a great deal and kept rather less, mostly
 * because it is forgotten by the evening. This is the list that remembers:
 * a name, what they asked for, every day it was prayed, and the day God
 * answered. Family is marked, so the family altar can pray for its own.
 */
@Serializable
data class PrayerPerson(
    val id: Long,
    val name: String,
    val request: String = "",
    val family: Boolean = false,
    /** When it was added, ms. */
    val since: Long = 0,
    /** Days it was prayed, as days since 1970. */
    val prayed: List<Long> = emptyList(),
    /** When God answered, ms; 0 while still waiting. */
    val answered: Long = 0,
    val answerNote: String = "",
) {
    fun prayedOn(day: Long) = day in prayed
}

object PrayingFor {
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(PrayerPerson.serializer())
    private val _people = MutableStateFlow<List<PrayerPerson>>(emptyList())
    val people: StateFlow<List<PrayerPerson>> = _people.asStateFlow()
    private var loaded = false

    fun load(context: PlatformContext): List<PrayerPerson> {
        if (loaded) return _people.value
        val raw = context.keyValues("praying_for").getString("all", "").orEmpty()
        _people.value = runCatching { json.decodeFromString(serializer, raw) }.getOrDefault(emptyList())
        loaded = true
        return _people.value
    }

    private fun save(context: PlatformContext, list: List<PrayerPerson>) {
        _people.value = list
        context.keyValues("praying_for").edit().putString("all", json.encodeToString(serializer, list)).apply()
    }

    private fun change(context: PlatformContext, id: Long, f: (PrayerPerson) -> PrayerPerson) {
        load(context)
        save(context, _people.value.map { if (it.id == id) f(it) else it })
    }

    fun add(context: PlatformContext, name: String, request: String, family: Boolean, now: Long) {
        load(context)
        val person = PrayerPerson(id = now, name = name.trim(), request = request.trim(), family = family, since = now)
        save(context, listOf(person) + _people.value)
    }

    fun edit(context: PlatformContext, id: Long, name: String, request: String, family: Boolean) =
        change(context, id) { it.copy(name = name.trim(), request = request.trim(), family = family) }

    fun remove(context: PlatformContext, id: Long) {
        load(context)
        save(context, _people.value.filterNot { it.id == id })
    }

    /** Marks today as prayed for this person; praying for someone is a day with God. */
    fun prayed(context: PlatformContext, id: Long, day: Long) {
        change(context, id) { if (it.prayedOn(day)) it else it.copy(prayed = (it.prayed + day).takeLast(400)) }
        runCatching { Walk.mark(context) }
    }

    fun answered(context: PlatformContext, id: Long, note: String, now: Long) =
        change(context, id) { it.copy(answered = now, answerNote = note.trim()) }

    fun reopen(context: PlatformContext, id: Long) =
        change(context, id) { it.copy(answered = 0, answerNote = "") }

    /**
     * The message that tells someone they were prayed for, ready for WhatsApp.
     * The blessing is Numbers 6:24-26, King James Version, word for word.
     */
    fun message(person: PrayerPerson): String = buildString {
        // the request is written about them ("her healing"), so it stays private
        append("${person.name}, I prayed for you today.\n\n")
        append("The LORD bless thee, and keep thee: The LORD make his face shine upon thee, and be gracious unto thee: ")
        append("The LORD lift up his countenance upon thee, and give thee peace.\nNumbers 6:24-26\n\n")
        append("I pray with PrayerKey: https://www.prayerkey.com")
    }
}
