package com.webempresarial.store.digitaltransformation.application.evidence;

import com.webempresarial.store.digitaltransformation.domain.evidence.EvidenceClassification;
import com.webempresarial.store.digitaltransformation.domain.evidence.EvidenceConfidence;
import com.webempresarial.store.digitaltransformation.domain.evidence.EvidenceExtractionOrigin;
import com.webempresarial.store.digitaltransformation.domain.evidence.EvidenceLocator;

public record ExtractSourceEvidenceCommand(
        Long storeId,
        Long projectId,
        Long sourceDocumentId,
        Long sourceSectionId,
        String evidenceCode,
        EvidenceClassification classification,
        EvidenceConfidence confidence,
        EvidenceExtractionOrigin extractionOrigin,
        String statement,
        String supportingExcerpt,
        String interpretation,
        EvidenceLocator locator
) {
}