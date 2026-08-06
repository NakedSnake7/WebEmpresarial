package com.webempresarial.store.digitaltransformation.application.source;

import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceDocument;

import java.util.List;

public interface SourceOfTruthPolicy {

    void validate(
            TransformationProject project,
            List<TransformationSourceDocument> sources
    );
}