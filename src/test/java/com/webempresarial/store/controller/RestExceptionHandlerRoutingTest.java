package com.webempresarial.store.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.webempresarial.store.controller.advice.MvcExceptionHandler;
import com.webempresarial.store.controller.advice.RestExceptionHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

class RestExceptionHandlerRoutingTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(
                                new FailingRestController()
                        )
                        .setControllerAdvice(
                                new MvcExceptionHandler(),
                                new RestExceptionHandler()
                        )
                        .build();
    }

    @Test
    void restControllerExceptionMustReturnJson500()
            throws Exception {

        mockMvc.perform(
                        post("/test/rest/failure")
                )
                .andExpect(status().isInternalServerError())
                .andExpect(
                        content()
                                .contentTypeCompatibleWith(
                                        MediaType.APPLICATION_JSON
                                )
                )
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                );
    }

    @RestController
    static class FailingRestController {

        @PostMapping("/test/rest/failure")
        void fail() {
            throw new RuntimeException("boom");
        }
    }
}
