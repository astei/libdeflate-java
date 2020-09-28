package me.steinborn.libdeflate;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

import static me.steinborn.libdeflate.LibdeflateJavaUtils.byteBufferArrayPosition;
import static me.steinborn.libdeflate.LibdeflateJavaUtils.checkBounds;

/**
 * Represents a {@code libdeflate} decompressor. This class contains compression methods for byte arrays and NIO
 * ByteBuffers.
 * <p/>
 * <strong>Thread-safety</strong>: libdeflate decompressors are not thread-safe, however using multiple decompressors
 * per thread is permissible.
 */
public class LibdeflateDecompressor implements Closeable, AutoCloseable {
    static {
        Libdeflate.ensureAvailable();
        initIDs();
    }

    private final long ctx;
    private long availInBytes = -1;
    private boolean closed = false;

    /**
     * Creates a new libdeflate decompressor.
     */
    public LibdeflateDecompressor() {
        this.ctx = allocate();
    }

    private void ensureNotClosed() {
        if (this.closed) {
            throw new IllegalStateException("Decompressor already closed.");
        }
    }

    /**
     * Retrieves and clears the number of read-in bytes representing the end of a zlib stream, for use with
     * byte array-based decompression APIs.
     *
     * @return the bytes read in by a previous decompress operation
     * @throws IllegalStateException if no decompression operation took place
     */
    public long readStreamBytes() {
        long bytes = availInBytes;
        if (bytes == -1) {
            throw new IllegalStateException("No byte array decompression done yet!");
        }
        availInBytes = -1;
        return bytes;
    }

    /**
     * Decompresses the given {@code in} array into the {@code out} array. This method assumes the uncompressed size of
     * the data is known.
     *
     * @param in the source array with compressed
     * @param out the destination which will hold decompressed data
     * @param type the compression container to use
     * @param uncompressedSize the known size of the data
     * @throws DataFormatException if the provided data was corrupt, or the data decompressed successfully but not to {@code uncompressedSize}
     */
    public void decompress(byte[] in, byte[] out, CompressionType type, int uncompressedSize) throws DataFormatException {
        ensureNotClosed();
        if (uncompressedSize > out.length) {
            throw new IndexOutOfBoundsException("uncompressedSize(" + uncompressedSize + ") > out(" + out.length + ")");
        }
        decompressBothHeap(in, 0, in.length, out, 0, out.length, type.getNativeType(), uncompressedSize);
    }

    /**
     * Decompresses the given {@code in} array into the {@code out} array. This method assumes the uncompressed size of
     * the data is known.
     *
     * @param in the source array with compressed data
     * @param inOff the offset into the source array
     * @param inLen the length into the source array from the offset
     * @param out the destination which will hold decompressed data
     * @param outOff the offset into the source array
     * @param type the compression container to use
     * @param uncompressedSize the known size of the data, which is also treated as the length into the output array from {@code outOff}
     * @throws DataFormatException if the provided data was corrupt, or the data decompressed successfully but not to {@code uncompressedSize}
     */
    public void decompress(byte[] in, int inOff, int inLen, byte[] out, int outOff, CompressionType type, int uncompressedSize) throws DataFormatException {
        ensureNotClosed();

        int outAvail = out.length - outOff;
        checkBounds(in.length, inOff, inLen);
        checkBounds(out.length, outOff, outAvail);
        if (uncompressedSize > outAvail) {
            throw new IndexOutOfBoundsException("uncompressedSize(" + uncompressedSize + ") > out(" + outAvail + ")");
        }
        decompressBothHeap(in, inOff, inLen, out, outOff, outAvail, type.getNativeType(), uncompressedSize);
    }

    /**
     * Decompresses the given {@code in} array into the {@code out} array. This method assumes the uncompressed size of
     * the data is unknown. Note that using libdeflate's decompressor when the uncompressed size of the data is not
     * known is not recommended, because libdeflate does not have a streaming API. If you require a streaming API, you
     * are better served by the {@code Deflater} and {@code Inflater} classes in {@code java.util.zip}.
     *
     * @param in the source array with compressed
     * @param out the destination which will hold decompressed data
     * @param type the compression container to use
     * @return a positive, non-zero integer with the size of the uncompressed output, or -1 if the given output buffer
     *         was too small
     * @throws DataFormatException if the provided data was corrupt
     */
    public long decompressUnknownSize(byte[] in, byte[] out, CompressionType type) throws DataFormatException {
        ensureNotClosed();
        return decompressBothHeap(in, 0, in.length, out, 0, out.length, type.getNativeType(), -1);
    }

