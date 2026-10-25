package com.webempresarial.store.controller;

import com.webempresarial.store.feature.PlatformAccessService;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.commerce.application.order.OrderService;
import com.webempresarial.store.config.StoreViewAdvice;
import com.webempresarial.store.controller.advice.MvcExceptionHandler;
import com.webempresarial.store.feature.registry.DashboardRegistry;
import com.webempresarial.store.repository.ResenaRepository;
import com.webempresarial.store.repository.StoreRepository;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.ProductoService;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.StoreLookupService;
import com.webempresarial.store.service.StoreSettingsService;
import com.webempresarial.store.theme.StoreResolver;
import com.webempresarial.store.theme.StoreThemeResolver;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UnknownTenantHttpStatusTest {

    @Test
    void unknownTenantHostMustReturn404() throws Exception {

        StoreRepository storeRepository =
                mock(StoreRepository.class);

        when(storeRepository.findByDominioAndActivaTrue(
                "missing.web-empresarial.com"
        )).thenReturn(Optional.empty());

        StoreLookupService storeLookupService =
                new StoreLookupService(
                        storeRepository,
                        mock(PlatformAccessService.class),
                        mock(FeatureAccessService.class)
                );

        StoreResolver storeResolver =
                new StoreResolver(storeLookupService);

        StoreContextService storeContextService =
                new StoreContextService(storeLookupService);

        FeatureAccessService featureAccessService =
                mock(FeatureAccessService.class);

        HomeController controller =
                new HomeController(
                        mock(ProductoService.class),
                        mock(ResenaRepository.class),
                        mock(StoreThemeResolver.class),
                        storeResolver,
                        featureAccessService,
                        mock(DashboardRegistry.class),
                        mock(OrderService.class)
                );

        StoreViewAdvice storeViewAdvice =
                new StoreViewAdvice(
                        storeContextService,
                        mock(StoreSettingsService.class),
                        featureAccessService
                );

        MockMvc mockMvc =
                MockMvcBuilders
                        .standaloneSetup(controller)
                        .setControllerAdvice(
                                storeViewAdvice,
                                new MvcExceptionHandler()
                        )
                        .build();

        mockMvc.perform(
                get("/")
                        .header(
                                "Host",
                                "missing.web-empresarial.com"
                        )
        )
        .andExpect(status().isNotFound());
    }
}
