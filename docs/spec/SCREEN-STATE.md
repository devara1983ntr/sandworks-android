# Screen-State & Behaviour Contract — SAND WORKS

Status: **SPECIFIED.** Applies to every screen; referenced by all screen specs.

## State machine (sealed UI state per screen)
Each screen exposes a truthful state: `Loading` → `Content | Empty | Error | Offline` plus transient `Submitting`, and role/account outcomes `Forbidden`, `Unauthorized/SessionExpired`, `AssignmentExpired`. No fake success, no fake loading completion.

- **Loading:** initial load; skeleton where appropriate; button progress on actions; prevent duplicate submission.
- **Empty:** clear, honest empty states (no trips/users/approvals/tractors/labourers/leaderboard/accrual/notifications/search/export). Actionable reset where filters apply.
- **Error:** typed — auth failure, permission denied, network, backend unavailable, timeout, rate limited, validation, conflict (concurrent edit), duplicate request, expired session, expired temporary access, feature-unavailable (plan), server error, export failure, notification failure, image upload failure. Message + retry where applicable.
- **Offline/network degradation:** online-first app shows connection indicator; retry; exponential backoff where appropriate; stale cached display only clearly marked; **never show "saved successfully" unless the backend accepted the authoritative write**; failed write handled honestly; duplicate prevention; recovery after reconnect.

## Behaviour contract per screen
- Purpose, entry/exit points, role visibility.
- Top app bar: deterministic top-left back (NAVIGATION.md §10); title; actions.
- Hamburger only per NAVIGATION.md (role-scoped; owner-only items never in D/L).
- Bottom navigation ≤5 role destinations; selected/unselected states; badge.
- Content hierarchy, cards/lists/forms/buttons/icons/typography/spacing/colours/surfaces per DESIGN-SYSTEM.md; responsive.
- Accessibility per ACCESSIBILITY.md; gestures per GESTURES.md.
- Validation with inline errors; confirmation dialogs for destructive actions; snackbar for ephemeral results.
- Navigation after success (documented target) and after failure (stay + error).
- State restoration on process death/rotation/back.
- Scroll/keyboard/focus behaviour; animation/haptics within DESIGN-SYSTEM rules.

## Session/role events
- Session expiry → Session Expired. Unauthorized → Forbidden. Temp-access expiry → Assignment Expired. Account disabled/suspended → Blocked.

## No placeholder rule
Every visible control works; every state is real. No inert/decorative controls (AGENT.md).
