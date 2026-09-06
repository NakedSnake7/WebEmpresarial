package com.webempresarial.store.digitaltransformation.application.strategic;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifact;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StrategicArtifactAccessService {

    private final StrategicArtifactRepository repository;

    public StrategicArtifactAccessService(
            StrategicArtifactRepository repository
    ) {
        this.repository = repository;
    }

    public StrategicArtifact requireArtifact(
            Long storeId,
            Long artifactId
    ) {
        validateId(
                storeId,
                "El storeId debe ser válido"
        );

        validateId(
                artifactId,
                "El artifactId debe ser válido"
        );

        return repository
                .findByIdAndProjectStoreId(
                        artifactId,
                        storeId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el artefacto estratégico " +
                                artifactId +
                                " para el store " +
                                storeId
                        )
                );
    }

    private static void validateId(
            Long id,
            String message
    ) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    message
            );
        }
    }
}