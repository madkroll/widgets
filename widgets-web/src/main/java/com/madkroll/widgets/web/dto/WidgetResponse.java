package com.madkroll.widgets.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WidgetResponse {

    private final String id;
    private final int x;
    private final int y;
    private final int z;
    private final int width;
    private final int height;
    private final long lastUpdate;
}
