package com.webempresarial.store.digitaltransformation.domain.traceability;

public enum TraceabilityRelationType {

    ORIGINATES_FROM,
    SUPPORTED_BY,
    DERIVED_FROM,
    INFERRED_FROM,

    ADDRESSES,
    SATISFIES,
    IMPLEMENTS,
    VALIDATES,

    CONFLICTS_WITH,
    REFINES,
    REPLACES,
    SUPERSEDES,

    DEPENDS_ON,
    BLOCKS,
    ENABLES,

    PART_OF,
    GROUPS,
    REFERENCES,

    TRANSLATES_TO,
    MATERIALIZES_AS,
    TESTED_BY,

    RELATED_TO
}