package io.cockroachdb.test;

public abstract class Constants {
    /**
     * Value object with CockroachDB process details.
     */
    public static final String PROCESS_DETAILS = "processDetails";

    /**
     * MIME-type of download binary.
     * Commonly "application/x-gzip".
     */
    public static final String MIME_TYPE = "mimeType";

    /**
     * Local path for downloaded binary.
     */
    public static final String BINARY_PATH_KEY = "binaryPath";

    /**
     * Local path for expanded binary.
     */
    public static String DESTINATION_PATH = "cockroach_test";

    private Constants() {
    }
}
