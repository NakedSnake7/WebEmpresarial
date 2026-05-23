package com.webempresarial.store.repository;

import com.webempresarial.store.model.Marca;
import com.webempresarial.store.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarcaRepository extends JpaRepository<Marca, Long> {

    Optional<Marca> findByNombreIgnoreCaseAndStore(
            String nombre,
            Store store
    );

    Optional<Marca> findByIdAndStore(
            Long id,
            Store store
    );

    List<Marca> findByStoreOrderByNombreAsc(
            Store store
    );

    boolean existsByIdAndStore(
            Long id,
            Store store
    );
}