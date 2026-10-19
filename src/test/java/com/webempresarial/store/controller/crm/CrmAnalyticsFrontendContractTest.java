package com.webempresarial.store.controller.crm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CrmAnalyticsFrontendContractTest {

    @Test
    void reportsNavigationShouldRequireAnalyticsPermission()
            throws IOException {

        String sidebar = Files.readString(
                Path.of(
                        "src/main/resources/templates/"
                                + "crm/crm-sidebar.html"
                )
        );

        assertThat(sidebar)
                .contains(
                        "th:if=\"${canViewAnalytics}\""
                )
                .contains(
                        "th:href=\"@{/crm/reports}\""
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
