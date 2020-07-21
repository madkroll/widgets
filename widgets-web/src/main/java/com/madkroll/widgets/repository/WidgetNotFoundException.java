package com.madkroll.widgets.repository;

public class WidgetNotFoundException extends RuntimeException {

    public WidgetNotFoundException(final String id) {
        super("Widget not found: " + id);
    }
}
