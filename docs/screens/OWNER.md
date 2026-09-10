# OWNER Screens — Screen-by-Screen Spec — SAND WORKS

Status: **SPECIFIED.** Role: OWNER only unless noted. Every screen follows the shared screen-behaviour contract (SCREEN-STATE.md) and role scoping (roles-access.md). Bottom nav: Home · Trips · People · More.

## Home (Owner Dashboard)
- Purpose: operational overview.
- Content: today's trips; today's work; money/accrual summary; active drivers/labourers; recent activity; pending approvals; alerts.
- Actions: quick links; open pending approvals; add tractor.
- States: loading/empty(honest)/error/offline/forbidden. Security: org-scoped reads.

## Daily Summary
- Purpose: review a date's accrued-money summary and closure state.
- Shows: total trips, per-person accrued, closure status/time. If a scheduled closure did not run and no server path exists, show honest state + documented fallback (idempotent).

## Trip Management / Trip History / Search-Filter Trips
- List all org trips with filters (date range, driver, labourer, tractor, status) + sort (see search-filter-sort.md). Empty/error/offline; paginated.

## Add Trip / Edit Trip / Trip Detail (owner may add/edit any)
- Add: date/time, tractor(active), driver, labourers present, rate snapshot, submit → backend number. Edit permitted fields; revision conflict handling. Detail: full read with actions (edit/void). No fabricated numbers.

## Tractor List / Add / Edit / Detail
- Per tractors.md. List with active/inactive filter. Add/Edit forms with validation/duplicate handling. Detail with per-tractor totals + history.

## Driver List / Detail; Labourer List / Detail
- List org people by role with status filter/search. Detail: profile, status, approval state, totals/accrual (owner-visible), attendance. Actions: disable/suspend/reactivate, (driver detail: see trips).

## User Approval / Pending Users
- List pending D/L registrations; Approve/Reject each (with optional note). Audit; notify applicant (type C). Security: owner-only; backend-authoritative.

## Temporary Access
- List/create assignments; create: target labourer, start, expiry, reason, scope. Revoke. Per temporary-access.md state machine. Enforced by backend.

## Rate Configuration
- Set per-trip rate (₹ default 200). Shows current; preview effect (future only). Historical immutable. Owner-only; audit.

## Money/Accrual Overview
- Org-wide accrued totals by person/date/period; filter; derived from closure. Owner-only.

## Person Accrual Detail
- One person's accrued breakdown by date/trip; own-scope rule for that person's money visible to owner.

## Daily Closure
- View closure per date; trigger/re-run idempotently (server-authoritative). Show status. Honest about scheduling backend.

## Weekly / Monthly Summary
- Summaries over period: totals, trips, per-person; owner-only; derived from persisted trips.

## Leaderboards
- Weekly/monthly top ranks (honest, per leaderboards.md). Owner sees org leaderboard.

## Attendance
- Per labourer per date working/absent; owner marks/corrects with reason; audited; conflict handled.

## Emergency Warning
- Composer: optional message, recipients, confirmation step → backend-sends strongest-compliant urgent notification. Delivery state, history, retry, duplicate prevention, cancellation where possible, audit. Never claims silent/DND override. (See notifications.md.)

## Message Composer / Message History (owner-only broadcast)
- Compose to recipients; send; history with delivery state; audit. Owner-only.

## Export Center / Export Configuration
- Choose range, breakdown, format PDF/CSV; generate; download/share; history. Owner-only; server-side where plan supports; honest fallback otherwise. (exports.md.)

## Audit / Activity History
- Read-only audit log; filters by user/action/date.

## Owner Profile / Application Settings / Security-Account Settings
- Profile (own) incl. photo (plan-dependent). Settings: summary time (19:30 Asia/Kolkata default), notification prefs, org profile. Security/account: change password, session, logout, (2FA PROPOSED/owner decision).
