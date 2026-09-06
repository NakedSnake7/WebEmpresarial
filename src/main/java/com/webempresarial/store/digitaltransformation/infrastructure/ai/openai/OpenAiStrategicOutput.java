package com.webempresarial.store.digitaltransformation.infrastructure.ai.openai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public class OpenAiStrategicOutput {

    @JsonPropertyDescription(
            "Refined strategic thesis derived strictly from the authorized strategic context."
    )
    public String interpretedThesis;

    @JsonPropertyDescription(
            "Concise executive narrative explaining the strategic interpretation without introducing new facts."
    )
    public String executiveNarrative;

    @JsonPropertyDescription(
            "Artifact identifiers directly referenced by the interpretation. "
            + "Every identifier must come from the authorized source artifact list."
    )
    public List<String> referencedArtifactCodes;

    public OpenAiStrategicOutput() {
    }
}