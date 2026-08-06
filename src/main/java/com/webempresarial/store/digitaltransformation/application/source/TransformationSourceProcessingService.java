package com.webempresarial.store.digitaltransformation.application.source;

import com.webempresarial.store.digitaltransformation.application.project.TransformationProjectAccessService;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceDocument;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TransformationSourceProcessingService {

    private final TransformationProjectAccessService accessService;
    private final TransformationSourceDocumentRepository sourceRepository;

    public TransformationSourceProcessingService(
            TransformationProjectAccessService accessService,
            TransformationSourceDocumentRepository sourceRepository
    ) {
        this.accessService = accessService;
        this.sourceRepository = sourceRepository;
    }

    public TransformationSourceResult markParsed(
            Long storeId,
            Long sourceId
    ) {
        TransformationSourceDocument document =
                accessService.requireSource(storeId, sourceId);

        document.getProject().ensureSourcesCanBeModified();
        document.markParsed();

        return TransformationSourceResult.from(
                sourceRepository.save(document)
        );
    }

    public TransformationSourceResult markAnalyzed(
            Long storeId,
            Long sourceId
    ) {
        TransformationSourceDocument document =
                accessService.requireSource(storeId, sourceId);

        document.getProject().ensureSourcesCanBeModified();
        document.markAnalyzed();

        return TransformationSourceResult.from(
                sourceRepository.save(document)
        );
    }

    public TransformationSourceResult verify(
            Long storeId,
            Long sourceId
    ) {
        TransformationSourceDocument document =
                accessService.requireSource(storeId, sourceId);

        document.getProject().ensureSourcesCanBeModified();
        document.verify();

        return TransformationSourceResult.from(
                sourceRepository.save(document)
        );
    }

    public TransformationSourceResult reject(
            Long storeId,
            Long sourceId
    ) {
        TransformationSourceDocument document =
                accessService.requireSource(storeId, sourceId);

        document.getProject().ensureSourcesCanBeModified();
        document.reject();

        return TransformationSourceResult.from(
                sourceRepository.save(document)
        );
    }
}