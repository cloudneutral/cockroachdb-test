package io.cockroachdb.test.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit-test")
public class HexUtilsTest {
    @Test
    public void whenConvertingBytesToHex_expectHexString() {
        String hex = Hex.toHex("The quick brown fox jumps over the lazy dog".getBytes());
        Assertions.assertEquals(
                "54686520717569636b2062726f776e20666f78206a756d7073206f76657220746865206c617a7920646f67", hex);

        char[] chars = Hex.toHexChars("The quick brown fox jumps over the lazy dog".getBytes());

        Assertions.assertArrayEquals(
                "54686520717569636b2062726f776e20666f78206a756d7073206f76657220746865206c617a7920646f67".toCharArray(),
                chars);

        String text = Hex.fromHex(
                "54686520717569636b2062726f776e20666f78206a756d7073206f76657220746865206c617a7920646f67".toCharArray());
        Assertions.assertEquals("The quick brown fox jumps over the lazy dog", text);
    }
}
