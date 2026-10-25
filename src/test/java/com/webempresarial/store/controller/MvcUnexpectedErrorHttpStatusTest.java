package com.webempresarial.store.controller;

import com.webempresarial.store.controller.advice.MvcExceptionHandler;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MvcUnexpectedErrorHttpStatusTest {

    @Test
    void unexpectedMvcExceptionMustReturn500() throws Exception {

        MockMvc mockMvc =
                MockMvcBuilders
                        .standaloneSetup(new FailingController())
                        .setControllerAdvice(
                                new MvcExceptionHandler()
                        )
                        .build();

        mockMvc.perform(get("/boom"))
                .andExpect(status().isInternalServerError());
    }

    @Controller
    static class FailingController {

        @GetMapping("/boom")
        String boom() {
            throw new IllegalStateException("boom");
        }
    }
}
