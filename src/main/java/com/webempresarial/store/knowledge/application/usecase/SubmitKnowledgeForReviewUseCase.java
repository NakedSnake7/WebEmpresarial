package com.webempresarial.store.knowledge.application.usecase;

import com.webempresarial.store.knowledge.application.command.SubmitKnowledgeForReviewCommand;
import com.webempresarial.store.knowledge.application.result.SubmitKnowledgeForReviewResult;

/**
 * Puerto de entrada para enviar conocimiento a revisión.
 */
public interface SubmitKnowledgeForReviewUseCase {

    SubmitKnowledgeForReviewResult execute(
            SubmitKnowledgeForReviewCommand command
    );
}