import {
    fetchKnowledgeVersionDetail
} from "./detail-api.js";

export function createKnowledgeDiffController({
    elements,
    knowledgeId,
    onDiffOpened,
    onDiffClosed
}) {

    const state = {
        baseVersion: null,
        targetVersion: null,
        loading: false
    };

    function bindEvents() {
        elements.closeDiffButton
                ?.addEventListener(
                        "click",
                        closeDiff
                );

        elements.swapDiffVersionsButton
                ?.addEventListener(
                        "click",
                        swapVersions
                );

        elements.diffModeButtons
                ?.forEach(button => {
                    button.addEventListener(
                            "click",
                            () => setDiffMode(
                                    button.dataset.diffMode
                            )
                    );
                });
    }

    async function compareVersions(
            baseVersionId,
            targetVersionId
    ) {
        if (!baseVersionId || !targetVersionId) {
            throw new Error(
                    "Debes seleccionar dos versiones para comparar"
            );
        }

        if (String(baseVersionId)
                === String(targetVersionId)) {

            throw new Error(
                    "Selecciona dos versiones diferentes"
            );
        }

        if (state.loading) {
            return;
        }

        state.loading = true;
        showLoading();

        try {
            const [
                baseVersion,
                targetVersion
            ] = await Promise.all([
                fetchKnowledgeVersionDetail(
                        knowledgeId,
                        baseVersionId
                ),

                fetchKnowledgeVersionDetail(
                        knowledgeId,
                        targetVersionId
                )
            ]);

            state.baseVersion =
                    baseVersion;

            state.targetVersion =
                    targetVersion;

            renderComparison();

            elements.diffPanel.hidden =
                    false;

            onDiffOpened?.({
                baseVersion,
                targetVersion
            });

            window.requestAnimationFrame(
                    () => {
                        elements.diffPanel.scrollIntoView({
                            behavior: "smooth",
                            block: "start"
                        });
                    }
            );

        } catch (error) {
            console.error(
                    "Knowledge diff load failed",
                    error
            );

            showError(
                    error.message
                    || "No fue posible comparar las versiones"
            );

            throw error;

        } finally {
            state.loading = false;
        }
    }

    function renderComparison() {
        const base =
                state.baseVersion;

        const target =
                state.targetVersion;

        if (!base || !target) {
            return;
        }

        hideLoading();
        hideError();

        setText(
                elements.diffBaseVersion,
                `v${base.semanticVersion || "—"}`
        );

        setText(
                elements.diffTargetVersion,
                `v${target.semanticVersion || "—"}`
        );

        setText(
                elements.diffBaseTitle,
                base.title || "Sin título"
        );

        setText(
                elements.diffTargetTitle,
                target.title || "Sin título"
        );

        renderFieldChanges(
                base,
                target
        );

        renderUnifiedDiff(
                base.content || "",
                target.content || ""
        );

        renderSideBySideDiff(
                base.content || "",
                target.content || ""
        );

        updateDiffSummary(
                base.content || "",
                target.content || ""
        );

        setDiffMode(
                "unified"
        );

        elements.diffContent.hidden =
                false;
    }

    function renderFieldChanges(
            base,
            target
    ) {
        if (!elements.diffMetadataChanges) {
            return;
        }

        elements.diffMetadataChanges
                .replaceChildren();

        const fields = [
            {
                label: "Título",
                before: base.title,
                after: target.title
            },
            {
                label: "Resumen",
                before: base.summary,
                after: target.summary
            },
            {
                label: "Formato",
                before: base.contentFormat,
                after: target.contentFormat
            },
            {
                label: "Confianza",
                before: formatConfidence(
                        base.confidence
                ),
                after: formatConfidence(
                        target.confidence
                )
            },
            {
                label: "Fuente",
                before: base.sourceReference,
                after: target.sourceReference
            }
        ];

        const changedFields =
                fields.filter(
                        field =>
                                normalizeValue(field.before)
                                !== normalizeValue(field.after)
                );

        if (changedFields.length === 0) {
            const empty =
                    document.createElement("p");

            empty.className =
                    "knowledge-diff-empty";

            empty.textContent =
                    "No hubo cambios en los metadatos editoriales.";

            elements.diffMetadataChanges.append(
                    empty
            );

            return;
        }

        changedFields.forEach(field => {
            elements.diffMetadataChanges.append(
                    createMetadataChange(field)
            );
        });
    }

    function createMetadataChange(field) {
        const article =
                document.createElement("article");

        article.className =
                "knowledge-diff-field";

        const label =
                document.createElement("strong");

        label.textContent =
                field.label;

        const values =
                document.createElement("div");

        values.className =
                "knowledge-diff-field-values";

        const before =
                document.createElement("div");

        before.className =
                "knowledge-diff-field-before";

        before.append(
                createDiffValueLabel("Antes"),
                createDiffValue(
                        field.before,
                        "removed"
                )
        );

        const after =
                document.createElement("div");

        after.className =
                "knowledge-diff-field-after";

        after.append(
                createDiffValueLabel("Después"),
                createDiffValue(
                        field.after,
                        "added"
                )
        );

        values.append(
                before,
                after
        );

        article.append(
                label,
                values
        );

        return article;
    }

    function createDiffValueLabel(label) {
        const element =
                document.createElement("span");

        element.className =
                "knowledge-diff-value-label";

        element.textContent =
                label;

        return element;
    }

    function createDiffValue(
            value,
            type
    ) {
        const element =
                document.createElement("p");

        element.className =
                `knowledge-diff-value `
                + `knowledge-diff-value-${type}`;

        element.textContent =
                normalizeValue(value)
                || "—";

        return element;
    }

    function renderUnifiedDiff(
            baseContent,
            targetContent
    ) {
        if (!elements.diffUnifiedBody) {
            return;
        }

        elements.diffUnifiedBody
                .replaceChildren();

        const operations =
                calculateLineDiff(
                        baseContent,
                        targetContent
                );

        let baseLineNumber = 1;
        let targetLineNumber = 1;

        operations.forEach(operation => {
            const row =
                    document.createElement("div");

            row.className =
                    `knowledge-diff-line `
                    + `knowledge-diff-line-${operation.type}`;

            const baseNumber =
                    document.createElement("span");

            baseNumber.className =
                    "knowledge-diff-line-number";

            const targetNumber =
                    document.createElement("span");

            targetNumber.className =
                    "knowledge-diff-line-number";

            const symbol =
                    document.createElement("span");

            symbol.className =
                    "knowledge-diff-symbol";

            const content =
                    document.createElement("code");

            content.textContent =
                    operation.line || " ";

            if (operation.type === "unchanged") {
                baseNumber.textContent =
                        String(baseLineNumber++);

                targetNumber.textContent =
                        String(targetLineNumber++);

                symbol.textContent = " ";
            }

            if (operation.type === "removed") {
                baseNumber.textContent =
                        String(baseLineNumber++);

                targetNumber.textContent = "";

                symbol.textContent = "−";
            }

            if (operation.type === "added") {
                baseNumber.textContent = "";

                targetNumber.textContent =
                        String(targetLineNumber++);

                symbol.textContent = "+";
            }

            row.append(
                    baseNumber,
                    targetNumber,
                    symbol,
                    content
            );

            elements.diffUnifiedBody.append(
                    row
            );
        });
    }

    function renderSideBySideDiff(
            baseContent,
            targetContent
    ) {
        if (!elements.diffSideBySideBody) {
            return;
        }

        elements.diffSideBySideBody
                .replaceChildren();

        const operations =
                calculateLineDiff(
                        baseContent,
                        targetContent
                );

        const rows =
                buildSideBySideRows(
                        operations
                );

        let baseLineNumber = 1;
        let targetLineNumber = 1;

        rows.forEach(rowData => {
            const row =
                    document.createElement("div");

            row.className =
                    "knowledge-diff-side-row";

            const before =
                    createSideCell(
                            rowData.before,
                            baseLineNumber,
                            "before"
                    );

            const after =
                    createSideCell(
                            rowData.after,
                            targetLineNumber,
                            "after"
                    );

            if (rowData.before !== null) {
                baseLineNumber++;
            }

            if (rowData.after !== null) {
                targetLineNumber++;
            }

            row.append(
                    before,
                    after
            );

            elements.diffSideBySideBody.append(
                    row
            );
        });
    }

    function buildSideBySideRows(operations) {
        const rows = [];

        let index = 0;

        while (index < operations.length) {
            const current =
                    operations[index];

            if (current.type === "unchanged") {
                rows.push({
                    before: {
                        line: current.line,
                        type: "unchanged"
                    },
                    after: {
                        line: current.line,
                        type: "unchanged"
                    }
                });

                index++;
                continue;
            }

            const removed = [];
            const added = [];

            while (index < operations.length
                    && operations[index].type !== "unchanged") {

                if (operations[index].type === "removed") {
                    removed.push(
                            operations[index]
                    );
                }

                if (operations[index].type === "added") {
                    added.push(
                            operations[index]
                    );
                }

                index++;
            }

            const maximum =
                    Math.max(
                            removed.length,
                            added.length
                    );

            for (
                let rowIndex = 0;
                rowIndex < maximum;
                rowIndex++
            ) {
                rows.push({
                    before:
                            removed[rowIndex]
                            ? {
                                line:
                                        removed[rowIndex].line,
                                type:
                                        "removed"
                            }
                            : null,

                    after:
                            added[rowIndex]
                            ? {
                                line:
                                        added[rowIndex].line,
                                type:
                                        "added"
                            }
                            : null
                });
            }
        }

        return rows;
    }

    function createSideCell(
            data,
            lineNumber,
            side
    ) {
        const cell =
                document.createElement("div");

        cell.className =
                `knowledge-diff-side-cell `
                + `knowledge-diff-side-cell-${side}`;

        if (!data) {
            cell.classList.add(
                    "is-empty"
            );

            return cell;
        }

        cell.classList.add(
                `knowledge-diff-line-${data.type}`
        );

        const number =
                document.createElement("span");

        number.className =
                "knowledge-diff-line-number";

        number.textContent =
                String(lineNumber);

        const content =
                document.createElement("code");

        content.textContent =
                data.line || " ";

        cell.append(
                number,
                content
        );

        return cell;
    }

    function calculateLineDiff(
            baseContent,
            targetContent
    ) {
        const beforeLines =
                normalizeLines(
                        baseContent
                );

        const afterLines =
                normalizeLines(
                        targetContent
                );

        const table =
                buildLcsTable(
                        beforeLines,
                        afterLines
                );

        return buildDiffOperations(
                beforeLines,
                afterLines,
                table
        );
    }

    function buildLcsTable(
            beforeLines,
            afterLines
    ) {
        const rows =
                beforeLines.length + 1;

        const columns =
                afterLines.length + 1;

        const table =
                Array.from(
                        { length: rows },
                        () => new Uint32Array(columns)
                );

        for (
            let beforeIndex =
                    beforeLines.length - 1;
            beforeIndex >= 0;
            beforeIndex--
        ) {
            for (
                let afterIndex =
                        afterLines.length - 1;
                afterIndex >= 0;
                afterIndex--
            ) {
                if (beforeLines[beforeIndex]
                        === afterLines[afterIndex]) {

                    table[beforeIndex][afterIndex] =
                            table[beforeIndex + 1][afterIndex + 1]
                            + 1;

                } else {
                    table[beforeIndex][afterIndex] =
                            Math.max(
                                    table[beforeIndex + 1][afterIndex],
                                    table[beforeIndex][afterIndex + 1]
                            );
                }
            }
        }

        return table;
    }

    function buildDiffOperations(
            beforeLines,
            afterLines,
            table
    ) {
        const operations = [];

        let beforeIndex = 0;
        let afterIndex = 0;

        while (beforeIndex < beforeLines.length
                && afterIndex < afterLines.length) {

            if (beforeLines[beforeIndex]
                    === afterLines[afterIndex]) {

                operations.push({
                    type: "unchanged",
                    line: beforeLines[beforeIndex]
                });

                beforeIndex++;
                afterIndex++;
                continue;
            }

            if (table[beforeIndex + 1][afterIndex]
                    >= table[beforeIndex][afterIndex + 1]) {

                operations.push({
                    type: "removed",
                    line: beforeLines[beforeIndex]
                });

                beforeIndex++;

            } else {
                operations.push({
                    type: "added",
                    line: afterLines[afterIndex]
                });

                afterIndex++;
            }
        }

        while (beforeIndex < beforeLines.length) {
            operations.push({
                type: "removed",
                line: beforeLines[beforeIndex++]
            });
        }

        while (afterIndex < afterLines.length) {
            operations.push({
                type: "added",
                line: afterLines[afterIndex++]
            });
        }

        return operations;
    }

    function updateDiffSummary(
            baseContent,
            targetContent
    ) {
        const operations =
                calculateLineDiff(
                        baseContent,
                        targetContent
                );

        const added =
                operations.filter(
                        operation =>
                                operation.type === "added"
                ).length;

        const removed =
                operations.filter(
                        operation =>
                                operation.type === "removed"
                ).length;

        const unchanged =
                operations.filter(
                        operation =>
                                operation.type === "unchanged"
                ).length;

        setText(
                elements.diffAddedCount,
                `+${added}`
        );

        setText(
                elements.diffRemovedCount,
                `−${removed}`
        );

        setText(
                elements.diffUnchangedCount,
                String(unchanged)
        );
    }

    function setDiffMode(mode) {
        const normalizedMode =
                mode === "side-by-side"
                ? "side-by-side"
                : "unified";

        elements.diffUnifiedView.hidden =
                normalizedMode !== "unified";

        elements.diffSideBySideView.hidden =
                normalizedMode !== "side-by-side";

        elements.diffModeButtons
                ?.forEach(button => {
                    const active =
                            button.dataset.diffMode
                            === normalizedMode;

                    button.classList.toggle(
                            "is-active",
                            active
                    );

                    button.setAttribute(
                            "aria-pressed",
                            String(active)
                    );
                });
    }

    function swapVersions() {
        if (!state.baseVersion
                || !state.targetVersion) {

            return;
        }

        const previousBase =
                state.baseVersion;

        state.baseVersion =
                state.targetVersion;

        state.targetVersion =
                previousBase;

        renderComparison();
    }

    function closeDiff() {
        state.baseVersion = null;
        state.targetVersion = null;

        elements.diffPanel.hidden =
                true;

        elements.diffContent.hidden =
                true;

        hideError();

        onDiffClosed?.();
    }

    function showLoading() {
        elements.diffPanel.hidden =
                false;

        elements.diffLoading.hidden =
                false;

        elements.diffError.hidden =
                true;

        elements.diffContent.hidden =
                true;
    }

    function hideLoading() {
        elements.diffLoading.hidden =
                true;
    }

    function showError(message) {
        hideLoading();

        elements.diffPanel.hidden =
                false;

        elements.diffContent.hidden =
                true;

        elements.diffError.textContent =
                message;

        elements.diffError.hidden =
                false;
    }

    function hideError() {
        elements.diffError.hidden =
                true;
    }

    function normalizeLines(value) {
        return String(value ?? "")
                .replace(/\r\n/g, "\n")
                .replace(/\r/g, "\n")
                .split("\n");
    }

    function normalizeValue(value) {
        return String(value ?? "")
                .trim();
    }

    function formatConfidence(value) {
        const numeric =
                Number(value);

        if (!Number.isFinite(numeric)) {
            return "—";
        }

        return `${Math.max(
                0,
                Math.min(100, numeric * 100)
        ).toFixed(2)}%`;
    }

    function setText(
            element,
            value
    ) {
        if (element) {
            element.textContent =
                    value;
        }
    }

    return {
        bindEvents,
        closeDiff,
        compareVersions
    };
}