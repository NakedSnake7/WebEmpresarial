package com.webempresarial.store.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.webempresarial.store.controller.advice.MvcExceptionHandler;
import com.webempresarial.store.controller.advice.RestExceptionHandler;
import com.webempresarial.store.knowledge.api.exception.KnowledgeApiExceptionHandler;

import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.Order;

class ExceptionHandlerOrderContractTest {

    @Test
    void specializedKnowledgeHandlerMustRunBeforeGenericRestAndMvcHandlers() {

        int knowledgeOrder =
                KnowledgeApiExceptionHandler.class
                        .getAnnotation(Order.class)
                        .value();

        int restOrder =
                RestExceptionHandler.class
                        .getAnnotation(Order.class)
                        .value();

        int mvcOrder =
                MvcExceptionHandler.class
                        .getAnnotation(Order.class)
                        .value();

        assertThat(knowledgeOrder)
                .isLessThan(restOrder);

        assertThat(restOrder)
                .isLessThan(mvcOrder);
    }
}
