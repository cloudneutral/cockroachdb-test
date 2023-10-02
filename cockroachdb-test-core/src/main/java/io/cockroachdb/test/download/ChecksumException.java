package io.cockroachdb.test.download;

import io.cockroachdb.test.download.http.HttpClientException;

public class ChecksumException extends HttpClientException {
    private final String url;

    private final String expectedChecksum;

    private final String actualChecksum;

    public ChecksumException(String url, String expectedChecksum, String actualChecksum) {
        super("Checksum error for ["
                + url + "]: expected [" + expectedChecksum + "] got ["
                + actualChecksum
                + "]");
        this.url = url;
        this.expectedChecksum = expectedChecksum;
        this.actualChecksum = actualChecksum;
    }

    public String getUrl() {
        return url;
    }

    public String getExpectedChecksum() {
        return expectedChecksum;
    }

    public String getActualChecksum() {
        return actualChecksum;
    }
}
