package com.webempresarial.store.knowledge.domain.value;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.Locale;
import java.util.Objects;

import com.webempresarial.store.knowledge.domain.enums.KnowledgeContextType;

/**
 * Value Object que identifica el contexto funcional
 * de un objeto de conocimiento.
 *
 * <p>Store representa la frontera de seguridad multi-tenant.
 * KnowledgeContextRoot identifica el asunto funcional dentro
 * de ese tenant: proyecto, sistema, proceso, producto, etc.</p>
 */
@Embeddable
public class KnowledgeContextRoot {

    public static final int MAX_REFERENCE_LENGTH = 120;
    public static final String PLATFORM_REFERENCE = "WEBEMPRESARIAL";

    @Enumerated(EnumType.STRING)
    @Column(
            name = "context_type",
            nullable = false,
            length = 40
    )
    private KnowledgeContextType type;

    @Column(
            name = "context_id",
            nullable = false,
            length = MAX_REFERENCE_LENGTH
    )
    private String reference;

    /**
     * Constructor requerido por JPA.
     */
    protected KnowledgeContextRoot() {
    }

    private KnowledgeContextRoot(
            KnowledgeContextType type,
            String reference
    ) {
        this.type = Objects.requireNonNull(
                type,
                "KnowledgeContextType es obligatorio"
        );

        this.reference = normalizeReference(reference);
    }

    /**
     * Crea un contexto funcional genérico.
     */
    public static KnowledgeContextRoot of(
            KnowledgeContextType type,
            String reference
    ) {
        return new KnowledgeContextRoot(type, reference);
    }

    /**
     * Crea el contexto general de la plataforma.
     */
    public static KnowledgeContextRoot platform() {
        return new KnowledgeContextRoot(
                KnowledgeContextType.PLATFORM,
                PLATFORM_REFERENCE
        );
    }

    /**
     * Crea un contexto asociado directamente a una Store.
     */
    public static KnowledgeContextRoot store(Long storeId) {
        if (storeId == null || storeId <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de Store debe ser válido"
            );
        }

        return new KnowledgeContextRoot(
                KnowledgeContextType.STORE,
                storeId.toString()
        );
    }

    private static String normalizeReference(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "La referencia del contexto es obligatoria"
            );
        }

        String normalized = value
                .trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9._-]", "-")
                .replaceAll("-{2,}", "-");

        if (normalized.length() > MAX_REFERENCE_LENGTH) {
            throw new IllegalArgumentException(
                    "La referencia del contexto no puede superar "
                            + MAX_REFERENCE_LENGTH
                            + " caracteres"
            );
        }

        return normalized;
    }

    public KnowledgeContextType getType() {
        return type;
    }

    public String getReference() {
        return reference;
    }

    /**
     * Indica si el contexto representa directamente
     * a la Store proporcionada.
     */
    public boolean belongsToStore(Long storeId) {
        return type == KnowledgeContextType.STORE
                && storeId != null
                && reference.equals(storeId.toString());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof KnowledgeContextRoot that)) {
            return false;
        }

        return type == that.type
                && Objects.equals(reference, that.reference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, reference);
    }

    @Override
    public String toString() {
        return type + ":" + reference;
    }
}