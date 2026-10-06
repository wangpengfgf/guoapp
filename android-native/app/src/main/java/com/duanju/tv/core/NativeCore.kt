package com.duanju.tv.core

/**
 * Thin JNI bridge to the Go core built from `native/` (see scripts/build_native.py).
 *
 * Every 站源 capability is exposed by a single JSON entry point:
 * `DuanjuRequest({"action": ...}) -> {"ok": true, "data": {...}}`.
 */
class NativeCore private constructor() {

    companion object {
        init {
            System.loadLibrary("duanju_core")
            System.loadLibrary("duanju_jni")
        }

        @JvmStatic
        private external fun request(json: String): String

        fun call(json: String): String = request(json)
    }
}
