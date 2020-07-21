package com.madkroll.widgets.web.ratelimit;

import lombok.Getter;

@Getter
public class RateLimitExceededException extends RuntimeException {

    private final long rateLimit;
    private final long resetTimestamp;

    public RateLimitExceededException(final long rateLimit, final long resetTimestamp) {
        super("Rate limit exceeded.");
        this.rateLimit = rateLimit;
        this.resetTimestamp = resetTimestamp;
    }
}
