package com.webempresarial.store.service;

import com.webempresarial.store.commerce.infrastructure.order.notification.NotificationService;
import com.webempresarial.store.entity.Lead;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.LeadPriority;
import com.webempresarial.store.model.Store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutomationServiceTest {

    @Mock
    private SalesTaskService taskService;

    @Mock
    private WhatsAppService whatsAppService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private FeatureAccessService featureAccessService;

    private AutomationService automationService;

    private Store store;
    private Lead lead;

    @BeforeEach
    void setUp() {
        automationService = new AutomationService(
                taskService,
                whatsAppService,
                notificationService,
                featureAccessService
        );

        store = new Store();

        lead = new Lead();
        lead.setStore(store);
    }

    @Test
    void followUpShouldCreateTaskWhenStoreCanUseLeads() {

        when(
                featureAccessService.canUse(
                        store,
                        Feature.LEADS
                )
        ).thenReturn(true);

        automationService.onLeadNotResponding24h(lead);

        verify(featureAccessService)
                .canUse(store, Feature.LEADS);

        verify(taskService)
                .createTask(
                        eq(lead),
                        eq("Follow-up 24h"),
                        eq("Enviar seguimiento corto por WhatsApp."),
                        any(LocalDateTime.class),
                        eq(LeadPriority.MEDIUM)
                );
    }

    @Test
    void followUpShouldDoNothingWhenStoreCannotUseLeads() {

        when(
                featureAccessService.canUse(
                        store,
                        Feature.LEADS
                )
        ).thenReturn(false);

        automationService.onLeadNotResponding24h(lead);

        verify(featureAccessService)
                .canUse(store, Feature.LEADS);

        verify(taskService, never())
                .createTask(
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                );
    }
}
