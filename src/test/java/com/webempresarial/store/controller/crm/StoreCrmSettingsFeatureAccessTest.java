package com.webempresarial.store.controller.crm;

import com.webempresarial.store.controller.admin.store.crm.StoreCrmSettingsController;
import com.webempresarial.store.exceptions.FeatureLockedException;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.crm.LeadBudgetRangeService;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ui.ExtendedModelMap;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreCrmSettingsFeatureAccessTest {

    @Mock
    private StoreContextService storeContextService;

    @Mock
    private LeadBudgetRangeService budgetRangeService;

    @Mock
    private FeatureAccessService featureAccessService;

    @Mock
    private HttpServletRequest request;

    private Store store;
    private StoreCrmSettingsController controller;

    @BeforeEach
    void setUp() {
        store = new Store();
        store.setId(10L);

        when(
                storeContextService.getCurrentStore(request)
        ).thenReturn(store);

        controller =
                new StoreCrmSettingsController(
                        storeContextService,
                        budgetRangeService,
                        featureAccessService
                );
    }

    @Test
    void settingsIndexShouldRequireCrm() {

        controller.index(
                new ExtendedModelMap(),
                request
        );

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.CRM
                );
    }

    @Test
    void budgetRangesShouldRequireCrm() {

        controller.budgetRanges(
                new ExtendedModelMap(),
                request
        );

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.CRM
                );

        verify(budgetRangeService)
                .getRanges(store);
    }

    @Test
    void lockedCrmShouldStopBudgetMutation() {

        doThrow(
                new FeatureLockedException(
                        Feature.CRM
                )
        )
        .when(featureAccessService)
        .requireFeature(
                store,
                Feature.CRM
        );

        assertThatThrownBy(
                () -> controller.createBudgetRange(
                        "10k-25k",
                        "$10,000 - $25,000",
                        null,
                        null,
                        null,
                        5,
                        10,
                        request
                )
        )
        .isInstanceOf(
                FeatureLockedException.class
        );

        verifyNoInteractions(
                budgetRangeService
        );
    }

    @Test
    void crmPlaceholdersShouldRequireBaseCrm() {

        controller.leadSources(request);
        controller.scoring(request);
        controller.duplicate(request);
        controller.merge(request);

        verify(
                featureAccessService,
                times(4)
        )
        .requireFeature(
                store,
                Feature.CRM
        );
    }

    @Test
    void pipelineSettingsShouldRequirePipeline() {

        controller.pipeline(request);

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.PIPELINE
                );
    }

    @Test
    void automationSettingsShouldRequireAutomations() {

        controller.automation(request);

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.AUTOMATIONS
                );
    }

    @Test
    void aiPlaceholderShouldCurrentlyRequireBaseCrm() {

        controller.ai(request);

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.CRM
                );
    }
}
