package com.webempresarial.store.digitaltransformation.web.strategic;

import com.webempresarial.store.digitaltransformation.application.strategic.api.GenerateStrategicSynthesisCommand;
import com.webempresarial.store.digitaltransformation.application.strategic.api.GenerateStrategicSynthesisResult;
import com.webempresarial.store.digitaltransformation.application.strategic.api.RequestStrategicInterpretationCommand;
import com.webempresarial.store.digitaltransformation.application.strategic.api.RequestStrategicInterpretationResult;
import com.webempresarial.store.digitaltransformation.application.strategic.api.ReviewStrategicSynthesisCommand;
import com.webempresarial.store.digitaltransformation.application.strategic.api.StrategicFindingOptionResponse;
import com.webempresarial.store.digitaltransformation.application.strategic.api.StrategicFindingSelectionQuery;
import com.webempresarial.store.digitaltransformation.application.strategic.api.StrategicIntelligenceStateQuery;
import com.webempresarial.store.digitaltransformation.application.strategic.api.StrategicIntelligenceStateResponse;
import com.webempresarial.store.digitaltransformation.application.strategic.api.SubmitStrategicSynthesisForReviewCommand;
import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.ReviewStrategicSynthesisResult;
import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StoredStrategicSynthesis;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicInterpretationMode;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.StrategicSynthesisReviewDecision;
import com.webempresarial.store.digitaltransformation.web.strategic.governance.StrategicGovernanceActor;
import com.webempresarial.store.digitaltransformation.web.strategic.governance.StrategicGovernanceActorResolver;
import com.webempresarial.store.model.Store;
import com.webempresarial.store.theme.StoreResolver;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Objects;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
@RequestMapping(
        "/admin/digital-transformation/projects/{projectId}/strategic-intelligence"
)
public class StrategicIntelligenceController {

    private final StrategicIntelligenceStateQuery
            stateQuery;

    private final StrategicFindingSelectionQuery
            findingSelectionQuery;

    private final StoreResolver
            storeResolver;
    
    private final GenerateStrategicSynthesisCommand
    generateStrategicSynthesisCommand;
    
    private final RequestStrategicInterpretationCommand
    requestStrategicInterpretationCommand;
    
    private final SubmitStrategicSynthesisForReviewCommand
    submitStrategicSynthesisForReviewCommand;

private final ReviewStrategicSynthesisCommand
    reviewStrategicSynthesisCommand;

private final StrategicGovernanceActorResolver
    governanceActorResolver;

    public StrategicIntelligenceController(
            StrategicIntelligenceStateQuery stateQuery,
            StrategicFindingSelectionQuery findingSelectionQuery,
            GenerateStrategicSynthesisCommand generateStrategicSynthesisCommand,
            RequestStrategicInterpretationCommand requestStrategicInterpretationCommand,
            SubmitStrategicSynthesisForReviewCommand submitStrategicSynthesisForReviewCommand,
            ReviewStrategicSynthesisCommand reviewStrategicSynthesisCommand,
            StrategicGovernanceActorResolver governanceActorResolver,
            StoreResolver storeResolver
    ) {
        this.stateQuery =
                Objects.requireNonNull(
                        stateQuery,
                        "StrategicIntelligenceStateQuery es obligatorio"
                );

        this.findingSelectionQuery =
                Objects.requireNonNull(
                        findingSelectionQuery,
                        "StrategicFindingSelectionQuery es obligatorio"
                );

        this.generateStrategicSynthesisCommand =
                Objects.requireNonNull(
                        generateStrategicSynthesisCommand,
                        "GenerateStrategicSynthesisCommand es obligatorio"
                );

        this.requestStrategicInterpretationCommand =
                Objects.requireNonNull(
                        requestStrategicInterpretationCommand,
                        "RequestStrategicInterpretationCommand es obligatorio"
                );

        this.submitStrategicSynthesisForReviewCommand =
                Objects.requireNonNull(
                        submitStrategicSynthesisForReviewCommand,
                        "SubmitStrategicSynthesisForReviewCommand es obligatorio"
                );

        this.reviewStrategicSynthesisCommand =
                Objects.requireNonNull(
                        reviewStrategicSynthesisCommand,
                        "ReviewStrategicSynthesisCommand es obligatorio"
                );

        this.governanceActorResolver =
                Objects.requireNonNull(
                        governanceActorResolver,
                        "StrategicGovernanceActorResolver es obligatorio"
                );

        this.storeResolver =
                Objects.requireNonNull(
                        storeResolver,
                        "StoreResolver es obligatorio"
                );
    }
    @PostMapping("/syntheses/{synthesisId}/approve")
    public String approveSynthesis(
            @PathVariable
            Long projectId,

            @PathVariable
            Long synthesisId,

            @RequestParam(
                    name = "findingArtifactId"
            )
            Long findingArtifactId,

            @RequestParam(
                    name = "reason"
            )
            String reason,

            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        return reviewSynthesis(
                projectId,
                synthesisId,
                findingArtifactId,
                reason,
                StrategicSynthesisReviewDecision.APPROVE,
                request,
                redirectAttributes
        );
    }
    
