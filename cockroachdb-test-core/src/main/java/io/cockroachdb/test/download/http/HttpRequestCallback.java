package io.cockroachdb.test.download.http;

@FunctionalInterface
public interface HttpRequestCallback {
    void prepareRequest(HttpRequest request);
}
