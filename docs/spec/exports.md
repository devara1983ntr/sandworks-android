# Exports — SAND WORKS

Status: **SPECIFIED.** Owner-only.

## 1. Scope
- Formats: **PDF** (primary human-readable) and **CSV** (raw data).
- Owner-only. Not available to DRIVER or LABOURER.
- Date range + breakdown options: overall, per tractor, per driver, per labourer, per date.
- Content: money totals (accrued), trip/work counts, working/absent days.

## 2. Access & authorization
- Authorization enforced by backend/Cloud Function — owner role checked; not UI-only.
- Export generation is server-side where the plan supports Cloud Functions/Storage; otherwise a documented honest fallback (owner-scoped local generation) with clear limits. Never fake server-side generation.

## 3. Flow
1. Owner opens Export Center, chooses range + breakdown + format.
2. Backend authorizes and generates the file.
3. File available for download/share per platform.
4. Export recorded in history with audit.

## 4. States
- loading / generating / success-with-download / failure / empty (no data in range) / retry / permission denied (non-owner). No fabricated totals.

## 5. Integrity
- Money figures are accrued only, integer-derived; never "paid".
- No cross-org data; no PII beyond owner scope.
- Concurrency: one export at a time per user or queued with clear progress.
