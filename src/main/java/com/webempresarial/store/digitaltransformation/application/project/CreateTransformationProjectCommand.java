package com.webempresarial.store.digitaltransformation.application.project;

import com.webempresarial.store.digitaltransformation.domain.project.TransformationProjectType;

public record CreateTransformationProjectCommand(
        Long storeId,
        String code,
        String name,
        String clientName,
        String clientWebsite,
        TransformationProjectType projectType,
        String executiveIntent
) {
}