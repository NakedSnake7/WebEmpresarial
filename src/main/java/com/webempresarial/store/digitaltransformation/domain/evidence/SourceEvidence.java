package com.webempresarial.store.digitaltransformation.domain.evidence;

import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import com.webempresarial.store.digitaltransformation.domain.source.SourceDocumentSection;
import com.webempresarial.store.digitaltransformation.domain.source.TransformationSourceDocument;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "transformation_source_evidence",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_transformation_evidence_project_code",
                        columnNames = {
                                "project_id",
                                "evidence_code"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_transformation_evidence_project_status",
                        columnList = "project_id,status"
                ),
                @Index(
                        name = "idx_transformation_evidence_classification",
                        columnList = "project_id,classification"
                ),
                @Index(
                        name = "idx_transformation_evidence_source",
                        columnList = "source_document_id"
                ),
                @Index(
                        name = "idx_transformation_evidence_page",
                        columnList = "source_document_id,page_from,page_to"
                )
        }
)
public class SourceEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "project_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_transformation_evidence_project"
            )
    )
    private TransformationProject project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "source_document_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_transformation_evidence_document"
            )
    )
    private TransformationSourceDocument sourceDocument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "source_section_id",
            foreignKey = @ForeignKey(
                    name = "fk_transformation_evidence_section"
            )
    )
    private SourceDocumentSection sourceSection;

    @Column(
            name = "evidence_code",
            nullable = false,
            length = 80
    )
    private String evidenceCode;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "classification",
            nullable = false,
            length = 60
    )
    private EvidenceClassification classification;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "confidence",
            nullable = false,
            length = 40
    )
    private EvidenceConfidence confidence;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "extraction_origin",
            nullable = false,
            length = 40
    )
    private EvidenceExtractionOrigin extractionOrigin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EvidenceStatus status;

    @Column(nullable = false, length = 1000)
    private String statement;

    @Lob
    @Column(
            name = "supporting_excerpt",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String supportingExcerpt;

    @Column(name = "interpretation", length = 4000)
    private String interpretation;

    @Embedded
    private EvidenceLocator locator;

    @Column(name = "requires_human_review", nullable = false)
    private boolean requiresHumanReview;

    @Column(name = "verified_by", length = 180)
    private String verifiedBy;

    @Column(name = "rejection_reason", length = 2000)
    private String rejectionReason;

    @Column(name = "extracted_at", nullable = false, updatable = false)
    private Instant extractedAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    protected SourceEvidence() {
    }

    private SourceEvidence(
            TransformationProject project,
            TransformationSourceDocument sourceDocument,
            SourceDocumentSection sourceSection,
            String evidenceCode,
            EvidenceClassification classification,
            EvidenceConfidence confidence,
            EvidenceExtractionOrigin extractionOrigin,
            String statement,
            String supportingExcerpt,
            String interpretation,
            EvidenceLocator locator
    ) {
        this.project = Objects.requireNonNull(
                project,
                "El proyecto es obligatorio"
        );

        this.sourceDocument = Objects.requireNonNull(
                sourceDocument,
                "El documento fuente es obligatorio"
        );

        validateRelationships(
                project,
                sourceDocument,
                sourceSection
        );

        this.sourceSection = sourceSection;
        this.evidenceCode = normalizeCode(evidenceCode);

        this.classification = Objects.requireNonNull(
                classification,
                "La clasificación es obligatoria"
        );

        this.confidence = Objects.requireNonNull(
                confidence,
                "La confianza es obligatoria"
        );

        this.extractionOrigin = Objects.requireNonNull(
                extractionOrigin,
                "El origen de extracción es obligatorio"
        );

        this.statement = normalizeRequired(
                statement,
                "La afirmación es obligatoria",
                1000
        );

        this.supportingExcerpt =
                normalizeRequiredText(
                        supportingExcerpt,
                        "El fragmento respaldatorio es obligatorio"
                );

        this.interpretation =
                normalizeOptional(interpretation, 4000);

        this.locator = Objects.requireNonNull(
                locator,
                "El localizador de la evidencia es obligatorio"
        );

        this.status = EvidenceStatus.EXTRACTED;

        this.requiresHumanReview =
                confidence == EvidenceConfidence.INFERRED
                || confidence == EvidenceConfidence.WEAKLY_SUPPORTED
                || confidence == EvidenceConfidence.UNCERTAIN
                || extractionOrigin == EvidenceExtractionOrigin.AI_ASSISTED;
    }

    public static SourceEvidence extract(
            TransformationProject project,
            TransformationSourceDocument sourceDocument,
            SourceDocumentSection sourceSection,
            String evidenceCode,
            EvidenceClassification classification,
            EvidenceConfidence confidence,
            EvidenceExtractionOrigin extractionOrigin,
            String statement,
            String supportingExcerpt,
            String interpretation,
            EvidenceLocator locator
    ) {
        return new SourceEvidence(
                project,
                sourceDocument,
                sourceSection,
                evidenceCode,
                classification,
                confidence,
                extractionOrigin,
                statement,
                supportingExcerpt,
                interpretation,
                locator
        );
    }

    public void requireReview() {
        if (status != EvidenceStatus.EXTRACTED) {
            throw new IllegalStateException(
                    "Solo una evidencia extraída puede enviarse a revisión"
            );
        }

        this.status = EvidenceStatus.REVIEW_REQUIRED;
        this.requiresHumanReview = true;
    }

    public void verify(String verifiedBy) {
        if (status != EvidenceStatus.EXTRACTED
                && status != EvidenceStatus.REVIEW_REQUIRED) {
            throw new IllegalStateException(
                    "La evidencia no puede verificarse desde el estado " +
                    status
            );
        }

        this.verifiedBy = normalizeRequired(
                verifiedBy,
                "El responsable de verificación es obligatorio",
                180
        );

        this.status = EvidenceStatus.VERIFIED;
        this.requiresHumanReview = false;
        this.verifiedAt = Instant.now();
        this.rejectionReason = null;
        this.rejectedAt = null;
    }

    public void reject(
            String reason,
            String reviewedBy
    ) {
        if (status == EvidenceStatus.ARCHIVED
                || status == EvidenceStatus.SUPERSEDED) {
            throw new IllegalStateException(
                    "La evidencia no puede rechazarse desde el estado " +
                    status
            );
        }

        this.rejectionReason = normalizeRequired(
                reason,
                "La razón del rechazo es obligatoria",
                2000
        );

        this.verifiedBy = normalizeRequired(
                reviewedBy,
                "El responsable de revisión es obligatorio",
                180
        );

        this.status = EvidenceStatus.REJECTED;
        this.requiresHumanReview = false;
        this.rejectedAt = Instant.now();
    }

    public void supersede() {
        if (status != EvidenceStatus.VERIFIED) {
            throw new IllegalStateException(
                    "Solo una evidencia verificada puede ser sustituida"
            );
        }

        this.status = EvidenceStatus.SUPERSEDED;
    }

    public void archive() {
        this.status = EvidenceStatus.ARCHIVED;
        this.requiresHumanReview = false;
    }

    public boolean canGenerateRequirements() {
        return status == EvidenceStatus.VERIFIED
                && confidence != EvidenceConfidence.UNCERTAIN;
    }

    public void ensureBelongsToStore(Long storeId) {
        if (storeId == null || storeId <= 0) {
            throw new IllegalArgumentException(
                    "El storeId debe ser válido"
            );
        }

        if (project.getStore() == null
                || project.getStore().getId() == null
                || !project.getStore().getId().equals(storeId)) {
            throw new IllegalArgumentException(
                    "La evidencia no pertenece al store indicado"
            );
        }
    }

    private static void validateRelationships(
            TransformationProject project,
            TransformationSourceDocument sourceDocument,
            SourceDocumentSection sourceSection
    ) {
        TransformationProject sourceProject =
                sourceDocument.getProject();

        boolean sameProject;

        if (project.getId() != null
                && sourceProject != null
                && sourceProject.getId() != null) {
            sameProject = project.getId()
                    .equals(sourceProject.getId());
        } else {
            sameProject = project == sourceProject;
        }

        if (!sameProject) {
            throw new IllegalArgumentException(
                    "El documento no pertenece al proyecto indicado"
            );
        }

        if (sourceSection == null) {
            return;
        }

        TransformationSourceDocument sectionDocument =
                sourceSection.getSourceContent()
                        .getSourceDocument();

        boolean sameDocument;

        if (sourceDocument.getId() != null
                && sectionDocument != null
                && sectionDocument.getId() != null) {
            sameDocument = sourceDocument.getId()
                    .equals(sectionDocument.getId());
        } else {
            sameDocument = sourceDocument == sectionDocument;
        }

        if (!sameDocument) {
            throw new IllegalArgumentException(
                    "La sección no pertenece al documento indicado"
            );
        }
    }

    private static String normalizeCode(String code) {
        String normalized = normalizeRequired(
                code,
                "El código de evidencia es obligatorio",
                80
        ).toUpperCase();

        if (!normalized.matches(
                "^[A-Z0-9][A-Z0-9_-]{2,79}$"
        )) {
            throw new IllegalArgumentException(
                    "El código de evidencia no tiene un formato válido"
            );
        }

        return normalized;
    }

    private static String normalizeRequiredText(
            String value,
            String message
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }

    private static String normalizeRequired(
            String value,
            String message,
            int maxLength
    ) {
        String normalized = normalizeOptional(value, maxLength);

        if (normalized == null) {
            throw new IllegalArgumentException(message);
        }

        return normalized;
    }

    private static String normalizeOptional(
            String value,
            int maxLength
    ) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        if (normalized.isBlank()) {
            return null;
        }

        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(
                    "El valor supera la longitud máxima de " +
                    maxLength + " caracteres"
            );
        }

        return normalized;
    }

    @PrePersist
    void onCreate() {
        this.extractedAt = Instant.now();

        if (status == null) {
            this.status = EvidenceStatus.EXTRACTED;
        }
    }

    public Long getId() {
        return id;
    }

    public TransformationProject getProject() {
        return project;
    }

    public TransformationSourceDocument getSourceDocument() {
        return sourceDocument;
    }

    public SourceDocumentSection getSourceSection() {
        return sourceSection;
    }

    public String getEvidenceCode() {
        return evidenceCode;
    }

    public EvidenceClassification getClassification() {
        return classification;
    }

    public EvidenceConfidence getConfidence() {
        return confidence;
    }

    public EvidenceExtractionOrigin getExtractionOrigin() {
        return extractionOrigin;
    }

    public EvidenceStatus getStatus() {
        return status;
    }

    public String getStatement() {
        return statement;
    }

    public String getSupportingExcerpt() {
        return supportingExcerpt;
    }

    public String getInterpretation() {
        return interpretation;
    }

    public EvidenceLocator getLocator() {
        return locator;
    }

    public boolean isRequiresHumanReview() {
        return requiresHumanReview;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public Instant getExtractedAt() {
        return extractedAt;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public Instant getRejectedAt() {
        return rejectedAt;
    }
}