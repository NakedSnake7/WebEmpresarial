export function resolveKnowledgeDetailElements() {
    return {
        loading:
                document.getElementById(
                        "knowledgeDetailLoading"
                ),

        error:
                document.getElementById(
                        "knowledgeDetailError"
                ),

        content:
                document.getElementById(
                        "knowledgeDetailContent"
                ),

        code:
                document.getElementById(
                        "knowledgeCode"
                ),

        status:
                document.getElementById(
                        "knowledgeStatus"
                ),

        title:
                document.getElementById(
                        "knowledgeTitle"
                ),

        summary:
                document.getElementById(
                        "knowledgeSummary"
                ),

        confidence:
                document.getElementById(
                        "knowledgeConfidence"
                ),

        confidenceProgress:
                document.getElementById(
                        "knowledgeConfidenceProgress"
                ),

        documentContent:
                document.getElementById(
                        "knowledgeContent"
                ),

        semanticVersion:
                document.getElementById(
                        "knowledgeSemanticVersion"
                ),

        type:
                document.getElementById(
                        "knowledgeType"
                ),

        domain:
                document.getElementById(
                        "knowledgeDomain"
                ),

        classification:
                document.getElementById(
                        "knowledgeClassification"
                ),

        riskLevel:
                document.getElementById(
                        "knowledgeRiskLevel"
                ),

        context:
                document.getElementById(
                        "knowledgeContext"
                ),

        createdAt:
                document.getElementById(
                        "knowledgeCreatedAt"
                ),

        updatedAt:
                document.getElementById(
                        "knowledgeUpdatedAt"
                ),

        validFrom:
                document.getElementById(
                        "knowledgeValidFrom"
                ),

        validUntil:
                document.getElementById(
                        "knowledgeValidUntil"
                ),

        createdBy:
                document.getElementById(
                        "knowledgeCreatedBy"
                ),

        sourceReference:
                document.getElementById(
                        "knowledgeSourceReference"
                ),

        refreshButton:
                document.getElementById(
                        "refreshKnowledgeDetailButton"
                ),

        createVersionButton:
                document.getElementById(
                        "createKnowledgeVersionButton"
                ),

        submitReviewButton:
                document.getElementById(
                        "submitReviewButton"
                ),

        approveButton:
                document.getElementById(
                        "approveKnowledgeButton"
                ),

        publishButton:
                document.getElementById(
                        "publishKnowledgeButton"
                ),

        archiveButton:
                document.getElementById(
                        "archiveKnowledgeButton"
                ),

        publishDialog:
                document.getElementById(
                        "publishKnowledgeDialog"
                ),

        publishForm:
                document.getElementById(
                        "publishKnowledgeForm"
                ),

        publishValidFrom:
                document.getElementById(
                        "publishValidFrom"
                ),

        publishValidUntil:
                document.getElementById(
                        "publishValidUntil"
                ),

        cancelPublishButton:
                document.getElementById(
                        "cancelPublishButton"
                ),

        tableOfContentsPanel:
                document.getElementById(
                        "knowledgeTableOfContentsPanel"
                ),

        tableOfContents:
                document.getElementById(
                        "knowledgeTableOfContents"
                ),

        toggleTableOfContentsButton:
                document.getElementById(
                        "toggleTableOfContentsButton"
                ),

        searchInsideDocumentButton:
                document.getElementById(
                        "searchInsideDocumentButton"
                ),

        documentSearchDialog:
                document.getElementById(
                        "documentSearchDialog"
                ),

        documentSearchForm:
                document.getElementById(
                        "documentSearchForm"
                ),

        documentSearchInput:
                document.getElementById(
                        "documentSearchInput"
                ),

        documentSearchResult:
                document.getElementById(
                        "documentSearchResult"
                ),

        closeDocumentSearchButton:
                document.getElementById(
                        "closeDocumentSearchButton"
                ),

        versionCount:
                document.getElementById(
                        "knowledgeVersionCount"
                ),

        versionsLoading:
                document.getElementById(
                        "knowledgeVersionsLoading"
                ),

        versionsError:
                document.getElementById(
                        "knowledgeVersionsError"
                ),

        versionHistory:
                document.getElementById(
                        "knowledgeVersionHistory"
                ),
				readerState:
				        document.getElementById(
				                "knowledgeReaderState"
				        ),

				readerVersion:
				        document.getElementById(
				                "knowledgeReaderVersion"
				        ),

				restorePrimaryVersionButton:
				        document.getElementById(
				                "restorePrimaryVersionButton"
				        ),
						diffPanel:
						        document.getElementById(
						                "knowledgeDiffPanel"
						        ),

						diffLoading:
						        document.getElementById(
						                "knowledgeDiffLoading"
						        ),

						diffError:
						        document.getElementById(
						                "knowledgeDiffError"
						        ),

						diffContent:
						        document.getElementById(
						                "knowledgeDiffContent"
						        ),

						diffBaseVersion:
						        document.getElementById(
						                "diffBaseVersion"
						        ),

						diffTargetVersion:
						        document.getElementById(
						                "diffTargetVersion"
						        ),

						diffBaseTitle:
						        document.getElementById(
						                "diffBaseTitle"
						        ),

						diffTargetTitle:
						        document.getElementById(
						                "diffTargetTitle"
						        ),

						diffAddedCount:
						        document.getElementById(
						                "diffAddedCount"
						        ),

						diffRemovedCount:
						        document.getElementById(
						                "diffRemovedCount"
						        ),

						diffUnchangedCount:
						        document.getElementById(
						                "diffUnchangedCount"
						        ),

						diffMetadataChanges:
						        document.getElementById(
						                "diffMetadataChanges"
						        ),

						diffUnifiedView:
						        document.getElementById(
						                "knowledgeDiffUnifiedView"
						        ),

						diffUnifiedBody:
						        document.getElementById(
						                "knowledgeDiffUnifiedBody"
						        ),

						diffSideBySideView:
						        document.getElementById(
						                "knowledgeDiffSideBySideView"
						        ),

						diffSideBySideBody:
						        document.getElementById(
						                "knowledgeDiffSideBySideBody"
						        ),

						diffModeButtons:
						        document.querySelectorAll(
						                "[data-diff-mode]"
						        ),

						swapDiffVersionsButton:
						        document.getElementById(
						                "swapDiffVersionsButton"
						        ),

						closeDiffButton:
						        document.getElementById(
						                "closeDiffButton"
						        )
    };
}