package com.webempresarial.store.knowledge.application.usecase;

import com.webempresarial.store.knowledge.application.command.PublishKnowledgeCommand;
import com.webempresarial.store.knowledge.application.result.PublishKnowledgeResult;

/**
 * Puerto de entrada para publicar conocimiento aprobado.
 */
public interface PublishKnowledgeUseCase {

    PublishKnowledgeResult execute(
            PublishKnowledgeCommand command
    );
}