package com.madkroll.widgets.web.mvc;

import com.madkroll.widgets.web.ratelimit.RateLimiter;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
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

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class GlobalRateLimiterIT {

    private static final String WIDGET_ID = "widget-id";
    private final static long FIVE_MINUTES_IN_NANOS = 5 * 60 * 1000 * 1000;
    private final static long LIMIT = 10L;
    private final static long ONE_IN_TIME = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsumptionProbe probe;

    @MockBean
    private Bucket bucket;

    @MockBean(name = "globalRateLimiter")
    private RateLimiter globalRateLimiter;

    @Test
    public void shouldReturnTooManyRequestsIfLimitExceeded() throws Exception {
        given(probe.getNanosToWaitForRefill()).willReturn(FIVE_MINUTES_IN_NANOS);
        given(bucket.tryConsumeAndReturnRemaining(ONE_IN_TIME)).willReturn(probe);
        given(globalRateLimiter.getLimit()).willReturn(LIMIT);
        given(globalRateLimiter.getAvailableSlots()).willReturn(bucket);
        given(probe.isConsumed()).willReturn(false);

        final MockHttpServletRequestBuilder requestRejected =
                MockMvcRequestBuilders.get("/widgets/get/" + WIDGET_ID);

        mockMvc.perform(requestRejected)
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("X-RateLimit-Limit"))
                .andExpect(header().exists("X-Rate-Limit-Remaining"))
                .andExpect(header().exists("X-RateLimit-Reset"));
    }
}
