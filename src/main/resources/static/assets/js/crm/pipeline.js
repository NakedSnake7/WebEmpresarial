const API_URL = "/api/crm/leads";
const STATS_URL = "/api/crm/pipeline/stats";

let allLeads = [];
let currentLeadId = null;

export async function initPipeline() {
    allLeads = await fetchLeads();
    renderPipeline(allLeads);
    await refreshPipelineStats();
    initFilters();
    initDrawer();
}

async function fetchLeads() {
    const response = await fetch(API_URL);

    if (!response.ok) {
        console.error("No se pudieron cargar los leads");
        return [];
    }

    return await response.json();
}

function renderPipeline(leads) {
    clearColumns();

    leads.forEach(lead => {
        const column = document.querySelector(
            `.crm-kanban-column[data-status="${lead.status}"] .crm-column-body`
        );

        if (!column) return;

        column.appendChild(createLeadCard(lead));
    });

    updateCounters();
    initDragAndDrop();
}

function createLeadCard(lead) {
    const card = document.createElement("article");

    card.className = "crm-lead-card";
    card.draggable = true;
    card.dataset.leadId = lead.id;
    card.dataset.temperature = lead.temperature || "";

    card.innerHTML = `
        <div class="crm-lead-top">
            <div>
                <div class="crm-lead-name">${escapeHtml(lead.fullName || "Sin nombre")}</div>
                <div class="crm-lead-business">${escapeHtml(lead.businessName || "Sin empresa")}</div>
            </div>
            <span class="crm-badge ${lead.temperature}">
                ${lead.temperature || "COLD"}
            </span>
        </div>

        <div class="crm-lead-meta">
            <span>📞 ${escapeHtml(lead.phone || "Sin WhatsApp")}</span>
            <span>📍 ${escapeHtml(lead.source || "Sin origen")}</span>
        </div>

        <div class="crm-lead-footer">
            <span class="crm-score">Score ${lead.score || 0}</span>
            <button class="crm-card-action" type="button" data-id="${lead.id}">
                Ver
            </button>
        </div>
    `;

    return card;
}

function initDragAndDrop() {
    const cards = document.querySelectorAll(".crm-lead-card");
    const columns = document.querySelectorAll(".crm-kanban-column");

    cards.forEach(card => {
        card.addEventListener("dragstart", () => {
            card.classList.add("dragging");
        });

        card.addEventListener("dragend", () => {
            card.classList.remove("dragging");

            document.querySelectorAll(".crm-kanban-column").forEach(column => {
                column.classList.remove("drag-target");
            });
        });
    });

    columns.forEach(column => {

        if (column.dataset.dndReady === "true") return;
        column.dataset.dndReady = "true";

        column.addEventListener("dragover", event => {
            event.preventDefault();

            const dragging = document.querySelector(".dragging");
            const body = column.querySelector(".crm-column-body");

            column.classList.add("drag-target");

            if (dragging && body) {
                body.appendChild(dragging);
            }
        });

        column.addEventListener("dragleave", () => {
            column.classList.remove("drag-target");
        });

        column.addEventListener("drop", async () => {
            const dragging = document.querySelector(".dragging");

            if (!dragging) return;

            const leadId = dragging.dataset.leadId;
            const newStatus = column.dataset.status;

            column.classList.remove("drag-target");

            const success = await updateLeadStatus(leadId, newStatus);

            if (!success) {
                allLeads = await fetchLeads();
                renderPipeline(allLeads);
                return;
            }

			const lead = allLeads.find(item => String(item.id) === String(leadId));

			if (lead) {
			    lead.status = newStatus;
			}

			updateCounters();

			await refreshPipelineStats();

			showToast(
			    "success",
			    "Estado actualizado",
			    `Lead movido a ${newStatus}.`
			);

			if (currentLeadId && String(currentLeadId) === String(leadId)) {
			    await openLeadDrawer(leadId);
			}
        });
    });
}
async function updateLeadStatus(leadId, status) {
    const response = await fetch(`${API_URL}/${leadId}/status`, {
        method: "PATCH",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ status })
    });

    if (!response.ok) {
		showToast(
		    "error",
		    "No se pudo actualizar",
		    "El estado del lead no se guardó correctamente."
		);
		        return false;
    }

    return true;
}

