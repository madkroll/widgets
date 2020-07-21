package com.madkroll.widgets.web.controllers;

import com.madkroll.widgets.repository.WidgetRepository;
import com.madkroll.widgets.repository.entity.Widget;
import com.madkroll.widgets.web.dto.WidgetResponseConverter;
import com.madkroll.widgets.web.dto.WidgetRequestData;
import com.madkroll.widgets.web.dto.WidgetResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CreateWidgetControllerTest {

    private static final WidgetRequestData SAMPLE_REQUEST = new WidgetRequestData(
            1, 2, 3, 4, 5
    );

    private static final Widget SAMPLE_WIDGET = new Widget(
            "widget-id", 1, 2, 3, 4, 5
    );

    private static final WidgetResponse SAMPLE_RESPONSE = new WidgetResponse(
            SAMPLE_WIDGET.getId(),
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
    private WidgetRepository widgetRepository;

    @Captor
    private ArgumentCaptor<Widget> widgetCaptor;

    @Test
    public void shouldGenerateUniqueIDs() {
        final CreateWidgetController controller =
                new CreateWidgetController(responseConverter, widgetRepository);

        final int times = 10;
        IntStream.range(0, times)
                .forEach(
                        nextCall -> {
                            given(widgetRepository.add(any())).willReturn(SAMPLE_WIDGET);
                            given(responseConverter.convert(SAMPLE_WIDGET)).willReturn(SAMPLE_RESPONSE);
                            controller.handleCreate(SAMPLE_REQUEST);
                        }
                );

        verify(widgetRepository, times(times)).add(widgetCaptor.capture());
        assertThat(
                widgetCaptor.getAllValues()
                        .stream()
                        .distinct()
                        .count()
        ).isEqualTo(times);
    }

    @Test
    public void shouldReturnResponseForWidgetFromRepository() {
        given(widgetRepository.add(any())).willReturn(SAMPLE_WIDGET);
        given(responseConverter.convert(SAMPLE_WIDGET)).willReturn(SAMPLE_RESPONSE);

        final ResponseEntity<WidgetResponse> responseEntity =
                new CreateWidgetController(responseConverter, widgetRepository)
                        .handleCreate(SAMPLE_REQUEST);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(SAMPLE_RESPONSE);
    }
}