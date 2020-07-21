package com.madkroll.widgets.service;

import com.madkroll.widgets.repository.WidgetNotFoundException;
import com.madkroll.widgets.repository.WidgetRepository;
import com.madkroll.widgets.repository.entity.Widget;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GetWidgetService {

    private final WidgetRepository widgetRepository;

    public Widget find(final String widgetId) {
        return widgetRepository.find(widgetId)
                .orElseThrow(() -> new WidgetNotFoundException(widgetId));
    }
}
