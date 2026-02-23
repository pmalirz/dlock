package io.github.pmalirz.dlock.core.handle;

/**
 * Generates globally unique handle identifier.
 *
 * @author Przemyslaw Malirz
 */
public interface LockHandleIdGenerator {

    String generate();

}
