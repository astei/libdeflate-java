#include <jni.h>

#ifndef JNIUTIL_H
#define JNIUTIL_H

#define LIBDEFLATEJAVA_PUBLIC __attribute__((visibility("default")))

JNIEXPORT void JNICALL
throwException(JNIEnv *env, const char *type, const char *msg);

#endif
