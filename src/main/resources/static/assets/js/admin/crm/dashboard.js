const DASHBOARD_API = "/api/crm/dashboard";
const ACTIVITY_API = "/api/crm/dashboard/activity";
const TASKS_API = "/api/crm/dashboard/tasks";

export async function initDashboard() {

    const [
        dashboard,
        activity,
        tasks
    ] = await Promise.all([
        fetchDashboard(),
        fetchActivity(),
        fetchTasks()
    ]);

    renderDashboard(dashboard);
    renderActivity(activity);
    renderTasks(tasks);
}

async function fetchDashboard() {
    const response = await fetch(DASHBOARD_API);

    if (!response.ok) {
        console.error("No se pudo cargar el dashboard CRM");
        return null;
    }

    return await response.json();
}

async function fetchActivity() {
    const response = await fetch(ACTIVITY_API);

    if (!response.ok) {
        return [];
    }

    return await response.json();
}

async function fetchTasks() {
    const response = await fetch(TASKS_API);

    if (!response.ok) {
        return [];
    }

    return await response.json();
}

function renderDashboard(data) {
    if (!data) return;

    setText("dashLeadsToday", data.leadsToday ?? 0);
    setText("dashLeadsWeek", data.leadsThisWeek ?? 0);
    setText("dashHotLeads", data.hotLeads ?? 0);
    setText("dashPendingTasks", data.pendingTasks ?? 0);
    setText("dashOverdueTasks", data.overdueTasks ?? 0);
    setText("dashSentProposals", data.sentProposals ?? 0);

    setText("dashPipelineValue", formatMoney(data.pipelineValue));
    setText("dashCloseRate", `${data.closeRate ?? 0}%`);
}

function renderActivity(items) {
    const container = document.getElementById("crmRecentActivity");

    if (!container) return;

    if (!items.length) {
        container.innerHTML = `
            <div class="crm-empty">
                Aún no hay actividad comercial registrada.
            </div>
        `;
        return;
    }

    container.innerHTML = items.map(item => `
        <div class="crm-feed-item">
            <div class="crm-feed-title">
                ${escapeHtml(item.title)}
            </div>

            <div class="crm-feed-description">
                ${escapeHtml(item.description || "")}
            </div>

            <div class="crm-feed-meta">
                ${escapeHtml(item.leadName)} · ${formatDate(item.createdAt)}
            </div>
        </div>
    `).join("");
}

function renderTasks(items) {
    const container = document.getElementById("crmUpcomingTasks");

    if (!container) return;

    if (!items.length) {
        container.innerHTML = `
            <div class="crm-empty">
                No tienes tareas pendientes.
            </div>
        `;
        return;
    }

    container.innerHTML = items.map(task => `
        <div class="crm-task-preview">
            <div class="crm-task-title">
                ${escapeHtml(task.title)}
            </div>

            <div class="crm-task-lead">
                ${escapeHtml(task.leadName)}
            </div>

            <div class="crm-task-date">
                ${formatDate(task.dueAt)}
            </div>
        </div>
    `).join("");
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

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}