package io.cockroachdb.test.download;

import io.cockroachdb.test.download.http.HttpEntityReader;
import io.cockroachdb.test.download.http.HttpResponse;
import io.cockroachdb.test.download.http.HttpStatus;
import io.cockroachdb.test.util.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import static java.nio.file.StandardOpenOption.*;

public class FileEntityReader implements HttpEntityReader<Path> {
    private static final int BUFFER_SIZE = 8196;

    private final Path outputPath;

    private final Path outputPartPath;

    private final List<DownloadListener> inputStreamListeners;

    public FileEntityReader(Path outputPath) {
        this(outputPath, Collections.emptyList());
    }

    public FileEntityReader(Path outputPath, List<DownloadListener> inputStreamListeners) {
        this.outputPath = outputPath;
        this.outputPartPath = outputPath.resolveSibling(outputPath.getFileName() + ".part");
        this.inputStreamListeners = inputStreamListeners;
    }

    @Override
    public Path readBody(HttpResponse response) throws IOException {
        if (!Files.isDirectory(outputPartPath.getParent())) {
            Files.createDirectories(outputPartPath.getParent());
        }

        if (response.getStatus().equals(HttpStatus.NOT_MODIFIED)) {
            return outputPath;
        }

        try (InputStream is = openInputStream(response);
             OutputStream os = Files.newOutputStream(outputPartPath, WRITE, CREATE, TRUNCATE_EXISTING)) {

            inputStreamListeners.forEach(listener -> listener.beforeDownload(response));

            int bytesRead;
            byte[] buffer = new byte[BUFFER_SIZE];

            while ((bytesRead = is.read(buffer)) != -1) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedIOException();
                }
                os.write(buffer, 0, bytesRead);

                final int bytes = bytesRead;
                inputStreamListeners.forEach(interceptor -> interceptor.bytesRead(response, buffer, bytes));
            }

            inputStreamListeners.forEach(interceptor -> interceptor.afterDownload(response));

            Files.setLastModifiedTime(outputPartPath, FileTime.fromMillis(response.getLastModified()));
            Files.move(outputPartPath, outputPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            try {
                Files.deleteIfExists(outputPartPath);
            } catch (IOException ex) {
                // ok
            }
            throw e;
        } finally {
            try {
                Files.setPosixFilePermissions(outputPath, FileUtils.PERMISSIONS);
            } catch (UnsupportedOperationException e) {
                // ok
            }
        }

        return outputPath;
    }

    private InputStream openInputStream(HttpResponse response) throws IOException {
        String contentEncoding = response.getContentEncoding();
        if ("gzip".equalsIgnoreCase(contentEncoding)) {
            return new GZIPInputStream(response.getBody());
        } else if ("deflate".equalsIgnoreCase(contentEncoding)) {
            return new InflaterInputStream(response.getBody(), new Inflater(true));
        }
        return response.getBody();
    }
}