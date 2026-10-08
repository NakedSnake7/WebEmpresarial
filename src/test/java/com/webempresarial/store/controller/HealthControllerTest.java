package com.webempresarial.store.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HealthControllerTest {

    private final MockMvc mockMvc =
            MockMvcBuilders.standaloneSetup(new HealthController()).build();

    @Test
    void shouldReturnOk() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void shouldNotDependOnTenantHost() throws Exception {
        mockMvc.perform(
                get("/health")
                    .header("Host", "webempresarial-rehearsal.onrender.com")
        )
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }
}
