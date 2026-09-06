package com.webempresarial.store.digitaltransformation.application.strategic;

public interface StrategicArtifactCodeGenerator {

    String generate(
            StrategicArtifactTypeDescriptor type,
            long sequence
    );
}