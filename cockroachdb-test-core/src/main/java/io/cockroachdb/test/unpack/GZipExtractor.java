package io.cockroachdb.test.unpack;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.GZIPInputStream;

public class GZipExtractor implements Extractor {
    @Override
    public boolean supports(ArchiveType archiveType) {
        return archiveType.equals(ArchiveType.gzip);
    }

    @Override
    public void extractTo(Path archive, String mimeType, Path destination) throws IOException {
        if (!Files.isDirectory(destination)) {
            Files.createDirectories(destination);
        }

        InputStream archiveStream = Files.newInputStream(archive);

        Path targetPath = destination.resolve(
                removeFileExtensions(archive.getFileName().toString()));

        try (BufferedInputStream inputStream = new BufferedInputStream(archiveStream);
             GZIPInputStream zipInputStream = new GZIPInputStream(inputStream)) {
            Files.copy(zipInputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String removeFileExtensions(String filename) {
        if (filename == null || filename.isEmpty()) {
            return filename;
        }
        return filename.replaceAll("(?<!^)[.].*", "");
    }
}

