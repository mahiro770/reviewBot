const codeInput = document.getElementById("codeInput");
const reviewBtn = document.getElementById("reviewBtn");
const statusEl = document.getElementById("status");
const errorBox = document.getElementById("errorBox");
const resultBox = document.getElementById("resultBox");
const scoreBadge = document.getElementById("scoreBadge");
const reviewText = document.getElementById("reviewText");
const historyList = document.getElementById("historyList");
const historyLoadMoreBtn = document.getElementById("historyLoadMoreBtn");
const HISTORY_PAGE_SIZE = 20;
let historyOffset = 0;

function renderReviewResult(data, resultBoxEl, scoreBadgeEl, reviewTextEl) {
    resultBoxEl.hidden = false;
    const cls = scoreClass(data.score);
    scoreBadgeEl.className = "score-badge " + cls;
    scoreBadgeEl.textContent = (data.score !== null && data.score !== undefined && data.score !== "")
        ? `スコア ${data.score} / 100`
        : "スコア未取得";
    reviewTextEl.textContent = data.review;
}

/** 問題に紐づくレビューの正誤判定バッジを描画する(isCorrectがnullの場合は非表示のまま) */
function renderJudgementBadge(el, isCorrect) {
    if (isCorrect === null || isCorrect === undefined) {
        el.hidden = true;
        return;
    }
    el.hidden = false;
    el.className = "score-badge " + (isCorrect ? "good" : "bad");
    el.innerHTML = isCorrect
        ? `${iconSvg("check-circle", { className: "icon-sm" })} 正解`
        : `${iconSvg("x-circle", { className: "icon-sm" })} 不正解`;
}

/** コードをレビューAPIに送信する共通関数。問題集タブからも problemId 付きで呼ばれる */
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
    statusEl.textContent = "Geminiがレビュー中...";

    try {
        const data = await requestReview(code, null);
        renderResult(data);
        await loadHistory();
        refreshHeaderStats();
    } catch (e) {
        showError(errorBox, e.message || ("通信エラーが発生しました: " + e.message));
    } finally {
        reviewBtn.disabled = false;
        statusEl.textContent = "";
    }
}

function renderHistoryItem(item) {
    const div = document.createElement("div");
    div.className = "history-item";
    const judgementIcon = item.isCorrect === true ? iconSvg("check-circle", { className: "icon-sm status-good-icon" }) + " "
        : item.isCorrect === false ? iconSvg("x-circle", { className: "icon-sm status-bad-icon" }) + " " : "";
    div.innerHTML = `
        <div class="meta">
            <span>${judgementIcon}${item.score !== null && item.score !== undefined ? item.score + "点" : "-"}</span>
            <span>${formatDate(item.createdAt)}</span>
        </div>
        <div class="code-preview"></div>
    `;
    div.querySelector(".code-preview").textContent = item.codePreview;
    div.addEventListener("click", () => loadReviewDetail(item.id));
    historyList.appendChild(div);
}

/** 履歴一覧を最初から読み直す(新しいレビュー提出後などに呼ぶ) */
async function loadHistory() {
    historyOffset = 0;
    try {
        const res = await fetch(`/api/reviews?limit=${HISTORY_PAGE_SIZE}&offset=0`);
        const page = await res.json();

        if (!page.items.length) {
            historyList.innerHTML = '<p class="empty">まだレビュー履歴はありません</p>';
            historyLoadMoreBtn.hidden = true;
            return;
        }

        historyList.innerHTML = "";
        page.items.forEach(renderHistoryItem);
        historyOffset = page.items.length;
        historyLoadMoreBtn.hidden = !page.hasMore;
    } catch (e) {
        historyList.innerHTML = `<p class="empty">履歴の取得に失敗しました: ${e.message}</p>`;
        historyLoadMoreBtn.hidden = true;
    }
}

/** 「もっと見る」で続きを追加読み込みする */
async function loadMoreHistory() {
    historyLoadMoreBtn.disabled = true;
    try {
        const res = await fetch(`/api/reviews?limit=${HISTORY_PAGE_SIZE}&offset=${historyOffset}`);
        const page = await res.json();
        page.items.forEach(renderHistoryItem);
        historyOffset += page.items.length;
        historyLoadMoreBtn.hidden = !page.hasMore;
    } catch (e) {
        showError(errorBox, "履歴の追加読み込みに失敗しました: " + e.message);
    } finally {
        historyLoadMoreBtn.disabled = false;
    }
}

historyLoadMoreBtn.addEventListener("click", loadMoreHistory);

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
