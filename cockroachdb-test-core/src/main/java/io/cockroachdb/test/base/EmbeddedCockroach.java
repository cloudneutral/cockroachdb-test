package io.cockroachdb.test.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.cockroachdb.test.Cockroach;
import io.cockroachdb.test.ProcessDetails;
import io.cockroachdb.test.TestContext;
import io.cockroachdb.test.download.DownloadStep;
import io.cockroachdb.test.init.InitStep;
import io.cockroachdb.test.process.ProcessStep;
import io.cockroachdb.test.unpack.UnpackStep;

/**
 * A thread-bound helper to manage an embedded CockroachDB instance.
 */
public class EmbeddedCockroach {
    public static Cockroach findCockroachAnnotation(Class<?> mainClass) {
        Cockroach cockroach = mainClass.getAnnotation(Cockroach.class);
        if (cockroach == null) {
            throw new IllegalStateException(
                    "Expected @Cockroach type-level annotation for " +
                            mainClass.getName());
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
        return testContext.get(TestContext.COCKROACH_DETAILS, ProcessDetails.class);
    }
}
