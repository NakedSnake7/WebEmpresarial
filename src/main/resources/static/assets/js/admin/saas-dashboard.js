document.addEventListener("DOMContentLoaded", () => {
    const data = window.saasDashboard;

    if (!data) {
        console.warn("No hay datos para SaaS Dashboard");
        return;
    }

    createPlansChart(data);
    createFeatureUsageChart(data);
    createTopStoresChart(data);
    createMrrChart(data);
});

const CHART_COLORS = {
    primary: "#2563EB",
    success: "#10B981",
    warning: "#F59E0B",
    danger: "#EF4444",
    purple: "#7C3AED",
    dark: "#111827",
    muted: "#6B7280"
};

function createPlansChart(data) {
    const canvas = document.getElementById("plansChart");
    if (!canvas) return;

    new Chart(canvas, {
        type: "doughnut",
        data: {
            labels: ["Basic", "Pro", "Premium"],
            datasets: [{
                data: [
                    data.basicStores ?? 0,
                    data.proStores ?? 0,
                    data.premiumStores ?? 0
                ],
                backgroundColor: [
                    CHART_COLORS.muted,
                    CHART_COLORS.primary,
                    CHART_COLORS.purple
                ],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            cutout: "68%",
            plugins: {
                legend: {
                    position: "bottom"
                }
            },
            animation: {
                duration: 1200
            }
        }
    });
}

function createFeatureUsageChart(data) {
    const canvas = document.getElementById("featureUsageChart");
    if (!canvas) return;

    new Chart(canvas, {
        type: "bar",
        data: {
            labels: data.featureLabels ?? [],
            datasets: [{
                label: "Usos últimos 30 días",
                data: data.featureTotals ?? [],
                backgroundColor: CHART_COLORS.primary,
                borderRadius: 8
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        precision: 0
                    }
                }
            },
            animation: {
                duration: 1200
            }
        }
    });
}

function createTopStoresChart(data) {
    const canvas = document.getElementById("topStoresChart");
    if (!canvas) return;

    new Chart(canvas, {
        type: "bar",
        data: {
            labels: data.topStoreLabels ?? [],
            datasets: [{
                label: "Eventos últimos 30 días",
                data: data.topStoreTotals ?? [],
                backgroundColor: CHART_COLORS.success,
                borderRadius: 8
            }]
        },
        options: {
            indexAxis: "y",
            responsive: true,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                x: {
                    beginAtZero: true,
                    ticks: {
                        precision: 0
                    }
                }
            },
            animation: {
                duration: 1200
            }
        }
    });
}

function createMrrChart(data) {
    const canvas = document.getElementById("mrrChart");
    if (!canvas) return;

    new Chart(canvas, {
        type: "line",
        data: {
            labels: data.mrrLabels ?? [],
            datasets: [{
                label: "MRR",
                data: data.mrrValues ?? [],
                borderColor: CHART_COLORS.primary,
                backgroundColor: "rgba(37, 99, 235, 0.12)",
                fill: true,
                tension: 0.35,
                pointRadius: 3,
                pointHoverRadius: 6
            }]
        },
        options: {
            responsive: true,
            interaction: {
                intersect: false,
                mode: "index"
            },
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: context => {
                            return formatMoney(context.parsed.y);
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: value => formatMoney(value)
                    }
                }
            },
            animation: {
                duration: 1200
            }
        }
    });
}

function formatMoney(value) {
    return new Intl.NumberFormat("es-MX", {
        style: "currency",
        currency: "MXN",
        maximumFractionDigits: 0
    }).format(value ?? 0);
}