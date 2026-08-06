import {
    executeKnowledgeCommand
} from "./detail-api.js";

import {
    resolveDisplayVersion
} from "./detail-utils.js";

export function createKnowledgeLifecycleController({
    elements,
    state,
    knowledgeId,
    reloadKnowledge
}) {

    function bindEvents() {
        elements.createVersionButton
                ?.addEventListener(
                        "click",
                        navigateToNewVersion
                );

        elements.submitReviewButton
                ?.addEventListener(
                        "click",
                        () => executeCommand(
                                "submit-review"
                        )
                );

        elements.approveButton
                ?.addEventListener(
                        "click",
                        () => executeCommand(
                                "approve"
                        )
                );

        elements.publishButton
                ?.addEventListener(
                        "click",
                        openPublishDialog
                );

        elements.archiveButton
                ?.addEventListener(
                        "click",
                        confirmArchive
                );

        elements.publishForm
                ?.addEventListener(
                        "submit",
                        submitPublication
                );

        elements.cancelPublishButton
                ?.addEventListener(
                        "click",
                        () => {
                            elements.publishDialog?.close();
                        }
                );
    }

    function renderLifecycle(currentStatus) {
        const order = [
            "DRAFT",
            "IN_REVIEW",
            "APPROVED",
            "PUBLISHED",
            "ARCHIVED"
        ];

        const currentIndex =
                order.indexOf(currentStatus);

        document
                .querySelectorAll(
                        ".lifecycle-step"
                )
                .forEach(step => {
                    const stepStatus =
                            step.dataset.status;

                    const stepIndex =
                            order.indexOf(
                                    stepStatus
                            );

                    step.classList.toggle(
                            "is-complete",
                            currentIndex >= 0
                            && stepIndex < currentIndex
                    );

                    step.classList.toggle(
                            "is-current",
                            currentIndex >= 0
                            && stepStatus === currentStatus
                    );
                });
    }

    function renderActions(
            status,
            version
    ) {
        hideLifecycleActions();

        if (elements.createVersionButton) {
            elements.createVersionButton.hidden =
                    status !== "DRAFT";
        }

        if (status === "DRAFT"
                && elements.submitReviewButton) {

            elements.submitReviewButton.hidden =
                    false;
        }

        if (status === "IN_REVIEW"
                && elements.approveButton) {

            elements.approveButton.hidden =
                    false;
        }

        if (status === "APPROVED"
                && version?.id
                && elements.publishButton) {

            elements.publishButton.hidden =
                    false;
        }

        if (status === "PUBLISHED"
                && elements.archiveButton) {

            elements.archiveButton.hidden =
                    false;
        }
    }

    function hideLifecycleActions() {
        [
            elements.submitReviewButton,
            elements.approveButton,
            elements.publishButton,
            elements.archiveButton
        ].forEach(button => {
            if (button) {
                button.hidden = true;
            }
        });
    }

    async function executeCommand(
            path,
            options = {}
    ) {
        try {
            setActionButtonsDisabled(true);

            await executeKnowledgeCommand(
                    knowledgeId,
                    path,
                    options
            );

            await reloadKnowledge();

        } catch (error) {
            console.error(
                    "Knowledge lifecycle command failed",
                    error
            );

            showWorkspaceMessage(
                    error.message
                    || "No fue posible ejecutar la acción"
            );

        } finally {
            setActionButtonsDisabled(false);
        }
    }

    function navigateToNewVersion() {
        window.location.href =
                `/admin/knowledge/${encodeURIComponent(knowledgeId)}`
                + "/versions/new";
    }

    function openPublishDialog() {
        const version =
                resolveDisplayVersion(
                        state.knowledge
                );

        if (!version?.id) {
            showWorkspaceMessage(
                    "No existe una versión disponible para publicar"
            );

            return;
        }

        if (!elements.publishDialog) {
            return;
        }

        const now =
                new Date();

        now.setMinutes(
                now.getMinutes()
                - now.getTimezoneOffset()
        );

        if (elements.publishValidFrom) {
            elements.publishValidFrom.value =
                    now.toISOString().slice(0, 16);
        }

        if (elements.publishValidUntil) {
            elements.publishValidUntil.value =
                    "";
        }

        elements.publishDialog.showModal();
    }

    function submitPublication(event) {
        event.preventDefault();

        const version =
                resolveDisplayVersion(
                        state.knowledge
                );

        const validFrom =
                elements.publishValidFrom
                        ?.value
                || "";

        const validUntil =
                elements.publishValidUntil
                        ?.value
                || "";

        if (!version?.id || !validFrom) {
            showWorkspaceMessage(
                    "La versión y la fecha inicial son obligatorias"
            );

            return;
        }

        elements.publishDialog?.close();

        executeCommand(
                "publish",
                {
                    body: {
                        versionId:
                                version.id,

                        validFrom,

                        validUntil:
                                validUntil || null
                    }
                }
        );
    }

    function confirmArchive() {
        const confirmed =
                window.confirm(
                        "¿Deseas archivar este conocimiento?"
                );

        if (confirmed) {
            executeCommand(
                    "archive"
            );
        }
    }

    function setActionButtonsDisabled(disabled) {
        [
            elements.createVersionButton,
            elements.submitReviewButton,
            elements.approveButton,
            elements.publishButton,
            elements.archiveButton
        ].forEach(button => {
            if (button) {
                button.disabled =
                        disabled;
            }
        });
    }

    function showWorkspaceMessage(message) {
        window.alert(message);
    }

    return {
        bindEvents,
        renderActions,
        renderLifecycle
    };
}