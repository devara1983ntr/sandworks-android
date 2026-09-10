# PRD — Product Requirements Document: SAND WORKS

Status: **SPECIFIED.** Revision 1.0. Owner-input items are marked clearly.

## 1. Product vision
A trustworthy, private, mobile operational tool that lets a single farm/operation owner coordinate tractors, drivers and daily labourers, record trips, and transparently communicate to each person exactly what they have **accrued** — ending guesswork and disputes over who worked and what they have earned.

## 2. Problem
A small operation hires drivers and daily labourers across several tractor trips each day. Manual tracking leads to:
- disputes over who worked on which trip/tractor and on which days;
- unclear money accrual (rate, distribution, totals);
- no single shared source of truth accessible to each person.

## 3. Users
| Persona | Real role | Notes |
|---|---|---|
| Ramesh Sahu | **OWNER** | Single owner, ultimate authority. |
| Mansingh Rana | **DRIVER** (primary/authorized driver) | Authorized to perform driver operations. **Not an admin.** |
| Other drivers | DRIVER | Approved by owner; each sees own scope. |
| Daily labourers | LABOURER | Approved by owner; operationally read-only. |

## 4. Roles & goals
- **OWNER goals:** run the operation; approve/register users; manage tractors, trips, rates, distribution; review accruals, attendance, leaderboards; send messages and emergency warnings; export; audit.
- **DRIVER goals:** record trips accurately; pick tractor and labourers; see own totals/history; share today's trip count.
- **LABOURER goals:** see own accrued money, working/absent days and dates, weekly + monthly rank; stay informed via notifications.

## 5. Non-goals
- Not a public SaaS, marketplace, or social product.
- Not a general payroll/payment/disbursement system — accrual tracking only.
- No multi-owner, ADMIN, or delegated administrative role.
- No legacy data migration; not derived from any prior app.
- Not offline-primary; not a public-store release (unless owner later decides).

## 6. Functional requirements (summary — full detail in PRD2 and docs/)
Cover: auth lifecycle, owner/approval, tractor registry, trips, rate & distribution config, accrual & daily accrued-money closure (19:30 Asia/Kolkata default), attendance, leaderboards, notifications, emergency warning, messaging, exports, profile/photo, search/filter/sort, audit. See `PRD2.md` and the `docs/screens`, `docs/spec`, and `docs/architecture` documents for the authoritative per-feature detail.

## 7. Non-functional requirements
- **Online-first**, authoritative Firebase backend; graceful network UX.
- Android native (Kotlin + Compose + Material 3); package `com.roshan.sandworks`.
- Money in **integer paise**; never float for authoritative money.
- APK target ~15–25 MB (≤50 MB practical upper bound).
- Accessible (see `docs/quality/ACCESSIBILITY.md`), responsive, fast (see `PERFORMANCE.md`), secure (see `SECURITY.md`), testable (see `TESTING.md`).
- Private/family data handling; no analytics PII; approvals backend-enforced.

## 8. Business rules (invariants)
See `docs/spec/money-engine.md`, `docs/spec/roles-access.md`, `docs/architecture/DATABASE.md`, `API.md`, `SECURITY.md`. Key invariants are summarised in `AGENT.md` §3. Highlights: exactly one owner; no admin (Mansingh Rana = DRIVER); accrued-money wording ("daily accrued-money summary"); ₹200 default rate with immutable per-trip snapshot; equal-split default distribution; backend-authoritative number/closure/leaderboard/expiry; read-only labourer; approval-before-access.

## 9. Acceptance criteria (top-level)
1. Owner can register/approve users and manage tractors, rate, distribution.
2. Authorized driver can create/edit trips with authoritative numbering and immutable rate snapshot.
3. Daily accrued-money closure runs idempotently at the configured evening time and never double-counts.
4. Each user sees only their own private figures; labourers are read-only.
5. Labourers see working/absent days/dates and honest weekly/monthly top-3 leaderboards (no fabricated ranks).
6. Owner can message and send emergency warnings; delivery is backend-authoritative and honest about Android limits.
7. All money is accrued-money in integer paise; never labelled "paid".
8. No fabricated data, backend, or notification anywhere; no secrets committed.

## 10. Dependencies & risks
- **External/blocked:** real Firebase project (Auth, Firestore, App Check, FCM, Crashlytics), paid-plan features (Storage/Cloud Functions where required) pending owner plan decision, release signing key. See `docs/implementation/status.md` and the blocker register.
- **Risks:** money double-counting (mitigated by idempotent closure + revision), cross-user money leakage (RBAC/rules), fake-offline drift (guarded by online-first policy), permission mis-configuration.

## 11. Privacy & security
Private/family data; least privilege; owner-only sensitive operations; backend authority; auditability; no PII analytics; secure Android storage; dedicated release signing; secrets never committed. Detail in `SECURITY.md` and `docs/architecture/DEPLOYMENT.md`.

## 12. Operational constraints
- Private APK distribution is the delivery target.
- Runs against an owner-provisioned Firebase backend.
- Must be maintainable by a small team of coding agents using `docs/implementation/`.
