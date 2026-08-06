package com.webempresarial.store.digitaltransformation.domain.project;

import com.webempresarial.store.model.Store;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "transformation_projects",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_transformation_project_store_code",
                        columnNames = {"store_id", "code"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_transformation_project_store_status",
                        columnList = "store_id,status"
                ),
                @Index(
                        name = "idx_transformation_project_created_at",
                        columnList = "created_at"
                )
        }
)
public class TransformationProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "store_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_transformation_project_store"
            )
    )
    private Store store;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(name = "client_name", nullable = false, length = 180)
    private String clientName;

    @Column(name = "client_website", length = 500)
    private String clientWebsite;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_type", nullable = false, length = 60)
    private TransformationProjectType projectType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private TransformationProjectStatus status;

    @Column(name = "executive_intent", columnDefinition = "TEXT")
    private String executiveIntent;

    @Column(name = "source_of_truth_locked", nullable = false)
    private boolean sourceOfTruthLocked;

    @Column(name = "current_blueprint_version")
    private Integer currentBlueprintVersion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected TransformationProject() {
    }

    private TransformationProject(
            Store store,
            String code,
            String name,
            String clientName,
            String clientWebsite,
            TransformationProjectType projectType,
            String executiveIntent
    ) {
        this.store = requireStore(store);
        this.code = normalizeRequired(code, "El código del proyecto es obligatorio", 50);
        this.name = normalizeRequired(name, "El nombre del proyecto es obligatorio", 180);
        this.clientName = normalizeRequired(
                clientName,
                "El nombre del cliente es obligatorio",
                180
        );
        this.clientWebsite = normalizeOptional(clientWebsite, 500);
        this.projectType = Objects.requireNonNull(
                projectType,
                "El tipo de proyecto es obligatorio"
        );
        this.executiveIntent = normalizeOptional(executiveIntent, 10_000);
        this.status = TransformationProjectStatus.CREATED;
        this.sourceOfTruthLocked = false;
        this.currentBlueprintVersion = null;
    }

    public static TransformationProject create(
            Store store,
            String code,
            String name,
            String clientName,
            String clientWebsite,
            TransformationProjectType projectType,
            String executiveIntent
    ) {
        return new TransformationProject(
                store,
                code,
                name,
                clientName,
                clientWebsite,
                projectType,
                executiveIntent
        );
    }

    public void markSourcesPending() {
        ensureSourcesCanBeModified();

        if (status == TransformationProjectStatus.CREATED) {
            this.status = TransformationProjectStatus.SOURCES_PENDING;
            return;
        }

        if (status != TransformationProjectStatus.SOURCES_PENDING) {
            throw new IllegalStateException(
                    "El proyecto no puede pasar a SOURCES_PENDING " +
                    "desde el estado " + status
            );
        }
    }
    public void markSourcesIngested() {
        if (status != TransformationProjectStatus.CREATED
                && status != TransformationProjectStatus.SOURCES_PENDING) {
            throw new IllegalStateException(
                    "Las fuentes solo pueden marcarse como ingeridas " +
                    "desde CREATED o SOURCES_PENDING"
            );
        }

        this.status = TransformationProjectStatus.SOURCES_INGESTED;
    }

    public void lockSourceOfTruth() {
        if (status != TransformationProjectStatus.SOURCES_INGESTED) {
            throw new IllegalStateException(
                    "La fuente de verdad solo puede bloquearse " +
                    "cuando los documentos han sido ingeridos"
            );
        }

        this.sourceOfTruthLocked = true;
    }

    public void registerBlueprintVersion(int version) {
        if (version < 1) {
            throw new IllegalArgumentException(
                    "La versión del blueprint debe ser mayor o igual a 1"
            );
        }

        this.currentBlueprintVersion = version;
    }

    public void updateExecutiveIntent(String executiveIntent) {
        ensureSourcesAreNotLocked();

        this.executiveIntent = normalizeOptional(
                executiveIntent,
                10_000
        );
    }

    public void complete() {
        if (status == TransformationProjectStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Un proyecto cancelado no puede completarse"
            );
        }

        this.status = TransformationProjectStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void cancel() {
        if (status == TransformationProjectStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Un proyecto completado no puede cancelarse"
            );
        }

        this.status = TransformationProjectStatus.CANCELLED;
    }

    private void ensureSourcesAreNotLocked() {
        ensureSourcesCanBeModified();
    }

    private void requireStatus(TransformationProjectStatus expected) {
        if (status != expected) {
            throw new IllegalStateException(
                    "Estado inválido. Se esperaba " + expected +
                    " pero el proyecto se encuentra en " + status
            );
        }
    }

    private static Store requireStore(Store store) {
        if (store == null || store.getId() == null || store.getId() <= 0) {
            throw new IllegalArgumentException(
                    "El store debe ser válido y persistido"
            );
        }

        return store;
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

    public void ensureSourcesCanBeModified() {
        if (sourceOfTruthLocked) {
            throw new IllegalStateException(
                    "Las fuentes de verdad del proyecto están bloqueadas"
            );
        }

        if (status == TransformationProjectStatus.COMPLETED
                || status == TransformationProjectStatus.CANCELLED) {
            throw new IllegalStateException(
                    "No se pueden modificar las fuentes de un proyecto " +
                    "en estado " + status
            );
        }
    }

    public void ensureBelongsToStore(Long storeId) {
        if (storeId == null || storeId <= 0) {
            throw new IllegalArgumentException(
                    "El storeId debe ser válido"
            );
        }

        if (store == null
                || store.getId() == null
                || !store.getId().equals(storeId)) {
            throw new IllegalArgumentException(
                    "El proyecto no pertenece al store indicado"
            );
        }
    }

    public boolean canReceiveSources() {
        return !sourceOfTruthLocked
                && status != TransformationProjectStatus.COMPLETED
                && status != TransformationProjectStatus.CANCELLED;
    }
    
    @PrePersist
    void onCreate() {
        Instant now = Instant.now();

        this.createdAt = now;
        this.updatedAt = now;

        if (status == null) {
            this.status = TransformationProjectStatus.CREATED;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Store getStore() {
        return store;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getClientName() {
        return clientName;
    }

    public String getClientWebsite() {
        return clientWebsite;
    }

    public TransformationProjectType getProjectType() {
        return projectType;
    }

    public TransformationProjectStatus getStatus() {
        return status;
    }

    public String getExecutiveIntent() {
        return executiveIntent;
    }

    public boolean isSourceOfTruthLocked() {
        return sourceOfTruthLocked;
    }

    public Integer getCurrentBlueprintVersion() {
        return currentBlueprintVersion;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}