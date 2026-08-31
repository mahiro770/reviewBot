---
target: src/main/resources/static/index.html (whole app)
total_score: 25
max_score: 40
na_heuristics: 
p0_count: 0
p1_count: 3
timestamp: 2026-08-30T13-41-19Z
slug: src-main-resources-static-index-html
---
Method: dual-agent (A: design-review sub-agent · B: detector/browser-evidence sub-agent)

## Design Health Score

| # | Heuristic | Score | Key Issue |
|---|-----------|-------|-----------|
| 1 | Visibility of System Status | 3 | Clear status text during Gemini calls, but no `aria-live` anywhere and no visual loading cue beyond text swap |
| 2 | Match System / Real World | 2 | Cert terminology is accurate, but Gemini output renders raw Markdown (`##`, `**`, `` ``` ``) as literal text |
| 3 | User Control and Freedom | 3 | Back nav and free tab switching everywhere; no way to cancel an in-flight Gemini call |
| 4 | Consistency and Standards | 2 | Score-trend chart line is always blue regardless of performance (breaks the app's own blue=interactive rule); detector independently confirmed all `<button>` labels render in the browser's fallback UA font, not the documented Segoe UI stack |
| 5 | Error Prevention | 3 | Required-field checks, disabled buttons mid-request, server-side retry on 429/5xx |
| 6 | Recognition Rather Than Recall | 3 | Good status labels on cards; review-history entries show no problem title, only score+timestamp+truncated code |
| 7 | Flexibility and Efficiency | 2 | Ctrl+Enter submit is the only accelerator; no shortcuts, no "today's problem" jump, no batch actions |
| 8 | Aesthetic and Minimalist Design | 3 | Genuinely restrained when working as designed; undercut by raw-Markdown noise in exactly the content areas that should read cleanest |
| 9 | Error Recovery | 3 | Specific Japanese error copy; network errors surface directly (acceptable, sole user is the developer) |
| 10 | Help and Documentation | 1 | The "2+ correct answers to clear a level" rule is never explained anywhere in the UI |
| **Total** | | **25/40** | **Acceptable — solid foundation, several concrete gaps** |

## Design Specificity Verdict

**LLM assessment**: This is authored, not generic. The curriculum is baked into real data and copy (12 levels titled by actual Silver/Gold exam topics), header widgets are domain-specific (days-to-goal, streak, cert badge counters), status copy names the model doing the work, and the icon/color/radius system is custom and applied with real discipline — confirmed live across 16 screenshots spanning both viewports. A generic dark-CRUD template would not produce a "2-correct-answers-to-clear" counter or this exact curriculum. This is the strongest part of the whole submission.

**Deterministic scan**: `detect.mjs` ran in a **degraded regex-only mode** (its own HTML-parser dependencies — htmlparser2, css-select, css-tree, domutils — aren't installed on this machine), so its `[]` result is an explicit undercount, not a clean bill of health, per the tool's own disclaimer. The browser-injected detector (network-served `/detect.js`, run against 3 live views) found the same two rule hits on every view: **`overused-font`** (Arial at ~30% of text) and **`flat-type-hierarchy`** (sizes 12/13/14/16/22px, plus an unexplained 18px on the 問題集/進捗 tabs, ratio 1.8:1).

The `overused-font` finding is a **verified real defect**, traced to source: `style.css` never resets `font-family` on `button` elements (only `input[type=...]` and `select` get `font-family: inherit`), so every button label — 目標を保存する, レビューを依頼する, 新しい問題を生成する, both tab rows, the favorite button — silently renders in the browser's default UI-control font instead of the documented Segoe UI stack, even though the rest of the app matches DESIGN.md's typography exactly. Neither assessment caught this independently of the other: the LLM review never opened dev tools on button text; the detector can't judge "is this on-brand," only "is one font dominating." Together they land on one real, fixable bug.

The `flat-type-hierarchy` finding is **not** treated as a defect here — a compact 1.8:1 ratio across a small utilitarian console UI is consistent with the deliberately restrained type system DESIGN.md documents, not a flaw. The extra 18px value on two tabs could not be traced to a literal CSS declaration (likely a Chart.js canvas-rendered legend/tick label or a browser default); flagged as an open, low-priority question rather than a confirmed issue.

## Overall Impression

The visual system is unusually disciplined for a solo project — the two-weight blue rule, the icon set, the flat depth model all survive contact with a real running instance with real accumulated data. What drags the score down isn't taste, it's follow-through: the contrast fix already applied to error-box text was never extended to the "不正解" badges that need it most, the font system has one un-reset element, and the app's single biggest motivational moment (clearing a level) currently happens in total silence. These are all small, concrete, and fixable — not a redesign.

## What's Working

1. **`makeClickable` keyboard-accessibility utility** — retrofits tabindex/role/Enter-Space handling onto every clickable card div. A genuinely above-average default most solo projects skip.
2. **The Two-Weight Blue Rule, verified end-to-end** — Console Blue for text/borders, the deeper Fill shade for button backgrounds, confirmed pixel-for-pixel live. Rare for a documented contrast rationale to survive intact into shipped CSS.
3. **Contextual empty states** — "まだ問題がありません。「新しい問題を生成する」を押してください" tells the user exactly what to do next rather than a bare "no data."

## Priority Issues

**[P1] "不正解" badge text fails its own contrast standard.** `.score-badge.bad` and the judgement badges render `#ef5b5b` text directly on its own 12%-tint background — measured ≈4.38:1, under the 4.5:1 floor DESIGN.md invokes as the exact reason `alert-red-text` (#ff9b9b) exists for `.error-box`. The fix applied to error-box copy never propagated to the badges shown at the one moment (wrong-answer feedback) where legibility matters most.
- **Why it matters**: This is a real, measurable accessibility regression, and it's inconsistent with a rule the project itself already wrote down and applied elsewhere.
- **Fix**: Swap `.score-badge.bad` / judgement-badge text color to `var(--bad)` → the existing `alert-red-text` value, same precedent as `.error-box`.
- **Suggested command**: `/impeccable polish`

**[P1] Gemini output renders raw Markdown as literal text.** Problem descriptions and review results display `## 要件`, `**0円**`, `` ``` `` fences verbatim inside `<pre>` blocks — confirmed live on both viewports.
- **Why it matters**: This is the single biggest gap between the app's polished visual system and what a real study session looks like; every problem and every review is cluttered with syntax noise the user has to mentally filter out.
- **Fix**: Run description/review text through a minimal Markdown-to-HTML pass before injecting into `#problemDescription`/`.review-text`, keeping the Monospace Boundary Rule intact for genuine code spans.
- **Suggested command**: `/impeccable harden`

**[P1] No milestone feedback when a level clears or a certification completes.** Badges and counters just recolor on next data load; no toast, banner, or transition marks the moment `correctCount` reaches `requiredCorrectCount`.
- **Why it matters**: This is the core motivational payoff of the entire goal → problem → review → progress loop, per PRODUCT.md — an unacknowledged milestone is a real retention risk over a multi-month cert grind.
- **Fix**: A small, on-brand (no confetti/emoji) inline banner or toast the instant a level/cert completes, using the existing Signal Green pill language.
- **Suggested command**: `/impeccable delight`

**[P2] Review-history entries are visually indistinguishable.** Sidebar entries show only score + timestamp + an ellipsis-truncated code preview; with similar test snippets (as in the live data), 3 of 4 entries render as visually identical text, and none show which problem (if any) they're linked to.
- **Why it matters**: The history sidebar's entire purpose is letting the user tell past attempts apart; right now it can't.
- **Fix**: Show the linked problem's title (or "自由投稿" when none) instead of/alongside the code preview.
- **Suggested command**: `/impeccable clarify`

**[P2] Buttons silently break the documented font system.** `style.css` never resets `font-family` on `<button>`, so every button label renders in the browser's fallback UA font instead of the Segoe UI stack every other element uses — independently confirmed by the browser detector and traced to the missing CSS rule.
- **Why it matters**: This is a real, invisible-until-you-look drift from a design system the project just finished documenting and applying everywhere else.
- **Fix**: Add `font-family: inherit;` to the base `button` rule (matching the existing pattern already used for `input`/`select`).
- **Suggested command**: `/impeccable typeset`

**[P2] Mobile problem-list header has no wrap strategy.** At 390px, a 2-line level title crams against a 3-line-wrapped generate button in `.result-header` — the one clearly cramped layout across the entire mobile pass.
- **Why it matters**: It's the single mobile regression in an otherwise clean responsive pass (header widgets, goal form, tab-nav all held up).
- **Fix**: Add a stacking/wrap rule for `.result-header` under the existing ≤480px breakpoint.
- **Suggested command**: `/impeccable adapt`

**[P3] Overdue goal and streak states use no distinct color.** "目標日を超過" (deadline passed) renders in the same blue pill as healthy progress; the streak widget is always amber regardless of value, including zero.
- **Why it matters**: Minor given the tool's calm tone, but it's a small gap in the Signal-Only Rule this system otherwise follows carefully.
- **Fix**: Toggle to an amber/red variant on overdue/zero-streak, reusing existing tokens.
- **Suggested command**: `/impeccable polish`

## Persona Red Flags

**Sam (accessibility-dependent, screen reader/keyboard-only)**: No `aria-live` region anywhere, so "Geminiがレビュー中...", "保存しました", "5問生成しました" are invisible to screen readers at exactly the moments confirmation matters most. The favorite button toggles a CSS class with no `aria-pressed`, so its state is unannounced. The tab rows are plain buttons with an `.active` class for *visual* state only — no `role="tablist"`/`aria-selected` — so real tab-panel semantics are invisible to assistive tech, despite the app's genuinely good keyboard-focus work elsewhere (`makeClickable`) not extending to this layer.

**Riley (stress-tester)**: The raw-Markdown leak and the indistinguishable-history-entries issue both got worse, not better, under real (non-seeded) use — both were already visible after just 4 review submissions and a handful of generated problems, meaning they compound with continued daily use rather than being edge cases.

**Casey (distracted mobile user)**: The `.result-header` wrap issue is the one place mobile breaks down; header-widget wrapping, the goal form's column collapse, and the tab-nav horizontal scroll all held up cleanly across both viewports.

## Minor Observations

- `.difficulty-badge` and neutral score badges are intentionally left uncolored per DESIGN.md's "undetermined state" rule — correct restraint, not a bug.
- Double-click-to-open-calendar on date inputs is a small, real usability nicety many forms skip.
- `formatDate` consistently uses `ja-JP` locale formatting — good discipline matching the Japanese-throughout brand commitment.
- The detector's `flat-type-hierarchy` finding (1.8:1 size ratio) reads as intentional restraint for this UI's scale, not a defect; the one untraced 18px value is a low-priority curiosity, not a confirmed issue.

## Questions to Consider

1. What if the Gemini review/problem text were run through even a minimal Markdown-to-HTML pass — would that alone lift this app's perceived polish more than any other single change here?
2. What if level-clear and cert-completion were the *only* two moments in the entire app allowed a small break from total visual flatness — would that change whether this survives as a daily habit past month three of a certification grind?
3. What if the review-history preview keyed off the diff from the previous submission to the same problem, instead of the first N characters of code — would that turn a currently-useless list into an actual "here's what changed" study aid?
