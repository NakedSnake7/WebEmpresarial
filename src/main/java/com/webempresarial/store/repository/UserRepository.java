package com.webempresarial.store.repository;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByEmailAndStore(
            String email,
            Store store
    );

    boolean existsByEmailAndStore(
            String email,
            Store store
    );
}