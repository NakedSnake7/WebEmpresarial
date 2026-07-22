package com.webempresarial.store.knowledge.application.usecase;

import com.webempresarial.store.knowledge.application.command.ApproveKnowledgeCommand;
import com.webempresarial.store.knowledge.application.result.ApproveKnowledgeResult;

/**
 * Puerto de entrada para aprobar un KnowledgeObject.
 */
public interface ApproveKnowledgeUseCase {

    ApproveKnowledgeResult execute(
            ApproveKnowledgeCommand command
    );
}