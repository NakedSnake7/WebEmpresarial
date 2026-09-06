package com.webempresarial.store.digitaltransformation.application.shared;

public class DuplicateStrategicArtifactException
        extends RuntimeException {

    public DuplicateStrategicArtifactException(
            Long projectId,
            String code
    ) {
        super(
                "Ya existe un artefacto estratégico con código " +
                code +
                " en el proyecto " +
                projectId
        );
    }
}