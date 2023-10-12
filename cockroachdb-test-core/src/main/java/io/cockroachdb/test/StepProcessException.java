package io.cockroachdb.test;

public class StepProcessException extends StepException {
    public StepProcessException(String message) {
        super(message);
    }

    public StepProcessException(String message, Throwable cause) {
        super(message, cause);
    }
}
