package io.cockroachdb.test.init;

import io.cockroachdb.test.Cockroach;
import io.cockroachdb.test.CockroachDetails;
import io.cockroachdb.test.TestContext;
import io.cockroachdb.test.base.Step;
import io.cockroachdb.test.base.StepException;
import io.cockroachdb.test.base.StepIOException;
import io.cockroachdb.test.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public class InitStep implements Step {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void setUp(TestContext testContext, Cockroach cockroach) throws StepException {
        if (!StringUtils.hasLength(cockroach.initSQL())) {
            return;
        }

        CockroachDetails cockroachDetails
                = testContext.get(TestContext.COCKROACH_DETAILS, CockroachDetails.class);

        try (Connection db = DriverManager.getConnection(
                cockroachDetails.getJdbcURL(),
                cockroachDetails.getUser(),
                cockroachDetails.getPassword())) {

            String[] parts = cockroach.initSQL().split(";");

            Arrays.stream(parts).forEach(sql -> {
                try {
                    logger.debug("Executing [{}]", sql);
                    executeUpdate(db, sql);
                } catch (SQLException e) {
                    throw new StepIOException(e);
                }
            });
        } catch (SQLException e) {
            throw new StepIOException(e);
        }
    }

    private int executeUpdate(Connection connection, String statement) throws SQLException {
        try (Statement s = connection.createStatement()) {
            return s.executeUpdate(statement);
        }
    }

    @Override
    public void cleanUp(TestContext testContext, Cockroach cockroach) throws StepException {

    }
}
