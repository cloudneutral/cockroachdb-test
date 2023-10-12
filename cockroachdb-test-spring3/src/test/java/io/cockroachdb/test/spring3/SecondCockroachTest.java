package io.cockroachdb.test.spring3;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.event.annotation.BeforeTestClass;

public class SecondCockroachTest extends AbstractEmbeddedCockroachTest {
    @BeforeTestClass
    public void beforeSecondTestClass() {
        logger.info("Before second test");
        new JdbcTemplate(dataSource).update("delete from payment where 1=1");
    }
}
