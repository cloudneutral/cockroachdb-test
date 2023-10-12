package io.cockroachdb.test.spring3;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.event.annotation.BeforeTestClass;

public class FirstCockroachTest extends AbstractEmbeddedCockroachTest {
    @BeforeTestClass
    public void beforeFirstTestClass() {
        logger.info("Before first test");
        new JdbcTemplate(dataSource).update("delete from payment where 1=1");
    }
}
