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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class LibdeflateCRC32Test {
  private static final int TEST_STRING_CRC32 = 0x71ca74cd;
  private static final String TEST_STRING = "libdeflate-jni";

  private static final String TEST_SIGNED_OVERFLOW =
      "libdeflate-jni is a very awesome JNI binding for libdeflate. Check it out!";
  private static final long TEST_SIGNED_OVERFLOW_CRC32 = 2599184220L;
  private static final long TEST_OFFSET_CRC32 = 1838842947;

  @Test
  void crc32Empty() {
    LibdeflateCRC32 crc32 = new LibdeflateCRC32();
    assertEquals(0, crc32.getValue());
  }

  @Test
  void crc32SingleByteUpdate() {
    LibdeflateCRC32 crc32 = new LibdeflateCRC32();
    crc32.update(0x42);
    assertEquals(1255198513, crc32.getValue());
  }

  @Test
  void crc32ResetCorrectly() {
    LibdeflateCRC32 crc32 = new LibdeflateCRC32();
    crc32.update(0x42);
    assertEquals(1255198513, crc32.getValue());

    crc32.reset();

    byte[] string = TEST_STRING.getBytes(StandardCharsets.US_ASCII);
    crc32.update(string);
    assertEquals(TEST_STRING_CRC32, crc32.getValue());
  }

  @Test
  void crc32Heap() {
    byte[] string = TEST_STRING.getBytes(StandardCharsets.US_ASCII);

    LibdeflateCRC32 crc32 = new LibdeflateCRC32();
    crc32.update(string);
    assertEquals(TEST_STRING_CRC32, crc32.getValue());
  }

  @Test
  void crc32HeapByteBuffer() {
    ByteBuffer buf = ByteBuffer.wrap(TEST_STRING.getBytes(StandardCharsets.US_ASCII));

    LibdeflateCRC32 crc32 = new LibdeflateCRC32();
    crc32.update(buf);
    assertEquals(TEST_STRING_CRC32, crc32.getValue());
  }

  @Test
  void crc32DirectByteBuffer() {
    ByteBuffer buf = ByteBuffer.allocateDirect(TEST_STRING.length());
    buf.put(TEST_STRING.getBytes(StandardCharsets.US_ASCII));
    buf.flip();

    LibdeflateCRC32 crc32 = new LibdeflateCRC32();
    crc32.update(buf);
    assertEquals(TEST_STRING_CRC32, crc32.getValue());
  }

  @Test
  void crc32SignedOverflow() {
    LibdeflateCRC32 crc32 = new LibdeflateCRC32();
    byte[] msg = TEST_SIGNED_OVERFLOW.getBytes(StandardCharsets.US_ASCII);
    crc32.update(msg, 0, msg.length);
    assertEquals(TEST_SIGNED_OVERFLOW_CRC32, crc32.getValue());
  }

  @Test
  void crc32Offset() {
    LibdeflateCRC32 crc32 = new LibdeflateCRC32();
    byte[] msg = TEST_SIGNED_OVERFLOW.getBytes(StandardCharsets.US_ASCII);
    crc32.update(msg, 1, msg.length - 2);
    assertEquals(TEST_OFFSET_CRC32, crc32.getValue());
  }
}
