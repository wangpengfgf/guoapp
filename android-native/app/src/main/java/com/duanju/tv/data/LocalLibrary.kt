package com.duanju.tv.data

import android.content.Context
import com.duanju.tv.core.Drama
import com.duanju.tv.core.WatchEntry
import org.json.JSONArray

/** 本地收藏与观看进度，使用 SharedPreferences 持久化。 */
class LocalLibrary(context: Context) {

    private val prefs = context.getSharedPreferences("duanju-tv", Context.MODE_PRIVATE)

    fun history(): List<WatchEntry> {
        val array = readArray(KEY_HISTORY)
        val values = ArrayList<WatchEntry>(array.length())
        for (index in 0 until array.length()) {
            val row = array.optJSONObject(index) ?: continue
            values.add(WatchEntry.fromJson(row))
        }
        return values.sortedByDescending { it.updatedAt }
    }

    fun progress(dramaId: String): WatchEntry? =
        history().firstOrNull { it.drama.id == dramaId }

    fun saveProgress(entry: WatchEntry) {
        val merged = ArrayList<WatchEntry>()
        merged.add(entry)
        merged.addAll(history().filter { it.drama.id != entry.drama.id })
        val array = JSONArray()
        merged.take(MAX_HISTORY).forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
    }

    fun removeProgress(dramaId: String) {
        val array = JSONArray()
        history().filter { it.drama.id != dramaId }.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    fun favorites(): List<Drama> {
        val array = readArray(KEY_FAVORITES)
        val values = ArrayList<Drama>(array.length())
        for (index in 0 until array.length()) {
            val row = array.optJSONObject(index) ?: continue
            values.add(Drama.fromJson(row))
        }
        return values
    }

    fun isFavorite(dramaId: String): Boolean = favorites().any { it.id == dramaId }

    /** 返回切换后的收藏状态：true 表示已收藏。 */
    fun toggleFavorite(drama: Drama): Boolean {
        val current = favorites()
        val exists = current.any { it.id == drama.id }
        val array = JSONArray()
        if (exists) {
            current.filter { it.id != drama.id }.forEach { array.put(it.toJson()) }
        } else {
            array.put(drama.toJson())
            current.forEach { array.put(it.toJson()) }
        }
        prefs.edit().putString(KEY_FAVORITES, array.toString()).apply()
        return !exists
    }

    private fun readArray(key: String): JSONArray = try {
        JSONArray(prefs.getString(key, "[]"))
    } catch (_: Throwable) {
        JSONArray()
    }

    private companion object {
        const val KEY_HISTORY = "history"
        const val KEY_FAVORITES = "favorites"
        const val MAX_HISTORY = 200
    }
}
