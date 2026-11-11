package com.webempresarial.store.controller.admin;

import com.webempresarial.store.config.AdminAuthenticationSuccessHandler;
import com.webempresarial.store.config.SecurityConfig;
import com.webempresarial.store.config.AdminCredentialVersionFilter;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.repository.AdminUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.mock.web.MockHttpSession;
import java.util.Optional;
import com.webempresarial.store.service.AdminPasswordChangeService;
import com.webempresarial.store.service.AdminUserDetailsService;
import com.webempresarial.store.service.AuthUserDetailsService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = AdminPasswordChangeController.class,
    useDefaultFilters = false
)
@Import({
    SecurityConfig.class,
    AdminPasswordChangeController.class
})
class AdminPasswordChangeHttpSecurityTest {

    private static final String PATH =
            "/admin/account/password";

    private static final String EMAIL =
            "admin@example.com";

    private static final String CURRENT =
            "ClaveActual2026!";

    private static final String NEXT =
            "NuevaClaveSegura2026!";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AdminPasswordChangeService passwordService;

    @MockitoBean
    private AdminUserDetailsService adminUserDetailsService;

    @MockitoBean
    private AuthUserDetailsService authUserDetailsService;

    @MockitoBean
    private AdminAuthenticationSuccessHandler successHandler;

    @MockitoBean
    private AdminUserRepository adminUserRepository;

    private AdminUser admin;

    @BeforeEach
    void configureCredentialVersion() {
        admin = new AdminUser();
        admin.setEmail(EMAIL);
        admin.setEnabled(true);

        when(adminUserRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(admin));
    }

    private MockHttpSession versionedSession() {
        MockHttpSession session = new MockHttpSession();

        session.setAttribute(
                AdminCredentialVersionFilter.SESSION_KEY,
                0L
        );

        return session;
    }

    @Test
    void anonymousCannotOpenPasswordForm() throws Exception {
        mvc.perform(get(PATH))
                .andExpect(status().is3xxRedirection());

        verifyNoInteractions(passwordService);
    }

    @Test
    void superAdminCanOpenPasswordForm() throws Exception {
        mvc.perform(
                get(PATH)
                    .with(user(EMAIL).roles("SUPER_ADMIN"))
                    .session(versionedSession())
        )
        .andExpect(status().isOk())
        .andExpect(view().name(
                "admin/account/change-password"
        ));
    }

    @Test
    void storeStaffCanOpenPasswordForm() throws Exception {
        mvc.perform(
                get(PATH)
                    .with(user(EMAIL).roles("STORE_STAFF"))
                    .session(versionedSession())
        )
        .andExpect(status().isOk());
    }

    @Test
    void salesCanOpenPasswordForm() throws Exception {
        mvc.perform(
                get(PATH)
                    .with(user(EMAIL).roles("SALES"))
                    .session(versionedSession())
        )
        .andExpect(status().isOk());
    }

    @Test
    void customerRoleCannotOpenAdminPasswordForm()
            throws Exception {

        mvc.perform(
                get(PATH)
                    .with(user(EMAIL).roles("CUSTOMER"))
        )
        .andExpect(status().isForbidden());

        verifyNoInteractions(passwordService);
    }

    @Test
    void adminPostWithoutCsrfIsRejected()
            throws Exception {

        mvc.perform(
                post(PATH)
                    .with(user(EMAIL).roles("SUPER_ADMIN"))
                    .session(versionedSession())
                    .param("currentPassword", CURRENT)
                    .param("newPassword", NEXT)
                    .param("confirmPassword", NEXT)
        )
        .andExpect(status().isForbidden());

        verifyNoInteractions(passwordService);
    }

    @Test
    void adminCanChangePasswordWithCsrf()
            throws Exception {

        mvc.perform(
                post(PATH)
                    .with(user(EMAIL).roles("STORE_OWNER"))
                    .session(versionedSession())
                    .with(csrf())
                    .param("currentPassword", CURRENT)
                    .param("newPassword", NEXT)
                    .param("confirmPassword", NEXT)
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
                "/admin/login?passwordChanged"
        ));

        verify(passwordService).changeOwnPassword(
                EMAIL,
                CURRENT,
                NEXT
        );
    }

    @Test
    void passwordConfirmationMismatchDoesNotCallService()
            throws Exception {

        mvc.perform(
                post(PATH)
                    .with(user(EMAIL).roles("STORE_ADMIN"))
                    .session(versionedSession())
                    .with(csrf())
                    .param("currentPassword", CURRENT)
                    .param("newPassword", NEXT)
                    .param(
                            "confirmPassword",
                            "OtraClaveSegura2026!"
                    )
        )
        .andExpect(status().isOk())
        .andExpect(model().attributeExists(
                "passwordError"
        ));

        verifyNoInteractions(passwordService);
    }

    @Test
    void customerCannotSubmitPasswordChange()
            throws Exception {

        mvc.perform(
                post(PATH)
                    .with(user(EMAIL).roles("CUSTOMER"))
                    .with(csrf())
                    .param("currentPassword", CURRENT)
                    .param("newPassword", NEXT)
                    .param("confirmPassword", NEXT)
        )
        .andExpect(status().isForbidden());

        verifyNoInteractions(passwordService);
    }

    @Test
    void unknownAccountSubrouteRemainsDenied()
            throws Exception {

        mvc.perform(
                get(PATH + "/other")
                    .with(user(EMAIL).roles("SUPER_ADMIN"))
                    .session(versionedSession())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void oldSessionIsRevokedAfterCredentialRotation()
            throws Exception {

        admin.rotateCredentialVersion();

        MockHttpSession oldSession = versionedSession();

        mvc.perform(
                get(PATH)
                    .with(user(EMAIL).roles("STORE_ADMIN"))
                    .session(oldSession)
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
                "/admin/login?sessionExpired"
        ));

        org.junit.jupiter.api.Assertions.assertTrue(
                oldSession.isInvalid()
        );

        verifyNoInteractions(passwordService);
    }

    @Test
    void disabledAdminSessionIsRevoked()
            throws Exception {

        admin.setEnabled(false);

        mvc.perform(
                get(PATH)
                    .with(user(EMAIL).roles("SUPER_ADMIN"))
                    .session(versionedSession())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
                "/admin/login?sessionExpired"
        ));

        verifyNoInteractions(passwordService);
    }

    @Test
    void missingCredentialVersionRevokesSession()
            throws Exception {

        mvc.perform(
                get(PATH)
                    .with(user(EMAIL).roles("STORE_ADMIN"))
                    .session(new MockHttpSession())
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
                "/admin/login?sessionExpired"
        ));

        verifyNoInteractions(passwordService);
    }

    @Test
    void expiredAdminApiSessionReturnsUnauthorized()
            throws Exception {

        admin.rotateCredentialVersion();

        mvc.perform(
                get("/api/admin/saas")
                    .with(user(EMAIL).roles("SUPER_ADMIN"))
                    .session(versionedSession())
        )
        .andExpect(status().isUnauthorized());
    }
}
