package com.webempresarial.store.config;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SaasWorkspaceActivePathTest {

    @Test
    void saasAdministrationRoutesShouldResolveToSaasWorkspace() {

        FeatureViewAdvice advice = advice();

        for (String path : new String[] {
                "/admin/saas",
                "/admin/stores",
                "/admin/stores/nuevo",
                "/admin/stores/editar/42",
                "/admin/stores/42/admins",
                "/admin/stores/42/admins/nuevo",
                "/admin/subscriptions",
                "/admin/subscriptions/42/activate"
        }) {
            HttpServletRequest request =
                    mock(HttpServletRequest.class);

            when(request.getRequestURI())
                    .thenReturn(path);

            assertThat(
                    advice.sidebarActivePath(request)
            )
                    .as(path)
                    .isEqualTo("/admin/saas");
        }
    }

    @Test
    void marketplaceMustRemainIndependent() {

        FeatureViewAdvice advice = advice();

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/admin/marketplace");

        assertThat(
                advice.sidebarActivePath(request)
        )
                .isEqualTo("/admin/marketplace");
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
