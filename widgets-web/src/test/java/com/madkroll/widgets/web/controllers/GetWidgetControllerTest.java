package com.madkroll.widgets.web.controllers;

import com.madkroll.widgets.repository.entity.Widget;
import com.madkroll.widgets.service.GetWidgetService;
import com.madkroll.widgets.web.dto.WidgetResponseConverter;
import com.madkroll.widgets.web.dto.WidgetResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class GetWidgetControllerTest {

    private static final String WIDGET_ID = "widget-id";
    private static final Widget SAMPLE_WIDGET = new Widget(WIDGET_ID, 2, 3, 4, 5, 6);
    private static final WidgetResponse SAMPLE_RESPONSE = new WidgetResponse(
            WIDGET_ID,
            SAMPLE_WIDGET.getX(),
            SAMPLE_WIDGET.getY(),
            SAMPLE_WIDGET.getZ(),
            SAMPLE_WIDGET.getWidth(),
            SAMPLE_WIDGET.getHeight(),
            SAMPLE_WIDGET.getLastUpdate()
    );

    @Mock
    private WidgetResponseConverter responseConverter;

    @Mock
    private GetWidgetService service;

    @Test
    public void shouldReturnResponseWithWidget() {
        given(service.find(WIDGET_ID)).willReturn(SAMPLE_WIDGET);
        given(responseConverter.convert(SAMPLE_WIDGET)).willReturn(SAMPLE_RESPONSE);

        final ResponseEntity<WidgetResponse> responseEntity =
                new GetWidgetController(responseConverter, service).handleGetWidget(WIDGET_ID);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(SAMPLE_RESPONSE);
    }
}