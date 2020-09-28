package me.steinborn.libdeflate;

import java.nio.ByteBuffer;

class LibdeflateJavaUtils {
    private LibdeflateJavaUtils() {

    }

    static void checkBounds(int backingLen, int userOffset, int userLen) {
        if (userOffset < 0) {
            throw new IndexOutOfBoundsException("userOffset = " + userOffset);
        }
        if (userLen < 0) {
            throw new IndexOutOfBoundsException("userLen = " + userLen);
        }
        int fullRange = userLen + userOffset;
        if (fullRange > backingLen) {
            throw new IndexOutOfBoundsException("userOffset+userLen(" + fullRange + ") > backingLen(" + userOffset + ")");
        }
    }

    static int byteBufferArrayPosition(ByteBuffer buffer) {
        return buffer.arrayOffset() + buffer.position();
    }
}
