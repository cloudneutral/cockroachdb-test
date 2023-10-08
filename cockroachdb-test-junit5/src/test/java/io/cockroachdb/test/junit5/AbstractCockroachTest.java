package io.cockroachdb.test.junit5;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cockroachdb.test.ProcessDetails;

//@ExtendWith(CockroachExtension.class)
public abstract class AbstractCockroachTest {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private ProcessDetails processDetails;

    public void setProcessDetails(ProcessDetails processDetails) {
        this.processDetails = processDetails;
    }

    @Test
    public void whenCockroachStarted_thenSayHelloAndWait() throws SQLException {
        Assertions.assertNotNull(processDetails);

        logger.info("Attempting connection to [{}] with credentials {}/{}",
                processDetails.getJdbcURL(),
                processDetails.getUser(),
                processDetails.getPassword());

        try (Connection db = DriverManager.getConnection(
                processDetails.getJdbcURL(),
                processDetails.getUser(),
                processDetails.getPassword())) {

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

        logger.info("Success!");
    }
}
