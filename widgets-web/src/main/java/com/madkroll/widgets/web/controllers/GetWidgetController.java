package com.madkroll.widgets.web.controllers;

import com.madkroll.widgets.service.GetWidgetService;
import com.madkroll.widgets.web.dto.WidgetResponseConverter;
import com.madkroll.widgets.web.dto.WidgetResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/widgets/get")
@AllArgsConstructor
public class GetWidgetController {

    private final WidgetResponseConverter responseConverter;
    private final GetWidgetService getWidgetService;

    @GetMapping(path = "{widgetId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WidgetResponse> handleGetWidget(@PathVariable final String widgetId) {
        return ResponseEntity.ok(
                responseConverter.convert(
                        getWidgetService.find(widgetId)
                )
        );
    }
}
