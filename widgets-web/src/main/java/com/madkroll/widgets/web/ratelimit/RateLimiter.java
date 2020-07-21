package com.madkroll.widgets.web.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Getter
@AllArgsConstructor
public class RateLimiter {

    public final static long ONE_IN_TIME = 1L;

    private final long limit;
    private final Bucket availableSlots;

    public void assertRateLimitIsNotExceeded() {
        final ConsumptionProbe attemptStatus =
                availableSlots.tryConsumeAndReturnRemaining(RateLimiter.ONE_IN_TIME);

        if (!attemptStatus.isConsumed()) {
            throw new RateLimitExceededException(
                    limit,
                    TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) +
                            TimeUnit.NANOSECONDS.toSeconds(attemptStatus.getNanosToWaitForRefill())
            );
        }
    }
}
