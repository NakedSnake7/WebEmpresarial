package com.webempresarial.store.digitaltransformation.application.source;

import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceRole;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceType;

public record RegisterTransformationSourceCommand(
        Long storeId,
        Long projectId,
        TransformationSourceType sourceType,
        TransformationSourceRole sourceRole,
        String originalFilename,
        String displayName,
        String mimeType,
        String storageReference,
        String checksumSha256,
        int documentVersion,
        String languageCode,
        Integer pageCount
) {
}