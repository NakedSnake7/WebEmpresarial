import { initPipeline } from "./pipeline.js";
import { initTasks } from "./tasks.js";

document.addEventListener("DOMContentLoaded", () => {
    const kanban = document.getElementById("crmKanban");
    const tasksBoard = document.getElementById("crmTasksBoard");

    if (kanban) {
        initPipeline();
    }

    if (tasksBoard) {
        initTasks();
    }
});