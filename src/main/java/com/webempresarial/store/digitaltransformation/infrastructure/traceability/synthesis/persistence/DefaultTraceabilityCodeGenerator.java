package com.webempresarial.store.digitaltransformation.infrastructure.traceability.synthesis.persistence;

import com.webempresarial.store.digitaltransformation.application.traceability.spi.TraceabilityCodeGenerator;
import com.webempresarial.store.digitaltransformation.domain.traceability.TraceabilityNodeType;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

@Component
public class DefaultTraceabilityCodeGenerator
        implements TraceabilityCodeGenerator {

    private static final int MAX_CODE_LENGTH = 100;

    @Override
    public String generateForExternalReference(
            TraceabilityNodeType nodeType,
            String externalReference
    ) {
        Objects.requireNonNull(
                nodeType,
                "El tipo de nodo es obligatorio"
        );

        String prefix = resolvePrefix(nodeType);

        String normalizedReference =
                removeKnownPrefix(
                        normalizeReference(externalReference),
                        prefix
                );

        String code = "NODE-" + prefix + "-" + normalizedReference;

        if (code.length() > MAX_CODE_LENGTH) {
            code = code.substring(0, MAX_CODE_LENGTH);
        }

        return code;
    }

    private static String resolvePrefix(
            TraceabilityNodeType nodeType
    ) {
        return switch (nodeType) {
            case SOURCE_DOCUMENT -> "SRC";
            case SOURCE_SECTION -> "SEC";
            case SOURCE_EVIDENCE -> "EVD";

            case EXECUTIVE_INTENT -> "INT";
            case STRATEGIC_FINDING -> "FND";
            case BUSINESS_OBJECTIVE -> "OBJ";
            case BUSINESS_PROBLEM -> "PRB";
            case STRATEGIC_OPPORTUNITY -> "OPP";
            case EXISTING_STRENGTH -> "STR";
            case TRANSFORMATION_PRINCIPLE -> "PRN";
            case STRATEGIC_SYNTHESIS -> "SYN";

            case SCOPE_COMMITMENT -> "SCP";
            case REQUIREMENT -> "REQ";
            case ASSUMPTION -> "ASM";
            case RISK -> "RSK";
            case REQUIRED_DECISION -> "DEC";

            case AUDIENCE -> "AUD";
            case USER_JOURNEY -> "JRN";
            case SITEMAP_NODE -> "MAP";
            case PAGE_SPECIFICATION -> "PAG";
            case SECTION_SPECIFICATION -> "PSEC";
            case CONVERSION_GOAL -> "CNV";

            case DESIGN_PRINCIPLE -> "DPR";
            case DESIGN_TOKEN -> "TOK";
            case DESIGN_COMPONENT -> "CMP";
            case INTERACTION_PATTERN -> "INTX";

            case CONTENT_BRIEF -> "CBR";
            case COPY_ARTIFACT -> "CPY";
            case LOCALIZED_CONTENT -> "LOC";

            case SEO_REQUIREMENT -> "SEO";
            case SEO_ARTIFACT -> "SEOA";
            case SCHEMA_DEFINITION -> "SCH";

            case SOLUTION_COMPONENT -> "SOL";
            case DOMAIN_COMPONENT -> "DOM";
            case APPLICATION_COMPONENT -> "APP";
            case INTEGRATION_COMPONENT -> "INTG";
            case DATABASE_ARTIFACT -> "DB";
            case CODE_ARTIFACT -> "CODE";

            case TEST_CASE -> "TST";
            case QA_RESULT -> "QA";
            case RELEASE_ARTIFACT -> "REL";

            case OTHER -> "OTH";
        };
    }

    private static String removeKnownPrefix(
            String reference,
            String prefix
    ) {
        String expectedPrefix = prefix + "-";

        if (reference.startsWith(expectedPrefix)) {
            return reference.substring(expectedPrefix.length());
        }

        return reference;
    }
    
    
    private static String normalizeReference(
            String externalReference
    ) {
        if (externalReference == null
                || externalReference.isBlank()) {
            throw new IllegalArgumentException(
                    "La referencia externa es obligatoria"
            );
        }

        String normalized = Normalizer.normalize(
                externalReference.trim(),
                Normalizer.Form.NFD
        );

        normalized = normalized
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "");

        if (normalized.isBlank()) {
            throw new IllegalArgumentException(
                    "La referencia externa no produce un código válido"
            );
        }

        return normalized;
    }
}