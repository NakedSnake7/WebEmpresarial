package com.webempresarial.store.knowledge.application.usecase;

import com.webempresarial.store.knowledge.application.command.CreateKnowledgeVersionCommand;
import com.webempresarial.store.knowledge.application.result.CreateKnowledgeVersionResult;

/**
 * Puerto de entrada para crear una nueva versión
 * de un KnowledgeObject.
 */
public interface CreateKnowledgeVersionUseCase {

    CreateKnowledgeVersionResult execute(
            CreateKnowledgeVersionCommand command
    );
}