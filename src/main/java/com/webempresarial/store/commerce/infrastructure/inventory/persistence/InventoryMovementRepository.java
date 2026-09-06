package com.webempresarial.store.commerce.infrastructure.inventory.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webempresarial.store.dto.inventory.InventoryTopMovementDTO;
import com.webempresarial.store.commerce.domain.inventory.InventoryMovement;
import com.webempresarial.store.commerce.domain.inventory.InventoryMovementType;
import com.webempresarial.store.model.Store;

public interface InventoryMovementRepository
        extends JpaRepository<InventoryMovement, Long> {

    List<InventoryMovement>
    findByStoreOrderByCreatedAtDesc(
            Store store
    );

    List<InventoryMovement>
    findByProductoIdAndStoreOrderByCreatedAtDesc(
            Long productoId,
            Store store
    );

    List<InventoryMovement>
    findByVarianteIdAndStoreOrderByCreatedAtDesc(
            Long varianteId,
            Store store
    );

    List<InventoryMovement>
    findByOrderIdAndStoreOrderByCreatedAtAsc(
            Long orderId,
            Store store
    );
    
    @Query("""
    	    SELECT COALESCE(SUM(m.quantity), 0)
    	    FROM InventoryMovement m
    	    WHERE m.store = :store
    	    AND m.type IN :types
    	    AND m.createdAt >= :from
    	    AND m.createdAt < :to
    	""")
    	long sumQuantityByTypesAndPeriod(
    	        @Param("store") Store store,
    	        @Param("types") List<InventoryMovementType> types,
    	        @Param("from") LocalDateTime from,
    	        @Param("to") LocalDateTime to
    	);
    
    @Query("""
    	    SELECT new com.webempresarial.store.dto.inventory.InventoryTopMovementDTO(
    	        p.id,
    	        p.productName,
    	        SUM(m.quantity)
    	    )
    	    FROM InventoryMovement m
    	    JOIN m.producto p
    	    WHERE m.store = :store
    	    AND m.type IN :types
    	    AND m.createdAt >= :from
    	    GROUP BY p.id, p.productName
    	    ORDER BY SUM(m.quantity) DESC
    	""")
    	List<InventoryTopMovementDTO> findTopOutgoingProducts(
    	        @Param("store") Store store,
    	        @Param("types") List<InventoryMovementType> types,
    	        @Param("from") LocalDateTime from,
    	        Pageable pageable
    	);

    boolean existsByOrderId(Long orderId);
    
    @Query("""
    	    SELECT m
    	    FROM InventoryMovement m
    	    JOIN FETCH m.producto p
    	    LEFT JOIN FETCH m.variante v
    	    LEFT JOIN FETCH m.order o
    	    WHERE m.store = :store
    	    AND (:type IS NULL OR m.type = :type)
    	    AND (:productId IS NULL OR p.id = :productId)
    	    AND (:variantId IS NULL OR v.id = :variantId)
    	    AND (:orderId IS NULL OR o.id = :orderId)
    	    AND (:from IS NULL OR m.createdAt >= :from)
    	    AND (:to IS NULL OR m.createdAt <= :to)
    	    ORDER BY m.createdAt DESC
    	""")
    	List<InventoryMovement> findFiltered(
    	        @Param("store") Store store,
    	        @Param("type") InventoryMovementType type,
    	        @Param("productId") Long productId,
    	        @Param("variantId") Long variantId,
    	        @Param("orderId") Long orderId,
    	        @Param("from") LocalDateTime from,
    	        @Param("to") LocalDateTime to
    	);
    List<InventoryMovement>
    findTop8ByStoreOrderByCreatedAtDesc(
            Store store
    );
    
    List<InventoryMovement>
    findTop50ByProductoIdAndStoreOrderByCreatedAtDesc(
            Long productoId,
            Store store
    );
    @Query("""
    	    SELECT COUNT(m)
    	    FROM InventoryMovement m
    	    WHERE m.store = :store
    	    AND m.createdAt >= :from
    	    AND m.createdAt < :to
    	""")
    	long countMovementsByPeriod(
    	        @Param("store") Store store,
    	        @Param("from") LocalDateTime from,
    	        @Param("to") LocalDateTime to
    	);
    
    @Query("""
    	    SELECT COALESCE(SUM(m.quantity), 0)
    	    FROM InventoryMovement m
    	    WHERE m.store = :store
    	    AND m.type = com.webempresarial.store.commerce.domain.inventory.InventoryMovementType.SALE
    	    AND m.createdAt >= :from
    	""")
    	long sumSalesUnitsSince(
    	        @Param("store") Store store,
    	        @Param("from") LocalDateTime from
    	);
    @Query("""
    	    SELECT new com.webempresarial.store.dto.inventory.InventoryTopMovementDTO(
    	        p.id,
    	        p.productName,
    	        COALESCE(SUM(
    	            CASE
    	                WHEN m.type = com.webempresarial.store.commerce.domain.inventory.InventoryMovementType.SALE
    	                THEN m.quantity
    	                ELSE 0
    	            END
    	        ), 0)
    	    )
    	    FROM Producto p
    	    LEFT JOIN InventoryMovement m
    	        ON m.producto = p
    	        AND m.createdAt >= :from
    	    WHERE p.store = :store
    	    GROUP BY p.id, p.productName
    	    ORDER BY COALESCE(SUM(
    	        CASE
    	            WHEN m.type = com.webempresarial.store.commerce.domain.inventory.InventoryMovementType.SALE
    	            THEN m.quantity
    	            ELSE 0
    	        END
    	    ), 0) ASC
    	""")
    	List<InventoryTopMovementDTO> findSlowMovingProducts(
    	        @Param("store") Store store,
    	        @Param("from") LocalDateTime from,
    	        Pageable pageable
    	);
}