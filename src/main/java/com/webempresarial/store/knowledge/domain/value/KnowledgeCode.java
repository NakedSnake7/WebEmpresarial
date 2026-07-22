package com.webempresarial.store.knowledge.domain.value;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object que representa el código funcional e inmutable
 * de un objeto de conocimiento.
 *
 * <p>El identificador técnico de persistencia será un Long,
 * mientras que KnowledgeCode representa la identidad empresarial
 * del conocimiento dentro de una Store.</p>
 *
 * <p>Ejemplos válidos:</p>
 *
 * <pre>
 * KS-000
 * KS-100
 * CRM-015
 * ARCH-020
 * POLICY-001
 * </pre>
 */
@Embeddable
public class KnowledgeCode {

    public static final int MAX_LENGTH = 16;

    private static final Pattern VALID_PATTERN =
            Pattern.compile("^[A-Z]{2,10}-\\d{3,5}$");

    @Column(
            name = "code",
            nullable = false,
            length = MAX_LENGTH,
            updatable = false
    )
    private String value;

    /**
     * Constructor requerido por JPA.
     */
    protected KnowledgeCode() {
    }

    private KnowledgeCode(String value) {
        String normalized = normalize(value);
        validate(normalized);

        this.value = normalized;
    }

    /**
     * Crea un KnowledgeCode validado.
     */
    public static KnowledgeCode of(String value) {
        return new KnowledgeCode(value);
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "El código de conocimiento es obligatorio"
            );
        }

        return value
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private static void validate(String value) {
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "El código de conocimiento no puede superar "
                            + MAX_LENGTH
                            + " caracteres"
            );
        }

        if (!VALID_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "El código de conocimiento debe cumplir el formato "
                            + "PREFIX-000. Ejemplos: KS-100, CRM-015, ARCH-020"
            );
        }
    }

    public String getValue() {
        return value;
    }

    /**
     * Devuelve el prefijo funcional del código.
     *
     * <p>Ejemplo: para KS-100 devuelve KS.</p>
     */
    public String getPrefix() {
        return value.substring(0, value.indexOf('-'));
    }

    /**
     * Devuelve la parte numérica del código.
     *
     * <p>Ejemplo: para KS-100 devuelve 100.</p>
     */
    public int getSequence() {
        return Integer.parseInt(
                value.substring(value.indexOf('-') + 1)
        );
    }

    /**
     * Indica si el código pertenece al prefijo indicado.
     */
    public boolean hasPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return false;
        }

        return getPrefix().equals(
                prefix.trim().toUpperCase(Locale.ROOT)
        );
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof KnowledgeCode that)) {
            return false;
        }

        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}