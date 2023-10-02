package io.cockroachdb.test.unpack;

import io.cockroachdb.test.Cockroach;
import io.cockroachdb.test.TestContext;
import io.cockroachdb.test.base.Step;
import io.cockroachdb.test.base.StepException;
import io.cockroachdb.test.base.StepIOException;
import io.cockroachdb.test.util.OperatingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class UnpackStep implements Step {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void setUp(TestContext testContext, Cockroach cockroach) throws StepException {
        Path tempFile = testContext.get(TestContext.BINARY_PATH, Path.class);
        String mimeType = testContext
                .get(TestContext.MIME_TYPE, String.class);

        Path destination = Paths.get(OperatingSystem.TEMP_DIR).resolve("cockroach_test");

        logger.info("Unpacking CockroachDB binary: {}", tempFile);

        try {
            FileExtractor.extractFile(tempFile, mimeType, destination);
        } catch (IOException e) {
            throw new StepIOException("I/O error unpacking binary", e);
        }
    }

    @Override
    public void cleanUp(TestContext testContext, Cockroach cockroach) throws StepException {
        Path destination = Paths.get(OperatingSystem.TEMP_DIR).resolve("cockroach_test");

        if (!Files.isDirectory(destination)) {
            throw new StepIOException("Not a directory: " + destination);
        }

        logger.info("Cleaning up CockroachDB binaries: {}", destination);

        try {
            Files.walkFileTree(destination,
                    new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult postVisitDirectory(
                                Path dir, IOException exc) throws IOException {
                            logger.debug("Delete dir: {}", dir);
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(
                                Path file, BasicFileAttributes attrs)
                                throws IOException {
                            logger.debug("Delete file: {}", file);
                            Files.delete(file);
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException e) {
            throw new StepIOException("I/O error cleaning binaries", e);
        }
    }
}
