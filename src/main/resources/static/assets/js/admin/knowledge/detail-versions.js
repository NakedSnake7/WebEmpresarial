import {
    fetchKnowledgeVersionDetail,
    fetchKnowledgeVersions
} from "./detail-api.js";

import {
    formatDateTime
} from "./detail-utils.js";

export function createKnowledgeVersionController({
    elements,
    knowledgeId,
    onVersionSelected,
    onCompareRequested
}) {
	
	let comparisonSelection =
	        [];

    let selectedVersionId =
            null;

    async function loadVersionHistory() {
        showLoading();

        try {
            const versions =
                    await fetchKnowledgeVersions(
                            knowledgeId
                    );

            renderVersionHistory(
                    versions
            );

        } catch (error) {
            console.error(
                    "Knowledge version history load failed",
                    error
            );

            showError(
                    error.message
                    || "No fue posible cargar las versiones"
            );
        }
    }

    async function selectVersion(versionId) {
        if (!versionId) {
            return;
        }

        try {
            setHistoryDisabled(true);

            const version =
                    await fetchKnowledgeVersionDetail(
                            knowledgeId,
                            versionId
                    );

            selectedVersionId =
                    version.id;

            markSelectedVersion(
                    selectedVersionId
            );

            onVersionSelected?.(
                    version
            );

        } catch (error) {
            console.error(
                    "Knowledge version selection failed",
                    error
            );

            window.alert(
                    error.message
                    || "No fue posible cargar la versión"
            );

        } finally {
            setHistoryDisabled(false);
        }
    }

    function clearSelection() {
        selectedVersionId =
                null;

        markSelectedVersion(
                null
        );
    }

    function renderVersionHistory(versions) {
        hideLoading();
        hideError();

        if (elements.versionCount) {
            elements.versionCount.textContent =
                    versions.length;
        }

        elements.versionHistory?.replaceChildren();

        if (!elements.versionHistory) {
            return;
        }

        if (versions.length === 0) {
            const empty =
                    document.createElement("li");

            empty.className =
                    "version-history-empty";

            empty.textContent =
                    "No existen versiones registradas.";

            elements.versionHistory.append(
                    empty
            );

            elements.versionHistory.hidden =
                    false;

            return;
        }

        versions.forEach(version => {
            elements.versionHistory.append(
                    createVersionHistoryItem(
                            version
                    )
            );
        });

        elements.versionHistory.hidden =
                false;

        markSelectedVersion(
                selectedVersionId
        );
    }

    function createVersionHistoryItem(version) {
        const item =
                document.createElement("li");

        item.className =
                "version-history-item";

        item.dataset.versionId =
                String(version.id);

        if (version.latest) {
            item.classList.add(
                    "is-latest"
            );
        }

        if (version.current) {
            item.classList.add(
                    "is-current"
            );
        }

        const marker =
                document.createElement("span");

        marker.className =
                "version-history-marker";

        const body =
                document.createElement("div");

        body.className =
                "version-history-body";

        const header =
                createVersionHeader(
                        version
                );

        const title =
                document.createElement("p");

        title.className =
                "version-history-title";

        title.textContent =
                version.title
                || "Sin título";

        const metadata =
                document.createElement("small");

        metadata.textContent =
                [
                    version.createdBy
                    || "Autor desconocido",

                    formatDateTime(
                            version.createdAt
                    )
                ].join(" · ");

        const actions =
                createVersionActions(
                        version
                );

        body.append(
                header,
                title,
                metadata,
                actions
        );

        item.append(
                marker,
                body
        );

        return item;
    }

    function createVersionHeader(version) {
        const header =
                document.createElement("div");

        header.className =
                "version-history-item-header";

        const semanticVersion =
                document.createElement("strong");

        semanticVersion.textContent =
                `v${version.semanticVersion || "—"}`;

        const badges =
                document.createElement("span");

        badges.className =
                "version-history-badges";

        if (version.current) {
            badges.append(
                    createVersionBadge(
                            "Vigente",
                            "current"
                    )
            );
        }

        if (version.latest) {
            badges.append(
                    createVersionBadge(
                            "Última",
                            "latest"
                    )
            );
        }

        header.append(
                semanticVersion,
                badges
        );

        return header;
    }

    function createVersionBadge(
            label,
            type
    ) {
        const badge =
                document.createElement("span");

        badge.className =
                `version-history-badge `
                + `version-history-badge-${type}`;

        badge.textContent =
                label;

        return badge;
    }

    function createVersionActions(version) {
        const actions =
                document.createElement("div");

        actions.className =
                "version-history-actions";

        const viewButton =
                document.createElement("button");

        viewButton.type =
                "button";

        viewButton.className =
                "version-history-action";

        viewButton.dataset.versionAction =
                "view";

        viewButton.textContent =
                "Ver";

        viewButton.addEventListener(
                "click",
                () => selectVersion(
                        version.id
                )
        );
		
		const compareButton =
		        document.createElement("button");

		compareButton.type =
		        "button";

		compareButton.className =
		        "version-history-action";

		compareButton.dataset.versionAction =
		        "compare";

		compareButton.textContent =
		        "Comparar";

		compareButton.addEventListener(
		        "click",
		        () => toggleComparisonVersion(
		                version
		        )
		);

		actions.append(
		        viewButton,
		        compareButton
		);
		

        return actions;
    }

	function toggleComparisonVersion(version) {
	    const existingIndex =
	            comparisonSelection.findIndex(
	                    selected =>
	                            String(selected.id)
	                            === String(version.id)
	            );

	    if (existingIndex >= 0) {
	        comparisonSelection.splice(
	                existingIndex,
	                1
	        );

	        renderComparisonSelection();
	        return;
	    }

	    if (comparisonSelection.length >= 2) {
	        comparisonSelection.shift();
	    }

	    comparisonSelection.push(
	            version
	    );

	    renderComparisonSelection();

	    if (comparisonSelection.length === 2) {
	        const ordered =
	                [...comparisonSelection]
	                        .sort(
	                                compareSemanticVersions
	                        );

	        onCompareRequested?.(
	                ordered[0].id,
	                ordered[1].id
	        );
	    }
	}

	function renderComparisonSelection() {
	    elements.versionHistory
	            ?.querySelectorAll(
	                    ".version-history-item"
	            )
	            .forEach(item => {
	                const selectedIndex =
	                        comparisonSelection.findIndex(
	                                version =>
	                                        String(version.id)
	                                        === item.dataset.versionId
	                        );

	                item.classList.toggle(
	                        "is-comparison-selected",
	                        selectedIndex >= 0
	                );

	                const button =
	                        item.querySelector(
	                                "[data-version-action='compare']"
	                        );

	                if (!button) {
	                    return;
	                }

	                button.textContent =
	                        selectedIndex >= 0
	                                ? `Selección ${selectedIndex + 1}`
	                                : "Comparar";
	            });
	}

	function compareSemanticVersions(
	        first,
	        second
	) {
	    const firstParts =
	            parseSemanticVersion(
	                    first.semanticVersion
	            );

	    const secondParts =
	            parseSemanticVersion(
	                    second.semanticVersion
	            );

	    for (
	        let index = 0;
	        index < 3;
	        index++
	    ) {
	        if (firstParts[index]
	                !== secondParts[index]) {

	            return firstParts[index]
	                    - secondParts[index];
	        }
	    }

	    return 0;
	}

	function parseSemanticVersion(value) {
	    const parts =
	            String(value || "0.0.0")
	                    .split(".")
	                    .slice(0, 3)
	                    .map(part => {
	                        const numeric =
	                                Number(part);

	                        return Number.isInteger(numeric)
	                                && numeric >= 0
	                                        ? numeric
	                                        : 0;
	                    });

	    while (parts.length < 3) {
	        parts.push(0);
	    }

	    return parts;
	}

	function clearComparisonSelection() {
	    comparisonSelection =
	            [];

	    renderComparisonSelection();
	}
	
    function markSelectedVersion(versionId) {
        elements.versionHistory
                ?.querySelectorAll(
                        ".version-history-item"
                )
                .forEach(item => {
                    const selected =
                            versionId !== null
                            && item.dataset.versionId
                                    === String(versionId);

                    item.classList.toggle(
                            "is-selected",
                            selected
                    );

                    const button =
                            item.querySelector(
                                    "[data-version-action='view']"
                            );

                    if (button) {
                        button.textContent =
                                selected
                                        ? "Visualizando"
                                        : "Ver";

                        button.disabled =
                                selected;
                    }
                });
    }

    function setHistoryDisabled(disabled) {
        elements.versionHistory
                ?.querySelectorAll("button")
                .forEach(button => {
                    button.disabled =
                            disabled;
                });
    }

    function showLoading() {
        if (elements.versionsLoading) {
            elements.versionsLoading.hidden =
                    false;
        }

        if (elements.versionsError) {
            elements.versionsError.hidden =
                    true;
        }

        if (elements.versionHistory) {
            elements.versionHistory.hidden =
                    true;
        }
    }

    function hideLoading() {
        if (elements.versionsLoading) {
            elements.versionsLoading.hidden =
                    true;
        }
    }

    function hideError() {
        if (elements.versionsError) {
            elements.versionsError.hidden =
                    true;
        }
    }

    function showError(message) {
        hideLoading();

        if (!elements.versionsError) {
            return;
        }

        elements.versionsError.textContent =
                message;

        elements.versionsError.hidden =
                false;
    }

	return {
	    clearComparisonSelection,
	    clearSelection,
	    loadVersionHistory,
	    selectVersion
	};
}