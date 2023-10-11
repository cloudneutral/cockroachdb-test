package io.cockroachdb.test.junit4;

import io.cockroachdb.test.Cockroach;
import io.cockroachdb.test.base.Step;
import io.cockroachdb.test.download.DownloadStep;
import io.cockroachdb.test.init.InitStep;
import io.cockroachdb.test.process.ProcessStep;
import io.cockroachdb.test.unpack.UnpackStep;
import io.cockroachdb.test.util.OperatingSystem;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CockroachExtension extends ExternalResource {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Class<?> clazz;

        public <T> Builder withTestClass(Class<T> clazz) {
            this.clazz = clazz;
            return this;
        }

        public CockroachExtension build() {
            Cockroach cockroach = clazz.getAnnotation(Cockroach.class);
            if (cockroach == null) {
                throw new IllegalStateException(
                        "Expected @Cockroach type-level annotation for " + clazz.getName());
            }
            return new CockroachExtension(cockroach);
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Cockroach cockroach;

    private final List<Step> steps = new ArrayList<>(
            List.of(new DownloadStep(), new UnpackStep(), new ProcessStep(), new InitStep()));

    private final ExtensionStoreContext context = ExtensionStoreContext.of();

    public CockroachExtension(Cockroach cockroach) {
        this.cockroach = cockroach;
    }

    public ExtensionStoreContext getContext() {
        return context;
    }

    @Override
    protected void before() {
        logger.info("Bootstrap CockroachDB JUnit4 extension");

        StringWriter sw = new StringWriter();
        OperatingSystem.print(new PrintWriter(sw));
        logger.debug("Host O/S metadata:\n" + sw);

        for (Step step : steps) {
            logger.debug("Running step setup: {}", step.getClass().getName());
            step.setUp(context, cockroach);
        }
    }

    @Override
    protected void after() {
        logger.info("Teardown CockroachDB JUnit4 extension");

        Collections.reverse(steps);

        for (Step step : steps) {
            logger.debug("Running step cleanup: {}", step.getClass().getName());
            step.cleanUp(context, cockroach);
        }
    }
}
