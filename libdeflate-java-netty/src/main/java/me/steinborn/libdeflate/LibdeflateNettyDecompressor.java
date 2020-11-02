package me.steinborn.libdeflate;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

/**
 * An extension of {@link LibdeflateCompressor} that accepts Netty {@link ByteBuf} instances. See the documentation for
 * {@link LibdeflateCompressor} for more.
 */
public class LibdeflateNettyDecompressor extends LibdeflateDecompressor {
    public long decompressUnknown(ByteBuf in, ByteBuf out, CompressionType type) throws DataFormatException {
        return handlePossiblyNonContiguous(in, out, type, -1);
    }

    public void decompress(ByteBuf in, ByteBuf out, CompressionType type) throws DataFormatException {
        decompress(in, out, type, out.readableBytes());
    }

    public void decompress(ByteBuf in, ByteBuf out, CompressionType type, int uncompressedSize) throws DataFormatException {
        handlePossiblyNonContiguous(in, out, type, uncompressedSize);
    }

    private long handlePossiblyNonContiguous(ByteBuf in, ByteBuf out, CompressionType type, int uncompressedSize) throws DataFormatException {
        ensureNotClosed();
        boolean notContiguous = notContiguous(in) || notContiguous(out);

        if (!notContiguous) {
            return (int) decompress0(in, out, type, uncompressedSize, true);
        } else {
            // We don't have a lot of options when both buffers are not contiguous, since libdeflate doesn't
            // support a streaming API. Our best option is to make contiguous copies of each buffer.
            ByteBuf contiguousIn = makeContiguous(in);
            ByteBuf tmpOut = out.alloc().buffer(out.writableBytes());
            try {
                int produced = (int) decompress0(contiguousIn, tmpOut, type, uncompressedSize, false);

                tmpOut.writerIndex(tmpOut.writerIndex() + produced);
                in.skipBytes((int) this.readStreamBytes());
                out.writeBytes(tmpOut);
                return produced;
            } finally {
                contiguousIn.release();
                tmpOut.release();
            }
        }
    }

    long decompress0(ByteBuf in, ByteBuf out, CompressionType type, int uncompressedSize, boolean advanceIndices) throws DataFormatException {
        int nativeType = type.getNativeType();

        int inAvail = in.readableBytes();
        if (in.hasMemoryAddress() && out.hasMemoryAddress()) {
            // Optimized fast path.
            int produced = (int) decompressInMemory(
                    in.memoryAddress(), in.readerIndex(), inAvail,
                    out.memoryAddress(), out.writerIndex(), out.writableBytes(),
                    nativeType, uncompressedSize);
            if (advanceIndices) {
                in.skipBytes((int) this.readStreamBytes());
                out.writerIndex(out.writerIndex() + produced);
            }
            return produced;
        } else if (in.hasArray() && out.hasArray()) {
            // An array backs both buffers.
            int produced = (int) decompressBothHeap(
                    in.array(), in.arrayOffset() + in.readerIndex(), inAvail,
                    out.array(), out.arrayOffset() + out.writerIndex(), out.writableBytes(),
                    nativeType, uncompressedSize);
            if (advanceIndices) {
                in.skipBytes((int) this.readStreamBytes());
                out.writerIndex(out.writerIndex() + produced);
            }
            return produced;
        } else {
            // nioBuffer() may either be a copy or be shared with the backing buffer. In practice copying seems to only
            // happen when the buffer is a composite buffer, which is a case we catch and avoid by using a temporary flat
            // buffer. Thus we use nioBuffer() to directly manipulate the ByteBuf on both buffers.
            ByteBuffer inAsNio = in.nioBuffer();
            ByteBuffer outAsNio = out.nioBuffer(out.writerIndex(), out.writableBytes());
            int produced = (int) decompress0(inAsNio, outAsNio, type, uncompressedSize, false);

            if (advanceIndices) {
                in.skipBytes((int) this.readStreamBytes());
                out.writerIndex(out.writerIndex() + produced);
            }
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
