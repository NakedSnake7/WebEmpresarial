package com.webempresarial.store.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.webempresarial.store.entity.Subscription;
import com.webempresarial.store.model.SubscriptionStatus;

public interface SubscriptionRepository
        extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByStoreId(Long storeId);

    Optional<Subscription> findByStripeCustomerId(String stripeCustomerId);

    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    long countByStatus(SubscriptionStatus status);

    @Query("""
        SELECT COALESCE(SUM(s.monthlyAmount), 0)
        FROM Subscription s
        WHERE s.status = :status
    """)
    BigDecimal sumMonthlyAmountByStatus(
            @Param("status") SubscriptionStatus status
    );
    
    @Query("""
    	    SELECT s
    	    FROM Subscription s
    	    JOIN FETCH s.store
    	    ORDER BY s.createdAt DESC
    	""")
    	List<Subscription> findAllWithStore();
    
    List<Subscription> findByStatusAndCurrentPeriodEndBefore(
            SubscriptionStatus status,
            LocalDateTime now
    );
    
    
}