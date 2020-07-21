package com.madkroll.widgets.web.controllers;

import com.madkroll.widgets.service.ListWidgetsService;
import com.madkroll.widgets.web.dto.WidgetResponse;
import com.madkroll.widgets.web.ratelimit.RateLimiter;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/widgets/list")
@AllArgsConstructor
public class ListWidgetsController {

    private final RateLimiter listRateLimiter;
    private final ListWidgetsService listWidgetsService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WidgetResponse>> handleList(
            @RequestParam(name = "from", required = false) final Integer from,
            @RequestParam(name = "limit", required = false, defaultValue = "10") final Integer limit
    ) {
        listRateLimiter.assertRateLimitIsNotExceeded();

        return ResponseEntity.ok(
                listWidgetsService.list(from, limit)
        );
    }
}