    @PostMapping("/syntheses/{synthesisId}/reject")
    public String rejectSynthesis(
            @PathVariable
            Long projectId,

            @PathVariable
            Long synthesisId,

            @RequestParam(
                    name = "findingArtifactId"
            )
            Long findingArtifactId,

            @RequestParam(
                    name = "reason"
            )
            String reason,

            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        return reviewSynthesis(
                projectId,
                synthesisId,
                findingArtifactId,
                reason,
                StrategicSynthesisReviewDecision.REJECT,
                request,
                redirectAttributes
        );
    }
    private String reviewSynthesis(
            Long projectId,
            Long synthesisId,
            Long findingArtifactId,
            String reason,
            StrategicSynthesisReviewDecision decision,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        requirePositive(
                projectId,
                "projectId"
        );

        requirePositive(
                synthesisId,
                "synthesisId"
        );

        requirePositive(
                findingArtifactId,
                "findingArtifactId"
        );

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "La razón de revisión es obligatoria"
            );
        }

        Store store =
                resolveStore(
                        request
                );

        List<StrategicFindingOptionResponse> findings =
                Objects.requireNonNull(
                        findingSelectionQuery.findAvailableFindings(
                                store.getId(),
                                projectId
                        ),
                        "StrategicFindingSelectionQuery devolvió una lista nula"
                );

        ensureFindingBelongsToSelection(
                findings,
                findingArtifactId
        );

        StrategicGovernanceActor actor =
                Objects.requireNonNull(
                        governanceActorResolver.resolve(),
                        "StrategicGovernanceActorResolver devolvió un actor nulo"
                );

        ReviewStrategicSynthesisResult result =
                Objects.requireNonNull(
                        reviewStrategicSynthesisCommand.review(
                                store.getId(),
                                projectId,
                                synthesisId,
                                actor.reviewer(),
                                actor.reviewerType(),
                                decision,
                                reason.trim()
                        ),
                        "ReviewStrategicSynthesisCommand devolvió un resultado nulo"
                );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                decision == StrategicSynthesisReviewDecision.APPROVE
                        ? "La síntesis fue aprobada correctamente."
                        : "La síntesis fue rechazada correctamente."
        );

        redirectAttributes.addFlashAttribute(
                "reviewResultingStatus",
                result.resultingStatus()
        );

        return redirectToWorkspace(
                projectId,
                findingArtifactId
        );
    }
    @PostMapping("/syntheses/{synthesisId}/submit-review")
    public String submitReview(
            @PathVariable
            Long projectId,

            @PathVariable
            Long synthesisId,

            @RequestParam(
                    name = "findingArtifactId"
            )
            Long findingArtifactId,

            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        requirePositive(
                projectId,
                "projectId"
        );

        requirePositive(
                synthesisId,
                "synthesisId"
        );

        requirePositive(
                findingArtifactId,
                "findingArtifactId"
        );

        Store store =
                resolveStore(
                        request
                );

        List<StrategicFindingOptionResponse> findings =
                Objects.requireNonNull(
                        findingSelectionQuery.findAvailableFindings(
                                store.getId(),
                                projectId
                        ),
                        "StrategicFindingSelectionQuery devolvió una lista nula"
                );

        ensureFindingBelongsToSelection(
                findings,
                findingArtifactId
        );

        StoredStrategicSynthesis result =
                Objects.requireNonNull(
                        submitStrategicSynthesisForReviewCommand.submit(
                                store.getId(),
                                projectId,
                                synthesisId
                        ),
                        "SubmitStrategicSynthesisForReviewCommand devolvió un resultado nulo"
                );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "La síntesis fue enviada a revisión correctamente."
        );

        redirectAttributes.addFlashAttribute(
                "submittedSynthesisId",
                result.id()
        );

        return redirectToWorkspace(
                projectId,
                findingArtifactId
        );
    }
    
    
    private static void ensureFindingBelongsToSelection(
            List<StrategicFindingOptionResponse> findings,
            Long findingArtifactId
    ) {
        Objects.requireNonNull(
                findings,
                "La lista de findings es obligatoria"
        );

        requirePositive(
                findingArtifactId,
                "findingArtifactId"
        );

        boolean exists =
                findings.stream()
                        .anyMatch(finding ->
                                finding != null
                                        && Objects.equals(
                                                finding.id(),
                                                findingArtifactId
                                        )
                        );

        if (!exists) {
            throw new IllegalArgumentException(
                    "El findingArtifactId solicitado no pertenece al proyecto"
            );
        }
    }

    private static String redirectToWorkspace(
            Long projectId,
            Long findingArtifactId
    ) {
        requirePositive(
                projectId,
                "projectId"
        );

        requirePositive(
                findingArtifactId,
                "findingArtifactId"
        );

        return "redirect:/admin/digital-transformation/projects/"
                + projectId
                + "/strategic-intelligence"
                + "?findingArtifactId="
                + findingArtifactId;
    }
    
    @PostMapping("/interpret")
    public String interpretSynthesis(
            @PathVariable
            Long projectId,

            @RequestParam(
                    name = "findingArtifactId"
            )
            Long findingArtifactId,

            @RequestParam(
                    name = "mode",
                    defaultValue = "REFINE_THESIS"
            )
            StrategicInterpretationMode mode,

            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        requirePositive(
                projectId,
                "projectId"
        );

        requirePositive(
                findingArtifactId,
                "findingArtifactId"
        );

        Objects.requireNonNull(
                mode,
                "El modo de interpretación es obligatorio"
        );

        Store store =
                resolveStore(
                        request
                );

        List<StrategicFindingOptionResponse> findings =
                Objects.requireNonNull(
                        findingSelectionQuery
                                .findAvailableFindings(
                                        store.getId(),
                                        projectId
                                ),
                        "StrategicFindingSelectionQuery devolvió una lista nula"
                );

        ensureFindingBelongsToSelection(
                findings,
                findingArtifactId
        );

        RequestStrategicInterpretationResult result =
                Objects.requireNonNull(
                        requestStrategicInterpretationCommand.interpret(
                                store.getId(),
                                projectId,
                                mode
                        ),
                        "RequestStrategicInterpretationCommand devolvió un resultado nulo"
                );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "La interpretación estratégica asistida por IA fue generada correctamente."
        );

        redirectAttributes.addFlashAttribute(
                "generatedAiSynthesisId",
                result.aiSynthesis().id()
        );

        return redirectToWorkspace(
                projectId,
                findingArtifactId
        );
    }
    
    @PostMapping("/generate")
    public String generateSynthesis(
            @PathVariable
            Long projectId,

            @RequestParam(
                    name = "findingArtifactId"
            )
            Long findingArtifactId,

            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        requirePositive(
                projectId,
                "projectId"
        );

        requirePositive(
                findingArtifactId,
                "findingArtifactId"
        );

        Store store =
                resolveStore(
                        request
                );

        List<StrategicFindingOptionResponse> findings =
                Objects.requireNonNull(
                        findingSelectionQuery
                                .findAvailableFindings(
                                        store.getId(),
                                        projectId
                                ),
                        "StrategicFindingSelectionQuery devolvió una lista nula"
                );

        ensureFindingBelongsToSelection(
                findings,
                findingArtifactId
        );

        GenerateStrategicSynthesisResult result =
                Objects.requireNonNull(
                        generateStrategicSynthesisCommand.generate(
                                store.getId(),
                                projectId,
                                findingArtifactId
                        ),
                        "GenerateStrategicSynthesisCommand devolvió un resultado nulo"
                );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "La síntesis estratégica determinista fue generada correctamente."
        );

        redirectAttributes.addFlashAttribute(
                "generatedSynthesisId",
                result.synthesis().id()
        );

        return redirectToWorkspace(
                projectId,
                findingArtifactId
        );
    }

    @GetMapping
    public String workspace(
            @PathVariable
            Long projectId,

            @RequestParam(
                    name = "findingArtifactId",
                    required = false
            )
            Long findingArtifactId,

            HttpServletRequest request,
            Model model
    ) {
        requirePositive(
                projectId,
                "projectId"
        );

        if (findingArtifactId != null) {
            requirePositive(
                    findingArtifactId,
                    "findingArtifactId"
            );
        }

        Store store =
                resolveStore(
                        request
                );

        Long storeId =
                store.getId();

        List<StrategicFindingOptionResponse> findings =
                Objects.requireNonNull(
                        findingSelectionQuery
                                .findAvailableFindings(
                                        storeId,
                                        projectId
                                ),
                        "StrategicFindingSelectionQuery devolvió una lista nula"
                );

        Long selectedFindingArtifactId =
                resolveSelectedFinding(
                        findings,
                        findingArtifactId
                );

        StrategicIntelligenceStateResponse state =
                resolveState(
                        storeId,
                        projectId,
                        selectedFindingArtifactId
                );

        populateModel(
                model,
                projectId,
                findings,
                selectedFindingArtifactId,
                state
        );

        return "admin/digital-transformation/strategic-intelligence/workspace";
    }

    private Long resolveSelectedFinding(
            List<StrategicFindingOptionResponse> findings,
            Long requestedFindingArtifactId
    ) {
        /*
         * No hay findings.
         *
         * No existe todavía un contexto estratégico que consultar.
         */
        if (findings.isEmpty()) {
            if (requestedFindingArtifactId != null) {
                throw new IllegalArgumentException(
                        "El findingArtifactId solicitado no pertenece al proyecto"
                );
            }

            return null;
        }

        /*
         * Si el cliente especificó un finding,
         * debe existir dentro de la selección tenant-safe.
         */
        if (requestedFindingArtifactId != null) {

            ensureFindingBelongsToSelection(
                    findings,
                    requestedFindingArtifactId
            );

            return requestedFindingArtifactId;
        }

        /*
         * Un único finding puede seleccionarse
         * automáticamente sin ambigüedad.
         */
        if (findings.size() == 1) {

            Long findingId =
                    findings.get(0).id();

            requirePositive(
                    findingId,
                    "findingId"
            );

            return findingId;
        }

        /*
         * Hay varios findings.
         *
         * El usuario debe seleccionar explícitamente uno.
         */
        return null;
    }

    private StrategicIntelligenceStateResponse resolveState(
            Long storeId,
            Long projectId,
            Long selectedFindingArtifactId
    ) {
        if (selectedFindingArtifactId == null) {
            return null;
        }

        return Objects.requireNonNull(
                stateQuery.findState(
                        storeId,
                        projectId,
                        selectedFindingArtifactId
                ),
                "StrategicIntelligenceStateQuery devolvió un estado nulo"
        );
    }

    private void populateModel(
            Model model,
            Long projectId,
            List<StrategicFindingOptionResponse> findings,
            Long selectedFindingArtifactId,
            StrategicIntelligenceStateResponse state
    ) {
        Objects.requireNonNull(
                model,
                "Model es obligatorio"
        );

        model.addAttribute(
                "pageTitle",
                "Strategic Intelligence"
        );

        model.addAttribute(
                "pageDescription",
                "Análisis, síntesis, evidencia y gobernanza estratégica"
        );

        model.addAttribute(
                "projectId",
                projectId
        );

        model.addAttribute(
                "findings",
                findings
        );

        model.addAttribute(
                "findingArtifactId",
                selectedFindingArtifactId
        );

        model.addAttribute(
                "strategicIntelligence",
                state
        );

        model.addAttribute(
                "hasFindings",
                !findings.isEmpty()
        );

        model.addAttribute(
                "requiresFindingSelection",
                findings.size() > 1
                        && selectedFindingArtifactId == null
        );

        model.addAttribute(
                "strategicIntelligencePage",
                true
        );
    }

    private Store resolveStore(
            HttpServletRequest request
    ) {
        Objects.requireNonNull(
                request,
                "HttpServletRequest es obligatorio"
        );

        final Store store;

        try {
            store =
                    storeResolver.getCurrentStore(
                            request
                    );

        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                    "No fue posible resolver la tienda de la solicitud",
                    exception
            );
        }

        if (store == null || store.getId() == null) {
            throw new IllegalStateException(
                    "No fue posible resolver la tienda de la solicitud"
            );
        }

        if (!store.isActiva()) {
            throw new IllegalStateException(
                    "La tienda se encuentra inactiva"
            );
        }

        return store;
    }

    private static void requirePositive(
            Long value,
            String name
    ) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(
                    name + " debe ser válido"
            );
        }
    }
}