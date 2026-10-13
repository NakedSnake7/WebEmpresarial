package com.webempresarial.store.controller;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.webempresarial.store.commerce.application.order.OrderService;
import com.webempresarial.store.controller.advice.MvcExceptionHandler;
import com.webempresarial.store.controller.advice.RestExceptionHandler;
import com.webempresarial.store.feature.registry.DashboardRegistry;
import com.webempresarial.store.repository.ResenaRepository;
import com.webempresarial.store.service.ProductoService;
import com.webempresarial.store.theme.StoreResolver;
import com.webempresarial.store.theme.StoreThemeResolver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class CheckoutConfirmationHttpStatusTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        HomeController controller =
                new HomeController(
                        mock(ProductoService.class),
                        mock(ResenaRepository.class),
                        mock(StoreThemeResolver.class),
                        mock(StoreResolver.class),
                        mock(DashboardRegistry.class),
                        mock(OrderService.class)
                );

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(controller)
                        .setControllerAdvice(
                                new MvcExceptionHandler(),
                                new RestExceptionHandler()
                        )
                        .build();
    }

    @Test
    void graciasWithDifferentOrderMustReturn404()
            throws Exception {

        MockHttpSession session =
                new MockHttpSession();

        session.setAttribute(
                "CHECKOUT_CONFIRMATION_ORDER_ID",
                5L
        );

        mockMvc.perform(
                        get("/gracias")
                                .param(
                                        "order_id",
                                        "999999"
                                )
                                .session(session)
                )
                .andExpect(
                        status().isNotFound()
                );
    }
}
