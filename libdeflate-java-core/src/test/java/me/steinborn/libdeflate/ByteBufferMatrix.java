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
import java.util.function.Function;

public enum ByteBufferMatrix {
  BOTH_HEAP(ByteBuffer::allocate, ByteBuffer::allocate),
  DIRECT_SOURCE(ByteBuffer::allocateDirect, ByteBuffer::allocate),
  DIRECT_DESTINATION(ByteBuffer::allocate, ByteBuffer::allocateDirect),
  BOTH_DIRECT(ByteBuffer::allocateDirect, ByteBuffer::allocateDirect);

  private final Function<Integer, ByteBuffer> sourceAlloc;
  private final Function<Integer, ByteBuffer> destAlloc;

  ByteBufferMatrix(
      Function<Integer, ByteBuffer> sourceAlloc, Function<Integer, ByteBuffer> destAlloc) {
    this.sourceAlloc = sourceAlloc;
    this.destAlloc = destAlloc;
  }

  public ByteBuffer allocateSource(int capacity) {
    return sourceAlloc.apply(capacity);
  }

  public ByteBuffer allocateDestination(int capacity) {
    return destAlloc.apply(capacity);
  }
}
