const goalVision = document.getElementById("goalVision");
const goalBuildTarget = document.getElementById("goalBuildTarget");
const goalDailyMinutes = document.getElementById("goalDailyMinutes");
const goalStartDate = document.getElementById("goalStartDate");
const goalTargetDate = document.getElementById("goalTargetDate");
const saveGoalBtn = document.getElementById("saveGoalBtn");
const goalStatus = document.getElementById("goalStatus");
const goalErrorBox = document.getElementById("goalErrorBox");
const goalWidget = document.getElementById("goalWidget");

function todayIso() {
    return new Date().toISOString().slice(0, 10);
}

function renderGoalWidget(goal) {
    if (!goal) {
        goalWidget.hidden = true;
        return;
    }
    goalWidget.hidden = false;
    const parts = [];
    if (goal.daysRemaining !== null && goal.daysRemaining !== undefined) {
        parts.push(goal.daysRemaining >= 0 ? `目標まであと ${goal.daysRemaining} 日` : `目標日を ${-goal.daysRemaining} 日超過`);
    }
    if (goal.progressPercent !== null && goal.progressPercent !== undefined) {
        parts.push(`進捗 ${goal.progressPercent}%`);
    }
    goalWidget.textContent = parts.length ? `🎯 ${parts.join(" / ")}` : "";
    goalWidget.hidden = parts.length === 0;
}

function fillGoalForm(goal) {
    goalVision.value = goal.targetVision || "";
    goalBuildTarget.value = goal.buildTarget || "";
    goalDailyMinutes.value = goal.dailyMinutes || "";
    goalStartDate.value = goal.startDate || todayIso();
    goalTargetDate.value = goal.targetDate || "";
}

async function loadGoal() {
    try {
        const res = await fetch("/api/goal");
        if (res.status === 204) {
            goalStartDate.value = todayIso();
            renderGoalWidget(null);
            return;
        }
        const data = await res.json();
        fillGoalForm(data);
        renderGoalWidget(data);
    } catch (e) {
        showError(goalErrorBox, "目標の取得に失敗しました: " + e.message);
    }
}

async function saveGoal() {
    clearError(goalErrorBox);

    if (!goalVision.value.trim()) {
        showError(goalErrorBox, "目指す姿を入力してください。");
        return;
    }

    saveGoalBtn.disabled = true;
    goalStatus.textContent = "保存中...";

    try {
        const res = await fetch("/api/goal", {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                targetVision: goalVision.value.trim(),
                buildTarget: goalBuildTarget.value.trim(),
                dailyMinutes: goalDailyMinutes.value ? Number(goalDailyMinutes.value) : null,
                startDate: goalStartDate.value || todayIso(),
                targetDate: goalTargetDate.value || null
            })
        });
        const data = await res.json();
        if (!res.ok) {
            showError(goalErrorBox, data.error || "目標の保存に失敗しました。");
            return;
        }
        fillGoalForm(data);
        renderGoalWidget(data);
        goalStatus.textContent = "保存しました";
        setTimeout(() => { goalStatus.textContent = ""; }, 2000);
    } catch (e) {
        showError(goalErrorBox, "通信エラーが発生しました: " + e.message);
    } finally {
        saveGoalBtn.disabled = false;
    }
}

saveGoalBtn.addEventListener("click", saveGoal);

// 日付入力欄はダブルクリックでカレンダーを開けるようにする(アイコン部分以外をクリックしても開くように)
[goalStartDate, goalTargetDate].forEach(input => {
    input.addEventListener("dblclick", () => {
        if (typeof input.showPicker === "function") {
            input.showPicker();
        }
    });
});

loadGoal();
