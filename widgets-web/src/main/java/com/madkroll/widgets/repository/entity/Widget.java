package com.madkroll.widgets.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
/**
 * Widget entity stored in repository
 * */
public class Widget {

    private final String id;
    private final int x;
    private final int y;
    private final int z;
    private final int width;
    private final int height;
    private final long lastUpdate = System.currentTimeMillis();
}
