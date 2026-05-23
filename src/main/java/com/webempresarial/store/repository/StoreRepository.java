package com.webempresarial.store.repository;

import com.webempresarial.store.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findByDominioAndActivaTrue(String dominio);

    Optional<Store> findByThemeAndActivaTrue(String theme);

    boolean existsByDominio(String dominio);

    boolean existsByTheme(String theme);
}