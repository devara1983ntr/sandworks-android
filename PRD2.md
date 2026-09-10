# PRD2 — Execution-Level Product Specification: SAND WORKS

Status: **SPECIFIED.** This document adds implementation-useful precision on top of `PRD.md`. It does not duplicate `PRD.md`; it expands every feature, workflow, edge case, rule, validation, state, permission and backend interaction. Screens are specified in detail in `docs/screens/`; this file is the horizontal, cross-cutting product spec.

## 1. Identity, roles & routing (canonical)
- Owner = Ramesh Sahu (uid provisioned as OWNER). Exactly one.
- Mansingh Rana = DRIVER (primary/authorized). No admin/delegate role exists.
- Role routing table (after sign-in, role from backend session):
  - OWNER → Owner dashboard.
  - DRIVER → Driver dashboard.
  - LABOURER → Labourer dashboard.
  - pending approval → Approval Pending screen (no operational access).
  - disabled/suspended/expired → Session Blocked → sign in.

## 2. Authentication & account lifecycle (states)
State machine for an account: `provisioned → pending → active | rejected | disabled | suspended`. Plus `session expired`, `logged out`.
- Sign Up: driver/labourer only for self-registration; owner account is provisioned by the operator (no public "create owner").
- New driver/labourer lands in `pending`; the OWNER approves or rejects in the Approvals screen. Until `active`, no operational data is returned (backend enforced).
- Password reset via backend secure flow. Session expiration and disabled/suspended handled truthfully; no fake success.
- Rejected user: sees rejection; may be re-registered/retried per owner.
- Approvals are recorded in audit.

## 3. Roles: detailed permission (capability) matrix
Read/create/update/delete/approve/configure/export/message/share per role. This is enforced by backend Security Rules + Cloud Functions, never UI-only. The authoritative matrix is in `docs/spec/roles-access.md` and `docs/architecture/SECURITY.md`. Representative rules:
- Only OWNER may: approve/reject users, disable/suspend, manage tractors (add/edit/deactivate/reactivate), set rate & distribution rule, correct attendance, export, send broadcasts/emergency warnings, read audit, see organisation-wide money.
- DRIVER may: manage own profile, read own dashboard/history/totals, add trips, edit own permitted trips within allowed window, select tractor from active registry and labourers, share today's trip count, support temporary assignment when authorized. DRIVER may NOT approve, configure money, export, see others' private money, or act as admin.
- LABOURER (read-only): own accrued money, working/absent days & dates, own trip participation, top-3 leaderboards + own rank, own profile, notifications. No writes to operational data.

## 4. Money & accrual precision
- Integer paise everywhere authoritative. ₹ default 200 (=20000 paise) per trip, owner-configurable.
- Per-trip immutable **rate snapshot** at record time. Historical trips never rewritten by rate changes.
- Distribution rule (owner-configurable, exact allowed set): equal (default) among driver + eligible labourers present; document permitted alternatives (equal/driver+labour share/custom %/fixed). Rounding in paise: leftover paise assigned by a deterministic documented rule so totals reconcile exactly to the trip pool.
- Cases: zero labourers → pool to driver per rule; one labourer → split driver+one; multiple → equal/split per rule. Duplicate submission prevented by idempotency key + client in-flight guard. Edits allowed per owner/driver rule with revision-based conflict handling. Deletion/voiding documented (soft-void + audit, never hard delete of history). Historical-rate: never recomputed. Concurrency: revision on mutable docs; on conflict show user and choose. Retry: idempotency keys so retries never double-count.
- **Daily accrued-money summary / closure**: around configured evening schedule (recommended default **19:30 Asia/Kolkata**), server-side scheduled execution where required; idempotent boundary (organisation,date); exactly-once; never double-counts; wording = "accrued", never "payment". Detail: `docs/spec/money-engine.md`.

