package com.madkroll.widgets.web;

import com.madkroll.widgets.repository.WidgetNotFoundException;
import com.madkroll.widgets.web.ratelimit.RateLimitExceededException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Log4j2
@ResponseBody
@ControllerAdvice
public class WidgetsExceptionHandler {

    /**
     * Handles cases when specified widget does not exist.
     * */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void widgetNotFoundHandler(final WidgetNotFoundException e) {
        log.error(e.getMessage(), e);
    }

    /**
     * Handles cases when specified widget does not exist.
     * */
    @ExceptionHandler
    public ResponseEntity<?> rateLimitExceededHandler(final RateLimitExceededException e) {
        log.error(e.getMessage(), e);

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header("X-RateLimit-Limit", String.valueOf(e.getRateLimit()))
                .header("X-RateLimit-Remaining", "0")
                .header("X-RateLimit-Reset", String.valueOf(e.getResetTimestamp()))
                .build();
    }
}
