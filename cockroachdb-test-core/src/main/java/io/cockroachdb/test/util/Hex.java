package io.cockroachdb.test.util;

public abstract class Hex {
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    private Hex() {
    }

    public static char[] encode(byte[] bytes) {
        int byteCount = bytes.length;
        char[] result = new char[2 * byteCount];
        int j = 0;

        for (byte b : bytes) {
            result[j++] = HEX_CHARS[(240 & b) >>> 4];
            result[j++] = HEX_CHARS[15 & b];
        }

        return result;
    }
}