## 5. Tractor model
Owner-controlled registry, initially seeded with Sonalika and John Deere as owner-entered records (not hardcoded as the permanent complete set). Support ~4–5 operating tractors + multiple drivers.
Operations: add, edit, deactivate (soft), restore/reactivate; fields name, optional identifier, active/inactive; validation (unique name within org), duplicate handling; per-tractor totals; trip history; driver association where applicable. Detail: `docs/spec/tractors.md`.

## 6. Temporary labourer access (business-critical)
If the regular driver is absent, the OWNER (or authorized driver, within documented limits) temporarily assigns a specific labourer the access to perform driver duties. Requirements:
- target a specific user; explicit `start` and `expiry`; backend-enforced; automatically invalid after expiry; does not permanently change role; auditable; revocable by OWNER.
- Behaviour after expiry login → access denied truthfully (Session/Assignment Expired).
- Overlapping assignment → defined resolution (newest valid / reject overlap; documented).
- No unauthorized privilege escalation.
Specified as a state machine + security rule in `docs/spec/temporary-access.md`.

## 7. Notifications & emergency warning (honest Android)
- Normal notifications: account approval, access changes, trip events, **daily accrued-money summary** (never "payment"), operational updates, system/account events.
- Owner-only broadcast. Emergency warning: confirmation step, optional message, recipient scope, delivery state, retry, duplicate prevention, notification history, audit, cancellation where possible.
- Use the **strongest Android-compliant urgent notification behavior**: high-importance channel, sound, vibration, heads-up where the OS permits, full-screen intent only where platform-appropriate. **Never claim** the app can force full volume in Silent/DND, override volume, or bypass DND. Distinguish device volume vs Silent vs DND vs permission/policy restrictions. Detail: `docs/architecture/notifications.md`.

## 8. Profile & profile picture
Every role has a profile. Profile picture support: upload/replace/delete, image validation, size limits, compression, crop, loading, progress, failure, retry, authorization, privacy, storage lifecycle. Firebase Storage is **conditional on the Firebase plan**; if Storage needs a paid plan it is documented as a plan/environment dependency, never assumed present. Detail: `docs/spec/profile-photo.md`.

## 9. Search, filter, sort
Defined wherever useful: users, tractors, trips, dates, driver, labourer, status (active/inactive), working/absent, leaderboard period, money/accrual history. For each: input behaviour, debounce, case handling, empty state, reset, sort stability, pagination if required, loading/error, permissions, backend query implications. Detail: `docs/spec/search-filter-sort.md`.

## 10. Attendance
Per labourer per date working/absent; owner marks and corrects with required reason; corrections audited; no silent overwrite; revision/conflict handling. Detail: `docs/spec/attendance.md`.

## 11. Leaderboards (honest)
Weekly leaderboard resets each week; monthly shows positions 1–3 only. Never fabricate ranks: if one qualifies show 1st only; if two show 1st–2nd only. Tie-break deterministic and documented. Minimum-data behaviour explicit. Timezone/period boundaries explicit (Asia/Kolkata). Detail: `docs/spec/leaderboards.md`.

## 12. Exports
Owner-only PDF + CSV, date-range and breakdown (overall/per tractor/per driver/per labourer/per date). Authorization by backend; generation; download/share per platform; export history; failure/retry. Detail: `docs/architecture/API.md`, `docs/spec/exports.md`.

## 13. Owner SOP
Operational procedures for the owner (approve, reject/block, add/deactivate tractor, add/edit trip, correct trip, configure rate, inspect accrual/leaderboards, send warning/message, export, manage profile, recover account, handle network/notification failure, temporary driver access, absent driver, review audit, prepare private APK release). Detail: `docs/spec/SOP.md`.

## 14. Edge cases & states (enumerated by operation)
Every operation in the system documents: success, validation error, permission denied, network failure, timeout, rate limited, conflict (concurrent edit), duplicate request, expired session, expired temporary access, feature unavailable (plan), server error, offline-with-retry. Consolidated in `docs/spec/ERROR-STATES.md`.
