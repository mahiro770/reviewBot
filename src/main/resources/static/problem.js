// ---- レベル一覧ビュー ----
const silverLevelGrid = document.getElementById("silverLevelGrid");
const goldLevelGrid = document.getElementById("goldLevelGrid");
const favoritesList = document.getElementById("favoritesList");
const favoritesLoadMoreBtn = document.getElementById("favoritesLoadMoreBtn");
const mistakesList = document.getElementById("mistakesList");
const mistakesLoadMoreBtn = document.getElementById("mistakesLoadMoreBtn");

// ---- 問題一覧ビュー ----
const backToLevelsBtn = document.getElementById("backToLevelsBtn");
const problemListTitle = document.getElementById("problemListTitle");
const generateProblemBtn = document.getElementById("generateProblemBtn");
const problemListStatus = document.getElementById("problemListStatus");
const problemListErrorBox = document.getElementById("problemListErrorBox");
const problemListCards = document.getElementById("problemListCards");
const problemListLoadMoreBtn = document.getElementById("problemListLoadMoreBtn");

// ---- 問題詳細/回答ビュー ----
const backToProblemListBtn = document.getElementById("backToProblemListBtn");
const problemTitle = document.getElementById("problemTitle");
const problemDifficulty = document.getElementById("problemDifficulty");
const problemFavoriteBtn = document.getElementById("problemFavoriteBtn");
const problemDescription = document.getElementById("problemDescription");
const problemCorrectBadge = document.getElementById("problemCorrectBadge");
const problemCodeInput = document.getElementById("problemCodeInput");
const problemSubmitBtn = document.getElementById("problemSubmitBtn");
const problemStatus = document.getElementById("problemStatus");
const problemErrorBox = document.getElementById("problemErrorBox");
const problemResultBox = document.getElementById("problemResultBox");
const problemJudgementBadge = document.getElementById("problemJudgementBadge");
const problemScoreBadge = document.getElementById("problemScoreBadge");
const problemReviewText = document.getElementById("problemReviewText");

const libViews = { levels: document.getElementById("libLevels"), favorites: document.getElementById("libFavorites"),
    mistakes: document.getElementById("libMistakes"), problemList: document.getElementById("libProblemList"),
    problemDetail: document.getElementById("libProblemDetail") };
const libTabs = document.querySelectorAll(".lib-tab");

const PAGE_SIZE = 20;

/** このタブが今どのレベル/問題を見ているか、各リストをどこまで読み込んだかをまとめて持つ */
const state = {
    currentLevelId: null,
    currentProblem: null,
    detailBackView: "levels",
    problemListOffset: 0,
    favoritesOffset: 0,
    mistakesOffset: 0,
};

function showLibView(name) {
    Object.values(libViews).forEach(v => { v.hidden = true; });
    libViews[name].hidden = false;
    libTabs.forEach(tab => tab.classList.toggle("active", tab.dataset.lib === name));
}

libTabs.forEach(tab => {
    tab.addEventListener("click", () => {
        showLibView(tab.dataset.lib);
        if (tab.dataset.lib === "favorites") loadFavorites();
        if (tab.dataset.lib === "mistakes") loadMistakes();
    });
});

function renderProblemCard(problem, container, showLevel, originView) {
    const card = document.createElement("div");
    card.className = "problem-card";
    let statusLabel = '<span class="status-label">未回答</span>';
    if (problem.attempted) {
        if (problem.correct === true) {
            statusLabel = `<span class="status-label status-good">${iconSvg("check-circle", { className: "icon-sm" })} 正解済み</span>`;
        } else if (problem.correct === false) {
            statusLabel = `<span class="status-label status-bad">${iconSvg("x-circle", { className: "icon-sm" })} 不正解</span>`;
        } else {
            statusLabel = '<span class="status-label">提出済み</span>';
        }
    }
    const favoriteMark = problem.favorite ? iconSvg("star", { filled: true, className: "icon-sm gold-icon" }) + " " : "";
    card.innerHTML = `
        <div class="problem-card-title">${favoriteMark}${problem.title}</div>
        <div class="problem-card-meta">
            ${showLevel ? `<span>${problem.levelTitle}</span>` : ""}
            <span>${problem.difficulty || ""}</span>
            ${statusLabel}
        </div>
    `;
    card.addEventListener("click", () => {
        state.detailBackView = originView;
        openProblemDetail(problem);
    });
    container.appendChild(card);
}

