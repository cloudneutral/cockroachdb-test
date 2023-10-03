package io.cockroachdb.test.junit5;

import io.cockroachdb.test.CockroachDetails;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.concurrent.TimeUnit;

//@ExtendWith(CockroachExtension.class)
public abstract class AbstractCockroachTest {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private CockroachDetails cockroachDetails;

    public void setCockroachDetails(CockroachDetails cockroachDetails) {
        this.cockroachDetails = cockroachDetails;
    }

    @Test
    public void whenCockroachStarted_thenSayHelloAndWait() throws SQLException {
        Assertions.assertNotNull(cockroachDetails);

        logger.info("Attempting connection to [{}] with credentials {}/{}",
                cockroachDetails.getJdbcURL(),
                cockroachDetails.getUser(),
                cockroachDetails.getPassword());

        try (Connection db = DriverManager.getConnection(
                cockroachDetails.getJdbcURL(),
                cockroachDetails.getUser(),
                cockroachDetails.getPassword())) {

            try (Statement s = db.createStatement();
                 ResultSet rs = s.executeQuery("SELECT 1+1")) {
                Assertions.assertTrue(rs.next());
                Assertions.assertEquals(2, rs.getInt(1));
            }
            try (Statement s = db.createStatement();
                 ResultSet rs = s.executeQuery("SELECT version()")) {
                Assertions.assertTrue(rs.next());
                logger.info("Connected to {}", rs.getString(1));
            }
        }

        logger.info("Success! Waiting 15 sec until quitting");
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(15));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
