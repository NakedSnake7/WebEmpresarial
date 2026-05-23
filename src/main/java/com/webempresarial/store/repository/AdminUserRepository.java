package com.webempresarial.store.repository;

import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    Optional<AdminUser> findByEmail(String email);

    boolean existsByEmail(String email);

    List<AdminUser> findByStoreId(Long storeId);

    List<AdminUser> findByRole(AdminRole role);
}