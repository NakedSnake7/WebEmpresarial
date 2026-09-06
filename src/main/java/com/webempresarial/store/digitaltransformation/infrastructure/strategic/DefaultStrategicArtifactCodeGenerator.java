package com.webempresarial.store.digitaltransformation.infrastructure.strategic;

import com.webempresarial.store.digitaltransformation.application.strategic.StrategicArtifactCodeGenerator;
import com.webempresarial.store.digitaltransformation.application.strategic.StrategicArtifactTypeDescriptor;
import org.springframework.stereotype.Component;

@Component
public class DefaultStrategicArtifactCodeGenerator
        implements StrategicArtifactCodeGenerator {

    @Override
    public String generate(
            StrategicArtifactTypeDescriptor type,
            long sequence
    ) {
        if (type == null) {
            throw new IllegalArgumentException(
                    "El tipo es obligatorio"
            );
        }

        if (sequence < 1) {
            throw new IllegalArgumentException(
                    "La secuencia debe ser mayor o igual a 1"
            );
        }

        return "%s-%03d".formatted(
                type.prefix(),
                sequence
        );
    }
}