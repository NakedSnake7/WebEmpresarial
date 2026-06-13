package com.webempresarial.store.repository;

import com.webempresarial.store.model.Store;
import com.webempresarial.store.model.StorePlan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findByDominioAndActivaTrue(String dominio);

    Optional<Store> findByThemeAndActivaTrue(String theme);

    boolean existsByDominio(String dominio);

    boolean existsByTheme(String theme);
    
    long countByActivaTrue();
    
    Optional<Store> findByDominio(String dominio);
    

    long countByActivaFalse();

    long countByPlan(StorePlan plan);

    long countByStripeConnectedTrue();
    
    @Query("""
    	    SELECT s
    	    FROM Store s
    	    LEFT JOIN FETCH s.subscription
    	    ORDER BY s.id DESC
    	""")
    	List<Store> findAllWithSubscription();
    
}