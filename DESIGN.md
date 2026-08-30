---
name: Java学習システム
description: A calm, dark developer-console UI for a personal Java Silver/Gold study loop — goal, problems, review, progress.
colors:
  console-blue: "#5b8def"
  console-blue-fill: "#2f6fe0"
  console-blue-fill-hover: "#2159c4"
  deep-navy: "#0f1420"
  slate-panel: "#171e2e"
  panel-border: "#2a3348"
  ink-well: "#0b0f18"
  text-primary: "#e6e9f0"
  text-dim: "#9aa4bb"
  signal-green: "#3ecf8e"
  warning-amber: "#e9b949"
  alert-red: "#ef5b5b"
  alert-red-text: "#ff9b9b"
  cert-silver: "#b9c2d0"
typography:
  display:
    fontFamily: '"Segoe UI", "Hiragino Sans", "Noto Sans JP", sans-serif'
    fontSize: "22px"
    fontWeight: 700
    lineHeight: "normal"
  headline:
    fontFamily: '"Segoe UI", "Hiragino Sans", "Noto Sans JP", sans-serif'
    fontSize: "16px"
    fontWeight: 700
  title:
    fontFamily: '"Segoe UI", "Hiragino Sans", "Noto Sans JP", sans-serif'
    fontSize: "14px"
    fontWeight: 700
  body:
    fontFamily: '"Segoe UI", "Hiragino Sans", "Noto Sans JP", sans-serif'
    fontSize: "14px"
    fontWeight: 400
    lineHeight: 1.5
  label:
    fontFamily: '"Segoe UI", "Hiragino Sans", "Noto Sans JP", sans-serif'
    fontSize: "13px"
    fontWeight: 600
  code:
    fontFamily: '"Fira Code", Consolas, "Courier New", monospace'
    fontSize: "13px"
    fontWeight: 400
    lineHeight: 1.5
rounded:
  md: "10px"
  pill: "999px"
components:
  button-primary:
    backgroundColor: "{colors.console-blue-fill}"
    textColor: "#ffffff"
    rounded: "{rounded.md}"
    padding: "10px 20px"
  button-primary-hover:
    backgroundColor: "{colors.console-blue-fill-hover}"
  card:
    backgroundColor: "{colors.slate-panel}"
    rounded: "{rounded.md}"
    padding: "18px"
  input:
    backgroundColor: "{colors.ink-well}"
    textColor: "{colors.text-primary}"
    rounded: "{rounded.md}"
    padding: "10px 12px"
---

# Design System: Java学習システム

## Overview

**Creative North Star: "The Night Console"**

The app reads as a single instrument panel a solo learner sits at after hours: deep navy field, a handful of glowing status widgets across the header (days-to-goal, streak, Silver/Gold badge progress), and one deliberate accent blue reserved for anything the user can act on right now. Nothing else competes for attention — there is no marketing voice, no illustration, no gradient; the interface exists purely to keep one person's study loop legible at a glance.

The mood is calm and focused. Every surface is flat and quiet by default; color is spent only when it means something (an interactive control, a pass/fail signal, a streak). This restraint is what makes the header widgets and score badges pop without the UI ever feeling busy — the palette has almost nowhere else to spend saturation, so when it does, it reads clearly.

Components are understated and utilitarian: thin hairline borders, a single shared corner radius, no shadows, no ornament. The system trusts background-layering and semantic color alone to carry structure and status, the way a terminal or diagnostics panel would rather than a consumer app.

**Key Characteristics:**
- Dark, single-theme console field — three fixed background layers (page → panel → well) instead of light/dark variants.
- One interactive hue (console blue); green/amber/red exist only as pass/caution/fail signals, never decoration.
- Fully flat — depth comes from borders and background-layer contrast, never `box-shadow`.
- A single custom inline-SVG line-icon set (`icons.js`) stands in for all iconography; no emoji anywhere in the UI.
- One shared corner radius (10px) everywhere, plus a full pill (999px) reserved for status chips.
- Monospace type appears only where Java code is actually read or written.
- Browser chrome is themed, not left default: scrollbars, text selection, and placeholder text all use the same palette as the rest of the UI.

## Colors

The palette is almost entirely neutral navy; color is rationed and always semantic.

### Primary
- **Console Blue** (`#5b8def`): the system's only interactive/accent hue — active tab underline, link text, focus rings, the goal-countdown widget. Used as text/border against the dark surfaces, where it clears 4.5:1+ contrast on its own.
- **Console Blue Fill** (`#2f6fe0`) / **Console Blue Fill Hover** (`#2159c4`): the same hue, deepened specifically for solid button backgrounds. White button text on the original Console Blue only reached ~3.2:1 (below the 4.5:1 floor for normal-size text); these two shades exist so filled buttons stay legible without inventing a second accent hue.

