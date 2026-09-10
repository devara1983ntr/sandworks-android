# LABOURER Screens — Screen-by-Screen Spec — SAND WORKS

Status: **SPECIFIED.** Role: LABOURER (own scope, operationally read-only). Bottom nav: Home · My Days · Leaderboard · More.

## Labourer Dashboard (Home)
- Purpose: personal metrics, read-only.
- Shows (own, derived from real persisted trips): total trips; accrued money; remaining/accrued outstanding (as defined); working days; absent days; working dates; absent dates; weekly + monthly rank.
- No write actions. States: loading/empty(honest zeros)/error/offline. Wording accrued-only.

## Today's Work
- Own participation today (trips/labourer presence); read-only.

## Trip History (read-only)
- Own trip participation by date; filters; read-only.

## Accrued Money
- Own accrued figure + breakdown by date/period. Derived; accrued-only wording.

## Remaining/accrued balance
- The outstanding/remaining accrued amount as defined by the product (the tracked accrued total owed, never "paid"). Label honestly.

## Working Days / Absent Days / Date Detail
- Days/absent counts; dates lists; a date detail shows trips/status that day. Read-only.

## Weekly Leaderboard / Monthly Top-3 Leaderboard
- Top-3 (honest; fewer participants → fewer ranks) + own rank. Period Asia/Kolkata. No fabricated ranks.

## Notifications / Profile / Settings / Account-Access Status
- Own notifications; own profile edit within bounds; settings subset; account/access status (active/approval/temporary-assignment). Read-only operational.
