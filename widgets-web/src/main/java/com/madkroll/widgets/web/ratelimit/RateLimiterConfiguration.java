package com.madkroll.widgets.web.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimiterConfiguration {

    private final static long DEFAULT_BANDWIDTH_IN_MINUTES = 1000;

    @Bean
    public RateLimiter globalRateLimiter(
            @Value("${rate-limit.global.queries-per-minute:" + DEFAULT_BANDWIDTH_IN_MINUTES + "}") final long limitPerMinute
    ) {
        return limiterPerMinute(limitPerMinute);
    }

    @Bean
    public RateLimiter updateRateLimiter(
            @Value("${rate-limit.update.queries-per-minute:" + DEFAULT_BANDWIDTH_IN_MINUTES + "}") final long limitPerMinute
    ) {
        return limiterPerMinute(limitPerMinute);
    }

    @Bean
    public RateLimiter listRateLimiter(
            @Value("${rate-limit.list.queries-per-minute:" + DEFAULT_BANDWIDTH_IN_MINUTES + "}") final long limitPerMinute
    ) {
        return limiterPerMinute(limitPerMinute);
    }

    private RateLimiter limiterPerMinute(final long limitPerMinute) {
        return new RateLimiter(
                limitPerMinute,
                Bucket4j.builder()
                        .addLimit(Bandwidth.classic(limitPerMinute, Refill.greedy(limitPerMinute, Duration.ofMinutes(1))))
                        .build()
        );
    }
}
