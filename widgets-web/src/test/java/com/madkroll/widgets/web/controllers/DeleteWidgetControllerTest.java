package com.madkroll.widgets.web.controllers;

import com.madkroll.widgets.service.DeleteWidgetService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DeleteWidgetControllerTest {

    private final static String WIDGET_ID = "widget-id";

    @Mock
    private DeleteWidgetService deleteWidgetService;

    @Test
    public void shouldDeleteWidget() {
        // given
        // when
        new DeleteWidgetController(deleteWidgetService).handleDelete(WIDGET_ID);

        // then
        verify(deleteWidgetService).delete(WIDGET_ID);
    }
}