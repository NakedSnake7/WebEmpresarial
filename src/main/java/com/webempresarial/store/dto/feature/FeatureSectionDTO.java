package com.webempresarial.store.dto.feature;

import java.util.List;

public record FeatureSectionDTO(
        String title,
        String icon,
        List<FeatureCardDTO> features
) {}