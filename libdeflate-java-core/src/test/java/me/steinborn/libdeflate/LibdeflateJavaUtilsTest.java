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

import static me.steinborn.libdeflate.LibdeflateJavaUtils.checkBounds;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class LibdeflateJavaUtilsTest {
  @Test
  void testSaneCheckInBounds() {
    assertDoesNotThrow(() -> checkBounds(1, 0, 1));
  }

  @Test
  void testZeroLengthSane() {
    assertDoesNotThrow(() -> checkBounds(0, 0, 0));
  }

  @Test
  void testZeroOffsetEndSane() {
    assertDoesNotThrow(() -> checkBounds(2, 2, 0));
  }

  @Test
  void testNonZeroOffsetAndLengthSane() {
    assertDoesNotThrow(() -> checkBounds(40, 2, 20));
  }

  @Test
  void testNegativeOffset() {
    assertThrows(IndexOutOfBoundsException.class, () -> checkBounds(0, -1, 0));
  }

  @Test
  void testNegativeLen() {
    assertThrows(IndexOutOfBoundsException.class, () -> checkBounds(0, 0, -1));
  }

  @Test
  void testTooBigOffset() {
    assertThrows(IndexOutOfBoundsException.class, () -> checkBounds(0, 1, 0));
  }

  @Test
  void testTooBigLen() {
    assertThrows(IndexOutOfBoundsException.class, () -> checkBounds(0, 0, 1));
  }

  @Test
  void testTooSmallOffsetAndLen() {
    assertThrows(IndexOutOfBoundsException.class, () -> checkBounds(200, 50, 300));
  }
}
