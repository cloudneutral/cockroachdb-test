package io.cockroachdb.test.download.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import io.cockroachdb.test.util.StringUtils;

public class HttpResponse implements Closeable {
    public static final String ETAG = "ETag";

    public static final String CONTENT_TYPE = "Content-Type";

    private final HttpURLConnection connection;

    private final int responseCode;

    private InputStream inputStream;

    HttpResponse(HttpURLConnection connection, int responseCode) {
        this.connection = connection;
        this.responseCode = responseCode;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void close() {
        if (this.inputStream != null) {
            try {
                byte[] buffer = new byte[8196];
                while (inputStream.read(buffer) != -1) {
                }
                inputStream.close();
            } catch (IOException ex) {
                // ok
            }
        }
    }

    public InputStream getBody() throws IOException {
        InputStream errorStream = this.connection.getErrorStream();
        this.inputStream = (errorStream != null ? errorStream : this.connection.getInputStream());
        return this.inputStream;
    }

    public String getBodyAsString() throws IOException {
        try (InputStreamReader reader = new InputStreamReader(getBody(), StandardCharsets.UTF_8);
             StringWriter writer = new StringWriter()) {
            char[] buffer = new char[4096];

            int bytesRead;
            while ((bytesRead = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, bytesRead);
            }

            writer.flush();
            return writer.toString();
        }
    }

    public HttpHeaders getHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();

        String name = this.connection.getHeaderFieldKey(0);
        if (StringUtils.hasLength(name)) {
            headers.put(name, this.connection.getHeaderField(0));
        }

        for (int i = 1; ; i++) {
            name = this.connection.getHeaderFieldKey(i);
            if (!StringUtils.hasLength(name)) {
                break;
            }
            headers.put(name, this.connection.getHeaderField(i));
        }

        return new HttpHeaders(headers);
    }

    public URL getURL() {
        return connection.getURL();
    }

    public URI getURI() {
        try {
            return connection.getURL().toURI();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    public int getResponseCode() {
        return responseCode;
    }

    public HttpStatus getStatus() {
        return HttpStatus.valueOf(getResponseCode());
    }

    public String getContentType() {
        return connection.getContentType();
    }

    public String getContentEncoding() {
        return connection.getContentEncoding();
    }

    public long getContentLength() {
        return connection.getContentLengthLong();
    }

    public long getLastModified() {
        return connection.getLastModified();
    }
}
