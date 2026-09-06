package com.webempresarial.store.digitaltransformation.application.strategic.synthesis;

import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicChain;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DeterministicStrategicThesisGenerator
        implements StrategicThesisGenerator {

    @Override
    public String generate(
            StrategicChain chain
    ) {
        Objects.requireNonNull(
                chain,
                "La cadena estratégica es obligatoria"
        );

        if (!chain.isComplete()) {
            throw new IllegalArgumentException(
                    "Solo una cadena estratégica completa puede generar una tesis"
            );
        }

        String problem =
                chain.getBusinessProblem()
                        .getStatement();

        String objective =
                chain.getBusinessObjective()
                        .getStatement();

        String opportunity =
                chain.getStrategicOpportunity()
                        .getStatement();

        /*
         * No introduce ningún hecho nuevo.
         * Únicamente conecta tres afirmaciones ya aprobadas.
         */
        return "Ante el problema «" +
                problem +
                "», la transformación debe perseguir el objetivo «" +
                objective +
                "» mediante la oportunidad estratégica «" +
                opportunity +
                "».";
    }
}