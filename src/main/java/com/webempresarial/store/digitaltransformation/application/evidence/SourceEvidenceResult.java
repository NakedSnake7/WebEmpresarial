package com.webempresarial.store.digitaltransformation.application.evidence;

import com.webempresarial.store.digitaltransformation.domain.evidence.EvidenceClassification;
import com.webempresarial.store.digitaltransformation.domain.evidence.EvidenceConfidence;
import com.webempresarial.store.digitaltransformation.domain.evidence.EvidenceExtractionOrigin;
import com.webempresarial.store.digitaltransformation.domain.evidence.EvidenceLocator;
import com.webempresarial.store.digitaltransformation.domain.evidence.EvidenceStatus;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;

public record SourceEvidenceResult(
        Long id,
        Long projectId,
        Long sourceDocumentId,
        Long sourceSectionId,
        String evidenceCode,
        EvidenceClassification classification,
        EvidenceConfidence confidence,
        EvidenceExtractionOrigin extractionOrigin,
        EvidenceStatus status,
        String statement,
        String supportingExcerpt,
        String interpretation,
        EvidenceLocator locator,
        boolean requiresHumanReview,
        boolean canGenerateRequirements,
        String verifiedBy
) {

    public static SourceEvidenceResult from(
            SourceEvidence evidence
    ) {
        return new SourceEvidenceResult(
                evidence.getId(),
                evidence.getProject().getId(),
                evidence.getSourceDocument().getId(),
                evidence.getSourceSection() != null
                        ? evidence.getSourceSection().getId()
                        : null,
                evidence.getEvidenceCode(),
                evidence.getClassification(),
                evidence.getConfidence(),
                evidence.getExtractionOrigin(),
                evidence.getStatus(),
                evidence.getStatement(),
                evidence.getSupportingExcerpt(),
                evidence.getInterpretation(),
                evidence.getLocator(),
                evidence.isRequiresHumanReview(),
                evidence.canGenerateRequirements(),
                evidence.getVerifiedBy()
        );
    }
}