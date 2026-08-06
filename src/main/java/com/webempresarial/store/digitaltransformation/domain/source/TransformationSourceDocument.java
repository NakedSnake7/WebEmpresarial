package com.webempresarial.store.digitaltransformation.domain.source;

import com.webempresarial.store.digitaltransformation.domain.project.TransformationProject;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "transformation_source_documents",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_transformation_source_project_type_version",
                        columnNames = {
                                "project_id",
                                "source_type",
                                "document_version"
                        }
                ),
                @UniqueConstraint(
                        name = "uk_transformation_source_project_checksum",
                        columnNames = {
                                "project_id",
                                "checksum_sha256"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_transformation_source_project_status",
                        columnList = "project_id,status"
                ),
                @Index(
                        name = "idx_transformation_source_type",
                        columnList = "source_type"
                )
        }
)
public class TransformationSourceDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "project_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_transformation_source_project"
            )
    )
    private TransformationProject project;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 80)
    private TransformationSourceType sourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_role", nullable = false, length = 40)
    private TransformationSourceRole sourceRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TransformationSourceStatus status;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "display_name", nullable = false, length = 255)
    private String displayName;

    @Column(name = "mime_type", nullable = false, length = 120)
    private String mimeType;

    @Column(name = "storage_reference", nullable = false, length = 1000)
    private String storageReference;

    @Column(name = "checksum_sha256", nullable = false, length = 64)
    private String checksumSha256;

    @Column(name = "document_version", nullable = false)
    private int documentVersion;

    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode;

    @Column(name = "is_authoritative", nullable = false)
    private boolean authoritative;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private Instant registeredAt;

    @Column(name = "parsed_at")
    private Instant parsedAt;

    @Column(name = "analyzed_at")
    private Instant analyzedAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    protected TransformationSourceDocument() {
    }

    private TransformationSourceDocument(
            TransformationProject project,
            TransformationSourceType sourceType,
            TransformationSourceRole sourceRole,
            String originalFilename,
            String displayName,
            String mimeType,
            String storageReference,
            String checksumSha256,
            int documentVersion,
            String languageCode,
            Integer pageCount
    ) {
        this.project = Objects.requireNonNull(
                project,
                "El proyecto de transformación es obligatorio"
        );

        this.sourceType = Objects.requireNonNull(
                sourceType,
                "El tipo de documento es obligatorio"
        );

        this.sourceRole = Objects.requireNonNull(
                sourceRole,
                "El rol del documento es obligatorio"
        );

        this.originalFilename = normalizeRequired(
                originalFilename,
                "El nombre original del archivo es obligatorio",
                255
        );

        this.displayName = normalizeRequired(
                displayName,
                "El nombre visible del documento es obligatorio",
                255
        );

        this.mimeType = normalizeRequired(
                mimeType,
                "El MIME type es obligatorio",
                120
        );

        this.storageReference = normalizeRequired(
                storageReference,
                "La referencia de almacenamiento es obligatoria",
                1000
        );

        this.checksumSha256 = normalizeChecksum(checksumSha256);

        if (documentVersion < 1) {
            throw new IllegalArgumentException(
                    "La versión del documento debe ser mayor o igual a 1"
            );
        }

        if (pageCount != null && pageCount < 1) {
            throw new IllegalArgumentException(
                    "El número de páginas debe ser mayor o igual a 1"
            );
        }

        this.documentVersion = documentVersion;
        this.languageCode = normalizeRequired(
                languageCode,
                "El idioma del documento es obligatorio",
                10
        ).toLowerCase();

        this.pageCount = pageCount;
        this.status = TransformationSourceStatus.REGISTERED;
        this.authoritative =
                sourceRole == TransformationSourceRole.SOURCE_OF_TRUTH;
    }
    
    

    public static TransformationSourceDocument register(
            TransformationProject project,
            TransformationSourceType sourceType,
            TransformationSourceRole sourceRole,
            String originalFilename,
            String displayName,
            String mimeType,
            String storageReference,
            String checksumSha256,
            int documentVersion,
            String languageCode,
            Integer pageCount
    ) {
        return new TransformationSourceDocument(
                project,
                sourceType,
                sourceRole,
                originalFilename,
                displayName,
                mimeType,
                storageReference,
                checksumSha256,
                documentVersion,
                languageCode,
                pageCount
        );
    }
    
    public void ensureBelongsToStore(Long storeId) {
        if (storeId == null || storeId <= 0) {
            throw new IllegalArgumentException(
                    "El storeId debe ser válido"
            );
        }

        if (project == null
                || project.getStore() == null
                || project.getStore().getId() == null
                || !project.getStore().getId().equals(storeId)) {
            throw new IllegalArgumentException(
                    "El documento no pertenece al store indicado"
            );
        }
    }
    
    public boolean isVerifiedAuthoritativeSource() {
        return authoritative
                && status == TransformationSourceStatus.VERIFIED;
    }

    public void markUploaded() {
        requireStatus(TransformationSourceStatus.REGISTERED);
        this.status = TransformationSourceStatus.UPLOADED;
    }

    public void markParsed() {
        if (status != TransformationSourceStatus.REGISTERED
                && status != TransformationSourceStatus.UPLOADED) {
            throw new IllegalStateException(
                    "El documento no puede marcarse como procesado " +
                    "desde el estado " + status
            );
        }

        this.status = TransformationSourceStatus.PARSED;
        this.parsedAt = Instant.now();
    }

    public void markAnalyzed() {
        requireStatus(TransformationSourceStatus.PARSED);

        this.status = TransformationSourceStatus.ANALYZED;
        this.analyzedAt = Instant.now();
    }

    public void verify() {
        requireStatus(TransformationSourceStatus.ANALYZED);

        this.status = TransformationSourceStatus.VERIFIED;
        this.verifiedAt = Instant.now();
    }

    public void reject() {
        if (status == TransformationSourceStatus.ARCHIVED) {
            throw new IllegalStateException(
                    "Un documento archivado no puede rechazarse"
            );
        }

        this.status = TransformationSourceStatus.REJECTED;
        this.authoritative = false;
    }

    public void supersede() {
        if (status != TransformationSourceStatus.VERIFIED) {
            throw new IllegalStateException(
                    "Solo un documento verificado puede ser sustituido"
            );
        }

        this.status = TransformationSourceStatus.SUPERSEDED;
        this.authoritative = false;
    }

    public void archive() {
        this.status = TransformationSourceStatus.ARCHIVED;
        this.authoritative = false;
    }

    private void requireStatus(TransformationSourceStatus expected) {
        if (status != expected) {
            throw new IllegalStateException(
                    "Estado inválido. Se esperaba " + expected +
                    " pero el documento se encuentra en " + status
            );
        }
    }

    private static String normalizeChecksum(String checksum) {
        String normalized = normalizeRequired(
                checksum,
                "El checksum SHA-256 es obligatorio",
                64
        ).toLowerCase();

        if (!normalized.matches("^[a-f0-9]{64}$")) {
            throw new IllegalArgumentException(
                    "El checksum SHA-256 debe contener exactamente " +
                    "64 caracteres hexadecimales"
            );
        }

        return normalized;
    }

    private static String normalizeRequired(
            String value,
            String message,
            int maxLength
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        String normalized = value.trim();

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
        this.registeredAt = Instant.now();

        if (status == null) {
            this.status = TransformationSourceStatus.REGISTERED;
        }
    }

    public Long getId() {
        return id;
    }

    public TransformationProject getProject() {
        return project;
    }

    public TransformationSourceType getSourceType() {
        return sourceType;
    }

    public TransformationSourceRole getSourceRole() {
        return sourceRole;
    }

    public TransformationSourceStatus getStatus() {
        return status;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getStorageReference() {
        return storageReference;
    }

    public String getChecksumSha256() {
        return checksumSha256;
    }

    public int getDocumentVersion() {
        return documentVersion;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public boolean isAuthoritative() {
        return authoritative;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public Instant getParsedAt() {
        return parsedAt;
    }

    public Instant getAnalyzedAt() {
        return analyzedAt;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }
}