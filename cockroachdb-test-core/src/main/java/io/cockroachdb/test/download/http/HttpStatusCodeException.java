package io.cockroachdb.test.download.http;

public class HttpStatusCodeException extends HttpClientException {
    private final HttpStatus httpStatus;

    public HttpStatusCodeException(String message, HttpStatus httpStatus) {
        super(message + ": " + httpStatus.getFullPhrase());
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
