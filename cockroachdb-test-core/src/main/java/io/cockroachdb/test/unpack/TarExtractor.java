package io.cockroachdb.test.unpack;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import io.cockroachdb.test.util.StringUtils;

public class TarExtractor implements Extractor {
    @Override
    public boolean supports(ArchiveType archiveType) {
        return archiveType.equals(ArchiveType.tar);
    }

    @Override
    public void extractTo(Path archive, String mimeType, Path destination) throws IOException {
        if (!StringUtils.hasLength(mimeType)) {
            mimeType = Files.probeContentType(archive);
            if (mimeType == null) {
                mimeType = URLConnection.guessContentTypeFromName(archive.toString());
            }
            if (mimeType == null && archive.toString().endsWith(".tgz")) {
                // best effort
                mimeType = "application/x-gzip";
            }
        }

        boolean isGzip = mimeType.equals("application/x-gzip")
                || mimeType.equals("application/gzip");

        InputStream archiveStream = Files.newInputStream(archive);

        if (!Files.isDirectory(destination)) {
            Files.createDirectories(destination);
        }

        try (BufferedInputStream inputStream = new BufferedInputStream(archiveStream);
             TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(
                     isGzip ? new GzipCompressorInputStream(inputStream) : inputStream)) {
            ArchiveEntry entry;
            while ((entry = tarArchiveInputStream.getNextEntry()) != null) {
                Path extractToPath = destination.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(extractToPath);
                } else {
                    if (!Files.isDirectory(extractToPath.getParent())) {
                        Files.createDirectories(extractToPath.getParent());
                    }
                    Files.copy(tarArchiveInputStream, extractToPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}