### Neutral
- **Deep Navy** (`#0f1420`): page background, the outermost layer.
- **Slate Panel** (`#171e2e`): panel/card surface, one step lighter than the page.
- **Ink Well** (`#0b0f18`): the recessed surface for anything the user reads or types into (textareas, inputs, level/problem/history cards) — one step darker than the page, the deepest layer in the stack.
- **Panel Border** (`#2a3348`): the single hairline border color used on every panel, input, and card.
- **Text Primary** (`#e6e9f0`): body and heading text.
- **Text Dim** (`#9aa4bb`): secondary/meta text — labels, subtitle, timestamps, inactive tabs.
- **Cert Silver** (`#b9c2d0`): reserved specifically for the Java Silver certification icon, distinguishing it from Text Dim by intent even though the values sit close.

### Status (semantic only)
- **Signal Green** (`#3ecf8e`): correct answer, level cleared, good review score, completed certification.
- **Warning Amber** (`#e9b949`): streak widget, favorited items, mid-range review score — "in progress" or "worth noting," not an error.
- **Alert Red** (`#ef5b5b`): incorrect answer, low review score, destructive/error state. Error box copy uses a lighter tint, **Alert Red Text** (`#ff9b9b`), for readability against its own tinted background.

### Named Rules
**The Signal-Only Rule.** Blue is the only interactive color; green, amber, and red are reserved exclusively for status (pass / caution-or-streak / fail) and never used decoratively.

**The Two-Weight Blue Rule.** One hue, two lightness roles: Console Blue stays light enough to read as text/border against Deep Navy; Console Blue Fill / Fill Hover stay dark enough that white button labels clear 4.5:1. Never put white text on Console Blue itself, and never use the Fill shades as text on a dark background.

## Typography

**Display/Body Font:** "Segoe UI", "Hiragino Sans", "Noto Sans JP", sans-serif
**Code Font:** "Fira Code", Consolas, "Courier New", monospace

**Character:** A plain, legible system-UI sans for every label and paragraph, with a distinct monospace voice that appears only around actual Java code — the typographic equivalent of the palette's restraint.

### Hierarchy
- **Display** (700, 22px): the single `<h1>` app title in the header. Appears once per page.
- **Headline** (700, 16px): panel/section titles (`## 学習目標`, `## レビュー結果`, etc.).
- **Title** (700, 14px, Text Dim): subsection group headers — level-grid category headings, chart titles.
- **Body** (400, 14px, line-height 1.5–1.7): paragraph text, review prose, problem descriptions — including `<pre>` blocks that hold explanatory prose rather than code.
- **Label** (600, 12–13px, often Text Dim): tab buttons, badges/pills, meta rows (timestamps, difficulty), form labels.
- **Code** (400, 13px, monospace, line-height 1.5): the only role used for text the user reads or writes as Java source — code input textareas and code-preview snippets.

### Named Rules
**The Monospace Boundary Rule.** Monospace type appears only where Java source is being read or written (code textareas, code-preview snippets) — even prose that discusses code (problem descriptions, review results) stays in the sans stack.

## Layout

A single-column app shell: header → top-level tab nav → padded main content area, no sidebar chrome. Page gutter is a consistent 32px left/right (`header`, `.tab-nav`, `main`), with 20–24px of vertical breathing room between blocks.

Two content patterns repeat throughout: a **single flat panel** (goal form, stats) and a **two-column layout that collapses to one column** below a breakpoint — the review tab's editor+history split (`1fr 320px` → `1fr` under 900px) and the goal form's 3-up field row (`repeat(3, 1fr)` → `1fr` under 700px). Card/tile grids (level grid, stat tiles) use `auto-fit`/`auto-fill` with a `minmax()` floor (140–220px) rather than fixed column counts, so density adapts to viewport width without an explicit breakpoint.

Spacing is not tokenized in code but follows a consistent rhythm in practice: ~8px for tight inline gaps (badge rows, action rows), 12–14px for internal card/panel padding and list-item gaps, 18–20px for panel padding and block spacing, 24–32px for page-level gutters and section separation.

## Elevation & Depth

Fully flat — there is no `box-shadow` anywhere in the stylesheet. Depth is conveyed entirely by a fixed three-step background stack (Deep Navy page → Slate Panel surface → Ink Well recessed surface) plus a single 1px Panel Border hairline on every panel, card, and input. Interactive depth changes (hover, active, selection) are communicated by swapping the border color to Console Blue or a status tint, never by adding a shadow or lifting the element.

### Named Rules
**The Layered-Well Rule.** Depth comes from three fixed background layers and a one-step border-color change on interaction — never from shadows or elevation transforms.

## Shapes

