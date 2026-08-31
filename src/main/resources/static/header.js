const streakWidget = document.getElementById("streakWidget");
const silverBadge = document.getElementById("silverBadge");
const goldBadge = document.getElementById("goldBadge");

/** ページ読み込み直後の初回呼び出しでは、既に達成済みでも通知を出さない
    (「今まさに達成した」瞬間だけに絞るため) */
let headerStatsInitialized = false;
let wasSilverComplete = false;
let wasGoldComplete = false;

/** ヘッダーの連続学習日数・Silver/Goldバッジを更新する。ページ読み込み時と問題提出成功後に呼ばれる */
async function refreshHeaderStats() {
    try {
        const res = await fetch("/api/stats");
        const stats = await res.json();

        streakWidget.hidden = false;
        streakWidget.innerHTML = `${iconSvg("trending-up")} 連続学習 ${stats.streakDays} 日`;
        streakWidget.classList.toggle("streak-zero", stats.streakDays === 0);

        const silverComplete = stats.silverCleared >= stats.silverTotal && stats.silverTotal > 0;
        silverBadge.hidden = false;
        silverBadge.innerHTML = `${iconSvg("award", { className: "silver-icon" })} Silver ${stats.silverCleared}/${stats.silverTotal}`;
        silverBadge.classList.toggle("cert-complete", silverComplete);

        const goldComplete = stats.goldCleared >= stats.goldTotal && stats.goldTotal > 0;
        goldBadge.hidden = false;
        goldBadge.innerHTML = `${iconSvg("award", { className: "gold-icon" })} Gold ${stats.goldCleared}/${stats.goldTotal}`;
        goldBadge.classList.toggle("cert-complete", goldComplete);

        if (headerStatsInitialized) {
            if (silverComplete && !wasSilverComplete) showMilestoneToast("Java Silver 相当の範囲をすべてクリアしました");
            if (goldComplete && !wasGoldComplete) showMilestoneToast("Java Gold 相当の範囲をすべてクリアしました");
        }
        headerStatsInitialized = true;
        wasSilverComplete = silverComplete;
        wasGoldComplete = goldComplete;
    } catch (e) {
        // ヘッダーの補助表示なので、失敗しても他の機能は継続させる
    }
}

refreshHeaderStats();
