package com.webempresarial.store.digitaltransformation.web.strategic;

import com.webempresarial.store.digitaltransformation.application.strategic.api.*;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicArtifactStatus;
import com.webempresarial.store.digitaltransformation.domain.strategic.StrategicConfidence;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.theme.StoreResolver;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StoredStrategicSynthesis;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicInterpretationMode;
import com.webempresarial.store.digitaltransformation.web.strategic.governance.StrategicGovernanceActorResolver;
import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.ReviewStrategicSynthesisResult;
import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StoredStrategicSynthesis;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisReviewDecision;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisReviewerType;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisStatus;
import com.webempresarial.store.digitaltransformation.web.strategic.governance.StrategicGovernanceActor;
class StrategicIntelligenceControllerTest {

    @Mock
    private StrategicIntelligenceStateQuery stateQuery;

    @Mock
    private StrategicFindingSelectionQuery findingSelectionQuery;

    @Mock
    private StoreResolver storeResolver;

    @Mock
    private HttpServletRequest request;

    private StrategicIntelligenceController controller;
    
    @Mock
    private GenerateStrategicSynthesisCommand
            generateStrategicSynthesisCommand;
    
    @Mock
    private RequestStrategicInterpretationCommand
            requestStrategicInterpretationCommand;
    
    @Mock
    private SubmitStrategicSynthesisForReviewCommand
            submitStrategicSynthesisForReviewCommand;

    @Mock
    private ReviewStrategicSynthesisCommand
            reviewStrategicSynthesisCommand;

