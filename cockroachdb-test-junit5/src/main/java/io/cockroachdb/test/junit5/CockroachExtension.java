package io.cockroachdb.test.junit5;

import io.cockroachdb.test.Cockroach;
import io.cockroachdb.test.CockroachDetails;
import io.cockroachdb.test.plugin.StandardSteps;
import io.cockroachdb.test.base.Step;
import io.cockroachdb.test.util.OperatingSystem;
import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CockroachExtension
        implements BeforeAllCallback, AfterAllCallback,
        TestInstancePostProcessor, TestExecutionExceptionHandler {

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

    public CockroachExtension(Cockroach cockroach) {
        this.cockroach = cockroach;
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        CockroachDetails cockroachDetails =
                context.getStore(ExtensionContext.Namespace.GLOBAL).get("cockroachDetails", CockroachDetails.class);
        try {
            testInstance.getClass()
                    .getMethod("setCockroachDetails", CockroachDetails.class)
                    .invoke(testInstance, cockroachDetails);
        } catch (NoSuchMethodException e) {
            // ok nvm
        } catch (IllegalAccessException | InvocationTargetException | SecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        logger.info("Bootstrap CockroachDB JUnit5 extension:\n{}",
                extensionContext.getDisplayName());

        StringWriter sw = new StringWriter();
        OperatingSystem.print(new PrintWriter(sw));
        logger.debug("Host O/S metadata:\n" + sw);

        for (Step step : steps) {
            logger.debug("Running setup step: {}", step);
            step.setUp(ExtensionStoreContext.of(extensionContext), cockroach);
        }
    }

    @Override
    public void handleTestExecutionException(ExtensionContext extensionContext, Throwable throwable) throws Throwable {
        logger.error("Test execution error", throwable);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        if (extensionContext.getExecutionException().isEmpty()) {
            Collections.reverse(steps);

            for (Step step : steps) {
                logger.debug("Running cleanup step: {}", step);
                step.cleanUp(ExtensionStoreContext.of(extensionContext), cockroach);
            }
        }
    }
}
