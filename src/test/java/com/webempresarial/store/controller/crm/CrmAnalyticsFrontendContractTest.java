package com.webempresarial.store.controller.crm;

import com.webempresarial.store.feature.catalog.PlatformFeatures;
import com.webempresarial.store.feature.sidebar.SidebarItemDefinition;
import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.Feature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CrmAnalyticsFrontendContractTest {

    @Test
    void reportsNavigationShouldBelongToPlatformAnalytics() {

        SidebarItemDefinition analytics =
                new PlatformFeatures()
                        .descriptor()
                        .getSidebarSections()
                        .stream()
                        .flatMap(section ->
                                section.getItems().stream()
                        )
                        .filter(item ->
                                "/crm/reports".equals(
                                        item.url()
                                )
                        )
                        .findFirst()
                        .orElseThrow();

        assertThat(analytics.title())
                .isEqualTo("Analytics");

        assertThat(analytics.url())
                .isEqualTo("/crm/reports");

        assertThat(analytics.feature())
                .isEqualTo(Feature.ANALYTICS);

        assertThat(analytics.requiredPermission())
                .isEqualTo(
                        AdminPermission.ANALYTICS_VIEW
                );
    }

    @Test
    void reportsViewShouldExistAndRenderAnalyticsMetrics()
            throws IOException {

        String reports = Files.readString(
                Path.of(
                        "src/main/resources/templates/"
                                + "crm/reports.html"
                )
        );

        assertThat(reports)
                .contains(
                        "data-crm-view=\"reports\""
                )
                .contains(
                        "${analytics.leadsToday}"
                )
                .contains(
                        "${analytics.leadsThisWeek}"
                )
                .contains(
                        "${analytics.leadsThisMonth}"
                )
                .contains(
                        "${analytics.hotLeads}"
                )
                .contains(
                        "analytics.pipelineValue"
                )
                .contains(
                        "analytics.revenueForecast"
                )
                .contains(
                        "analytics.closeRate"
                )
                .contains(
                        "${analytics.bestChannel}"
                )
                .contains(
                        "${analytics.bookedCalls}"
                )
                .contains(
                        "${analytics.sentProposals}"
                )
                .contains(
                        "${analytics.pendingTasks}"
                )
                .contains(
                        "${analytics.overdueTasks}"
                );
    }
}
