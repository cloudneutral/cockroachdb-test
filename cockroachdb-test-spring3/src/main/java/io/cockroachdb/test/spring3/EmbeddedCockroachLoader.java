package io.cockroachdb.test.spring3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootContextLoader;

public class EmbeddedCockroachLoader extends SpringBootContextLoader {
    @Override
    protected SpringApplication getSpringApplication() {
        SpringApplication app = super.getSpringApplication();
        app.addListeners(
                new ApplicationEnvironmentPreparedListener()
        );
        return app;
    }
}
