const API_URL = "/api/crm/tasks";

let allTasks = [];

export async function initTasks() {
    allTasks = await fetchTasks();
    renderTasks(allTasks);
    initTaskFilters();
    initCompleteButtons();
}

async function fetchTasks() {
    const response = await fetch(API_URL);

    if (!response.ok) {
        console.error("No se pudieron cargar las tareas");
        return [];
    }

    return await response.json();
}

function renderTasks(tasks) {
    clearTaskColumns();

    tasks.forEach(task => {
        const status = task.status || "PENDING";

        const column = document.querySelector(
            `.crm-task-column[data-status="${status}"] .crm-task-column-body`
        );

        if (!column) return;

        column.appendChild(createTaskCard(task));
    });

    updateTaskCounters();
}

function createTaskCard(task) {
    const card = document.createElement("article");
    card.className = "crm-task-card";

    card.innerHTML = `
        <div class="crm-task-title">${escapeHtml(task.title || "Tarea")}</div>
        <div class="crm-task-desc">${escapeHtml(task.description || "Sin descripción")}</div>

        <div class="crm-task-meta">
            <span>${formatDate(task.dueAt)}</span>
            <span class="crm-priority ${task.priority}">
                ${escapeHtml(task.priority || "MEDIUM")}
            </span>
        </div>

        ${
            task.status !== "COMPLETED"
                ? `<button class="crm-complete-task-btn" data-id="${task.id}" type="button">
                    Completar
                   </button>`
                : `<div class="crm-task-completed">✓ Completada</div>`
        }
    `;

    return card;
}

function initTaskFilters() {
    const searchInput = document.getElementById("crmTaskSearch");
    const statusFilter = document.getElementById("crmTaskStatusFilter");

    function applyFilters() {
        const search = searchInput.value.toLowerCase().trim();
        const status = statusFilter.value;

        const filtered = allTasks.filter(task => {
            const matchesSearch =
                String(task.title || "").toLowerCase().includes(search) ||
                String(task.description || "").toLowerCase().includes(search);

            const matchesStatus =
                !status || task.status === status;

            return matchesSearch && matchesStatus;
        });

        renderTasks(filtered);
    }

    searchInput.addEventListener("input", applyFilters);
    statusFilter.addEventListener("change", applyFilters);
}

function clearTaskColumns() {
    document.querySelectorAll(".crm-task-column-body").forEach(column => {
        column.innerHTML = "";
    });
}

function updateTaskCounters() {
    document.querySelectorAll(".crm-task-column").forEach(column => {
        const count = column.querySelectorAll(".crm-task-card").length;
        const counter = column.querySelector(".crm-column-header strong");

        if (counter) {
            counter.textContent = count;
        }
    });
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function formatDate(value) {
    if (!value) return "Sin fecha";

    return new Intl.DateTimeFormat("es-MX", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(value));
}

function initCompleteButtons() {
    document.addEventListener("click", async event => {
        const button = event.target.closest(".crm-complete-task-btn");

        if (!button) return;

        const taskId = button.dataset.id;

        const response = await fetch(`${API_URL}/${taskId}/complete`, {
            method: "PATCH"
        });

        if (!response.ok) {
            alert("No se pudo completar la tarea.");
            return;
        }

        allTasks = await fetchTasks();
        renderTasks(allTasks);
    });
}
