package io.cockroachdb.test.util;

public abstract class AssertThat {
    private AssertThat() {
    }

    public static void notNull(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }
    }
}
