package com.webempresarial.store.digitaltransformation.application.strategic.api;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicChainGapType;

public record StrategicChainGapResponse(

        StrategicChainGapType type,

        String description

) {
}