#include <jni.h>

#define LIBDEFLATEJAVA_PUBLIC __attribute__((visibility("default")))

JNIEXPORT void JNICALL
throwException(JNIEnv *env, const char *type, const char *msg);