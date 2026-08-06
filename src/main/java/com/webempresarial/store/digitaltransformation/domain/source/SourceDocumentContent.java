package com.webempresarial.store.digitaltransformation.domain.source;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "transformation_source_contents",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_transformation_source_content_version",
                        columnNames = {
                                "source_document_id",
                                "content_version"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_transformation_source_content_status",
                        columnList = "source_document_id,extraction_status"
                ),
                @Index(
                        name = "idx_transformation_source_content_current",
                        columnList = "source_document_id,is_current"
                )
        }
)
public class SourceDocumentContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "source_document_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_transformation_source_content_document"
            )
    )
    private TransformationSourceDocument sourceDocument;

    @Column(name = "content_version", nullable = false)
    private int contentVersion;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "extraction_method",
            nullable = false,
            length = 40
    )
    private SourceContentExtractionMethod extractionMethod;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "extraction_status",
            nullable = false,
            length = 40
    )
    private SourceContentExtractionStatus extractionStatus;

    @Lob
    @Column(name = "raw_text", columnDefinition = "LONGTEXT")
    private String rawText;

    @Column(name = "character_count")
    private Integer characterCount;

    @Column(name = "word_count")
    private Integer wordCount;

    @Column(name = "detected_language_code", length = 10)
    private String detectedLanguageCode;

    @Column(name = "parser_name", length = 120)
    private String parserName;

    @Column(name = "parser_version", length = 60)
    private String parserVersion;

    @Column(name = "is_current", nullable = false)
    private boolean current;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "extracted_at")
    private Instant extractedAt;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "failure_reason", length = 2000)
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SourceDocumentContent() {
    }

    private SourceDocumentContent(
            TransformationSourceDocument sourceDocument,
            int contentVersion,
            SourceContentExtractionMethod extractionMethod,
            String parserName,
            String parserVersion
    ) {
        this.sourceDocument = Objects.requireNonNull(
                sourceDocument,
                "El documento fuente es obligatorio"
        );

        if (contentVersion < 1) {
            throw new IllegalArgumentException(
                    "La versión del contenido debe ser mayor o igual a 1"
            );
        }

        this.contentVersion = contentVersion;
        this.extractionMethod = Objects.requireNonNull(
                extractionMethod,
                "El método de extracción es obligatorio"
        );

        this.parserName = normalizeOptional(parserName, 120);
        this.parserVersion = normalizeOptional(parserVersion, 60);

        this.extractionStatus =
                SourceContentExtractionStatus.PENDING;

        this.current = false;
    }

    public static SourceDocumentContent create(
            TransformationSourceDocument sourceDocument,
            int contentVersion,
            SourceContentExtractionMethod extractionMethod,
            String parserName,
            String parserVersion
    ) {
        return new SourceDocumentContent(
                sourceDocument,
                contentVersion,
                extractionMethod,
                parserName,
                parserVersion
        );
    }

    public void startExtraction() {
        requireStatus(SourceContentExtractionStatus.PENDING);

        this.extractionStatus =
                SourceContentExtractionStatus.EXTRACTING;

        this.startedAt = Instant.now();
        this.failureReason = null;
    }

    public void completeExtraction(
            String rawText,
            String detectedLanguageCode
    ) {
        requireStatus(SourceContentExtractionStatus.EXTRACTING);

        String normalizedText = normalizeRequiredText(
                rawText,
                "El contenido extraído es obligatorio"
        );

        this.rawText = normalizedText;
        this.characterCount = normalizedText.length();
        this.wordCount = countWords(normalizedText);
        this.detectedLanguageCode =
                normalizeLanguageCode(detectedLanguageCode);

        this.extractionStatus =
                SourceContentExtractionStatus.EXTRACTED;

        this.extractedAt = Instant.now();
        this.failureReason = null;
    }

    public void requireReview() {
        if (extractionStatus
                != SourceContentExtractionStatus.EXTRACTED) {
            throw new IllegalStateException(
                    "Solo el contenido extraído puede enviarse a revisión"
            );
        }

        this.extractionStatus =
                SourceContentExtractionStatus.REVIEW_REQUIRED;
    }

    public void verify() {
        if (extractionStatus
                != SourceContentExtractionStatus.EXTRACTED
                && extractionStatus
                != SourceContentExtractionStatus.REVIEW_REQUIRED) {
            throw new IllegalStateException(
                    "El contenido solo puede verificarse después de ser extraído"
            );
        }

        this.extractionStatus =
                SourceContentExtractionStatus.VERIFIED;

        this.verifiedAt = Instant.now();
    }

    public void fail(String reason) {
        if (extractionStatus
                == SourceContentExtractionStatus.VERIFIED) {
            throw new IllegalStateException(
                    "Una extracción verificada no puede marcarse como fallida"
            );
        }

        this.failureReason = normalizeRequired(
                reason,
                "La razón del fallo es obligatoria",
                2000
        );

        this.extractionStatus =
                SourceContentExtractionStatus.FAILED;

        this.current = false;
    }

    public void markCurrent() {
        if (extractionStatus
                != SourceContentExtractionStatus.VERIFIED) {
            throw new IllegalStateException(
                    "Solo una extracción verificada puede marcarse como vigente"
            );
        }

        this.current = true;
    }

    public void markHistorical() {
        this.current = false;
    }

    public void ensureBelongsToStore(Long storeId) {
        if (storeId == null || storeId <= 0) {
            throw new IllegalArgumentException(
                    "El storeId debe ser válido"
            );
        }

        if (sourceDocument == null
                || sourceDocument.getProject() == null
                || sourceDocument.getProject().getStore() == null
                || sourceDocument.getProject().getStore().getId() == null
                || !sourceDocument.getProject()
                .getStore()
                .getId()
                .equals(storeId)) {
            throw new IllegalArgumentException(
                    "El contenido no pertenece al store indicado"
            );
        }
    }

    private void requireStatus(
            SourceContentExtractionStatus expected
    ) {
        if (extractionStatus != expected) {
            throw new IllegalStateException(
                    "Estado inválido. Se esperaba " +
                    expected +
                    " pero el contenido se encuentra en " +
                    extractionStatus
            );
        }
    }

    private static int countWords(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }

        return text.trim().split("\\s+").length;
    }

    private static String normalizeLanguageCode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toLowerCase();

        if (!normalized.matches(
                "^[a-z]{2,3}(-[a-z0-9]{2,8})*$"
        )) {
            throw new IllegalArgumentException(
                    "El código de idioma detectado no es válido"
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
        String normalized = normalizeOptional(
                value,
                maxLength
        );

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
        this.createdAt = Instant.now();

        if (extractionStatus == null) {
            this.extractionStatus =
                    SourceContentExtractionStatus.PENDING;
        }
    }

    public Long getId() {
        return id;
    }

    public TransformationSourceDocument getSourceDocument() {
        return sourceDocument;
    }

    public int getContentVersion() {
        return contentVersion;
    }

    public SourceContentExtractionMethod getExtractionMethod() {
        return extractionMethod;
    }

    public SourceContentExtractionStatus getExtractionStatus() {
        return extractionStatus;
    }

    public String getRawText() {
        return rawText;
    }

    public Integer getCharacterCount() {
        return characterCount;
    }

    public Integer getWordCount() {
        return wordCount;
    }

    public String getDetectedLanguageCode() {
        return detectedLanguageCode;
    }

    public String getParserName() {
        return parserName;
    }

    public String getParserVersion() {
        return parserVersion;
    }

    public boolean isCurrent() {
        return current;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getExtractedAt() {
        return extractedAt;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}