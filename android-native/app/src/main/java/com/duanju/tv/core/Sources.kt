package com.duanju.tv.core

data class SourceSite(val id: String, val name: String, val description: String)

/** 站源清单，与 native/core 中启用的站源保持一致（全站源版本）。 */
object Sources {
    val all: List<SourceSite> = listOf(
        SourceSite("hongguo", "红果", "短剧 · 漫剧 · AI 剧"),
        SourceSite("yaguo", "芽果", "星芽短剧 · 登录接口"),
        SourceSite("maoguo", "猫果", "七猫短剧 · 签名接口"),
        SourceSite("fanguo", "饭果", "西饭短剧 · 搜索接口"),
        SourceSite("guanguo", "观果", "围观短剧 · 分类接口"),
        SourceSite("heguo", "河果", "河马剧场 · 网页接口"),
        SourceSite("xingguo", "星果", "星星短剧 · 连载接口"),
        SourceSite("huaguo", "花果", "花生短剧 · 网页目录"),
        SourceSite("niuguo", "牛果", "牛牛短剧 · 分类接口"),
        SourceSite("wangguo", "网果", "短剧网站 · 网页目录"),
        SourceSite("faguo", "发果", "168 短剧 · 网页目录"),
        SourceSite("piguo", "皮果", "PTT 短剧 · 网页目录"),
        SourceSite("wuguo", "伍果", "五五短剧 · 网页目录"),
    )

    fun nameOf(id: String): String = all.firstOrNull { it.id == id }?.name ?: id
}
