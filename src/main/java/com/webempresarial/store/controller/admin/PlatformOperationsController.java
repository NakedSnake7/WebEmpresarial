package com.webempresarial.store.controller.admin;

import java.io.IOException;

import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.service.AdminAccountInvitationResendService;
import com.webempresarial.store.service.PlatformOperationsService;
import com.webempresarial.store.service.ProvisioningService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PlatformOperationsController {

    private final PlatformOperationsService operationsService;

    private final AdminAccountInvitationResendService
            invitationResendService;

    private final ProvisioningService provisioningService;

    public PlatformOperationsController(
            PlatformOperationsService operationsService,
            AdminAccountInvitationResendService
                    invitationResendService,
            ProvisioningService provisioningService
    ) {
        this.operationsService = operationsService;
        this.invitationResendService =
                invitationResendService;
        this.provisioningService =
                provisioningService;
    }

    @GetMapping("/admin/platform/operations")
    public String operations(Model model) {

        model.addAttribute(
                "operations",
                operationsService.operations()
        );

        return "admin/platform/operations";
    }

    @PostMapping(
            "/admin/platform/operations/create-owner"
    )
    public String createOwner(
            @RequestParam Long storeId,
            @RequestParam String fullName,
            @RequestParam String email,
            RedirectAttributes redirectAttributes
    ) {

        try {

            AdminUser owner =
                    provisioningService
                            .provisionOwnerForExistingStore(
                                    storeId,
                                    fullName,
                                    email
                            );

            redirectAttributes.addFlashAttribute(
                    "platformOperationSuccess",
                    "Propietario creado. "
                    + "Invitación de activación generada. "
                    + "Admin ID: "
                    + owner.getId()
            );

        } catch (IllegalArgumentException
                | IllegalStateException ex) {

            redirectAttributes.addFlashAttribute(
                    "platformOperationError",
                    ex.getMessage()
            );
        }

        return "redirect:/admin/platform/operations";
    }

    @PostMapping(
            "/admin/platform/operations/resend-activation"
    )
    public String resendActivation(
            @RequestParam Long adminUserId,
            RedirectAttributes redirectAttributes
    ) {

        try {

            invitationResendService.resend(
                    adminUserId
            );

            redirectAttributes.addFlashAttribute(
                    "platformOperationSuccess",
                    "Invitación de activación reenviada."
            );

        } catch (IOException ex) {

            redirectAttributes.addFlashAttribute(
                    "platformOperationError",
                    "No fue posible enviar la invitación "
                    + "de activación."
            );

        } catch (IllegalArgumentException
                | IllegalStateException ex) {

            redirectAttributes.addFlashAttribute(
                    "platformOperationError",
                    ex.getMessage()
            );
        }

        return "redirect:/admin/platform/operations";
    }
}
