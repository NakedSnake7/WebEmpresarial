package com.webempresarial.store.controller.admin;

import java.io.IOException;

import com.webempresarial.store.service.AdminAccountInvitationResendService;
import com.webempresarial.store.service.PlatformOperationsService;

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

    public PlatformOperationsController(
            PlatformOperationsService operationsService,
            AdminAccountInvitationResendService
                    invitationResendService
    ) {
        this.operationsService = operationsService;
        this.invitationResendService =
                invitationResendService;
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
