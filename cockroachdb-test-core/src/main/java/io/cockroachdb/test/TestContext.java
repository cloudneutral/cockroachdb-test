package io.cockroachdb.test;

public interface TestContext {
    /**
     * Value object with CockroachDB process details.
     */
    String COCKROACH_DETAILS = "cockroachDetails";

    /**
     * MIME-type of download binary.
     * Commonly "application/x-gzip".
     */
    String MIME_TYPE = "mimeType";

    /**
     * Local path for downloaded binary.
     */
    String BINARY_PATH = "binaryPath";

    void put(String key, Object value);

    <T> T get(String key, Class<T> expectedType);
}
