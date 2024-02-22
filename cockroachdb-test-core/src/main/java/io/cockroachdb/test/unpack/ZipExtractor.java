package io.cockroachdb.test.unpack;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipExtractor implements Extractor {
    @Override
    public boolean supports(ArchiveType archiveType) {
        return archiveType.equals(ArchiveType.zip);
    }

    @Override
    public void extractTo(Path archive, String mimeType, Path destination) throws IOException {
        InputStream archiveStream = Files.newInputStream(archive);

        try (BufferedInputStream inputStream = new BufferedInputStream(archiveStream);
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {

            ZipEntry zipEntry = zipInputStream.getNextEntry();

            while (zipEntry != null) {
                Path entryPath = entryPath(destination, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!Files.isDirectory(entryPath)) {
                        Files.createDirectories(entryPath);
                    }
                } else {
                    // Windows-created archives are wierd
                    Path parent = entryPath.getParent();
                    if (!Files.isDirectory(parent)) {
                        Files.createDirectories(parent);
                    }

                    Files.copy(zipInputStream, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zipEntry = zipInputStream.getNextEntry();
            }
            zipInputStream.closeEntry();
        }
    }

    /**
     * Detect zip slip
     */
    private static Path entryPath(Path destination, ZipEntry zipEntry) throws IOException {
        Path destinationFile = destination.resolve(zipEntry.getName());
        if (!destinationFile.toAbsolutePath()
                .startsWith(destination.toAbsolutePath() + File.separator)) {
            throw new IOException("Zip entry is outside of the target dir: " + zipEntry.getName());
        }
        return destinationFile;
    }
}
