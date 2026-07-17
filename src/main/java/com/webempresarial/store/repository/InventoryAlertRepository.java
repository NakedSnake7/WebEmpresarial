package com.webempresarial.store.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webempresarial.store.entity.InventoryAlert;
import com.webempresarial.store.model.InventoryAlertStatus;
import com.webempresarial.store.model.Store;

public interface InventoryAlertRepository
        extends JpaRepository<InventoryAlert, Long> {

    Optional<InventoryAlert> findByActiveKey(
            String activeKey
    );

    Optional<InventoryAlert> findByIdAndStore(
            Long id,
            Store store
    );

    long countByStoreAndStatusIn(
            Store store,
            Collection<InventoryAlertStatus> statuses
    );

    List<InventoryAlert>
    findByStoreAndStatusInOrderByLevelDescLastDetectedAtDesc(
            Store store,
            Collection<InventoryAlertStatus> statuses
    );

    List<InventoryAlert>
    findByStoreOrderByLastDetectedAtDesc(
            Store store
    );

    @Query("""
        SELECT a
        FROM InventoryAlert a
        JOIN FETCH a.producto p
        LEFT JOIN FETCH a.variante v
        WHERE a.store = :store
        AND a.status IN :statuses
        ORDER BY
            CASE a.level
                WHEN com.webempresarial.store.model.InventoryAlertLevel.OUT_OF_STOCK
                    THEN 1
                WHEN com.webempresarial.store.model.InventoryAlertLevel.CRITICAL
                    THEN 2
                ELSE 3
            END,
            a.lastDetectedAt DESC
    """)
    List<InventoryAlert> findActiveWithDetails(
            @Param("store") Store store,
            @Param("statuses")
            Collection<InventoryAlertStatus> statuses
    );
    @Query("""
    	    SELECT DISTINCT a
    	    FROM InventoryAlert a
    	    JOIN FETCH a.producto p
    	    LEFT JOIN FETCH a.variante v
    	    WHERE a.store = :store
    	    ORDER BY a.lastDetectedAt DESC
    	""")
    	List<InventoryAlert> findHistoryWithDetails(
    	        @Param("store") Store store
    	);	
}