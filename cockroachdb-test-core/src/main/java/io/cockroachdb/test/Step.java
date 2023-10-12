package io.cockroachdb.test;

public interface Step {
    void setUp(TestContext testContext, Cockroach cockroach) throws StepException;

    void cleanUp(TestContext testContext, Cockroach cockroach) throws StepException;
}
