package me.steinborn.libdeflate;

import java.nio.ByteBuffer;
import java.util.zip.Checksum;

import static me.steinborn.libdeflate.LibdeflateJavaUtils.byteBufferArrayPosition;
import static me.steinborn.libdeflate.LibdeflateJavaUtils.checkBounds;

/**
 * Equivalent to {@link java.util.zip.Adler32}, but uses libdeflate's Adler-32 routines. As a result, performance of
 * this class is likely to be better than the JDK version.
 */
public class LibdeflateAdler32 implements Checksum {
    static {
        Libdeflate.ensureAvailable();
    }

    private int adler32 = 1;

    @Override
    public void update(int b) {
        byte[] tmp = new byte[] { (byte) b };
        adler32 = adler32Heap(adler32, tmp, 0, 1);
    }

    public void update(byte[] b) {
        adler32 = adler32Heap(adler32, b, 0, b.length);
    }

    @Override
    public void update(byte[] b, int off, int len) {
        checkBounds(b.length, off, len);
        adler32 = adler32Heap(adler32, b, off, len);
    }

    public void update(ByteBuffer buffer) {
        int pos = buffer.position();
        int limit = buffer.limit();
        int remaining = limit - pos;
        if (buffer.hasArray()) {
            adler32 = adler32Heap(adler32, buffer.array(), byteBufferArrayPosition(buffer), remaining);
        } else if (buffer.isDirect()) {
            adler32 = adler32Direct(adler32, buffer, pos, remaining);
        } else {
            // make a copy of this array
            byte[] data = new byte[remaining];
            buffer.get(data);
            adler32 = adler32Heap(adler32, data, 0, data.length);
        }
        buffer.position(limit);
    }

    @Override
    public long getValue() {
        return ((long) adler32 & 0xffffffffL);
    }

    @Override
    public void reset() {
        adler32 = 1;
    }

    private static native int adler32Heap(long adler32, byte[] array, int off, int len);
    private static native int adler32Direct(long adler32, ByteBuffer buf, int off, int len);
}
