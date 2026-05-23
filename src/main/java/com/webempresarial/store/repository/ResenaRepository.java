package com.webempresarial.store.repository;

import com.webempresarial.store.entity.ResenaEntity;
import com.webempresarial.store.model.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResenaRepository
        extends JpaRepository<ResenaEntity, Long> {

    List<ResenaEntity> findByStoreOrderByEstrellasDesc(Store store);

    Page<ResenaEntity> findByStoreOrderByEstrellasDesc(
            Store store,
            Pageable pageable
    );

    Optional<ResenaEntity> findByIdAndStore(
            Long id,
            Store store
    );
}