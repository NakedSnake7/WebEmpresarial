package com.webempresarial.store.service;

import com.webempresarial.store.config.AdminCredentialVersionFilter;
import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.repository.AdminUserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:mysql://127.0.0.1:3306/webempresarial_flyway_test",
    "spring.flyway.enabled=true",
    "spring.flyway.locations=classpath:db/migration",
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.task.scheduling.enabled=false",
    "webempresarial.demo.digital-transformation.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@ContextConfiguration(
    initializers = AdminCredentialSessionHttpIT.DatabaseGuard.class
)
class AdminCredentialSessionHttpIT {

    private static final String DATABASE =
            "webempresarial_flyway_test";

    private static final String URL =
            "jdbc:mysql://127.0.0.1:3306/" + DATABASE;

    private static final String ORIGINAL =
            "ClaveOriginalSegura#2026";

    private static final String NEW_PASSWORD =
            "ClaveRenovadaSegura#2026";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AdminUserRepository repository;

    @Autowired
    private AdminPasswordChangeService passwordService;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private DataSource dataSource;

    private Long adminId;
    private String email;

    public static class DatabaseGuard
            implements ApplicationContextInitializer<
                    ConfigurableApplicationContext> {

        @Override
        public void initialize(
                ConfigurableApplicationContext context
        ) {
            var environment = context.getEnvironment();

            String datasourceUrl =
                    environment.getProperty(
                            "spring.datasource.url"
                    );

            String flywayUrl =
                    environment.getProperty(
                            "spring.flyway.url"
                    );

            if (!URL.equals(datasourceUrl)) {
                throw new IllegalStateException(
                        "Base no autorizada: " + datasourceUrl
                );
            }

            if (flywayUrl != null
                    && !flywayUrl.isBlank()
                    && !URL.equals(flywayUrl)) {

                throw new IllegalStateException(
                        "Flyway apunta a otra base"
                );
            }
        }
    }

    @BeforeEach
    void createTemporaryAdministrator() throws Exception {

        verifyDatabase();

        email = "session-http-it-"
                + UUID.randomUUID()
                + "@example.invalid";

        AdminUser admin = new AdminUser();
        admin.setFullName("Admin Session HTTP IT");
        admin.setEmail(email);
        admin.setRole(AdminRole.SUPER_ADMIN);
        admin.setEnabled(true);
        admin.setPassword(
                encoder.encode(ORIGINAL)
        );

        adminId = repository.saveAndFlush(admin).getId();

        assertNotNull(adminId);
    }

    @AfterEach
    void removeTemporaryAdministrator() throws Exception {

        if (adminId != null) {
            verifyDatabase();
            repository.deleteById(adminId);
        }
    }

    private void verifyDatabase() throws Exception {

        try (Connection connection =
                     dataSource.getConnection()) {

            assertEquals(
                    DATABASE,
                    connection.getCatalog(),
                    "La prueba no debe usar webempresarial_dev"
            );
        }
    }

    private MockHttpSession login(
            String password,
            long expectedCredentialVersion
    ) throws Exception {

        MvcResult result = mvc.perform(
                post("/admin/login")
                    .with(csrf())
                    .param("username", email)
                    .param("password", password)
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
                "/admin/dashboard"
        ))
        .andReturn();

        MockHttpSession session =
                (MockHttpSession) result
                        .getRequest()
                        .getSession(false);

        assertNotNull(
                session,
                "El login debe crear una sesión"
        );

        assertEquals(
                expectedCredentialVersion,
                session.getAttribute(
                        AdminCredentialVersionFilter.SESSION_KEY
                )
        );

        return session;
    }

    @Test
    void passwordRotationRevokesOldSessionsAndAllowsNewLogin()
            throws Exception {

        MockHttpSession firstSession =
                login(ORIGINAL, 0L);

        MockHttpSession secondSession =
                login(ORIGINAL, 0L);

        /*
         * Antes de cambiar la contraseña, la sesión
         * puede abrir la página protegida.
         */
        mvc.perform(
                get("/admin/account/password")
                    .session(firstSession)
        )
        .andExpect(status().isOk());

        /*
         * La operación real usa JPA, BCrypt,
         * transacción y bloqueo PESSIMISTIC_WRITE.
         */
        passwordService.changeOwnPassword(
                email,
                ORIGINAL,
                NEW_PASSWORD
        );

        AdminUser updated = repository
                .findByEmail(email)
                .orElseThrow();

        assertEquals(
                1L,
                updated.getCredentialVersion()
        );

        /*
         * La primera sesión debe cerrarse.
         */
        mvc.perform(
                get("/admin/account/password")
                    .session(firstSession)
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
                "/admin/login?sessionExpired"
        ));

        assertTrue(
                firstSession.isInvalid()
        );

        /*
         * La segunda sesión tampoco puede
         * utilizar una API administrativa.
         */
        mvc.perform(
                get("/api/admin/saas")
                    .session(secondSession)
        )
        .andExpect(status().isUnauthorized());

        assertTrue(
                secondSession.isInvalid()
        );

        /*
         * Un nuevo login utiliza la versión 1.
         */
        MockHttpSession newSession =
                login(NEW_PASSWORD, 1L);

        mvc.perform(
                get("/admin/account/password")
                    .session(newSession)
        )
        .andExpect(status().isOk());
    }
}
