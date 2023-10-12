package io.cockroachdb.test;

public class StepIOException extends StepException {
    public StepIOException(Throwable cause) {
        super(cause);
    }

    public StepIOException(String message, Throwable cause) {
        super(message, cause);
    }
}
