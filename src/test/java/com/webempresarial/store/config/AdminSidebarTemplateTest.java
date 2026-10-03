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
                "/admin/dashboard"
        );

        assertThat(html)
                .doesNotContain("href=\"/admin/billing\"");
    }

    private String renderSidebar(
            boolean isSuperAdmin,
            boolean isStoreAdmin,
            String currentPath
    ) {
        Context context = new Context();

        context.setVariables(
                Map.of(
                        "isSuperAdmin", isSuperAdmin,
                        "isStoreAdmin", isStoreAdmin,
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
