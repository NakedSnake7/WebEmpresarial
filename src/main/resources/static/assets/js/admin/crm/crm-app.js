import { initPipeline } from "./pipeline.js";
import { initTasks } from "./tasks.js";
import { initDashboard } from "./dashboard.js";

document.addEventListener("DOMContentLoaded", () => {
    const kanban = document.getElementById("crmKanban");
    const tasksBoard = document.getElementById("crmTasksBoard");
    const dashboard = document.querySelector('[data-crm-view="dashboard"]');

    if (kanban) initPipeline();
    if (tasksBoard) initTasks();
    if (dashboard) initDashboard();
});