package com.webempresarial.store.repository;

import com.webempresarial.store.model.AdminRole;
import com.webempresarial.store.model.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    Optional<AdminUser> findByEmail(String email);

    @EntityGraph(attributePaths = "store")
    Optional<AdminUser> findWithStoreByEmail(String email);

    boolean existsByEmail(String email);

    List<AdminUser> findByStoreId(Long storeId);

    List<AdminUser> findByRole(AdminRole role);

    Optional<AdminUser> findByIdAndStoreId(
            Long id,
            Long storeId

    );

    boolean existsByEmailAndIdNot(
            String email,
            Long id
    );

}
