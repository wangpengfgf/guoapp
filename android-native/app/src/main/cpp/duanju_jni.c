#include <jni.h>

// Exported by libduanju_core.so (Go, built with -buildmode=c-shared).
// Declared here instead of including the generated header so CMake does not
// need the shared library to be present at configure time.
extern char *DuanjuRequest(char *input);
extern void DuanjuFree(char *value);

static jstring duanju_call(JNIEnv *env, jstring input) {
    if (input == NULL) {
        return NULL;
    }
    const char *raw = (*env)->GetStringUTFChars(env, input, NULL);
    if (raw == NULL) {
        return NULL;
    }
    char *output = DuanjuRequest((char *) raw);
    (*env)->ReleaseStringUTFChars(env, input, raw);
    if (output == NULL) {
        return NULL;
    }
    jstring result = (*env)->NewStringUTF(env, output);
    DuanjuFree(output);
    return result;
}

// Kotlin 的 @JvmStatic external 会同时在 NativeCore 和 NativeCore$Companion 上
// 生成 native 声明，两个符号都必须存在，否则会抛 "No implementation found"。
JNIEXPORT jstring JNICALL
Java_com_duanju_tv_core_NativeCore_request(JNIEnv *env, jclass clazz, jstring input) {
    (void) clazz;
    return duanju_call(env, input);
}

JNIEXPORT jstring JNICALL
Java_com_duanju_tv_core_NativeCore_00024Companion_request(JNIEnv *env, jobject self, jstring input) {
    (void) self;
    return duanju_call(env, input);
}
