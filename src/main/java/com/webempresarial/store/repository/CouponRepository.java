package com.webempresarial.store.repository;

import com.webempresarial.store.model.Coupon;
import com.webempresarial.store.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponRepository
extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCodeIgnoreCaseAndStore(
            String code,
            Store store
    );

    List<Coupon> findByStore(Store store);

    boolean existsByCodeIgnoreCaseAndStore(
            String code,
            Store store
    );
}