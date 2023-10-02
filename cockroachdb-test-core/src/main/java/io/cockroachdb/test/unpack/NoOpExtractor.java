package io.cockroachdb.test.unpack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class NoOpExtractor implements Extractor {
    @Override
    public boolean supports(ArchiveType archiveType) {
        return archiveType.equals(ArchiveType.unknown);
    }

    @Override
    public void extractTo(Path archive, String mimeType, Path destination) throws IOException {
        if (!Files.isDirectory(destination)) {
            Files.createDirectories(destination);
        }
        Files.copy(archive, destination, StandardCopyOption.REPLACE_EXISTING);
    }
}