    @Mock
    private StrategicGovernanceActorResolver
            governanceActorResolver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        controller =
                new StrategicIntelligenceController(
                        stateQuery,
                        findingSelectionQuery,
                        generateStrategicSynthesisCommand,
                        requestStrategicInterpretationCommand,
                        submitStrategicSynthesisForReviewCommand,
                        reviewStrategicSynthesisCommand,
                        governanceActorResolver,
                        storeResolver
                );
    }
    @Test
    void shouldSubmitSynthesisForReviewAndRedirectToWorkspace() {
        Store store =
                activeStore(
                        10L
                );

        StoredStrategicSynthesis submitted =
                mock(
                        StoredStrategicSynthesis.class
                );

        RedirectAttributes redirectAttributes =
                mock(
                        RedirectAttributes.class
                );

        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                findingSelectionQuery.findAvailableFindings(
                        10L,
                        20L
                )
        ).thenReturn(
                List.of(
                        finding(
                                30L,
                                "FND-001"
                        )
                )
        );

        when(
                submitStrategicSynthesisForReviewCommand.submit(
                        10L,
                        20L,
                        41L
                )
        ).thenReturn(
                submitted
        );

        when(
                submitted.id()
        ).thenReturn(
                42L
        );

        String redirect =
                controller.submitReview(
                        20L,
                        41L,
                        30L,
                        request,
                        redirectAttributes
                );

        assertThat(redirect)
                .isEqualTo(
                        "redirect:/admin/digital-transformation/projects/20/strategic-intelligence"
                                + "?findingArtifactId=30"
                );

        verify(submitStrategicSynthesisForReviewCommand)
                .submit(
                        10L,
                        20L,
                        41L
                );

        verify(redirectAttributes)
                .addFlashAttribute(
                        "successMessage",
                        "La síntesis fue enviada a revisión correctamente."
                );

        verify(redirectAttributes)
                .addFlashAttribute(
                        "submittedSynthesisId",
                        42L
                );

        verifyNoInteractions(
                governanceActorResolver,
                reviewStrategicSynthesisCommand
        );
    }
    
    @Test
    void shouldRejectNullSubmitReviewResult() {
        Store store =
                activeStore(
                        10L
                );

        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                findingSelectionQuery.findAvailableFindings(
                        10L,
                        20L
                )
        ).thenReturn(
                List.of(
                        finding(
                                30L,
                                "FND-001"
                        )
                )
        );

        when(
                submitStrategicSynthesisForReviewCommand.submit(
                        10L,
                        20L,
                        41L
                )
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                controller.submitReview(
                        20L,
                        41L,
                        30L,
                        request,
                        mock(RedirectAttributes.class)
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "resultado nulo"
                );

        verifyNoInteractions(
                governanceActorResolver,
                reviewStrategicSynthesisCommand
        );
    }
    @Test
    void shouldApproveSynthesisUsingAuthenticatedGovernanceActor() {
        Store store =
                activeStore(
                        10L
                );

        StrategicGovernanceActor actor =
                new StrategicGovernanceActor(
                        "consultant@webempresarial.com",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT
                );

        ReviewStrategicSynthesisResult result =
                mock(
                        ReviewStrategicSynthesisResult.class
                );

        RedirectAttributes redirectAttributes =
                mock(
                        RedirectAttributes.class
                );

        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                findingSelectionQuery.findAvailableFindings(
                        10L,
                        20L
                )
        ).thenReturn(
                List.of(
                        finding(
                                30L,
                                "FND-001"
                        )
                )
        );

        when(
                governanceActorResolver.resolve()
        ).thenReturn(
                actor
        );

        when(
                reviewStrategicSynthesisCommand.review(
                        10L,
                        20L,
                        42L,
                        "consultant@webempresarial.com",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "La síntesis está sustentada correctamente"
                )
        ).thenReturn(
                result
        );

        when(
                result.resultingStatus()
        ).thenReturn(
                StrategicSynthesisStatus.APPROVED
        );

        String redirect =
                controller.approveSynthesis(
                        20L,
                        42L,
                        30L,
                        "  La síntesis está sustentada correctamente  ",
                        request,
                        redirectAttributes
                );

        assertThat(redirect)
                .isEqualTo(
                        "redirect:/admin/digital-transformation/projects/20/strategic-intelligence"
                                + "?findingArtifactId=30"
                );

        verify(governanceActorResolver)
                .resolve();

        /*
         * También verificamos que reason sea normalizada
         * antes de entrar al application command.
         */
        verify(reviewStrategicSynthesisCommand)
                .review(
                        10L,
                        20L,
                        42L,
                        "consultant@webempresarial.com",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.APPROVE,
                        "La síntesis está sustentada correctamente"
                );

        verify(redirectAttributes)
                .addFlashAttribute(
                        "successMessage",
                        "La síntesis fue aprobada correctamente."
                );

        verify(redirectAttributes)
                .addFlashAttribute(
                        "reviewResultingStatus",
                        StrategicSynthesisStatus.APPROVED
                );
    }
    @Test
    void shouldRejectSynthesisUsingAuthenticatedGovernanceActor() {
        Store store =
                activeStore(
                        10L
                );

        StrategicGovernanceActor actor =
                new StrategicGovernanceActor(
                        "owner@webempresarial.com",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT
                );

        ReviewStrategicSynthesisResult result =
                mock(
                        ReviewStrategicSynthesisResult.class
                );

        RedirectAttributes redirectAttributes =
                mock(
                        RedirectAttributes.class
                );

        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                findingSelectionQuery.findAvailableFindings(
                        10L,
                        20L
                )
        ).thenReturn(
                List.of(
                        finding(
                                30L,
                                "FND-001"
                        )
                )
        );

        when(
                governanceActorResolver.resolve()
        ).thenReturn(
                actor
        );

        when(
                reviewStrategicSynthesisCommand.review(
                        10L,
                        20L,
                        42L,
                        "owner@webempresarial.com",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.REJECT,
                        "La interpretación requiere ajustes"
                )
        ).thenReturn(
                result
        );

        when(
                result.resultingStatus()
        ).thenReturn(
                StrategicSynthesisStatus.REJECTED
        );

        String redirect =
                controller.rejectSynthesis(
                        20L,
                        42L,
                        30L,
                        "La interpretación requiere ajustes",
                        request,
                        redirectAttributes
                );

        assertThat(redirect)
                .isEqualTo(
                        "redirect:/admin/digital-transformation/projects/20/strategic-intelligence"
                                + "?findingArtifactId=30"
                );

        verify(reviewStrategicSynthesisCommand)
                .review(
                        10L,
                        20L,
                        42L,
                        "owner@webempresarial.com",
                        StrategicSynthesisReviewerType.HUMAN_CONSULTANT,
                        StrategicSynthesisReviewDecision.REJECT,
                        "La interpretación requiere ajustes"
                );

        verify(redirectAttributes)
                .addFlashAttribute(
                        "successMessage",
                        "La síntesis fue rechazada correctamente."
                );

        verify(redirectAttributes)
                .addFlashAttribute(
                        "reviewResultingStatus",
                        StrategicSynthesisStatus.REJECTED
                );
    }
    @Test
    void shouldRejectBlankReviewReasonBeforeResolvingTenantOrActor() {
        assertThatThrownBy(() ->
                controller.approveSynthesis(
                        20L,
                        42L,
                        30L,
                        "   ",
                        request,
                        mock(RedirectAttributes.class)
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "razón"
                );

        verifyNoInteractions(
                storeResolver,
                findingSelectionQuery,
                governanceActorResolver,
                reviewStrategicSynthesisCommand
        );
    }
    @Test
    void shouldNotReviewWhenGovernanceActorCannotBeResolved() {
        Store store =
                activeStore(
                        10L
                );

        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                findingSelectionQuery.findAvailableFindings(
                        10L,
                        20L
                )
        ).thenReturn(
                List.of(
                        finding(
                                30L,
                                "FND-001"
                        )
                )
        );

        when(
                governanceActorResolver.resolve()
        ).thenThrow(
                new IllegalStateException(
                        "El usuario autenticado no está autorizado para decisiones de governance"
                )
        );

        assertThatThrownBy(() ->
                controller.approveSynthesis(
                        20L,
                        42L,
                        30L,
                        "Approved by consultant",
                        request,
                        mock(RedirectAttributes.class)
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "no está autorizado"
                );

        verifyNoInteractions(
                reviewStrategicSynthesisCommand
        );
    }
    @Test
    void shouldRequestAiInterpretationAndRedirectToWorkspace() {
        Store store =
                activeStore(
                        10L
                );

        RequestStrategicInterpretationResult result =
                mock(
                        RequestStrategicInterpretationResult.class
                );

        StoredStrategicSynthesis storedAi =
                mock(
                        StoredStrategicSynthesis.class
                );

        RedirectAttributes redirectAttributes =
                mock(
                        RedirectAttributes.class
                );

        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                findingSelectionQuery.findAvailableFindings(
                        10L,
                        20L
                )
        ).thenReturn(
                List.of(
                        finding(
                                30L,
                                "FND-001"
                        )
                )
        );

        when(
                requestStrategicInterpretationCommand.interpret(
                        10L,
                        20L,
                        StrategicInterpretationMode.REFINE_THESIS
                )
        ).thenReturn(
                result
        );

        when(
                result.aiSynthesis()
        ).thenReturn(
                storedAi
        );

        when(
                storedAi.id()
        ).thenReturn(
                42L
        );

        String redirect =
                controller.interpretSynthesis(
                        20L,
                        30L,
                        StrategicInterpretationMode.REFINE_THESIS,
                        request,
                        redirectAttributes
                );

        assertThat(redirect)
                .isEqualTo(
                        "redirect:/admin/digital-transformation/projects/20/strategic-intelligence"
                                + "?findingArtifactId=30"
                );

        verify(requestStrategicInterpretationCommand)
                .interpret(
                        10L,
                        20L,
                        StrategicInterpretationMode.REFINE_THESIS
                );

        verify(redirectAttributes)
                .addFlashAttribute(
                        "generatedAiSynthesisId",
                        42L
                );
    }

    @Test
    void shouldRejectInvalidProjectIdBeforeAiInterpretationDependencies() {
        assertThatThrownBy(() ->
                controller.interpretSynthesis(
                        0L,
                        30L,
                        StrategicInterpretationMode.REFINE_THESIS,
                        request,
                        mock(RedirectAttributes.class)
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "projectId"
                );

        verifyNoInteractions(
                storeResolver,
                findingSelectionQuery,
                requestStrategicInterpretationCommand
        );
    }

    @Test
    void shouldRejectInvalidFindingBeforeAiInterpretation() {
        Store store =
                activeStore(
                        10L
                );

        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                findingSelectionQuery.findAvailableFindings(
                        10L,
                        20L
                )
        ).thenReturn(
                List.of(
                        finding(
                                31L,
                                "FND-002"
                        )
                )
        );

        assertThatThrownBy(() ->
                controller.interpretSynthesis(
                        20L,
                        30L,
                        StrategicInterpretationMode.REFINE_THESIS,
                        request,
                        mock(RedirectAttributes.class)
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "no pertenece al proyecto"
                );

        verifyNoInteractions(
                requestStrategicInterpretationCommand
        );
    }

    @Test
    void shouldRejectNullAiInterpretationResult() {
        Store store =
                activeStore(
                        10L
                );

        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                findingSelectionQuery.findAvailableFindings(
                        10L,
                        20L
                )
        ).thenReturn(
                List.of(
                        finding(
                                30L,
                                "FND-001"
                        )
                )
        );

        when(
                requestStrategicInterpretationCommand.interpret(
                        10L,
                        20L,
                        StrategicInterpretationMode.REFINE_THESIS
                )
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                controller.interpretSynthesis(
                        20L,
                        30L,
                        StrategicInterpretationMode.REFINE_THESIS,
                        request,
                        mock(RedirectAttributes.class)
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "resultado nulo"
                );
    }

    @Test
    void shouldUseTenantIdentityWhenRequestingAiInterpretation() {
        Store store =
                activeStore(
                        77L
                );

        RequestStrategicInterpretationResult result =
                mock(
                        RequestStrategicInterpretationResult.class
                );

        StoredStrategicSynthesis storedAi =
                mock(
                        StoredStrategicSynthesis.class
                );

        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                findingSelectionQuery.findAvailableFindings(
                        77L,
                        200L
                )
        ).thenReturn(
                List.of(
                        finding(
                                300L,
                                "FND-300"
                        )
                )
        );

        when(
                requestStrategicInterpretationCommand.interpret(
                        77L,
                        200L,
                        StrategicInterpretationMode.REFINE_THESIS
                )
        ).thenReturn(
                result
        );

        when(
                result.aiSynthesis()
        ).thenReturn(
                storedAi
        );

        when(
                storedAi.id()
        ).thenReturn(
                900L
        );

        controller.interpretSynthesis(
                200L,
                300L,
                StrategicInterpretationMode.REFINE_THESIS,
                request,
                mock(RedirectAttributes.class)
        );

        verify(requestStrategicInterpretationCommand)
                .interpret(
                        77L,
                        200L,
                        StrategicInterpretationMode.REFINE_THESIS
                );
    }
    
    @Test
    void shouldGenerateSynthesisAndRedirectToWorkspace() {
        Store store =
                activeStore(
                        10L
                );

        StrategicFindingOptionResponse finding =
                finding(
                        30L,
                        "FND-001"
                );

        GenerateStrategicSynthesisResult result =
                mock(
                        GenerateStrategicSynthesisResult.class
                );

        StoredStrategicSynthesis stored =
                mock(
                        StoredStrategicSynthesis.class
                );

        RedirectAttributes redirectAttributes =
                mock(
                        RedirectAttributes.class
                );

        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                findingSelectionQuery.findAvailableFindings(
                        10L,
                        20L
                )
        ).thenReturn(
                List.of(
                        finding
                )
        );

        when(
                generateStrategicSynthesisCommand.generate(
                        10L,
                        20L,
                        30L
                )
        ).thenReturn(
                result
        );

        when(
                result.synthesis()
        ).thenReturn(
                stored
        );

        when(
                stored.id()
        ).thenReturn(
                41L
        );

        String redirect =
                controller.generateSynthesis(
                        20L,
                        30L,
                        request,
                        redirectAttributes
                );

        assertThat(redirect)
                .isEqualTo(
                        "redirect:/admin/digital-transformation/projects/20/strategic-intelligence"
                                + "?findingArtifactId=30"
                );

        verify(generateStrategicSynthesisCommand)
                .generate(
                        10L,
                        20L,
                        30L
                );

        verify(redirectAttributes)
                .addFlashAttribute(
                        "successMessage",
                        "La síntesis estratégica determinista fue generada correctamente."
                );

        verify(redirectAttributes)
                .addFlashAttribute(
                        "generatedSynthesisId",
                        41L
                );
    }

    @Test
    void shouldRejectInvalidProjectIdBeforeGenerateDependencies() {
        RedirectAttributes redirectAttributes =
                mock(
                        RedirectAttributes.class
                );

        assertThatThrownBy(() ->
                controller.generateSynthesis(
                        0L,
                        30L,
                        request,
                        redirectAttributes
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "projectId"
                );

        verifyNoInteractions(
                storeResolver,
                findingSelectionQuery,
                generateStrategicSynthesisCommand
        );
    }

    @Test
    void shouldRejectInvalidFindingArtifactIdBeforeGenerateDependencies() {
        RedirectAttributes redirectAttributes =
                mock(
                        RedirectAttributes.class
                );

        assertThatThrownBy(() ->
                controller.generateSynthesis(
                        20L,
                        0L,
                        request,
                        redirectAttributes
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "findingArtifactId"
                );

        verifyNoInteractions(
                storeResolver,
                findingSelectionQuery,
                generateStrategicSynthesisCommand
        );
    }

    @Test
    void shouldRejectGenerateWhenFindingDoesNotBelongToProjectSelection() {
        Store store =
                activeStore(
                        10L
                );

        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                findingSelectionQuery.findAvailableFindings(
                        10L,
                        20L
                )
        ).thenReturn(
                List.of(
                        finding(
                                31L,
                                "FND-002"
                        )
                )
        );

        assertThatThrownBy(() ->
                controller.generateSynthesis(
                        20L,
                        30L,
                        request,
                        mock(
                                RedirectAttributes.class
                        )
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "no pertenece al proyecto"
                );

        verifyNoInteractions(
                generateStrategicSynthesisCommand
        );
    }

    @Test
    void shouldRejectNullGenerateResultReturnedByCommand() {
        Store store =
                activeStore(
                        10L
                );

        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                findingSelectionQuery.findAvailableFindings(
                        10L,
                        20L
                )
        ).thenReturn(
                List.of(
                        finding(
                                30L,
                                "FND-001"
                        )
                )
        );

        when(
                generateStrategicSynthesisCommand.generate(
                        10L,
                        20L,
                        30L
                )
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                controller.generateSynthesis(
                        20L,
                        30L,
                        request,
                        mock(
                                RedirectAttributes.class
                        )
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "resultado nulo"
                );
    }

    @Test
    void shouldUseTenantIdentityWhenGeneratingSynthesis() {
        Store store =
                activeStore(
                        77L
                );

        StrategicFindingOptionResponse finding =
                finding(
                        300L,
                        "FND-300"
                );

        GenerateStrategicSynthesisResult result =
                mock(
                        GenerateStrategicSynthesisResult.class
                );

        StoredStrategicSynthesis stored =
                mock(
                        StoredStrategicSynthesis.class
                );

        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                findingSelectionQuery.findAvailableFindings(
                        77L,
                        200L
                )
        ).thenReturn(
                List.of(
                        finding
                )
        );

        when(
                generateStrategicSynthesisCommand.generate(
                        77L,
                        200L,
                        300L
                )
        ).thenReturn(
                result
        );

        when(
                result.synthesis()
        ).thenReturn(
                stored
        );

        when(
                stored.id()
        ).thenReturn(
                900L
        );

        controller.generateSynthesis(
                200L,
                300L,
                request,
                mock(
                        RedirectAttributes.class
                )
        );

        verify(findingSelectionQuery)
                .findAvailableFindings(
                        77L,
                        200L
                );

        verify(generateStrategicSynthesisCommand)
                .generate(
                        77L,
                        200L,
                        300L
                );
    }

    @Test
    void shouldRenderWorkspaceUsingRequestedFinding() {
        Store store =
                activeStore(
                        10L
                );

        StrategicFindingOptionResponse first =
                finding(
                        30L,
                        "FND-001"
                );

        StrategicFindingOptionResponse second =
                finding(
                        31L,
                        "FND-002"
                );

        StrategicIntelligenceStateResponse state =
                mock(
                        StrategicIntelligenceStateResponse.class
                );

        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                findingSelectionQuery.findAvailableFindings(
                        10L,
                        20L
                )
        ).thenReturn(
                List.of(
                        first,
                        second
                )
        );

        when(
                stateQuery.findState(
                        10L,
                        20L,
                        31L
                )
        ).thenReturn(
                state
        );

        Model model =
                new ConcurrentModel();

        String view =
                controller.workspace(
                        20L,
                        31L,
                        request,
                        model
                );

        assertThat(view)
                .isEqualTo(
                        "admin/digital-transformation/strategic-intelligence/workspace"
                );

        assertThat(
                model.getAttribute(
                        "projectId"
                )
        ).isEqualTo(
                20L
        );

        assertThat(
                model.getAttribute(
                        "findingArtifactId"
                )
        ).isEqualTo(
                31L
        );

        assertThat(
                model.getAttribute(
                        "strategicIntelligence"
                )
        ).isSameAs(
                state
        );

        assertThat(
                model.getAttribute(
                        "findings"
                )
        ).isEqualTo(
                List.of(
                        first,
                        second
                )
        );

        assertThat(
                model.getAttribute(
                        "hasFindings"
                )
        ).isEqualTo(
                true
        );

        assertThat(
                model.getAttribute(
                        "requiresFindingSelection"
                )
        ).isEqualTo(
                false
        );

        assertThat(
                model.getAttribute(
                        "strategicIntelligencePage"
                )
        ).isEqualTo(
                true
        );

        verify(stateQuery)
                .findState(
                        10L,
                        20L,
                        31L
                );
    }

    @Test
    void shouldAutomaticallySelectOnlyFinding() {
        Store store =
                activeStore(
                        10L
                );

        StrategicFindingOptionResponse finding =
                finding(
                        30L,
                        "FND-001"
                );

        StrategicIntelligenceStateResponse state =
                mock(
                        StrategicIntelligenceStateResponse.class
                );

        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                findingSelectionQuery.findAvailableFindings(
                        10L,
                        20L
                )
        ).thenReturn(
                List.of(
                        finding
                )
        );

        when(
                stateQuery.findState(
                        10L,
                        20L,
                        30L
                )
        ).thenReturn(
                state
        );

        Model model =
                new ConcurrentModel();

        controller.workspace(
                20L,
                null,
                request,
                model
        );

        assertThat(
                model.getAttribute(
                        "findingArtifactId"
                )
        ).isEqualTo(
                30L
        );

        assertThat(
                model.getAttribute(
                        "strategicIntelligence"
                )
        ).isSameAs(
                state
        );

        assertThat(
                model.getAttribute(
                        "requiresFindingSelection"
                )
        ).isEqualTo(
                false
        );

        verify(stateQuery)
                .findState(
                        10L,
                        20L,
                        30L
                );
    }

    @Test
    void shouldRequireSelectionWhenMultipleFindingsExistAndNoneWasRequested() {
        Store store =
                activeStore(
                        10L
                );

        StrategicFindingOptionResponse first =
                finding(
                        30L,
                        "FND-001"
                );

        StrategicFindingOptionResponse second =
                finding(
                        31L,
                        "FND-002"
                );

        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                findingSelectionQuery.findAvailableFindings(
                        10L,
                        20L
                )
        ).thenReturn(
                List.of(
                        first,
                        second
                )
        );

        Model model =
                new ConcurrentModel();

        String view =
                controller.workspace(
                        20L,
                        null,
                        request,
                        model
                );

        assertThat(view)
                .isEqualTo(
                        "admin/digital-transformation/strategic-intelligence/workspace"
                );

        assertThat(
                model.getAttribute(
                        "findingArtifactId"
                )
        ).isNull();

        assertThat(
                model.getAttribute(
                        "strategicIntelligence"
                )
        ).isNull();

        assertThat(
                model.getAttribute(
                        "hasFindings"
                )
        ).isEqualTo(
                true
        );

        assertThat(
                model.getAttribute(
                        "requiresFindingSelection"
                )
        ).isEqualTo(
                true
        );

        verifyNoInteractions(
                stateQuery
        );
    }

    @Test
    void shouldRenderEmptyStateWhenNoFindingsExist() {
        Store store =
                activeStore(
                        10L
                );

        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                findingSelectionQuery.findAvailableFindings(
                        10L,
                        20L
                )
        ).thenReturn(
                List.of()
        );

        Model model =
                new ConcurrentModel();

        String view =
                controller.workspace(
                        20L,
                        null,
                        request,
                        model
                );

        assertThat(view)
                .isEqualTo(
                        "admin/digital-transformation/strategic-intelligence/workspace"
                );

        assertThat(
                model.getAttribute(
                        "hasFindings"
                )
        ).isEqualTo(
                false
        );

        assertThat(
                model.getAttribute(
                        "requiresFindingSelection"
                )
        ).isEqualTo(
                false
        );

        assertThat(
                model.getAttribute(
                        "findingArtifactId"
                )
        ).isNull();

        assertThat(
                model.getAttribute(
                        "strategicIntelligence"
                )
        ).isNull();

        verifyNoInteractions(
                stateQuery
        );
    }

    @Test
    void shouldRejectRequestedFindingThatDoesNotBelongToTenantProjectSelection() {
        Store store =
                activeStore(
                        10L
                );

        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                findingSelectionQuery.findAvailableFindings(
                        10L,
                        20L
                )
        ).thenReturn(
                List.of(
                        finding(
                                30L,
                                "FND-001"
                        )
                )
        );

        assertThatThrownBy(() ->
                controller.workspace(
                        20L,
                        999L,
                        request,
                        new ConcurrentModel()
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "no pertenece al proyecto"
                );

        verifyNoInteractions(
                stateQuery
        );
    }

    @Test
    void shouldRejectRequestedFindingWhenProjectHasNoFindings() {
        Store store =
                activeStore(
                        10L
                );

        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                findingSelectionQuery.findAvailableFindings(
                        10L,
                        20L
                )
        ).thenReturn(
                List.of()
        );

        assertThatThrownBy(() ->
                controller.workspace(
                        20L,
                        30L,
                        request,
                        new ConcurrentModel()
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "no pertenece al proyecto"
                );

        verifyNoInteractions(
                stateQuery
        );
    }

    @Test
    void shouldRejectInvalidProjectIdBeforeResolvingTenant() {
        assertThatThrownBy(() ->
                controller.workspace(
                        0L,
                        null,
                        request,
                        new ConcurrentModel()
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "projectId"
                );

        verifyNoInteractions(
                storeResolver,
                findingSelectionQuery,
                stateQuery
        );
    }

    @Test
    void shouldRejectInvalidRequestedFindingBeforeResolvingTenant() {
        assertThatThrownBy(() ->
                controller.workspace(
                        20L,
                        -1L,
                        request,
                        new ConcurrentModel()
                )
        )
                .isInstanceOf(
                        IllegalArgumentException.class
                )
                .hasMessageContaining(
                        "findingArtifactId"
                );

        verifyNoInteractions(
                storeResolver,
                findingSelectionQuery,
                stateQuery
        );
    }

    @Test
    void shouldRejectWhenStoreCannotBeResolved() {
        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                controller.workspace(
                        20L,
                        null,
                        request,
                        new ConcurrentModel()
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "resolver la tienda"
                );

        verifyNoInteractions(
                findingSelectionQuery,
                stateQuery
        );
    }

    @Test
    void shouldRejectWhenResolvedStoreHasNoPersistentIdentity() {
        Store store =
                activeStore(
                        null
                );

        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        assertThatThrownBy(() ->
                controller.workspace(
                        20L,
                        null,
                        request,
                        new ConcurrentModel()
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "resolver la tienda"
                );

        verifyNoInteractions(
                findingSelectionQuery,
                stateQuery
        );
    }

    @Test
    void shouldRejectInactiveStore() {
        Store store =
                activeStore(
                        10L
                );

        when(
                store.isActiva()
        ).thenReturn(
                false
        );

        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        assertThatThrownBy(() ->
                controller.workspace(
                        20L,
                        null,
                        request,
                        new ConcurrentModel()
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "inactiva"
                );

        verifyNoInteractions(
                findingSelectionQuery,
                stateQuery
        );
    }

    @Test
    void shouldWrapStoreResolutionFailure() {
        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenThrow(
                new IllegalStateException(
                        "resolver interno"
                )
        );

        assertThatThrownBy(() ->
                controller.workspace(
                        20L,
                        null,
                        request,
                        new ConcurrentModel()
                )
        )
                .isInstanceOf(
                        IllegalStateException.class
                )
                .hasMessageContaining(
                        "resolver la tienda"
                )
                .hasCauseInstanceOf(
                        IllegalStateException.class
                );

        verifyNoInteractions(
                findingSelectionQuery,
                stateQuery
        );
    }

    @Test
    void shouldRejectNullFindingListReturnedByApplicationQuery() {
        Store store =
                activeStore(
                        10L
                );

        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                findingSelectionQuery.findAvailableFindings(
                        10L,
                        20L
                )
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                controller.workspace(
                        20L,
                        null,
                        request,
                        new ConcurrentModel()
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "lista nula"
                );

        verifyNoInteractions(
                stateQuery
        );
    }

    @Test
    void shouldRejectNullStateReturnedByApplicationQuery() {
        Store store =
                activeStore(
                        10L
                );

        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                findingSelectionQuery.findAvailableFindings(
                        10L,
                        20L
                )
        ).thenReturn(
                List.of(
                        finding(
                                30L,
                                "FND-001"
                        )
                )
        );

        when(
                stateQuery.findState(
                        10L,
                        20L,
                        30L
                )
        ).thenReturn(
                null
        );

        assertThatThrownBy(() ->
                controller.workspace(
                        20L,
                        null,
                        request,
                        new ConcurrentModel()
                )
        )
                .isInstanceOf(
                        NullPointerException.class
                )
                .hasMessageContaining(
                        "estado nulo"
                );
    }

    @Test
    void shouldUseTenantIdentityWhenLoadingFindingsAndState() {
        Store store =
                activeStore(
                        77L
                );

        when(
                storeResolver.getCurrentStore(
                        request
                )
        ).thenReturn(
                store
        );

        when(
                findingSelectionQuery.findAvailableFindings(
                        77L,
                        200L
                )
        ).thenReturn(
                List.of(
                        finding(
                                300L,
                                "FND-300"
                        )
                )
        );

        when(
                stateQuery.findState(
                        77L,
                        200L,
                        300L
                )
        ).thenReturn(
                mock(
                        StrategicIntelligenceStateResponse.class
                )
        );

        controller.workspace(
                200L,
                null,
                request,
                new ConcurrentModel()
        );

        verify(findingSelectionQuery)
                .findAvailableFindings(
                        77L,
                        200L
                );

        verify(stateQuery)
                .findState(
                        77L,
                        200L,
                        300L
                );
    }

    private static StrategicFindingOptionResponse finding(
            Long id,
            String code
    ) {
        return new StrategicFindingOptionResponse(
                id,
                code,
                "Strategic finding " + code,
                StrategicArtifactStatus.DRAFT,
                StrategicConfidence.STRONGLY_SUPPORTED,
                false
        );
    }

    private static Store activeStore(
            Long id
    ) {
        Store store =
                mock(
                        Store.class
                );

        when(
                store.getId()
        ).thenReturn(
                id
        );

        when(
                store.isActiva()
        ).thenReturn(
                true
        );

        return store;
    }
}