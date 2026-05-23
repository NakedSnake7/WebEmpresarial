package com.webempresarial.store.repository;

import com.webempresarial.store.model.MensajePendiente;
import com.webempresarial.store.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajePendienteRepository
        extends JpaRepository<MensajePendiente, Long> {

    List<MensajePendiente>
    findTop10ByStoreAndEnviadoFalseOrderByCreadoEnAsc(
            Store store
    );
}