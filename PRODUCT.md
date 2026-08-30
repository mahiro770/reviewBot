# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users
The developer themself (mahiro770), studying Java toward the Oracle Java Silver/Gold certifications. Single-user personal tool — no accounts, no login, no multi-tenant data model. Not currently intended for other learners; see Capabilities and Constraints for the explicit decision behind that.

## Product Purpose
A self-study loop for learning Java: set a goal → answer daily practice problems → get an AI code review → see progress. It exists to keep the user's Java study consistent and measurable while they work toward Java Silver/Gold certification, and to give them hands-on practice with a real Spring Boot → Service → Repository → SQLite stack.

## Positioning
Combines a certification-scoped problem curriculum (12 levels: Silver 7 + Gold 5, stored in a `levels` table) with on-demand Gemini-generated problems and Gemini-scored code review, all wrapped in one continuous progress loop (goal countdown, streak, per-certification badge progress, score trend, activity chart). A generic "paste code, get an AI review" tool would not have the certification-scoped curriculum or the goal/streak/progress layer built around it.

## Operating Context
- Runs locally via `mvn spring-boot:run`, served at `http://localhost:8080`.
- Requires a user-supplied Gemini API key (free tier) as the `GEMINI_API_KEY` environment variable.
- Requires an internet connection in the browser only to load Chart.js from a CDN; the app itself is local.
- Data (goals, generated problems, reviews) persists in a local SQLite file (`review.db`) that survives restarts; schema is versioned via `SchemaMigrator` applying `db/migration/V*.sql` files in order.
- Four tabs: 目標 (Goal), 問題集 (Problem set), レビュー (Review), 進捗 (Stats). Header always shows days-to-goal, streak, and Silver/Gold badge progress.
- A level counts as "cleared" only after 2+ correct answers at that level (a single correct answer is treated as possibly lucky).
- Lists (review history, problem list, favorites, mistakes) are paginated 20 at a time with a "load more" button.
- Only one goal exists at a time; saving a new goal overwrites the previous one (no goal history).

## Capabilities and Constraints
- **JDBC-only, intentionally**: the user has decided to keep using Spring's `JdbcTemplate` rather than JPA, as a deliberate learning constraint of this project. Future work should preserve this rather than introducing JPA/Hibernate.
- Single-user, no-auth by decision (confirmed during init) — do not add accounts/login speculatively; that would only become relevant if the user later decides to open this to other learners.
- Gemini API calls are centralized in `GeminiClient`, which retries 429/5xx responses with exponential backoff.
- The Gemini model is swappable via `application.properties` (`gemini.api.model`), constrained to whatever the free tier allows.
- Review/problem-generation prompts are customizable by editing `SYSTEM_PROMPT` in `GeminiReviewService` / `GeminiProblemService`.
- Problem generation produces 1/3/5/10 problems per Gemini call (default 5, max 10) — batching problems into one call keeps API call count from scaling with problem count.
- Schema changes are additive-only: new `V3__xxx.sql`-style migration files; existing V1/V2 are not modified.

## Brand Commitments
- Product name/title: "Java学習システム" (Java Learning System). UI language is Japanese throughout.
- Certification terminology follows Oracle's official Java Silver/Gold naming and level scoping.

## Evidence on Hand
No user-facing marketing copy, testimonials, or case studies exist or are needed — this is a personal tool, not a product with an audience to persuade. Future work must not invent any.

## Product Principles
- Keep the loop closed: goal → problem → review → progress should stay visibly connected (header widgets, badges, streak) rather than becoming four disconnected tabs.
- Preserve the JDBC/Repository learning constraint over convenience abstractions.
- Treat the Gemini free tier's rate limits as a real constraint (retries, not unlimited assumptions) when touching anything that calls Gemini.
- Optimize for one user's continued daily use, not for scale, multi-tenancy, or onboarding flows for others.
- Don't add speculative auth/multi-user scaffolding — it's explicitly out of scope until the user decides otherwise.

## Accessibility & Inclusion
No accessibility requirement has been established yet; this is a personal single-user browser tool.
