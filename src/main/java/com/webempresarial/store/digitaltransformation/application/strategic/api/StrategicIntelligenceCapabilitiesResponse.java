package com.webempresarial.store.digitaltransformation.application.strategic.api;

public record StrategicIntelligenceCapabilitiesResponse(

        boolean canGenerateDeterministicSynthesis,

        boolean canRequestAiInterpretation,

        boolean canSubmitForReview,

        boolean canApprove,

        boolean canReject,

        boolean aiAvailable

) {
}