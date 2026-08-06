import {
    extractApiError,
    getCsrfHeaders,
    parseJsonResponse
} from "./detail-utils.js";

export async function fetchKnowledgeDetail(
        knowledgeId
) {
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
            await parseJsonResponse(response);

    if (!response.ok) {
        throw new Error(
                extractApiError(
                        payload,
                        "No fue posible cargar el conocimiento"
                )
        );
    }

    return payload;
}

export async function fetchKnowledgeVersions(
        knowledgeId
) {
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
            await parseJsonResponse(response);

    if (!response.ok) {
        throw new Error(
                extractApiError(
                        payload,
                        "No fue posible cargar las versiones"
                )
        );
    }

    return Array.isArray(payload)
            ? payload
            : [];
}

export async function executeKnowledgeCommand(
        knowledgeId,
        path,
        options = {}
) {
    const hasBody =
            options.body !== undefined
            && options.body !== null;

    const response = await fetch(
            `/api/knowledge/${encodeURIComponent(knowledgeId)}/${path}`,
            {
                method: "POST",
                credentials: "same-origin",
                headers: {
                    "Accept": "application/json",
                    ...getCsrfHeaders(),
                    ...(hasBody
                            ? {
                                "Content-Type":
                                        "application/json"
                            }
                            : {})
                },
                body:
                        hasBody
                                ? JSON.stringify(options.body)
                                : undefined
            }
    );

    const payload =
            await parseJsonResponse(response);

    if (!response.ok) {
        throw new Error(
                extractApiError(
                        payload,
                        "No fue posible ejecutar la acción"
                )
        );
    }

    return payload;
}

export async function fetchKnowledgeVersionDetail(
        knowledgeId,
        versionId
) {
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

    return payload;
}