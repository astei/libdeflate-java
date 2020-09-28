package me.steinborn.libdeflate;

import java.nio.ByteBuffer;
import java.util.function.Function;

public enum ByteBufferMatrix {
    BOTH_HEAP(ByteBuffer::allocate, ByteBuffer::allocate),
    DIRECT_SOURCE(ByteBuffer::allocateDirect, ByteBuffer::allocate),
    DIRECT_DESTINATION(ByteBuffer::allocate, ByteBuffer::allocateDirect),
    BOTH_DIRECT(ByteBuffer::allocateDirect, ByteBuffer::allocateDirect);

    private final Function<Integer, ByteBuffer> sourceAlloc;
    private final Function<Integer, ByteBuffer> destAlloc;

    ByteBufferMatrix(Function<Integer, ByteBuffer> sourceAlloc, Function<Integer, ByteBuffer> destAlloc) {
        this.sourceAlloc = sourceAlloc;
        this.destAlloc = destAlloc;
    }

    public ByteBuffer allocateSource(int capacity) {
        return sourceAlloc.apply(capacity);
    }

    public ByteBuffer allocateDestination(int capacity) {
        return destAlloc.apply(capacity);
    }
}