function initFilters() {
    const searchInput = document.getElementById("crmSearchInput");
    const temperatureFilter = document.getElementById("crmTemperatureFilter");

    function applyFilters() {
        const search = searchInput.value.toLowerCase().trim();
        const temperature = temperatureFilter.value;

        const filtered = allLeads.filter(lead => {
            const matchesSearch =
                String(lead.fullName || "").toLowerCase().includes(search) ||
                String(lead.businessName || "").toLowerCase().includes(search) ||
                String(lead.phone || "").toLowerCase().includes(search) ||
                String(lead.source || "").toLowerCase().includes(search);

            const matchesTemperature =
                !temperature || lead.temperature === temperature;

            return matchesSearch && matchesTemperature;
        });

        renderPipeline(filtered);
    }

    searchInput.addEventListener("input", applyFilters);
    temperatureFilter.addEventListener("change", applyFilters);
}

function clearColumns() {
    document.querySelectorAll(".crm-column-body").forEach(body => {
        body.innerHTML = "";
    });
}

function updateCounters() {
    document.querySelectorAll(".crm-kanban-column").forEach(column => {
        const count = column.querySelectorAll(".crm-lead-card").length;
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

function initDrawer() {
    const drawer = document.getElementById("crmLeadDrawer");
    const backdrop = document.getElementById("crmDrawerBackdrop");
    const closeBtn = document.getElementById("crmDrawerClose");
    const saveNoteBtn = document.getElementById("crmSaveNoteBtn");
    const createTaskBtn = document.getElementById("crmCreateTaskBtn");

    document.addEventListener("click", async event => {
        const button = event.target.closest(".crm-card-action");

        if (!button) return;

        const leadId = button.dataset.id;

        await openLeadDrawer(leadId);
    });

    closeBtn?.addEventListener("click", closeDrawer);
    backdrop?.addEventListener("click", closeDrawer);

    saveNoteBtn?.addEventListener("click", saveNote);
    createTaskBtn?.addEventListener("click", createTask);
}
function openDrawerUI() {
    document.getElementById("crmLeadDrawer")?.classList.add("open");
    document.getElementById("crmDrawerBackdrop")?.classList.add("open");
}

async function openLeadDrawer(leadId) {
    currentLeadId = leadId;

    const response = await fetch(`${API_URL}/${leadId}`);

    if (!response.ok) {
		showToast(
		    "error",
		    "No se pudo cargar",
		    "No fue posible obtener el detalle del lead."
		);
		        return;
    }

    const lead = await response.json();

	fillDrawer(lead);
	openDrawerUI();
}

function closeDrawer() {
    document.getElementById("crmLeadDrawer")?.classList.remove("open");
    document.getElementById("crmDrawerBackdrop")?.classList.remove("open");
    currentLeadId = null;
}

function fillDrawer(lead) {
    setText("drawerLeadName", lead.fullName || "Sin nombre");
    setText("drawerLeadBusiness", lead.businessName || "Sin empresa");
    setText("drawerLeadPhone", lead.phone || "—");
    setText("drawerLeadStatus", lead.status || "—");
    setText("drawerLeadTemperature", lead.temperature || "—");
    setText("drawerLeadScore", lead.score ?? "0");
    setText("drawerLeadSource", lead.source || "—");
    setText("drawerLeadValue", formatMoney(lead.projectedValue));
    
	renderTasks(lead.tasks || []);
    renderTimeline(lead.activities || []);
}

function renderTimeline(activities) {
    const timeline = document.getElementById("crmTimeline");

    if (!timeline) return;

    if (!activities.length) {
        timeline.innerHTML = `<div class="crm-empty">Sin actividad todavía.</div>`;
        return;
    }

    timeline.innerHTML = activities.map(activity => `
        <div class="crm-timeline-item">
            <strong>${escapeHtml(activity.title || activity.type || "Actividad")}</strong>
            <p>${escapeHtml(activity.description || "")}</p>
            <small>${formatDate(activity.createdAt)}</small>
        </div>
    `).join("");
}

async function saveNote() {
    if (!currentLeadId) return;

    const input = document.getElementById("crmNoteInput");
    const note = input.value.trim();

    if (!note) return;

    const response = await fetch(`${API_URL}/${currentLeadId}/notes`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ note })
    });

    if (!response.ok) {
		showToast(
		    "error",
		    "Nota no guardada",
		    "Intenta nuevamente en unos segundos."
		);        return;
    }

	input.value = "";

	showToast(
	    "success",
	    "Nota guardada",
	    "La actividad fue agregada al timeline."
	);

	await openLeadDrawer(currentLeadId);
}

