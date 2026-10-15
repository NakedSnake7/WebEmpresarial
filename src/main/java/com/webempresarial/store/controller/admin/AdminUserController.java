package com.webempresarial.store.controller.admin;

import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.Feature;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.service.AdminUserService;
import com.webempresarial.store.service.FeatureAccessService;
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
    private final FeatureAccessService featureAccessService;

    public AdminUserController(
            AdminUserService adminUserService,
            StoreContextService storeContextService,
            FeatureAccessService featureAccessService
    ) {
        this.adminUserService = adminUserService;
        this.storeContextService = storeContextService;
        this.featureAccessService = featureAccessService;
    }

    @GetMapping
    public String listar(
            HttpServletRequest request,
            Model model
    ) {

        Store store =
                currentStoreWithMultiUser(
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
                currentStoreWithMultiUser(
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
                currentStoreWithMultiUser(
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
                currentStoreWithMultiUser(
                        request
                );

        adminUserService.cambiarEstadoParaTienda(
                store,
                id,
                currentRole(authentication)
        );

        return "redirect:/admin/users";
    }

    /**
     * Resuelve el tenant actual y comprueba que su plan
     * incluya administración multiusuario.
     *
     * Si MULTI_USER está bloqueado, FeatureAccessService
     * lanza FeatureLockedException y el handler global
     * redirige a:
     *
     * /admin/upgrade?feature=MULTI_USER
     */
    private Store currentStoreWithMultiUser(
            HttpServletRequest request
    ) {

        Store store =
                storeContextService.getCurrentStore(
                        request
                );

        featureAccessService.requireFeature(
                store,
                Feature.MULTI_USER
        );

        return store;
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
