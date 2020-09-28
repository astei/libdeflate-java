#include <jni.h>
#include "./common.h"
#include "./jni_util.h"
#include "./libdeflate/libdeflate.h"

static jfieldID ctxFieldID;
static jfieldID availInFieldID;

LIBDEFLATEJAVA_PUBLIC JNIEXPORT void JNICALL Java_me_steinborn_libdeflate_LibdeflateDecompressor_initIDs(JNIEnv *env, jclass klass) {
    ctxFieldID = (*env)->GetFieldID(env, klass, "ctx", "J");
    availInFieldID = (*env)->GetFieldID(env, klass, "availInBytes", "J");
}

LIBDEFLATEJAVA_PUBLIC JNIEXPORT jlong JNICALL Java_me_steinborn_libdeflate_LibdeflateDecompressor_allocate(JNIEnv *env, jclass klass) {
    struct libdeflate_decompressor *decompressor = libdeflate_alloc_decompressor();
    if (decompressor == NULL) {
        // Out of memory!
        throwException(env, "java/lang/OutOfMemoryError", "libdeflate allocate decompressor");
        return 0;
    }
    return (jlong) decompressor;
}

LIBDEFLATEJAVA_PUBLIC JNIEXPORT void JNICALL Java_me_steinborn_libdeflate_LibdeflateDecompressor_free(JNIEnv *env, jclass klass, jlong ctx) {
    libdeflate_free_decompressor((struct libdeflate_decompressor *) ctx);
}

jlong performDecompression(
    JNIEnv *env, jobject this, /* JNI fluff */
    jbyte* inBytes, jint inPos, jint inSize, /* Input buffer */
    jbyte* outBytes, jint outPos, jint outSize, /* Output buffer */
    jint type, /* Compression wrapper */
    jint knownSize)
{
    // We assume that any input validation has already been done before the method has been called.
    jlong ctx = (*env)->GetLongField(env, this, ctxFieldID);
    struct libdeflate_decompressor *decompressor = (struct libdeflate_decompressor *) ctx;

    void *inStart = (void *) (inBytes + inPos);
    void *outStart = (void *) (outBytes + outPos);

    size_t availableOutBytes = knownSize == -1 ? outSize : knownSize;
    size_t actualInBytes = 0;
    size_t actualOutBytes = 0; // in case of unknown size

    enum libdeflate_result result = 0;
    switch (type) {
        case COMPRESSION_TYPE_DEFLATE:
            result = libdeflate_deflate_decompress_ex(decompressor, inStart, inSize, outStart, availableOutBytes,
                &actualInBytes, knownSize == -1 ? &actualOutBytes : NULL);
            break;
        case COMPRESSION_TYPE_ZLIB:
            result = libdeflate_zlib_decompress_ex(decompressor, inStart, inSize, outStart, availableOutBytes,
                &actualInBytes, knownSize == -1 ? &actualOutBytes : NULL);
            break;
        case COMPRESSION_TYPE_GZIP:
            result = libdeflate_gzip_decompress_ex(decompressor, inStart, inSize, outStart, availableOutBytes,
                &actualInBytes, knownSize == -1 ? &actualOutBytes : NULL);
            break;
    }

    switch (result) {
        case LIBDEFLATE_SUCCESS:
            (*env)->SetLongField(env, this, availInFieldID, actualInBytes);
            return actualOutBytes;
        case LIBDEFLATE_BAD_DATA:
            throwException(env, "java/util/zip/DataFormatException", "input data is corrupted");
            return 0;
        case LIBDEFLATE_SHORT_OUTPUT:
            // This case only fires when the exact uncompressed size was specified by the user
            throwException(env, "java/util/zip/DataFormatException", "decompressed data is shorter than expected size");
            return 0;
        case LIBDEFLATE_INSUFFICIENT_SPACE:
            // There's two ways we could handle this:
            // - Throw an exception.
            // - Return a sentinel value indicating that the output buffer was not big enough.
            // It's probably better to split the difference. This should be an exception if the uncompressed size was known
            // but if not, it needs to be indicated to the user as a sentinel value.
            if (knownSize == -1) {
                return -1;
            } else {
                throwException(env, "java/util/zip/DataFormatException", "decompressed data would be too large for given output buffer");
                return 0;
            }
        default:
            throwException(env, "java/util/zip/DataFormatException", "unknown libdeflate error");
            return 0;
    }
}

