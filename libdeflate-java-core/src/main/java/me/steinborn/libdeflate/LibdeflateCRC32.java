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

import static me.steinborn.libdeflate.LibdeflateJavaUtils.byteBufferArrayPosition;
import static me.steinborn.libdeflate.LibdeflateJavaUtils.checkBounds;

import java.nio.ByteBuffer;
import java.util.zip.Checksum;

/**
 * Equivalent to {@link java.util.zip.CRC32}, but uses libdeflate's CRC-32 routines. As a result,
 * performance of this class is likely to be better than the JDK version.
 */
public class LibdeflateCRC32 implements Checksum {
  static {
    Libdeflate.ensureAvailable();
  }

  private int crc32 = 0;

  @Override
  public void update(int b) {
    byte[] tmp = new byte[] {(byte) b};
    crc32 = crc32Heap(crc32, tmp, 0, 1);
  }

  public void update(byte[] b) {
    crc32 = crc32Heap(crc32, b, 0, b.length);
  }

  @Override
  public void update(byte[] b, int off, int len) {
    checkBounds(b.length, off, len);
    crc32 = crc32Heap(crc32, b, off, len);
  }

  public void update(ByteBuffer buffer) {
    int pos = buffer.position();
    int limit = buffer.limit();
    int remaining = limit - pos;
    if (buffer.hasArray()) {
      crc32 = crc32Heap(crc32, buffer.array(), byteBufferArrayPosition(buffer), remaining);
    } else if (buffer.isDirect()) {
      crc32 = crc32Direct(crc32, buffer, pos, remaining);
    } else {
      // make a copy of this array
      byte[] data = new byte[remaining];
      buffer.get(data);
      crc32 = crc32Heap(crc32, data, 0, data.length);
    }
    buffer.position(limit);
  }

  @Override
  public long getValue() {
    return ((long) crc32 & 0xffffffffL);
  }

  @Override
  public void reset() {
    crc32 = 0;
  }

  private static native int crc32Heap(long crc32, byte[] array, int off, int len);

  private static native int crc32Direct(long crc32, ByteBuffer buf, int off, int len);
}
