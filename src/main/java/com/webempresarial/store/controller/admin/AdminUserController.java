package com.webempresarial.store.controller.admin;

import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.AdminUserService;
import com.webempresarial.store.service.StoreContextService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
                storeContextService.getCurrentStore(
                        request
                );

        model.addAttribute(
                "store",
                store
        );

        model.addAttribute(
                "admins",
                adminUserService.listarPorTienda(
                        store
                )
        );

        return "admin/users/list";
    }

    @GetMapping("/nuevo")
    public String nuevo(
            HttpServletRequest request,
            Authentication authentication,
            Model model
    ) {

        Store store =
                storeContextService.getCurrentStore(
                        request
                );

        AdminRole actorRole =
                currentRole(authentication);

        model.addAttribute(
                "store",
                store
        );

        model.addAttribute(
                "adminUser",
                adminUserService.nuevoAdmin(
                        store,
                        actorRole
                )
        );

        model.addAttribute(
                "roles",
                adminUserService
                        .rolesPermitidosParaGestion(
                                actorRole
                        )
        );

        return "admin/users/form";
    }

    @PostMapping("/guardar")
    public String guardar(
            HttpServletRequest request,
            Authentication authentication,
            @ModelAttribute AdminUser adminUser
    ) {

        Store store =
                storeContextService.getCurrentStore(
                        request
                );

        adminUserService.guardarParaTienda(
                store,
                adminUser,
                currentRole(authentication)
        );

        return "redirect:/admin/users";
    }

    @PostMapping("/estado/{id}")
    public String cambiarEstado(
            HttpServletRequest request,
            Authentication authentication,
            @PathVariable Long id
    ) {

        Store store =
                storeContextService.getCurrentStore(
                        request
                );

        adminUserService.cambiarEstadoParaTienda(
                store,
                id,
                currentRole(authentication)
        );

        return "redirect:/admin/users";
    }

    private AdminRole currentRole(
            Authentication authentication
    ) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "No existe un administrador autenticado"
            );
        }

        /*
         * El orden es intencional.
         * Un usuario administrativo debe tener un único ROLE_*,
         * pero priorizamos siempre el de mayor privilegio.
         */
        AdminRole[] precedence = {
                AdminRole.SUPER_ADMIN,
                AdminRole.STORE_OWNER,
                AdminRole.STORE_ADMIN,
                AdminRole.STORE_STAFF,
                AdminRole.SALES
        };

        for (AdminRole role : precedence) {

            String authority =
                    "ROLE_" + role.name();

            boolean present =
                    authentication
                            .getAuthorities()
                            .stream()
                            .anyMatch(granted ->
                                    authority.equals(
                                            granted.getAuthority()
                                    )
                            );

            if (present) {
                return role;
            }
        }

        throw new IllegalStateException(
                "El usuario autenticado no tiene un rol administrativo válido"
        );
    }
}
