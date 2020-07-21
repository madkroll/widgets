package com.madkroll.widgets.service;

import com.madkroll.widgets.repository.WidgetNotFoundException;
import com.madkroll.widgets.repository.WidgetRepository;
import com.madkroll.widgets.repository.entity.Widget;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class UpdateWidgetServiceTest {

    private static final Widget SAMPLE_WIDGET = new Widget("widget-id", 1, 2, 3, 4, 5);

    @Mock
    private WidgetRepository widgetRepository;

    @Test
    public void shouldReturnWidgetWhenFound() {
        // given
        given(widgetRepository.update(SAMPLE_WIDGET)).willReturn(Optional.of(SAMPLE_WIDGET));

        // when
        final Widget resultWidget = new UpdateWidgetService(widgetRepository).update(SAMPLE_WIDGET);

        // then
        assertThat(resultWidget).isEqualTo(SAMPLE_WIDGET);
    }

    @Test
    public void shouldThrowErrorIfWidgetNotFound() {
        // given
        given(widgetRepository.update(SAMPLE_WIDGET)).willReturn(Optional.empty());

        // when
        final ThrowableAssert.ThrowingCallable updateCall =
                () -> new UpdateWidgetService(widgetRepository).update(SAMPLE_WIDGET);

        // then
        assertThatThrownBy(updateCall)
                .isExactlyInstanceOf(WidgetNotFoundException.class)
                .hasMessage("Widget not found: " + SAMPLE_WIDGET.getId());
    }
}