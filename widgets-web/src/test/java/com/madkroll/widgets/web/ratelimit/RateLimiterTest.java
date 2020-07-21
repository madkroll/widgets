package com.madkroll.widgets.web.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RateLimiterTest {

    private final static long ONE_IN_TIME = 1L;
    private final static long LIMIT = 10L;
    private final static long FIVE_MINUTES_IN_NANOS = 5 * 60 * 1000 * 1000;

    @Mock
    private ConsumptionProbe probe;

    @Mock
    private Bucket bucket;

    @Before
    public void setUp() {
        given(probe.getNanosToWaitForRefill()).willReturn(FIVE_MINUTES_IN_NANOS);
        given(bucket.tryConsumeAndReturnRemaining(ONE_IN_TIME)).willReturn(probe);
    }

    @Test
    public void shouldSucceedIfRateLimitIsNotExceeded() {
        given(probe.isConsumed()).willReturn(true);

        new RateLimiter(LIMIT, bucket)
                .assertRateLimitIsNotExceeded();

        verify(bucket).tryConsumeAndReturnRemaining(ONE_IN_TIME);
    }

    @Test
    public void shouldFailWhenRateLimitExceeded() {
        given(probe.isConsumed()).willReturn(false);

        assertThatThrownBy(
                () -> new RateLimiter(LIMIT, bucket)
                        .assertRateLimitIsNotExceeded()
        )
                .isExactlyInstanceOf(RateLimitExceededException.class)
                .hasMessage("Rate limit exceeded.");

        verify(bucket).tryConsumeAndReturnRemaining(ONE_IN_TIME);
    }
}