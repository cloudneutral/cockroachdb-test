package io.cockroachdb.test.junit4;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cockroachdb.test.CockroachDetails;
import io.cockroachdb.test.TestContext;

public abstract class AbstractCockroachTest {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected abstract CockroachExtension getExtension();

    @Test
    public void whenCockroachStarted_thenSayHelloAndWait() throws SQLException {
        CockroachDetails cockroachDetails
                = getExtension().getContext().get(TestContext.COCKROACH_DETAILS, CockroachDetails.class);

        Assert.assertNotNull(cockroachDetails);

        logger.info("Attempting connection to [{}] with credentials {}/{}",
                cockroachDetails.getJdbcURL(),
                cockroachDetails.getUser(),
                cockroachDetails.getPassword());

        try (Connection db = DriverManager.getConnection(
                cockroachDetails.getJdbcURL(),
                cockroachDetails.getUser(),
                cockroachDetails.getPassword());
             Statement s = db.createStatement();
             ResultSet rs = s.executeQuery("SELECT 1+1")) {
            Assert.assertTrue(rs.next());
            Assert.assertEquals(2, rs.getInt(1));
        }

        logger.info("Success! Waiting 15 sec until quitting");
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(15));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