LIBDEFLATEJAVA_PUBLIC JNIEXPORT jlong JNICALL Java_me_steinborn_libdeflate_LibdeflateDecompressor_decompressBothHeap(
    JNIEnv *env, jobject this,
    jbyteArray in, jint inPos, jint inSize,
    jbyteArray out, jint outPos, jint outSize,
    jint type,
    jint knownSize)
{
    jbyte *inBytes = (*env)->GetPrimitiveArrayCritical(env, in, 0);
    jbyte *outBytes = (*env)->GetPrimitiveArrayCritical(env, out, 0);

    if (inBytes == NULL || outBytes == NULL) {
        if (inBytes != NULL) {
            (*env)->ReleasePrimitiveArrayCritical(env, in, inBytes, JNI_ABORT);
        }
        return -1;
    }

    jlong result = performDecompression(env, this, inBytes, inPos, inSize, outBytes, outPos, outSize, type, knownSize);

    // We immediately commit the changes to the output array, but the input array is never touched, so use JNI_ABORT
    // to improve performance a bit.
    (*env)->ReleasePrimitiveArrayCritical(env, in, inBytes, JNI_ABORT);
    (*env)->ReleasePrimitiveArrayCritical(env, in, outBytes, 0);
    return result;
}

LIBDEFLATEJAVA_PUBLIC JNIEXPORT jlong JNICALL Java_me_steinborn_libdeflate_LibdeflateDecompressor_decompressBothDirect(
    JNIEnv *env, jobject this,
    jobject in, jint inPos, jint inSize,
    jobject out, jint outPos, jint outSize,
    jint type,
    jint knownSize)
{
    jbyte *inBytes = (*env)->GetDirectBufferAddress(env, in);
    jbyte *outBytes = (*env)->GetDirectBufferAddress(env, out);

    if (inBytes == NULL || outBytes == NULL) {
        throwException(env, "java/lang/IllegalArgumentException", "unable to obtain direct access to buffer");
        return -1;
    }

    return performDecompression(env, this, inBytes, inPos, inSize, outBytes, outPos, outSize, type, knownSize);
}

LIBDEFLATEJAVA_PUBLIC JNIEXPORT jlong JNICALL Java_me_steinborn_libdeflate_LibdeflateDecompressor_decompressOnlySourceDirect(
    JNIEnv *env, jobject this,
    jobject in, jint inPos, jint inSize,
    jbyteArray out, jint outPos, jint outSize,
    jint type,
    jint knownSize)
{
    jbyte *inBytes = (*env)->GetDirectBufferAddress(env, in);
    if (inBytes == NULL) {
        throwException(env, "java/lang/IllegalArgumentException", "unable to obtain direct access to input buffer");
        return -1;
    }

    jbyte *outBytes = (*env)->GetPrimitiveArrayCritical(env, out, 0);
    if (outBytes == NULL) {
        // out of memory
        return -1;
    }

    jlong result = performDecompression(env, this, inBytes, inPos, inSize, outBytes, outPos, outSize, type, knownSize);
    // Commit the output array
    (*env)->ReleasePrimitiveArrayCritical(env, out, outBytes, 0);
    return result;
}

LIBDEFLATEJAVA_PUBLIC JNIEXPORT jlong JNICALL Java_me_steinborn_libdeflate_LibdeflateDecompressor_decompressOnlyDestinationDirect(
    JNIEnv *env, jobject this,
    jbyteArray in, jint inPos, jint inSize,
    jobject out, jint outPos, jint outSize,
    jint type,
    jint knownSize)
{
    jbyte *outBytes = (*env)->GetDirectBufferAddress(env, out);
    if (outBytes == NULL) {
        throwException(env, "java/lang/IllegalArgumentException", "unable to obtain direct access to output buffer");
        return -1;
    }

    jbyte *inBytes = (*env)->GetPrimitiveArrayCritical(env, in, 0);
    if (outBytes == NULL) {
        // out of memory
        return -1;
    }

    jlong result = performDecompression(env, this, inBytes, inPos, inSize, outBytes, outPos, outSize, type, knownSize);
    (*env)->ReleasePrimitiveArrayCritical(env, in, inBytes, JNI_ABORT);
    return result;
}