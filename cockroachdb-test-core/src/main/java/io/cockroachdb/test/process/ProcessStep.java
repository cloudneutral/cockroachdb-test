package io.cockroachdb.test.process;

import io.cockroachdb.test.Cockroach;
import io.cockroachdb.test.CockroachDetails;
import io.cockroachdb.test.TestContext;
import io.cockroachdb.test.base.Step;
import io.cockroachdb.test.base.StepException;
import io.cockroachdb.test.base.StepIOException;
import io.cockroachdb.test.util.OperatingSystem;
import io.cockroachdb.test.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.cockroachdb.test.TestContext.COCKROACH_DETAILS;

public class ProcessStep implements Step {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Process process;

    @Override
    public void setUp(TestContext testContext, Cockroach cockroach) throws StepException {
        Path destination = Paths.get(OperatingSystem.TEMP_DIR).resolve("cockroach_test");

        Path executable = findExecutable(destination);
        if (executable == null) {
            throw new StepIOException("Unable to find executable in: " + destination);
        }

        Path workingDir = executable.getParent();

        try {
            List<String> command = CommandBuilder.toCommand(executable, cockroach);

            logger.info("Starting CockroachDB process using working dir [{}] and commands ({}):",
                    workingDir, command.size());
            AtomicInteger c = new AtomicInteger();
            command.forEach(s -> logger.info("[{}] {}", c.getAndIncrement(), s));

            this.process = new ProcessBuilder()
                    .directory(workingDir.toFile())
                    .command(command)
                    .inheritIO()
                    .start();
        } catch (IOException e) {
            throw new StepIOException("I/O error starting CockroachDB process", e);
        }

        if (!process.isAlive()) {
            throw new StepIOException("CockroachDB process failed");
        }

        logger.info("CockroachDB process started (pid: {}) - waiting for node to get ready", process.pid());

        String connectionURL = waitForNodeReady(workingDir, cockroach);

        CockroachDetails details = CockroachDetailsBuilder.fromConnectionURL(connectionURL);

        testContext.put(COCKROACH_DETAILS, details);

        logger.info("""
                Node is ready!
                 Connection URL: {}
                       JDBC URL: {}
                           user: {}
                            pwd: {}""",
                connectionURL,
                details.getJdbcURL(),
                details.getUser(),
                details.getPassword()
        );
    }

    private Path findExecutable(Path destination) {
        AtomicReference<Path> executableRef = new AtomicReference<>();

        try {
            Files.walkFileTree(destination,
                    new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(
                                Path file, BasicFileAttributes attrs) {
                            if (Files.isExecutable(file)) {
                                executableRef.set(file);
                                return FileVisitResult.TERMINATE;
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException e) {
            throw new StepIOException("I/O error locating executable", e);
        }

        return executableRef.get();
    }

    private String waitForNodeReady(Path workingDir, Cockroach setup) {
        String listeningURLFile = setup.demoFlags().listeningURLFile();
        if (StringUtils.hasLength(listeningURLFile)) {
            Path urlFile = workingDir.resolve(listeningURLFile);

            for (Instant deadline = Instant.now().plus(Duration.ofSeconds(30));
                 Instant.now().isBefore(deadline); ) {
                try {
                    if (Files.isReadable(urlFile)) {
                        return Files.readString(urlFile).trim();
                    } else {
                        Thread.sleep(500);
                    }
                } catch (IOException | InterruptedException e) {
                    throw new StepIOException(e);
                }
            }

            throw new StepIOException("Timeout waiting for node to get ready");
        }
        throw new UnsupportedOperationException("Expected listening file URL");
    }

    @Override
    public void cleanUp(TestContext testContext, Cockroach cockroach) throws StepException {
        if (process != null) {
            boolean force = false;
            while (process.isAlive()) {
                logger.info("Stopping pid [{}]", process.pid());
                try {
                    if (!force && process.supportsNormalTermination()) {
                        process.destroy();
                    } else {
                        process.destroyForcibly();
                    }
                    boolean exited = process.waitFor(10, TimeUnit.SECONDS);
                    logger.info("Process destruction {}", exited ? "completed" : "expired");
                    if (!exited) {
                        force = true;
                    } else {
                        logger.debug("Waiting another 5s before quitting");
                        Thread.sleep(5000);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
