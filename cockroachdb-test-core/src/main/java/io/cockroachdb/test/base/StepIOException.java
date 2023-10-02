package io.cockroachdb.test.base;

public class StepIOException extends StepException {
    public StepIOException(Throwable cause) {
        super(cause);
    }

    public StepIOException(String message) {
        super(message);
    }

    public StepIOException(String message, Throwable cause) {
        super(message, cause);
    }
}
