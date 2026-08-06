export function createKnowledgeDocumentController({
    elements,
    state
}) {

    function bindEvents() {
        elements.toggleTableOfContentsButton
                ?.addEventListener(
                        "click",
                        toggleTableOfContents
                );

        elements.searchInsideDocumentButton
                ?.addEventListener(
                        "click",
                        openDocumentSearch
                );

        elements.documentSearchForm
                ?.addEventListener(
                        "submit",
                        searchInsideDocument
                );

        elements.closeDocumentSearchButton
                ?.addEventListener(
                        "click",
                        closeDocumentSearch
                );
    }

    function renderDocument(version) {
        state.sectionObserver?.disconnect();

        elements.documentContent?.replaceChildren();

        if (!elements.documentContent) {
            return;
        }

        if (!version) {
            elements.documentContent.textContent =
                    "Sin contenido disponible.";

            renderTableOfContents();
            return;
        }

        if (version.renderedContent) {
            /*
             * El backend ya sanitizó este contenido.
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
            navigateToInitialHash();

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

    function toggleTableOfContents() {
        if (!elements.tableOfContentsPanel) {
            return;
        }

        elements.tableOfContentsPanel.hidden =
                !elements.tableOfContentsPanel.hidden;
    }

    function getDocumentHeadings() {
        if (!elements.documentContent) {
            return [];
        }

        return Array.from(
                elements.documentContent.querySelectorAll(
                        "h1, h2, h3, h4"
                )
        );
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

            heading.id =
                    candidateId;

            heading.tabIndex =
                    -1;
        });
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
            list.append(
                    createTableOfContentsItem(
                            heading
                    )
            );
        });

        elements.tableOfContents.append(list);
    }

    function createTableOfContentsItem(heading) {
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

        return item;
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
                () => {
                    heading.focus({
                        preventScroll: true
                    });
                },
                350
        );
    }

    function navigateToInitialHash() {
        const hash =
                window.location.hash
                        .replace(/^#/, "");

        if (!hash) {
            return;
        }

        const heading =
                document.getElementById(hash);

        if (!heading
                || !elements.documentContent.contains(heading)) {

            return;
        }

        window.setTimeout(
                () => {
                    heading.scrollIntoView({
                        block: "start"
                    });
                },
                50
        );
    }

    function observeDocumentSections() {
        state.sectionObserver?.disconnect();

        const headings =
                getDocumentHeadings();

        if (headings.length === 0
                || typeof IntersectionObserver === "undefined") {

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
                                                    (
                                                            first,
                                                            second
                                                    ) =>
                                                            first
                                                                    .boundingClientRect
                                                                    .top
                                                            - second
                                                                    .boundingClientRect
                                                                    .top
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
                            link.dataset.targetId
                                    === headingId
                    );
                });
    }

    function openDocumentSearch() {
        if (!elements.documentSearchDialog) {
            return;
        }

        if (elements.documentSearchResult) {
            elements.documentSearchResult.textContent =
                    "";
        }

        if (elements.documentSearchInput) {
            elements.documentSearchInput.value =
                    "";
        }

        elements.documentSearchDialog.showModal();

        window.setTimeout(
                () => {
                    elements.documentSearchInput
                            ?.focus();
                },
                50
        );
    }

    function closeDocumentSearch() {
        elements.documentSearchDialog?.close();
    }

    function searchInsideDocument(event) {
        event.preventDefault();

        const query =
                elements.documentSearchInput
                        ?.value
                        .trim()
                        .toLocaleLowerCase("es-MX")
                || "";

        if (!query) {
            setSearchMessage(
                    "Escribe un término de búsqueda."
            );

            return;
        }

        const matchingElement =
                findMatchingDocumentElement(
                        query
                );

        if (!matchingElement) {
            setSearchMessage(
                    "No se encontraron coincidencias."
            );

            return;
        }

        setSearchMessage(
                "Se encontró una coincidencia."
        );

        closeDocumentSearch();

        matchingElement.scrollIntoView({
            behavior: "smooth",
            block: "center"
        });

        temporarilyHighlightElement(
                matchingElement
        );
    }

    function findMatchingDocumentElement(query) {
        if (!elements.documentContent) {
            return null;
        }

        const candidates =
                elements.documentContent.querySelectorAll(
                        "h1, h2, h3, h4, p, li, "
                        + "blockquote, pre, td, th"
                );

        return Array.from(candidates)
                .find(
                        element =>
                                element.textContent
                                        .toLocaleLowerCase(
                                                "es-MX"
                                        )
                                        .includes(query)
                )
                || null;
    }

    function setSearchMessage(message) {
        if (elements.documentSearchResult) {
            elements.documentSearchResult.textContent =
                    message;
        }
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

    return {
        bindEvents,
        renderDocument
    };
}