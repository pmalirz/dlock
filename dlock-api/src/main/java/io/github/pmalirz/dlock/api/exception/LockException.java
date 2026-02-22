package io.github.pmalirz.dlock.api.exception;

/**
 * Exception thrown when a lock operation fails unexpectedly.
 * This is a runtime exception, as lock failures due to system errors
 * (e.g., database connectivity issues) are typically unrecoverable
 * in the immediate context of a lock acquisition attempt.
 */
public class LockException extends RuntimeException {

    public LockException(String message) {
        super(message);
    }

    public LockException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockException(Throwable cause) {
        super(cause);
    }
}
