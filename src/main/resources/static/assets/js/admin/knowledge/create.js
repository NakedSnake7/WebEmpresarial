document.addEventListener("DOMContentLoaded", () => {

    const form =
            document.getElementById("knowledgeCreateForm");

    const saveButton =
            document.getElementById("saveKnowledgeButton");

    const errorContainer =
            document.getElementById("knowledgeCreateError");

    const contextType =
            document.getElementById("knowledgeContextType");

    const contextReference =
            document.getElementById(
                    "knowledgeContextReference"
            );

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

    function normalizeOptional(value) {
        if (value === null || value === undefined) {
            return null;
        }

        const normalized =
                String(value).trim();

        return normalized.length > 0
                ? normalized
                : null;
    }

    function valueOf(id) {
        return document
                .getElementById(id)
                ?.value;
    }

    function buildRequest() {
        return {
            code:
                    normalizeOptional(
                            valueOf("knowledgeCode")
                    ),
            typeCode:
                    normalizeOptional(
                            valueOf("knowledgeTypeCode")
                    ),
            domain:
                    normalizeOptional(
                            valueOf("knowledgeDomain")
                    ),
            classification:
                    normalizeOptional(
                            valueOf("knowledgeClassification")
                    ),
            riskLevel:
                    normalizeOptional(
                            valueOf("knowledgeRiskLevel")
                    ),
            contextType:
                    normalizeOptional(
                            valueOf("knowledgeContextType")
                    ),
            contextReference:
                    normalizeOptional(
                            valueOf("knowledgeContextReference")
                    ),
            title:
                    normalizeOptional(
                            valueOf("knowledgeTitle")
                    ),
            summary:
                    normalizeOptional(
                            valueOf("knowledgeSummary")
                    ),
            content:
                    normalizeOptional(
                            valueOf("knowledgeContent")
                    ),
            contentFormat:
                    normalizeOptional(
                            valueOf("knowledgeContentFormat")
                    ),
            confidence:
                    Number(
                            valueOf("knowledgeConfidence")
                    ),
            sourceReference:
                    normalizeOptional(
                            valueOf("knowledgeSourceReference")
                    )
        };
    }

    function validateContext() {
        const type =
                normalizeOptional(contextType.value);

        const reference =
                normalizeOptional(contextReference.value);

        if ((type && !reference)
                || (!type && reference)) {

            throw new Error(
                    "El tipo y la referencia de contexto "
                    + "deben proporcionarse juntos"
            );
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

    function extractError(payload) {
        if (Array.isArray(payload?.violations)
                && payload.violations.length > 0) {

            return payload.violations
                    .map(
                            violation =>
                                    violation.message
                    )
                    .join(" ");
        }

        return payload?.message
                || payload?.error
                || "No fue posible crear el conocimiento";
    }

    function showError(message) {
        errorContainer.textContent = message;
        errorContainer.hidden = false;

        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });
    }

    function clearError() {
        errorContainer.textContent = "";
        errorContainer.hidden = true;
    }

    function setSubmitting(submitting) {
        saveButton.disabled = submitting;

        saveButton.textContent =
                submitting
                        ? "Creando…"
                        : "Crear conocimiento";
    }

    form.addEventListener(
            "submit",
            async event => {
                event.preventDefault();
                clearError();

                try {
                    validateContext();
                    setSubmitting(true);

                    const response = await fetch(
                            "/api/knowledge",
                            {
                                method: "POST",
                                credentials: "same-origin",
                                headers: {
                                    "Content-Type": "application/json",
                                    "Accept": "application/json",
                                    ...getCsrfHeaders()
                                },
                                body: JSON.stringify(
                                        buildRequest()
                                )
                            }
                    );

                    const payload =
                            await parseResponse(response);

                    if (!response.ok) {
                        throw new Error(
                                extractError(payload)
                        );
                    }

                    if (!payload?.id) {
                        throw new Error(
                                "La API no devolvió el identificador "
                                + "del conocimiento creado"
                        );
                    }

                    window.location.href =
                            `/admin/knowledge/${encodeURIComponent(payload.id)}`;

                } catch (error) {
                    console.error(
                            "Knowledge creation failed",
                            error
                    );

                    showError(
                            error.message
                            || "Ocurrió un error inesperado"
                    );

                } finally {
                    setSubmitting(false);
                }
            }
    );
});