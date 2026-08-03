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

@Controller
@RequestMapping("/admin/knowledge")
public class KnowledgeAdminController {

    @GetMapping
    public String index(
            Model model
    ) {
        model.addAttribute(
                "pageTitle",
                "Knowledge Engine"
        );

        model.addAttribute(
                "pageDescription",
                "Administración del conocimiento empresarial"
        );

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

        return "admin/knowledge/index";
    }
    
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
                "Registrar un nuevo objeto de conocimiento y su versión inicial"
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

        return "admin/knowledge/create";
    }
    
    @GetMapping("/{knowledgeObjectId}/versions/new")
    public String createVersionForm(
            @PathVariable Long knowledgeObjectId,
            Model model
    ) {
        if (knowledgeObjectId == null || knowledgeObjectId <= 0) {
            throw new IllegalArgumentException(
                    "El knowledgeObjectId debe ser válido"
            );
        }

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
    
    
    @GetMapping("/{knowledgeObjectId}")
    public String detail(
            @PathVariable
            Long knowledgeObjectId,
            Model model
    ) {
        model.addAttribute(
                "pageTitle",
                "Detalle de conocimiento"
        );

        model.addAttribute(
                "knowledgeObjectId",
                knowledgeObjectId
        );

        return "admin/knowledge/detail";
    }
    
}