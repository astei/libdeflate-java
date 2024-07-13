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

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class LibdeflateAdler32Test {
  private static final int TEST_STRING_ADLER32 = 0x29a6057b;
  private static final String TEST_STRING = "libdeflate-jni";

  private static final String TEST_SIGNED_OVERFLOW =
      "libdeflate-jni is a very awesome JNI binding for libdeflate. Check it out!";
  private static final long TEST_SIGNED_OVERFLOW_ADLER32 = 3926923840L;
  private static final long TEST_OFFSET_ADLER32 = 2969770419L;

  @Test
  void adler32Empty() {
    LibdeflateAdler32 adler32 = new LibdeflateAdler32();
    assertEquals(1, adler32.getValue());
  }

  @Test
  void adler32SingleByteUpdate() {
    LibdeflateAdler32 adler32 = new LibdeflateAdler32();
    adler32.update(0x42);
    assertEquals(4390979, adler32.getValue());
  }

  @Test
  void adler32ResetCorrectly() {
    LibdeflateAdler32 adler32 = new LibdeflateAdler32();
    adler32.update(0x42);
    assertEquals(4390979, adler32.getValue());

    adler32.reset();

    byte[] string = TEST_STRING.getBytes(StandardCharsets.US_ASCII);
    adler32.update(string);
    assertEquals(TEST_STRING_ADLER32, adler32.getValue());
  }

  @Test
  void adler32Heap() {
    byte[] string = TEST_STRING.getBytes(StandardCharsets.US_ASCII);

    LibdeflateAdler32 adler32 = new LibdeflateAdler32();
    adler32.update(string);
    assertEquals(TEST_STRING_ADLER32, adler32.getValue());
  }

  @Test
  void adler32HeapByteBuffer() {
    ByteBuffer buf = ByteBuffer.wrap(TEST_STRING.getBytes(StandardCharsets.US_ASCII));

    LibdeflateAdler32 adler32 = new LibdeflateAdler32();
    adler32.update(buf);
    assertEquals(TEST_STRING_ADLER32, adler32.getValue());
  }

  @Test
  void adler32DirectByteBuffer() {
    ByteBuffer buf = ByteBuffer.allocateDirect(TEST_STRING.length());
    buf.put(TEST_STRING.getBytes(StandardCharsets.US_ASCII));
    buf.flip();

    LibdeflateAdler32 adler32 = new LibdeflateAdler32();
    adler32.update(buf);
    assertEquals(TEST_STRING_ADLER32, adler32.getValue());
  }

  @Test
  void adler32SignedOverflow() {
    LibdeflateAdler32 adler32 = new LibdeflateAdler32();
    byte[] msg = TEST_SIGNED_OVERFLOW.getBytes(StandardCharsets.US_ASCII);
    adler32.update(msg, 0, msg.length);
    assertEquals(TEST_SIGNED_OVERFLOW_ADLER32, adler32.getValue());
  }

  @Test
  void adler32Offset() {
    LibdeflateAdler32 adler32 = new LibdeflateAdler32();
    byte[] msg = TEST_SIGNED_OVERFLOW.getBytes(StandardCharsets.US_ASCII);
    adler32.update(msg, 1, msg.length - 2);
    assertEquals(TEST_OFFSET_ADLER32, adler32.getValue());
  }
}
