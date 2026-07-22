package com.webempresarial.store.knowledge.domain.value;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object que representa el nivel de confianza
 * asociado a una versión de conocimiento.
 *
 * <p>El valor permitido se encuentra dentro del rango cerrado:</p>
 *
 * <pre>
 * 0.0000 <= confidence <= 1.0000
 * </pre>
 *
 * <p>Ejemplos:</p>
 *
 * <pre>
 * 0.2500 = confianza baja
 * 0.7000 = confianza media
 * 0.9500 = confianza alta
 * 1.0000 = confianza completa
 * </pre>
 */
@Embeddable
public class KnowledgeConfidence
        implements Comparable<KnowledgeConfidence> {

    public static final int SCALE = 4;
    public static final int PRECISION = 5;

    private static final BigDecimal MIN_VALUE =
            BigDecimal.ZERO.setScale(SCALE);

    private static final BigDecimal MAX_VALUE =
            BigDecimal.ONE.setScale(SCALE);

    @Column(
            name = "confidence",
            nullable = false,
            precision = PRECISION,
            scale = SCALE
    )
    private BigDecimal value;

    /**
     * Constructor requerido por JPA.
     */
    protected KnowledgeConfidence() {
    }

    private KnowledgeConfidence(BigDecimal value) {
        this.value = normalizeAndValidate(value);
    }

    /**
     * Crea un nivel de confianza a partir de BigDecimal.
     */
    public static KnowledgeConfidence of(BigDecimal value) {
        return new KnowledgeConfidence(value);
    }

    /**
     * Crea un nivel de confianza a partir de double.
     *
     * <p>Se utiliza BigDecimal.valueOf para evitar la mayoría
     * de los problemas de representación binaria de double.</p>
     */
    public static KnowledgeConfidence of(double value) {
        return new KnowledgeConfidence(
                BigDecimal.valueOf(value)
        );
    }

    /**
     * Confianza mínima.
     */
    public static KnowledgeConfidence zero() {
        return new KnowledgeConfidence(MIN_VALUE);
    }

    /**
     * Confianza completa.
     */
    public static KnowledgeConfidence full() {
        return new KnowledgeConfidence(MAX_VALUE);
    }

    private static BigDecimal normalizeAndValidate(
            BigDecimal value
    ) {
        Objects.requireNonNull(
                value,
                "El nivel de confianza es obligatorio"
        );

        BigDecimal normalized;

        try {
            normalized = value.setScale(
                    SCALE,
                    RoundingMode.HALF_UP
            );
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "No fue posible normalizar el nivel de confianza",
                    exception
            );
        }

        if (normalized.compareTo(MIN_VALUE) < 0) {
            throw new IllegalArgumentException(
                    "El nivel de confianza no puede ser menor que 0.0000"
            );
        }

        if (normalized.compareTo(MAX_VALUE) > 0) {
            throw new IllegalArgumentException(
                    "El nivel de confianza no puede ser mayor que 1.0000"
            );
        }

        return normalized;
    }

    public BigDecimal getValue() {
        return value;
    }

    /**
     * Devuelve el nivel de confianza como porcentaje.
     *
     * <p>Ejemplo: 0.8500 devuelve 85.00.</p>
     */
    public BigDecimal asPercentage() {
        return value
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isZero() {
        return value.compareTo(MIN_VALUE) == 0;
    }

    public boolean isFull() {
        return value.compareTo(MAX_VALUE) == 0;
    }

    public boolean isHigherThan(
            KnowledgeConfidence other
    ) {
        Objects.requireNonNull(
                other,
                "El nivel de confianza comparado es obligatorio"
        );

        return compareTo(other) > 0;
    }

    public boolean isLowerThan(
            KnowledgeConfidence other
    ) {
        Objects.requireNonNull(
                other,
                "El nivel de confianza comparado es obligatorio"
        );

        return compareTo(other) < 0;
    }

    public boolean isAtLeast(
            KnowledgeConfidence minimum
    ) {
        Objects.requireNonNull(
                minimum,
                "El nivel mínimo de confianza es obligatorio"
        );

        return compareTo(minimum) >= 0;
    }

    @Override
    public int compareTo(KnowledgeConfidence other) {
        Objects.requireNonNull(
                other,
                "El nivel de confianza comparado es obligatorio"
        );

        return this.value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof KnowledgeConfidence that)) {
            return false;
        }

        /*
         * compareTo se utiliza en lugar de BigDecimal.equals
         * para que valores equivalentes con distinta escala,
         * como 0.7 y 0.7000, sean considerados iguales.
         */
        return value.compareTo(that.value) == 0;
    }

    @Override
    public int hashCode() {
        return value.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }
}