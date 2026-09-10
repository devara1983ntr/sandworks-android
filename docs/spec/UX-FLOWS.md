# UX-FLOWS — End-to-End Workflows — SAND WORKS

Status: **SPECIFIED.** Each flow documents the main path and every conditional/failure branch. Backend-authoritative where noted.

## Flow 1 — New user → approval → dashboard
New DRIVER/LABOURER: Welcome → Sign Up (role request) → Approval Pending → (owner approves) → Sign In → role dashboard. Branches: rejected → Account Rejected (no access); disabled/suspended → Blocked; session expires → re-sign-in.

## Flow 2 — Owner approves a user
Owner → Approvals → review → Approve/Reject. Success: user active, audit, notify (C). Branch: reject → user sees rejection.

## Flow 3 — Driver creates a trip
Driver → +ADD TRIP → select date/time, tractor (active), labourers present → backend validates (role, active tractor/labourer, approval), snapshots rate, assigns number → saved → accrual queued → (closure) daily accrued-money summary notification. Branches: tractor inactive → choose active; labourer ineligible → error; duplicate → rejected (idempotent); offline → honest error/pending, retry; conflict → surface.

## Flow 4 — Driver absent → temporary labour assignment → expiry
Owner (or authorized driver) creates assignment (labourer, start, expiry, reason, scope) → backend enforces → labourer performs driver duties during window → expiry → access removed (Assignment Expired) → role back to LABOURER. Branches: overlap → defined resolution; revoked early by owner; login after expiry → denied.

## Flow 5 — Owner configures rate
Owner → Rate config → set new ₹ → snapshot future → historical trips unchanged. Branch: validate integer/paise.

## Flow 6 — Daily operation → closure
Trips recorded through day → scheduled closure (19:30 Asia/Kolkata) or documented fallback → snapshot trips → compute per-person accrued (integer, idempotent) → daily accrued-money summary → notify eligible. Branch: closure re-run → idempotent, no double count; partial data → honest.

## Flow 7 — Owner emergency warning
Owner → Emergency Warning → compose optional message → recipients → confirm → backend authorize → strongest-compliant urgent notification (sound/vibration/heads-up where OS permits; never silent/DND override) → delivery state + audit. Branches: delivery failure → retry; duplicate prevention; cancellation where possible.

## Flow 8 — Labourer reads dashboard/leaderboard
Labourer → dashboard (own metrics) → working/absent → weekly leaderboard (top-3 honest) → monthly top-3. Branches: <3 qualify → fewer ranks; none → empty; period reset Asia/Kolkata.

## Flow 9 — Owner exports
Owner → Export → range + breakdown + format → authorize → generate (server-side where plan supports) → download/share → history. Branch: empty range; failure; non-owner denied.

## Flow 10 — Profile photo
User → Profile → upload/replace/delete → validate → (plan) storage → success. Branch: plan lacks Storage → feature marked unavailable (not faked); failure → retry.

## Flow 11 — Notification deep link
Notification → open → re-validate auth+role+org+ownership+existence → target screen or honest Forbidden/NotFound.

## Flow 12 — Account recovery
Forgot → reset via backend → sign in. Branch: disabled → Blocked.
