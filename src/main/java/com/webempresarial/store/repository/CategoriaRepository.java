package com.webempresarial.store.repository;

import com.webempresarial.store.model.Categoria;
import com.webempresarial.store.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository
extends JpaRepository<Categoria, Long> {

Optional<Categoria> findByNombreIgnoreCaseAndStore(
    String nombre,
    Store store
);

Optional<Categoria> findByIdAndStore(
    Long id,
    Store store
);

List<Categoria> findByStoreOrderByNombreAsc(
    Store store
);

boolean existsByIdAndStore(
    Long id,
    Store store
);

boolean existsByNombreIgnoreCaseAndStore(
    String nombre,
    Store store
);
}