package com.webempresarial.store.knowledge.application.usecase;

import com.webempresarial.store.knowledge.application.command.CreateKnowledgeObjectCommand;
import com.webempresarial.store.knowledge.application.result.CreateKnowledgeObjectResult;

/**
 * Puerto de entrada para crear un KnowledgeObject.
 */
public interface CreateKnowledgeObjectUseCase {

    CreateKnowledgeObjectResult execute(
            CreateKnowledgeObjectCommand command
    );
}