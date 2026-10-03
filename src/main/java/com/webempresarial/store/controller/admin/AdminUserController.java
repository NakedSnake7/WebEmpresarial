package com.webempresarial.store.controller.admin;

import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.AdminUserService;
import com.webempresarial.store.service.StoreContextService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final StoreContextService storeContextService;

    public AdminUserController(
            AdminUserService adminUserService,
            StoreContextService storeContextService
    ) {
        this.adminUserService = adminUserService;
        this.storeContextService = storeContextService;
    }

    @GetMapping
    public String listar(
            HttpServletRequest request,
            Model model
    ) {

        Store store =
                storeContextService.getCurrentStore(request);

        model.addAttribute(
                "store",
                store
        );

        model.addAttribute(
                "admins",
                adminUserService.listarPorTienda(store)
        );

        return "admin/users/list";
    }

    @GetMapping("/nuevo")
    public String nuevo(
            HttpServletRequest request,
            Model model
    ) {

        Store store =
                storeContextService.getCurrentStore(request);

        model.addAttribute(
                "store",
                store
        );

        model.addAttribute(
                "adminUser",
                adminUserService.nuevoAdmin(store)
        );

        model.addAttribute(
                "roles",
                List.of(
                        AdminRole.STORE_ADMIN,
                        AdminRole.STORE_STAFF
                )
        );

        return "admin/users/form";
    }

    @PostMapping("/guardar")
    public String guardar(
            HttpServletRequest request,
            @ModelAttribute AdminUser adminUser
    ) {

        Store store =
                storeContextService.getCurrentStore(request);

        adminUserService.guardarParaTienda(
                store,
                adminUser
        );

        return "redirect:/admin/users";
    }

    @PostMapping("/estado/{id}")
    public String cambiarEstado(
            HttpServletRequest request,
            @PathVariable Long id
    ) {

        Store store =
                storeContextService.getCurrentStore(request);

        adminUserService.cambiarEstadoParaTienda(
                store,
                id
        );

        return "redirect:/admin/users";
    }
}