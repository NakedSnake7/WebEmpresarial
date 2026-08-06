package com.webempresarial.store.digitaltransformation.application.source;

import com.webempresarial.store.digitaltransformation.application.project.TransformationProjectAccessService;
import com.webempresarial.store.digitaltransformation.application.project.TransformationProjectResult;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProjectRepository;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceDocument;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LockTransformationSourceOfTruthService {

    private final TransformationProjectAccessService accessService;
    private final TransformationProjectRepository projectRepository;
    private final TransformationSourceDocumentRepository sourceRepository;
    private final SourceOfTruthPolicy sourceOfTruthPolicy;

    public LockTransformationSourceOfTruthService(
            TransformationProjectAccessService accessService,
            TransformationProjectRepository projectRepository,
            TransformationSourceDocumentRepository sourceRepository,
            SourceOfTruthPolicy sourceOfTruthPolicy
    ) {
        this.accessService = accessService;
        this.projectRepository = projectRepository;
        this.sourceRepository = sourceRepository;
        this.sourceOfTruthPolicy = sourceOfTruthPolicy;
    }

    public TransformationProjectResult lock(
            Long storeId,
            Long projectId
    ) {
        TransformationProject project =
                accessService.requireProject(storeId, projectId);

        project.ensureSourcesCanBeModified();

        List<TransformationSourceDocument> sources =
                sourceRepository
                        .findAllByProjectIdAndAuthoritativeTrueOrderByRegisteredAtAsc(
                                projectId
                        );

        sourceOfTruthPolicy.validate(project, sources);

        project.markSourcesIngested();
        project.lockSourceOfTruth();

        TransformationProject saved =
                projectRepository.save(project);

        return TransformationProjectResult.from(saved);
    }
}