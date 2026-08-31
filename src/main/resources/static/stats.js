const statsTiles = document.getElementById("statsTiles");
const scoreTrendCanvas = document.getElementById("scoreTrendChart");
const activityCanvas = document.getElementById("activityChart");

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
        tiles.push({
            label: "目標まで",
            value: stats.daysRemaining >= 0 ? `${stats.daysRemaining} 日` : `${-stats.daysRemaining} 日超過`,
            overdue: stats.daysRemaining < 0,
        });
    }
    if (stats.progressPercent !== null && stats.progressPercent !== undefined) {
        tiles.push({ label: "期間の進捗", value: `${stats.progressPercent}%` });
    }

    statsTiles.innerHTML = "";
    tiles.forEach(tile => {
        const div = document.createElement("div");
        div.className = "stat-tile";
        div.innerHTML = `<div class="stat-value${tile.overdue ? " overdue" : ""}">${tile.value}</div><div class="stat-label">${tile.label}</div>`;
        statsTiles.appendChild(div);
    });
}

function renderScoreTrendChart(scoreTrend) {
    const ctx = scoreTrendCanvas;
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
    const ctx = activityCanvas;
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

/** Chart.jsはCDN経由のため、オフライン等で読み込めないことがある。
    その場合でも自分のAPIから取れた集計値(タイル)は表示し、グラフだけ
    「読み込めなかった」ことが分かる状態にする(統計取得自体の失敗と混同させない) */
function renderChartsOrFallback(stats) {
    if (typeof Chart === "undefined") {
        [scoreTrendCanvas, activityCanvas].forEach(canvas => {
            canvas.hidden = true;
            const container = canvas.closest(".chart-container");
            let fallback = container.querySelector(".chart-unavailable");
            if (!fallback) {
                fallback = document.createElement("p");
                fallback.className = "empty chart-unavailable";
                container.appendChild(fallback);
            }
            fallback.textContent = "グラフの読み込みに失敗しました(インターネット接続を確認してください)";
        });
        return;
    }
    [scoreTrendCanvas, activityCanvas].forEach(canvas => {
        canvas.hidden = false;
        const fallback = canvas.closest(".chart-container").querySelector(".chart-unavailable");
        if (fallback) fallback.remove();
    });
    renderScoreTrendChart(stats.scoreTrend || []);
    renderActivityChart(stats.dailyActivity || []);
}

async function loadStats() {
    let stats;
    try {
        const res = await fetch("/api/stats");
        stats = await res.json();
    } catch (e) {
        statsTiles.innerHTML = `<p class="empty">統計の取得に失敗しました: ${friendlyErrorMessage(e)}</p>`;
        return;
    }
    renderStatsTiles(stats);
    renderChartsOrFallback(stats);
}

// 進捗タブは display:none の間はcanvasのサイズが取れずグラフが崩れるため、
// タブが実際に表示されてから(nav.jsのタブ切り替えの後)読み込む
document.querySelector('.tab-btn[data-tab="stats"]').addEventListener("click", loadStats);
