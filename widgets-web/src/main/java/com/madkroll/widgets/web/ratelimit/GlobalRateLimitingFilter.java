package com.madkroll.widgets.web.ratelimit;

import io.github.bucket4j.ConsumptionProbe;
import lombok.AllArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@AllArgsConstructor
@Order
public class GlobalRateLimitingFilter implements Filter {

    private static final long ONE_ITEM_IN_TIME = 1L;
    private final RateLimiter globalRateLimiter;

    @Override
    public void doFilter(
            final ServletRequest servletRequest,
            final ServletResponse servletResponse,
            final FilterChain filterChain
    ) throws IOException, ServletException {

        /*
         * Note:
         * On real production environment we should exclude health check HTTP requests
         * */

        final ConsumptionProbe probe = globalRateLimiter.getAvailableSlots().tryConsumeAndReturnRemaining(ONE_ITEM_IN_TIME);

        final HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        if (probe.isConsumed()) {
            httpResponse.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setHeader("X-RateLimit-Limit", String.valueOf(globalRateLimiter.getLimit()));
            httpResponse.setHeader("X-Rate-Limit-Remaining", "0");
            httpResponse.setHeader("X-RateLimit-Reset", String.valueOf(
                    TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) +
                            TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill())
            ));
        }
    }
}
