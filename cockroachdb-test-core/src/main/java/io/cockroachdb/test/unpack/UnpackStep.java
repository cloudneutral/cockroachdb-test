package io.cockroachdb.test.unpack;

import io.cockroachdb.test.Cockroach;
import io.cockroachdb.test.TestContext;
import io.cockroachdb.test.Constants;
import io.cockroachdb.test.Step;
import io.cockroachdb.test.StepException;
import io.cockroachdb.test.StepIOException;
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
        Path tempFile = testContext.get(Constants.BINARY_PATH_KEY, Path.class);
        String mimeType = testContext.get(Constants.MIME_TYPE, String.class);
        Path destination = OperatingSystem.TEMP_DIR.resolve(Constants.DESTINATION_PATH);

        logger.info("Unpacking CockroachDB binary: {}", tempFile);

        try {
            FileExtractor.extractFile(tempFile, mimeType, destination);
        } catch (IOException e) {
            throw new StepIOException("I/O error unpacking binary", e);
        }
    }

    @Override
    public void cleanUp(TestContext testContext, Cockroach cockroach) throws StepException {
        Path destination = OperatingSystem.TEMP_DIR.resolve(Constants.DESTINATION_PATH);

        if (!Files.isDirectory(destination)) {
            logger.debug("Nothing to clean up: {}", destination);
            return;
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
