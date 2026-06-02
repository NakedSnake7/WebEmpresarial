const API_URL = "/api/admin/saas";

document.addEventListener("DOMContentLoaded", async () => {
    const data = await fetchDashboard();
    renderDashboard(data);
});

async function fetchDashboard() {
    const response = await fetch(API_URL);

    if (!response.ok) {
        console.error("No se pudo cargar el dashboard SaaS");
        return null;
    }

    return await response.json();
}

function renderDashboard(data) {
    if (!data) return;

    setText("saasTotalStores", data.totalStores ?? 0);
    setText("saasActiveStores", data.activeStores ?? 0);

    setText("saasBasicStores", data.basicStores ?? 0);
    setText("saasProStores", data.proStores ?? 0);
    setText("saasPremiumStores", data.premiumStores ?? 0);

    setText("saasTotalLeads", data.totalLeads ?? 0);
    setText("saasTotalProposals", data.totalProposals ?? 0);

    setText("saasPipelineValue", formatMoney(data.globalPipelineValue));
    setText("saasRevenueForecast", formatMoney(data.globalRevenueForecast));

    setText("saasMRR", formatMoney(data.estimatedMRR));
    setText("saasARR", formatMoney(data.estimatedARR));
}

function setText(id, value) {
    const element = document.getElementById(id);
    if (element) element.textContent = value;
}

function formatMoney(value) {
    if (!value) return "$0";

    return new Intl.NumberFormat("es-MX", {
        style: "currency",
        currency: "MXN"
    }).format(value);
}