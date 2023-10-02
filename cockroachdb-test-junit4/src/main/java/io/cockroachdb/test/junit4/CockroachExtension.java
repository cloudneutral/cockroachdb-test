package io.cockroachdb.test.junit4;

import io.cockroachdb.test.Cockroach;
import io.cockroachdb.test.plugin.StandardSteps;
import io.cockroachdb.test.base.Step;
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
                        "Expected @CockroachSetup type-level annotation for " + clazz.getName());
            }
            return new CockroachExtension(cockroach);
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Cockroach cockroach;

    private final List<Step> steps = new ArrayList<>(StandardSteps.LIST);

    private final ExtensionStoreContext context = ExtensionStoreContext.createInstance();

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
            logger.debug("Running setup step: {}", step);
            step.setUp(context, cockroach);
        }
    }

    @Override
    protected void after() {
        Collections.reverse(steps);

        for (Step step : steps) {
            logger.debug("Running cleanup step: {}", step);
            step.cleanUp(context, cockroach);
        }
    }
}
