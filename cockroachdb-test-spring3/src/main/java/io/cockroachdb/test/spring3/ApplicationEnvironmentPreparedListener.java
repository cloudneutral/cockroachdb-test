package io.cockroachdb.test.spring3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.PropertySource;
import org.springframework.lang.NonNull;

import io.cockroachdb.test.EmbeddedCockroach;
import jakarta.annotation.Nullable;

/**
 * An {@link ApplicationListener} that manages the lifecycle of an embedded CockroachDB
 * instance, similar to the JUnit4 and JUnit5 extensions. This application
 * listener will be triggered exactly once per JVM and it registers a spring
 * shutdown hook to terminate the database and run cleanup.
 */
public class ApplicationEnvironmentPreparedListener implements
        ApplicationListener<ApplicationEnvironmentPreparedEvent>,
        Ordered {
    private static final String DATASOURCE_URL_PROPERTY = "spring.datasource.url";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        Class<?> testClass = event.getSpringApplication().getMainApplicationClass();

        logger.info("ApplicationEnvironmentPreparedEvent: Starting embedded CockroachDB for {}", testClass.getName());

        EmbeddedCockroach.getInstance().start(testClass);

        final String jdbcUrl = EmbeddedCockroach.getInstance()
                .getProcessDetails()
                .getJdbcURL();

        event.getEnvironment().getPropertySources().addFirst(
                new PropertySource<>("cockroachdb-test") {
                    @Nullable
                    @Override
                    public Object getProperty(@NonNull String propertyName) {
                        if (propertyName.equalsIgnoreCase(DATASOURCE_URL_PROPERTY)) {
                            logger.info("Replacing '" + propertyName + "' with: " + jdbcUrl);
                            return jdbcUrl;
                        }
                        return null;
                    }
                });

        SpringApplication.getShutdownHandlers().add(() -> {
            logger.info("Stopping embedded CockroachDB for {}", testClass.getName());
            EmbeddedCockroach.getInstance().stop(testClass);
        });
    }
}
