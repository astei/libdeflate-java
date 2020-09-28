package me.steinborn.libdeflate;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class LibdeflateAdler32Test {
    private static final int TEST_STRING_ADLER32 = 0x29a6057b;
    private static final String TEST_STRING = "libdeflate-jni";

    private static final String TEST_SIGNED_OVERFLOW = "libdeflate-jni is a very awesome JNI binding for libdeflate. Check it out!";
    private static final long TEST_SIGNED_OVERFLOW_ADLER32 = 3926923840L;

    @Test
    void adler32Empty() {
        LibdeflateAdler32 adler32 = new LibdeflateAdler32();
        assertEquals(1, adler32.getValue());
    }

    @Test
    void adler32SingleByteUpdate() {
        LibdeflateAdler32 adler32 = new LibdeflateAdler32();
        adler32.update(0x42);
        assertEquals(4390979, adler32.getValue());
    }

    @Test
    void adler32Heap() {
        byte[] string = TEST_STRING.getBytes(StandardCharsets.US_ASCII);

        LibdeflateAdler32 adler32 = new LibdeflateAdler32();
        adler32.update(string, 0, string.length);
        assertEquals(TEST_STRING_ADLER32, adler32.getValue());
    }

    @Test
    void adler32HeapByteBuffer() {
        ByteBuffer buf = ByteBuffer.wrap(TEST_STRING.getBytes(StandardCharsets.US_ASCII));

        LibdeflateAdler32 adler32 = new LibdeflateAdler32();
        adler32.update(buf);
        assertEquals(TEST_STRING_ADLER32, adler32.getValue());
    }

    @Test
    void adler32DirectByteBuffer() {
        ByteBuffer buf = ByteBuffer.allocateDirect(TEST_STRING.length());
        buf.put(TEST_STRING.getBytes(StandardCharsets.US_ASCII));
        buf.flip();

        LibdeflateAdler32 adler32 = new LibdeflateAdler32();
        adler32.update(buf);
        assertEquals(TEST_STRING_ADLER32, adler32.getValue());
    }

    @Test
    void adler32SignedOverflow() {
        LibdeflateAdler32 adler32 = new LibdeflateAdler32();
        byte[] msg = TEST_SIGNED_OVERFLOW.getBytes(StandardCharsets.US_ASCII);
        adler32.update(msg, 0, msg.length);
        assertEquals(TEST_SIGNED_OVERFLOW_ADLER32, adler32.getValue());
    }
}