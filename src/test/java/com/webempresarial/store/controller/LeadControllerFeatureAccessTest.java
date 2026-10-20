package com.webempresarial.store.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.webempresarial.store.dto.LeadRequestDTO;
import com.webempresarial.store.entity.Lead;
import com.webempresarial.store.exceptions.FeatureLockedException;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.LeadService;
import com.webempresarial.store.theme.StoreResolver;

class LeadControllerFeatureAccessTest {

    private LeadService leadService;
    private StoreResolver storeResolver;
    private FeatureAccessService featureAccessService;
    private HttpServletRequest request;
    private LeadRequestDTO dto;
    private Store store;

    private LeadController controller;

    @BeforeEach
    void setUp() {

        leadService = mock(LeadService.class);
        storeResolver = mock(StoreResolver.class);
        featureAccessService = mock(FeatureAccessService.class);
        request = mock(HttpServletRequest.class);
        dto = mock(LeadRequestDTO.class);
        store = mock(Store.class);

        controller = new LeadController(
                leadService,
                storeResolver,
                featureAccessService
        );

        when(storeResolver.getCurrentStore(request))
                .thenReturn(store);
    }

    @Test
    void createLeadShouldRequireLeadsForResolvedStore() {

        Lead lead = mock(Lead.class);

        when(leadService.createLead(dto, store))
                .thenReturn(lead);

        when(lead.getId())
                .thenReturn(55L);

        when(store.getNombre())
                .thenReturn("Stride");

        var response =
                controller.createLead(
                        dto,
                        request
                );

        assertThat(response.getStatusCode().value())
                .isEqualTo(201);

        verify(storeResolver)
                .getCurrentStore(request);

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.LEADS
                );

        verify(leadService)
                .createLead(
                        dto,
                        store
                );
    }

    @Test
    void createLeadShouldStopWhenLeadsFeatureIsLocked() {

        doThrow(
                new FeatureLockedException(
                        Feature.LEADS
                )
        )
        .when(featureAccessService)
        .requireFeature(
                store,
                Feature.LEADS
        );

        assertThatThrownBy(() ->
                controller.createLead(
                        dto,
                        request
                )
        )
        .isInstanceOf(
                FeatureLockedException.class
        );

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.LEADS
                );

        verifyNoInteractions(
                leadService
        );
    }
}
