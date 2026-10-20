package com.webempresarial.store.service;

import com.webempresarial.store.model.AdminPermission;
import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.AdminUserRepository;
import com.webempresarial.store.repository.StoreRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminPermissionService adminPermissionService;

    public AdminUserService(
            AdminUserRepository adminUserRepository,
            StoreRepository storeRepository,
            PasswordEncoder passwordEncoder,
            AdminPermissionService adminPermissionService
    ) {
        this.adminUserRepository = adminUserRepository;
        this.storeRepository = storeRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminPermissionService = adminPermissionService;
    }

    /*
     * =========================================================
     * Administración de plataforma / legacy
     * =========================================================
     */

    public List<AdminUser> listarPorTienda(Long storeId) {
        return adminUserRepository.findByStoreId(storeId);
    }

    public AdminUser nuevoAdmin(Long storeId) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Tienda no encontrada"
                        )
                );

        AdminUser adminUser = new AdminUser();

        adminUser.setStore(store);
        adminUser.setRole(AdminRole.STORE_ADMIN);
        adminUser.setEnabled(true);

        return adminUser;
    }

    @Transactional
    public void guardar(
            Long storeId,
            AdminUser adminUser
    ) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Tienda no encontrada"
                        )
                );

        /*
         * Esta ruta de plataforma es únicamente para altas.
         * Nunca aceptamos un id enviado por el formulario.
         */
        if (adminUser.getId() != null) {
            throw new IllegalArgumentException(
                    "El alta de usuarios no admite un id existente"
            );
        }

        /*
         * SUPER_ADMIN es un rol global de plataforma y nunca
         * debe quedar asociado a una tienda.
         */
        if (adminUser.getRole()
                == AdminRole.SUPER_ADMIN) {

            throw new IllegalArgumentException(
                    "SUPER_ADMIN no puede asignarse a una tienda"
            );
        }

        adminUser.setStore(store);

        if (adminUser.getRole() == null) {
            adminUser.setRole(
                    AdminRole.STORE_ADMIN
            );
        }

        if (adminUser.getPassword() != null
                && !adminUser.getPassword().isBlank()) {

            adminUser.setPassword(
                    passwordEncoder.encode(
                            adminUser.getPassword()
                    )
            );

        } else {
            throw new RuntimeException(
                    "La contraseña es obligatoria"
            );
        }

        adminUserRepository.save(adminUser);
    }

    @Transactional
    public void cambiarEstado(
            Long storeId,
            Long id
    ) {

        AdminUser adminUser =
                adminUserRepository
                        .findByIdAndStoreId(
                                id,
                                storeId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Admin no encontrado en esta tienda"
                                )
                        );

        adminUser.setEnabled(
                !adminUser.isEnabled()
        );
    }

    /*
     * =========================================================
     * Administración de equipo dentro del tenant
     * =========================================================
     */

    public List<AdminUser> listarPorTienda(
            Store store
    ) {

        requireStore(store);

        return adminUserRepository.findByStoreId(
                store.getId()
        );
    }

    /**
     * Roles que el actor puede asignar desde /admin/users.
     *
     * STORE_OWNER:
     *   STORE_ADMIN, STORE_STAFF, SALES
     *
     * STORE_ADMIN:
     *   STORE_STAFF, SALES
     *
     * El STORE_OWNER nunca se crea desde el gestor de equipo.
     */
    public List<AdminRole> rolesPermitidosParaGestion(
            AdminRole actorRole
    ) {

        if (actorRole == AdminRole.SUPER_ADMIN
                || actorRole == AdminRole.STORE_OWNER) {

            return List.of(
                    AdminRole.STORE_ADMIN,
                    AdminRole.STORE_STAFF,
                    AdminRole.SALES
            );
        }

        if (actorRole == AdminRole.STORE_ADMIN) {

            return List.of(
                    AdminRole.STORE_STAFF,
                    AdminRole.SALES
            );
        }

        return List.of();
    }

    public AdminUser nuevoAdmin(
            Store store,
            AdminRole actorRole
    ) {

        requireStore(store);

        requirePermission(
                actorRole,
                AdminPermission.TEAM_INVITE
        );

        AdminUser adminUser = new AdminUser();

        adminUser.setStore(store);
        adminUser.setRole(
                AdminRole.STORE_STAFF
        );
        adminUser.setEnabled(true);

        return adminUser;
    }

    @Transactional
    public void guardarParaTienda(
            Store store,
            AdminUser adminUser,
            AdminRole actorRole
    ) {

        requireStore(store);

        if (adminUser == null) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio"
            );
        }

        requirePermission(
                actorRole,
                AdminPermission.TEAM_INVITE
        );

        /*
         * /admin/users/guardar es únicamente un alta.
         *
         * Nunca aceptamos un id proveniente del formulario,
         * porque podría utilizarse para intentar actualizar
         * otro usuario o incluso moverlo entre tenants.
         */
        if (adminUser.getId() != null) {
            throw new IllegalArgumentException(
                    "El alta de usuarios no admite un id existente"
            );
        }

        AdminRole requestedRole =
                adminUser.getRole();

        if (requestedRole == null) {
            throw new IllegalArgumentException(
                    "El rol es obligatorio"
            );
        }

        if (!rolesPermitidosParaGestion(
                actorRole
        ).contains(requestedRole)) {

            throw new AccessDeniedException(
                    "No puedes crear usuarios con el rol "
                            + requestedRole
            );
        }

        /*
         * Jamás confiamos en store_id del formulario.
         */
        adminUser.setStore(store);

        String fullName =
                adminUser.getFullName();

        if (fullName == null
                || fullName.isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre es obligatorio"
            );
        }

        adminUser.setFullName(
                fullName.trim()
        );

        String email =
                adminUser.getEmail();

        if (email == null
                || email.isBlank()) {

            throw new IllegalArgumentException(
                    "El email es obligatorio"
            );
        }

        email = email
                .trim()
                .toLowerCase();

        adminUser.setEmail(email);

        if (adminUserRepository.existsByEmail(
                email
        )) {

            throw new IllegalArgumentException(
                    "Ya existe un usuario con ese email"
            );
        }

        if (adminUser.getPassword() == null
                || adminUser.getPassword().isBlank()) {

            throw new IllegalArgumentException(
                    "La contraseña es obligatoria"
            );
        }

        adminUser.setPassword(
                passwordEncoder.encode(
                        adminUser.getPassword()
                )
        );

        adminUserRepository.save(
                adminUser
        );
    }

    @Transactional
    public void cambiarEstadoParaTienda(
            Store store,
            Long adminUserId,
            AdminRole actorRole
    ) {

        requireStore(store);

        requirePermission(
                actorRole,
                AdminPermission.TEAM_EDIT
        );

        AdminUser target =
                adminUserRepository
                        .findByIdAndStoreId(
                                adminUserId,
                                store.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Usuario no encontrado en esta tienda"
                                )
                        );

        /*
         * El propietario contractual nunca puede ser
         * desactivado desde el gestor normal de equipo.
         */
        if (target.getRole()
                == AdminRole.STORE_OWNER) {

            throw new AccessDeniedException(
                    "El propietario de la tienda no puede ser desactivado"
            );
        }

        /*
         * STORE_ADMIN solo puede administrar STAFF y SALES.
         *
         * No puede desactivar:
         * - otro STORE_ADMIN
         * - un STORE_OWNER
         */
        if (!rolesPermitidosParaGestion(
                actorRole
        ).contains(target.getRole())) {

            throw new AccessDeniedException(
                    "No puedes administrar usuarios con el rol "
                            + target.getRole()
            );
        }

        target.setEnabled(
                !target.isEnabled()
        );
    }

    private void requireStore(
            Store store
    ) {

        if (store == null
                || store.getId() == null) {

            throw new IllegalArgumentException(
                    "La tienda es obligatoria"
            );
        }
    }

    private void requirePermission(
            AdminRole actorRole,
            AdminPermission permission
    ) {

        if (!adminPermissionService
                .hasPermission(
                        actorRole,
                        permission
                )) {

            throw new AccessDeniedException(
                    "No tienes permiso: "
                            + permission.name()
            );
        }
    }
}
