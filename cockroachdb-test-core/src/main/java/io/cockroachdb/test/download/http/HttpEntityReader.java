package io.cockroachdb.test.download.http;

import java.io.IOException;

@FunctionalInterface
public interface HttpEntityReader<T> {
    T readBody(HttpResponse response) throws IOException;
}
