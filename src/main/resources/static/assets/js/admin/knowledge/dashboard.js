document.addEventListener(
        "DOMContentLoaded",
        loadKnowledgeDashboard
);

async function loadKnowledgeDashboard() {
    const elements = {
        total:
                document.getElementById(
                        "knowledgeDashboardTotal"
                ),

        draft:
                document.getElementById(
                        "knowledgeDashboardDraft"
                ),

        review:
                document.getElementById(
                        "knowledgeDashboardReview"
                ),

        published:
                document.getElementById(
                        "knowledgeDashboardPublished"
                )
    };

    try {
        const [
            total,
            draft,
            review,
            published
        ] = await Promise.all([
            fetchCount(),
            fetchCount("DRAFT"),
            fetchCount("IN_REVIEW"),
            fetchCount("PUBLISHED")
        ]);

        setText(
                elements.total,
                total
        );

        setText(
                elements.draft,
                draft
        );

        setText(
                elements.review,
                review
        );

        setText(
                elements.published,
                published
        );

    } catch (error) {
        console.error(
                "Knowledge dashboard metrics failed",
                error
        );

        Object.values(elements)
                .forEach(
                        element =>
                                setText(
                                        element,
                                        "—"
                                )
                );
    }
}

async function fetchCount(
        status = null
) {
    const params =
            new URLSearchParams({
                page: "0",
                size: "1"
            });

    if (status) {
        params.set(
                "status",
                status
        );
    }

    const response = await fetch(
            `/api/knowledge?${params.toString()}`,
            {
                credentials: "same-origin",
                headers: {
                    "Accept": "application/json"
                }
            }
    );

    if (!response.ok) {
        throw new Error(
                `No fue posible consultar la métrica ${status || "TOTAL"}`
        );
    }

    const payload =
            await response.json();

    return Number(
            payload.totalElements
            ?? payload.total
            ?? 0
    );
}

function setText(
        element,
        value
) {
    if (element) {
        element.textContent =
                String(value);
    }
}