package com.webempresarial.store.knowledge.application.result;

import java.util.List;
import java.util.Objects;

/**
 * Resultado paginado del Knowledge Query Engine.
 *
 * <p>Evita exponer directamente Page de Spring Data fuera
 * de la infraestructura de persistencia.</p>
 */
public record KnowledgeQueryPage(

        List<KnowledgeQueryItem> items,

        int page,

        int size,

        long totalElements,

        int totalPages,

        boolean first,

        boolean last,

        boolean hasNext,

        boolean hasPrevious
) {

    public KnowledgeQueryPage {
        items = List.copyOf(
                Objects.requireNonNull(
                        items,
                        "La lista de resultados es obligatoria"
                )
        );

        if (page < 0) {
            throw new IllegalArgumentException(
                    "El número de página no puede ser negativo"
            );
        }

        if (size <= 0) {
            throw new IllegalArgumentException(
                    "El tamaño de página debe ser mayor que cero"
            );
        }

        if (totalElements < 0) {
            throw new IllegalArgumentException(
                    "El total de elementos no puede ser negativo"
            );
        }

        if (totalPages < 0) {
            throw new IllegalArgumentException(
                    "El total de páginas no puede ser negativo"
            );
        }

        validateConsistency(
                items,
                page,
                size,
                totalElements,
                totalPages,
                first,
                last,
                hasNext,
                hasPrevious
        );
    }

    public static KnowledgeQueryPage empty(
            int page,
            int size
    ) {
        return new KnowledgeQueryPage(
                List.of(),
                page,
                size,
                0,
                0,
                true,
                true,
                false,
                false
        );
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int numberOfElements() {
        return items.size();
    }

    private static void validateConsistency(
            List<KnowledgeQueryItem> items,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last,
            boolean hasNext,
            boolean hasPrevious
    ) {
        if (items.size() > size) {
            throw new IllegalArgumentException(
                    "La página contiene más elementos que el tamaño permitido"
            );
        }

        if (items.size() > totalElements) {
            throw new IllegalArgumentException(
                    "La página no puede contener más elementos "
                            + "que el total registrado"
            );
        }

        int expectedTotalPages =
                totalElements == 0
                        ? 0
                        : (int) Math.ceil(
                                (double) totalElements / size
                        );

        if (totalPages != expectedTotalPages) {
            throw new IllegalArgumentException(
                    "El total de páginas es inconsistente. "
                            + "Esperado="
                            + expectedTotalPages
                            + ", recibido="
                            + totalPages
            );
        }

        boolean expectedFirst =
                page == 0;

        if (first != expectedFirst) {
            throw new IllegalArgumentException(
                    "El indicador first es inconsistente"
            );
        }

        boolean expectedHasPrevious =
                page > 0;

        if (hasPrevious != expectedHasPrevious) {
            throw new IllegalArgumentException(
                    "El indicador hasPrevious es inconsistente"
            );
        }

        boolean expectedHasNext =
                totalPages > 0
                        && page + 1 < totalPages;

        if (hasNext != expectedHasNext) {
            throw new IllegalArgumentException(
                    "El indicador hasNext es inconsistente"
            );
        }

        boolean expectedLast =
                totalPages == 0
                        || page + 1 >= totalPages;

        if (last != expectedLast) {
            throw new IllegalArgumentException(
                    "El indicador last es inconsistente"
            );
        }

        if (totalPages > 0 && page >= totalPages) {
            /*
             * Permitimos páginas fuera del rango únicamente cuando
             * vienen vacías y no existe una página siguiente.
             * Esto conserva compatibilidad con consultas paginadas.
             */
            if (!items.isEmpty() || hasNext) {
                throw new IllegalArgumentException(
                        "El número de página excede el total de páginas"
                );
            }
        }
    }
}