package com.webempresarial.store.digitaltransformation.application.strategic.derivation;

import com.webempresarial.store.digitaltransformation.application.strategic.StrategicArtifactCodeGenerator;
import com.webempresarial.store.digitaltransformation.application.strategic.StrategicArtifactTypeDescriptor;
import com.webempresarial.store.digitaltransformation.application.strategic.classification.StrategicClassificationResult;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class StrategicArtifactRegistrar {

    private final StrategicArtifactRepository artifactRepository;
    private final StrategicArtifactCodeGenerator codeGenerator;

    public StrategicArtifactRegistrar(
            StrategicArtifactRepository artifactRepository,
            StrategicArtifactCodeGenerator codeGenerator
    ) {
        this.artifactRepository =
                Objects.requireNonNull(
                        artifactRepository,
                        "StrategicArtifactRepository es obligatorio"
                );

        this.codeGenerator =
                Objects.requireNonNull(
                        codeGenerator,
                        "StrategicArtifactCodeGenerator es obligatorio"
                );
    }

    public StrategicArtifact register(
            SourceEvidence evidence,
            StrategicClassificationResult classification
    ) {
        Objects.requireNonNull(
                evidence,
                "La evidencia es obligatoria"
        );

        Objects.requireNonNull(
                classification,
                "La clasificación es obligatoria"
        );

        StrategicArtifactType type =
                Objects.requireNonNull(
                        classification.proposedType(),
                        "El tipo estratégico es obligatorio"
                );

        return artifactRepository
                .findBySourceEvidenceIdAndArtifactType(
                        evidence.getId(),
                        type
                )
                .orElseGet(() ->
                        create(
                                evidence,
                                classification
                        )
                );
    }

    private StrategicArtifact create(
            SourceEvidence evidence,
            StrategicClassificationResult classification
    ) {
        long sequence =
                artifactRepository
                        .countByProjectIdAndArtifactType(
                                evidence.getProject().getId(),
                                classification.proposedType()
                        ) + 1;

        String code =
                codeGenerator.generate(
                        StrategicArtifactTypeDescriptor.of(
                                classification.proposedType()
                        ),
                        sequence
                );

        StrategicArtifact artifact =
                StrategicArtifact.deriveFromEvidence(
                        evidence.getProject(),
                        evidence,
                        code,
                        classification.proposedType(),
                        classification.confidence(),
                        StrategicArtifactOrigin.EVIDENCE_DERIVATION,
                        evidence.getStatement(),
                        evidence.getInterpretation(),
                        null
                );

        return artifactRepository.save(
                artifact
        );
    }
}