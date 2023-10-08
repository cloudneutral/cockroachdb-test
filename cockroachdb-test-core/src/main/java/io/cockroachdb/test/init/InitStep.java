package io.cockroachdb.test.init;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cockroachdb.test.Cockroach;
import io.cockroachdb.test.ProcessDetails;
import io.cockroachdb.test.TestContext;
import io.cockroachdb.test.base.Step;
import io.cockroachdb.test.base.StepException;
import io.cockroachdb.test.base.StepSQLException;

public class InitStep implements Step {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void setUp(TestContext testContext, Cockroach cockroach) throws StepException {
        if (cockroach.initSQL().length == 0) {
            return;
        }

        ProcessDetails processDetails
                = testContext.get(TestContext.COCKROACH_DETAILS, ProcessDetails.class);

        try (Connection db = DriverManager.getConnection(
                processDetails.getJdbcURL(),
                processDetails.getUser(),
                processDetails.getPassword())) {
            db.setAutoCommit(true);

            Arrays.stream(cockroach.initSQL()).forEach(sql -> {
                try (Statement s = db.createStatement()) {
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