async function createTask() {
    if (!currentLeadId) return;

    const titleInput = document.getElementById("crmTaskTitle");
    const descriptionInput = document.getElementById("crmTaskDescription");
    const priorityInput = document.getElementById("crmTaskPriority");
    const dueAtInput = document.getElementById("crmTaskDueAt");

    const title = titleInput.value.trim();
    const description = descriptionInput.value.trim();
    const priority = priorityInput.value;
    const dueAt = dueAtInput.value;

    if (!title) {
        showToast(
            "info",
            "Falta el título",
            "Agrega un título para crear la tarea."
        );
        return;
    }

    const response = await fetch(`${API_URL}/${currentLeadId}/tasks`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            title,
            description,
            priority,
            dueAt: dueAt || null
        })
    });

    if (!response.ok) {
        showToast(
            "error",
            "Tarea no creada",
            "No fue posible guardar la tarea."
        );
        return;
    }

    titleInput.value = "";
    descriptionInput.value = "";
    dueAtInput.value = "";

    showToast(
        "success",
        "Tarea creada",
        "El seguimiento quedó registrado."
    );

    await openLeadDrawer(currentLeadId);
}

function setText(id, value) {
    const element = document.getElementById(id);

    if (element) {
        element.textContent = value;
    }
}

function formatMoney(value) {
    if (!value) return "$0";

    return new Intl.NumberFormat("es-MX", {
        style: "currency",
        currency: "MXN"
    }).format(value);
}

function formatDate(value) {
    if (!value) return "Sin fecha";

    return new Intl.DateTimeFormat("es-MX", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(new Date(value));
}

function renderTasks(tasks) {
    const container = document.getElementById("crmLeadTasks");

    if (!container) return;

    if (!tasks.length) {
        container.innerHTML = `<div class="crm-empty">Sin tareas registradas.</div>`;
        return;
    }

    container.innerHTML = tasks.map(task => `
        <article class="crm-task-mini">
            <div class="crm-task-mini-top">
                <strong>${escapeHtml(task.title || "Tarea")}</strong>
                <span class="crm-task-status ${task.status}">
                    ${escapeHtml(task.status || "PENDING")}
                </span>
            </div>

            <p>${escapeHtml(task.description || "Sin descripción")}</p>

            <div class="crm-task-mini-footer">
                <span>${formatDate(task.dueAt)}</span>
                <span class="crm-priority ${task.priority}">
                    ${escapeHtml(task.priority || "MEDIUM")}
                </span>
            </div>
        </article>
    `).join("");
}
function showToast(type = "info", title = "Listo", message = "") {
    const container = document.getElementById("crmToastContainer");

    if (!container) return;

    const toast = document.createElement("div");
    toast.className = `crm-toast ${type}`;

    toast.innerHTML = `
        <strong>${escapeHtml(title)}</strong>
        <span>${escapeHtml(message)}</span>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.classList.add("hide");

        setTimeout(() => {
            toast.remove();
        }, 240);
    }, 3200);
}

async function refreshPipelineStats() {
    const stats = await fetchPipelineStats();
    updatePipelineStats(stats);
}

async function fetchPipelineStats() {
    const response = await fetch(STATS_URL);

    if (!response.ok) {
        console.error("No se pudieron cargar las métricas del pipeline");
        return [];
    }

    return await response.json();
}

function updatePipelineStats(stats) {
    const normalized = new Map();

    stats.forEach(item => {
        normalized.set(item.status, {
            count: item.count || 0,
            value: item.value || 0
        });
    });

    document.querySelectorAll(".crm-kanban-column").forEach(column => {
        const status = column.dataset.status;
        const data = normalized.get(status) || {
            count: 0,
            value: 0
        };

        const counter = column.querySelector(".crm-column-header strong");
        const value = column.querySelector(`[data-value-status="${status}"]`);

        if (counter) {
            counter.textContent = data.count;
        }

        if (value) {
            value.textContent = formatMoney(data.value);
        }
    });
}

