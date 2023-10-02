package io.cockroachdb.test.unpack;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cockroachdb.test.base.StepIOException;
import io.cockroachdb.test.util.FileUtils;

public abstract class FileExtractor {
    private FileExtractor() {
    }

    private static final Logger logger = LoggerFactory.getLogger(FileExtractor.class);

    private static final List<Extractor> extractorList = Arrays.asList(
            new GZipExtractor(),
            new ZipExtractor(),
            new TarExtractor(),
            new NoOpExtractor()
    );

    public static void extractFile(Path path, String mimeType, Path outputPath) throws IOException {
        ArchiveType archiveType = resolveArchiveType(path.toString());

        Extractor extractor = extractorList.stream().filter(e -> e.supports(archiveType)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown archive type " + archiveType));

        logger.debug("Extracting archive file [{}] of type [{}] mime type[{}] to {}",
                path, archiveType, mimeType, outputPath);

        extractor.extractTo(path, mimeType, outputPath);

        try {
            Files.walkFileTree(outputPath,
                    new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(
                                Path file, BasicFileAttributes attrs)
                                throws IOException {
                            try {
                                if (file.getFileName().normalize().startsWith("cockroach")) {
                                    Files.setPosixFilePermissions(file, FileUtils.PERMISSIONS);
                                }
                            } catch (UnsupportedOperationException e) {
                                // ok
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException e) {
            throw new StepIOException("I/O error setting file permissions", e);
        }
    }

    private static ArchiveType resolveArchiveType(String path) {
        if (path.endsWith("tar") || path.endsWith("tar.gz") || path.endsWith(".tgz")) {
            return ArchiveType.tar;
        } else if (path.endsWith(".gz")) {
            return ArchiveType.gzip;
        } else if (path.endsWith(".zip")) {
            return ArchiveType.zip;
        }
        return ArchiveType.unknown;
    }
}
