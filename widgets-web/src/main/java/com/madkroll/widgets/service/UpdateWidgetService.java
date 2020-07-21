package com.madkroll.widgets.service;

import com.madkroll.widgets.repository.WidgetNotFoundException;
import com.madkroll.widgets.repository.WidgetRepository;
import com.madkroll.widgets.repository.entity.Widget;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateWidgetService {

    private final WidgetRepository widgetRepository;

    public Widget update(final Widget widget) {
        return widgetRepository.update(widget)
                .orElseThrow(() -> new WidgetNotFoundException(widget.getId()));
    }
}
