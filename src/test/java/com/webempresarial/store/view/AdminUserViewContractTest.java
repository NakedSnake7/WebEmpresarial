package com.webempresarial.store.view;

import org.junit.jupiter.api.Test;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class AdminUserViewContractTest {

    @Test
    void creationFormMustNotSubmitExistingUserId()
            throws IOException {

        String html = read(
                "templates/admin/users/form.html"
        );

        assertThat(html)
                .doesNotContain(
                        "th:field=\"*{id}\""
                );

        assertThat(html)
                .contains(
                        "th:each=\"role : ${roles}\""
                );

        assertThat(html)
                .contains(
                        "Crear usuario"
                );
    }

    @Test
    void teamListMustRespectPermissionFlags()
            throws IOException {

        String html = read(
                "templates/admin/users/list.html"
        );

        assertThat(html)
                .contains(
                        "th:if=\"${canInviteTeam}\""
                );

        assertThat(html)
                .contains(
                        "canEditTeam"
                );

        assertThat(html)
                .contains(
                        "STORE_OWNER"
                );

        assertThat(html)
                .contains(
                        "Propietario protegido"
                );
    }

    @Test
    void stateChangeMustIncludeCsrfProtection()
            throws IOException {

        String html = read(
                "templates/admin/users/list.html"
        );

        assertThat(html)
                .contains(
                        "/admin/users/estado/{id}"
                );

        assertThat(html)
                .contains(
                        "fragments/csrf :: token"
                );
    }

    @Test
    void sidebarMustNotHardcodeTeamModule()
            throws IOException {

        String html = read(
                "templates/admin/fragments/sidebar.html"
        );

        /*
         * /admin/users pertenece al catálogo dinámico
         * Feature.MULTI_USER.
         *
         * No debe existir una segunda entrada estática que
         * ignore el plan de la tienda.
         */
        assertThat(html)
                .doesNotContain(
                        "href=\"/admin/users\""
                );
    }

    private String read(
            String resource
    ) throws IOException {

        return new ClassPathResource(
                resource
        ).getContentAsString(
                StandardCharsets.UTF_8
        );
    }
}
