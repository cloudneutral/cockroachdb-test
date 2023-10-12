package io.cockroachdb.test.process;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cockroachdb.test.Cockroach;
import io.cockroachdb.test.ProcessDetails;
import io.cockroachdb.test.TestContext;
import io.cockroachdb.test.base.Constants;
import io.cockroachdb.test.base.Step;
import io.cockroachdb.test.base.StepException;
import io.cockroachdb.test.base.StepIOException;
import io.cockroachdb.test.base.StepProcessException;
import io.cockroachdb.test.util.OperatingSystem;
import io.cockroachdb.test.util.StringUtils;

import static io.cockroachdb.test.TestContext.COCKROACH_DETAILS;

public class ProcessStep implements Step {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Logger processLogger = LoggerFactory.getLogger("COCKROACH_PROCESS");

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private Process process;

    @Override
    public void setUp(TestContext testContext, Cockroach cockroach) throws StepException {
        String listeningURLFile = cockroach.demoFlags().listeningURLFile();
        if (!StringUtils.hasLength(listeningURLFile)) {
            throw new UnsupportedOperationException("Expected listening file URL parameter");
        }

        Path executable = findExecutable(OperatingSystem.TEMP_DIR.resolve(Constants.DESTINATION));
        if (executable == null) {
            throw new StepProcessException("Unable to find CockroachDB executable");
        }

        final Path workingDir = executable.getParent();

        Process process;

        try {
            List<String> command = CommandBuilder.toCommand(executable, cockroach);

            logger.info("Starting CockroachDB process using working dir [{}] and commands ({}):",
                    workingDir, command.size());
            AtomicInteger c = new AtomicInteger();
            command.forEach(s -> logger.info("[{}] {}", c.getAndIncrement(), s));

            if (StringUtils.hasLength(cockroach.redirectProcessOutputToFile())) {
                Path logFile = OperatingSystem.TEMP_DIR.resolve(cockroach.redirectProcessOutputToFile());

                logger.info("Redirecting process stderr and stdout to {}", logFile);

                process = new ProcessBuilder()
                        .directory(workingDir.toFile())
                        .command(command)
                        .redirectErrorStream(true)
                        .redirectOutput(ProcessBuilder.Redirect.appendTo(logFile.toFile()))
                        .start();
            } else {
                process = new ProcessBuilder()
                        .directory(workingDir.toFile())
                        .command(command)
                        .redirectErrorStream(true)
                        .start();

                if (processLogger.isDebugEnabled()) {
                    executorService.submit(new ProcessOutputReader(process.getInputStream(), processLogger::debug));
                }
            }
        } catch (IOException e) {
            throw new StepProcessException("I/O error starting CockroachDB process", e);
        }

        if (!process.isAlive()) {
            throw new StepProcessException("CockroachDB process start failed");
        }

        logger.info("CockroachDB process started (pid: {}) - waiting for node to get ready", process.pid());

        String connectionURL = waitForNodeReady(workingDir, listeningURLFile, cockroach.nodeStartupWaitSeconds());

        final ProcessDetails details = DetailsBuilder.fromConnectionURL(connectionURL);

        testContext.put(COCKROACH_DETAILS, details);

        logger.info("""
                        Node is ready!
                         Connection URL: {}
                               JDBC URL: {}
                                   User: {}
                               Password: {}""",
                connectionURL,
                details.getJdbcURL(),
                details.getUser(),
                details.getPassword()
        );

        this.process = process;
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
            throw new StepProcessException("I/O error locating executable", e);
        }

        return executableRef.get();
    }

    private String waitForNodeReady(Path workingDir, String listeningURLFile, int delaySeconds) {
        for (Instant deadline = Instant.now().plus(Duration.ofSeconds(delaySeconds));
             Instant.now().isBefore(deadline); ) {
            try {
                TimeUnit.MILLISECONDS.sleep(5000);
                Path urlFile = workingDir.resolve(listeningURLFile);
                if (Files.isReadable(urlFile)) {
                    return Files.readString(urlFile).trim();
                }
            } catch (IOException e) {
                throw new StepIOException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new StepIOException(e);
            }
        }

        throw new StepProcessException("Timeout waiting for node to get ready");
    }

    @Override
    public void cleanUp(TestContext testContext, Cockroach cockroach) throws StepException {
        if (process == null || !process.isAlive()) {
            logger.info("No process running");
            return;
        }

        boolean force = false;

        while (process.isAlive()) {
            try {
                logger.info("Waiting {} sec before stopping process (pid: {})",
                        cockroach.nodeShutdownWaitSeconds(),
                        process.pid());

                TimeUnit.SECONDS.sleep(cockroach.nodeShutdownWaitSeconds());

                if (!force && process.supportsNormalTermination()) {
                    process.destroy();
                } else {
                    process.destroyForcibly();
                }

                boolean exited = process.waitFor(10, TimeUnit.SECONDS);
                if (exited) {
                    logger.info("Process shutdown graceful (exit code: {})", process.exitValue());
                    TimeUnit.SECONDS.sleep(cockroach.nodeShutdownWaitSeconds());
                } else {
                    logger.info("Process shutdown failed - forcing quit next time");
                    force = true;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
