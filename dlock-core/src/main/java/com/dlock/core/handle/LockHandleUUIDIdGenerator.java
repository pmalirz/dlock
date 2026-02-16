package com.dlock.core.handle;

import java.util.UUID;

/**
 * UUID based handle ID generator.
 *
 * @author Przemyslaw Malirz
 */
public class LockHandleUUIDIdGenerator implements LockHandleIdGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }

}
