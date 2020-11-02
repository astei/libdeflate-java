package me.steinborn.libdeflate;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;

import java.nio.ByteBuffer;

/**
 * An extension of {@link LibdeflateCompressor} that accepts Netty {@link ByteBuf} instances. See the documentation for
 * {@link LibdeflateCompressor} for more.
 */
public class LibdeflateNettyCompressor extends LibdeflateCompressor {
    public LibdeflateNettyCompressor() {
    }

    public LibdeflateNettyCompressor(int level) {
        super(level);
    }

    /**
     * Compresses the given {@code in} ByteBuffer into the {@code out} ByteBuffer. When the compression operation
     * completes, the {@code writerIndex} of the output buffer will be incremented by the number of bytes produced, and
     * the input {@code readerIndex} will be incremented by the number of bytes remaining.
     *
     * @param in the source byte buffer to compress
     * @param out the destination which will hold compressed data
     * @param type the compression container to use
     * @return a positive, non-zero integer with the size of the compressed output, or zero if the given output buffer
     *         was too small
     */
    public int compress(ByteBuf in, ByteBuf out, CompressionType type) {
        ensureNotClosed();
        boolean notContiguous = notContiguous(in) || notContiguous(out);
        if (notContiguous) {
            // We don't have a lot of options when both buffers are not contiguous, since libdeflate doesn't
            // support a streaming API. Our best option is to make contiguous copies of each buffer.
            ByteBuf contiguousIn = makeContiguous(in);
            ByteBuf tmpOut = out.alloc().buffer(out.writableBytes());
            try {
                int inAvail = in.readableBytes();
                int produced = compressContiguous(contiguousIn, tmpOut, type);

                in.skipBytes(inAvail);
                out.writeBytes(tmpOut);
                return produced;
            } finally {
                contiguousIn.release();
                tmpOut.release();
            }
        } else {
            return compressContiguous(in, out, type);
        }
    }

    private int compressContiguous(ByteBuf in, ByteBuf out, CompressionType type) {
        int nativeType = type.getNativeType();

        int inAvail = in.readableBytes();
        if (in.hasMemoryAddress() && out.hasMemoryAddress()) {
            // Optimized fast path.
            int produced = (int) compressInMemory(this.ctx,
                    in.memoryAddress(), in.readerIndex(), inAvail,
                    out.memoryAddress(), out.writerIndex(), out.writableBytes(),
                    nativeType);
            in.skipBytes(inAvail);
            out.writerIndex(out.writerIndex() + produced);
            return produced;
        } else if (in.hasArray() && out.hasArray()) {
            // An array backs both buffers.
            int produced = (int) compressBothHeap(ctx,
                    in.array(), in.arrayOffset() + in.readerIndex(), inAvail,
                    out.array(), out.arrayOffset() + out.writerIndex(), out.writableBytes(),
                    nativeType);
            in.skipBytes(inAvail);
            out.writerIndex(out.writerIndex() + produced);
            return produced;
        } else {
            // nioBuffer() may either be a copy or be shared with the backing buffer. In practice copying seems to only
            // happen when the buffer is a composite buffer, which is a case we catch and avoid by using a temporary flat
            // buffer. Thus we use nioBuffer() to directly manipulate the ByteBuf on both buffers.
            ByteBuffer inAsNio = in.nioBuffer();
            ByteBuffer outAsNio = out.nioBuffer(out.writerIndex(), out.writableBytes());
            int produced = compress(inAsNio, outAsNio, type);

            in.skipBytes(inAvail);
            out.writerIndex(out.writerIndex() + produced);
            return produced;
        }
    }

    private static boolean notContiguous(ByteBuf buf) {
        if (buf instanceof CompositeByteBuf) {
            return ((CompositeByteBuf) buf).numComponents() > 1;
        } else {
            return false;
        }
    }

    private static ByteBuf makeContiguous(ByteBuf buf) {
        if (buf.isContiguous()) {
            return buf.retain();
        } else {
            return buf.copy();
        }
    }
}
