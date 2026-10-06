package com.duanju.tv.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject

class CoreFailure(message: String) : Exception(message)

/**
 * 新的原生客户端与 guoapp 站源核心之间的唯一通道。
 * 所有请求/响应都是 JSON，字段与 lib/core_bridge.dart 完全一致。
 */
class CoreRepository(private val directory: String) {

    private val initMutex = Mutex()

    @Volatile
    private var initialized = false

    private var readSequence = 0L
    private var playSequence = System.currentTimeMillis()

    private fun blocking(input: JSONObject): JSONObject {
        val response = try {
            NativeCore.call(input.toString())
        } catch (error: Throwable) {
            throw CoreFailure(error.message ?: "本地核心加载失败，请重新安装完整包")
        }
        val parsed = try {
            JSONObject(response)
        } catch (error: Throwable) {
            throw CoreFailure("本地核心返回了无法识别的结果")
        }
        if (!parsed.optBoolean("ok")) {
            throw CoreFailure(parsed.optString("error", "读取失败，请重试"))
        }
        return parsed.optJSONObject("data") ?: JSONObject()
    }

    private suspend fun call(input: JSONObject): JSONObject =
        withContext(Dispatchers.IO) { blocking(input) }

    private fun read(scope: String, input: JSONObject): JSONObject = input
        .put("session", "android:$scope")
        .put("sequence", ++readSequence)

    suspend fun ensureInitialized(): Boolean {
        if (initialized) return true
        initMutex.withLock {
            if (initialized) return true
            val data = call(JSONObject().put("action", "initialize").put("directory", directory))
            if (!data.optBoolean("allSources")) {
                throw CoreFailure("应用与站源版本不一致，请安装全站源版本")
            }
            initialized = true
        }
        return true
    }

    suspend fun catalog(
        source: String,
        page: Int = 1,
        query: String = "",
        category: String = "",
        force: Boolean = false,
    ): CatalogPage {
        ensureInitialized()
        val input = read(
            "catalog-$source",
            JSONObject()
                .put("action", "catalog")
                .put("source", source)
                .put("page", page)
                .put("query", query)
                .put("category", category)
                .put("force", force),
        )
        return CatalogPage.fromJson(call(input))
    }

    suspend fun categories(source: String, force: Boolean = false): List<CatalogCategory> {
        ensureInitialized()
        val input = read(
            "categories-$source",
            JSONObject()
                .put("action", "categories")
                .put("source", source)
                .put("force", force),
        )
        val result = call(input)
        val rows = result.optJSONArray("items") ?: return emptyList()
        val values = ArrayList<CatalogCategory>(rows.length())
        for (index in 0 until rows.length()) {
            val row = rows.optJSONObject(index) ?: continue
            values.add(CatalogCategory(row.optString("id"), row.optString("name", "全部")))
        }
        return values
    }

    suspend fun detail(drama: Drama): DramaDetail {
        ensureInitialized()
        return DramaDetail.fromJson(
            call(JSONObject().put("action", "detail").put("drama", drama.toJson())),
        )
    }

    suspend fun cover(drama: Drama): String {
        ensureInitialized()
        val result = call(JSONObject().put("action", "cover").put("drama", drama.toJson()))
        return result.optString("path")
    }

    suspend fun resolve(drama: Drama, episode: Episode, quality: Int = 0): PlaybackPlan {
        ensureInitialized()
        return PlaybackPlan.fromJson(
            call(
                JSONObject()
                    .put("action", "resolve")
                    .put("drama", drama.toJson())
                    .put("chapter", episode.raw)
                    .put("index", episode.number)
                    .put("quality", quality)
                    .put("sequence", ++playSequence),
            ),
        )
    }

    suspend fun release(session: String) {
        if (session.isEmpty()) return
        try {
            call(JSONObject().put("action", "release").put("session", session))
        } catch (_: Throwable) {
        }
    }

    suspend fun cancelPlayback() {
        try {
            call(JSONObject().put("action", "cancelPlayback").put("sequence", ++playSequence))
        } catch (_: Throwable) {
        }
    }
}
