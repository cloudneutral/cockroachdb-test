package io.cockroachdb.test.base;

public abstract class StepException extends RuntimeException {
    public StepException(String message) {
        super(message);
    }

    public StepException(String message, Throwable cause) {
        super(message, cause);
    }

    public StepException(Throwable cause) {
        super(cause);
    }
}
