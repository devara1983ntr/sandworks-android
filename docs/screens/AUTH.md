# Auth & Global Screens — Screen-by-Screen Spec — SAND WORKS

Status: **SPECIFIED.** Each screen lists: purpose, entry/exit, role, app-bar/back, actions, content, states, security, post-action navigation.

Shared screen-behaviour contract (applies to all screens, defined in `docs/spec/SCREEN-STATE.md`): top app bar with deterministic top-left back; content hierarchy; responsive; accessibility; loading/empty/error/retry/success/disabled/permission-denied/network/validation/confirm-destructive/snackbar; state restoration; scroll/keyboard/focus; gestures per GESTURES.md; no fake success.

## Splash/Initialization
- Purpose: show locked logo, initialize, restore session.
- Entry: app open. Exit: route by session state.
- Content: logo, subtle progress. States: checking session; if session valid route to role dashboard; else Welcome.

## Welcome
- Purpose: entry point to sign in/up; show brand.
- Actions: Sign In, Create Account. Links: (help/about).
- Exit: Sign In, Sign Up.

## Sign In
- Fields: email, password. Actions: Sign in; Forgot password; Create account.
- States: idle/submitting/error(invalid)/account-not-approved/account-disabled-suspended/network(retry).
- On success route by role (backend role). Security: TLS; no client role assertion.

## Sign Up (DRIVER/LABOURER)
- Fields: name, phone(optional), role request (driver/labourer), email, password, confirm.
- Owner signup: provisioned by operator (no public create-owner).
- On submit (D/L) → Approval Pending. States incl. validation-error, already-registered, network.
- Security: role selection is a request only; grant by owner + backend.

## Forgot Password
- Field email → send reset (backend). Confirmation; do not reveal account existence. Network handled.

## Email/Account verification (if applicable)
- If verification is used: resend/verify status; not blocking if product chooses password-reset-only (owner decision). PROPOSED default: use secure password-reset; no mandatory email verify.

## Approval Pending
- Informational: awaiting owner approval. Action: sign out. No operational data shown.

## Account Rejected
- Honest state: rejected by owner. May be re-registered/retried per owner; shows no operational data.

## Account Suspended/Blocked / Disabled
- Honest: no access. Reason context if provided. Action: sign out; contact owner.

## Session Expired
- Message + sign in. Drop to Sign In.

## Network Unavailable (global)
- Banner/overlay + retry. Never fake success. Screens still show cached presentation where appropriate but writes show pending/error honestly.

## Maintenance/System Unavailable
- If backend reports maintenance/unavailable: honest system-unavailable state + retry.

## Profile (any role) — see SHARED.
## Logout confirmation — confirm → sign out → Welcome.

Refer to `docs/architecture/ERROR-STATES.md` for the global state catalogue and `SHARED.md` for Profile/Notifications/Settings/About.
