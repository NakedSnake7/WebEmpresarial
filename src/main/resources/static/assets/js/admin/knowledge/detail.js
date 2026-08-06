import {
    fetchKnowledgeDetail
} from "./detail-api.js";

import {
    createKnowledgeDocumentController
} from "./detail-document.js";

import {
    resolveKnowledgeDetailElements
} from "./detail-elements.js";

import {
    createKnowledgeLifecycleController
} from "./detail-lifecycle.js";

import {
    buildContextLabel,
    formatDateTime,
    formatEnum,
    resolveDisplayVersion
} from "./detail-utils.js";

import {
    createKnowledgeVersionController
} from "./detail-versions.js";

import {
    createKnowledgeDiffController
} from "./detail-diff.js";

document.addEventListener(
        "DOMContentLoaded",
        initializeKnowledgeWorkspace
);

function initializeKnowledgeWorkspace() {
    const page =
            document.querySelector(
                    ".knowledge-workspace"
            );

    const knowledgeId =
            page?.dataset.knowledgeId;

    const elements =
            resolveKnowledgeDetailElements();

			const state = {
			    knowledge: null,
			    loading: false,
			    sectionObserver: null,
			    displayedVersion: null,
			    historicalMode: false,
			    diffMode: false
			};

    if (!knowledgeId) {
        showFatalError(
                elements,
                "No fue posible identificar el conocimiento"
        );

        return;
    }

    const documentController =
            createKnowledgeDocumentController({
                elements,
                state
            });
			
			let versionController;

			const diffController =
			        createKnowledgeDiffController({
			            elements,
			            knowledgeId,

			            onDiffOpened:
			                    () => {
			                        state.diffMode = true;
			                    },

			            onDiffClosed:
			                    () => {
			                        state.diffMode = false;

			                        versionController
			                                ?.clearComparisonSelection();
			                    }
			        });

			versionController =
			        createKnowledgeVersionController({
			            elements,
			            knowledgeId,

			            onVersionSelected:
			                    displayHistoricalVersion,

			            onCompareRequested:
			                    (
			                            baseVersionId,
			                            targetVersionId
			                    ) => {
			                        diffController.compareVersions(
			                                baseVersionId,
			                                targetVersionId
			                        );
			                    }
			        });

    const lifecycleController =
            createKnowledgeLifecycleController({
                elements,
                state,
                knowledgeId,
                reloadKnowledge:
                        loadKnowledge
            });

			documentController.bindEvents();
			diffController.bindEvents();
			lifecycleController.bindEvents();

    elements.refreshButton
            ?.addEventListener(
                    "click",
                    loadKnowledge
            );

    elements.restorePrimaryVersionButton
            ?.addEventListener(
                    "click",
                    restorePrimaryVersion
            );

    loadKnowledge();

    async function loadKnowledge() {
        if (state.loading) {
            return;
        }

        state.loading = true;

        showLoading(
                elements
        );

        try {
            const knowledge =
                    await fetchKnowledgeDetail(
                            knowledgeId
                    );

					state.knowledge =
					        knowledge;

					state.displayedVersion =
					        null;

					state.historicalMode =
					        false;

					state.diffMode =
					        false;

					versionController.clearSelection();

					versionController.clearComparisonSelection();

					diffController.closeDiff();

					renderKnowledge(
					        knowledge
					);

					await versionController
					        .loadVersionHistory();

        } catch (error) {
            console.error(
                    "Knowledge detail load failed",
                    error
            );

            showFatalError(
                    elements,
                    error.message
                    || "Ocurrió un error inesperado"
            );

        } finally {
            state.loading = false;
        }
    }
	

    function displayHistoricalVersion(
            version
    ) {
        if (!version) {
            return;
        }

        state.displayedVersion =
                version;

        state.historicalMode =
                true;

        renderKnowledge(
                state.knowledge
        );

        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });
    }

    function restorePrimaryVersion() {
        state.displayedVersion =
                null;

        state.historicalMode =
                false;

        versionController.clearSelection();

        renderKnowledge(
                state.knowledge
        );
    }

    function renderKnowledge(
            knowledge
    ) {
        if (!knowledge) {
            showFatalError(
                    elements,
                    "No existe información de conocimiento para mostrar"
            );

            return;
        }

        const primaryVersion =
                resolveDisplayVersion(
                        knowledge
                );

        const visibleVersion =
                state.historicalMode
                        ? state.displayedVersion
                        : primaryVersion;

        renderKnowledgeHeader(
                knowledge,
                visibleVersion
        );

        documentController.renderDocument(
                visibleVersion
        );

        lifecycleController.renderLifecycle(
                knowledge.status
        );

        /*
         * Las acciones siempre se calculan usando la
         * versión principal del agregado, nunca usando
         * una versión histórica visible.
         */
        lifecycleController.renderActions(
                knowledge.status,
                primaryVersion
        );

        renderReaderState(
                visibleVersion
        );

        showContent(
                elements
        );
    }

    function renderKnowledgeHeader(
            knowledge,
            version
    ) {
        setText(
                elements.code,
                knowledge.code || "—"
        );

        renderStatus(
                elements,
                knowledge.status
        );

        setText(
                elements.title,
                version?.title
                || "Sin título"
        );

        setText(
                elements.summary,
                version?.summary
                || "Sin resumen"
        );

        renderConfidence(
                elements,
                version?.confidence
        );

        setText(
                elements.semanticVersion,
                version?.semanticVersion
                || "Sin versión disponible"
        );

        setText(
                elements.type,
                formatEnum(
                        knowledge.typeCode
                )
        );

        setText(
                elements.domain,
                formatEnum(
                        knowledge.domain
                )
        );

        setText(
                elements.classification,
                formatEnum(
                        knowledge.classification
                )
        );

        setText(
                elements.riskLevel,
                formatEnum(
                        knowledge.riskLevel
                )
        );

        setText(
                elements.context,
                buildContextLabel(
                        knowledge
                )
        );

        /*
         * En modo histórico mostramos las fechas editoriales
         * de la versión seleccionada.
         */
        setText(
                elements.createdAt,
                formatDateTime(
                        version?.createdAt
                        || knowledge.createdAt
                )
        );

        setText(
                elements.updatedAt,
                formatDateTime(
                        version?.updatedAt
                        || knowledge.updatedAt
                )
        );

        setText(
                elements.validFrom,
                state.historicalMode
                        ? "No aplica a vista histórica"
                        : formatDateTime(
                                knowledge.validFrom
                        )
        );

        setText(
                elements.validUntil,
                state.historicalMode
                        ? "No aplica a vista histórica"
                        : knowledge.validUntil
                                ? formatDateTime(
                                        knowledge.validUntil
                                )
                                : "Sin vencimiento"
        );

        setText(
                elements.createdBy,
                version?.createdBy
                || "—"
        );

        setText(
                elements.sourceReference,
                version?.sourceReference
                || "Sin referencia"
        );
    }

    function renderReaderState(
            version
    ) {
        if (!elements.readerState) {
            return;
        }

        elements.readerState.hidden =
                !state.historicalMode;

        if (!state.historicalMode) {
            setText(
                    elements.readerVersion,
                    "v—"
            );

            return;
        }

        setText(
                elements.readerVersion,
                `v${version?.semanticVersion || "—"}`
        );
    }
}

