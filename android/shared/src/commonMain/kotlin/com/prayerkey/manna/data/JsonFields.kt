package com.prayerkey.manna.data

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/*
 * The handful of reads the asset loaders need, named as org.json named them
 * so the parsing code still reads the way it did on Android: get* throws when
 * the field is missing, opt* falls back.
 */
internal fun JsonObject.getString(key: String): String = getValue(key).jsonPrimitive.content
internal fun JsonObject.optString(key: String, fallback: String = ""): String =
    (this[key] as? JsonPrimitive)?.contentOrNull ?: fallback
internal fun JsonObject.getInt(key: String): Int = getValue(key).jsonPrimitive.int
internal fun JsonObject.optInt(key: String, fallback: Int): Int = (this[key] as? JsonPrimitive)?.intOrNull ?: fallback
internal fun JsonObject.getObject(key: String): JsonObject = getValue(key).jsonObject
internal fun JsonObject.getArray(key: String): JsonArray = getValue(key).jsonArray
internal fun JsonObject.optArray(key: String): JsonArray? = this[key] as? JsonArray
internal fun JsonObject.has(key: String): Boolean = key in this
