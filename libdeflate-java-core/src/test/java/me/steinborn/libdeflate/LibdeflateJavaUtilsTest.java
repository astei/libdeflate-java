package me.steinborn.libdeflate;

import org.junit.jupiter.api.Test;

import static me.steinborn.libdeflate.LibdeflateJavaUtils.checkBounds;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
