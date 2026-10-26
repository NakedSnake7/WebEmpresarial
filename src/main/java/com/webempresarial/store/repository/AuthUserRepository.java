package com.webempresarial.store.repository;

import com.webempresarial.store.model.AuthUser;
import com.webempresarial.store.model.Store;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUserRepository
        extends JpaRepository<AuthUser, Long> {

    Optional<AuthUser> findByEmailAndCliente_Store(
            String email,
            Store store
    );

    boolean existsByEmailAndCliente_Store(
            String email,
            Store store
    );
}
