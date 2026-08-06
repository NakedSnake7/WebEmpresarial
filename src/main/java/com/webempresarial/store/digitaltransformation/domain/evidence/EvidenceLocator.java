package com.webempresarial.store.digitaltransformation.domain.evidence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class EvidenceLocator {

    @Column(name = "page_from")
    private Integer pageFrom;

    @Column(name = "page_to")
    private Integer pageTo;

    @Column(name = "paragraph_reference", length = 200)
    private String paragraphReference;

    @Column(name = "element_reference", length = 200)
    private String elementReference;

    @Column(name = "character_start")
    private Integer characterStart;

    @Column(name = "character_end")
    private Integer characterEnd;

    protected EvidenceLocator() {
    }

    private EvidenceLocator(
            Integer pageFrom,
            Integer pageTo,
            String paragraphReference,
            String elementReference,
            Integer characterStart,
            Integer characterEnd
    ) {
        validatePages(pageFrom, pageTo);
        validateCharacterRange(characterStart, characterEnd);

        this.pageFrom = pageFrom;
        this.pageTo = pageTo;
        this.paragraphReference =
                normalizeOptional(paragraphReference, 200);
        this.elementReference =
                normalizeOptional(elementReference, 200);
        this.characterStart = characterStart;
        this.characterEnd = characterEnd;
    }

    public static EvidenceLocator pages(
            int pageFrom,
            int pageTo
    ) {
        return new EvidenceLocator(
                pageFrom,
                pageTo,
                null,
                null,
                null,
                null
        );
    }

    public static EvidenceLocator page(int page) {
        return pages(page, page);
    }

    public static EvidenceLocator detailed(
            Integer pageFrom,
            Integer pageTo,
            String paragraphReference,
            String elementReference,
            Integer characterStart,
            Integer characterEnd
    ) {
        return new EvidenceLocator(
                pageFrom,
                pageTo,
                paragraphReference,
                elementReference,
                characterStart,
                characterEnd
        );
    }

    public boolean includesPage(int page) {
        if (pageFrom == null || pageTo == null) {
            return false;
        }

        return page >= pageFrom && page <= pageTo;
    }

    private static void validatePages(
            Integer pageFrom,
            Integer pageTo
    ) {
        if (pageFrom == null && pageTo == null) {
            return;
        }

        if (pageFrom == null || pageTo == null) {
            throw new IllegalArgumentException(
                    "El rango de páginas debe estar completo"
            );
        }

        if (pageFrom < 1) {
            throw new IllegalArgumentException(
                    "La página inicial debe ser mayor o igual a 1"
            );
        }

        if (pageTo < pageFrom) {
            throw new IllegalArgumentException(
                    "La página final no puede ser menor que la inicial"
            );
        }
    }

    private static void validateCharacterRange(
            Integer start,
            Integer end
    ) {
        if (start == null && end == null) {
            return;
        }

        if (start == null || end == null) {
            throw new IllegalArgumentException(
                    "El rango de caracteres debe estar completo"
            );
        }

        if (start < 0) {
            throw new IllegalArgumentException(
                    "La posición inicial no puede ser negativa"
            );
        }

        if (end < start) {
            throw new IllegalArgumentException(
                    "La posición final no puede ser menor que la inicial"
            );
        }
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

    public Integer getPageFrom() {
        return pageFrom;
    }

    public Integer getPageTo() {
        return pageTo;
    }

    public String getParagraphReference() {
        return paragraphReference;
    }

    public String getElementReference() {
        return elementReference;
    }

    public Integer getCharacterStart() {
        return characterStart;
    }

    public Integer getCharacterEnd() {
        return characterEnd;
    }
}