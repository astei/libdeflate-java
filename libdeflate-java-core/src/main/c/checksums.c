/*
 * Copyright 2024 Andrew Steinborn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include "./jni_util.h"
#include "./libdeflate/libdeflate.h"

LIBDEFLATEJAVA_PUBLIC JNIEXPORT jint JNICALL
Java_me_steinborn_libdeflate_LibdeflateCRC32_crc32Heap(JNIEnv *env,
                                                       jclass klass,
                                                       jlong crc32,
                                                       jbyteArray array,
                                                       jint off, jint len) {
  jbyte *arrayBytes = (*env)->GetPrimitiveArrayCritical(env, array, 0);
  if (arrayBytes) {
    crc32 = (jint)libdeflate_crc32((uint32_t)crc32, (void *)(arrayBytes + off),
                                   len);
    (*env)->ReleasePrimitiveArrayCritical(env, array, arrayBytes, JNI_ABORT);
  }
  return crc32;
}

LIBDEFLATEJAVA_PUBLIC JNIEXPORT jint JNICALL
Java_me_steinborn_libdeflate_LibdeflateCRC32_crc32Direct(
    JNIEnv *env, jclass klass, jlong crc32, jobject buf, jint off, jint len) {
  jbyte *bufBytes = (*env)->GetDirectBufferAddress(env, buf);

  if (bufBytes == NULL) {
    throwException(env, "java/lang/IllegalArgumentException",
                   "unable to obtain direct access to buffer");
    return -1;
  }

  return (jint)libdeflate_crc32((uint32_t)crc32, (void *)(bufBytes + off), len);
}

LIBDEFLATEJAVA_PUBLIC JNIEXPORT jint JNICALL
Java_me_steinborn_libdeflate_LibdeflateAdler32_adler32Heap(JNIEnv *env,
                                                           jclass klass,
                                                           jlong adler32,
                                                           jbyteArray array,
                                                           jint off, jint len) {
  jbyte *arrayBytes = (*env)->GetPrimitiveArrayCritical(env, array, 0);
  if (arrayBytes) {
    adler32 = (jint)libdeflate_adler32((uint32_t)adler32,
                                       (void *)(arrayBytes + off), len);
    (*env)->ReleasePrimitiveArrayCritical(env, array, arrayBytes, JNI_ABORT);
  }
  return adler32;
}

LIBDEFLATEJAVA_PUBLIC JNIEXPORT jint JNICALL
Java_me_steinborn_libdeflate_LibdeflateAdler32_adler32Direct(
    JNIEnv *env, jclass klass, jlong adler32, jobject buf, jint off, jint len) {
  jbyte *bufBytes = (*env)->GetDirectBufferAddress(env, buf);

  if (bufBytes == NULL) {
    throwException(env, "java/lang/IllegalArgumentException",
                   "unable to obtain direct access to buffer");
    return -1;
  }

  return (jint)libdeflate_adler32((uint32_t)adler32, (void *)(bufBytes + off),
                                  len);
}
