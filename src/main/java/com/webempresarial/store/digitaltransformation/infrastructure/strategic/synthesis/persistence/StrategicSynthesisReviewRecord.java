package com.webempresarial.store.digitaltransformation.infrastructure.strategic.synthesis.persistence;

import com.webempresarial.store.digitaltransformation.application.strategic.synthesis.StoredStrategicSynthesisReview;
import com.webempresarial.store.digitaltransformation.domain.strategic.synthesis.*;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(
        name = "transformation_strategic_synthesis_reviews",
        indexes = {
                @Index(
                        name = "idx_strategic_synthesis_review_reviewed",
                        columnList = "reviewed_synthesis_id,reviewed_at"
                ),
                @Index(
                        name = "idx_strategic_synthesis_review_resulting",
                        columnList = "resulting_synthesis_id"
                ),
                @Index(
                        name = "idx_strategic_synthesis_review_reviewer",
                        columnList = "reviewer"
                )
        }
)
public class StrategicSynthesisReviewRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "reviewed_synthesis_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_strategic_review_reviewed_synthesis"
            )
    )
    private StrategicSynthesisRecord reviewedSynthesis;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "resulting_synthesis_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_strategic_review_resulting_synthesis"
            )
    )
    private StrategicSynthesisRecord resultingSynthesis;

    @Column(
            nullable = false,
            length = 180
    )
    private String reviewer;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "reviewer_type",
            nullable = false,
            length = 40
    )
    private StrategicSynthesisReviewerType reviewerType;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private StrategicSynthesisReviewDecision decision;

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String reason;

    @Column(
            name = "reviewed_at",
            nullable = false,
            updatable = false
    )
    private java.time.Instant reviewedAt;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "previous_status",
            nullable = false,
            length = 30
    )
    private StrategicSynthesisStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "resulting_status",
            nullable = false,
            length = 30
    )
    private StrategicSynthesisStatus resultingStatus;

    protected StrategicSynthesisReviewRecord() {
    }

    private StrategicSynthesisReviewRecord(
            StrategicSynthesisRecord reviewedSynthesis,
            StrategicSynthesisRecord resultingSynthesis,
            StrategicSynthesisReview review
    ) {
        this.reviewedSynthesis =
                Objects.requireNonNull(
                        reviewedSynthesis,
                        "La síntesis revisada es obligatoria"
                );

        this.resultingSynthesis =
                Objects.requireNonNull(
                        resultingSynthesis,
                        "La síntesis resultante es obligatoria"
                );

        Objects.requireNonNull(
                review,
                "La revisión estratégica es obligatoria"
        );

        ensureSameProject(
                reviewedSynthesis,
                resultingSynthesis
        );

        ensureReviewCorrespondsToReviewedSnapshot(
                reviewedSynthesis,
                review
        );

        ensureResultingSnapshotMatchesReview(
                resultingSynthesis,
                review
        );

        this.reviewer =
                review.getReviewer();

        this.reviewerType =
                review.getReviewerType();

        this.decision =
                review.getDecision();

        this.reason =
                review.getReason();

        this.reviewedAt =
                review.getReviewedAt();

        this.previousStatus =
                review.getPreviousStatus();

        this.resultingStatus =
                review.getResultingStatus();
    }

    public static StrategicSynthesisReviewRecord from(
            StrategicSynthesisRecord reviewedSynthesis,
            StrategicSynthesisRecord resultingSynthesis,
            StrategicSynthesisReview review
    ) {
        return new StrategicSynthesisReviewRecord(
                reviewedSynthesis,
                resultingSynthesis,
                review
        );
    }

    public StoredStrategicSynthesisReview toStoredReview() {
        if (id == null) {
            throw new IllegalStateException(
                    "El StrategicSynthesisReviewRecord todavía no está persistido"
            );
        }

        if (reviewedSynthesis.getId() == null) {
            throw new IllegalStateException(
                    "La síntesis revisada no tiene identidad persistente"
            );
        }

        if (resultingSynthesis.getId() == null) {
            throw new IllegalStateException(
                    "La síntesis resultante no tiene identidad persistente"
            );
        }

        StrategicSynthesis reviewedDomain =
                reviewedSynthesis.toDomain();

        StrategicSynthesisReview review =
                StrategicSynthesisReview.record(
                        reviewedDomain,
                        reviewer,
                        reviewerType,
                        decision,
                        reason,
                        reviewedAt
                );

        /*
         * Fail closed ante datos persistidos incoherentes.
         */
        if (review.getPreviousStatus() != previousStatus) {
            throw new IllegalStateException(
                    "El previousStatus persistido no coincide con la revisión"
            );
        }

        if (review.getResultingStatus() != resultingStatus) {
            throw new IllegalStateException(
                    "El resultingStatus persistido no coincide con la revisión"
            );
        }

        if (resultingSynthesis.getStatus() != resultingStatus) {
            throw new IllegalStateException(
                    "El snapshot resultante no coincide con el estado de la revisión"
            );
        }

        return new StoredStrategicSynthesisReview(
                id,
                reviewedSynthesis.getId(),
                resultingSynthesis.getId(),
                review
        );
    }

    private static void ensureSameProject(
            StrategicSynthesisRecord first,
            StrategicSynthesisRecord second
    ) {
        if (first.getProject() == second.getProject()) {
            return;
        }

        if (first.getProject() == null
                || second.getProject() == null) {
            throw new IllegalArgumentException(
                    "Los snapshots deben pertenecer al mismo proyecto"
            );
        }

        Long firstId =
                first.getProject().getId();

        Long secondId =
                second.getProject().getId();

        if (firstId == null
                || secondId == null
                || !firstId.equals(secondId)) {

            throw new IllegalArgumentException(
                    "Los snapshots deben pertenecer al mismo proyecto"
            );
        }
    }

    private static void ensureReviewCorrespondsToReviewedSnapshot(
            StrategicSynthesisRecord reviewedSynthesis,
            StrategicSynthesisReview review
    ) {
        StrategicSynthesis persisted =
                reviewedSynthesis.toDomain();

        StrategicSynthesis reviewed =
                review.getSynthesis();

        if (persisted.getStatus()
                != reviewed.getStatus()) {

            throw new IllegalArgumentException(
                    "La revisión no corresponde al estado del snapshot revisado"
            );
        }

        if (!Objects.equals(
                persisted.getStrategicThesis(),
                reviewed.getStrategicThesis()
        )) {
            throw new IllegalArgumentException(
                    "La revisión no corresponde a la síntesis persistida"
            );
        }

        if (!persisted.getSourceArtifactCodes()
                .equals(
                        reviewed.getSourceArtifactCodes()
                )) {

            throw new IllegalArgumentException(
                    "La revisión no corresponde a los artefactos del snapshot"
            );
        }
    }

    private static void ensureResultingSnapshotMatchesReview(
            StrategicSynthesisRecord resultingSynthesis,
            StrategicSynthesisReview review
    ) {
        if (resultingSynthesis.getStatus()
                != review.getResultingStatus()) {

            throw new IllegalArgumentException(
                    "El snapshot resultante no coincide con el resultado de la revisión"
            );
        }
    }

    public Long getId() {
        return id;
    }

    public StrategicSynthesisRecord getReviewedSynthesis() {
        return reviewedSynthesis;
    }

    public StrategicSynthesisRecord getResultingSynthesis() {
        return resultingSynthesis;
    }

    public String getReviewer() {
        return reviewer;
    }

    public StrategicSynthesisReviewerType getReviewerType() {
        return reviewerType;
    }

    public StrategicSynthesisReviewDecision getDecision() {
        return decision;
    }

    public String getReason() {
        return reason;
    }

    public java.time.Instant getReviewedAt() {
        return reviewedAt;
    }

    public StrategicSynthesisStatus getPreviousStatus() {
        return previousStatus;
    }

    public StrategicSynthesisStatus getResultingStatus() {
        return resultingStatus;
    }
}