package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicChain;

import java.util.Optional;

public interface StrategicChainResolver {

    Optional<StrategicChain> resolve(
            Long projectId
    );
}