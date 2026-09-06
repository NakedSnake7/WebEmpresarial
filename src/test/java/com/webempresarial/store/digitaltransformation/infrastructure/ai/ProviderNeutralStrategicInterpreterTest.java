package com.webempresarial.store.digitaltransformation.infrastructure.ai;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StrategicInterpretationPromptPolicy;
import com.webempresarial.store.digitaltransformation.domain.evidence.SourceEvidence;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import com.webempresarial.store.digitaltransformation.support.TestSources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProviderNeutralStrategicInterpreterTest {

    @Mock
    private StrategicAIClient aiClient;

    @Mock
    private StrategicInterpretationPromptPolicy
            promptPolicy;

    private ProviderNeutralStrategicInterpreter
            interpreter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        interpreter =
                new ProviderNeutralStrategicInterpreter(
                        aiClient,
                        promptPolicy
                );
    }

    @Test
    void shouldTranslateDomainRequestIntoProviderNeutralRequest() {
        StrategicInterpretationRequest request =
                request();

        when(
                promptPolicy.systemInstruction()
        ).thenReturn(
                "SYSTEM"
        );

        when(
                promptPolicy.taskInstruction(
                        request
                )
        ).thenReturn(
                "TASK"
        );

        when(
                aiClient.generate(
                        any()
                )
        ).thenReturn(
                new StrategicAIResponse(
                        "Refined thesis",
                        "Executive narrative",
                        request.getSourceArtifactCodes()
                )
        );

        StrategicInterpretationResult result =
                interpreter.interpret(
                        request
                );

        ArgumentCaptor<StrategicAIRequest> captor =
                ArgumentCaptor.forClass(
                        StrategicAIRequest.class
                );

        verify(aiClient)
                .generate(
                        captor.capture()
                );

        StrategicAIRequest aiRequest =
                captor.getValue();

        assertThat(aiRequest.systemInstruction())
                .isEqualTo("SYSTEM");

        assertThat(aiRequest.taskInstruction())
                .isEqualTo("TASK");

        assertThat(aiRequest.finding())
                .isEqualTo(
                        request.getFinding()
                );

        assertThat(aiRequest.businessProblem())
                .isEqualTo(
                        request.getBusinessProblem()
                );

        assertThat(aiRequest.businessObjective())
                .isEqualTo(
                        request.getBusinessObjective()
                );

        assertThat(aiRequest.strategicOpportunity())
                .isEqualTo(
                        request.getStrategicOpportunity()
                );

        assertThat(aiRequest.deterministicThesis())
                .isEqualTo(
                        request.getDeterministicThesis()
                );

        assertThat(aiRequest.sourceArtifactCodes())
                .containsExactlyElementsOf(
                        request.getSourceArtifactCodes()
                );

        assertThat(aiRequest.constraints())
                .contains(
                        "DO_NOT_INTRODUCE_NEW_FACTS",
                        "DO_NOT_INTRODUCE_NEW_OBJECTIVES",
                        "DO_NOT_INTRODUCE_NEW_OPPORTUNITIES"
                );

        assertThat(result.getInterpretedThesis())
                .isEqualTo(
                        "Refined thesis"
                );

        assertThat(result.getExecutiveNarrative())
                .isEqualTo(
                        "Executive narrative"
                );

        assertThat(result.getReferencedArtifactCodes())
                .containsExactlyElementsOf(
                        request.getSourceArtifactCodes()
                );
    }

    @Test
    void shouldRejectNullProviderResponse() {
        StrategicInterpretationRequest request =
                request();

        when(
                promptPolicy.systemInstruction()
        ).thenReturn(
                "SYSTEM"
        );

        when(
                promptPolicy.taskInstruction(
                        request
                )
        ).thenReturn(
                "TASK"
        );

        when(
                aiClient.generate(
                        any()
                )
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                interpreter.interpret(
                        request
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "respuesta nula"
                );
    }

    @Test
    void shouldRejectNullDomainRequestBeforeCallingProvider() {
        assertThatThrownBy(() ->
                interpreter.interpret(
                        null
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                );

        verifyNoInteractions(
                aiClient,
                promptPolicy
        );
    }

    private static StrategicInterpretationRequest request() {
        SourceEvidence evidence =
                TestSources.validEvidence();

        StrategicSynthesis synthesis =
                StrategicSynthesis.create(
                        evidence.getProject(),
                        "Finding",
                        "Business problem",
                        "Business objective",
                        "Strategic opportunity",
                        "Deterministic thesis",
                        StrategicSynthesisEvidenceSummary.of(
                                StrategicEvidenceCoverageStatus.FULLY_SUPPORTED,
                                100,
                                List.of(
                                        "EVD-001"
                                ),
                                4
                        ),
                        StrategicSynthesisConfidence.HIGH,
                        StrategicSynthesisOrigin.DETERMINISTIC,
                        StrategicSynthesisStatus.READY,
                        List.of(
                                "FND-001",
                                "PRB-001",
                                "OBJ-001",
                                "OPP-001"
                        )
                );

        return StrategicInterpretationRequest.from(
                synthesis,
                StrategicInterpretationMode.REFINE_THESIS
        );
    }
}