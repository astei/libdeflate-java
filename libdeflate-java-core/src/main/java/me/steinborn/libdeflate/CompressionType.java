/*
 * Copyright 2024 Andrew Steinborn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    assert type != null
        : "No native type associated with " + this + " - this is a bug in libdeflate-java";
    return type;
  }
}
