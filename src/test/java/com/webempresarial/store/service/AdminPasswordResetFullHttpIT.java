package com.webempresarial.store.service;

import com.webempresarial.store.config.AdminCredentialVersionFilter;
import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.AdminPasswordResetToken;
import com.webempresarial.store.repository.AdminUserRepository;
import com.webempresarial.store.repository.AdminPasswordResetTokenRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.ArgumentCaptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.sql.DataSource;

import java.sql.Connection;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:mysql://127.0.0.1:3306/webempresarial_flyway_test",
    "spring.flyway.enabled=true",
    "spring.flyway.locations=classpath:db/migration",
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.task.scheduling.enabled=false",
    "webempresarial.demo.digital-transformation.enabled=false",
    "app.base-url=https://web-empresarial.com"
})
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@ContextConfiguration(
    initializers =
        AdminPasswordResetFullHttpIT.DatabaseGuard.class
)
class AdminPasswordResetFullHttpIT {

    private static final String DATABASE =
            "webempresarial_flyway_test";

    private static final String URL =
            "jdbc:mysql://127.0.0.1:3306/" + DATABASE;

    private static final String FORGOT =
            "/admin/password/forgot";

    private static final String RESET =
            "/admin/password/reset";

    private static final String ORIGINAL =
            "ClaveOriginalSegura#2026";

    private static final String RECOVERED =
            "ClaveRecuperadaSegura#2026";

    private static final String CHANGED =
            "ClaveCambiadaSegura#2026";

    private static final String REPLAY =
            "ClaveReutilizadaSegura#2026";

    private static final Pattern TOKEN_URL =
            Pattern.compile(
                "/admin/password/reset\\?token=([A-Za-z0-9_-]{43})"
            );

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AdminUserRepository admins;

    @Autowired
    private AdminPasswordResetTokenRepository tokens;

    @Autowired
    private AdminPasswordResetTokenCodec codec;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private DataSource datasource;

    @Autowired
    private JdbcTemplate jdbc;

