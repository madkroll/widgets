package com.madkroll.widgets.service;

import com.madkroll.widgets.repository.WidgetNotFoundException;
import com.madkroll.widgets.repository.WidgetRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DeleteWidgetService {

    private final WidgetRepository widgetRepository;

    public void delete(final String widgetId) {
        widgetRepository.delete(widgetId)
                .orElseThrow(() -> new WidgetNotFoundException(widgetId));
    }
}
