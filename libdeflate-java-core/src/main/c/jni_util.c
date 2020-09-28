#include <jni.h>
#include "jni_util.h"

void throwException(JNIEnv *env, const char *type, const char *msg) {
    // We don't cache these, since they will only occur rarely.
    jclass klazz = (*env)->FindClass(env, type);

    if (klazz != 0) {
        (*env)->ThrowNew(env, klazz, msg);
    }
}
