const streakWidget = document.getElementById("streakWidget");
const silverBadge = document.getElementById("silverBadge");
const goldBadge = document.getElementById("goldBadge");

/** ヘッダーの連続学習日数・Silver/Goldバッジを更新する。ページ読み込み時と問題提出成功後に呼ばれる */
async function refreshHeaderStats() {
    try {
        const res = await fetch("/api/stats");
        const stats = await res.json();

        streakWidget.hidden = false;
        streakWidget.textContent = `🔥 連続学習 ${stats.streakDays} 日`;

        silverBadge.hidden = false;
        silverBadge.textContent = `🥈 Silver ${stats.silverCleared}/${stats.silverTotal}`;
        silverBadge.classList.toggle("cert-complete", stats.silverCleared >= stats.silverTotal && stats.silverTotal > 0);

        goldBadge.hidden = false;
        goldBadge.textContent = `🥇 Gold ${stats.goldCleared}/${stats.goldTotal}`;
        goldBadge.classList.toggle("cert-complete", stats.goldCleared >= stats.goldTotal && stats.goldTotal > 0);
    } catch (e) {
        // ヘッダーの補助表示なので、失敗しても他の機能は継続させる
    }
}

refreshHeaderStats();
