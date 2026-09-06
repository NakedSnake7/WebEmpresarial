package com.webempresarial.store.digitaltransformation.application.strategic.classification;

import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactType;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicConfidence;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DefaultStrategicClassificationEngine
        implements StrategicClassificationEngine {

    private final List<StrategicClassificationRule> rules;

    private final StrategicConfidenceEvaluator
            confidenceEvaluator;

    public DefaultStrategicClassificationEngine(
            List<StrategicClassificationRule> rules,
            StrategicConfidenceEvaluator confidenceEvaluator
    ) {
        this.rules =
                List.copyOf(
                        Objects.requireNonNull(
                                rules,
                                "Las reglas son obligatorias"
                        )
                );

        this.confidenceEvaluator =
                Objects.requireNonNull(
                        confidenceEvaluator,
                        "El evaluador de confianza es obligatorio"
                );
    }

    @Override
    public StrategicClassificationResult classify(
            StrategicClassificationCandidate candidate
    ) {
        Objects.requireNonNull(
                candidate,
                "El candidato es obligatorio"
        );

        List<StrategicRuleMatch> matches =
                rules.stream()
                        .flatMap(rule ->
                                rule.evaluate(candidate)
                                        .stream()
                        )
                        .toList();

        if (matches.isEmpty()) {
            return uncertain(
                    "No existen señales suficientes para clasificar el candidato"
            );
        }

        Map<StrategicArtifactType, Integer> scores =
                calculateScores(matches);

        List<Map.Entry<StrategicArtifactType, Integer>>
                ranking =
                scores.entrySet()
                        .stream()
                        .sorted(
                                Map.Entry
                                        .<StrategicArtifactType, Integer>
                                        comparingByValue()
                                        .reversed()
                        )
                        .toList();
        
        if (ranking.size() > 1
                && Objects.equals(
                        ranking.get(0).getValue(),
                        ranking.get(1).getValue()
                )) {

            return new StrategicClassificationResult(
                    null,
                    StrategicConfidence.UNCERTAIN,
                    StrategicClassificationDecision.REVIEW_REQUIRED,
                    ranking.get(0).getValue(),
                    ranking.get(1).getValue(),
                    "Existe un empate entre dos o más clasificaciones estratégicas",
                    matches,
                    true,
                    false
            );
        }

        Map.Entry<StrategicArtifactType, Integer> winner =
                ranking.getFirst();

        int competingScore =
                ranking.size() > 1
                        ? ranking.get(1).getValue()
                        : 0;

        StrategicConfidence confidence =
                confidenceEvaluator.evaluate(
                        candidate,
                        winner.getValue(),
                        competingScore,
                        matches
                );

        StrategicClassificationDecision decision =
                resolveDecision(
                        confidence,
                        winner.getValue(),
                        competingScore
                );

        boolean review =
                decision
                != StrategicClassificationDecision.AUTO_ACCEPT;

        return new StrategicClassificationResult(
                winner.getKey(),
                confidence,
                decision,
                winner.getValue(),
                competingScore,
                buildRationale(
                        winner.getKey(),
                        winner.getValue(),
                        competingScore,
                        matches
                ),
                matches,
                review,
                decision == StrategicClassificationDecision.AUTO_ACCEPT
        );
    }

    private static Map<StrategicArtifactType, Integer>
    calculateScores(
            List<StrategicRuleMatch> matches
    ) {
        Map<StrategicArtifactType, Integer> scores =
                new EnumMap<>(
                        StrategicArtifactType.class
                );

        for (StrategicRuleMatch match : matches) {
            scores.merge(
                    match.suggestedType(),
                    match.signedWeight(),
                    Integer::sum
            );
        }

        return scores;
    }

    private static StrategicClassificationDecision
    resolveDecision(
            StrategicConfidence confidence,
            int winningScore,
            int competingScore
    ) {
        int margin =
                winningScore - competingScore;

        if ((confidence == StrategicConfidence.EXPLICIT
                || confidence
                == StrategicConfidence.STRONGLY_SUPPORTED)
                && winningScore >= 5
                && margin >= 3) {
            return StrategicClassificationDecision.AUTO_ACCEPT;
        }

        if (confidence == StrategicConfidence.UNCERTAIN
                || winningScore <= 0) {
            return StrategicClassificationDecision.REJECTED;
        }

        return StrategicClassificationDecision.REVIEW_REQUIRED;
    }

    private static String buildRationale(
            StrategicArtifactType winner,
            int winningScore,
            int competingScore,
            List<StrategicRuleMatch> matches
    ) {
        String supportingRules =
                matches.stream()
                        .filter(match ->
                                match.suggestedType() == winner
                        )
                        .map(StrategicRuleMatch::ruleCode)
                        .sorted()
                        .collect(
                                Collectors.joining(", ")
                        );

        return "Clasificación propuesta: " +
                winner +
                ". Score: " +
                winningScore +
                ". Score competidor: " +
                competingScore +
                ". Reglas principales: " +
                supportingRules;
    }

    private static StrategicClassificationResult uncertain(
            String rationale
    ) {
        return new StrategicClassificationResult(
                null,
                StrategicConfidence.UNCERTAIN,
                StrategicClassificationDecision.REJECTED,
                0,
                0,
                rationale,
                List.of(),
                true,
                false
        );
    }
}