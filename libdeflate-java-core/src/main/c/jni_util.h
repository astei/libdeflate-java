#ifndef JNIUTIL_H
#define JNIUTIL_H

#include <jni.h>

#ifndef _WIN32
  #define LIBDEFLATEJAVA_PUBLIC __attribute__((visibility("default")))
#else
  #define LIBDEFLATEJAVA_PUBLIC
#endif

void throwException(JNIEnv *env, const char *type, const char *msg);

#endif
