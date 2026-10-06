#include <jni.h>

// Exported by libduanju_core.so (Go, built with -buildmode=c-shared).
// Declared here instead of including the generated header so CMake does not
// need the shared library to be present at configure time.
extern char *DuanjuRequest(char *input);
extern void DuanjuFree(char *value);

JNIEXPORT jstring JNICALL
Java_com_duanju_tv_core_NativeCore_request(JNIEnv *env, jclass clazz, jstring input) {
    (void) clazz;
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