async function loadLevels() {
    try {
        const res = await fetch("/api/levels");
        const levels = await res.json();

        silverLevelGrid.innerHTML = "";
        goldLevelGrid.innerHTML = "";
        levels.forEach(level => {
            const card = document.createElement("div");
            card.className = "level-card" + (level.cleared ? " level-cleared" : "");
            const clearedMark = level.cleared ? iconSvg("check-circle", { className: "icon-sm status-good-icon" }) + " " : "";
            card.innerHTML = `
                <div class="level-card-title">${clearedMark}Lv.${level.id} ${level.title}</div>
                <div class="problem-card-meta"><span>正解 ${level.correctCount}/${level.requiredCorrectCount}</span></div>
            `;
            card.addEventListener("click", () => openLevel(level.id, level.title));
            (level.certification === "SILVER" ? silverLevelGrid : goldLevelGrid).appendChild(card);
        });
    } catch (e) {
        silverLevelGrid.innerHTML = `<p class="empty">レベル一覧の取得に失敗しました: ${e.message}</p>`;
    }
}

async function openLevel(levelId, levelTitle) {
    state.currentLevelId = levelId;
    state.detailBackView = "problemList";
    state.problemListOffset = 0;
    problemListTitle.textContent = `Lv.${levelId} ${levelTitle}`;
    showLibView("problemList");
    await loadProblemsForLevel();
}

async function loadProblemsForLevel() {
    clearError(problemListErrorBox);
    problemListCards.innerHTML = "";
    state.problemListOffset = 0;
    try {
        const res = await fetch(`/api/problems?levelId=${state.currentLevelId}&limit=${PAGE_SIZE}&offset=0`);
        const page = await res.json();
        if (!page.items.length) {
            problemListCards.innerHTML = '<p class="empty">まだ問題がありません。「新しい問題を生成する」を押してください</p>';
            problemListLoadMoreBtn.hidden = true;
            return;
        }
        page.items.forEach(p => renderProblemCard(p, problemListCards, false, "problemList"));
        state.problemListOffset = page.items.length;
        problemListLoadMoreBtn.hidden = !page.hasMore;
    } catch (e) {
        showError(problemListErrorBox, "問題一覧の取得に失敗しました: " + e.message);
    }
}

async function loadMoreProblemsForLevel() {
    problemListLoadMoreBtn.disabled = true;
    try {
        const res = await fetch(`/api/problems?levelId=${state.currentLevelId}&limit=${PAGE_SIZE}&offset=${state.problemListOffset}`);
        const page = await res.json();
        page.items.forEach(p => renderProblemCard(p, problemListCards, false, "problemList"));
        state.problemListOffset += page.items.length;
        problemListLoadMoreBtn.hidden = !page.hasMore;
    } catch (e) {
        showError(problemListErrorBox, "追加読み込みに失敗しました: " + e.message);
    } finally {
        problemListLoadMoreBtn.disabled = false;
    }
}

async function generateProblem() {
    clearError(problemListErrorBox);
    generateProblemBtn.disabled = true;
    problemListStatus.textContent = "Geminiが問題を生成中...";

    try {
        const res = await fetch("/api/problems/generate", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ levelId: state.currentLevelId })
        });
        const data = await res.json();
        if (!res.ok) {
            showError(problemListErrorBox, data.error || "問題の生成に失敗しました。");
            return;
        }
        state.detailBackView = "problemList";
        openProblemDetail(data);
        await loadProblemsForLevel();
    } catch (e) {
        showError(problemListErrorBox, "通信エラーが発生しました: " + e.message);
    } finally {
        generateProblemBtn.disabled = false;
        problemListStatus.textContent = "";
    }
}

async function loadFavorites() {
    favoritesList.innerHTML = "";
    state.favoritesOffset = 0;
    try {
        const res = await fetch(`/api/problems/favorites?limit=${PAGE_SIZE}&offset=0`);
        const page = await res.json();
        if (!page.items.length) {
            favoritesList.innerHTML = '<p class="empty">お気に入り登録した問題はまだありません</p>';
            favoritesLoadMoreBtn.hidden = true;
            return;
        }
        page.items.forEach(p => renderProblemCard(p, favoritesList, true, "favorites"));
        state.favoritesOffset = page.items.length;
        favoritesLoadMoreBtn.hidden = !page.hasMore;
    } catch (e) {
        favoritesList.innerHTML = `<p class="empty">取得に失敗しました: ${e.message}</p>`;
    }
}

async function loadMoreFavorites() {
    favoritesLoadMoreBtn.disabled = true;
    try {
        const res = await fetch(`/api/problems/favorites?limit=${PAGE_SIZE}&offset=${state.favoritesOffset}`);
        const page = await res.json();
        page.items.forEach(p => renderProblemCard(p, favoritesList, true, "favorites"));
        state.favoritesOffset += page.items.length;
        favoritesLoadMoreBtn.hidden = !page.hasMore;
    } finally {
        favoritesLoadMoreBtn.disabled = false;
    }
}

