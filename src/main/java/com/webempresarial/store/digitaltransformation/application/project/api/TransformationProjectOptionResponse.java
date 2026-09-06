package com.webempresarial.store.digitaltransformation.application.project.api;

import com.webempresarial.store.digitaltransformation.domain.project.TransformationProjectStatus;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProjectType;

import java.time.Instant;

public record TransformationProjectOptionResponse(

        Long id,

        String code,

        String name,

        String clientName,

        TransformationProjectType projectType,

        TransformationProjectStatus status,

        boolean sourceOfTruthLocked,

        Integer currentBlueprintVersion,

        Instant createdAt,

        Instant updatedAt

) {
}