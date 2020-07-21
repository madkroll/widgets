package com.madkroll.widgets.web.controllers;

import com.madkroll.widgets.repository.entity.Widget;
import com.madkroll.widgets.service.UpdateWidgetService;
import com.madkroll.widgets.web.dto.WidgetRequestData;
import com.madkroll.widgets.web.dto.WidgetResponse;
import com.madkroll.widgets.web.dto.WidgetResponseConverter;
import com.madkroll.widgets.web.ratelimit.RateLimiter;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/widgets/update")
@Validated
@AllArgsConstructor
public class UpdateWidgetController {

    private final RateLimiter updateRateLimiter;
    private final UpdateWidgetService updateWidgetService;
    private final WidgetResponseConverter responseConverter;

    @PutMapping(
            path = "{widgetId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WidgetResponse> handleUpdate(
            @PathVariable @NotBlank final String widgetId,
            @RequestBody @Valid final WidgetRequestData widgetData
    ) {
        updateRateLimiter.assertRateLimitIsNotExceeded();

        return ResponseEntity.ok(
                responseConverter.convert(
                        updateWidgetService.update(
                                new Widget(
                                        widgetId,
                                        widgetData.getX(),
                                        widgetData.getY(),
                                        widgetData.getZ(),
                                        widgetData.getWidth(),
                                        widgetData.getHeight()
                                )
                        )
                )
        );
    }
}