async function loadMistakes() {
    mistakesList.innerHTML = "";
    state.mistakesOffset = 0;
    try {
        const res = await fetch(`/api/problems/mistakes?limit=${PAGE_SIZE}&offset=0`);
        const page = await res.json();
        if (!page.items.length) {
            mistakesList.innerHTML = '<p class="empty">間違えた問題はまだありません</p>';
            mistakesLoadMoreBtn.hidden = true;
            return;
        }
        page.items.forEach(p => renderProblemCard(p, mistakesList, true, "mistakes"));
        state.mistakesOffset = page.items.length;
        mistakesLoadMoreBtn.hidden = !page.hasMore;
    } catch (e) {
        mistakesList.innerHTML = `<p class="empty">取得に失敗しました: ${e.message}</p>`;
    }
}

async function loadMoreMistakes() {
    mistakesLoadMoreBtn.disabled = true;
    try {
        const res = await fetch(`/api/problems/mistakes?limit=${PAGE_SIZE}&offset=${state.mistakesOffset}`);
        const page = await res.json();
        page.items.forEach(p => renderProblemCard(p, mistakesList, true, "mistakes"));
        state.mistakesOffset += page.items.length;
        mistakesLoadMoreBtn.hidden = !page.hasMore;
    } finally {
        mistakesLoadMoreBtn.disabled = false;
    }
}

/** お気に入りボタンの見た目(星の塗りつぶし)を更新する */
function setFavoriteButtonState(favorite) {
    problemFavoriteBtn.innerHTML = iconSvg("star", { filled: favorite, className: "icon-lg" });
    problemFavoriteBtn.classList.toggle("favorited", favorite);
}

function openProblemDetail(problem) {
    state.currentProblem = problem;
    if (problem.levelId) state.currentLevelId = problem.levelId;

    clearError(problemErrorBox);
    problemResultBox.hidden = true;
    problemCodeInput.value = "";

    problemTitle.textContent = problem.title;
    problemDifficulty.textContent = problem.difficulty || "";
    problemDescription.textContent = problem.description;
    setFavoriteButtonState(problem.favorite);

    if (problem.attempted && problem.correct !== null && problem.correct !== undefined) {
        renderJudgementBadge(problemCorrectBadge, problem.correct);
    } else {
        problemCorrectBadge.hidden = true;
    }

    showLibView("problemDetail");
}

async function toggleFavorite() {
    if (!state.currentProblem) return;
    const newValue = !state.currentProblem.favorite;
    try {
        const res = await fetch(`/api/problems/${state.currentProblem.id}/favorite`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ favorite: newValue })
        });
        const data = await res.json();
        if (res.ok) {
            state.currentProblem = data;
            setFavoriteButtonState(data.favorite);
        }
    } catch (e) {
        // お気に入り切替の失敗は致命的ではないため、静かに無視する
    }
}

async function submitProblemAnswer() {
    const code = problemCodeInput.value.trim();
    clearError(problemErrorBox);
    problemResultBox.hidden = true;

    if (!code) {
        showError(problemErrorBox, "コードを入力してください。");
        return;
    }
    if (!state.currentProblem) {
        showError(problemErrorBox, "問題が選択されていません。");
        return;
    }

    problemSubmitBtn.disabled = true;
    problemStatus.textContent = "Geminiがレビュー中...";

    try {
        const data = await requestReview(code, state.currentProblem.id);
        renderReviewResult(data, problemResultBox, problemScoreBadge, problemReviewText);
        renderJudgementBadge(problemJudgementBadge, data.isCorrect);
        renderJudgementBadge(problemCorrectBadge, data.isCorrect);
        state.currentProblem.attempted = true;
        state.currentProblem.correct = data.isCorrect;
        refreshHeaderStats();
    } catch (e) {
        showError(problemErrorBox, e.message);
    } finally {
        problemSubmitBtn.disabled = false;
        problemStatus.textContent = "";
    }
}

backToLevelsBtn.addEventListener("click", () => showLibView("levels"));
backToProblemListBtn.addEventListener("click", () => showLibView(state.detailBackView));
generateProblemBtn.addEventListener("click", generateProblem);
problemFavoriteBtn.addEventListener("click", toggleFavorite);
problemSubmitBtn.addEventListener("click", submitProblemAnswer);
problemListLoadMoreBtn.addEventListener("click", loadMoreProblemsForLevel);
favoritesLoadMoreBtn.addEventListener("click", loadMoreFavorites);
mistakesLoadMoreBtn.addEventListener("click", loadMoreMistakes);

problemCodeInput.addEventListener("keydown", (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key === "Enter") {
        submitProblemAnswer();
    }
});

loadLevels();
