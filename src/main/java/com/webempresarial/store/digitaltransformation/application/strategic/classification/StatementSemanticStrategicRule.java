package com.webempresarial.store.digitaltransformation.application.strategic.classification;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class StatementSemanticStrategicRule
        implements StrategicClassificationRule {

    @Override
    public List<StrategicRuleMatch> evaluate(
            StrategicClassificationCandidate candidate
    ) {
        String text =
                candidate.statement()
                        .toLowerCase(Locale.ROOT);

        List<StrategicRuleMatch> matches =
                new ArrayList<>();

        detectProblem(text, matches);
        detectObjective(text, matches);
        detectOpportunity(text, matches);
        detectStrength(text, matches);
        detectPrinciple(text, matches);
        detectFinding(text, matches);

        return List.copyOf(matches);
    }

    private static void detectProblem(
            String text,
            List<StrategicRuleMatch> matches
    ) {
        if (containsAny(
                text,
                "problema",
                "brecha",
                "limita",
                "reduce",
                "debilita",
                "impide",
                "riesgo",
                "ineficiente",
                "insuficiente"
        )) {
            matches.add(
                    positive(
                            "SCR-SEM-PROBLEM",
                            StrategicArtifactType.BUSINESS_PROBLEM,
                            StrategicRuleStrength.MODERATE,
                            "La afirmación contiene señales de una condición negativa o limitante"
                    )
            );
        }
    }

    private static void detectObjective(
            String text,
            List<StrategicRuleMatch> matches
    ) {
        if (containsAny(
                text,
                "elevar",
                "mejorar",
                "aumentar",
                "alcanzar",
                "fortalecer",
                "convertir",
                "lograr",
                "incrementar",
                "reducir"
        )) {
            matches.add(
                    positive(
                            "SCR-SEM-OBJECTIVE",
                            StrategicArtifactType.BUSINESS_OBJECTIVE,
                            StrategicRuleStrength.MODERATE,
                            "La afirmación expresa un cambio o resultado deseado"
                    )
            );
        }
    }

    private static void detectOpportunity(
            String text,
            List<StrategicRuleMatch> matches
    ) {
        if (containsAny(
                text,
                "oportunidad",
                "aprovechar",
                "capitalizar",
                "expandir",
                "potencial",
                "posibilidad",
                "crecimiento"
        )) {
            matches.add(
                    positive(
                            "SCR-SEM-OPPORTUNITY",
                            StrategicArtifactType.STRATEGIC_OPPORTUNITY,
                            StrategicRuleStrength.MODERATE,
                            "La afirmación describe una posibilidad de capturar valor"
                    )
            );
        }
    }

    private static void detectStrength(
            String text,
            List<StrategicRuleMatch> matches
    ) {
        if (containsAny(
                text,
                "fortaleza",
                "activo",
                "autoridad",
                "reputación",
                "experiencia",
                "credibilidad",
                "diferenciador",
                "capacidad existente"
        )) {
            matches.add(
                    positive(
                            "SCR-SEM-STRENGTH",
                            StrategicArtifactType.EXISTING_STRENGTH,
                            StrategicRuleStrength.MODERATE,
                            "La afirmación describe un activo o capacidad existente"
                    )
            );
        }
    }

    private static void detectPrinciple(
            String text,
            List<StrategicRuleMatch> matches
    ) {
        if (containsAny(
                text,
                "debe guiar",
                "principio",
                "regla",
                "siempre",
                "centro de la experiencia",
                "por diseño",
                "debe orientar"
        )) {
            matches.add(
                    positive(
                            "SCR-SEM-PRINCIPLE",
                            StrategicArtifactType.TRANSFORMATION_PRINCIPLE,
                            StrategicRuleStrength.STRONG,
                            "La afirmación expresa una regla rectora transversal"
                    )
            );
        }
    }

    private static void detectFinding(
            String text,
            List<StrategicRuleMatch> matches
    ) {
        if (containsAny(
                text,
                "actualmente",
                "hoy",
                "se observa",
                "presenta",
                "existe",
                "la auditoría identifica",
                "se detecta"
        )) {
            matches.add(
                    positive(
                            "SCR-SEM-FINDING",
                            StrategicArtifactType.FINDING,
                            StrategicRuleStrength.MODERATE,
                            "La afirmación describe una condición observada"
                    )
            );
        }
    }

    private static StrategicRuleMatch positive(
            String code,
            StrategicArtifactType type,
            StrategicRuleStrength strength,
            String explanation
    ) {
        return new StrategicRuleMatch(
                code,
                StrategicClassificationRuleType.SEMANTIC_SIGNAL,
                type,
                strength,
                true,
                explanation
        );
    }

    private static boolean containsAny(
            String text,
            String... values
    ) {
        for (String value : values) {
            if (text.contains(value)) {
                return true;
            }
        }

        return false;
    }
}