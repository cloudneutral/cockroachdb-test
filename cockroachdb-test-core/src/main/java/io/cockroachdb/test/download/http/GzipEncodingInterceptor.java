package io.cockroachdb.test.download.http;

public class GzipEncodingInterceptor implements HttpRequestInterceptor {
    @Override
    public HttpRequest intercept(HttpRequest request) {
        if (HttpMethod.GET.equals(request.getMethod())) {
            request.putHeader("Accept-Encoding", "gzip,deflate");
        }
        return request;
    }
}
