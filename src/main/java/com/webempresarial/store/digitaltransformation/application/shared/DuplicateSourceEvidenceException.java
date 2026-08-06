package com.webempresarial.store.digitaltransformation.application.shared;

public class DuplicateSourceEvidenceException
        extends RuntimeException {

    public DuplicateSourceEvidenceException(
            Long projectId,
            String evidenceCode
    ) {
        super(
                "Ya existe una evidencia con código " +
                evidenceCode +
                " en el proyecto " +
                projectId
        );
    }
}