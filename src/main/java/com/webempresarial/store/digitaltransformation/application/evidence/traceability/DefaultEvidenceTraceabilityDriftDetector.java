package com.webempresarial.store.digitaltransformation.application.evidence.traceability;

import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNode;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DefaultEvidenceTraceabilityDriftDetector
        implements EvidenceTraceabilityDriftDetector {

    @Override
    public EvidenceTraceabilityDrift detect(
            SourceEvidence evidence,
            TraceabilityNode node
    ) {
        Objects.requireNonNull(evidence);
        Objects.requireNonNull(node);

        boolean titleChanged =
                !normalize(evidence.getStatement())
                        .equals(normalize(node.getTitle()));

        boolean classificationChanged =
                node.getDescription() == null
                || !node.getDescription().contains(
                        "Clasificación: " +
                        evidence.getClassification()
                );

        if (!titleChanged && !classificationChanged) {
            return EvidenceTraceabilityDrift.none();
        }

        StringBuilder reason =
                new StringBuilder(
                        "Se detectó desalineación entre evidencia y nodo"
                );

        if (titleChanged) {
            reason.append(
                    "; cambió la afirmación principal"
            );
        }

        if (classificationChanged) {
            reason.append(
                    "; cambió o no coincide la clasificación"
            );
        }

        return new EvidenceTraceabilityDrift(
                true,
                titleChanged,
                classificationChanged,
                reason.toString()
        );
    }

    private static String normalize(
            String value
    ) {
        if (value == null) {
            return "";
        }

        return value
                .trim()
                .replaceAll("\\s+", " ");
    }
}