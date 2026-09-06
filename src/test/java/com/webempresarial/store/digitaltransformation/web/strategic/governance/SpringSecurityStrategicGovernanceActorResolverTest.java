package com.webempresarial.store.digitaltransformation.web.strategic.governance;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisReviewerType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SpringSecurityStrategicGovernanceActorResolverTest {

    private SpringSecurityStrategicGovernanceActorResolver resolver;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        resolver =
                new SpringSecurityStrategicGovernanceActorResolver();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldResolveSuperAdminAsHumanConsultant() {
        authenticate(
                "superadmin@webempresarial.com",
                "ROLE_SUPER_ADMIN"
        );

        StrategicGovernanceActor actor =
                resolver.resolve();

        assertThat(actor.reviewer())
                .isEqualTo(
                        "superadmin@webempresarial.com"
                );

        assertThat(actor.reviewerType())
                .isEqualTo(
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT
                );
    }

    @Test
    void shouldResolveStoreAdminAsHumanConsultant() {
        authenticate(
                "admin@store.com",
                "ROLE_STORE_ADMIN"
        );

        StrategicGovernanceActor actor =
                resolver.resolve();

        assertThat(actor.reviewer())
                .isEqualTo(
                        "admin@store.com"
                );

        assertThat(actor.reviewerType())
                .isEqualTo(
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT
                );
    }

    @Test
    void shouldRejectStoreStaffForGovernance() {
        authenticate(
                "staff@store.com",
                "ROLE_STORE_STAFF"
        );

        assertThatThrownBy(
                resolver::resolve
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "no está autorizado"
                );
    }

    @Test
    void shouldRejectAuthenticatedUserWithoutGovernanceAuthority() {
        authenticate(
                "cliente@example.com",
                "ROLE_CLIENTE"
        );

        assertThatThrownBy(
                resolver::resolve
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "identidad válida"
                );
    }

    @Test
    void shouldRejectMissingAuthentication() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(
                resolver::resolve
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "usuario autenticado"
                );
    }

    @Test
    void shouldRejectAnonymousAuthentication() {
        AnonymousAuthenticationToken authentication =
                new AnonymousAuthenticationToken(
                        "anonymous-key",
                        "anonymousUser",
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_ANONYMOUS"
                                )
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        authentication
                );

        assertThatThrownBy(
                resolver::resolve
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "usuario autenticado"
                );
    }

    @Test
    void shouldPreferSuperAdminAuthorityWhenSeveralAuthoritiesExist() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "admin@example.com",
                        "ignored",
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_STORE_STAFF"
                                ),
                                new SimpleGrantedAuthority(
                                        "ROLE_SUPER_ADMIN"
                                )
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        authentication
                );

        StrategicGovernanceActor actor =
                resolver.resolve();

        assertThat(actor.reviewerType())
                .isEqualTo(
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT
                );
    }

    private static void authenticate(
            String username,
            String authority
    ) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
                        "ignored",
                        List.of(
                                new SimpleGrantedAuthority(
                                        authority
                                )
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        authentication
                );
    }
}