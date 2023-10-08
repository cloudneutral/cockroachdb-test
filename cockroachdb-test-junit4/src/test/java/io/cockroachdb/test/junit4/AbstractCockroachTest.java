package io.cockroachdb.test.junit4;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cockroachdb.test.ProcessDetails;
import io.cockroachdb.test.TestContext;

public abstract class AbstractCockroachTest {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected abstract CockroachExtension getExtension();

    @Test
    public void whenCockroachStarted_thenSayHelloAndWait() throws SQLException {
        ProcessDetails processDetails
                = getExtension().getContext().get(TestContext.COCKROACH_DETAILS, ProcessDetails.class);

        Assert.assertNotNull(processDetails);

        logger.info("Attempting connection to [{}] with credentials {}/{}",
                processDetails.getJdbcURL(),
                processDetails.getUser(),
                processDetails.getPassword());

        try (Connection db = DriverManager.getConnection(
                processDetails.getJdbcURL(),
                processDetails.getUser(),
                processDetails.getPassword());
             Statement s = db.createStatement();
             ResultSet rs = s.executeQuery("SELECT 1+1")) {
            Assert.assertTrue(rs.next());
            Assert.assertEquals(2, rs.getInt(1));
        }

        logger.info("Success!");
    }
}
