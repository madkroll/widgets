package com.madkroll.widgets.web.controllers;

import com.madkroll.widgets.repository.WidgetNotFoundException;
import com.madkroll.widgets.repository.WidgetRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/widgets/delete")
@AllArgsConstructor
public class DeleteWidgetController {

    private final WidgetRepository widgetRepository;

    @DeleteMapping("{widgetId}")
    public void handleDelete(@PathVariable final String widgetId) {
        widgetRepository.delete(widgetId).orElseThrow(() -> new WidgetNotFoundException(widgetId));
    }
}
