package io.cockroachdb.test.download;

import io.cockroachdb.test.download.http.*;
import io.cockroachdb.test.util.AssertThat;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class FileDownload implements Callable<HttpEntity<Path>> {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final FileDownload instance = new FileDownload();

        public Builder withURL(URL url) {
            instance.url = url;
            return this;
        }

        public Builder withEtag(String etag) {
            instance.etag = etag;
            return this;
        }

        public Builder withChecksumListener(ChecksumListener listener) {
            instance.checksumListener = listener;
            return this;
        }

        public Builder withOutputPath(Path outputPath) {
            instance.outputPath = outputPath;
            return this;
        }

        public Builder withProgressCallback(ProgressCallback progressCallback) {
            instance.progressCallback = progressCallback;
            return this;
        }

        public FileDownload build() {
            AssertThat.notNull(instance.url);
            AssertThat.notNull(instance.outputPath);
            return instance;
        }

    }

    private URL url;

    private String etag;

    private Path outputPath;

    private ProgressCallback progressCallback;

    private ChecksumListener checksumListener;

    private FileDownload() {
    }

    @Override
    public HttpEntity<Path> call() {
        HttpClient httpClient = HttpClient.createDefault()
                .withRequestInterceptor(new GzipEncodingInterceptor());

        // Send conditional HEAD request to get proper headers
        HttpEntity<Path> headResponse = httpClient.execute(url.toExternalForm(),
                HttpMethod.HEAD,
                r -> outputPath,
                null);

        final List<DownloadListener> listeners = new ArrayList<>();

        if (progressCallback != null) {
            listeners.add(new ProgressListener(progressCallback));
        }

        if (checksumListener != null) {
            listeners.add(checksumListener);
        }

        HttpEntity<Path> getResponse = httpClient.execute(url.toExternalForm(),
                HttpMethod.GET,
                new FileEntityReader(outputPath, listeners),
                request -> request.putHeader(HttpRequest.IF_NONE_MATCH, etag));

        if (getResponse.getStatus().equals(HttpStatus.NOT_MODIFIED)) {
            return new HttpEntity<>(outputPath,
                    getResponse.getStatus(),
                    headResponse.getHeaders(),
                    headResponse.getContentType(),
                    headResponse.getContentLength());
        }

        return new HttpEntity<>(outputPath,
                getResponse.getStatus(),
                getResponse.getHeaders(),
                getResponse.getContentType(),
                getResponse.getContentLength());
    }
}
