document.addEventListener("DOMContentLoaded", () => {

    const page =
            document.querySelector(".knowledge-workspace");

    const knowledgeId =
            page?.dataset.knowledgeId;

			const state = {
			    knowledge: null,
			    loading: false,
			    sectionObserver: null
			};

    const elements = {
        loading:
                document.getElementById("knowledgeDetailLoading"),

        error:
                document.getElementById("knowledgeDetailError"),

        content:
                document.getElementById("knowledgeDetailContent"),

        code:
                document.getElementById("knowledgeCode"),

        status:
                document.getElementById("knowledgeStatus"),

        title:
                document.getElementById("knowledgeTitle"),

        summary:
                document.getElementById("knowledgeSummary"),

        confidence:
                document.getElementById("knowledgeConfidence"),

        confidenceProgress:
                document.getElementById(
                        "knowledgeConfidenceProgress"
                ),

        documentContent:
                document.getElementById("knowledgeContent"),

        semanticVersion:
                document.getElementById(
                        "knowledgeSemanticVersion"
                ),

        type:
                document.getElementById("knowledgeType"),

        domain:
                document.getElementById("knowledgeDomain"),

        classification:
                document.getElementById(
                        "knowledgeClassification"
                ),

        riskLevel:
                document.getElementById(
                        "knowledgeRiskLevel"
                ),

        context:
                document.getElementById("knowledgeContext"),

        createdAt:
                document.getElementById("knowledgeCreatedAt"),

        updatedAt:
                document.getElementById("knowledgeUpdatedAt"),

        validFrom:
                document.getElementById("knowledgeValidFrom"),

        validUntil:
                document.getElementById("knowledgeValidUntil"),

        createdBy:
                document.getElementById("knowledgeCreatedBy"),

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
				
    };

    if (!knowledgeId) {
        showError(
                "No fue posible identificar el conocimiento"
        );
        return;
    }
	
	async function loadVersionHistory() {
	    elements.versionsLoading.hidden = false;
	    elements.versionsError.hidden = true;
	    elements.versionHistory.hidden = true;

	    try {
	        const response = await fetch(
	                `/api/knowledge/${encodeURIComponent(knowledgeId)}/versions`,
	                {
	                    credentials: "same-origin",
	                    headers: {
	                        "Accept": "application/json"
	                    }
	                }
	        );

	        const payload =
	                await parseResponse(response);

	        if (!response.ok) {
	            throw new Error(
	                    payload?.message
	                    || payload?.error
	                    || "No fue posible cargar las versiones"
	            );
	        }

	        renderVersionHistory(
	                Array.isArray(payload)
	                        ? payload
	                        : []
	        );

	    } catch (error) {
	        console.error(
	                "Knowledge version history load failed",
	                error
	        );

	        elements.versionsLoading.hidden = true;
	        elements.versionsError.textContent =
	                error.message
	                || "No fue posible cargar las versiones";

	        elements.versionsError.hidden = false;
	    }
	}
	
	function observeDocumentSections() {
	    state.sectionObserver?.disconnect();

	    const headings =
	            getDocumentHeadings();

	    if (headings.length === 0) {
	        return;
	    }

	    state.sectionObserver =
	            new IntersectionObserver(
	                    entries => {
	                        const visibleEntry =
	                                entries
	                                        .filter(
	                                                entry =>
	                                                        entry.isIntersecting
	                                        )
	                                        .sort(
	                                                (first, second) =>
	                                                        first.boundingClientRect.top
	                                                        - second.boundingClientRect.top
	                                        )[0];

	                        if (!visibleEntry) {
	                            return;
	                        }

	                        activateTableOfContentsLink(
	                                visibleEntry.target.id
	                        );
	                    },
	                    {
	                        rootMargin:
	                                "-15% 0px -70% 0px",
	                        threshold: 0
	                    }
	            );

	    headings.forEach(
	            heading =>
	                    state.sectionObserver.observe(
	                            heading
	                    )
	    );
	}

	function activateTableOfContentsLink(
	        headingId
	) {
	    if (!elements.tableOfContents) {
	        return;
	    }

	    elements.tableOfContents
	            .querySelectorAll("a")
	            .forEach(link => {
	                link.classList.toggle(
	                        "is-active",
	                        link.dataset.targetId === headingId
	                );
	            });
	}
	
	function openDocumentSearch() {
	    if (!elements.documentSearchDialog) {
	        return;
	    }

	    elements.documentSearchResult.textContent = "";
	    elements.documentSearchInput.value = "";

	    elements.documentSearchDialog.showModal();

	    window.setTimeout(
	            () => elements.documentSearchInput.focus(),
	            50
	    );
	}

	function searchInsideDocument(event) {
	    event.preventDefault();

	    const query =
	            elements.documentSearchInput
	                    .value
	                    .trim()
	                    .toLocaleLowerCase("es-MX");

	    if (!query) {
	        elements.documentSearchResult.textContent =
	                "Escribe un término de búsqueda.";

	        return;
	    }

	    const documentText =
	            elements.documentContent
	                    .textContent
	                    .toLocaleLowerCase("es-MX");

	    const position =
	            documentText.indexOf(query);

	    if (position < 0) {
	        elements.documentSearchResult.textContent =
	                "No se encontraron coincidencias.";

	        return;
	    }

	    const matchingElement =
	            findMatchingDocumentElement(query);

	    elements.documentSearchResult.textContent =
	            "Se encontró una coincidencia.";

	    if (matchingElement) {
	        elements.documentSearchDialog.close();

	        matchingElement.scrollIntoView({
	            behavior: "smooth",
	            block: "center"
	        });

	        temporarilyHighlightElement(
	                matchingElement
	        );
	    }
	}

	function findMatchingDocumentElement(query) {
	    const candidates =
	            elements.documentContent.querySelectorAll(
	                    "h1, h2, h3, h4, p, li, blockquote, pre"
	            );

	    return Array.from(candidates)
	            .find(
	                    element =>
	                            element.textContent
	                                    .toLocaleLowerCase("es-MX")
	                                    .includes(query)
	            );
	}

	function temporarilyHighlightElement(element) {
	    element.classList.add(
	            "knowledge-search-highlight"
	    );

	    window.setTimeout(
	            () => {
	                element.classList.remove(
	                        "knowledge-search-highlight"
	                );
	            },
	            2200
	    );
	}
	
	function resolveDisplayVersion(knowledge) {
	    if (!knowledge) {
	        return null;
	    }

	    return knowledge.currentVersion
	            ?? knowledge.latestVersion
	            ?? null;
	}

    function getCsrfHeaders() {
        const token =
                document.querySelector('meta[name="_csrf"]');

        const header =
                document.querySelector(
                        'meta[name="_csrf_header"]'
                );

        if (!token || !header) {
            return {};
        }

        return {
            [header.content]: token.content
        };
    }

    async function parseResponse(response) {
        const contentType =
                response.headers.get("content-type") || "";

        if (!contentType.includes("application/json")) {
            return null;
        }

        return response.json();
    }
	
	elements.toggleTableOfContentsButton?.addEventListener(
	        "click",
	        () => {
	            if (!elements.tableOfContentsPanel) {
	                return;
	            }

	            elements.tableOfContentsPanel.hidden =
	                    !elements.tableOfContentsPanel.hidden;
	        }
	);

	elements.searchInsideDocumentButton?.addEventListener(
	        "click",
	        openDocumentSearch
	);

	elements.documentSearchForm?.addEventListener(
	        "submit",
	        searchInsideDocument
	);

	elements.closeDocumentSearchButton?.addEventListener(
	        "click",
	        () => elements.documentSearchDialog?.close()
	);

    async function loadKnowledge() {
        if (state.loading) {
            return;
        }

        state.loading = true;
        showLoading();

        try {
            const response = await fetch(
                    `/api/knowledge/${encodeURIComponent(knowledgeId)}`,
                    {
                        credentials: "same-origin",
                        headers: {
                            "Accept": "application/json"
                        }
                    }
            );

            const payload =
                    await parseResponse(response);

            if (!response.ok) {
                throw new Error(
                        payload?.message
                        || payload?.error
                        || "No fue posible cargar el conocimiento"
                );
            }

            state.knowledge = payload;
            renderKnowledge(payload);
			await loadVersionHistory();

        } catch (error) {
            console.error(
                    "Knowledge detail load failed",
                    error
            );

            showError(
                    error.message
                    || "Ocurrió un error inesperado"
            );

        } finally {
            state.loading = false;
        }
    }
	

	function createVersionHistoryItem(version) {
	    const item =
	            document.createElement("li");

	    item.className =
	            "version-history-item";

	    if (version.latest) {
	        item.classList.add("is-latest");
	    }

	    if (version.current) {
	        item.classList.add("is-current");
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

	    const title =
	            document.createElement("p");

	    title.className =
	            "version-history-title";

	    title.textContent =
	            version.title || "Sin título";

	    const metadata =
	            document.createElement("small");

	    metadata.textContent =
	            [
	                version.createdBy || "Autor desconocido",
	                formatDateTime(version.createdAt)
	            ].join(" · ");

	    const actions =
	            document.createElement("div");

	    actions.className =
	            "version-history-actions";

	    const viewButton =
	            document.createElement("button");

	    viewButton.type = "button";
	    viewButton.className =
	            "version-history-action";

	    viewButton.textContent =
	            "Ver";

	    viewButton.addEventListener(
	            "click",
	            () => loadSpecificVersion(version.id)
	    );

	    actions.append(viewButton);

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

	function createVersionBadge(
	        label,
	        type
	) {
	    const badge =
	            document.createElement("span");

	    badge.className =
	            `version-history-badge version-history-badge-${type}`;

	    badge.textContent =
	            label;

	    return badge;
	}
	
	async function loadSpecificVersion(versionId) {

	    const response =
	            await fetch(
	                    `/api/knowledge/versions/${versionId}`,
	                    {
	                        headers:{
	                            Accept:"application/json"
	                        }
	                    });

	    const version =
	            await response.json();

	    state.knowledge.currentVersion =
	            version;

	    renderKnowledge(state.knowledge);
	}

	function renderKnowledge(knowledge) {
	    const version =
	            resolveDisplayVersion(knowledge);

	    elements.code.textContent =
	            knowledge.code || "—";

	    renderStatus(knowledge.status);

	    elements.title.textContent =
	            version?.title
	            || "Sin título";

	    elements.summary.textContent =
	            version?.summary
	            || "Sin resumen";

	    renderConfidence(
	            version?.confidence
	    );

	    elements.semanticVersion.textContent =
	            version?.semanticVersion
	            || "Sin versión disponible";

	    elements.type.textContent =
	            formatEnum(knowledge.typeCode);

	    elements.domain.textContent =
	            formatEnum(knowledge.domain);

	    elements.classification.textContent =
	            formatEnum(knowledge.classification);

	    elements.riskLevel.textContent =
	            formatEnum(knowledge.riskLevel);

	    elements.context.textContent =
	            buildContextLabel(knowledge);

	    elements.createdAt.textContent =
	            formatDateTime(knowledge.createdAt);

	    elements.updatedAt.textContent =
	            formatDateTime(knowledge.updatedAt);

	    elements.validFrom.textContent =
	            formatDateTime(knowledge.validFrom);

	    elements.validUntil.textContent =
	            knowledge.validUntil
	                    ? formatDateTime(knowledge.validUntil)
	                    : "Sin vencimiento";

	    elements.createdBy.textContent =
	            version?.createdBy
	            || "—";

	    elements.sourceReference.textContent =
	            version?.sourceReference
	            || "Sin referencia";

	    renderDocument(version);
	    renderLifecycle(knowledge.status);
	    renderActions(knowledge.status, version);
	    showContent();
	}
	function renderVersionHistory(versions) {

	    elements.versionsLoading.hidden = true;
	    elements.versionsError.hidden = true;

	    elements.versionCount.textContent =
	            versions.length;

	    elements.versionHistory.replaceChildren();

	    if (versions.length === 0) {

	        const empty =
	                document.createElement("li");

	        empty.className =
	                "version-history-empty";

	        empty.textContent =
	                "No existen versiones registradas.";

	        elements.versionHistory.append(empty);

	        elements.versionHistory.hidden = false;

	        return;
	    }

	    versions.forEach(version => {

	        elements.versionHistory.append(
	                createVersionHistoryItem(version)
	        );

	    });

	    elements.versionHistory.hidden = false;
	}
    function renderStatus(status) {
        const normalized =
                status || "UNKNOWN";

        elements.status.className =
                `knowledge-status knowledge-status-${normalized.toLowerCase()}`;

        elements.status.textContent =
                formatEnum(normalized);
    }

    function renderConfidence(value) {
        const numeric =
                Number(value);

        if (Number.isNaN(numeric)) {
            elements.confidence.textContent = "—";
            elements.confidenceProgress.style.width = "0%";
            return;
        }

        const percentage =
                Math.max(
                        0,
                        Math.min(100, numeric * 100)
                );

        elements.confidence.textContent =
                `${percentage.toFixed(2)}%`;

        elements.confidenceProgress.style.width =
                `${percentage}%`;
    }

	function renderDocument(version) {
	    state.sectionObserver?.disconnect();

	    elements.documentContent.replaceChildren();

	    if (!version) {
	        elements.documentContent.textContent =
	                "Sin contenido disponible.";

	        renderTableOfContents();
	        return;
	    }

	    if (version.renderedContent) {
	        /*
	         * El contenido ya fue renderizado y sanitizado
	         * por KnowledgeContentRenderer.
	         */
	        elements.documentContent.innerHTML =
	                version.renderedContent;

	        elements.documentContent.dataset.format =
	                version.renderedContentFormat
	                || version.contentFormat
	                || "PLAIN_TEXT";

	        prepareDocumentHeadings();
	        renderTableOfContents();
	        observeDocumentSections();

	        return;
	    }

	    const pre =
	            document.createElement("pre");

	    pre.textContent =
	            version.content
	            || "Sin contenido disponible.";

	    pre.dataset.format =
	            version.contentFormat
	            || "PLAIN_TEXT";

	    elements.documentContent.append(pre);

	    renderTableOfContents();
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
	            .querySelectorAll(".lifecycle-step")
	            .forEach(step => {

	                const stepStatus =
	                        step.dataset.status;

	                const stepIndex =
	                        order.indexOf(stepStatus);

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

        elements.createVersionButton.hidden =
                status !== "DRAFT";

        if (status === "DRAFT") {
            elements.submitReviewButton.hidden = false;
        }

        if (status === "IN_REVIEW") {
            elements.approveButton.hidden = false;
        }

        if (status === "APPROVED" && version?.id) {
            elements.publishButton.hidden = false;
        }

        if (status === "PUBLISHED") {
            elements.archiveButton.hidden = false;
        }
    }

    function hideLifecycleActions() {
        elements.submitReviewButton.hidden = true;
        elements.approveButton.hidden = true;
        elements.publishButton.hidden = true;
        elements.archiveButton.hidden = true;
    }

    async function executeLifecycleCommand(
            path,
            options = {}
    ) {
        try {
            setActionButtonsDisabled(true);

            const response = await fetch(
                    `/api/knowledge/${encodeURIComponent(knowledgeId)}/${path}`,
                    {
                        method: "POST",
                        credentials: "same-origin",
                        headers: {
                            "Accept": "application/json",
                            ...getCsrfHeaders(),
                            ...(options.body
                                    ? {
                                        "Content-Type":
                                                "application/json"
                                    }
                                    : {})
                        },
                        body:
                                options.body
                                ? JSON.stringify(options.body)
                                : undefined
                    }
            );

            const payload =
                    await parseResponse(response);

            if (!response.ok) {
                throw new Error(
                        extractError(payload)
                );
            }

            await loadKnowledge();

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

    function extractError(payload) {
        if (Array.isArray(payload?.violations)
                && payload.violations.length > 0) {

            return payload.violations
                    .map(item => item.message)
                    .join(" ");
        }

        return payload?.message
                || payload?.error
                || "No fue posible ejecutar la acción";
    }

    function showWorkspaceMessage(message) {
        window.alert(message);
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

	    const now =
	            new Date();

	    now.setMinutes(
	            now.getMinutes()
	                    - now.getTimezoneOffset()
	    );

	    elements.publishValidFrom.value =
	            now.toISOString().slice(0, 16);

	    elements.publishValidUntil.value = "";

	    elements.publishDialog.showModal();
	}

	function submitPublication(event) {
	    event.preventDefault();

	    const version =
	            resolveDisplayVersion(
	                    state.knowledge
	            );

	    const validFrom =
	            elements.publishValidFrom.value;

	    const validUntil =
	            elements.publishValidUntil.value;

	    if (!version?.id || !validFrom) {
	        showWorkspaceMessage(
	                "La versión y la fecha inicial son obligatorias"
	        );
	        return;
	    }

	    elements.publishDialog.close();

	    executeLifecycleCommand(
	            "publish",
	            {
	                body: {
	                    versionId: version.id,
	                    validFrom,
	                    validUntil:
	                            validUntil || null
	                }
	            }
	    );
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
	            button.disabled = disabled;
	        }
	    });
	}

    function formatEnum(value) {
        if (!value) {
            return "—";
        }

        return String(value)
                .toLowerCase()
                .split("_")
                .map(
                        word =>
                                word.charAt(0).toUpperCase()
                                + word.slice(1)
                )
                .join(" ");
    }

    function formatDateTime(value) {
        if (!value) {
            return "—";
        }

        const date =
                new Date(value);

        if (Number.isNaN(date.getTime())) {
            return value;
        }

        return new Intl.DateTimeFormat(
                "es-MX",
                {
                    dateStyle: "medium",
                    timeStyle: "short"
                }
        ).format(date);
    }

	function prepareDocumentHeadings() {
	    const headings =
	            getDocumentHeadings();

	    const usedIds =
	            new Set();

	    headings.forEach((heading, index) => {
	        const baseId =
	                slugifyHeading(
	                        heading.textContent
	                )
	                || `section-${index + 1}`;

	        let candidateId =
	                baseId;

	        let suffix =
	                2;

	        while (usedIds.has(candidateId)) {
	            candidateId =
	                    `${baseId}-${suffix}`;

	            suffix++;
	        }

	        usedIds.add(candidateId);
	        heading.id = candidateId;
	        heading.tabIndex = -1;
	    });
	}

	function getDocumentHeadings() {
	    return Array.from(
	            elements.documentContent.querySelectorAll(
	                    "h1, h2, h3, h4"
	            )
	    );
	}

	function slugifyHeading(value) {
	    return String(value || "")
	            .normalize("NFD")
	            .replace(/[\u0300-\u036f]/g, "")
	            .toLowerCase()
	            .trim()
	            .replace(/[^a-z0-9\s-]/g, "")
	            .replace(/\s+/g, "-")
	            .replace(/-+/g, "-");
	}
	
	function renderTableOfContents() {
		if (!elements.tableOfContents) {
		    return;
		}
	    const headings =
	            getDocumentHeadings();

	    elements.tableOfContents.replaceChildren();

	    if (headings.length === 0) {
	        const emptyMessage =
	                document.createElement("p");

	        emptyMessage.className =
	                "knowledge-toc-empty";

	        emptyMessage.textContent =
	                "Este documento todavía no contiene encabezados.";

	        elements.tableOfContents.append(
	                emptyMessage
	        );

	        return;
	    }

	    const list =
	            document.createElement("ol");

	    list.className =
	            "knowledge-toc-list";

	    headings.forEach(heading => {
	        const item =
	                document.createElement("li");

	        const level =
	                Number(
	                        heading.tagName.substring(1)
	                );

	        item.className =
	                `knowledge-toc-level-${level}`;

	        const link =
	                document.createElement("a");

	        link.href =
	                `#${heading.id}`;

	        link.dataset.targetId =
	                heading.id;

	        link.textContent =
	                heading.textContent.trim();

	        link.addEventListener(
	                "click",
	                event => {
	                    event.preventDefault();

	                    navigateToDocumentHeading(
	                            heading
	                    );
	                }
	        );

	        item.append(link);
	        list.append(item);
	    });

	    elements.tableOfContents.append(list);
	}
	
	function navigateToDocumentHeading(heading) {
	    heading.scrollIntoView({
	        behavior: "smooth",
	        block: "start"
	    });

	    history.replaceState(
	            null,
	            "",
	            `#${heading.id}`
	    );

	    window.setTimeout(
	            () => heading.focus({
	                preventScroll: true
	            }),
	            350
	    );
	}
	
	
    function buildContextLabel(knowledge) {
        if (!knowledge.contextType) {
            return "Contexto natural de la tienda";
        }

        return `${formatEnum(knowledge.contextType)} · ${
                knowledge.contextReference || "—"
        }`;
    }

    function showLoading() {
        elements.loading.hidden = false;
        elements.error.hidden = true;
        elements.content.hidden = true;
    }

    function showError(message) {
        elements.loading.hidden = true;
        elements.content.hidden = true;

        elements.error.textContent = message;
        elements.error.hidden = false;
    }

    function showContent() {
        elements.loading.hidden = true;
        elements.error.hidden = true;
        elements.content.hidden = false;
    }

    elements.refreshButton.addEventListener(
            "click",
            loadKnowledge
    );

    elements.createVersionButton.addEventListener(
            "click",
            () => {
                window.location.href =
                        `/admin/knowledge/${encodeURIComponent(knowledgeId)}/versions/new`;
            }
    );

    elements.submitReviewButton.addEventListener(
            "click",
            () => executeLifecycleCommand("submit-review")
    );

    elements.approveButton.addEventListener(
            "click",
            () => executeLifecycleCommand("approve")
    );

    elements.publishButton.addEventListener(
            "click",
            openPublishDialog
    );

    elements.archiveButton.addEventListener(
            "click",
            () => {
                const confirmed =
                        window.confirm(
                                "¿Deseas archivar este conocimiento?"
                        );

                if (confirmed) {
                    executeLifecycleCommand("archive");
                }
            }
    );

    elements.publishForm.addEventListener(
            "submit",
            submitPublication
    );

    elements.cancelPublishButton.addEventListener(
            "click",
            () => elements.publishDialog.close()
    );
	
	

    loadKnowledge();
});