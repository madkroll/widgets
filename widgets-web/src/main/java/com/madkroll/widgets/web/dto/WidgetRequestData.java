package com.madkroll.widgets.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
public class WidgetRequestData {

    @NotNull
    private final Integer x;
    @NotNull
    private final Integer y;
    @NotNull
    private final Integer z;
    @NotNull
    private final Integer width;
    @NotNull
    private final Integer height;
}
