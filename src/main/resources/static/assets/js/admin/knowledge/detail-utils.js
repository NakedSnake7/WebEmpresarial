export async function parseJsonResponse(response) {
    const contentType =
            response.headers.get("content-type") || "";

    if (!contentType.includes("application/json")) {
        return null;
    }

    return response.json();
}

export function getCsrfHeaders() {
    const token =
            document.querySelector(
                    'meta[name="_csrf"]'
            );

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

export function resolveDisplayVersion(knowledge) {
    if (!knowledge) {
        return null;
    }

    return knowledge.currentVersion
            ?? knowledge.latestVersion
            ?? null;
}

export function extractApiError(
        payload,
        fallbackMessage
) {
    if (Array.isArray(payload?.violations)
            && payload.violations.length > 0) {

        return payload.violations
                .map(violation => violation.message)
                .filter(Boolean)
                .join(" ");
    }

    return payload?.message
            || payload?.error
            || fallbackMessage;
}

export function formatEnum(value) {
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

export function formatDateTime(value) {
    if (!value) {
        return "—";
    }

    const date =
            new Date(value);

    if (Number.isNaN(date.getTime())) {
        return String(value);
    }

    return new Intl.DateTimeFormat(
            "es-MX",
            {
                dateStyle: "medium",
                timeStyle: "short"
            }
    ).format(date);
}

export function buildContextLabel(knowledge) {
    if (!knowledge?.contextType) {
        return "Contexto natural de la tienda";
    }

    return `${formatEnum(knowledge.contextType)} · ${
            knowledge.contextReference || "—"
    }`;
}

export function showElement(element) {
    if (element) {
        element.hidden = false;
    }
}

export function hideElement(element) {
    if (element) {
        element.hidden = true;
    }
}