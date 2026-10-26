package com.webempresarial.store.controller.admin;

import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.service.AdminAccountInvitationResendService;
import com.webempresarial.store.service.PlatformOperationsService;
import com.webempresarial.store.service.ProvisioningService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlatformOperationsControllerTest {

    private PlatformOperationsService operationsService;

    private AdminAccountInvitationResendService
            invitationResendService;

    private ProvisioningService provisioningService;

    private PlatformOperationsController controller;

    @BeforeEach
    void setUp() {

        operationsService =
                mock(PlatformOperationsService.class);

        invitationResendService =
                mock(
                        AdminAccountInvitationResendService.class
                );

        provisioningService =
                mock(ProvisioningService.class);

        controller =
                new PlatformOperationsController(
                        operationsService,
                        invitationResendService,
                        provisioningService
                );
    }

    @Test
    void createOwnerMustDelegateAndReturnSuccessFlash() {

        AdminUser owner = new AdminUser();
        owner.setId(42L);

        when(
                provisioningService
                        .provisionOwnerForExistingStore(
                                4L,
                                "Stride Owner",
                                "owner@stride.test"
                        )
        ).thenReturn(owner);

        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        String view =
                controller.createOwner(
                        4L,
                        "Stride Owner",
                        "owner@stride.test",
                        redirect
                );

        assertThat(view)
                .isEqualTo(
                        "redirect:/admin/platform/operations"
                );

        assertThat(
                redirect
                        .getFlashAttributes()
                        .get("platformOperationSuccess")
        ).isEqualTo(
                "Propietario creado. "
                + "Invitación de activación generada. "
                + "Admin ID: 42"
        );

        verify(provisioningService)
                .provisionOwnerForExistingStore(
                        4L,
                        "Stride Owner",
                        "owner@stride.test"
                );
    }

    @Test
    void createOwnerMustExposeBusinessErrorAsFlash() {

        when(
                provisioningService
                        .provisionOwnerForExistingStore(
                                4L,
                                "Stride Owner",
                                "owner@stride.test"
                        )
        ).thenThrow(
                new IllegalStateException(
                        "La tienda ya tiene un propietario"
                )
        );

        RedirectAttributesModelMap redirect =
                new RedirectAttributesModelMap();

        String view =
                controller.createOwner(
                        4L,
                        "Stride Owner",
                        "owner@stride.test",
                        redirect
                );

        assertThat(view)
                .isEqualTo(
                        "redirect:/admin/platform/operations"
                );

        assertThat(
                redirect
                        .getFlashAttributes()
                        .get("platformOperationError")
        ).isEqualTo(
                "La tienda ya tiene un propietario"
        );
    }
}
