package com.webempresarial.store.repository;

import com.webempresarial.store.entity.SaasMetricSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SaasMetricSnapshotRepository
        extends JpaRepository<SaasMetricSnapshot, Long> {

    List<SaasMetricSnapshot> findBySnapshotDateAfterOrderBySnapshotDateAsc(
            LocalDate date
    );
}