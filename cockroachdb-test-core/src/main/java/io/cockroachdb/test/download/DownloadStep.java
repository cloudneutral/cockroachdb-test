package io.cockroachdb.test.download;

import io.cockroachdb.test.URLResolver;
import io.cockroachdb.test.Cockroach;
import io.cockroachdb.test.TestContext;
import io.cockroachdb.test.download.http.HttpClientException;
import io.cockroachdb.test.download.http.HttpEntity;
import io.cockroachdb.test.download.http.HttpResponse;
import io.cockroachdb.test.Step;
import io.cockroachdb.test.StepException;
import io.cockroachdb.test.StepIOException;
import io.cockroachdb.test.util.ByteFormat;
import io.cockroachdb.test.util.OperatingSystem;
import io.cockroachdb.test.util.TimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.cockroachdb.test.Constants.BINARY_PATH_KEY;
import static io.cockroachdb.test.Constants.MIME_TYPE;

/**
 * Test step that downloads the CockroachDB binary over HTTP.
 * Uses conditional request with ETags for client-side caching.
 * <p>
 * Linux:
 * <a href="https://binaries.cockroachdb.com/cockroach-v23.2.1.linux-amd64.tgz">linux-amd64</a>
 * <a href="https://binaries.cockroachdb.com/cockroach-v23.2.1.linux-arm64.tgz">linux-arm64</a>
 * Mac:
 * <a href="https://binaries.cockroachdb.com/cockroach-v23.2.1.darwin-10.9-amd64.tgz">darwin-amd64</a>
 * <a href="https://binaries.cockroachdb.com/cockroach-v23.2.1.darwin-11.0-arm64.tgz">darwin-arm64</a>
 * Windows:
 * <a href="https://binaries.cockroachdb.com/cockroach-v23.2.1.windows-6.2-amd64.zip">windows-amd64</a>
 */
public class DownloadStep implements Step {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static URL resolveBinaryURL(Cockroach cockroach) {
        try {
            URLResolver resolver = cockroach.binaryResolver()
                    .getDeclaredConstructor().newInstance();
            return resolver.resolveBinaryURL(cockroach);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new IllegalStateException("Error loading binary URL resolver", e);
        }
    }

    @Override
    public void setUp(TestContext testContext, Cockroach cockroach) {
        URL binaryURL = resolveBinaryURL(cockroach);
        Path fileName = Paths.get(binaryURL.getFile()).getFileName();
        Path tempFile = OperatingSystem.TEMP_DIR.resolve(fileName);
        Path etagFile = Paths.get(tempFile + ".etag");

        try {
            String etag = readEtag(tempFile, etagFile);

            FileDownload fileDownload = FileDownload.builder()
                    .withURL(binaryURL)
                    .withEtag(etag)
                    .withOutputPath(tempFile)
                    .withChecksumListener(new ChecksumListener("SHA-256") {
                        @Override
                        protected void verifyChecksum(HttpResponse response, String actualChecksum)
                                throws ChecksumException {
                            // Defer reading checksum until its actually needed
                            String expectedChecksum = readChecksum(binaryURL);
                            if (!actualChecksum.equals(expectedChecksum)) {
                                throw new ChecksumException(binaryURL.toExternalForm(),
                                        expectedChecksum, actualChecksum);
                            } else {
                                logger.info("SHA-256 verified [{}]", expectedChecksum);
                            }
                        }
                    })
                    .withProgressCallback((progress, averageSpeedBps, totalBytes, remainingMillis) -> {
                        int ticks = Math.max(0, (int) (30 * progress / 100.0) - 1);
                        System.out.printf(
                                "%5.1f%%[%-30s] %6s at %.1fMB/s eta %-10s\n",
                                progress,
                                new String(new char[ticks]).replace('\0', '#') + ">",
                                ByteFormat.byteCountToDisplaySize(totalBytes),
                                averageSpeedBps,
                                TimeFormat.millisecondsToDisplayString(remainingMillis)
                        );
                        System.out.flush();
                    })
                    .build();

            logger.info("""
                            CockroachDB binary download request:
                                   URL: {}
                            Local file: {}
                                  Etag: {}""",
                    binaryURL.toExternalForm(),
                    tempFile,
                    etag);

            // Fire away!
            HttpEntity<Path> responseEntity = fileDownload.call();

            String mimeTye = responseEntity.getHeaders().get(HttpResponse.CONTENT_TYPE, "");

            // Store etag
            String lastEtag = responseEntity.getHeaders().get(HttpResponse.ETAG, "");
            Files.writeString(etagFile, lastEtag);

            // Put in context
            testContext.put(BINARY_PATH_KEY, responseEntity.getBody());
            testContext.put(MIME_TYPE, mimeTye);

            // Wrap-up
            logger.info("""
                            CockroachDB binary download response:
                                     Status: {}
                             Content-Length: {}
                                  Mime-Type: {}
                                       Etag: {}""",
                    responseEntity.getStatus().getFullPhrase(),
                    ByteFormat.byteCountToDisplaySize(responseEntity.getContentLength()),
                    mimeTye,
                    lastEtag);
        } catch (HttpClientException e) {
            throw new StepIOException("HTTP error downloading binary", e);
        } catch (IOException e) {
            throw new StepIOException("I/O error downloading binary", e);
        }
    }

    private String readChecksum(URL binaryURL) {
        return new ChecksumDownload(binaryURL.toExternalForm() + ".sha256sum").call();
    }

    private String readEtag(Path tempFile, Path etagFile) throws IOException {
        String etag = "";
        if (Files.exists(tempFile, LinkOption.NOFOLLOW_LINKS)) {
            if (Files.isRegularFile(etagFile)) {
                etag = Files.readString(etagFile);
                logger.debug("Found local binary and stored etag: {}", etag);
            } else {
                logger.debug("Found local binary but no stored etag");
            }
        } else {
            logger.debug("Found no local binary");
        }
        return etag;
    }

    @Override
    public void cleanUp(TestContext testContext, Cockroach cockroach) throws StepException {
        if (cockroach.cacheBinary()) {
            logger.info("Skip cleanup of CockroachDB binary (cached)");
            return;
        }

        try {
            Path tempFile = testContext.get(BINARY_PATH_KEY, Path.class);
            logger.info("Delete CockroachDB binary: {}", tempFile);
            if (tempFile != null && Files.isRegularFile(tempFile)) {
                Files.deleteIfExists(tempFile);
            }
        } catch (IOException e) {
            throw new StepIOException("Cleanup of binary failed", e);
        }
    }
}
