package io.cockroachdb.test.util;

public abstract class StringUtils {
    private StringUtils() {
    }

    public static boolean hasLength(String text) {
        return text != null && text.length() != 0;
    }
}
