package com.webempresarial.store.digitaltransformation.application.source;

import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceDocument;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceRole;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceStatus;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceType;

public record TransformationSourceResult(
        Long id,
        Long projectId,
        TransformationSourceType sourceType,
        TransformationSourceRole sourceRole,
        TransformationSourceStatus status,
        String originalFilename,
        String displayName,
        String checksumSha256,
        int documentVersion,
        String languageCode,
        boolean authoritative,
        Integer pageCount
) {

    public static TransformationSourceResult from(
            TransformationSourceDocument document
    ) {
        return new TransformationSourceResult(
                document.getId(),
                document.getProject().getId(),
                document.getSourceType(),
                document.getSourceRole(),
                document.getStatus(),
                document.getOriginalFilename(),
                document.getDisplayName(),
                document.getChecksumSha256(),
                document.getDocumentVersion(),
                document.getLanguageCode(),
                document.isAuthoritative(),
                document.getPageCount()
        );
    }
}