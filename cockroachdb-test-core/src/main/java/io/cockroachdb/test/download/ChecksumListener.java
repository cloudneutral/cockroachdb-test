package io.cockroachdb.test.download;

import io.cockroachdb.test.download.http.HttpResponse;
import io.cockroachdb.test.util.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class ChecksumListener implements DownloadListener {
    private final MessageDigest messageDigest;

    private long totalBytesRead;

    public ChecksumListener(String algorithm) {
        try {
            this.messageDigest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(algorithm, e);
        }
    }

    @Override
    public void bytesRead(HttpResponse response, byte[] buffer, int bytesRead) {
        messageDigest.update(buffer, 0, bytesRead);
        this.totalBytesRead += bytesRead;
    }

    @Override
    public void afterDownload(HttpResponse response) {
        if (totalBytesRead > 0) {
            verifyChecksum(response, new String(Hex.encode(messageDigest.digest())));
        }
    }

    protected abstract void verifyChecksum(HttpResponse response, String actualChecksum) throws
            ChecksumException;
}

