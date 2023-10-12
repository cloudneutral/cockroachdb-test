package io.cockroachdb.test.junit4;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cockroachdb.test.Cockroach;
import io.cockroachdb.test.EmbeddedCockroach;
import io.cockroachdb.test.util.OperatingSystem;

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
            return new CockroachExtension(clazz);
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Class<?> testClass;

    public CockroachExtension(Class<?> testClass) {
        Cockroach cockroach = testClass.getAnnotation(Cockroach.class);
        if (cockroach == null) {
            throw new IllegalStateException(
                    "Expected @Cockroach type-level annotation for " + testClass.getName());
        }
        this.testClass = testClass;
    }

    @Override
    protected void before() {
        logger.info("Bootstrap CockroachDB JUnit4 extension");

        StringWriter sw = new StringWriter();
        OperatingSystem.print(new PrintWriter(sw));
        logger.debug("Host O/S metadata:\n" + sw);

        EmbeddedCockroach.getInstance().start(testClass);
    }

    @Override
    protected void after() {
        logger.info("Teardown CockroachDB JUnit4 extension");

        EmbeddedCockroach.getInstance().stop(testClass);
    }
}
