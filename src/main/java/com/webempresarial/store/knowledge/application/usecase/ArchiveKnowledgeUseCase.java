package com.webempresarial.store.knowledge.application.usecase;

import com.webempresarial.store.knowledge.application.command.ArchiveKnowledgeCommand;
import com.webempresarial.store.knowledge.application.result.ArchiveKnowledgeResult;

/**
 * Puerto de entrada para archivar conocimiento publicado.
 */
public interface ArchiveKnowledgeUseCase {

    ArchiveKnowledgeResult execute(
            ArchiveKnowledgeCommand command
    );
}