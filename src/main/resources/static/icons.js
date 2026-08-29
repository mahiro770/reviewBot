/**
 * アプリ全体で使う最小限のラインアイコンセット(絵文字の代わり)。
 * 24x24のviewBoxに統一し、strokeはcurrentColorでCSS側の文字色を継承する。
 * 動的にDOMへ挿入する箇所(problem.js/review.js/header.js)から iconSvg() で呼び出す。
 */
const ICON_PATHS = {
    target: '<circle cx="12" cy="12" r="8"/><circle cx="12" cy="12" r="4.5"/><circle cx="12" cy="12" r="1" fill="currentColor" stroke="none"/>',
    book: '<rect x="4" y="4" width="16" height="16" rx="1.5"/><line x1="12" y1="4" x2="12" y2="20"/>',
    search: '<circle cx="10" cy="10" r="6"/><line x1="14.5" y1="14.5" x2="20" y2="20"/>',
    "bar-chart": '<line x1="5" y1="20" x2="5" y2="12"/><line x1="12" y1="20" x2="12" y2="6"/><line x1="19" y1="20" x2="19" y2="15"/>',
    "trending-up": '<polyline points="4 16 10 10 14 14 20 6"/><polyline points="14 6 20 6 20 12"/>',
    award: '<circle cx="12" cy="8" r="5"/><polyline points="8.5 12.5 7 21 12 18 17 21 15.5 12.5"/>',
    star: '<polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>',
    "check-circle": '<circle cx="12" cy="12" r="9"/><polyline points="8 12.5 11 15.5 16 9"/>',
    "x-circle": '<circle cx="12" cy="12" r="9"/><line x1="9" y1="9" x2="15" y2="15"/><line x1="15" y1="9" x2="9" y2="15"/>',
    refresh: '<path d="M4 12a8 8 0 0 1 14-5.3M20 12a8 8 0 0 1-14 5.3"/><polyline points="18 3 18 7 14 7"/><polyline points="6 21 6 17 10 17"/>',
    circle: '<circle cx="12" cy="12" r="9"/>',
};

/** name: ICON_PATHSのキー。filled: true でstar/awardなどを塗りつぶす。className: 追加のCSSクラス */
function iconSvg(name, { filled = false, className = "" } = {}) {
    const inner = ICON_PATHS[name] || "";
    const fill = filled ? "currentColor" : "none";
    return `<svg class="icon ${className}" viewBox="0 0 24 24" fill="${fill}" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${inner}</svg>`;
}