function setText(
        element,
        value
) {
    if (element) {
        element.textContent =
                value;
    }
}

function renderStatus(
        elements,
        status
) {
    if (!elements.status) {
        return;
    }

    const normalized =
            status || "UNKNOWN";

    elements.status.className =
            `knowledge-status `
            + `knowledge-status-${normalized.toLowerCase()}`;

    elements.status.textContent =
            formatEnum(
                    normalized
            );
}

function renderConfidence(
        elements,
        value
) {
    const numeric =
            Number(value);

    if (!Number.isFinite(numeric)) {
        setText(
                elements.confidence,
                "—"
        );

        if (elements.confidenceProgress) {
            elements.confidenceProgress.style.width =
                    "0%";
        }

        return;
    }

    const percentage =
            Math.max(
                    0,
                    Math.min(
                            100,
                            numeric * 100
                    )
            );

    setText(
            elements.confidence,
            `${percentage.toFixed(2)}%`
    );

    if (elements.confidenceProgress) {
        elements.confidenceProgress.style.width =
                `${percentage}%`;
    }
}

function showLoading(
        elements
) {
    if (elements.loading) {
        elements.loading.hidden =
                false;
    }

    if (elements.error) {
        elements.error.hidden =
                true;
    }

    if (elements.content) {
        elements.content.hidden =
                true;
    }
}

function showContent(
        elements
) {
    if (elements.loading) {
        elements.loading.hidden =
                true;
    }

    if (elements.error) {
        elements.error.hidden =
                true;
    }

    if (elements.content) {
        elements.content.hidden =
                false;
    }
}

function showFatalError(
        elements,
        message
) {
    if (elements.loading) {
        elements.loading.hidden =
                true;
    }

    if (elements.content) {
        elements.content.hidden =
                true;
    }

    if (elements.error) {
        elements.error.textContent =
                message;

        elements.error.hidden =
                false;
    }
}