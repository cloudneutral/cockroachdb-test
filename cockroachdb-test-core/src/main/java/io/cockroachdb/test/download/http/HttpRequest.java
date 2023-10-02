package io.cockroachdb.test.download.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class HttpRequest {
    public static final String IF_NONE_MATCH = "If-None-Match";

    private final HttpURLConnection connection;

    private final HttpMethod httpMethod;

    private final Map<String, String> headers = new TreeMap<>();

    private long ifModifiedSince = -1;

    HttpRequest(HttpURLConnection connection, HttpMethod httpMethod) {
        this.connection = connection;
        this.httpMethod = httpMethod;
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public URL getUrl() {
        return connection.getURL();
    }

    public HttpMethod getMethod() {
        return httpMethod;
    }

    public long getIfModifiedSince() {
        return ifModifiedSince;
    }

    public HttpRequest setIfModifiedSince(long ifModifiedSince) {
        this.ifModifiedSince = ifModifiedSince;
        return this;
    }

    public HttpRequest putHeader(String key, String value) {
        headers.putIfAbsent(key, value);
        return this;
    }

    public HttpResponse execute() throws IOException {
        if (ifModifiedSince >= 0) {
            connection.setIfModifiedSince(ifModifiedSince);
        }

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }

        connection.connect();
        return new HttpResponse(connection, connection.getResponseCode());
    }
}
