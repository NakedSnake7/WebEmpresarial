package com.webempresarial.store.config;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlatformWorkspaceActivePathTest {

    @Test
    void platformRoutesShouldResolveToPlatformWorkspace() {

        FeatureViewAdvice advice = advice();

        for (String path : new String[] {
                "/admin/platform",
                "/admin/platform/operations",
                "/admin/platform/dev",
                "/admin/platform/timeline/abc",
                "/admin/platform/trace/abc",
                "/admin/platform/graph/abc"
        }) {
            HttpServletRequest request =
                    mock(HttpServletRequest.class);

            when(request.getRequestURI())
                    .thenReturn(path);

            assertThat(
                    advice.sidebarActivePath(request)
            )
                    .as(path)
                    .isEqualTo("/admin/platform");
        }
    }

    @Test
    void unrelatedAdminRouteMustNotBeRemapped() {

        FeatureViewAdvice advice = advice();

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/admin/permissions");

        assertThat(
                advice.sidebarActivePath(request)
        )
                .isEqualTo("/admin/permissions");
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
