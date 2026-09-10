# GESTURES — SAND WORKS

Status: **SPECIFIED.** Only meaningful gestures; no gimmicks. For each: screen, target, action, visual feedback, haptic, accessibility alternative, accidental-trigger protection, confirmation for destructive actions.

## 1. Tap
- Primary selection/activation (buttons, list items, tabs, bottom-nav, chips). Visual: ripple/pressed. A11y: standard button/list semantics. Accidental: min 48dp target.

## 2. Long press
- Context actions on list items where useful (e.g. a trip row → quick actions menu). Visual: selection/context. A11y: an explicit "More" affordance is also present (never rely on long-press only). Confirm destructive actions.

## 3. Swipe / horizontal actions
- Where used (e.g. notifications → dismiss; a list row → primary quick action). Visual: item slides + action revealed. A11y: buttons also exposed. Provide undo/snackbar for destructive; confirmation where appropriate.

## 4. Pull-to-refresh
- On dashboards, trip lists, notifications, leaderboards, reports (refresh from backend). Visual: standard pull indicator. A11y: refresh action also exposed. Respects online state (refresh only makes backend calls; offline shows error/retry, never fake).

## 5. Scroll
- Lists/history scroll; pull-to-refresh disabled while loading. Loading/empty/error handled.

## 6. Drag / dismiss
- Bottom sheet drag to dismiss; dialogs only via explicit action or system back (not accidental drag). Dismiss of in-progress forms → confirm (unsaved changes).

## 7. Back gesture / system back
- Deterministic per NAVIGATION.md §10. Edge-to-edge back gesture supported; no ambiguous leaves.

## 8. Keyboard interaction
- Forms: next/done; focus order logical; fields scroll into view; submit on IME action where single-field; block double submit.

## 9. Pinch / zoom
- Only if genuinely needed (e.g. profile image crop preview uses crop handles/drag, not necessarily pinch; export/report images not zoomed). Avoid unneeded pinch.

## 10. Image crop gestures
- Profile photo: crop overlay with drag handles + drag image; confirm/cancel; a11y buttons.

## 11. List interactions
- Tap → open detail; swipe quick-action; long-press context where used. Sort/filter via explicit controls (not gestures).

## Global rules
- Every gesture has a non-gesture alternative.
- Haptics: light on confirm/complete, error on failure, none where reduced-haptics.
- Destructive actions require confirmation; visual + (where set) haptic feedback on success/failure.
- Accidental-trigger protection: swipe targets have thresholds; undo offered for non-destructive dismisss.
