const statsTiles = document.getElementById("statsTiles");

const CHART_TEXT_COLOR = "#9aa4bb";
const CHART_GRID_COLOR = "#2a3348";
const CHART_ACCENT_COLOR = "#5b8def";
const CHART_GOOD_COLOR = "#3ecf8e";

let scoreTrendChart = null;
let activityChart = null;

function renderStatsTiles(stats) {
    const tiles = [
        { label: "総レビュー数", value: `${stats.totalReviews} 件` },
        { label: "平均スコア", value: stats.averageScore !== null && stats.averageScore !== undefined ? `${stats.averageScore.toFixed(1)} 点` : "-" },
        { label: "連続学習日数", value: `${stats.streakDays} 日` },
    ];
    if (stats.daysRemaining !== null && stats.daysRemaining !== undefined) {
        tiles.push({ label: "目標まで", value: stats.daysRemaining >= 0 ? `${stats.daysRemaining} 日` : `${-stats.daysRemaining} 日超過` });
    }
    if (stats.progressPercent !== null && stats.progressPercent !== undefined) {
        tiles.push({ label: "期間の進捗", value: `${stats.progressPercent}%` });
    }

    statsTiles.innerHTML = "";
    tiles.forEach(tile => {
        const div = document.createElement("div");
        div.className = "stat-tile";
        div.innerHTML = `<div class="stat-value">${tile.value}</div><div class="stat-label">${tile.label}</div>`;
        statsTiles.appendChild(div);
    });
}

function renderScoreTrendChart(scoreTrend) {
    const ctx = document.getElementById("scoreTrendChart");
    const labels = scoreTrend.map(p => p.date);
    const data = scoreTrend.map(p => p.averageScore);

    if (scoreTrendChart) {
        scoreTrendChart.destroy();
    }
    scoreTrendChart = new Chart(ctx, {
        type: "line",
        data: {
            labels,
            datasets: [{
                label: "平均スコア",
                data,
                borderColor: CHART_ACCENT_COLOR,
                backgroundColor: "rgba(91, 141, 239, 0.2)",
                tension: 0.25,
                fill: true,
                pointRadius: 3,
            }]
        },
        options: {
            responsive: true,
            scales: {
                x: { ticks: { color: CHART_TEXT_COLOR }, grid: { color: CHART_GRID_COLOR } },
                y: { min: 0, max: 100, ticks: { color: CHART_TEXT_COLOR }, grid: { color: CHART_GRID_COLOR } }
            },
            plugins: { legend: { labels: { color: CHART_TEXT_COLOR } } }
        }
    });
}

function renderActivityChart(dailyActivity) {
    const ctx = document.getElementById("activityChart");
    const labels = dailyActivity.map(p => p.date.slice(5));
    const data = dailyActivity.map(p => p.count);

    if (activityChart) {
        activityChart.destroy();
    }
    activityChart = new Chart(ctx, {
        type: "bar",
        data: {
            labels,
            datasets: [{
                label: "提出件数",
                data,
                backgroundColor: CHART_GOOD_COLOR,
            }]
        },
        options: {
            responsive: true,
            scales: {
                x: { ticks: { color: CHART_TEXT_COLOR }, grid: { display: false } },
                y: { beginAtZero: true, ticks: { color: CHART_TEXT_COLOR, precision: 0 }, grid: { color: CHART_GRID_COLOR } }
            },
            plugins: { legend: { labels: { color: CHART_TEXT_COLOR } } }
        }
    });
}

async function loadStats() {
    try {
        const res = await fetch("/api/stats");
        const stats = await res.json();
        renderStatsTiles(stats);
        renderScoreTrendChart(stats.scoreTrend || []);
        renderActivityChart(stats.dailyActivity || []);
    } catch (e) {
        statsTiles.innerHTML = `<p class="empty">統計の取得に失敗しました: ${e.message}</p>`;
    }
}

// 進捗タブは display:none の間はcanvasのサイズが取れずグラフが崩れるため、
// タブが実際に表示されてから(nav.jsのタブ切り替えの後)読み込む
document.querySelector('.tab-btn[data-tab="stats"]').addEventListener("click", loadStats);
