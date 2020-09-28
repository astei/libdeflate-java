#include <jni.h>
#include "./jni_util.h"
#include "./libdeflate/libdeflate.h"

LIBDEFLATEJAVA_PUBLIC JNIEXPORT jint JNICALL Java_me_steinborn_libdeflate_LibdeflateCRC32_crc32Heap
  (JNIEnv *env, jclass klass, jlong crc32, jbyteArray array, jint off, jint len)
{
    jbyte *arrayBytes = (*env)->GetPrimitiveArrayCritical(env, array, 0);
    if (arrayBytes) {
        crc32 = (jint) libdeflate_crc32((uint32_t) crc32, (void*) (arrayBytes + off), len);
        (*env)->ReleasePrimitiveArrayCritical(env, array, arrayBytes, JNI_ABORT);
    }
    return crc32;
}

LIBDEFLATEJAVA_PUBLIC JNIEXPORT jint JNICALL Java_me_steinborn_libdeflate_LibdeflateCRC32_crc32Direct
  (JNIEnv *env, jclass klass, jlong crc32, jobject buf, jint off, jint len)
{
    jbyte *bufBytes = (*env)->GetDirectBufferAddress(env, buf);

    if (bufBytes == NULL) {
        throwException(env, "java/lang/IllegalArgumentException", "unable to obtain direct access to buffer");
        return -1;
    }

    return (jint) libdeflate_crc32((uint32_t) crc32, (void*) (bufBytes + off), len);
}

LIBDEFLATEJAVA_PUBLIC JNIEXPORT jint JNICALL Java_me_steinborn_libdeflate_LibdeflateAdler32_adler32Heap
  (JNIEnv *env, jclass klass, jlong adler32, jbyteArray array, jint off, jint len)
{
    jbyte *arrayBytes = (*env)->GetPrimitiveArrayCritical(env, array, 0);
    if (arrayBytes) {
        adler32 = (jint) libdeflate_adler32((uint32_t) adler32, (void*) (arrayBytes + off), len);
        (*env)->ReleasePrimitiveArrayCritical(env, array, arrayBytes, JNI_ABORT);
    }
    return adler32;
}

LIBDEFLATEJAVA_PUBLIC JNIEXPORT jint JNICALL Java_me_steinborn_libdeflate_LibdeflateAdler32_adler32Direct
  (JNIEnv *env, jclass klass, jlong adler32, jobject buf, jint off, jint len)
{
    jbyte *bufBytes = (*env)->GetDirectBufferAddress(env, buf);

    if (bufBytes == NULL) {
        throwException(env, "java/lang/IllegalArgumentException", "unable to obtain direct access to buffer");
        return -1;
    }

    return (jint) libdeflate_adler32((uint32_t) adler32, (void*) (bufBytes + off), len);
}
