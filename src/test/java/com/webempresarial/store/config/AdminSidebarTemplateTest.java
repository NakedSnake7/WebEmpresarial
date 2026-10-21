package com.webempresarial.store.config;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static org.assertj.core.api.Assertions.assertThat;

class AdminSidebarTemplateTest {

    private TemplateEngine templateEngine;

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver resolver =
                new ClassLoaderTemplateResolver();

        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");

        SpringTemplateEngine engine =
                new SpringTemplateEngine();

        engine.setTemplateResolver(resolver);

        templateEngine = engine;
    }

    @Test
    void shouldShowBillingForSuperAdmin() {
        String html = renderSidebar(
                true,
                false,
                true,
                false,
                "/admin/dashboard"
        );

        assertThat(html)
                .contains("href=\"/admin/billing\"");
    }

    @Test
    void shouldShowBillingForStoreAdmin() {
        String html = renderSidebar(
                false,
                true,
                true,
                false,
                "/admin/dashboard"
        );

        assertThat(html)
                .contains("href=\"/admin/billing\"");
    }

    @Test
    void shouldHideBillingForStoreStaff() {
        String html = renderSidebar(
                false,
                false,
                false,
                false,
                "/admin/dashboard"
        );

        assertThat(html)
                .doesNotContain("href=\"/admin/billing\"");
    }

    @Test
    void shouldHideKnowledgeAndDigitalTransformationForSales() {

        String html = renderSidebar(
                false,
                false,
                false,
                true,
                "/admin/dashboard"
        );

        assertThat(html)
                .doesNotContain(
                        "href=\"/admin/digital-transformation/projects\""
                )
                .doesNotContain(
                        "href=\"/admin/knowledge\""
                )
                .doesNotContain(
                        "href=\"/admin/knowledge/library\""
                )
                .doesNotContain(
                        "href=\"/admin/knowledge/new\""
                );
    }

    private String renderSidebar(
            boolean isSuperAdmin,
            boolean isStoreAdmin,
            boolean canViewBilling,
            boolean isSales,
            String currentPath
    ) {
        Context context = new Context();

        context.setVariables(
                Map.of(
                        "isSuperAdmin", isSuperAdmin,
                        "isStoreAdmin", isStoreAdmin,
                        "isSales", isSales,
                        "canViewBilling", canViewBilling,
                        "currentPlan", "BASIC",
                        "theme", "WebEmpresarial"
                )
        );

        if (currentPath != null) {
            context.setVariable(
                    "currentPath",
                    currentPath
            );
        }

        return templateEngine.process(
                "admin/fragments/sidebar",
                context
        );
    }
}
