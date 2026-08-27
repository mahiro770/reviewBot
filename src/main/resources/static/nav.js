const tabButtons = document.querySelectorAll(".tab-btn");
const tabPanels = document.querySelectorAll(".tab-panel");

function activateTab(tabName) {
    tabButtons.forEach(btn => btn.classList.toggle("active", btn.dataset.tab === tabName));
    tabPanels.forEach(panel => panel.classList.toggle("active", panel.id === `tab-${tabName}`));
}

tabButtons.forEach(btn => {
    btn.addEventListener("click", () => activateTab(btn.dataset.tab));
});

/** 各タブのJSから共通で使うユーティリティ */
function showError(box, message) {
    box.textContent = message;
    box.hidden = false;
}

function clearError(box) {
    box.hidden = true;
    box.textContent = "";
}

function scoreClass(score) {
    if (score === null || score === undefined || score === "") return "";
    if (score >= 80) return "good";
    if (score >= 50) return "mid";
    return "bad";
}

function formatDate(iso) {
    if (!iso) return "";
    const d = new Date(iso);
    if (isNaN(d.getTime())) return iso;
    return d.toLocaleString("ja-JP", { hour12: false });
}
