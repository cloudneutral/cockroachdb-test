package io.cockroachdb.test.download;

import io.cockroachdb.test.download.http.*;
import io.cockroachdb.test.base.StepIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Callable;

public class ChecksumDownload implements Callable<String> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String url;

    public ChecksumDownload(String url) {
        this.url = url;
    }

    @Override
    public String call() {
        try {
            HttpRequest request = HttpClient.createDefault().createRequest(url, HttpMethod.GET);
            try (HttpResponse response = request.execute()) {
                HttpStatus status = response.getStatus();
                if (status.is2xxSuccessful()) {
                    String body = response.getBodyAsString();
                    String[] parts = body.split("\\s+");
                    if (parts.length == 2) {
                        return parts[0];
                    } else {
                        logger.error("Unexpected checksum format [{}]: {}", url, body);
                    }
                } else {
                    logger.warn("HTTP error reading SHA-256 checksum [{}]: {}", url,
                            response.getStatus().getFullPhrase());
                }
            }
        } catch (IOException e) {
            throw new StepIOException("I/O error reading checksum", e);
        }

        return "";
    }
}
