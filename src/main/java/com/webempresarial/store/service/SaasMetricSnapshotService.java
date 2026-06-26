package com.webempresarial.store.service;

import com.webempresarial.store.dto.saas.MrrSnapshotDTO;
import com.webempresarial.store.repository.SaasMetricSnapshotRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SaasMetricSnapshotService {

    private final SaasMetricSnapshotRepository snapshotRepository;

    public SaasMetricSnapshotService(SaasMetricSnapshotRepository snapshotRepository) {
        this.snapshotRepository = snapshotRepository;
    }

    public List<MrrSnapshotDTO> getMrrLast30Days() {
        return snapshotRepository
                .findBySnapshotDateAfterOrderBySnapshotDateAsc(LocalDate.now().minusDays(30))
                .stream()
                .map(s -> new MrrSnapshotDTO(
                        s.getSnapshotDate(),
                        s.getMrr()
                ))
                .toList();
    }
}