    /**
     * Decompresses the given {@code in} array into the {@code out} array. This method assumes the uncompressed size of
     * the data is unknown. Note that using libdeflate's decompressor when the uncompressed size of the data is not
     * known is not recommended, because libdeflate does not have a streaming API. If you require a streaming API, you
     * are better served by the {@code Deflater} and {@code Inflater} classes in {@code java.util.zip}.
     *
     * @param in the source array with compressed
     * @param inOff the offset into the source array
     * @param inLen the length into the source array from the offset
     * @param out the destination which will hold decompressed data
     * @param outOff the offset into the source array
     * @param outLen the length into the source array from {@code outOff}
     * @param type the compression container to use
     * @return a positive, non-zero integer with the size of the uncompressed output, or -1 if the given output buffer
     *         was too small
     * @throws DataFormatException if the provided data was corrupt
     */
    public long decompressUnknownSize(byte[] in, int inOff, int inLen, byte[] out, int outOff, int outLen, CompressionType type) throws DataFormatException {
        ensureNotClosed();

        checkBounds(in.length, inOff, inLen);
        checkBounds(out.length, outOff, outLen);
        return decompressBothHeap(in, inOff, inLen, out, outOff, outLen, type.getNativeType(), -1);
    }

    private long decompress0(ByteBuffer in, ByteBuffer out, CompressionType type, int uncompressedSize) throws DataFormatException {
        ensureNotClosed();
        int nativeType = type.getNativeType();

        int inAvail = in.remaining();
        int outAvail = out.remaining();

        if (uncompressedSize < -1) {
            throw new IndexOutOfBoundsException("uncompressedSize = " + uncompressedSize);
        }
        if (uncompressedSize > outAvail) {
            throw new IndexOutOfBoundsException("uncompressedSize(" + uncompressedSize + ") > outAvail(" + outAvail + ")");
        }

        // Either ByteBuffer could be direct or heap.
        long outRealSize;
        if (in.isDirect()) {
            if (out.isDirect()) {
                outRealSize = decompressBothDirect(in, in.position(), inAvail, out, out.position(),
                        outAvail, nativeType, uncompressedSize);
            } else {
                outRealSize = decompressOnlySourceDirect(in, in.position(), inAvail, out.array(),
                        byteBufferArrayPosition(out), outAvail, nativeType, uncompressedSize);
            }
        } else {
            int inPos = byteBufferArrayPosition(in);
            if (out.isDirect()) {
                outRealSize = decompressOnlyDestinationDirect(in.array(), inPos, inAvail, out, out.position(),
                        outAvail, nativeType, uncompressedSize);
            } else {
                outRealSize = decompressBothHeap(in.array(), inPos, inAvail, out.array(), byteBufferArrayPosition(out),
                        outAvail, nativeType, uncompressedSize);
            }
        }

        if (uncompressedSize != -1) {
            outRealSize = uncompressedSize;
        }
        out.position((int) (out.position() + outRealSize));
        in.position((int) (in.position() + this.readStreamBytes()));
        return outRealSize;
    }

    /**
     * Decompresses the given {@code in} ByteBuffer into the {@code out} ByteBuffer. When the decompression operation
     * completes, the {@code position} of the output buffer will be incremented by the number of bytes produced, and
     * the input {@code position} will be incremented by the number of bytes read.
     *
     * @param in the source byte buffer to decompress
     * @param out the destination which will hold decompressed data
     * @param type the compression container in use
     * @throws DataFormatException if the provided data was corrupt, or the data decompresses to an invalid size
     */
    public void decompress(ByteBuffer in, ByteBuffer out, CompressionType type, int uncompressedSize) throws DataFormatException {
        decompress0(in, out, type, uncompressedSize);
    }

    /**
     * Decompresses the given {@code in} ByteBuffer into the {@code out} ByteBuffer. When the decompression operation
     * completes, the {@code position} of the output buffer will be incremented by the number of bytes produced, and
     * the input {@code position} will be incremented by the number of bytes read.
     * <p/>
     * Note that using libdeflate's decompressor when the uncompressed size of the data is not known is not recommended,
     * because libdeflate does not have a streaming API. If you require a streaming API, you are better served by the
     * {@code Deflater} and {@code Inflater} classes in {@code java.util.zip}.
     *
     * @param in the source byte buffer to decompress
     * @param out the destination which will hold decompressed data
     * @param type the compression container in use
     * @return a positive, non-zero integer with the size of the uncompressed output, or -1 if the given output buffer
     *         was too small
     * @throws DataFormatException if the provided data was corrupt, or the data decompresses to an invalid size
     */
    public long decompressUnknown(ByteBuffer in, ByteBuffer out, CompressionType type) throws DataFormatException {
        return decompress0(in, out, type, -1);
    }

    @Override
    public void close() {
        ensureNotClosed();
        free(this.ctx);
        this.closed = true;
    }

    /* Native function declarations. */
    private static native void initIDs();
    private static native long allocate();
    private static native void free(long ctx);
    private native long decompressBothHeap(byte[] in, int inPos, int inSize, byte[] out, int outPos, int outSize, int type, int knownSize) throws DataFormatException;
    private native long decompressOnlyDestinationDirect(byte[] in, int inPos, int inSize, ByteBuffer out, int outPos, int outSize, int type, int knownSize) throws DataFormatException;
    private native long decompressOnlySourceDirect(ByteBuffer in, int inPos, int inSize, byte[] out, int outPos, int outSize, int type, int knownSize) throws DataFormatException;
    private native long decompressBothDirect(ByteBuffer in, int inPos, int inSize, ByteBuffer out, int outPos, int outSize, int type, int knownSize) throws DataFormatException;
}
