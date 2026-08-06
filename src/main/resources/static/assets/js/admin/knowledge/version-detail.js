import {
    extractApiError,
    formatDateTime,
    parseJsonResponse
} from "./detail-utils.js";

document.addEventListener(
        "DOMContentLoaded",
        initializeVersionDetail
);

function initializeVersionDetail() {
    const page =
            document.querySelector(
                    ".version-detail-page"
            );

    const knowledgeId =
            page?.dataset.knowledgeId;

    const versionId =
            page?.dataset.versionId;

    const elements = {
        loading:
                document.getElementById(
                        "versionDetailLoading"
                ),

        error:
                document.getElementById(
                        "versionDetailError"
                ),

        content:
                document.getElementById(
                        "versionDetailContent"
                ),

        semanticVersion:
                document.getElementById(
                        "versionSemanticVersion"
                ),

        currentBadge:
                document.getElementById(
                        "versionCurrentBadge"
                ),

        latestBadge:
                document.getElementById(
                        "versionLatestBadge"
                ),

        historicalBadge:
                document.getElementById(
                        "versionHistoricalBadge"
                ),

        title:
                document.getElementById(
                        "versionTitle"
                ),

        summary:
                document.getElementById(
                        "versionSummary"
                ),

        confidence:
                document.getElementById(
                        "versionConfidence"
                ),

        contentFormat:
                document.getElementById(
                        "versionContentFormat"
                ),

        renderedContent:
                document.getElementById(
                        "versionRenderedContent"
                ),

        createdAt:
                document.getElementById(
                        "versionCreatedAt"
                ),

        updatedAt:
                document.getElementById(
                        "versionUpdatedAt"
                ),

        createdBy:
                document.getElementById(
                        "versionCreatedBy"
                ),

        updatedBy:
                document.getElementById(
                        "versionUpdatedBy"
                ),

        sourceReference:
                document.getElementById(
                        "versionSourceReference"
                )
    };

    if (!knowledgeId || !versionId) {
        showError(
                "No fue posible identificar la versión"
        );

        return;
    }

    loadVersion();

    async function loadVersion() {
        showLoading();

        try {
            const response = await fetch(
                    `/api/knowledge/${encodeURIComponent(knowledgeId)}`
                    + `/versions/${encodeURIComponent(versionId)}`,
                    {
                        credentials: "same-origin",
                        headers: {
                            "Accept": "application/json"
                        }
                    }
            );

            const payload =
                    await parseJsonResponse(response);

            if (!response.ok) {
                throw new Error(
                        extractApiError(
                                payload,
                                "No fue posible cargar la versión"
                        )
                );
            }

            renderVersion(payload);

        } catch (error) {
            console.error(
                    "Knowledge version detail load failed",
                    error
            );

            showError(
                    error.message
                    || "No fue posible cargar la versión"
            );
        }
    }

    function renderVersion(version) {
        setText(
                elements.semanticVersion,
                `v${version.semanticVersion || "—"}`
        );

        elements.currentBadge.hidden =
                !version.current;

        elements.latestBadge.hidden =
                !version.latest;

        elements.historicalBadge.hidden =
                version.current
                || version.latest;

        setText(
                elements.title,
                version.title || "Sin título"
        );

        setText(
                elements.summary,
                version.summary || "Sin resumen"
        );

        setText(
                elements.confidence,
                formatConfidence(
                        version.confidence
                )
        );

        setText(
                elements.contentFormat,
                version.renderedContentFormat
                || version.contentFormat
                || "—"
        );

        renderContent(version);

        setText(
                elements.createdAt,
                formatDateTime(
                        version.createdAt
                )
        );

        setText(
                elements.updatedAt,
                formatDateTime(
                        version.updatedAt
                )
        );

        setText(
                elements.createdBy,
                version.createdBy || "—"
        );

        setText(
                elements.updatedBy,
                version.updatedBy || "—"
        );

        setText(
                elements.sourceReference,
                version.sourceReference
                || "Sin referencia"
        );

        showContent();
    }

    function renderContent(version) {
        elements.renderedContent.replaceChildren();

        if (version.renderedContent) {
            elements.renderedContent.innerHTML =
                    version.renderedContent;

            return;
        }

        const pre =
                document.createElement("pre");

        pre.textContent =
                version.content
                || "Sin contenido disponible.";

        elements.renderedContent.append(pre);
    }

    function formatConfidence(value) {
        const numeric =
                Number(value);

        if (Number.isNaN(numeric)) {
            return "—";
        }

        return `${Math.max(
                0,
                Math.min(100, numeric * 100)
        ).toFixed(2)}%`;
    }

    function setText(element, value) {
        if (element) {
            element.textContent = value;
        }
    }

    function showLoading() {
        elements.loading.hidden = false;
        elements.error.hidden = true;
        elements.content.hidden = true;
    }

    function showContent() {
        elements.loading.hidden = true;
        elements.error.hidden = true;
        elements.content.hidden = false;
    }

    function showError(message) {
        elements.loading.hidden = true;
        elements.content.hidden = true;
        elements.error.textContent = message;
        elements.error.hidden = false;
    }
}