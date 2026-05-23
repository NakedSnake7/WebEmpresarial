package com.webempresarial.store.controller.admin;

import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.service.AdminUserService;
import com.webempresarial.store.service.StoreAdminService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/stores/{storeId}/admins")
public class AdminStoreUserController {

    private final AdminUserService adminUserService;
    private final StoreAdminService storeAdminService;

    public AdminStoreUserController(
            AdminUserService adminUserService,
            StoreAdminService storeAdminService
    ) {
        this.adminUserService = adminUserService;
        this.storeAdminService = storeAdminService;
    }

    @GetMapping
    public String listar(
            @PathVariable Long storeId,
            Model model
    ) {

        model.addAttribute(
                "store",
                storeAdminService.buscarPorId(storeId)
        );

        model.addAttribute(
                "admins",
                adminUserService.listarPorTienda(storeId)
        );

        return "admin/stores/admins/list";
    }

    @GetMapping("/nuevo")
    public String nuevo(
            @PathVariable Long storeId,
            Model model
    ) {

        model.addAttribute(
                "store",
                storeAdminService.buscarPorId(storeId)
        );

        model.addAttribute(
                "adminUser",
                adminUserService.nuevoAdmin(storeId)
        );

        model.addAttribute(
                "roles",
                AdminRole.values()
        );

        return "admin/stores/admins/form";
    }

    @PostMapping("/guardar")
    public String guardar(
            @PathVariable Long storeId,
            @ModelAttribute AdminUser adminUser
    ) {

        adminUserService.guardar(storeId, adminUser);

        return "redirect:/admin/stores/" + storeId + "/admins";
    }

    @PostMapping("/estado/{id}")
    public String cambiarEstado(
            @PathVariable Long storeId,
            @PathVariable Long id
    ) {

        adminUserService.cambiarEstado(id);

        return "redirect:/admin/stores/" + storeId + "/admins";
    }
}