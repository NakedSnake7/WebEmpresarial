package com.webempresarial.store.digitaltransformation.domain.source;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "transformation_source_sections",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_transformation_source_section_code",
                        columnNames = {
                                "source_content_id",
                                "section_code"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_transformation_source_section_page",
                        columnList = "source_content_id,start_page,end_page"
                ),
                @Index(
                        name = "idx_transformation_source_section_order",
                        columnList = "source_content_id,display_order"
                )
        }
)
public class SourceDocumentSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "source_content_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_transformation_source_section_content"
            )
    )
    private SourceDocumentContent sourceContent;

    @Column(
            name = "section_code",
            nullable = false,
            length = 80
    )
    private String sectionCode;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "section_type",
            nullable = false,
            length = 50
    )
    private SourceSectionType sectionType;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "start_page", nullable = false)
    private int startPage;

    @Column(name = "end_page", nullable = false)
    private int endPage;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Lob
    @Column(name = "section_text", columnDefinition = "LONGTEXT")
    private String sectionText;

    @Column(name = "summary", length = 4000)
    private String summary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SourceDocumentSection() {
    }

    private SourceDocumentSection(
            SourceDocumentContent sourceContent,
            String sectionCode,
            SourceSectionType sectionType,
            String title,
            int startPage,
            int endPage,
            int displayOrder,
            String sectionText,
            String summary
    ) {
        this.sourceContent = Objects.requireNonNull(
                sourceContent,
                "El contenido fuente es obligatorio"
        );

        if (sourceContent.getExtractionStatus()
                != SourceContentExtractionStatus.VERIFIED) {
            throw new IllegalStateException(
                    "Solo puede estructurarse contenido verificado"
            );
        }

        this.sectionCode = normalizeCode(sectionCode);

        this.sectionType = Objects.requireNonNull(
                sectionType,
                "El tipo de sección es obligatorio"
        );

        this.title = normalizeRequired(
                title,
                "El título de la sección es obligatorio",
                500
        );

        validatePages(startPage, endPage);

        if (displayOrder < 0) {
            throw new IllegalArgumentException(
                    "El orden de la sección no puede ser negativo"
            );
        }

        this.startPage = startPage;
        this.endPage = endPage;
        this.displayOrder = displayOrder;
        this.sectionText = normalizeRequiredText(sectionText);
        this.summary = normalizeOptional(summary, 4000);
    }

    public static SourceDocumentSection create(
            SourceDocumentContent sourceContent,
            String sectionCode,
            SourceSectionType sectionType,
            String title,
            int startPage,
            int endPage,
            int displayOrder,
            String sectionText,
            String summary
    ) {
        return new SourceDocumentSection(
                sourceContent,
                sectionCode,
                sectionType,
                title,
                startPage,
                endPage,
                displayOrder,
                sectionText,
                summary
        );
    }

    public boolean containsPage(int page) {
        return page >= startPage && page <= endPage;
    }

    public void updateSummary(String summary) {
        this.summary = normalizeOptional(summary, 4000);
    }

    private static void validatePages(
            int startPage,
            int endPage
    ) {
        if (startPage < 1) {
            throw new IllegalArgumentException(
                    "La página inicial debe ser mayor o igual a 1"
            );
        }

        if (endPage < startPage) {
            throw new IllegalArgumentException(
                    "La página final no puede ser menor que la inicial"
            );
        }
    }

    private static String normalizeCode(String code) {
        String normalized = normalizeRequired(
                code,
                "El código de sección es obligatorio",
                80
        ).toUpperCase();

        if (!normalized.matches(
                "^[A-Z0-9][A-Z0-9_-]{2,79}$"
        )) {
            throw new IllegalArgumentException(
                    "El código de sección no tiene un formato válido"
            );
        }

        return normalized;
    }

    private static String normalizeRequiredText(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "El texto de la sección es obligatorio"
            );
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
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public SourceDocumentContent getSourceContent() {
        return sourceContent;
    }

    public String getSectionCode() {
        return sectionCode;
    }

    public SourceSectionType getSectionType() {
        return sectionType;
    }

    public String getTitle() {
        return title;
    }

    public int getStartPage() {
        return startPage;
    }

    public int getEndPage() {
        return endPage;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public String getSectionText() {
        return sectionText;
    }

    public String getSummary() {
        return summary;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}