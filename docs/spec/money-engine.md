# Money Engine — SAND WORKS (authoritative spec)

Status: **SPECIFIED.**

## 1. Money representation
- All authoritative money is **integer paise**. ₹200 = `20000` paise.
- Never use floating point for authoritative monetary calculation or storage.
- Display formatting (₹) happens only at the UI boundary from integer paise.

## 2. Rate & immutable snapshot
- Default per-trip rate: **₹200** (owner-configurable).
- The OWNER may change the rate at any time.
- Every trip records an **immutable `rateSnapshot`** at record time. Changing today's rate **never rewrites historical trips**. Historical trips keep their snapshot forever.

## 3. Trip money fields
For each trip:
- tractor selected; driver identified; participating labourers identified (present/eligible); applicable rate captured as `rateSnapshot`; total = pool; trip recorded; authoritative trip number (backend); money distribution computed per the approved rule.

## 4. Distribution rule (owner-configurable — exact allowed set)
- **Default:** equal split among the driver and the eligible labourers marked present/participating on that trip.
- The OWNER may configure the distribution rule. Permitted configuration options are limited to:
  1. **Equal** — pool divided equally among driver + each eligible present labourer (default).
  2. **Driver + labour share** — distinct share for driver vs each labourer (fixed ratio, owner-set).
  3. **Custom %** — owner-set percentages summing to 100%.
  4. **Fixed allocation** — owner-set fixed paise amounts.
- No other wage model may be invented.
- Distribution is computed by the backend for the daily closure, not trusted from the client.

## 5. Rounding in paise (deterministic)
- Pool is integer paise. Division that does not divide evenly leaves a remainder in paise.
- Deterministic remainder rule (documented and unit-tested): distribute the remainder one paise at a time in a fixed, stable order (e.g. lowest-priority recipients first, or a documented fixed participant order) so that the **sum always reconciles exactly to the pool**. The exact order is a documented constant; tests assert `Σ shares == pool`.

## 6. Labourer-count cases
- **Zero labourers present:** pool applies per rule (equal → the driver receives the full pool; other rules apply their driver share handling per configuration).
- **One labourer present:** equal → driver and the single labourer split; other rules per config.
- **Multiple labourers present:** equal → split evenly among driver + each present labourer; other rules per config.

## 7. Duplicate submission & in-flight guard
- A trip submission is idempotent: client generates an idempotency key; backend rejects a duplicate (same key/operation) rather than creating two trips.
- Client disables submit while in flight to prevent accidental double-tap; the backend is the authority.

## 8. Edits
- Permitted trip edits (driver-own within scope, owner-any) follow optimistic concurrency: each mutable trip carries a `revision`; on conflict the user is shown the situation and chooses; never silent last-writer-wins.
- Edits never change an already-snapshotted `rateSnapshot` unless the edit is a correction of an error before closure, which is itself owner/audit governed.

## 9. Deletion / voiding
- Trips are not hard-deleted from history. A trip may be **voided** (soft) by the owner with a reason; voided trips are excluded from accrual/closure and marked in audit. Never double-count a void then re-close without idempotency.

## 10. Historical-rate behaviour
- Past closures, leaderboards and person totals are computed from immutable snapshots and are not recomputed when the current rate changes. Only new/future trips use the new rate.

## 11. Concurrency & retry / idempotency
- Mutable documents carry `revision`; conflicts surfaced to the user.
- Server-authoritative operations (closure, accrual, numbering, leaderboard) carry idempotency keys and are transactional; retries never double-apply.

## 12. Daily accrued-money summary / closure
- Recommended default schedule: **19:30 Asia/Kolkata**, owner-configurable within a documented window (e.g. 18:00–22:00).
- Closure is **server-side scheduled** where the plan supports Cloud Functions; otherwise an honest fallback is documented (never a fake local schedule claiming server authority).
- Idempotency boundary: **(organisation, date)**. Running closure for the same date twice must not double-count.
- Exactly-once semantics; audit records each closure.
- Wording is **"daily accrued-money summary"** / "Today's earnings added" — **never "payment completed"**.

## 13. Integrity rules
- Money math deterministic, integer, unit-tested (see `docs/quality/TESTING.md`).
- Backend authoritative for money, numbering, closure, leaderboards.
- Idempotency protects money writes from retry duplication.
- Every money-related server operation is audited.
