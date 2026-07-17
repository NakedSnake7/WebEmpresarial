package com.webempresarial.store.repository;

import com.webempresarial.store.dto.inventory.VariantStockProjection;
import com.webempresarial.store.model.ProductoVariante;
import com.webempresarial.store.model.Store;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductoVarianteRepository
        extends JpaRepository<ProductoVariante, Long> {
	
	
	@Query("""
		    SELECT new com.webempresarial.store.dto.inventory.VariantStockProjection(
		        p.id,
		        p.productName,
		        v.id,
		        v.stock,
		        COALESCE(v.precio, p.price)
		    )
		    FROM ProductoVariante v
		    JOIN v.producto p
		    WHERE p.store = :store
		    AND v.stock <= :threshold
		    ORDER BY v.stock ASC, p.productName ASC, v.id ASC
		""")
		List<VariantStockProjection> findLowStockVariants(
		        @Param("store") Store store,
		        @Param("threshold") int threshold
		);

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
    
    @Query("""
    	    SELECT COUNT(v)
    	    FROM ProductoVariante v
    	    WHERE v.producto.store = :store
    	    AND v.stock > 0
    	    AND v.stock <= :threshold
    	""")
    	long countLowStockVariants(
    	        @Param("store") Store store,
    	        @Param("threshold") int threshold
    	);
    
    @Query("""
    	    SELECT COALESCE(
    	        SUM(
    	            COALESCE(v.precio, v.producto.price)
    	            * v.stock
    	        ),
    	        0
    	    )
    	    FROM ProductoVariante v
    	    WHERE v.producto.store = :store
    	""")
    	BigDecimal calculateVariantInventoryValue(
    	        @Param("store") Store store
    	);
    
    @Query("""
    	    SELECT COALESCE(SUM(v.stock), 0)
    	    FROM ProductoVariante v
    	    WHERE v.producto.store = :store
    	""")
    	long sumVariantStock(
    	        @Param("store") Store store
    	);
}