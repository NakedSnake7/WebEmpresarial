package com.webempresarial.store.digitaltransformation.application.project;

import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProjectStatus;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProjectType;

public record TransformationProjectResult(
        Long id,
        Long storeId,
        String code,
        String name,
        String clientName,
        String clientWebsite,
        TransformationProjectType projectType,
        TransformationProjectStatus status,
        String executiveIntent,
        boolean sourceOfTruthLocked
) {

    public static TransformationProjectResult from(
            TransformationProject project
    ) {
        return new TransformationProjectResult(
                project.getId(),
                project.getStore().getId(),
                project.getCode(),
                project.getName(),
                project.getClientName(),
                project.getClientWebsite(),
                project.getProjectType(),
                project.getStatus(),
                project.getExecutiveIntent(),
                project.isSourceOfTruthLocked()
        );
    }
}