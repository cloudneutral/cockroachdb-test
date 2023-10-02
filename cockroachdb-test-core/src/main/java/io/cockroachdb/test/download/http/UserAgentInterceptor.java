package io.cockroachdb.test.download.http;

public class UserAgentInterceptor implements HttpRequestInterceptor {
    private static final String MOZILLA_AGENT
            = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";

    private final String userAgent;

    public UserAgentInterceptor() {
        this(MOZILLA_AGENT);
    }

    public UserAgentInterceptor(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public HttpRequest intercept(HttpRequest request) {
        request.putHeader("User-Agent", userAgent);
        return request;
    }
}
