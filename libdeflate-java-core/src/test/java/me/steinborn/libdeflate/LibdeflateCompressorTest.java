package me.steinborn.libdeflate;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class LibdeflateCompressorTest {
    private static Stream<Arguments> byteBufferCompressionCombos() {
        // a product set of ByteBufferMatrix with CompressionTypes
        return Arrays.stream(ByteBufferMatrix.values())
                .flatMap(bufferMatrix -> Arrays.stream(CompressionType.values())
                        .map(compressionType -> arguments(bufferMatrix, compressionType)));
    }

    @ParameterizedTest
    @MethodSource("byteBufferCompressionCombos")
    void compressorByteBufferSanity(ByteBufferMatrix matrix, CompressionType compressionType) throws Exception {
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
            verifyWrittenData(source, destination, compressionType);
        }
    }

    @ParameterizedTest
    @MethodSource("byteBufferCompressionCombos")
    void compressorByteBufferSanityNonZeroPosition(ByteBufferMatrix matrix, CompressionType compressionType) throws Exception {
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
            verifyWrittenData(source, destination, compressionType);
        }
    }

    @ParameterizedTest
    @EnumSource(CompressionType.class)
    void compressorByteArraySanity(CompressionType compressionType) throws Exception {
        byte[] source = new byte[100];
        byte[] destination = new byte[200];
        ByteBuffer sourceAsBuf = ByteBuffer.wrap(source);
        for (int i = 0; i < 20; i++) {
            sourceAsBuf.putInt(4);
        }

        try (LibdeflateCompressor compressor = new LibdeflateCompressor()) {
            int produced = compressor.compress(source, destination, compressionType);
            verifyWrittenData(ByteBuffer.wrap(source), ByteBuffer.wrap(destination, 0, produced), compressionType);
        }
    }

    @ParameterizedTest
    @EnumSource(CompressionType.class)
    void compressorByteArrayNonZeroSourcePosition(CompressionType compressionType) throws Exception {
        byte[] source = new byte[100];
        byte[] destination = new byte[200];
        ByteBuffer sourceAsBuf = ByteBuffer.wrap(source, 5, source.length - 5);
        for (int i = 0; i < 20; i++) {
            sourceAsBuf.putInt(4);
        }

        try (LibdeflateCompressor compressor = new LibdeflateCompressor()) {
            int produced = compressor.compress(source, 5, source.length - 5, destination, 0, destination.length, compressionType);
            sourceAsBuf.position(5);
            verifyWrittenData(sourceAsBuf, ByteBuffer.wrap(destination, 0, produced), compressionType);
        }
    }

    @ParameterizedTest
    @EnumSource(CompressionType.class)
    void compressorByteArrayNonZeroDestinationPosition(CompressionType compressionType) throws Exception {
        byte[] source = new byte[100];
        byte[] destination = new byte[200];
        ByteBuffer sourceAsBuf = ByteBuffer.wrap(source);
        for (int i = 0; i < 20; i++) {
            sourceAsBuf.putInt(4);
        }

        try (LibdeflateCompressor compressor = new LibdeflateCompressor()) {
            int produced = compressor.compress(source, 0, source.length, destination, 10, destination.length - 10, compressionType);
            sourceAsBuf.position(0);
            verifyWrittenData(sourceAsBuf, ByteBuffer.wrap(destination, 10, produced), compressionType);
        }
    }

    private void verifyWrittenData(ByteBuffer source, ByteBuffer destination, CompressionType compressionType) throws Exception {
        // Drain the destination buffer and use regular java.util.zip.Inflater to verify the result
        byte[] written = new byte[destination.remaining()];
        destination.get(written);

        byte[] data = new byte[source.remaining()];
        int read;
        if (compressionType == CompressionType.GZIP) {
            try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(written))) {
                read = in.read(data);
            }
        } else {
            Inflater inflater = new Inflater(compressionType == CompressionType.DEFLATE);
            try {
                inflater.setInput(written);
                read = inflater.inflate(data);
            } finally {
                inflater.end();
            }
        }

        assertEquals(source, ByteBuffer.wrap(data, 0, read));
    }
}
