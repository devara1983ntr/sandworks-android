# Navigation Architecture — SAND WORKS

Status: **SPECIFIED.**

## 1. Root navigation & auth
- Root: an Auth gate. Not signed in → Auth flow. Signed in → role routing.
- Deep-link / shared content: re-validates auth + role + org + ownership + existence before showing content; otherwise honest Forbidden/NotFound.

## 2. Authentication navigation
`Welcome → Sign In | Sign Up | Forgot`; Sign Up (D/L) → `Approval Pending`; valid session → role dashboard. Session expired/blocked → Session Blocked → Sign In.

## 3. Role routing
- OWNER → owner bottom-nav shell.
- DRIVER → driver bottom-nav shell.
- LABOURER → labourer bottom-nav shell.

## 4. Bottom navigation (4–5 primary destinations max per role)
- **OWNER (4):** Home · Trips · People · More. (Sub-functions — money, reports, audit, settings, tractors, approvals — under More or contextual detail screens; not a 9-item bar.)
- **DRIVER (4):** Home · My Trips · My Totals · More.
- **LABOURER (4):** Home · My Days · Leaderboard · More.
Selected/unselected states per Material 3; badge for notifications/pending approvals where appropriate.

## 5. Hamburger / drawer
- Hamburger appears only where a drawer is the right pattern (secondary/tertiary actions). For each role the drawer contains only that role's items. OWNER-only functions never appear in D/L drawers.
- Contents (role-scoped): profile, notifications, settings, help, export (owner), security/account, logout.
- Behaviour: tap item → navigate; outside-tap closes; back closes drawer; a11y labelled; no owner-only items in D/L.

## 6. Detail & modal navigation
- Detail push (e.g. Trip Detail from list). Modals/bottom sheets for quick pickers (tractor select, labourer select). Full forms are screens, not cramped sheets.

## 7. Nested flows & back stack
- Add/Edit Trip is a flow with back to originating list; after success navigate to Trip Detail or return with result.
- Temporary-assignment flow, export flow, emergency-warning flow are each nested with defined back.

## 8. Deep links (if required)
- Notification → target screen after re-validation. Reset link → Forgot/verify.

## 9. Routing on events
- Session expiry → Session Blocked (drop to sign-in). Unauthorized → Forbidden. Temporary-access expiry → Assignment Expired / reduced to labourer.

## 10. Back / top-left arrow deterministic behaviour
- Top-left back arrow and system Back behave identically to pop the current destination to its logical parent.
- Inside a form with unsaved changes → warn/confirm before leaving (don't lose data silently; don't fake-save).
- Modal open → back dismisses modal first.
- Keyboard visible → back first dismisses keyboard.
- Operation in progress → block navigation-with-context (show in-progress; prevent duplicate submit) but allow cancel where safe.
- Session expires mid-flow → force to Session Blocked.
