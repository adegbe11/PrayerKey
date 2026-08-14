package com.prayerkey.manna.ui.church

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object SermonSessionStore {
    fun directory(context: Context) = File(context.filesDir, "sermons").apply { mkdirs() }
    fun metadataFor(audio: File) = File(audio.parentFile, audio.nameWithoutExtension + ".json")
    fun write(audio: File, startedAt: Long, status: String, segments: List<SermonSegment>) {
        val body = JSONObject().put("audio", audio.absolutePath).put("started_at", startedAt).put("status", status)
            .put("segments", JSONArray().apply { segments.forEach { put(JSONObject().put("start_ms", it.startMs).put("end_ms", it.endMs).put("text", it.text)) } })
        metadataFor(audio).writeText(body.toString())
    }
    fun recoverable(context: Context): List<File> = directory(context).listFiles { f -> f.extension == "json" && runCatching { JSONObject(f.readText()).optString("status") == "recording" }.getOrDefault(false) }?.toList().orEmpty()
}
