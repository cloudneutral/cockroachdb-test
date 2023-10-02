package io.cockroachdb.test.download;

import io.cockroachdb.test.download.http.HttpResponse;

public interface DownloadListener {
    /**
     * Invoked before response body is being read.
     */
    default void beforeDownload(HttpResponse response) {
    }

    /**
     * Invoked when a block of bytes is read from the response body input stream.
     */
    default void bytesRead(HttpResponse response, byte[] buffer, int bytesRead) {

    }

    /**
     * Invoked after entire response body has been read.
     */
    default void afterDownload(HttpResponse response) {
    }
}
