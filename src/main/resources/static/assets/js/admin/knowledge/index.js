document.addEventListener("DOMContentLoaded", () => {

    const state = {
        page: 0,
        size: 20,
        totalPages: 0,
        loading: false
    };

    const elements = {
        form: document.getElementById("knowledgeSearchForm"),

        text: document.getElementById("knowledgeText"),
        status: document.getElementById("knowledgeStatus"),
        domain: document.getElementById("knowledgeDomain"),
        typeCode: document.getElementById("knowledgeType"),
        classification:
                document.getElementById("knowledgeClassification"),
        riskLevel:
                document.getElementById("knowledgeRiskLevel"),

        refreshButton:
                document.getElementById("refreshKnowledgeButton"),
        clearButton:
                document.getElementById("clearKnowledgeFiltersButton"),
        createButton:
                document.getElementById("createKnowledgeButton"),

        loadingState:
                document.getElementById("knowledgeLoadingState"),
        errorState:
                document.getElementById("knowledgeErrorState"),
        emptyState:
                document.getElementById("knowledgeEmptyState"),

        tableWrapper:
                document.getElementById("knowledgeTableWrapper"),
        tableBody:
                document.getElementById("knowledgeTableBody"),

        pagination:
                document.getElementById("knowledgePagination"),
        previousButton:
                document.getElementById(
                        "previousKnowledgePageButton"
                ),
        nextButton:
                document.getElementById(
                        "nextKnowledgePageButton"
                ),
        pageIndicator:
                document.getElementById("knowledgePageIndicator"),

        resultSummary:
                document.getElementById("knowledgeResultSummary"),

        totalMetric:
                document.getElementById("totalKnowledgeMetric"),
        draftMetric:
                document.getElementById("draftKnowledgeMetric"),
        reviewMetric:
                document.getElementById("reviewKnowledgeMetric"),
        publishedMetric:
                document.getElementById("publishedKnowledgeMetric"),
				emptyTitle:
				        document.getElementById(
				                "knowledgeEmptyTitle"
				        ),

				emptyDescription:
				        document.getElementById(
				                "knowledgeEmptyDescription"
				        )
    };
	const createEmptyButton =
	        document.getElementById(
	                "createKnowledgeEmptyButton"
	        );

	createEmptyButton?.addEventListener(
	        "click",
	        () => {
	            window.location.href =
	                    "/admin/knowledge/new";
	        }
	);
	
    function getCsrfHeaders() {
        const tokenElement =
                document.querySelector('meta[name="_csrf"]');

        const headerElement =
                document.querySelector(
                        'meta[name="_csrf_header"]'
                );

        if (!tokenElement || !headerElement) {
            return {};
        }

        return {
            [headerElement.content]: tokenElement.content
        };
    }

    function normalizeOptional(value) {
        if (value === null || value === undefined) {
            return null;
        }

        const normalized = String(value).trim();

        return normalized.length > 0
                ? normalized
                : null;
    }

    function buildSearchRequest() {
        return {
            code: null,
            typeCode:
                    normalizeOptional(
                            elements.typeCode?.value
                    ),
            domain:
                    normalizeOptional(
                            elements.domain?.value
                    ),
            classification:
                    normalizeOptional(
                            elements.classification?.value
                    ),
            riskLevel:
                    normalizeOptional(
                            elements.riskLevel?.value
                    ),
            status:
                    normalizeOptional(
                            elements.status?.value
                    ),
            contextType: null,
            contextReference: null,
            minimumConfidence: null,
            effectiveAt: null,
            text:
                    normalizeOptional(
                            elements.text?.value
                    ),
            page: state.page,
            size: state.size
        };
    }

    async function searchKnowledge() {
        if (state.loading) {
            return;
        }

        state.loading = true;
        showLoading();

        try {
            const response = await fetch(
                    "/api/knowledge/search",
                    {
                        method: "POST",
                        credentials: "same-origin",
                        headers: {
                            "Content-Type": "application/json",
                            "Accept": "application/json",
                            ...getCsrfHeaders()
                        },
                        body: JSON.stringify(
                                buildSearchRequest()
                        )
                    }
            );

            const payload = await parseResponse(response);

            if (!response.ok) {
                throw new Error(
                        payload?.message
                        || payload?.error
                        || "No fue posible consultar el conocimiento"
                );
            }

            renderPage(payload);

        } catch (error) {
            console.error(
                    "Knowledge search failed",
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

    async function parseResponse(response) {
        const contentType =
                response.headers.get("content-type") || "";

        if (!contentType.includes("application/json")) {
            return null;
        }

        return response.json();
    }

    function renderPage(page) {
        const content =
                Array.isArray(page?.content)
                        ? page.content
                        : [];

        state.page =
                Number.isInteger(page?.page)
                        ? page.page
                        : 0;

        state.totalPages =
                Number.isInteger(page?.totalPages)
                        ? page.totalPages
                        : 0;

        updateMetrics(page, content);
        updateResultSummary(page);
        updatePagination(page);

        if (content.length === 0) {
            showEmpty();
            return;
        }

        elements.tableBody.replaceChildren(
                ...content.map(createKnowledgeRow)
        );

        showTable();
    }

    function createKnowledgeRow(item) {
        const row = document.createElement("tr");

        const codeCell =
                createCell(
                        item.code || "—",
                        "knowledge-code-cell"
                );

        const knowledgeCell =
                document.createElement("td");

        const title =
                document.createElement("strong");

        title.textContent =
                item.title
                || "Sin título";

        const summary =
                document.createElement("p");

        summary.textContent =
                item.summary
                || "Sin resumen";

        knowledgeCell.append(
                title,
                summary
        );

        const statusCell =
                document.createElement("td");

        statusCell.append(
                createStatusBadge(item.status)
        );

        const domainCell =
                createCell(
                        formatEnum(item.domain)
                );

        const confidenceCell =
                createCell(
                        formatConfidence(
                                item.confidence
                        )
                );

        const updatedCell =
                createCell(
                        formatDateTime(
                                item.updatedAt
                                || item.createdAt
                        )
                );

        const actionsCell =
                document.createElement("td");

        actionsCell.className =
                "knowledge-actions-cell";

        const detailLink =
                document.createElement("a");

        detailLink.className =
                "button button-small button-secondary";

        detailLink.textContent =
                "Ver";

        detailLink.href =
                `/admin/knowledge/${encodeURIComponent(item.id)}`;

        actionsCell.append(detailLink);

        row.append(
                codeCell,
                knowledgeCell,
                statusCell,
                domainCell,
                confidenceCell,
                updatedCell,
                actionsCell
        );

        return row;
    }

    function createCell(
            value,
            className = null
    ) {
        const cell =
                document.createElement("td");

        cell.textContent =
                value ?? "—";

        if (className) {
            cell.className = className;
        }

        return cell;
    }

    function createStatusBadge(status) {
        const badge =
                document.createElement("span");

        const normalizedStatus =
                normalizeOptional(status)
                || "UNKNOWN";

        badge.className =
                `knowledge-status knowledge-status-${normalizedStatus.toLowerCase()}`;

        badge.textContent =
                formatEnum(normalizedStatus);

        return badge;
    }

    function formatEnum(value) {
        const normalized =
                normalizeOptional(value);

        if (!normalized) {
            return "—";
        }

        return normalized
                .toLowerCase()
                .split("_")
                .map(
                        word =>
                                word.charAt(0).toUpperCase()
                                + word.slice(1)
                )
                .join(" ");
    }

    function formatConfidence(value) {
        if (value === null
                || value === undefined
                || Number.isNaN(Number(value))) {

            return "—";
        }

        return new Intl.NumberFormat(
                "es-MX",
                {
                    style: "percent",
                    minimumFractionDigits: 0,
                    maximumFractionDigits: 2
                }
        ).format(Number(value));
    }

    function formatDateTime(value) {
        if (!value) {
            return "—";
        }

        const date = new Date(value);

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

    function updateMetrics(page, content) {
        elements.totalMetric.textContent =
                page?.totalElements ?? 0;

        /*
         * Estos tres valores representan la página actual.
         * Más adelante podemos crear un endpoint de métricas
         * globales por estado.
         */
        elements.draftMetric.textContent =
                countByStatus(
                        content,
                        "DRAFT"
                );

        elements.reviewMetric.textContent =
                countByStatus(
                        content,
                        "IN_REVIEW"
                );

        elements.publishedMetric.textContent =
                countByStatus(
                        content,
                        "PUBLISHED"
                );
    }

    function countByStatus(
            content,
            status
    ) {
        return content.filter(
                item => item.status === status
        ).length;
    }

    function updateResultSummary(page) {
        const total =
                page?.totalElements ?? 0;

        elements.resultSummary.textContent =
                total === 1
                        ? "1 objeto de conocimiento"
                        : `${total} objetos de conocimiento`;
    }

    function updatePagination(page) {
        const totalPages =
                page?.totalPages ?? 0;

        const currentPage =
                page?.page ?? 0;

        elements.pageIndicator.textContent =
                totalPages === 0
                        ? "Página 0 de 0"
                        : `Página ${currentPage + 1} de ${totalPages}`;

        elements.previousButton.disabled =
                Boolean(page?.first)
                || currentPage <= 0;

        elements.nextButton.disabled =
                Boolean(page?.last)
                || totalPages === 0
                || currentPage >= totalPages - 1;

        elements.pagination.hidden =
                totalPages <= 1;
    }
	

    function hideStates() {
        elements.loadingState.hidden = true;
        elements.errorState.hidden = true;
        elements.emptyState.hidden = true;
        elements.tableWrapper.hidden = true;
        elements.pagination.hidden = true;
    }

    function showLoading() {
        hideStates();

        elements.loadingState.hidden = false;
        elements.resultSummary.textContent =
                "Consultando conocimiento…";
    }

    function showError(message) {
        hideStates();

        elements.errorState.textContent = message;
        elements.errorState.hidden = false;

        elements.resultSummary.textContent =
                "No fue posible cargar los resultados";
    }

	function showEmpty() {
	    hideStates();

	    const hasActiveFilters =
	            Boolean(
	                    normalizeOptional(elements.text?.value)
	                    || normalizeOptional(elements.status?.value)
	                    || normalizeOptional(elements.domain?.value)
	                    || normalizeOptional(elements.typeCode?.value)
	                    || normalizeOptional(elements.classification?.value)
	                    || normalizeOptional(elements.riskLevel?.value)
	            );

	    elements.emptyTitle.textContent =
	            hasActiveFilters
	                    ? "No se encontraron coincidencias"
	                    : "Aún no hay conocimiento registrado";

	    elements.emptyDescription.textContent =
	            hasActiveFilters
	                    ? "Prueba con otros términos o limpia los filtros."
	                    : "Crea el primer objeto de conocimiento para comenzar a construir la base intelectual de la organización.";

	    elements.emptyState.hidden = false;

	    elements.resultSummary.textContent =
	            "0 objetos de conocimiento";
	}

    function showTable() {
        elements.loadingState.hidden = true;
        elements.errorState.hidden = true;
        elements.emptyState.hidden = true;
        elements.tableWrapper.hidden = false;

        if (state.totalPages > 1) {
            elements.pagination.hidden = false;
        }
    }

    function resetFilters() {
        elements.form.reset();
        state.page = 0;
        searchKnowledge();
    }

    elements.form.addEventListener(
            "submit",
            event => {
                event.preventDefault();
                state.page = 0;
                searchKnowledge();
            }
    );

    elements.refreshButton.addEventListener(
            "click",
            () => searchKnowledge()
    );

    elements.clearButton.addEventListener(
            "click",
            resetFilters
    );

    elements.previousButton.addEventListener(
            "click",
            () => {
                if (state.page <= 0) {
                    return;
                }

                state.page--;
                searchKnowledge();
            }
    );

    elements.nextButton.addEventListener(
            "click",
            () => {
                if (state.page >= state.totalPages - 1) {
                    return;
                }

                state.page++;
                searchKnowledge();
            }
    );

    elements.createButton.addEventListener(
            "click",
            () => {
                window.location.href =
                        "/admin/knowledge/new";
            }
    );

    searchKnowledge();
});