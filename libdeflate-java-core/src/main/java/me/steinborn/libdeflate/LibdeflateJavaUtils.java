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

import java.nio.ByteBuffer;

class LibdeflateJavaUtils {
  private LibdeflateJavaUtils() {}

  static void checkBounds(int backingLen, int userOffset, int userLen) {
    if (userOffset < 0) {
      throw new IndexOutOfBoundsException("userOffset = " + userOffset);
    }
    if (userLen < 0) {
      throw new IndexOutOfBoundsException("userLen = " + userLen);
    }
    int fullRange = userLen + userOffset;
    if (fullRange > backingLen) {
      throw new IndexOutOfBoundsException(
          "userOffset+userLen(" + fullRange + ") > backingLen(" + userOffset + ")");
    }
  }

  static int byteBufferArrayPosition(ByteBuffer buffer) {
    return buffer.arrayOffset() + buffer.position();
  }
}
