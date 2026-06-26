package com.webempresarial.store.dto.saas;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MrrSnapshotDTO(
        LocalDate date,
        BigDecimal mrr
) {}