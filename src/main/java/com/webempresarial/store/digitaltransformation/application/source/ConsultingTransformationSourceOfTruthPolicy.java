package com.webempresarial.store.digitaltransformation.application.source;

import com.webempresarial.store.digitaltransformation.application.shared.IncompleteSourceOfTruthException;
import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceDocument;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceStatus;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ConsultingTransformationSourceOfTruthPolicy
        implements SourceOfTruthPolicy {

    private static final Set<TransformationSourceType>
            REQUIRED_SOURCE_TYPES = EnumSet.of(
            TransformationSourceType.DIGITAL_EXCELLENCE_AUDIT,
            TransformationSourceType.DIGITAL_TRANSFORMATION_PROPOSAL
    );

    @Override
    public void validate(
            TransformationProject project,
            List<TransformationSourceDocument> sources
    ) {
        if (project == null) {
            throw new IllegalArgumentException(
                    "El proyecto es obligatorio"
            );
        }

        if (sources == null || sources.isEmpty()) {
            throw new IncompleteSourceOfTruthException(
                    "El proyecto no contiene fuentes de verdad"
            );
        }

        List<TransformationSourceDocument> unverified =
                sources.stream()
                        .filter(source ->
                                source.getStatus()
                                        != TransformationSourceStatus.VERIFIED
                        )
                        .toList();

        if (!unverified.isEmpty()) {
            throw new IncompleteSourceOfTruthException(
                    "Todas las fuentes autoritativas deben estar verificadas"
            );
        }

        Set<TransformationSourceType> registeredTypes =
                sources.stream()
                        .map(TransformationSourceDocument::getSourceType)
                        .collect(Collectors.toSet());

        Set<TransformationSourceType> missingTypes =
                EnumSet.copyOf(REQUIRED_SOURCE_TYPES);

        missingTypes.removeAll(registeredTypes);

        if (!missingTypes.isEmpty()) {
            throw new IncompleteSourceOfTruthException(
                    "Faltan fuentes obligatorias: " + missingTypes
            );
        }
    }
}