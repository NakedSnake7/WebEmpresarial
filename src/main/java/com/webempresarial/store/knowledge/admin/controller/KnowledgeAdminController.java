package com.webempresarial.store.knowledge.admin.controller;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeClassification;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeDomain;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeRiskLevel;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeStatus;
import com.webempresarial.store.knowledge.domain.enums.KnowledgeTypeCode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/knowledge")
public class KnowledgeAdminController {

    /*
     * =========================================================
     * MODULE DASHBOARD
     * =========================================================
     */

    @GetMapping
    public String dashboard(
            Model model
    ) {
        model.addAttribute(
                "pageTitle",
                "Knowledge Engine"
        );

        model.addAttribute(
                "pageDescription",
                "Gobernanza, evolución y administración del conocimiento empresarial"
        );

        return "admin/knowledge/dashboard";
    }

    /*
     * =========================================================
     * KNOWLEDGE LIBRARY
     * =========================================================
     */

    @GetMapping("/library")
    public String library(
            @RequestParam(
                    name = "status",
                    required = false
            )
            KnowledgeStatus initialStatus,

            Model model
    ) {
        model.addAttribute(
                "pageTitle",
                resolveLibraryTitle(initialStatus)
        );

        model.addAttribute(
                "pageDescription",
                resolveLibraryDescription(initialStatus)
        );

        model.addAttribute(
                "initialStatus",
                initialStatus
        );

        addKnowledgeCatalogs(model);

        return "admin/knowledge/index";
    }

    /*
     * =========================================================
     * CREATE KNOWLEDGE
     * =========================================================
     */

    @GetMapping("/new")
    public String createForm(
            Model model
    ) {
        model.addAttribute(
                "pageTitle",
                "Nuevo conocimiento"
        );

        model.addAttribute(
                "pageDescription",
                "Registrar un objeto de conocimiento y su versión editorial inicial"
        );

        addKnowledgeCatalogs(model);

        return "admin/knowledge/create";
    }

    /*
     * =========================================================
     * CREATE VERSION
     * =========================================================
     */

    @GetMapping("/{knowledgeObjectId}/versions/new")
    public String createVersionForm(
            @PathVariable
            Long knowledgeObjectId,

            Model model
    ) {
        validateIdentifier(
                knowledgeObjectId,
                "knowledgeObjectId"
        );

        model.addAttribute(
                "pageTitle",
                "Nueva versión"
        );

        model.addAttribute(
                "pageDescription",
                "Crear una nueva versión editorial del conocimiento"
        );

        model.addAttribute(
                "knowledgeObjectId",
                knowledgeObjectId
        );

        return "admin/knowledge/version-create";
    }

    /*
     * =========================================================
     * KNOWLEDGE WORKSPACE
     * =========================================================
     */

    @GetMapping("/{knowledgeObjectId}")
    public String detail(
            @PathVariable
            Long knowledgeObjectId,

            Model model
    ) {
        validateIdentifier(
                knowledgeObjectId,
                "knowledgeObjectId"
        );

        model.addAttribute(
                "pageTitle",
                "Knowledge Workspace"
        );

        model.addAttribute(
                "pageDescription",
                "Lectura, gobernanza y evolución del conocimiento"
        );

        model.addAttribute(
                "knowledgeObjectId",
                knowledgeObjectId
        );

        return "admin/knowledge/detail";
    }

    /*
     * =========================================================
     * HISTORICAL VERSION
     * =========================================================
     */

    @GetMapping("/{knowledgeObjectId}/versions/{versionId}")
    public String versionDetail(
            @PathVariable
            Long knowledgeObjectId,

            @PathVariable
            Long versionId,

            Model model
    ) {
        validateIdentifier(
                knowledgeObjectId,
                "knowledgeObjectId"
        );

        validateIdentifier(
                versionId,
                "versionId"
        );

        model.addAttribute(
                "pageTitle",
                "Versión histórica"
        );

        model.addAttribute(
                "pageDescription",
                "Vista editorial de una versión específica"
        );

        model.addAttribute(
                "knowledgeObjectId",
                knowledgeObjectId
        );

        model.addAttribute(
                "versionId",
                versionId
        );

        return "admin/knowledge/version-detail";
    }

    /*
     * =========================================================
     * SHARED MODEL DATA
     * =========================================================
     */

    private void addKnowledgeCatalogs(
            Model model
    ) {
        model.addAttribute(
                "knowledgeStatuses",
                KnowledgeStatus.values()
        );

        model.addAttribute(
                "knowledgeTypes",
                KnowledgeTypeCode.values()
        );

        model.addAttribute(
                "knowledgeDomains",
                KnowledgeDomain.values()
        );

        model.addAttribute(
                "knowledgeClassifications",
                KnowledgeClassification.values()
        );

        model.addAttribute(
                "knowledgeRiskLevels",
                KnowledgeRiskLevel.values()
        );

        model.addAttribute(
                "knowledgeContextTypes",
                KnowledgeContextType.values()
        );
    }

    private String resolveLibraryTitle(
            KnowledgeStatus status
    ) {
        if (status == KnowledgeStatus.IN_REVIEW) {
            return "Conocimiento en revisión";
        }

        if (status == KnowledgeStatus.PUBLISHED) {
            return "Conocimiento publicado";
        }

        if (status == KnowledgeStatus.DRAFT) {
            return "Borradores de conocimiento";
        }

        if (status == KnowledgeStatus.APPROVED) {
            return "Conocimiento aprobado";
        }

        if (status == KnowledgeStatus.ARCHIVED) {
            return "Conocimiento archivado";
        }

        return "Biblioteca de conocimiento";
    }

    private String resolveLibraryDescription(
            KnowledgeStatus status
    ) {
        if (status == KnowledgeStatus.IN_REVIEW) {
            return "Objetos pendientes de validación editorial";
        }

        if (status == KnowledgeStatus.PUBLISHED) {
            return "Conocimiento vigente disponible para la organización";
        }

        if (status == KnowledgeStatus.DRAFT) {
            return "Contenido editorial que continúa en preparación";
        }

        if (status == KnowledgeStatus.APPROVED) {
            return "Conocimiento validado y listo para publicación";
        }

        if (status == KnowledgeStatus.ARCHIVED) {
            return "Conocimiento retirado de las consultas vigentes";
        }

        return "Consulta y administra el patrimonio intelectual de la organización";
    }

    private void validateIdentifier(
            Long value,
            String fieldName
    ) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(
                    "El " + fieldName + " debe ser válido"
            );
        }
    }
}