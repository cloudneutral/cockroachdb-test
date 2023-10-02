package io.cockroachdb.test.download.http;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class HttpHeaders implements Iterable<Map.Entry<String, String>> {
    private final Map<String, String> headers;

    HttpHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Set<String> keys() {
        return headers.keySet();
    }

    public String get(String key, String defaultValue) {
        return headers.getOrDefault(key, defaultValue);
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return headers.entrySet().iterator();
    }
}
