package io.cockroachdb.test.junit4;

import io.cockroachdb.test.Cockroach;
import io.cockroachdb.test.CockroachDetails;
import io.cockroachdb.test.DemoFlags;
import io.cockroachdb.test.TestContext;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.concurrent.TimeUnit;

@Cockroach(
        version = "v23.1.10",
        architecture = Cockroach.Architecture.amd64,
        command = Cockroach.Command.demo,
        demoFlags = @DemoFlags(global = true, nodes = 9)
)
public class CockroachJunit4Test {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @ClassRule
    public static CockroachExtension extension =
            CockroachExtension.builder()
                    .withTestClass(CockroachJunit4Test.class)
                    .build();

    @Test
    public void whenCockroachStarted_thenSayHelloAndWait() throws SQLException {
        CockroachDetails cockroachDetails
                = extension.getContext().get(TestContext.COCKROACH_DETAILS, CockroachDetails.class);

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
