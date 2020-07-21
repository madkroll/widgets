package com.madkroll.widgets.web.mvc;

import com.madkroll.widgets.repository.WidgetRepository;
import com.madkroll.widgets.repository.entity.Widget;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CreateWidgetServiceIT {

    private static final String WIDGET_ID = "widget-id";
    private static final Widget SAMPLE_WIDGET = new Widget(
            WIDGET_ID, 1, 2, 3, 4, 5
    );

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WidgetRepository widgetRepository;

    @Test
    public void shouldCreateWidget() throws Exception {
        given(widgetRepository.add(any())).willReturn(SAMPLE_WIDGET);

        final MockHttpServletRequestBuilder foundWidget =
                MockMvcRequestBuilders.post("/widgets/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"x\":1, \"y\":2, \"z\":-1, \"width\":4, \"height\":5}");

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

    @Test
    public void shouldReturnBadRequestIfParametersMissing() throws Exception {
        given(widgetRepository.add(any())).willReturn(SAMPLE_WIDGET);

        final MockHttpServletRequestBuilder parametersIsMissing =
                MockMvcRequestBuilders.post("/widgets/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"x\":1}");

        mockMvc.perform(parametersIsMissing)
                .andExpect(status().isBadRequest());
    }
}
