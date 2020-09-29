package me.steinborn.libdeflate;

import java.util.EnumMap;
import java.util.Map;

public enum CompressionType {
    DEFLATE,
    ZLIB,
    GZIP;

    // Keep this and the definitions in the compressor.c in sync.
    private static final Map<CompressionType, Integer> NATIVE_MAPPINGS;

    static {
        NATIVE_MAPPINGS = new EnumMap<>(CompressionType.class);
        NATIVE_MAPPINGS.put(DEFLATE, 0);
        NATIVE_MAPPINGS.put(ZLIB, 1);
        NATIVE_MAPPINGS.put(GZIP, 2);
    }

    int getNativeType() {
        Integer type = NATIVE_MAPPINGS.get(this);
        assert type != null : "No native type associated with " + this + " - this is a bug in libdeflate-java";
        return type;
    }
}
