package com.webempresarial.store.knowledge.api.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record KnowledgePageResponse<T>(

        List<T> content,

        int page,

        int size,

        long totalElements,

        int totalPages,

        boolean first,

        boolean last,

        boolean empty
) {

    public KnowledgePageResponse {
        content = content == null
                ? List.of()
                : List.copyOf(content);
    }

    public static <S, T> KnowledgePageResponse<T> from(
            Page<S> source,
            Function<S, T> mapper
    ) {
        if (source == null) {
            throw new IllegalArgumentException(
                    "La página de origen es obligatoria"
            );
        }

        if (mapper == null) {
            throw new IllegalArgumentException(
                    "El mapper es obligatorio"
            );
        }

        List<T> mappedContent =
                source.getContent()
                        .stream()
                        .map(mapper)
                        .toList();

        return new KnowledgePageResponse<>(
                mappedContent,
                source.getNumber(),
                source.getSize(),
                source.getTotalElements(),
                source.getTotalPages(),
                source.isFirst(),
                source.isLast(),
                source.isEmpty()
        );
    }
}