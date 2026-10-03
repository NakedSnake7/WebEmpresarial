package com.webempresarial.store.service;

import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.repository.AdminUserRepository;
import com.webempresarial.store.repository.StoreRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(
            AdminUserRepository adminUserRepository,
            StoreRepository storeRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.adminUserRepository = adminUserRepository;
        this.storeRepository = storeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AdminUser> listarPorTienda(Long storeId) {
        return adminUserRepository.findByStoreId(storeId);
    }

    public AdminUser nuevoAdmin(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Tienda no encontrada"));

        AdminUser adminUser = new AdminUser();
        adminUser.setStore(store);
        adminUser.setRole(AdminRole.STORE_ADMIN);
        adminUser.setEnabled(true);

        return adminUser;
    }

    @Transactional
    public void guardar(Long storeId, AdminUser adminUser) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Tienda no encontrada"));

        adminUser.setStore(store);

        if (adminUser.getRole() == null) {
            adminUser.setRole(AdminRole.STORE_ADMIN);
        }

        if (adminUser.getPassword() != null && !adminUser.getPassword().isBlank()) {
            adminUser.setPassword(passwordEncoder.encode(adminUser.getPassword()));
        } else {
            throw new RuntimeException("La contraseña es obligatoria");
        }

        adminUserRepository.save(adminUser);
    }

    @Transactional
    public void cambiarEstado(Long id) {
        AdminUser adminUser = adminUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin no encontrado"));

        adminUser.setEnabled(!adminUser.isEnabled());
    }
    public List<AdminUser> listarPorTienda(Store store) {

        if (store == null || store.getId() == null) {
            throw new IllegalArgumentException(
                    "La tienda es obligatoria"
            );
        }

        return adminUserRepository.findByStoreId(
                store.getId()
        );
    }

    public AdminUser nuevoAdmin(Store store) {

        if (store == null || store.getId() == null) {
            throw new IllegalArgumentException(
                    "La tienda es obligatoria"
            );
        }

        AdminUser adminUser = new AdminUser();

        adminUser.setStore(store);
        adminUser.setRole(AdminRole.STORE_STAFF);
        adminUser.setEnabled(true);

        return adminUser;
    }

    @Transactional
    public void guardarParaTienda(
            Store store,
            AdminUser adminUser
    ) {

        if (store == null || store.getId() == null) {
            throw new IllegalArgumentException(
                    "La tienda es obligatoria"
            );
        }

        if (adminUser == null) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio"
            );
        }

        /*
         * Un tenant jamás puede crear SUPER_ADMIN.
         */
        if (adminUser.getRole() == AdminRole.SUPER_ADMIN) {
            throw new IllegalArgumentException(
                    "Una tienda no puede crear SUPER_ADMIN"
            );
        }

        /*
         * Solo permitimos los roles pertenecientes
         * al tenant.
         */
        if (adminUser.getRole() != AdminRole.STORE_ADMIN
                && adminUser.getRole() != AdminRole.STORE_STAFF) {

            throw new IllegalArgumentException(
                    "Rol administrativo no permitido"
            );
        }

        /*
         * Nunca confiamos en store_id proveniente
         * del formulario.
         */
        adminUser.setStore(store);

        String email = adminUser.getEmail();

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "El email es obligatorio"
            );
        }

        email = email.trim().toLowerCase();

        adminUser.setEmail(email);

        if (adminUserRepository.existsByEmail(email)) {
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

        adminUserRepository.save(adminUser);
    }

    @Transactional
    public void cambiarEstadoParaTienda(
            Store store,
            Long adminUserId
    ) {

        if (store == null || store.getId() == null) {
            throw new IllegalArgumentException(
                    "La tienda es obligatoria"
            );
        }

        AdminUser adminUser =
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

        adminUser.setEnabled(
                !adminUser.isEnabled()
        );
    }
}