One shared corner radius (`10px`) is used on every rectangular surface without exception: panels, buttons, inputs, textareas, selects, and every card type (level, problem, history-item). The only other radius in the system is a full pill (`999px`), reserved exclusively for status chips (header widgets, cert badges, score/difficulty badges, favorite button when active). There are no sharp-cornered surfaces and no radius values outside these two. Borders are uniformly 1px and solid (or a low-opacity tint of a status color on chips); nothing is clipped or masked beyond the radius itself.

## Components

### Buttons
- **Shape:** 10px radius (`{rounded.md}`).
- **Primary:** Console Blue Fill background, white text, 14px/600 weight, `10px 20px` padding.
- **Hover:** background darkens to Console Blue Fill Hover; no shadow, no scale change.
- **Disabled:** 60% opacity, `cursor: not-allowed`.
- **Ghost/Link:** transparent background, Console Blue text, no border, underline on hover (`.link-btn`; "もっと見る", "← 戻る" back-navigation).

### Status Pills (chips)
Used for every countdown/streak/badge/score/difficulty indicator (`.goal-widget`, `.cert-badge`, `.score-badge`, `.difficulty-badge`, `.favorite-btn.favorited`).
- **Shape:** full pill (999px radius), `6px 14px` padding.
- **Style:** a 12%-opacity tint of the semantic color as background, a 40%-opacity tint of the same color as a 1px border, and the full-strength semantic color as text. Neutral/undetermined state uses Text Dim + Panel Border tint instead of a status hue.
- **Complete state:** cert badges swap to the Signal Green tint once a certification's levels are cleared.

### Cards / Containers
- **Corner style:** 10px radius, matching every other surface.
- **Background:** Ink Well (`#0b0f18`) — one step darker than the parent panel, distinguishing list items/cards from the panel that contains them.
- **Border:** 1px Panel Border; on hover, border shifts to Console Blue (or stays Signal-Green-tinted if the level is already cleared).
- **Shadow strategy:** none — see Elevation & Depth.
- **Internal padding:** 12–14px.

### Inputs / Fields
- **Style:** Ink Well background, 1px Panel Border, 10px radius, `10–12px` padding; code textareas add the monospace Code type role.
- **Focus:** a global `:focus-visible` rule outlines any focused control (inputs, buttons, tabs, and the clickable level/problem/history cards) with a 2px Console Blue outline, 2px offset — keyboard focus is visible everywhere, not just where a component happens to inherit the browser default.
- **Placeholder:** placeholder text uses Text Dim rather than the unstyled browser default, so it stays legible against Ink Well.
- **Disabled/Error:** not distinctly styled at the field level; errors surface via a separate Alert-Red-tinted `.error-box`, not a field border change.

### Navigation
- **Primary tabs** (`.tab-nav`): transparent background, Text Dim label, underline-style active state — 2px bottom border and Console Blue text when active, no background fill.
- **Secondary tabs** (`.lib-nav`, the 問題集 sub-nav): bordered pill-ish buttons (1px Panel Border, 10px radius); active state adds a Console Blue border plus a 10%-opacity Console Blue fill, unlike the primary tabs' fill-less underline.
- **Mobile:** no distinct mobile nav pattern exists; the same tab row is used at all widths.

### Icon System (signature component)
A single custom inline-SVG line-icon set (`icons.js`) replaces all iconography app-wide — this app explicitly moved away from emoji icons to this set. Every icon shares one geometric language: 24×24 viewBox, `stroke-width: 1.8`, round `stroke-linecap`/`stroke-linejoin`, `stroke="currentColor"` (so color is always controlled by the surrounding CSS, never hardcoded), with `fill="none"` by default and an opt-in `filled` mode for solid glyphs (star, award). Display size is controlled purely by CSS utility classes (`.icon` 13px, `.icon-sm` 10px, `.icon-lg` 15px, `.title-icon` 16px), never by SVG attributes.

## Do's and Don'ts

### Do:
- **Do** use the shared 10px radius (`{rounded.md}`) on every rectangular surface; reserve the 999px pill radius exclusively for status chips.
- **Do** treat Console Blue as the system's only interactive hue; keep green/amber/red scoped to pass/caution-or-streak/fail meaning.
- **Do** build all iconography from the `icons.js` line-icon set (24×24, stroke-width 1.8, round caps/joins, `currentColor`).
- **Do** use the Code typography role only for Java source the user reads or writes; keep review/description prose in the sans body role even when it discusses code.
- **Do** use the Ink Well background to mark anything "one step deeper" than its parent panel (inputs, list cards), keeping the three-layer stack consistent.

### Don't:
- **Don't** use emoji as UI icons or tab/status labels — this system deliberately replaced its emoji icons with the `icons.js` line-icon set; never reintroduce emoji glyphs into the interface.
- **Don't** introduce a second interactive accent color; Console Blue is the system's only "this is clickable" signal.
- **Don't** repurpose Signal Green / Warning Amber / Alert Red for anything other than their pass / caution-or-streak / fail meaning — they are status signals, not a decorative palette.
