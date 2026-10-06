package com.duanju.tv.core

import org.json.JSONArray
import org.json.JSONObject

data class CatalogCategory(val id: String, val name: String)

data class Drama(
    val id: String,
    val source: String,
    val sourceId: String,
    val title: String,
    val description: String,
    val cover: String,
    val episodes: Int,
    val category: String,
    val vip: Boolean?,
    val heat: String,
    val views: String,
    val onlineDate: String,
    val tags: List<String>,
    val releaseStatus: String,
) {
    fun toJson(): JSONObject = JSONObject()
        .put("metadataSchema", 1)
        .put("id", id)
        .put("source", source)
        .put("sourceId", sourceId)
        .put("title", title)
        .put("description", description)
        .put("cover", cover)
        .put("episodes", episodes)
        .put("category", category)
        .put("vip", vip)
        .put("heat", heat)
        .put("views", views)
        .put("onlineDate", onlineDate)
        .put("tags", JSONArray(tags))
        .put("releaseStatus", releaseStatus)

    companion object {
        fun fromJson(json: JSONObject): Drama = Drama(
            id = json.optString("id"),
            source = json.optString("source", "hongguo"),
            sourceId = json.optString("sourceId"),
            title = json.optString("title", "短剧"),
            description = json.optString("description"),
            cover = json.optString("cover"),
            episodes = json.optInt("episodes"),
            category = json.optString("category"),
            vip = if (json.has("vip") && !json.isNull("vip")) json.optBoolean("vip") else null,
            heat = json.opt("heat")?.toString().orEmpty(),
            views = json.opt("views")?.toString().orEmpty(),
            onlineDate = json.optString("onlineDate"),
            tags = json.optJSONArray("tags").toStringList(),
            releaseStatus = json.optString("releaseStatus"),
        )
    }
}

data class Episode(
    val raw: JSONObject,
    val id: String,
    val title: String,
    val number: Int,
) {
    companion object {
        fun fromJson(json: JSONObject, fallback: Int): Episode {
            val current = json.optInt("currentEpisode")
            return Episode(
                raw = json,
                id = json.optString("id"),
                title = json.optString("title").ifEmpty { "第${fallback}集" },
                number = if (current > 0) current else fallback,
            )
        }
    }
}

data class DramaDetail(
    val drama: Drama,
    val episodes: List<Episode>,
    val warning: String,
) {
    companion object {
        fun fromJson(json: JSONObject): DramaDetail {
            val drama = Drama.fromJson(json.optJSONObject("drama") ?: JSONObject())
            val rows = json.optJSONArray("chapters") ?: JSONArray()
            val items = ArrayList<Episode>(rows.length())
            for (index in 0 until rows.length()) {
                val row = rows.optJSONObject(index) ?: continue
                items.add(Episode.fromJson(row, index + 1))
            }
            return DramaDetail(drama, items, json.optString("warning"))
        }
    }
}

data class CatalogPage(
    val items: List<Drama>,
    val hasMore: Boolean,
    val page: Int,
    val warning: String,
) {
    companion object {
        fun fromJson(json: JSONObject): CatalogPage {
            val rows = json.optJSONArray("items") ?: JSONArray()
            val items = ArrayList<Drama>(rows.length())
            for (index in 0 until rows.length()) {
                val row = rows.optJSONObject(index) ?: continue
                items.add(Drama.fromJson(row))
            }
            return CatalogPage(
                items = items,
                hasMore = json.optBoolean("hasMore"),
                page = json.optInt("page", 1).coerceAtLeast(1),
                warning = json.optString("warning"),
            )
        }
    }
}

data class PlaybackPlan(
    val url: String,
    val headers: Map<String, String>,
    val session: String,
    val quality: Int,
    val qualities: List<Int>,
    val local: Boolean,
) {
    val hasNextRoute: Boolean
        get() = session.isNotEmpty()

    companion object {
        fun fromJson(json: JSONObject): PlaybackPlan {
            val headers = LinkedHashMap<String, String>()
            json.optJSONObject("headers")?.let { obj ->
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    headers[key] = obj.opt(key)?.toString().orEmpty()
                }
            }
            val qualities = ArrayList<Int>()
            json.optJSONArray("qualities")?.let { array ->
                for (index in 0 until array.length()) {
                    qualities.add(array.optInt(index))
                }
            }
            return PlaybackPlan(
                url = json.optString("url"),
                headers = headers,
                session = json.optString("session"),
                quality = json.optInt("quality"),
                qualities = qualities.distinct().sortedDescending(),
                local = json.optBoolean("local"),
            )
        }
    }
}

data class WatchEntry(
    val drama: Drama,
    val episode: Int,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long,
) {
    fun toJson(): JSONObject = JSONObject()
        .put("drama", drama.toJson())
        .put("episode", episode)
        .put("positionMs", positionMs)
        .put("durationMs", durationMs)
        .put("updatedAt", updatedAt)

    companion object {
        fun fromJson(json: JSONObject): WatchEntry = WatchEntry(
            drama = Drama.fromJson(json.optJSONObject("drama") ?: JSONObject()),
            episode = json.optInt("episode"),
            positionMs = json.optLong("positionMs"),
            durationMs = json.optLong("durationMs"),
            updatedAt = json.optLong("updatedAt"),
        )
    }
}

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    val values = ArrayList<String>(length())
    for (index in 0 until length()) {
        val value = optString(index)
        if (value.isNotEmpty()) values.add(value)
    }
    return values
}
