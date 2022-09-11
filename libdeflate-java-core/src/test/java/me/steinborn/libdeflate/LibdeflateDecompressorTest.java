package me.steinborn.libdeflate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class LibdeflateDecompressorTest {
    private static Stream<Arguments> byteBufferCompressionCombos() {
        // a product set of ByteBufferMatrix with CompressionTypes and UseDecompressors
        return Arrays.stream(ByteBufferMatrix.values())
                .flatMap(bufferMatrix -> Arrays.stream(CompressionType.values())
                        .flatMap(compressionType -> Arrays.stream(UseDecompressor.values())
                                .map(decompressor -> arguments(bufferMatrix, compressionType, decompressor))));
    }

    private static Stream<Arguments> byteArraySanityCombos() {
        // a product set of ByteBufferMatrix with UseDecompressors
        return Arrays.stream(CompressionType.values())
                .flatMap(compressionType -> Arrays.stream(UseDecompressor.values())
                        .map(decompressor -> arguments(compressionType, decompressor)));
    }

    @Test
    void errorsIfSourceByteArrayNull() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            try (LibdeflateDecompressor decompressor = new LibdeflateDecompressor()) {
                decompressor.decompress(null, new byte[1], CompressionType.DEFLATE);
            }
        });
    }

    @Test
    void errorsIfDestByteArrayNull() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            try (LibdeflateDecompressor decompressor = new LibdeflateDecompressor()) {
                decompressor.decompress(new byte[1], null, CompressionType.DEFLATE);
            }
        });
    }

    @Test
    void errorsIfTypeByteArrayNull() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            try (LibdeflateDecompressor decompressor = new LibdeflateDecompressor()) {
                decompressor.decompress(new byte[1], new byte[1], null);
            }
        });
    }

    @Test
    void errorsIfSourceByteBufferNull() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            try (LibdeflateDecompressor decompressor = new LibdeflateDecompressor()) {
                decompressor.decompress(null, ByteBuffer.allocate(1), CompressionType.DEFLATE);
            }
        });
    }

    @Test
    void errorsIfDestByteBufferNull() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            try (LibdeflateDecompressor decompressor = new LibdeflateDecompressor()) {
                decompressor.decompress(ByteBuffer.allocate(1), null, CompressionType.DEFLATE);
            }
        });
    }

    @Test
    void errorsIfTypeByteBufferNull() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            try (LibdeflateDecompressor decompressor = new LibdeflateDecompressor()) {
                decompressor.decompress(ByteBuffer.allocate(1), ByteBuffer.allocate(1), null);
            }
        });
    }

    @Test
    void ensureCompressorFailsOnClosed() throws Exception {
        LibdeflateDecompressor decompressor = new LibdeflateDecompressor();
        decompressor.close();

        assertThrows(IllegalStateException.class, () -> decompressor.decompress(new byte[1], new byte[1], CompressionType.DEFLATE));
        assertThrows(IllegalStateException.class, () -> decompressor.decompress(new byte[1], 0, 1, new byte[1], 0, 1, CompressionType.DEFLATE, 1));
        assertThrows(IllegalStateException.class, () -> decompressor.decompress(ByteBuffer.allocate(1), ByteBuffer.allocate(1), CompressionType.DEFLATE));
        assertThrows(IllegalStateException.class, () -> decompressor.decompressUnknownSize(new byte[1], new byte[1], CompressionType.DEFLATE));
        assertThrows(IllegalStateException.class, () -> decompressor.decompressUnknownSize(new byte[1], 0, 1, new byte[1], 0, 1, CompressionType.DEFLATE));
        assertThrows(IllegalStateException.class, () -> decompressor.decompressUnknownSize(ByteBuffer.allocate(1), ByteBuffer.allocate(1), CompressionType.DEFLATE));
    }
}
