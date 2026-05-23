package com.webempresarial.store.repository;

import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductoVarianteRepository
        extends JpaRepository<ProductoVariante, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT v
        FROM ProductoVariante v
        WHERE v.id = :id
        AND v.producto.store = :store
    """)
    Optional<ProductoVariante> findByIdForUpdate(
            @Param("id") Long id,
            @Param("store") Store store
    );

    @Query("""
        SELECT v
        FROM ProductoVariante v
        WHERE v.id = :id
        AND v.producto.store = :store
    """)
    Optional<ProductoVariante> findByIdAndStore(
            @Param("id") Long id,
            @Param("store") Store store
    );

    @Modifying
    @Query("""
        DELETE FROM ProductoVariante v
        WHERE v.producto.id = :productoId
        AND v.producto.store = :store
    """)
    void deleteByProductoId(
            @Param("productoId") Long productoId,
            @Param("store") Store store
    );
}