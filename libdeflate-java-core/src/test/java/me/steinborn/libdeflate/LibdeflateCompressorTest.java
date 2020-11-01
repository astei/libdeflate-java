package me.steinborn.libdeflate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class LibdeflateCompressorTest {
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
            try (LibdeflateCompressor compressor = new LibdeflateCompressor()) {
                compressor.compress(null, new byte[1], CompressionType.DEFLATE);
            }
        });
    }

    @Test
    void errorsIfDestByteArrayNull() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            try (LibdeflateCompressor compressor = new LibdeflateCompressor()) {
                compressor.compress(new byte[1], null, CompressionType.DEFLATE);
            }
        });
    }

    @Test
    void errorsIfTypeByteArrayNull() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            try (LibdeflateCompressor compressor = new LibdeflateCompressor()) {
                compressor.compress(new byte[1], new byte[1], null);
            }
        });
    }

    @Test
    void errorsIfSourceByteBufferNull() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            try (LibdeflateCompressor compressor = new LibdeflateCompressor()) {
                compressor.compress(null, ByteBuffer.allocate(1), CompressionType.DEFLATE);
            }
        });
    }

    @Test
    void errorsIfDestByteBufferNull() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            try (LibdeflateCompressor compressor = new LibdeflateCompressor()) {
                compressor.compress(ByteBuffer.allocate(1), null, CompressionType.DEFLATE);
            }
        });
    }

    @Test
    void errorsIfTypeByteBufferNull() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            try (LibdeflateCompressor compressor = new LibdeflateCompressor()) {
                compressor.compress(ByteBuffer.allocate(1), ByteBuffer.allocate(1), null);
            }
        });
    }

    @Test
    void ensureCompressorFailsOnClosed() throws Exception {
        LibdeflateCompressor compressor = new LibdeflateCompressor();
        compressor.close();

        assertThrows(IllegalStateException.class, () -> compressor.compress(new byte[1], new byte[1], CompressionType.DEFLATE));
        assertThrows(IllegalStateException.class, () -> compressor.compress(new byte[1], 0, 1, new byte[1], 0, 1, CompressionType.DEFLATE));
        assertThrows(IllegalStateException.class, () -> compressor.compress(ByteBuffer.allocate(1), ByteBuffer.allocate(1), CompressionType.DEFLATE));
        assertThrows(IllegalStateException.class, () -> compressor.getCompressBound(1, CompressionType.DEFLATE));
    }

    @ParameterizedTest
    @MethodSource("byteBufferCompressionCombos")
    void compressorByteBufferSanity(ByteBufferMatrix matrix, CompressionType compressionType, UseDecompressor decompressor) throws Exception {
        ByteBuffer source = matrix.allocateSource(100);
        ByteBuffer destination = matrix.allocateDestination(200);
        for (int i = 0; i < 20; i++) {
            source.putInt(3);
        }
        source.flip();

        try (LibdeflateCompressor compressor = new LibdeflateCompressor()) {
            compressor.compress(source, destination, compressionType);

            destination.flip();

            source.position(0);
            verifyWrittenData(source, destination, compressionType, decompressor);
        }
    }

    @ParameterizedTest
    @MethodSource("byteBufferCompressionCombos")
    void compressorByteBufferSanityNonZeroPosition(ByteBufferMatrix matrix, CompressionType compressionType, UseDecompressor decompressor) throws Exception {
        ByteBuffer source = matrix.allocateSource(100);
        ByteBuffer destination = matrix.allocateDestination(200);
        source.position(5);
        for (int i = 0; i < 20; i++) {
            source.putInt(34);
        }
        source.flip();
        source.position(5);

        try (LibdeflateCompressor compressor = new LibdeflateCompressor()) {
            compressor.compress(source, destination, compressionType);

            destination.flip();

            source.position(5);
            verifyWrittenData(source, destination, compressionType, decompressor);
        }
    }

    @ParameterizedTest
    @MethodSource("byteArraySanityCombos")
    void compressorByteArraySanity(CompressionType compressionType, UseDecompressor decompressor) throws Exception {
        byte[] source = new byte[100];
        byte[] destination = new byte[200];
        ByteBuffer sourceAsBuf = ByteBuffer.wrap(source);
        for (int i = 0; i < 20; i++) {
            sourceAsBuf.putInt(4);
        }

        try (LibdeflateCompressor compressor = new LibdeflateCompressor()) {
            int produced = compressor.compress(source, destination, compressionType);
            verifyWrittenData(ByteBuffer.wrap(source), ByteBuffer.wrap(destination, 0, produced), compressionType, decompressor);
        }
    }

    @ParameterizedTest
    @MethodSource("byteArraySanityCombos")
    void compressorByteArrayNonZeroSourcePosition(CompressionType compressionType, UseDecompressor decompressor) throws Exception {
        byte[] source = new byte[100];
        byte[] destination = new byte[200];
        ByteBuffer sourceAsBuf = ByteBuffer.wrap(source, 5, source.length - 5);
        for (int i = 0; i < 20; i++) {
            sourceAsBuf.putInt(4);
        }

        try (LibdeflateCompressor compressor = new LibdeflateCompressor()) {
            int produced = compressor.compress(source, 5, source.length - 5, destination, 0, destination.length, compressionType);
            sourceAsBuf.position(5);
            verifyWrittenData(sourceAsBuf, ByteBuffer.wrap(destination, 0, produced), compressionType, decompressor);
        }
    }

    @ParameterizedTest
    @MethodSource("byteArraySanityCombos")
    void compressorByteArrayNonZeroDestinationPosition(CompressionType compressionType, UseDecompressor decompressor) throws Exception {
        byte[] source = new byte[100];
        byte[] destination = new byte[200];
        ByteBuffer sourceAsBuf = ByteBuffer.wrap(source);
        for (int i = 0; i < 20; i++) {
            sourceAsBuf.putInt(4);
        }

        try (LibdeflateCompressor compressor = new LibdeflateCompressor()) {
            int produced = compressor.compress(source, 0, source.length, destination, 10, destination.length - 10, compressionType);
            sourceAsBuf.position(0);
            verifyWrittenData(sourceAsBuf, ByteBuffer.wrap(destination, 10, produced), compressionType, decompressor);
        }
    }

    private void verifyWrittenData(ByteBuffer source, ByteBuffer destination, CompressionType compressionType, UseDecompressor decompressor) throws Exception {
        // Drain the destination buffer and use regular java.util.zip.Inflater (or libdeflate) to verify the result
        if (decompressor == UseDecompressor.JAVA) {
            byte[] compressed = new byte[destination.remaining()];
            destination.get(compressed);

            byte[] read = new byte[source.remaining()];
            if (compressionType == CompressionType.GZIP) {
                try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
                    in.read(read);
                }
            } else {
                Inflater inflater = new Inflater(compressionType == CompressionType.DEFLATE);
                try {
                    inflater.setInput(compressed);
                    inflater.inflate(read);
                } finally {
                    inflater.end();
                }
            }

            assertEquals(source, ByteBuffer.wrap(read), "Output from libdeflate compressor doesn't match input (as decompressed by j.u.z)");
        } else {
            // These also serve as a test to ensure LibdeflateDecompressor is working.
            ByteBuffer readHeap = ByteBuffer.allocate(source.remaining());
            ByteBuffer readDirect = ByteBuffer.allocateDirect(source.remaining());
            try (LibdeflateDecompressor libdeflateDecompressor = new LibdeflateDecompressor()) {
                libdeflateDecompressor.decompress(destination.slice(), readHeap, compressionType);
                libdeflateDecompressor.decompress(destination.slice(), readDirect, compressionType);
            }

            readHeap.flip();
            readDirect.flip();

            assertEquals(source, readHeap, "Output from libdeflate compressor doesn't match input (as decompressed by libdeflate, heap buffer destination)");
            assertEquals(source, readDirect, "Output from libdeflate compressor doesn't match input (as decompressed by libdeflate, direct buffer destination)");
        }
    }
}
