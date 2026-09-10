# ERROR / LOADING / EMPTY / OFFLINE STATES — SAND WORKS

Status: **SPECIFIED.**

## Loading
- Initial loading: skeleton/spinner where appropriate.
- Button progress during submits; **prevent duplicate submission** (disable while in flight).
- Never show fake "loading completed" then fake success.

## Empty
Honest empty states with context + action (usually reset/new):
- no trips, no users, no pending approvals, no tractors, no labourers, no leaderboard participants, no accrual, no notifications, no search results, no export history.
- Empty states never fabricate data or default values that mislead.

## Error (typed catalogue)
- authentication failure; permission denied; network failure; Firebase/backend unavailable; timeout; rate limited; validation error; conflict (concurrent edit/revision); duplicate request; expired session; expired temporary access; feature unavailable (plan); server error; export failure; notification failure; image upload failure.
Each: message, optional retry, stays on screen (no fake nav), no data loss of in-progress form.

## Offline / network degradation (online-first)
- Connection indicator when offline.
- Retry; exponential backoff where appropriate (sync/retry tasks).
- Stale cached display allowed ONLY clearly marked as not authoritative/stale.
- **Never show "saved successfully" unless the backend accepted the authoritative write.**
- Failed write → pending/error + retry; duplicate prevention; recovery after reconnection re-syncs correctly.

## Success
- Real confirmation after backend acceptance; snackbar/state as appropriate.

## Special account states
- Permission denied / Forbidden, Unauthorized/SessionExpired, AccountBlocked, AssignmentExpired, NetworkUnavailable, MaintenanceUnavailable.

## Global state layer
Referenced by SCREEN-STATE.md; each screen maps its operations to these states.
