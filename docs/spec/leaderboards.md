# Leaderboards — SAND WORKS

Status: **SPECIFIED.**

## 1. Scope & periods
- **Weekly leaderboard:** resets each week. Period boundary = Asia/Kolkata week (documented start day, e.g. Monday).
- **Monthly leaderboard:** calendar month in Asia/Kolkata, **top 3 only**.

## 2. Honest ranks — never fabricate
- If only **one** user qualifies → show 1st place only.
- If **two** qualify → show 1st and 2nd only.
- Never display a fake 3rd place when fewer than three qualify.
- If none qualify → empty state ("no data yet"), not a fabricated table.

## 3. Ranking metric
- Primary metric: number of qualifying trips (or accrued amount) in the period — documented constant. Use **trip count** as the leaderboard metric (matches "share today's trips" and trip-based labour tracking). Ties resolved by a deterministic secondary key.

## 4. Tie behaviour (deterministic)
- Equal primary metric → tie-break by deterministic secondary key (documented: e.g. earlier first-qualifying-trip timestamp, then user id). Same result every computation.

## 5. Minimum-data behaviour
- A user qualifies if they meet the documented minimum (e.g. ≥1 eligible trip in the period). Periods with no eligible trips produce an empty/zero state.

## 6. Data source & integrity
- Derived **by the backend** from real persisted trips, at weekly/monthly boundaries.
- Deterministic and idempotent; recomputable; never client-invented.
- Only real, eligible participants appear; no fabricated ranks.

## 7. Privacy
- Leaderboard shows rank + count/accrued for the top participants as permitted by owner viewing rules; a labourer sees top-3 and their own rank; the owner sees organisation leaderboard.

## 8. Timezone/dates
- All period boundaries and resets use **Asia/Kolkata**, consistent with daily closure. Period labels shown in local calendar terms.
