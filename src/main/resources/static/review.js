const codeInput = document.getElementById("codeInput");
const reviewBtn = document.getElementById("reviewBtn");
const statusEl = document.getElementById("status");
const errorBox = document.getElementById("errorBox");
const resultBox = document.getElementById("resultBox");
const scoreBadge = document.getElementById("scoreBadge");
const reviewText = document.getElementById("reviewText");
const historyList = document.getElementById("historyList");

function renderReviewResult(data, resultBoxEl, scoreBadgeEl, reviewTextEl) {
    resultBoxEl.hidden = false;
    const cls = scoreClass(data.score);
    scoreBadgeEl.className = "score-badge " + cls;
    scoreBadgeEl.textContent = (data.score !== null && data.score !== undefined && data.score !== "")
        ? `スコア ${data.score} / 100`
        : "スコア未取得";
    reviewTextEl.textContent = data.review;
}

/** コードをレビューAPIに送信する共通関数。今日の問題タブからも problemId 付きで呼ばれる */
async function requestReview(code, problemId) {
    const res = await fetch("/api/reviews", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ code, problemId: problemId || null })
    });
    const data = await res.json();
    if (!res.ok) {
        throw new Error(data.error || "レビューに失敗しました。");
    }
    return data;
}

function renderResult(data) {
    renderReviewResult(data, resultBox, scoreBadge, reviewText);
}

async function submitReview() {
    const code = codeInput.value.trim();
    clearError(errorBox);
    resultBox.hidden = true;

    if (!code) {
        showError(errorBox, "コードを入力してください。");
        return;
    }

    reviewBtn.disabled = true;
    statusEl.textContent = "Claudeがレビュー中...";

    try {
        const data = await requestReview(code, null);
        renderResult(data);
        await loadHistory();
    } catch (e) {
        showError(errorBox, e.message || ("通信エラーが発生しました: " + e.message));
    } finally {
        reviewBtn.disabled = false;
        statusEl.textContent = "";
    }
}

async function loadHistory() {
    try {
        const res = await fetch("/api/reviews");
        const items = await res.json();

        if (!items.length) {
            historyList.innerHTML = '<p class="empty">まだレビュー履歴はありません</p>';
            return;
        }

        historyList.innerHTML = "";
        items.forEach(item => {
            const div = document.createElement("div");
            div.className = "history-item";
            div.innerHTML = `
                <div class="meta">
                    <span>${item.score !== null && item.score !== undefined ? item.score + "点" : "-"}</span>
                    <span>${formatDate(item.createdAt)}</span>
                </div>
                <div class="code-preview"></div>
            `;
            div.querySelector(".code-preview").textContent = item.codePreview;
            div.addEventListener("click", () => loadReviewDetail(item.id));
            historyList.appendChild(div);
        });
    } catch (e) {
        historyList.innerHTML = `<p class="empty">履歴の取得に失敗しました: ${e.message}</p>`;
    }
}

async function loadReviewDetail(id) {
    clearError(errorBox);
    try {
        const res = await fetch(`/api/reviews/${id}`);
        if (!res.ok) {
            showError(errorBox, "履歴の取得に失敗しました。");
            return;
        }
        const data = await res.json();
        codeInput.value = data.code;
        renderResult(data);
        activateTab("review");
        window.scrollTo({ top: 0, behavior: "smooth" });
    } catch (e) {
        showError(errorBox, "通信エラーが発生しました: " + e.message);
    }
}

reviewBtn.addEventListener("click", submitReview);

// Ctrl+Enter / Cmd+Enter で送信
codeInput.addEventListener("keydown", (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key === "Enter") {
        submitReview();
    }
});

loadHistory();
