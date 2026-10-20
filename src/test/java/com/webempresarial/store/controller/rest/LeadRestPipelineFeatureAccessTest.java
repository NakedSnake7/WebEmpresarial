package com.webempresarial.store.controller.rest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.authentication
        .UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority
        .SimpleGrantedAuthority;
import org.springframework.security.core.context
        .SecurityContextHolder;

import com.webempresarial.store.crm.merge.LeadMergeService;
import com.webempresarial.store.dto.lead.UpdateLeadStatusDTO;
import com.webempresarial.store.exceptions.FeatureLockedException;
import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.LeadService;
import com.webempresarial.store.service.SalesTaskService;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.crm.LeadAuditLogService;
import com.webempresarial.store.service.crm.LeadTimelineService;

class LeadRestPipelineFeatureAccessTest {

    private LeadService leadService;
    private SalesTaskService taskService;
    private StoreContextService storeContextService;
    private LeadAuditLogService leadAuditLogService;
    private LeadTimelineService leadTimelineService;
    private LeadMergeService leadMergeService;
    private FeatureAccessService featureAccessService;
    private HttpServletRequest request;

    private Store store;
    private LeadRestController controller;

    @BeforeEach
    void setUp() {

        SecurityContextHolder.clearContext();

        leadService = mock(LeadService.class);
        taskService = mock(SalesTaskService.class);
        storeContextService = mock(StoreContextService.class);
        leadAuditLogService = mock(LeadAuditLogService.class);
        leadTimelineService = mock(LeadTimelineService.class);
        leadMergeService = mock(LeadMergeService.class);
        featureAccessService = mock(FeatureAccessService.class);
        request = mock(HttpServletRequest.class);

        store = new Store();
        store.setId(10L);

        when(storeContextService.getCurrentStore(request))
                .thenReturn(store);

        controller =
                new LeadRestController(
                        leadService,
                        taskService,
                        storeContextService,
                        leadAuditLogService,
                        leadTimelineService,
                        leadMergeService,
                        featureAccessService
                );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void pipelineViewShouldUsePipelineEntitlementForSharedLeadReads() {

        authenticate(
                AdminPermission.PIPELINE_VIEW
        );

        when(featureAccessService.canUse(
                store,
                Feature.PIPELINE
        )).thenReturn(true);

        when(leadService.getLeadsForStore(10L))
                .thenReturn(List.of());

        controller.getLeads(request);

        verify(featureAccessService)
                .canUse(
                        store,
                        Feature.PIPELINE
                );

        verify(featureAccessService, never())
                .requireFeature(
                        store,
                        Feature.LEADS
                );

        verify(leadService)
                .getLeadsForStore(10L);
    }

    @Test
    void pipelineManageShouldUsePipelineEntitlementForStatusMutation() {

        authenticate(
                AdminPermission.PIPELINE_MANAGE
        );

        when(featureAccessService.canUse(
                store,
                Feature.PIPELINE
        )).thenReturn(true);

        UpdateLeadStatusDTO dto =
                mock(UpdateLeadStatusDTO.class);

        controller.updateStatus(
                55L,
                dto,
                request
        );

        verify(featureAccessService)
                .canUse(
                        store,
                        Feature.PIPELINE
                );

        verify(featureAccessService, never())
                .requireFeature(
                        store,
                        Feature.LEADS
                );

        verify(leadService)
                .updateStatus(
                        eq(55L),
                        eq(10L),
                        any(),
                        isNull()
                );
    }

    @Test
    void pipelineViewShouldNotFallBackToLeadsWhenPipelineIsLocked() {

        authenticate(
                AdminPermission.PIPELINE_VIEW
        );

        when(featureAccessService.canUse(
                store,
                Feature.PIPELINE
        )).thenReturn(false);

        doThrow(
                new FeatureLockedException(
                        Feature.PIPELINE
                )
        )
        .when(featureAccessService)
        .requireFeature(
                store,
                Feature.PIPELINE
        );

        assertThatThrownBy(() ->
                controller.getLeads(request)
        )
        .isInstanceOf(
                FeatureLockedException.class
        );

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.PIPELINE
                );

        verify(featureAccessService, never())
                .requireFeature(
                        store,
                        Feature.LEADS
                );

        verifyNoInteractions(
                leadService
        );
    }

    @Test
    void crmViewShouldContinueUsingLeadsEntitlement() {

        authenticate(
                AdminPermission.CRM_VIEW
        );

        when(featureAccessService.canUse(
                store,
                Feature.LEADS
        )).thenReturn(true);

        when(leadService.getLeadsForStore(10L))
                .thenReturn(List.of());

        controller.getLeads(request);

        verify(featureAccessService)
                .canUse(
                        store,
                        Feature.LEADS
                );

        verify(featureAccessService, never())
                .canUse(
                        store,
                        Feature.PIPELINE
                );

        verify(leadService)
                .getLeadsForStore(10L);
    }

    private void authenticate(
            AdminPermission permission
    ) {

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        "pipeline@test",
                        "password",
                        List.of(
                                new SimpleGrantedAuthority(
                                        permission.authority()
                                )
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }
}
