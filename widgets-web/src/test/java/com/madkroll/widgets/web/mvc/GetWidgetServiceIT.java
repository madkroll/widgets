package com.madkroll.widgets.web.mvc;

import com.madkroll.widgets.repository.WidgetRepository;
import com.madkroll.widgets.repository.entity.Widget;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class GetWidgetServiceIT {

    private static final String WIDGET_ID = "widget-id";
    private static final Widget SAMPLE_WIDGET = new Widget(
            WIDGET_ID, 1, 2, 3, 4, 5
    );

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WidgetRepository widgetRepository;

    @Test
    public void shouldReturnNotFoundIfNoSuchWidget() throws Exception {
        given(widgetRepository.find(WIDGET_ID)).willReturn(Optional.empty());

        final MockHttpServletRequestBuilder widgetNotFound =
                MockMvcRequestBuilders.get("/widgets/get/" + WIDGET_ID);

        mockMvc.perform(widgetNotFound)
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnWidget() throws Exception {
        given(widgetRepository.find(WIDGET_ID)).willReturn(Optional.of(SAMPLE_WIDGET));

        final MockHttpServletRequestBuilder foundWidget =
                MockMvcRequestBuilders.get("/widgets/get/" + WIDGET_ID);

        mockMvc.perform(foundWidget)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(WIDGET_ID)))
                .andExpect(jsonPath("$.x", is(SAMPLE_WIDGET.getX())))
                .andExpect(jsonPath("$.y", is(SAMPLE_WIDGET.getY())))
                .andExpect(jsonPath("$.z", is(SAMPLE_WIDGET.getZ())))
                .andExpect(jsonPath("$.width", is(SAMPLE_WIDGET.getWidth())))
                .andExpect(jsonPath("$.height", is(SAMPLE_WIDGET.getHeight())))
                .andExpect(jsonPath("$.lastUpdate", is(SAMPLE_WIDGET.getLastUpdate())));
    }
}
