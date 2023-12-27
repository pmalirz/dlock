package com.dlock.core.expiration

import com.dlock.core.model.ReadLockRecord
import com.dlock.core.util.time.DateTimeProvider
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

/**
 * Tests of LocalLockExpirationPolicy.
 *
 * @author Przemyslaw Malirz
 */
class LocalKeyLockExpirationPolicyTest extends Specification {

    private static final NOW = LocalDateTime.parse("2019-01-01T12:00:00")

    private LocalLockExpirationPolicy expirationPolicy

    def setup() {
        expirationPolicy = new LocalLockExpirationPolicy()
    }

    @Unroll("Test lock expiration: #createTimeString, #expirationSeconds, #expired")
    def "Test lock expiration"(String createTimeString, Long expirationSeconds, boolean expired) {
        given:
        def createTime = LocalDateTime.parse(createTimeString)
        def givenLock = new ReadLockRecord("1", "1", createTime, expirationSeconds, NOW)

        when:
        def actualExpired = expirationPolicy.expired(givenLock)

        then:
        actualExpired == expired

        where:
        createTimeString      | expirationSeconds | expired
        "2019-01-01T11:59:00" | 61                | false
        "2019-01-01T11:59:00" | 59                | true
        "2019-01-01T11:59:00" | 60                | false
    }

}
