package io.cockroachdb.test.download.http;

@FunctionalInterface
public interface HttpRequestInterceptor {
    HttpRequest intercept(HttpRequest request);
}
