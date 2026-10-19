package com.webempresarial.store.config;

import com.webempresarial.store.commerce.application.inventory.InventoryPersistentAlertService;
import com.webempresarial.store.feature.registry.SidebarRegistry;
import com.webempresarial.store.interceptor.AdminTenantAccessInterceptor;
import com.webempresarial.store.interceptor.SubscriptionInterceptor;
import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.service.AdminUserDetailsService;
import com.webempresarial.store.service.AuthUserDetailsService;
import com.webempresarial.store.service.FeatureAccessService;
import com.webempresarial.store.service.StoreContextService;
import com.webempresarial.store.service.StoreSettingsService;
import com.webempresarial.store.theme.StoreThemeResolver;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.patch;

import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;

@WebMvcTest(
        controllers =
                CrmAdminAuthorizationMockMvcTest
                        .TestController.class
)
@Import(SecurityConfig.class)
class CrmAdminAuthorizationMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminAuthenticationSuccessHandler
            adminAuthenticationSuccessHandler;

    @MockitoBean
    private AdminUserDetailsService
            adminUserDetailsService;

    @MockitoBean
    private AuthUserDetailsService
            authUserDetailsService;

    @MockitoBean
    private StoreContextService
            storeContextService;

    @MockitoBean
    private SidebarRegistry
            sidebarRegistry;

    @MockitoBean
    private InventoryPersistentAlertService
            inventoryPersistentAlertService;

    @MockitoBean
    private StoreSettingsService
            storeSettingsService;

    @MockitoBean
    private AdminTenantAccessInterceptor
            adminTenantAccessInterceptor;

    @MockitoBean
    private SubscriptionInterceptor
            subscriptionInterceptor;

    @MockitoBean
    private StoreThemeResolver
            storeThemeResolver;

    @MockitoBean
    private FeatureAccessService
            featureAccessService;

    @Test
    void clientWithoutAdminPermissionsShouldNotAccessCrm()
            throws Exception {

        mockMvc.perform(
                get("/crm/dashboard")
                        .with(
                                user("cliente@test.local")
                                        .roles("CLIENTE")
                        )
        )
        .andExpect(
                status().isForbidden()
        );

        mockMvc.perform(
                get("/api/crm/leads")
                        .with(
                                user("cliente@test.local")
                                        .roles("CLIENTE")
                        )
        )
        .andExpect(
                status().isForbidden()
        );
    }

    @Test
    void adminRoleWithoutPermissionShouldNotGrantCrmAccess()
            throws Exception {

        mockMvc.perform(
                get("/crm/dashboard")
                        .with(
                                user("admin@stride.test")
                                        .roles("STORE_ADMIN")
                        )
        )
        .andExpect(
                status().isForbidden()
        );
    }

    @Test
    void crmViewShouldAllowReadOperations()
            throws Exception {

        RequestPostProcessor admin =
                adminWith(
                        AdminPermission.CRM_VIEW
                );

        mockMvc.perform(
                get("/crm/dashboard")
                        .with(admin)
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/crm/leads/10")
                        .with(admin)
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/admin/leads")
                        .with(admin)
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/api/crm/dashboard")
                        .with(admin)
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/api/crm/leads")
                        .with(admin)
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/api/crm/tasks")
                        .with(admin)
        )
        .andExpect(status().isOk());
    }

    @Test
    void crmViewShouldNotAllowWriteOperations()
            throws Exception {

        RequestPostProcessor admin =
                adminWith(
                        AdminPermission.CRM_VIEW
                );

        mockMvc.perform(
                post("/admin/leads/10/status")
                        .with(admin)
                        .with(csrf())
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                patch("/api/crm/leads/10/status")
                        .with(admin)
                        .with(csrf())
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                patch("/api/crm/tasks/10/complete")
                        .with(admin)
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void crmEditShouldAllowCrmWrites()
            throws Exception {

        RequestPostProcessor admin =
                adminWith(
                        AdminPermission.CRM_EDIT
                );

        mockMvc.perform(
                post("/admin/leads/10/status")
                        .with(admin)
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                patch("/api/crm/leads/10/status")
                        .with(admin)
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                patch("/api/crm/tasks/10/complete")
                        .with(admin)
                        .with(csrf())
        )
        .andExpect(status().isOk());
    }

    @Test
    void pipelineShouldRequirePipelineView()
            throws Exception {

        mockMvc.perform(
                get("/crm/pipeline")
                        .with(
                                adminWith(
                                        AdminPermission.CRM_VIEW
                                )
                        )
        )
        .andExpect(status().isForbidden());

        RequestPostProcessor pipelineUser =
                adminWith(
                        AdminPermission.PIPELINE_VIEW
                );

        mockMvc.perform(
                get("/crm/pipeline")
                        .with(pipelineUser)
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/api/crm/pipeline/stats")
                        .with(pipelineUser)
        )
        .andExpect(status().isOk());
    }

    @Test
    void pipelineViewShouldAllowSharedLeadReads()
            throws Exception {

        RequestPostProcessor pipelineUser =
                adminWith(
                        AdminPermission.PIPELINE_VIEW
                );

        mockMvc.perform(
                get("/api/crm/leads")
                        .with(pipelineUser)
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/api/crm/leads/10")
                        .with(pipelineUser)
        )
        .andExpect(status().isOk());
    }

    @Test
    void pipelineViewShouldNotAllowPipelineMutation()
            throws Exception {

        mockMvc.perform(
                patch("/api/crm/leads/10/status")
                        .with(
                                adminWith(
                                        AdminPermission.PIPELINE_VIEW
                                )
                        )
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void pipelineManageShouldAllowLeadStatusMutation()
            throws Exception {

        mockMvc.perform(
                patch("/api/crm/leads/10/status")
                        .with(
                                adminWith(
                                        AdminPermission.PIPELINE_MANAGE
                                )
                        )
                        .with(csrf())
        )
        .andExpect(status().isOk());
    }

    @Test
    void pipelineViewShouldNotGrantProposalReads()
            throws Exception {

        mockMvc.perform(
                get("/api/crm/leads/10/proposals")
                        .with(
                                adminWith(
                                        AdminPermission.PIPELINE_VIEW
                                )
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void crmViewShouldNotAllowGenericCrmWrites()
            throws Exception {

        RequestPostProcessor viewer =
                adminWith(
                        AdminPermission.CRM_VIEW
                );

        mockMvc.perform(
                post("/api/crm/leads/10/notes")
                        .with(viewer)
                        .with(csrf())
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                post("/api/crm/leads/10/tasks")
                        .with(viewer)
                        .with(csrf())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void crmEditShouldAllowGenericCrmWrites()
            throws Exception {

        RequestPostProcessor editor =
                adminWith(
                        AdminPermission.CRM_EDIT
                );

        mockMvc.perform(
                post("/api/crm/leads/10/notes")
                        .with(editor)
                        .with(csrf())
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/api/crm/leads/10/tasks")
                        .with(editor)
                        .with(csrf())
        )
        .andExpect(status().isOk());
    }

    @Test
    void reportsShouldRequireAnalyticsView()
            throws Exception {

        mockMvc.perform(
                get("/crm/reports")
                        .with(
                                adminWith(
                                        AdminPermission.CRM_VIEW
                                )
                        )
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                get("/crm/reports")
                        .with(
                                adminWith(
                                        AdminPermission.ANALYTICS_VIEW
                                )
                        )
        )
        .andExpect(status().isOk());
    }

    @Test
    void proposalReadsShouldRequireProposalView()
            throws Exception {

        mockMvc.perform(
                get("/api/crm/leads/10/proposals")
                        .with(
                                adminWith(
                                        AdminPermission.CRM_VIEW
                                )
                        )
        )
        .andExpect(status().isForbidden());

        RequestPostProcessor proposalViewer =
                adminWith(
                        AdminPermission.PROPOSAL_VIEW
                );

        mockMvc.perform(
                get("/api/crm/leads/10/proposals")
                        .with(proposalViewer)
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/api/crm/proposals/20/pdf")
                        .with(proposalViewer)
        )
        .andExpect(status().isOk());
    }

    @Test
    void proposalCreationShouldRequireProposalCreate()
            throws Exception {

        mockMvc.perform(
                post("/api/crm/leads/10/proposals")
                        .with(
                                adminWith(
                                        AdminPermission.PROPOSAL_VIEW
                                )
                        )
                        .with(csrf())
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                post("/api/crm/leads/10/proposals")
                        .with(
                                adminWith(
                                        AdminPermission.PROPOSAL_CREATE
                                )
                        )
                        .with(csrf())
        )
        .andExpect(status().isOk());
    }

    @Test
    void proposalMutationShouldRequireProposalEdit()
            throws Exception {

        mockMvc.perform(
                patch("/api/crm/proposals/20/send")
                        .with(
                                adminWith(
                                        AdminPermission.PROPOSAL_VIEW
                                )
                        )
                        .with(csrf())
        )
        .andExpect(status().isForbidden());

        mockMvc.perform(
                patch("/api/crm/proposals/20/send")
                        .with(
                                adminWith(
                                        AdminPermission.PROPOSAL_EDIT
                                )
                        )
                        .with(csrf())
        )
        .andExpect(status().isOk());
    }

    @Test
    void unknownCrmRoutesShouldFailClosed()
            throws Exception {

        mockMvc.perform(
                get("/api/crm/internal-future-endpoint")
                        .with(
                                adminWith(
                                        AdminPermission.CRM_VIEW,
                                        AdminPermission.CRM_EDIT,
                                        AdminPermission.PIPELINE_VIEW,
                                        AdminPermission.PIPELINE_MANAGE,
                                        AdminPermission.PROPOSAL_VIEW,
                                        AdminPermission.PROPOSAL_CREATE,
                                        AdminPermission.PROPOSAL_EDIT,
                                        AdminPermission.ANALYTICS_VIEW
                                )
                        )
        )
        .andExpect(status().isForbidden());
    }

    private RequestPostProcessor adminWith(
            AdminPermission... permissions
    ) {

        SimpleGrantedAuthority[] authorities =
                Stream.concat(
                        Stream.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_STORE_ADMIN"
                                )
                        ),
                        Arrays.stream(permissions)
                                .map(permission ->
                                        new SimpleGrantedAuthority(
                                                permission.authority()
                                        )
                                )
                )
                .toArray(
                        SimpleGrantedAuthority[]::new
                );

        return user(
                "admin@stride.test"
        ).authorities(
                authorities
        );
    }

    @RestController
    static class TestController {

        @GetMapping("/crm/dashboard")
        String dashboard() {
            return "OK";
        }

        @GetMapping("/crm/leads/{id}")
        String leadDetail() {
            return "OK";
        }

        @GetMapping("/crm/pipeline")
        String pipeline() {
            return "OK";
        }

        @GetMapping("/crm/tasks")
        String tasks() {
            return "OK";
        }

        @GetMapping("/crm/reports")
        String reports() {
            return "OK";
        }

        @GetMapping("/admin/leads")
        String adminLeads() {
            return "OK";
        }

        @PostMapping("/admin/leads/{id}/status")
        String updateAdminLeadStatus() {
            return "OK";
        }

        @GetMapping("/api/crm/dashboard")
        String apiDashboard() {
            return "OK";
        }

        @GetMapping("/api/crm/leads")
        String leads() {
            return "OK";
        }

        @GetMapping("/api/crm/leads/{id}")
        String lead() {
            return "OK";
        }

        @PostMapping("/api/crm/leads/{id}/notes")
        String createNote() {
            return "OK";
        }

        @PostMapping("/api/crm/leads/{id}/tasks")
        String createTask() {
            return "OK";
        }

        @PatchMapping("/api/crm/leads/{id}/status")
        String updateLeadStatus() {
            return "OK";
        }

        @GetMapping("/api/crm/pipeline/stats")
        String pipelineStats() {
            return "OK";
        }

        @GetMapping("/api/crm/tasks")
        String apiTasks() {
            return "OK";
        }

        @PatchMapping("/api/crm/tasks/{id}/complete")
        String completeTask() {
            return "OK";
        }

        @GetMapping("/api/crm/leads/{id}/proposals")
        String proposals() {
            return "OK";
        }

        @PostMapping("/api/crm/leads/{id}/proposals")
        String createProposal() {
            return "OK";
        }

        @PatchMapping("/api/crm/proposals/{id}/send")
        String sendProposal() {
            return "OK";
        }

        @GetMapping("/api/crm/proposals/{id}/pdf")
        String proposalPdf() {
            return "OK";
        }

        @GetMapping("/api/crm/internal-future-endpoint")
        String futureEndpoint() {
            return "OK";
        }
    }
}
