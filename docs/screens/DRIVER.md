# DRIVER Screens — Screen-by-Screen Spec — SAND WORKS

Status: **SPECIFIED.** Role: DRIVER (own scope). Bottom nav: Home · My Trips · My Totals · More.

## Driver Dashboard (Home)
- Purpose: today's working view + primary ADD TRIP.
- Shows: today's trips; today's tractor; assigned labourers today; today's summary; recent trips.
- Primary action: prominent **+ ADD TRIP**.
- States: loading/empty(honest)/error/offline/submitting.

## Today's Trips
- List today's own trips; tap → Trip Detail; add. Refresh.

## Add Trip / Edit Trip / Trip Detail (own)
- Fields: date/time, tractor (active registry), driver=self, labourers present, rate snapshot (auto from current, immutable after record), total; trip number backend-authoritative.
- Driver may edit own permitted trips; revision conflict handling; never silent overwrite. Void only via owner (driver may request/correct errors within scope).
- States incl. conflict, backend-rejected, network.

## Trip History
- Own trip history with date filter/sort; paginated; empty/error/offline.

## Tractor Selection (within Add Trip)
- Modal/bottom sheet listing active tractors per authorization; pick; validation.

## Labourer Selection
- Modal listing active labourers to add as present participants; multi-select; eligibility check.

## Daily Total
- Own trips count for the day + work summary (today's total trip count).

## Accrued Money (own)
- Own accrued figure + breakdown; derived from closure; own-scope. Wording accrued-only.

## Share Today's Trips
- Share today's trip count via Android/WhatsApp share intent (real values; share-sheet fallback). No fabricated numbers.

## Notifications / Profile / Settings
- Own notifications (approval, assignment, alerts, daily accrued-money summary). Profile edit own fields; photo plan-dependent. Settings subset.

## Temporary Assignment status / Assignment Expired
- If the driver is acting under an owner-authorized capability or a labourer is temporarily assigned, show assignment status. "Assignment Expired" honest state when a temporary grant ends (no silent access). Driver is never an admin.
