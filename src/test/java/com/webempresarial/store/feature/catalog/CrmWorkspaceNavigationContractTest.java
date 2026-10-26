package com.webempresarial.store.feature.catalog;

import com.webempresarial.store.feature.sidebar.SidebarItemDefinition;
import com.webempresarial.store.feature.sidebar.SidebarSectionDefinition;
import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.Feature;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CrmWorkspaceNavigationContractTest {

    @Test
    void crmSidebarShouldExposeSingleTopLevelWorkspace() {

        SidebarSectionDefinition crm =
                new CrmFeatures()
                        .descriptor()
                        .getSidebarSections()
                        .get(0);

        List<SidebarItemDefinition> items =
                crm.getItems();

        assertThat(crm.getTitle())
                .isEqualTo("CRM Comercial");

        assertThat(items)
                .hasSize(1);

        assertThat(items)
                .extracting(SidebarItemDefinition::title)
                .containsExactly("CRM");

        assertThat(items)
                .extracting(SidebarItemDefinition::url)
                .containsExactly("/crm/dashboard");

        assertThat(items)
                .extracting(SidebarItemDefinition::feature)
                .containsExactly(Feature.CRM);

        assertThat(items)
                .extracting(
                        SidebarItemDefinition::requiredPermission
                )
                .containsExactly(
                        AdminPermission.CRM_VIEW
                );
    }

    @Test
    void crmSidebarShouldNotPromoteWorkspaceSubsections() {

        List<String> titles =
                new CrmFeatures()
                        .descriptor()
                        .getSidebarSections()
                        .get(0)
                        .getItems()
                        .stream()
                        .map(SidebarItemDefinition::title)
                        .toList();

        assertThat(titles)
                .doesNotContain(
                        "CRM Dashboard",
                        "Pipeline",
                        "Leads",
                        "Tareas"
                );
    }

    @Test
    void crmFeaturesShouldRemainIndependentCapabilities() {

        var features =
                new CrmFeatures()
                        .descriptor()
                        .getFeatures();

        assertThat(features)
                .extracting(definition ->
                        definition.getFeature()
                )
                .contains(
                        Feature.CRM,
                        Feature.LEADS,
                        Feature.PIPELINE,
                        Feature.TASKS,
                        Feature.PROPOSALS
                );
    }
}
