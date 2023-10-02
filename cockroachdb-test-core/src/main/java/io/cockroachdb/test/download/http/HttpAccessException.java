package io.cockroachdb.test.download.http;

/**
 * Exception thrown on I/O errors, typically socket connection failures.
 */
public class HttpAccessException extends HttpClientException {
    public HttpAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
