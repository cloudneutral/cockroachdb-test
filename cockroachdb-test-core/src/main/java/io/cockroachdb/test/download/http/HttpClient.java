package io.cockroachdb.test.download.http;

import io.cockroachdb.test.util.AssertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class HttpClient {
    private static final int CONNECTION_TIMEOUT = 10 * 10000;

    private final List<HttpRequestInterceptor> requestInterceptors = new ArrayList<>();

    private int readTimeout = CONNECTION_TIMEOUT;

    private int connectionTimeout = CONNECTION_TIMEOUT;

    public static String resolve(String baseUrl, Path path) {
        AssertThat.notNull(baseUrl);
        AssertThat.notNull(path);
        StringBuilder sb = new StringBuilder(baseUrl);
        for (Path part : path) {
            if (!sb.toString().endsWith("/")) {
                sb.append("/");
            }
            sb.append(part.toString());
        }
        return sb.toString();
    }

    public static HttpClient createDefault() {
        HttpClient httpClient = new HttpClient();
        httpClient.withConnectionTimeout(CONNECTION_TIMEOUT);
        httpClient.withReadTimeout(CONNECTION_TIMEOUT);
        httpClient.withRequestInterceptor(new UserAgentInterceptor());
        return httpClient;
    }

    public HttpClient withReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public HttpClient withConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public HttpClient withRequestInterceptor(HttpRequestInterceptor interceptor) {
        this.requestInterceptors.add(interceptor);
        return this;
    }

    public <E> HttpEntity<E> execute(String url,
                                     HttpMethod method,
                                     HttpEntityReader<E> entityReader,
                                     HttpRequestCallback callback) throws HttpClientException {
        AssertThat.notNull(url);

        try {
            HttpResponse response = executeMethod(url, method, callback);

            if (!response.getStatus().isSuccessful()) {
                throw new HttpStatusCodeException("Unexpected status on " + method
                        + " request for '" + url, response.getStatus());
            }

            E body = entityReader.readBody(response);

            return new HttpEntity<>(body,
                    response.getStatus(), response.getHeaders(), response.getContentType(), response.getContentLength());
        } catch (IOException ex) {
            throw newHttpAccessException(method, url, ex);
        }
    }

    public HttpResponse get(String url) throws HttpClientException {
        try {
            return createRequest(url, HttpMethod.GET).execute();
        } catch (IOException ex) {
            throw newHttpAccessException(HttpMethod.GET, url, ex);
        }
    }

    public HttpResponse executeMethod(String url,
                                      HttpMethod method,
                                      HttpRequestCallback callback)
            throws HttpClientException {
        try {
            HttpRequest request = createRequest(url, method);
            if (callback != null) {
                callback.prepareRequest(request);
            }

            return request.execute();
        } catch (IOException ex) {
            throw newHttpAccessException(method, url, ex);
        }
    }

    public HttpRequest createRequest(String url, HttpMethod httpMethod)
            throws HttpClientException, IOException {
        AssertThat.notNull(url);
        AssertThat.notNull(httpMethod);

        HttpRequest request = new HttpRequest(
                openConnection(new URL(url), httpMethod), httpMethod);

        for (HttpRequestInterceptor interceptor : requestInterceptors) {
            request = interceptor.intercept(request);
        }

        return request;
    }

    private HttpURLConnection openConnection(URL url, HttpMethod httpMethod) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(connectionTimeout);
        connection.setReadTimeout(readTimeout);
        connection.setDoInput(true);
        connection.setDoOutput(false);
        connection.setInstanceFollowRedirects(httpMethod.equals(HttpMethod.GET));
        connection.setRequestMethod(httpMethod.name());
        return connection;
    }

    private HttpAccessException newHttpAccessException(HttpMethod method, String url, Throwable ex) {
        return new HttpAccessException("I/O error on " + method + " request for '" + url
                + "': " + ex.toString(), ex);
    }
}
