package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.application.traceability.spi.TraceabilityNodeRegistrar;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesis;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNode;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNodeRepository;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNodeType;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityOrigin;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

@Component
public class StrategicSynthesisTraceabilityRegistrar {

    private static final String EXTERNAL_ENTITY_TYPE =
            "StrategicSynthesis";

    private final TraceabilityNodeRepository
            nodeRepository;

    private final TraceabilityNodeRegistrar
            nodeRegistrar;

    public StrategicSynthesisTraceabilityRegistrar(
            TraceabilityNodeRepository nodeRepository,
            TraceabilityNodeRegistrar nodeRegistrar
    ) {
        this.nodeRepository =
                Objects.requireNonNull(
                        nodeRepository,
                        "TraceabilityNodeRepository es obligatorio"
                );

        this.nodeRegistrar =
                Objects.requireNonNull(
                        nodeRegistrar,
                        "TraceabilityNodeRegistrar es obligatorio"
                );
    }

    public TraceabilityNode register(
            StrategicSynthesis synthesis
    ) {
        Objects.requireNonNull(
                synthesis,
                "La síntesis estratégica es obligatoria"
        );

        String externalReference =
                externalReference(
                        synthesis
                );

        TraceabilityNode node =
                nodeRepository
                        .findByProjectIdAndNodeTypeAndExternalReference(
                                synthesis.getProject().getId(),
                                TraceabilityNodeType.STRATEGIC_SYNTHESIS,
                                externalReference
                        )
                        .orElseGet(() ->
                                createNode(
                                        synthesis,
                                        externalReference
                                )
                        );

        validateRegisteredNode(
                synthesis,
                node
        );

        return node;
    }

    private TraceabilityNode createNode(
            StrategicSynthesis synthesis,
            String externalReference
    ) {
        String nodeCode =
                nodeCode(
                        externalReference
                );

        return nodeRegistrar.register(
                synthesis.getProject(),
                nodeCode,
                TraceabilityNodeType.STRATEGIC_SYNTHESIS,
                resolveOrigin(
                        synthesis
                ),
                synthesis.getStrategicThesis(),
                buildDescription(
                        synthesis
                ),
                externalReference,
                EXTERNAL_ENTITY_TYPE,
                requiresReview(
                        synthesis
                )
        );
    }

    private static TraceabilityOrigin resolveOrigin(
            StrategicSynthesis synthesis
    ) {
        return switch (synthesis.getOrigin()) {
            case DETERMINISTIC ->
                    TraceabilityOrigin.SYSTEM_GENERATED;

            case AI_ASSISTED ->
                    TraceabilityOrigin.SYSTEM_GENERATED;

            case HUMAN_AUTHORED ->
                    TraceabilityOrigin.MANUAL;
        };
    }

    private static boolean requiresReview(
            StrategicSynthesis synthesis
    ) {
        return synthesis.getStatus()
                == com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisStatus.REQUIRES_REVIEW
                || synthesis.getOrigin()
                == com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisOrigin.AI_ASSISTED;
    }

    private static String buildDescription(
            StrategicSynthesis synthesis
    ) {
        return "Finding: " +
                synthesis.getFindingStatement() +
                "\nBusiness problem: " +
                synthesis.getBusinessProblemStatement() +
                "\nBusiness objective: " +
                synthesis.getBusinessObjectiveStatement() +
                "\nStrategic opportunity: " +
                synthesis.getStrategicOpportunityStatement();
    }

    private static String externalReference(
            StrategicSynthesis synthesis
    ) {
        String material =
                synthesis.getProject().getId() +
                "|" +
                String.join(
                        "|",
                        synthesis.getSourceArtifactCodes()
                ) +
                "|" +
                synthesis.getStrategicThesis() +
                "|" +
                synthesis.getOrigin();

        return "SYNTHESIS:" +
                sha256(
                        material
                );
    }

    private static String nodeCode(
            String externalReference
    ) {
        String hash =
                externalReference.substring(
                        externalReference.indexOf(':') + 1
                );

        return "SYN-" +
                hash.substring(
                        0,
                        20
                ).toUpperCase();
    }

    private static String sha256(
            String value
    ) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            return HexFormat.of()
                    .formatHex(
                            digest.digest(
                                    value.getBytes(
                                            StandardCharsets.UTF_8
                                    )
                            )
                    );

        } catch (NoSuchAlgorithmException exception) {

            throw new IllegalStateException(
                    "SHA-256 no está disponible",
                    exception
            );
        }
    }

    private static void validateRegisteredNode(
            StrategicSynthesis synthesis,
            TraceabilityNode node
    ) {
        if (node == null) {
            throw new IllegalStateException(
                    "TraceabilityNodeRegistrar devolvió un nodo nulo"
            );
        }

        node.ensureBelongsToProject(
                synthesis.getProject()
        );

        if (node.getNodeType()
                != TraceabilityNodeType.STRATEGIC_SYNTHESIS) {

            throw new IllegalStateException(
                    "El nodo registrado debe ser de tipo STRATEGIC_SYNTHESIS"
            );
        }
    }
}