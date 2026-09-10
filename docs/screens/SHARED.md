# Shared Screens — Screen-by-Screen Spec — SAND WORKS

Status: **SPECIFIED.** Screens reachable by more than one role; re-scope by role.

## Trip Detail (shared owner/driver)
- Shows date/time, tractor, driver, participating labourers, rate snapshot, total, trip number.
- Actions by role: owner (edit/void any), driver (edit own permitted). Conflict handled.
- Security: role + org + ownership.

## Notifications / Notification Detail
- Centre listing own notifications (types A–F: A daily accrued-money summary, B operational alert, C approval outcome, D trip assignment, E temporary-assignment expiry, F operational).
- Open → deep link target (re-validated). Mark read/unread; badge.
- Wording: daily accrued-money summary, never "payment". No cross-user money broadcast.

## Settings (role subset)
- Owner full settings (summary time 19:30 Asia/Kolkata default, notification prefs, org profile). D/L see only their own allowed settings (e.g. notification prefs).

## Help / About
- SAND WORKS brand, version, private one-owner nature, contact owner. Uses locked logo.

## Profile / Edit Profile (any role)
- Own name, role, phone, status, photo (plan-dependent), own stats. Edit allowed own fields within owner-controlled bounds. Security: own record.

## Change Password
- Backend-secure change; sign-in again after if required. Validation.

## Logout confirmation
- Confirm → sign out → Welcome. Sessions cleared; no data fabrication.

## Global overlays
- Network Unavailable, Maintenance, Session Expired, Account Blocked, Assignment Expired — see `docs/architecture/ERROR-STATES.md`.
