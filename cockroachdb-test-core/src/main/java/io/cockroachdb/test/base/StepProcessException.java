package io.cockroachdb.test.base;

public class StepProcessException extends StepException {
    public StepProcessException(String message) {
        super(message);
    }

    public StepProcessException(String message, Throwable cause) {
        super(message, cause);
    }
}
