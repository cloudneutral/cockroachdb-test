package io.cockroachdb.test;

import io.cockroachdb.test.download.DownloadStep;
import io.cockroachdb.test.init.InitStep;
import io.cockroachdb.test.process.ProcessStep;
import io.cockroachdb.test.unpack.UnpackStep;

import java.util.*;

/**
 * A thread-bound helper to manage an embedded CockroachDB instance.
 */
public class EmbeddedCockroach {
    public static Cockroach findCockroachAnnotation(Class<?> testClass) {
        Cockroach cockroach = testClass.getAnnotation(Cockroach.class);
        if (cockroach == null) {
            throw new IllegalStateException(
                    "Expected @Cockroach type-level annotation for " +
                            testClass.getName());
        }
        return cockroach;
    }

    public static final ThreadLocal<EmbeddedCockroach> threadLocal = ThreadLocal.withInitial(EmbeddedCockroach::new);

    public static EmbeddedCockroach getInstance() {
        return threadLocal.get();
    }

    private final List<Step> steps = new ArrayList<>(
            List.of(new DownloadStep(), new UnpackStep(), new ProcessStep(), new InitStep()));

    private final Map<String, Object> extensionContext = new HashMap<>();

    private final TestContext testContext = new TestContext() {
        @Override
        public void put(String key, Object value) {
            extensionContext.put(key, value);
        }

        @Override
        public <T> T get(String key, Class<T> expectedType) {
            return expectedType.cast(extensionContext.get(key));
        }
    };

    public void start(Class<?> mainClass) {
        Cockroach cockroach = findCockroachAnnotation(mainClass);

        for (Step step : steps) {
            step.setUp(testContext, cockroach);
        }
    }

    public void stop(Class<?> mainClass) {
        Cockroach cockroach = findCockroachAnnotation(mainClass);

        Collections.reverse(steps);

        for (Step step : steps) {
            step.cleanUp(testContext, cockroach);
        }
    }

    public ProcessDetails getProcessDetails() {
        return testContext.get(Constants.PROCESS_DETAILS, ProcessDetails.class);
    }
}
