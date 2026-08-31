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

/** エラー文言をアプリ全体で揃えるための共通ヘルパー。
    サーバーが返した(すでに日本語の)エラーメッセージはそのまま使い、
    fetch自体やJSON解析が失敗した場合(オフライン等)は "Failed to fetch" のような
    ブラウザの生の技術的な文言を出さず、常に同じ日本語の案内にする */
function friendlyErrorMessage(e) {
    if (e instanceof TypeError || e instanceof SyntaxError) {
        return "通信エラーが発生しました。ネットワーク接続を確認してください。";
    }
    return e && e.message ? e.message : "予期しないエラーが発生しました。";
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

/** クリックだけで作られがちなカード要素(div)をキーボードでも操作できるようにする。
    tabindex/role/Enter・Space操作を付与し、通常のclickハンドラも登録する */
function makeClickable(el, handler) {
    el.tabIndex = 0;
    el.setAttribute("role", "button");
    el.addEventListener("click", handler);
    el.addEventListener("keydown", (e) => {
        if (e.key === "Enter" || e.key === " ") {
            e.preventDefault();
            handler();
        }
    });
}

/** レベルクリア・資格達成など、数か月続く学習の節目だけに使う控えめな通知。
    多用すると煩わしくなるので、本当の達成の瞬間だけで呼ぶこと */
function showMilestoneToast(message) {
    const container = document.getElementById("toastContainer");
    if (!container) return;

    const toast = document.createElement("div");
    toast.className = "toast";
    toast.innerHTML = `${iconSvg("check-circle", { className: "icon-lg" })} <span></span>`;
    toast.querySelector("span").textContent = message;
    container.appendChild(toast);

    requestAnimationFrame(() => toast.classList.add("show"));
    setTimeout(() => {
        toast.classList.remove("show");
        setTimeout(() => toast.remove(), 250);
    }, 4000);
}
