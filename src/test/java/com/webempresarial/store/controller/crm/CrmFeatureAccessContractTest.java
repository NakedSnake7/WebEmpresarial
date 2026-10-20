package com.webempresarial.store.controller.crm;

import com.webempresarial.store.controller.admin.LeadAdminController;
import com.webempresarial.store.controller.rest.CrmDashboardRestController;
import com.webempresarial.store.controller.rest.LeadRestController;
import com.webempresarial.store.controller.rest.PipelineRestController;
import com.webempresarial.store.controller.rest.ProposalRestController;
import com.webempresarial.store.controller.rest.SalesTaskRestController;

import com.webempresarial.store.crm.merge.LeadMergeService;

import com.webempresarial.store.exceptions.FeatureLockedException;

import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;

import com.webempresarial.store.repository.LeadRepository;

import com.webempresarial.store.service.CrmDashboardService;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.LeadService;
import com.webempresarial.store.service.PipelineService;
import com.webempresarial.store.service.ProposalPdfService;
import com.webempresarial.store.service.ProposalService;
import com.webempresarial.store.service.SalesTaskService;
import com.webempresarial.store.service.StoreContextService;

import com.webempresarial.store.service.crm.LeadAuditLogService;
import com.webempresarial.store.service.crm.LeadTimelineService;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ui.ExtendedModelMap;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrmFeatureAccessContractTest {

    @Mock
    private StoreContextService storeContextService;

    @Mock
    private FeatureAccessService featureAccessService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private CrmDashboardService crmDashboardService;

    @Mock
    private LeadService leadService;

    @Mock
    private SalesTaskService salesTaskService;

    @Mock
    private LeadAuditLogService leadAuditLogService;

    @Mock
    private LeadTimelineService leadTimelineService;

    @Mock
    private LeadMergeService leadMergeService;

    @Mock
    private PipelineService pipelineService;

    @Mock
    private ProposalService proposalService;

    @Mock
    private ProposalPdfService proposalPdfService;

    private Store store;

    @BeforeEach
    void setUp() {

        store = new Store();
        store.setId(10L);

        when(
                storeContextService.getCurrentStore(
                        request
                )
        ).thenReturn(store);
    }

    @Test
    void adminLeadListShouldRequireLeads() {

        when(
                leadRepository
                        .findByStoreIdAndMergedFalseOrderByCreatedAtDesc(
                                10L
                        )
        ).thenReturn(List.of());

        LeadAdminController controller =
                new LeadAdminController(
                        leadRepository,
                        storeContextService,
                        featureAccessService
                );

        controller.index(
                new ExtendedModelMap(),
                request
        );

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.LEADS
                );

        verify(leadRepository)
                .findByStoreIdAndMergedFalseOrderByCreatedAtDesc(
                        10L
                );
    }

    @Test
    void crmDashboardApiShouldRequireCrm() {

        CrmDashboardRestController controller =
                new CrmDashboardRestController(
                        crmDashboardService,
                        storeContextService,
                        featureAccessService
                );

        controller.getDashboard(request);

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.CRM
                );

        verify(crmDashboardService)
                .getDashboard(10L);
    }

    @Test
    void crmDashboardTasksShouldRequireCrmAndTasks() {

        CrmDashboardRestController controller =
                new CrmDashboardRestController(
                        crmDashboardService,
                        storeContextService,
                        featureAccessService
                );

        controller.getUpcomingTasks(request);

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.CRM
                );

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.TASKS
                );

        verify(crmDashboardService)
                .getUpcomingTasks(10L);
    }

    @Test
    void lockedTasksShouldStopDashboardTaskService() {

        doNothing()
        .when(featureAccessService)
        .requireFeature(
                store,
                Feature.CRM
        );

        doThrow(
                new FeatureLockedException(
                        Feature.TASKS
                )
        )
        .when(featureAccessService)
        .requireFeature(
                store,
                Feature.TASKS
        );

        CrmDashboardRestController controller =
                new CrmDashboardRestController(
                        crmDashboardService,
                        storeContextService,
                        featureAccessService
                );

        assertThatThrownBy(() ->
                controller.getUpcomingTasks(request)
        )
        .isInstanceOf(
                FeatureLockedException.class
        );

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.CRM
                );

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.TASKS
                );

        verify(crmDashboardService, never())
                .getUpcomingTasks(
                        anyLong()
                );
    }

    @Test
    void leadApiShouldRequireLeads() {

        LeadRestController controller =
                leadController();

        controller.getLeads(request);

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.LEADS
                );

        verify(leadService)
                .getLeadsForStore(10L);
    }

    @Test
    void creatingLeadTaskShouldRequireLeadsAndTasks() {

        LeadRestController controller =
                leadController();

        controller.createTask(
                55L,
                null,
                request
        );

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.LEADS
                );

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.TASKS
                );

        verify(salesTaskService)
                .createManualTask(
                        55L,
                        10L,
                        null
                );
    }

    @Test
    void pipelineApiShouldRequirePipeline() {

        PipelineRestController controller =
                new PipelineRestController(
                        pipelineService,
                        storeContextService,
                        featureAccessService
                );

        controller.getStats(request);

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.PIPELINE
                );

        verify(pipelineService)
                .getStats(10L);
    }

    @Test
    void taskApiShouldRequireTasks() {

        SalesTaskRestController controller =
                new SalesTaskRestController(
                        salesTaskService,
                        storeContextService,
                        featureAccessService
                );

        controller.getTasks(request);

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.TASKS
                );

        verify(salesTaskService)
                .getAllTasks(10L);
    }

    @Test
    void proposalApiShouldRequireProposals() {

        ProposalRestController controller =
                new ProposalRestController(
                        proposalService,
                        storeContextService,
                        proposalPdfService,
                        featureAccessService
                );

        controller.getLeadProposals(
                55L,
                request
        );

        verify(featureAccessService)
                .requireFeature(
                        store,
                        Feature.PROPOSALS
                );

        verify(proposalService)
                .getByLead(
                        55L,
                        10L
                );
    }

    @Test
    void lockedCrmFeatureShouldStopBusinessService() {

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

        CrmDashboardRestController controller =
                new CrmDashboardRestController(
                        crmDashboardService,
                        storeContextService,
                        featureAccessService
                );

        assertThatThrownBy(
                () -> controller.getDashboard(
                        request
                )
        )
        .isInstanceOf(
                FeatureLockedException.class
        );

        verifyNoInteractions(
                crmDashboardService
        );
    }

    private LeadRestController leadController() {

        return new LeadRestController(
                leadService,
                salesTaskService,
                storeContextService,
                leadAuditLogService,
                leadTimelineService,
                leadMergeService,
                featureAccessService
        );
    }
}
