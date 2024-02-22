package io.cockroachdb.test.download;

import io.cockroachdb.test.download.http.HttpClient;
import io.cockroachdb.test.download.http.HttpMethod;
import io.cockroachdb.test.download.http.HttpRequest;
import io.cockroachdb.test.download.http.HttpResponse;
import io.cockroachdb.test.util.ByteFormat;
import io.cockroachdb.test.util.TimeFormat;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

@Tag("integration-test")
public class FileDownloadTest {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void whenDownloadingCRDBBinary_thenWriteToTempFiles() throws Exception {
        URL fileURL = new URL("https://binaries.cockroachdb.com/cockroach-v23.2.1.windows-6.2-amd64.zip");
        String checksum = "";

        HttpRequest request = HttpClient.createDefault().createRequest(fileURL + ".sha256sum", HttpMethod.GET);

        try (HttpResponse response = request.execute()) {
            String body = response.getBodyAsString();
            String[] parts = body.split("\\s+");
            if (parts.length == 2) {
                checksum = parts[0];
            }
        }

        Path fileName = Paths.get(fileURL.getFile()).getFileName();
        Path tempFile = Paths.get(System.getProperty("java.io.tmpdir")).resolve(fileName);

        FileDownload fileDownload = FileDownload.builder()
                .withURL(fileURL)
                .withOutputPath(tempFile)
                .withChecksumListener(new ChecksumListener("SHA-256") {
                    @Override
                    protected void verifyChecksum(HttpResponse response, String actualChecksum) throws ChecksumException {
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

        fileDownload.call();

        logger.info("Downloaded {} => {} (checksum: {})", fileURL, tempFile, checksum);
    }
}
