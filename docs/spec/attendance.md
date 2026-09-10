# Attendance — SAND WORKS

Status: **SPECIFIED.**

## 1. Concept
Per labourer per date, an attendance record records whether they **worked** or were **absent**. Attendance drives the labourer's working/absent days, working/absent dates, and eligibility for accrual on trips that day.

## 2. Model
| Field | Type | Notes |
|---|---|---|
| id | string | `orgId_{labourerUid}_{date}` (derived) |
| orgId | string | scope |
| labourerUid | string | target |
| date | string/date | Asia/Kolkata day |
| state | enum | working / absent |
| source | enum | derived-from-trip / owner-marked / owner-corrected |
| correctedBy | uid | required when corrected |
| reason | string | required on correction |
| revision | int | concurrency |
| createdAt/updatedAt | timestamp | server |

## 3. Owner operations
- Mark a day working or absent for a labourer.
- **Correct** an existing record with a required reason → audited; no silent overwrite.
- Concurrent-change handling: on revision conflict, surface and let owner choose (never silently overwrite).

## 4. Derivation vs manual
- A labourer present on ≥1 trip that day is considered working (derived). Owner may still correct.
- Manual absent/working records exist where no trip is present (e.g. driver reported, or labourer marked absent).

## 5. Counts surfaced (labourer dashboard)
- working days, absent days, working dates, absent dates — derived from attendance/trips within a selected period.

## 6. Integrity
- Only OWNER marks/corrects. Labourer read-only. DRIVER does not change another's attendance.
- Corrections audited; idempotent writes; no double-count of a day.
