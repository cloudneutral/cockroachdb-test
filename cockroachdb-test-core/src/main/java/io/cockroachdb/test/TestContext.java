package io.cockroachdb.test;

public interface TestContext {
    void put(String key, Object value);

    <T> T get(String key, Class<T> expectedType);
}
