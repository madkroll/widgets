package com.madkroll.widgets.service;

import com.madkroll.widgets.web.dto.WidgetResponseConverter;
import com.madkroll.widgets.web.dto.WidgetResponse;
import com.madkroll.widgets.repository.entity.Widget;
import com.madkroll.widgets.repository.WidgetRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class ListWidgetsService {

    private final WidgetRepository widgetRepository;
    private final WidgetResponseConverter responseConverter;

    public List<WidgetResponse> list(final Integer from, final Integer limit) {
        Stream<Widget> widgets = widgetRepository.listAllOrderedByZIndex();

        if (Objects.nonNull(from)) {
            widgets = widgets.skip(from);
        }

        return widgets.limit(limit)
                .map(responseConverter::convert)
                .collect(Collectors.toList());
    }
}
