package com.webempresarial.store.knowledge.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KnowledgeTypeCodeTest {

    @Test
    void shouldIdentifyGovernanceTypes() {
        assertTrue(KnowledgeTypeCode.POLICY.isGovernanceType());
        assertTrue(KnowledgeTypeCode.STANDARD.isGovernanceType());
        assertTrue(KnowledgeTypeCode.PRINCIPLE.isGovernanceType());
        assertTrue(KnowledgeTypeCode.DECISION.isGovernanceType());

        assertFalse(KnowledgeTypeCode.PLAYBOOK.isGovernanceType());
    }

    @Test
    void shouldIdentifyOperationalTypes() {
        assertTrue(KnowledgeTypeCode.PROCEDURE.isOperationalType());
        assertTrue(KnowledgeTypeCode.GUIDELINE.isOperationalType());
        assertTrue(KnowledgeTypeCode.PLAYBOOK.isOperationalType());
        assertTrue(KnowledgeTypeCode.TEMPLATE.isOperationalType());

        assertFalse(KnowledgeTypeCode.ARCHITECTURE.isOperationalType());
    }

    @Test
    void shouldIdentifyStructuralTypes() {
        assertTrue(KnowledgeTypeCode.ARCHITECTURE.isStructuralType());
        assertTrue(KnowledgeTypeCode.FRAMEWORK.isStructuralType());
        assertTrue(KnowledgeTypeCode.BLUEPRINT.isStructuralType());
        assertTrue(KnowledgeTypeCode.SPECIFICATION.isStructuralType());

        assertFalse(KnowledgeTypeCode.POLICY.isStructuralType());
    }
}