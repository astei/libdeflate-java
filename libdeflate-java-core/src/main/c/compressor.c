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
#include "./common.h"
#include "./jni_util.h"
#include "./libdeflate/libdeflate.h"

LIBDEFLATEJAVA_PUBLIC JNIEXPORT jlong JNICALL
Java_me_steinborn_libdeflate_LibdeflateCompressor_allocate(JNIEnv *env,
                                                           jclass klass,
                                                           jint level) {
  struct libdeflate_compressor *compressor = libdeflate_alloc_compressor(level);
  if (compressor == NULL) {
    // Out of memory!
    throwException(env, "java/lang/OutOfMemoryError",
                   "libdeflate allocate compressor");
    return 0;
  }
  return (jlong)compressor;
}

LIBDEFLATEJAVA_PUBLIC JNIEXPORT void JNICALL
Java_me_steinborn_libdeflate_LibdeflateCompressor_free(JNIEnv *env,
                                                       jclass klass,
                                                       jlong ctx) {
  libdeflate_free_compressor((struct libdeflate_compressor *)ctx);
}

jlong performCompression(jlong ctx, jbyte *inBytes, jint inPos, jint inSize,
                         jbyte *outBytes, jint outPos, jint outSize,
                         jint type) {
  // We assume that any input validation has already been done before the method
  // has been called.
  struct libdeflate_compressor *compressor =
      (struct libdeflate_compressor *)ctx;
  void *inStart = (void *)(inBytes + inPos);
  void *outStart = (void *)(outBytes + outPos);

  size_t result = 0;
  switch (type) {
  case COMPRESSION_TYPE_DEFLATE:
    result = libdeflate_deflate_compress(compressor, inStart, inSize, outStart,
                                         outSize);
    break;
  case COMPRESSION_TYPE_ZLIB:
    result = libdeflate_zlib_compress(compressor, inStart, inSize, outStart,
                                      outSize);
    break;
  case COMPRESSION_TYPE_GZIP:
    result = libdeflate_gzip_compress(compressor, inStart, inSize, outStart,
                                      outSize);
    break;
  }
  return (jlong)result;
}

LIBDEFLATEJAVA_PUBLIC JNIEXPORT jlong JNICALL
Java_me_steinborn_libdeflate_LibdeflateCompressor_compressBothHeap(
    JNIEnv *env, jclass klass, jlong ctx, jbyteArray in, jint inPos,
    jint inSize, jbyteArray out, jint outPos, jint outSize, jint type) {
  jbyte *inBytes = (*env)->GetPrimitiveArrayCritical(env, in, 0);
  jbyte *outBytes = (*env)->GetPrimitiveArrayCritical(env, out, 0);

  if (inBytes == NULL || outBytes == NULL) {
    if (inBytes != NULL) {
      (*env)->ReleasePrimitiveArrayCritical(env, in, inBytes, JNI_ABORT);
    }
    return -1;
  }

  jlong result = performCompression(ctx, inBytes, inPos, inSize, outBytes,
                                    outPos, outSize, type);

  // We immediately commit the changes to the output array, but the input array
  // is never touched, so use JNI_ABORT to improve performance a bit.
  (*env)->ReleasePrimitiveArrayCritical(env, in, inBytes, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, out, outBytes, 0);
  return (jint)result;
}

LIBDEFLATEJAVA_PUBLIC JNIEXPORT jlong JNICALL
Java_me_steinborn_libdeflate_LibdeflateCompressor_compressBothDirect(
    JNIEnv *env, jclass klass, jlong ctx, jobject in, jint inPos, jint inSize,
    jobject out, jint outPos, jint outSize, jint type) {
  jbyte *inBytes = (*env)->GetDirectBufferAddress(env, in);
  jbyte *outBytes = (*env)->GetDirectBufferAddress(env, out);

  if (inBytes == NULL || outBytes == NULL) {
    throwException(env, "java/lang/IllegalArgumentException",
                   "unable to obtain direct access to buffer");
    return -1;
  }

  return performCompression(ctx, inBytes, inPos, inSize, outBytes, outPos,
                            outSize, type);
}

LIBDEFLATEJAVA_PUBLIC JNIEXPORT jlong JNICALL
Java_me_steinborn_libdeflate_LibdeflateCompressor_compressOnlySourceDirect(
    JNIEnv *env, jclass klass, jlong ctx, jobject in, jint inPos, jint inSize,
    jbyteArray out, jint outPos, jint outSize, jint type) {
  jbyte *inBytes = (*env)->GetDirectBufferAddress(env, in);
  if (inBytes == NULL) {
    throwException(env, "java/lang/IllegalArgumentException",
                   "unable to obtain direct access to input buffer");
    return -1;
  }

  jbyte *outBytes = (*env)->GetPrimitiveArrayCritical(env, out, 0);
  if (outBytes == NULL) {
    // out of memory
    return -1;
  }

  jlong result = performCompression(ctx, inBytes, inPos, inSize, outBytes,
                                    outPos, outSize, type);
  // Commit the output array
  (*env)->ReleasePrimitiveArrayCritical(env, out, outBytes, 0);
  return result;
}

LIBDEFLATEJAVA_PUBLIC JNIEXPORT jlong JNICALL
Java_me_steinborn_libdeflate_LibdeflateCompressor_compressOnlyDestinationDirect(
    JNIEnv *env, jclass klass, jlong ctx, jbyteArray in, jint inPos,
    jint inSize, jobject out, jint outPos, jint outSize, jint type) {
  jbyte *outBytes = (*env)->GetDirectBufferAddress(env, out);
  if (outBytes == NULL) {
    throwException(env, "java/lang/IllegalArgumentException",
                   "unable to obtain direct access to output buffer");
    return -1;
  }

  jbyte *inBytes = (*env)->GetPrimitiveArrayCritical(env, in, 0);
  if (outBytes == NULL) {
    // out of memory
    return -1;
  }

  jlong result = performCompression(ctx, inBytes, inPos, inSize, outBytes,
                                    outPos, outSize, type);
  (*env)->ReleasePrimitiveArrayCritical(env, in, inBytes, JNI_ABORT);
  return result;
}

LIBDEFLATEJAVA_PUBLIC JNIEXPORT jlong JNICALL
Java_me_steinborn_libdeflate_LibdeflateCompressor_getCompressBound(
    JNIEnv *env, jclass klass, jlong ctx, jlong length, jint type) {
  struct libdeflate_compressor *compressor =
      (struct libdeflate_compressor *)ctx;
  size_t result = 0;
  switch (type) {
  case COMPRESSION_TYPE_DEFLATE:
    result = libdeflate_deflate_compress_bound(compressor, length);
    break;
  case COMPRESSION_TYPE_ZLIB:
    result = libdeflate_zlib_compress_bound(compressor, length);
    break;
  case COMPRESSION_TYPE_GZIP:
    result = libdeflate_gzip_compress_bound(compressor, length);
    break;
  }
  return (jlong)result;
}
