package com.duanju.tv

import android.content.Context
import com.duanju.tv.core.CoreRepository
import com.duanju.tv.data.LocalLibrary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object App {
    @Volatile
    private var ready = false

    lateinit var repository: CoreRepository
        private set

    lateinit var library: LocalLibrary
        private set

    /** 用于退出页面后仍需完成的收尾请求（释放播放会话等）。 */
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun init(context: Context) {
        if (ready) return
        synchronized(this) {
            if (ready) return
            repository = CoreRepository(context.filesDir.absolutePath)
            library = LocalLibrary(context)
            ready = true
        }
    }
}
