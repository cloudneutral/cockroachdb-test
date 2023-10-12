package io.cockroachdb.test.init;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import io.cockroachdb.test.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitStep implements Step {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void setUp(TestContext testContext, Cockroach cockroach) throws StepException {
        if (cockroach.initSQL().length == 0) {
            return;
        }

        ProcessDetails processDetails
                = testContext.get(Constants.PROCESS_DETAILS, ProcessDetails.class);

        try (Connection connection = DriverManager.getConnection(
                processDetails.getJdbcURL(),
                processDetails.getUser(),
                processDetails.getPassword())) {
            connection.setAutoCommit(true);

            Arrays.stream(cockroach.initSQL()).forEach(sql -> {
                try (Statement s = connection.createStatement()) {
                    logger.debug("Executing [{}]", sql);
                    s.executeUpdate(sql);
                } catch (SQLException e) {
                    logger.error("Init statement error [{}] state [{}]: {}",
                            sql, e.getSQLState(), e.toString());
                    throw new StepSQLException(e);
                }
            });
        } catch (SQLException e) {
            throw new StepSQLException(e);
        }
    }

    @Override
    public void cleanUp(TestContext testContext, Cockroach cockroach) throws StepException {
    }
}
