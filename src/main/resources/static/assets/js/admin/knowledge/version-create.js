document.addEventListener("DOMContentLoaded", () => {

    const page =
            document.querySelector(".version-editor-page");

    const knowledgeId =
            page?.dataset.knowledgeId;

    const state = {
        knowledge: null,
        initialValues: null,
        loading: false,
        submitting: false
    };

    const elements = {
        form:
                document.getElementById("knowledgeVersionForm"),

        loading:
                document.getElementById("versionEditorLoading"),

        error:
                document.getElementById("versionEditorError"),

        identity:
                document.getElementById("versionKnowledgeIdentity"),

        major:
                document.getElementById("versionMajor"),

        minor:
                document.getElementById("versionMinor"),

        patch:
                document.getElementById("versionPatch"),

        semanticPreview:
                document.getElementById("semanticVersionPreview"),

        title:
                document.getElementById("versionTitle"),

        summary:
                document.getElementById("versionSummary"),

        content:
                document.getElementById("versionContent"),

        contentFormat:
                document.getElementById("versionContentFormat"),

        confidence:
                document.getElementById("versionConfidence"),

        sourceReference:
                document.getElementById("versionSourceReference"),

        preview:
                document.getElementById("versionPreview"),

        sourceFormatLabel:
                document.getElementById("sourceFormatLabel"),

        characterCount:
                document.getElementById("versionCharacterCount"),

        resetButton:
                document.getElementById("resetVersionButton"),

        saveButton:
                document.getElementById("saveVersionButton")
    };

    if (!knowledgeId) {
        showFatalError(
                "No fue posible identificar el conocimiento"
        );
        return;
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

    function resolveLatestVersion(knowledge) {
        return knowledge?.latestVersion
                ?? knowledge?.currentVersion
                ?? null;
    }

    async function loadKnowledge() {
        if (state.loading) {
            return;
        }

        state.loading = true;

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

            const latestVersion =
                    resolveLatestVersion(payload);

            if (!latestVersion) {
                throw new Error(
                        "El conocimiento no contiene una versión editorial"
                );
            }

            state.knowledge = payload;

            populateEditor(
                    payload,
                    latestVersion
            );

        } catch (error) {
            console.error(
                    "Knowledge version editor load failed",
                    error
            );

            showFatalError(
                    error.message
                    || "No fue posible inicializar el editor"
            );

        } finally {
            state.loading = false;
        }
    }

    function populateEditor(
            knowledge,
            version
    ) {
        const semanticVersion =
                parseSemanticVersion(
                        version.semanticVersion
                );

        const nextVersion = {
            major: semanticVersion.major,
            minor: semanticVersion.minor,
            patch: semanticVersion.patch + 1
        };

        elements.identity.textContent =
                `${knowledge.code} · ${version.title}`;

        elements.major.value =
                nextVersion.major;

        elements.minor.value =
                nextVersion.minor;

        elements.patch.value =
                nextVersion.patch;

        elements.title.value =
                version.title || "";

        elements.summary.value =
                version.summary || "";

        elements.content.value =
                version.content || "";

        elements.contentFormat.value =
                version.contentFormat || "MARKDOWN";

        elements.confidence.value =
                version.confidence ?? "0.9500";

        elements.sourceReference.value =
                version.sourceReference || "";

        state.initialValues =
                captureEditorValues();

        updateEditorPresentation();

        elements.loading.hidden = true;
        elements.error.hidden = true;
        elements.form.hidden = false;
    }

    function parseSemanticVersion(value) {
        const parts =
                String(value || "1.0.0")
                        .split(".")
                        .map(part => Number(part));

        return {
            major:
                    Number.isInteger(parts[0])
                            ? parts[0]
                            : 1,

            minor:
                    Number.isInteger(parts[1])
                            ? parts[1]
                            : 0,

            patch:
                    Number.isInteger(parts[2])
                            ? parts[2]
                            : 0
        };
    }

    function getNumericValue(element) {
        const value =
                Number(element.value);

        return Number.isInteger(value)
                && value >= 0
                        ? value
                        : 0;
    }

    function buildSemanticVersion() {
        return [
            getNumericValue(elements.major),
            getNumericValue(elements.minor),
            getNumericValue(elements.patch)
        ].join(".");
    }

    function updateSemanticVersionPreview() {
        elements.semanticPreview.textContent =
                buildSemanticVersion();
    }

    function applyVersionBump(type) {
        const current =
                parseSemanticVersion(
                        resolveLatestVersion(
                                state.knowledge
                        )?.semanticVersion
                );

        if (type === "major") {
            elements.major.value =
                    current.major + 1;

            elements.minor.value = 0;
            elements.patch.value = 0;
        }

        if (type === "minor") {
            elements.major.value =
                    current.major;

            elements.minor.value =
                    current.minor + 1;

            elements.patch.value = 0;
        }

        if (type === "patch") {
            elements.major.value =
                    current.major;

            elements.minor.value =
                    current.minor;

            elements.patch.value =
                    current.patch + 1;
        }

        updateSemanticVersionPreview();
    }

    function updateCharacterCount() {
        const length =
                elements.content.value.length;

        elements.characterCount.textContent =
                length === 1
                        ? "1 carácter"
                        : `${length} caracteres`;
    }

    function updateFormatLabel() {
        const labels = {
            MARKDOWN: "Markdown",
            PLAIN_TEXT: "Texto plano",
            HTML: "HTML"
        };

        elements.sourceFormatLabel.textContent =
                labels[elements.contentFormat.value]
                || elements.contentFormat.value;
    }

    function updatePreview() {
        const format =
                elements.contentFormat.value;

        const content =
                elements.content.value;

        elements.preview.replaceChildren();

        /*
         * La fuente nunca se inserta con innerHTML.
         * La vista previa permanece segura en el navegador.
         * El backend generará el HTML definitivo al consultar
         * posteriormente la versión.
         */
        if (format === "PLAIN_TEXT") {
            const pre =
                    document.createElement("pre");

            pre.textContent = content;

            elements.preview.append(pre);
            return;
        }

        if (format === "HTML") {
            const warning =
                    document.createElement("p");

            warning.className =
                    "version-preview-warning";

            warning.textContent =
                    "El HTML será sanitizado por el servidor.";

            const pre =
                    document.createElement("pre");

            pre.textContent = content;

            elements.preview.append(
                    warning,
                    pre
            );

            return;
        }

        renderSafeMarkdownPreview(content);
    }

    function renderSafeMarkdownPreview(markdown) {
        const fragment =
                document.createDocumentFragment();

        const lines =
                String(markdown || "")
                        .split(/\r?\n/);

        let paragraphLines = [];

        function flushParagraph() {
            if (paragraphLines.length === 0) {
                return;
            }

            const paragraph =
                    document.createElement("p");

            paragraph.textContent =
                    paragraphLines.join(" ");

            fragment.append(paragraph);

            paragraphLines = [];
        }

        lines.forEach(line => {
            const headingMatch =
                    line.match(/^(#{1,4})\s+(.+)$/);

            if (headingMatch) {
                flushParagraph();

                const level =
                        headingMatch[1].length;

                const heading =
                        document.createElement(
                                `h${level}`
                        );

                heading.textContent =
                        headingMatch[2];

                fragment.append(heading);
                return;
            }

            if (!line.trim()) {
                flushParagraph();
                return;
            }

            paragraphLines.push(
                    line.trim()
            );
        });

        flushParagraph();

        if (!fragment.hasChildNodes()) {
            const empty =
                    document.createElement("p");

            empty.textContent =
                    "La vista previa aparecerá aquí.";

            fragment.append(empty);
        }

        elements.preview.append(fragment);
    }

    function updateEditorPresentation() {
        updateSemanticVersionPreview();
        updateCharacterCount();
        updateFormatLabel();
        updatePreview();
    }

    function captureEditorValues() {
        return {
            major: elements.major.value,
            minor: elements.minor.value,
            patch: elements.patch.value,
            title: elements.title.value,
            summary: elements.summary.value,
            content: elements.content.value,
            contentFormat: elements.contentFormat.value,
            confidence: elements.confidence.value,
            sourceReference: elements.sourceReference.value
        };
    }

    function restoreInitialValues() {
        if (!state.initialValues) {
            return;
        }

        const values =
                state.initialValues;

        elements.major.value = values.major;
        elements.minor.value = values.minor;
        elements.patch.value = values.patch;
        elements.title.value = values.title;
        elements.summary.value = values.summary;
        elements.content.value = values.content;
        elements.contentFormat.value = values.contentFormat;
        elements.confidence.value = values.confidence;
        elements.sourceReference.value = values.sourceReference;

        clearError();
        updateEditorPresentation();
    }

    function buildRequest() {
        return {
            major:
                    getNumericValue(elements.major),

            minor:
                    getNumericValue(elements.minor),

            patch:
                    getNumericValue(elements.patch),

            title:
                    elements.title.value.trim(),

            summary:
                    elements.summary.value.trim(),

            content:
                    elements.content.value.trim(),

            contentFormat:
                    elements.contentFormat.value,

            confidence:
                    Number(elements.confidence.value),

            sourceReference:
                    normalizeOptional(
                            elements.sourceReference.value
                    )
        };
    }

    function normalizeOptional(value) {
        const normalized =
                String(value ?? "").trim();

        return normalized.length > 0
                ? normalized
                : null;
    }

    function validateRequest(request) {
        if (!request.title) {
            throw new Error(
                    "El título es obligatorio"
            );
        }

        if (!request.summary) {
            throw new Error(
                    "El resumen es obligatorio"
            );
        }

        if (!request.content) {
            throw new Error(
                    "El contenido es obligatorio"
            );
        }

        if (!Number.isFinite(request.confidence)
                || request.confidence < 0
                || request.confidence > 1) {

            throw new Error(
                    "La confianza debe estar entre 0 y 1"
            );
        }
    }

    async function submitVersion(event) {
        event.preventDefault();

        if (state.submitting) {
            return;
        }

        clearError();

        try {
            const request =
                    buildRequest();

            validateRequest(request);

            state.submitting = true;
            setSubmitting(true);

            const response = await fetch(
                    `/api/knowledge/${encodeURIComponent(knowledgeId)}/versions`,
                    {
                        method: "POST",
                        credentials: "same-origin",
                        headers: {
                            "Content-Type": "application/json",
                            "Accept": "application/json",
                            ...getCsrfHeaders()
                        },
                        body: JSON.stringify(request)
                    }
            );

            const payload =
                    await parseResponse(response);

            if (!response.ok) {
                throw new Error(
                        extractError(payload)
                );
            }

            window.location.href =
                    `/admin/knowledge/${encodeURIComponent(knowledgeId)}`;

        } catch (error) {
            console.error(
                    "Knowledge version creation failed",
                    error
            );

            showError(
                    error.message
                    || "No fue posible crear la versión"
            );

        } finally {
            state.submitting = false;
            setSubmitting(false);
        }
    }

    function extractError(payload) {
        if (Array.isArray(payload?.violations)
                && payload.violations.length > 0) {

            return payload.violations
                    .map(violation => violation.message)
                    .join(" ");
        }

        return payload?.message
                || payload?.error
                || "No fue posible crear la versión";
    }

    function setSubmitting(submitting) {
        elements.saveButton.disabled = submitting;

        elements.saveButton.textContent =
                submitting
                        ? "Creando…"
                        : "Crear versión";

        elements.form
                .querySelectorAll(
                        "button[type='submit']"
                )
                .forEach(button => {
                    button.disabled = submitting;
                });
    }

    function showError(message) {
        elements.error.textContent = message;
        elements.error.hidden = false;

        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });
    }

    function clearError() {
        elements.error.textContent = "";
        elements.error.hidden = true;
    }

    function showFatalError(message) {
        elements.loading.hidden = true;
        elements.form.hidden = true;
        showError(message);
    }

    [
        elements.major,
        elements.minor,
        elements.patch
    ].forEach(element => {
        element.addEventListener(
                "input",
                updateSemanticVersionPreview
        );
    });

    [
        elements.title,
        elements.summary,
        elements.content,
        elements.contentFormat,
        elements.confidence,
        elements.sourceReference
    ].forEach(element => {
        element.addEventListener(
                "input",
                updateEditorPresentation
        );
    });

    document
            .querySelectorAll("[data-version-bump]")
            .forEach(button => {
                button.addEventListener(
                        "click",
                        () => applyVersionBump(
                                button.dataset.versionBump
                        )
                );
            });

    elements.resetButton.addEventListener(
            "click",
            restoreInitialValues
    );

    elements.form.addEventListener(
            "submit",
            submitVersion
    );

    loadKnowledge();
});