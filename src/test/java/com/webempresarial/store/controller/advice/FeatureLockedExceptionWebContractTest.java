package com.webempresarial.store.controller.advice;

import com.webempresarial.store.exceptions.FeatureLockedException;
import com.webempresarial.store.model.Feature;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.springframework.stereotype.Controller;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.springframework.test.context.web.WebAppConfiguration;

import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.context.WebApplicationContext;

import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.flash;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.jsonPath;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.redirectedUrl;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(
        classes =
                FeatureLockedExceptionWebContractTest
                        .TestConfig.class
)
class FeatureLockedExceptionWebContractTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        mockMvc =
                MockMvcBuilders
                        .webAppContextSetup(context)
                        .build();
    }

    @Test
    void mvcFeatureLockShouldRedirectToUpgrade()
            throws Exception {

        mockMvc.perform(
                get("/test/feature/mvc")
        )
        .andExpect(
                status().is3xxRedirection()
        )
        .andExpect(
                redirectedUrl(
                        "/admin/upgrade?feature=CRM"
                )
        )
        .andExpect(
                flash().attribute(
                        "lockedFeature",
                        "CRM"
                )
        );
    }

    @Test
    void restFeatureLockShouldReturnForbiddenJson()
            throws Exception {

        mockMvc.perform(
                get("/test/feature/api")
        )
        .andExpect(
                status().isForbidden()
        )
        .andExpect(
                jsonPath("$.success")
                        .value(false)
        )
        .andExpect(
                jsonPath("$.error")
                        .value("FEATURE_LOCKED")
        )
        .andExpect(
                jsonPath("$.feature")
                        .value("PIPELINE")
        )
        .andExpect(
                jsonPath("$.upgradeUrl")
                        .value(
                                "/admin/upgrade?feature=PIPELINE"
                        )
        );
    }

    @Configuration
    @EnableWebMvc
    @Import({
            MvcExceptionHandler.class,
            RestExceptionHandler.class,
            MvcTestController.class,
            RestTestController.class
    })
    static class TestConfig {
    }

    @Controller
    static class MvcTestController {

        @GetMapping("/test/feature/mvc")
        String lockedMvc() {

            throw new FeatureLockedException(
                    Feature.CRM
            );
        }
    }

    @RestController
    static class RestTestController {

        @GetMapping("/test/feature/api")
        String lockedApi() {

            throw new FeatureLockedException(
                    Feature.PIPELINE
            );
        }
    }
}
