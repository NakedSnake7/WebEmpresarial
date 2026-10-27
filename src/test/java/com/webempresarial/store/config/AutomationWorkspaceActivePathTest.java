package com.webempresarial.store.config;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AutomationWorkspaceActivePathTest {

    @Test
    void platformAutomationRoutesShouldResolveToKernelWorkspace() {

        FeatureViewAdvice advice = advice();

        for (String path : new String[] {
                "/admin/automations/kernel",
                "/admin/automations/history",
                "/admin/automations/history/42"
        }) {
            HttpServletRequest request =
                    mock(HttpServletRequest.class);

            when(request.getRequestURI())
                    .thenReturn(path);

            assertThat(
                    advice.sidebarActivePath(request)
            )
                    .as(path)
                    .isEqualTo(
                            "/admin/automations/kernel"
                    );
        }
    }

    @Test
    void tenantAutomationRootMustNotBeRemapped() {

        FeatureViewAdvice advice = advice();

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/admin/automations");

        assertThat(
                advice.sidebarActivePath(request)
        )
                .isEqualTo("/admin/automations");
    }

    private FeatureViewAdvice advice() {

        return new FeatureViewAdvice(
                null,
                null,
                null,
                null
        );
    }
}
