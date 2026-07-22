package com.webempresarial.store.knowledge.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KnowledgeDomainTest {

    @Test
    void shouldIdentifyTechnologyDomains() {
        assertTrue(KnowledgeDomain.ARCHITECTURE.isTechnologyDomain());
        assertTrue(KnowledgeDomain.ENGINEERING.isTechnologyDomain());
        assertTrue(KnowledgeDomain.SECURITY.isTechnologyDomain());
        assertTrue(KnowledgeDomain.DATA.isTechnologyDomain());
        assertTrue(KnowledgeDomain.PLATFORM.isTechnologyDomain());

        assertFalse(KnowledgeDomain.SALES.isTechnologyDomain());
    }

    @Test
    void shouldIdentifyCommercialDomains() {
        assertTrue(KnowledgeDomain.SALES.isCommercialDomain());
        assertTrue(KnowledgeDomain.MARKETING.isCommercialDomain());
        assertTrue(KnowledgeDomain.CRM.isCommercialDomain());
        assertTrue(KnowledgeDomain.COMMERCE.isCommercialDomain());

        assertFalse(KnowledgeDomain.ENGINEERING.isCommercialDomain());
    }

    @Test
    void shouldIdentifyGovernanceDomains() {
        assertTrue(KnowledgeDomain.GOVERNANCE.isGovernanceDomain());
        assertTrue(KnowledgeDomain.LEGAL.isGovernanceDomain());
        assertTrue(KnowledgeDomain.FINANCE.isGovernanceDomain());
        assertTrue(KnowledgeDomain.SECURITY.isGovernanceDomain());

        assertFalse(KnowledgeDomain.MARKETING.isGovernanceDomain());
    }
}