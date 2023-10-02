package io.cockroachdb.test.base;

import io.cockroachdb.test.Cockroach;
import io.cockroachdb.test.TestContext;

public interface Step {
    void setUp(TestContext testContext, Cockroach cockroach) throws StepException;

    void cleanUp(TestContext testContext, Cockroach cockroach) throws StepException;
}
