package io.cockroachdb.test.unpack;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit-test")
public class FileExtractorTest {
    static Path toPath(String resource) {
        try {
            return Paths.get(FileExtractorTest.class.getResource(resource).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    static Path zipFile() {
        return toPath("/unpack/files.zip");
    }

    static Path gzipFile() {
        return toPath("/unpack/files.gz");
    }

    static Path tarFile() {
        return toPath("/unpack/files.tar");
    }

    static Path tarGzFile() {
        return toPath("/unpack/files.tar.gz");
    }

    @Test
    public void givenZipFile_whenUnpack_thenExtractedToDestination() throws IOException {
        Path destination = Paths.get(System.getProperty("java.io.tmpdir")).resolve("unpack_zip");

        FileExtractor.extractFile(zipFile(), "", destination);

        try (Stream<Path> files = Files.list(destination)) {
            assertTrue(files.findFirst().isPresent());
        }
    }

//    @Test
    public void givenGZipFile_whenUnpack_thenExtractedToDestination() throws IOException {
        Path destination = Paths.get(System.getProperty("java.io.tmpdir")).resolve("unpack_gzip");

        FileExtractor.extractFile(gzipFile(), "",destination);

        try (Stream<Path> files = Files.list(destination)) {
            assertTrue(files.findFirst().isPresent());
        }
    }

    @Test
    public void givenTarFile_whenUnpack_thenExtractedToDestination() throws IOException {
        Path destination = Paths.get(System.getProperty("java.io.tmpdir")).resolve("unpack_tar");

        FileExtractor.extractFile(tarFile(), "",destination);

        try (Stream<Path> files = Files.list(destination)) {
            assertTrue(files.findFirst().isPresent());
        }
    }

//    @Test
    public void givenTarGzFile_whenUnpack_thenExtractedToDestination() throws IOException {
        Path destination = Paths.get(System.getProperty("java.io.tmpdir")).resolve("unpack_tar_gz");

        FileExtractor.extractFile(tarGzFile(), "",destination);

        try (Stream<Path> files = Files.list(destination)) {
            assertTrue(files.findFirst().isPresent());
        }
    }
}
