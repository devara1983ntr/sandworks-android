# Tractor Model — SAND WORKS

Status: **SPECIFIED.**

## 1. Registry model
- Tractor registry is **OWNER-controlled**. The system supports approximately 4–5 operating tractors and multiple driver accounts.
- Initial records: **Sonalika** and **John Deere**, entered by the owner as registry records. These are **not** hard-coded as the permanent complete set; the owner may add/edit/deactivate as needed.

## 2. Fields
| Field | Type | Req | Notes |
|---|---|---|---|
| id | string | yes | auto |
| orgId | string | yes | scope |
| name | string | yes | human name (e.g. "Sonalika 60") |
| model/make (optional) | string | no | e.g. Sonalika, John Deere |
| identifier (optional) | string | no | plate/reg number |
| active | boolean | yes | soft-state |
| createdAt / updatedAt | timestamp | yes | server |
| revision | int | yes | concurrency |
| createdBy | uid | yes | owner |

## 3. Operations
- **Add:** owner only; validation — unique name within org (case-insensitive), optional identifier; duplicate handling → reject with clear error; active by default.
- **Edit:** owner only; revision conflict handled (surface, don't overwrite); audit.
- **Deactivate (soft):** owner; tractor no longer offered for new trips; existing trips keep their tractor reference/history.
- **Restore/reactivate:** owner; tractor available again for new trips.

## 4. Relationships
- Trip → tractor (reference). Per-tractor totals and trip history derive from trips referencing the tractor (even if the tractor is later deactivated).
- Driver association "where applicable": a trip records which driver operated which tractor; there is no enforced one-driver-per-tractor binding, but a tractor may be associated with trips by any approved driver.

## 5. Validation & duplicate handling
- Name required, trimmed, ≤ some max length; uniqueness within org.
- Duplicate add → validation error, no silent overwrite.
- Deactivated tractor referenced by an in-flight draft → warn user to choose an active tractor at submit.

## 6. Owner SOP pointers
See `docs/spec/SOP.md` (add tractor, deactivate tractor).
