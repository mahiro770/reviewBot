const problemTitle = document.getElementById("problemTitle");
const problemDifficulty = document.getElementById("problemDifficulty");
const problemLoading = document.getElementById("problemLoading");
const problemDescription = document.getElementById("problemDescription");
const problemSolvedBadge = document.getElementById("problemSolvedBadge");
const problemCodeInput = document.getElementById("problemCodeInput");
const problemSubmitBtn = document.getElementById("problemSubmitBtn");
const problemStatus = document.getElementById("problemStatus");
const problemErrorBox = document.getElementById("problemErrorBox");
const problemResultBox = document.getElementById("problemResultBox");
const problemScoreBadge = document.getElementById("problemScoreBadge");
const problemReviewText = document.getElementById("problemReviewText");

let currentProblemId = null;

async function loadTodayProblem() {
    clearError(problemErrorBox);
    problemLoading.hidden = false;
    problemLoading.textContent = "今日の問題を用意しています(初回はGeminiが生成するので少し時間がかかります)...";
    problemDescription.hidden = true;

    try {
        const res = await fetch("/api/problems/today");
        const data = await res.json();

        if (!res.ok) {
            showError(problemErrorBox, data.error || "問題の取得に失敗しました。");
            problemLoading.hidden = true;
            return;
        }

        currentProblemId = data.id;
        problemTitle.textContent = `📅 ${data.title}`;
        problemDifficulty.textContent = data.difficulty || "";
        problemDescription.textContent = data.description;
        problemDescription.hidden = false;
        problemSolvedBadge.hidden = !data.solved;
        problemLoading.hidden = true;
    } catch (e) {
        showError(problemErrorBox, "通信エラーが発生しました: " + e.message);
        problemLoading.hidden = true;
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
    if (!currentProblemId) {
        showError(problemErrorBox, "問題がまだ読み込まれていません。");
        return;
    }

    problemSubmitBtn.disabled = true;
    problemStatus.textContent = "Geminiがレビュー中...";

    try {
        const data = await requestReview(code, currentProblemId);
        renderReviewResult(data, problemResultBox, problemScoreBadge, problemReviewText);
        problemSolvedBadge.hidden = false;
    } catch (e) {
        showError(problemErrorBox, e.message);
    } finally {
        problemSubmitBtn.disabled = false;
        problemStatus.textContent = "";
    }
}

problemSubmitBtn.addEventListener("click", submitProblemAnswer);

problemCodeInput.addEventListener("keydown", (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key === "Enter") {
        submitProblemAnswer();
    }
});

loadTodayProblem();
