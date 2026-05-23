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
}