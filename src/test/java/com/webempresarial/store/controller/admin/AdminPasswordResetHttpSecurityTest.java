package com.webempresarial.store.controller.admin;

import com.webempresarial.store.config.AdminAuthenticationSuccessHandler;
import com.webempresarial.store.config.SecurityConfig;
import com.webempresarial.store.repository.AdminUserRepository;
import com.webempresarial.store.service.AdminPasswordResetRateLimiter;
import com.webempresarial.store.service.AdminPasswordResetRequestDispatcher;
import com.webempresarial.store.service.AdminPasswordResetService;
import com.webempresarial.store.service.AdminUserDetailsService;
import com.webempresarial.store.service.AuthUserDetailsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = AdminPasswordResetController.class,
    useDefaultFilters = false
)
@Import({
    SecurityConfig.class,
    AdminPasswordResetController.class
})
class AdminPasswordResetHttpSecurityTest {

    private static final String FORGOT =
            "/admin/password/forgot";

    private static final String RESET =
            "/admin/password/reset";

    private static final String RAW =
            "A".repeat(43);

    private static final String NEXT =
            "NuevaClaveSegura#2026";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AdminPasswordResetService resetService;

    @MockitoBean
    private AdminPasswordResetRequestDispatcher dispatcher;

    @MockitoBean
    private AdminPasswordResetRateLimiter limiter;

    @MockitoBean
    private AdminUserDetailsService adminUserDetailsService;

    @MockitoBean
    private AuthUserDetailsService authUserDetailsService;

    @MockitoBean
    private AdminAuthenticationSuccessHandler successHandler;

    @MockitoBean
    private AdminUserRepository admins;

    @BeforeEach
    void allowTestOrigin() {
        when(limiter.allowRecoveryRequest(anyString()))
                .thenReturn(true);

        when(limiter.allowRecoveryCompletion(anyString()))
                .thenReturn(true);
    }

    private MockHttpSession openResetSession()
            throws Exception {

        var result = mvc.perform(
                get(RESET)
                    .param("token", RAW)
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(RESET))
        .andExpect(header().string(
                "Referrer-Policy",
                "no-referrer"
        ))
        .andReturn();

        MockHttpSession session =
                (MockHttpSession) result
                        .getRequest()
                        .getSession(false);

        assertNotNull(session);

        return session;
    }

    @Test
    void anonymousCanOpenForgotForm()
            throws Exception {

        mvc.perform(get(FORGOT))
            .andExpect(status().isOk())
            .andExpect(view().name(
                    "admin/account/forgot-password"
            ))
            .andExpect(header().string(
                    "Cache-Control",
                    containsString("no-store")
            ))
            .andExpect(header().string(
                    "Referrer-Policy",
                    "no-referrer"
            ));
    }

    @Test
    void forgotPostWithoutCsrfIsForbidden()
            throws Exception {

        mvc.perform(
                post(FORGOT)
                    .param(
                        "email",
                        "admin@example.invalid"
                    )
        )
        .andExpect(status().isForbidden());

        verifyNoInteractions(dispatcher);
    }

    @Test
    void forgotPostAlwaysGivesGenericRedirect()
            throws Exception {

        mvc.perform(
                post(FORGOT)
                    .with(csrf())
                    .param(
                        "email",
                        "unknown@example.invalid"
                    )
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
                FORGOT + "?sent"
        ));

        verify(dispatcher).dispatch(
                "unknown@example.invalid"
        );
    }

    @Test
    void rateLimitedForgotPostStillLooksGeneric()
            throws Exception {

        when(limiter.allowRecoveryRequest(anyString()))
                .thenReturn(false);

        mvc.perform(
                post(FORGOT)
                    .with(csrf())
                    .param(
                        "email",
                        "admin@example.invalid"
                    )
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
                FORGOT + "?sent"
        ));

        verifyNoInteractions(dispatcher);
    }

    @Test
    void resetTokenMovesToSessionAndNotHtml()
            throws Exception {

        MockHttpSession session = openResetSession();

        mvc.perform(
                get(RESET).session(session)
        )
        .andExpect(status().isOk())
        .andExpect(view().name(
                "admin/account/reset-password"
        ))
        .andExpect(content().string(
                not(containsString(RAW))
        ))
        .andExpect(header().string(
                "Cache-Control",
                containsString("no-store")
        ));
    }

    @Test
    void resetPostWithoutCsrfIsForbidden()
            throws Exception {

        MockHttpSession session = openResetSession();

        mvc.perform(
                post(RESET)
                    .session(session)
                    .param("newPassword", NEXT)
                    .param("confirmPassword", NEXT)
        )
        .andExpect(status().isForbidden());

        verifyNoInteractions(resetService);
    }

    @Test
    void passwordMismatchDoesNotCallService()
            throws Exception {

        MockHttpSession session = openResetSession();

        mvc.perform(
                post(RESET)
                    .session(session)
                    .with(csrf())
                    .param("newPassword", NEXT)
                    .param(
                        "confirmPassword",
                        "OtraClaveSegura#2026"
                    )
        )
        .andExpect(status().isOk())
        .andExpect(view().name(
                "admin/account/reset-password"
        ));

        verifyNoInteractions(resetService);
    }

    @Test
    void successfulRedemptionClosesCurrentSession()
            throws Exception {

        MockHttpSession session = openResetSession();

        mvc.perform(
                post(RESET)
                    .session(session)
                    .with(csrf())
                    .param("newPassword", NEXT)
                    .param("confirmPassword", NEXT)
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(
                "/admin/login?passwordReset"
        ));

        verify(resetService).completeRecovery(
                RAW,
                NEXT
        );

        assertTrue(session.isInvalid());
    }

    @Test
    void invalidTokenErrorDoesNotRevealInternalDetails()
            throws Exception {

        MockHttpSession session = openResetSession();

        doThrow(new IllegalArgumentException(
                "INTERNAL_TOKEN_ERROR"
        ))
        .when(resetService)
        .completeRecovery(RAW, NEXT);

        mvc.perform(
                post(RESET)
                    .session(session)
                    .with(csrf())
                    .param("newPassword", NEXT)
                    .param("confirmPassword", NEXT)
        )
        .andExpect(status().isOk())
        .andExpect(content().string(
                not(containsString(
                        "INTERNAL_TOKEN_ERROR"
                ))
        ));
    }

    @Test
    void redemptionRateLimitPreventsServiceCall()
            throws Exception {

        MockHttpSession session = openResetSession();

        when(limiter.allowRecoveryCompletion(anyString()))
                .thenReturn(false);

        mvc.perform(
                post(RESET)
                    .session(session)
                    .with(csrf())
                    .param("newPassword", NEXT)
                    .param("confirmPassword", NEXT)
        )
        .andExpect(status().isTooManyRequests());

        verifyNoInteractions(resetService);
    }

    @Test
    void missingTokenCannotChangePassword()
            throws Exception {

        mvc.perform(
                post(RESET)
                    .with(csrf())
                    .param("newPassword", NEXT)
                    .param("confirmPassword", NEXT)
        )
        .andExpect(status().isOk())
        .andExpect(view().name(
                "admin/account/reset-password"
        ));

        verifyNoInteractions(resetService);
    }
}
