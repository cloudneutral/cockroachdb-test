package io.cockroachdb.test.download.http;

public class HttpEntity<T> {
    private final T body;

    private final HttpStatus status;

    private final HttpHeaders headers;

    private final String contentType;

    private final long contentLength;

    public HttpEntity(T body, HttpStatus status,
                      HttpHeaders headers,
                      String contentType,
                      long contentLength) {
        this.body = body;
        this.status = status;
        this.headers = headers;
        this.contentType = contentType;
        this.contentLength = contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public long getContentLength() {
        return contentLength;
    }

    public T getBody() {
        return body;
    }
}
