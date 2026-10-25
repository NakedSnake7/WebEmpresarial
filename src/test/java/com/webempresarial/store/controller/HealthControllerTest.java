package com.webempresarial.store.controller;

import com.webempresarial.store.config.StoreViewAdvice;
import com.webempresarial.store.exceptions.StoreNotFoundException;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.StoreSettingsService;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HealthControllerTest {

    @Test
    void shouldReturnOk() throws Exception {

        MockMvc mockMvc =
                MockMvcBuilders
                        .standaloneSetup(new HealthController())
                        .build();

        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void shouldNotDependOnTenantHost() throws Exception {

        StoreContextService storeContextService =
                mock(StoreContextService.class);

        when(storeContextService.getCurrentStore(any()))
                .thenThrow(
                        new StoreNotFoundException(
                                "webempresarial-rehearsal.onrender.com"
                        )
                );

        StoreViewAdvice storeViewAdvice =
                new StoreViewAdvice(
                        storeContextService,
                        mock(StoreSettingsService.class),
                        mock(FeatureAccessService.class)
                );

        MockMvc mockMvc =
                MockMvcBuilders
                        .standaloneSetup(new HealthController())
                        .setControllerAdvice(storeViewAdvice)
                        .build();

        mockMvc.perform(
                get("/health")
                        .header(
                                "Host",
                                "webempresarial-rehearsal.onrender.com"
                        )
        )
        .andExpect(status().isOk())
        .andExpect(content().string("OK"));
    }
}
