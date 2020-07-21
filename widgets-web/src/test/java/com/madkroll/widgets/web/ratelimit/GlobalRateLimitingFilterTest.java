package com.madkroll.widgets.web.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GlobalRateLimitingFilterTest {

    private final static long ONE_PER_TIME = 1L;
    private final static long LIMIT = 10L;
    private final static long STILL_REMAINING = 5L;
    private final static long NO_REMAINING_LEFT = 0L;
    private final static long FIVE_MINUTES_IN_NANOS = 5 * 60 * 1000 * 1000L;

    @Mock
    private FilterChain filterChain;

    @Mock
    private ServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ConsumptionProbe probe;

    @Mock
    private Bucket bucket;

    @Mock
    private RateLimiter rateLimiter;

    @Before
    public void setUp() {
        given(probe.getNanosToWaitForRefill()).willReturn(FIVE_MINUTES_IN_NANOS);
        given(bucket.tryConsumeAndReturnRemaining(ONE_PER_TIME)).willReturn(probe);
        given(rateLimiter.getAvailableSlots()).willReturn(bucket);
        given(rateLimiter.getLimit()).willReturn(LIMIT);
    }

    @Test
    public void shouldAllowRequestHandling() throws IOException, ServletException {
        // given
        given(probe.isConsumed()).willReturn(true);
        given(probe.getRemainingTokens()).willReturn(STILL_REMAINING);

        // when
        new GlobalRateLimitingFilter(rateLimiter).doFilter(request, response, filterChain);

        // then
        verify(response).setHeader("X-Rate-Limit-Remaining", String.valueOf(STILL_REMAINING));
        verify(filterChain).doFilter(request, response);

        verify(response, never()).setHeader(eq("X-RateLimit-Limit"), anyString());
        verify(response, never()).setHeader(eq("X-RateLimit-Reset"), anyString());
    }

    @Test
    public void shouldSendTooManyRequestsIfRateLimitIsExceeded() throws IOException, ServletException {
        // given
        given(probe.isConsumed()).willReturn(false);

        // when
        new GlobalRateLimitingFilter(rateLimiter).doFilter(request, response, filterChain);

        // then
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(response).setHeader("X-Rate-Limit-Remaining", String.valueOf(NO_REMAINING_LEFT));
        verify(response).setHeader("X-RateLimit-Limit", String.valueOf(LIMIT));
        final ArgumentCaptor<String> nextResetNanos = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(eq("X-RateLimit-Reset"), nextResetNanos.capture());

        final long nowInSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        assertThat(Long.parseLong(nextResetNanos.getValue()))
                .isBetween(nowInSeconds, nowInSeconds + TimeUnit.NANOSECONDS.toSeconds(FIVE_MINUTES_IN_NANOS));

        verifyNoInteractions(filterChain);
    }

}