    /*
     * El servicio real produce el correo.
     * Solo se simula la salida hacia MailerSend.
     */
    @MockitoBean
    private EmailService emailService;

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
                        "Base MySQL no autorizada: "
                        + datasourceUrl
                );
            }

            if (flywayUrl != null
                    && !flywayUrl.isBlank()
                    && !URL.equals(flywayUrl)) {
                throw new IllegalStateException(
                        "Flyway apunta a otra base de datos"
                );
            }
        }
    }

    @BeforeEach
    void createAdministrator() throws Exception {

        verifyDatabase();

        email = "reset-full-http-"
                + UUID.randomUUID()
                + "@example.invalid";

        AdminUser admin = new AdminUser();

        admin.setFullName("Reset Full HTTP IT");
        admin.setEmail(email);
        admin.setRole(AdminRole.SUPER_ADMIN);
        admin.setEnabled(true);
        admin.setPassword(
                encoder.encode(ORIGINAL)
        );

        adminId = admins.saveAndFlush(admin).getId();

        assertNotNull(adminId);
    }

    @AfterEach
    void cleanup() throws Exception {

        if (adminId != null) {

            verifyDatabase();

            jdbc.update(
                    """
                    DELETE FROM admin_password_reset_tokens
                    WHERE admin_user_id = ?
                    """,
                    adminId
            );

            admins.deleteById(adminId);
        }
    }

    private void verifyDatabase() throws Exception {

        try (Connection connection =
                datasource.getConnection()) {

            assertEquals(
                    DATABASE,
                    connection.getCatalog(),
                    "No se permite utilizar webempresarial_dev"
            );
        }
    }

    private MockHttpSession login(
            String password,
            long expectedVersion
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
                (MockHttpSession) result.getRequest()
                        .getSession(false);

        assertNotNull(session);

        assertEquals(
                expectedVersion,
                session.getAttribute(
                    AdminCredentialVersionFilter.SESSION_KEY
                )
        );

        return session;
    }

    private void assertLoginRejected(
            String password
    ) throws Exception {

        mvc.perform(
                post("/admin/login")
                    .with(csrf())
                    .param("username", email)
                    .param("password", password)
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
                "/admin/login?error"
        ));
    }

    /*
     * Utiliza el token CSRF que Thymeleaf genera
     * al renderizar el formulario, conservando
     * la misma sesión durante el POST.
     */
    private record BrowserForm(
            MockHttpSession session,
            String csrfToken
    ) {}

    private BrowserForm browserForm(
            String path,
            MockHttpSession existingSession
    ) throws Exception {

        MockHttpServletRequestBuilder request = get(path);

        if (existingSession != null) {
            request.session(existingSession);
        }

        MvcResult result = mvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        String html =
                result.getResponse().getContentAsString();

        Matcher matcher = Pattern.compile(
                "name=\"_csrf\"\\s+value=\"([^\"]+)\""
        ).matcher(html);

        assertTrue(
                matcher.find(),
                "El formulario debe incluir un token CSRF"
        );

        MockHttpSession session =
                (MockHttpSession) result.getRequest()
                        .getSession(false);

        assertNotNull(
                session,
                "El formulario debe conservar su sesión CSRF"
        );

        return new BrowserForm(
                session,
                matcher.group(1)
        );
    }

    private void requestRecoveryByHttp()
            throws Exception {

        BrowserForm form = browserForm(FORGOT, null);

        mvc.perform(
                post(FORGOT)
                    .session(form.session())
                    .param("_csrf", form.csrfToken())
                    .param("email", email)
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
                FORGOT + "?sent"
        ))
        .andExpect(header().string(
                "Cache-Control",
                containsString("no-store")
        ));
    }

    private String captureRecoveryToken()
            throws Exception {

        ArgumentCaptor<String> html =
                ArgumentCaptor.forClass(String.class);

        /*
         * Espera acotada: la solicitud se procesa
         * en el ejecutor y el correo se encola
         * después del commit.
         */
        verify(
                emailService,
                timeout(20_000).times(1)
        ).enviarCorreoHTML(
                eq(email),
                eq(
                    "Recupera tu contraseña de WebEmpresarial"
                ),
                html.capture()
        );

        Matcher matcher =
                TOKEN_URL.matcher(html.getValue());

        assertTrue(
                matcher.find(),
                "El correo debe incluir un enlace válido"
        );

        String raw = matcher.group(1);

        assertEquals(43, raw.length());

        return raw;
    }

    private MockHttpSession openResetLink(
            String rawToken
    ) throws Exception {

        MvcResult result = mvc.perform(
                get(RESET)
                    .param("token", rawToken)
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(RESET))
        .andExpect(header().string(
                "Referrer-Policy",
                "no-referrer"
        ))
        .andExpect(header().string(
                "Cache-Control",
                containsString("no-store")
        ))
        .andReturn();

        MockHttpSession session =
                (MockHttpSession) result.getRequest()
                        .getSession(false);

        assertNotNull(session);

        mvc.perform(
                get(RESET).session(session)
        )
        .andExpect(status().isOk())
        .andExpect(view().name(
                "admin/account/reset-password"
        ))
        .andExpect(content().string(
                not(containsString(rawToken))
        ));

        return session;
    }

    @Test
    void completeRecoveryThroughHttpRevokesSessionsAndToken()
            throws Exception {

        MockHttpSession originalSession =
                login(ORIGINAL, 0L);

        mvc.perform(
                get("/admin/account/password")
                    .session(originalSession)
        )
        .andExpect(status().isOk());

        requestRecoveryByHttp();

        String raw = captureRecoveryToken();

        AdminPasswordResetToken issued =
                tokens.findByTokenHash(
                        codec.hashToken(raw)
                ).orElseThrow();

        assertEquals(
                adminId,
                issued.getAdminUser().getId()
        );

        assertEquals(
                0L,
                issued.getCredentialVersionAtIssue()
        );

        MockHttpSession resetSession =
                openResetLink(raw);

        mvc.perform(
                post(RESET)
                    .session(resetSession)
                    .param(
                        "_csrf",
                        browserForm(
                            RESET,
                            resetSession
                        ).csrfToken()
                    )
                    .param("newPassword", RECOVERED)
                    .param("confirmPassword", RECOVERED)
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
                "/admin/login?passwordReset"
        ));

        assertTrue(resetSession.isInvalid());

        AdminUser updated =
                admins.findById(adminId).orElseThrow();

        assertEquals(
                1L,
                updated.getCredentialVersion()
        );

        assertTrue(
                encoder.matches(
                        RECOVERED,
                        updated.getPassword()
                )
        );

        /*
         * La sesión anterior deja de ser válida.
         */
        mvc.perform(
                get("/admin/account/password")
                    .session(originalSession)
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
                "/admin/login?sessionExpired"
        ));

        assertTrue(originalSession.isInvalid());

        assertLoginRejected(ORIGINAL);

        MockHttpSession freshSession =
                login(RECOVERED, 1L);

        mvc.perform(
                get("/admin/account/password")
                    .session(freshSession)
        )
        .andExpect(status().isOk());

        assertNotNull(
                tokens.findByTokenHash(
                        codec.hashToken(raw)
                ).orElseThrow().getConsumedAt()
        );

        /*
         * Volver a abrir el enlace consumido
         * no permite cambiar otra vez la clave.
         */
        MockHttpSession replaySession =
                openResetLink(raw);

        mvc.perform(
                post(RESET)
                    .session(replaySession)
                    .param(
                        "_csrf",
                        browserForm(
                            RESET,
                            replaySession
                        ).csrfToken()
                    )
                    .param("newPassword", REPLAY)
                    .param("confirmPassword", REPLAY)
        )
        .andExpect(status().isOk())
        .andExpect(model().attributeExists(
                "passwordError"
        ));

        AdminUser afterReplay =
                admins.findById(adminId).orElseThrow();

        assertEquals(
                1L,
                afterReplay.getCredentialVersion()
        );

        assertTrue(
                encoder.matches(
                        RECOVERED,
                        afterReplay.getPassword()
                )
        );
    }

    @Test
    void changingOwnPasswordRevokesExistingResetLink()
            throws Exception {

        MockHttpSession authenticatedSession =
                login(ORIGINAL, 0L);

        requestRecoveryByHttp();

        String oldRaw = captureRecoveryToken();

        /*
         * El administrador cambia su contraseña
         * desde el formulario autenticado.
         */
        mvc.perform(
                post("/admin/account/password")
                    .session(authenticatedSession)
                    .param(
                        "_csrf",
                        browserForm(
                            "/admin/account/password",
                            authenticatedSession
                        ).csrfToken()
                    )
                    .param(
                        "currentPassword",
                        ORIGINAL
                    )
                    .param(
                        "newPassword",
                        CHANGED
                    )
                    .param(
                        "confirmPassword",
                        CHANGED
                    )
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
                "/admin/login?passwordChanged"
        ));

        assertTrue(authenticatedSession.isInvalid());

        MockHttpSession staleResetSession =
                openResetLink(oldRaw);

        /*
         * Aunque el enlace tenga menos de
         * veinte minutos, su versión ya no coincide.
         */
        mvc.perform(
                post(RESET)
                    .session(staleResetSession)
                    .param(
                        "_csrf",
                        browserForm(
                            RESET,
                            staleResetSession
                        ).csrfToken()
                    )
                    .param("newPassword", REPLAY)
                    .param("confirmPassword", REPLAY)
        )
        .andExpect(status().isOk())
        .andExpect(model().attributeExists(
                "passwordError"
        ));

        AdminUser updated =
                admins.findById(adminId).orElseThrow();

        assertEquals(
                1L,
                updated.getCredentialVersion()
        );

        assertTrue(
                encoder.matches(
                        CHANGED,
                        updated.getPassword()
                )
        );

        assertFalse(
                encoder.matches(
                        REPLAY,
                        updated.getPassword()
                )
        );

        assertLoginRejected(ORIGINAL);

        login(CHANGED, 1L);
    }
}
