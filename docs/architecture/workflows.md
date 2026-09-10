# Core Workflows — SAND WORKS

## 1. Account creation & approval (online)
1. New user opens SC-AUTH-WELCOME → SC-AUTH-SIGNUP, selects role (driver or labourer) and enters details.
2. Backend creates a **pending** account (role selection is a request only).
3. Owner sees it in SC-OWN-APPROVALS and approves/rejects.
4. On approval the account is activated; the user signs in and lands on their role dashboard. Rejected users see an honest rejection state.
5. (Owner account is provisioned by the operator — the one-owner rule.)

## 2. Daily operation — a trip
1. Driver opens SC-DRV-DASH and taps **+ ADD TRIP**.
2. Driver picks date/time, tractor (from active registry), labourers present; rate snapshot auto-applies; backend assigns a collision-safe trip number.
3. On save, backend authorises (role, own-scope, tractor/labourer valid, approval active), snapshots the rate, and records the trip. Any conflict is surfaced, not silently overwritten.
4. The trip now counts toward attendance, accrual and leaderboards.

## 3. Daily closure / accrual (server-authoritative)
1. At the configured time (default 19:30), the backend produces one daily closure for the organisation+date.
2. It snapshots eligible trips, applies the distribution rule per trip, and computes each user's accrued totals.
3. Closure is idempotent — re-running never double-counts.
4. Eligible users are notified with accrued-totals wording.

## 4. Attendance corrections (owner)
1. Owner marks a day working/absent, or **corrects** an existing record with a required reason.
2. Correction is audited; no silent overwrite. A concurrent change surfaces a conflict.

## 5. Reports & export (owner)
1. Owner builds a report over a date range and breakdown (overall/per tractor/per driver/per labourer/per date).
2. Owner exports PDF or CSV. Backend enforces owner-only scope. No fabricated totals.

## 6. Temporary assignment & expiry (labourer in a driver-like role)
1. Owner (or an authorised driver within limits) creates a temporary assignment with start/end/reason/scope.
2. The backend enforces expiry: access ends automatically at `end`; the assignee cannot extend it.
3. Expiry may trigger a type-E notification.

## 7. Operational alert (owner)
1. Owner composes a message and picks recipients.
2. Backend delivers a high-priority, prominent, acknowledgeable alert to those users (honest platform limits apply).
3. Acknowledgement is recorded.

## 8. Notifications → deep link
1. A notification arrives for a user.
2. Opening it re-validates auth + role + org + ownership + existence, then routes to the right screen; otherwise an honest Forbidden/NotFound is shown.
