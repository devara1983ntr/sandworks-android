# DATABASE — Firestore Data Architecture — SAND WORKS

Status: **SPECIFIED.** Cloud Firestore is the authoritative backend. Model is online-first. Money in integer paise. Backend-authoritative fields are never client-written.

## Conventions
- Money fields stored as integer paise.
- All writes from Cloud Functions / rules-authorised; mutable docs carry `revision` (optimistic concurrency); idempotency keys on server ops.
- Server timestamps for createdAt/updatedAt.
- Org scoping: every doc path includes orgId.

## Collections & documents

### `orgs/{orgId}`
Fields: name; ownerUid; settings { defaultRatePaise=20000, distributionRule, summaryTime (default "19:30", tz Asia/Kolkata), retentionPrefs }; createdAt/updatedAt. Server-authoritative settings writes.

### `users/{uid}`
Fields: orgId; name; role (OWNER/DRIVER/LABOURER); phone; email; status (pending/active/rejected/disabled/suspended); approvedBy/approvedAt; profilePhotoUrl?; createdAt/updatedAt; revision. Role/status are server-authoritative (Cloud Functions). OWNER exactly one.

### `tractors/{id}`
Fields per tractors.md: orgId; name; model; identifier?; active; createdAt/updatedAt; revision. Indexes: orgId+active, orgId+name.

### `trips/{id}`
Fields: orgId; date; time; tractorId; driverUid; labourerUids[]; labourerPresence[]; rateSnapshotPaise (immutable); totalPaise; tripNumber (server, collision-safe); revision; voidedAt?/voidedBy?/voidReason?; createdAt/updatedAt; idemKey. Indexes: orgId+date, orgId+tractorId+date, orgId+driverUid+date, orgId+labourerUid+date (array-contains for labourer). TripNumber unique per org.

### `attendance/{orgId}_{labourerUid}_{date}`
Fields per attendance.md: orgId; labourerUid; date; state; source; correctedBy?; reason?; revision; timestamps.

### `rates/{id}` (history) and trip snapshots
- `orgs/{orgId}` current defaultRate; each trip stores immutable `rateSnapshotPaise`. A `rateChanges` sub-collection/log may record rate history (audit). Never recompute history.

### `distributionRules/{id}` / org settings
Current rule in org settings; per-trip/closure uses snapshot or current at closure as documented.

### `closures/{orgId}_{date}`
Fields: orgId; date; status; tripIds[]; perUserAccruals map {uid: paise}; summary; generatedBy; timestamps; idemKey; audit. Idempotent boundary (org,date).

### `earnings/accruals` (per user per period)
Derived/append-only from closures: `accruals/{orgId}_{periodKey}` or subcollection per user. Owner/person read per role.

### `leaderboards/{orgId}_{week|month}_{period}`
top3 computed by backend; honest ranks; deterministic tie.

### `tempAssignments/{id}`
Per temporary-access.md fields incl. start/expiry/status/scope/reason; backend-enforced expiry; indexes orgId+targetUser+status.

### `notifications/{id}`
recipientUid; type; payload; readAt?; sentAt; deepLink target.

### `messages/{id}` and `alerts/{id}`
Owner-only broadcast. message/alert: orgId, senderUid, recipients[], message, ackBy[], sentAt, deliveryState, history.

### `exports/{id}`
orgId; requester; range; breakdown; format; status; fileRef; createdAt.

### `audit/{id}`
orgId; actorUid; action; targetType; targetId; before/after; timestamp. **Server-written only.**

### `appConfig` / org config
Version/gates as needed (server).

## Integrity & consistency
- Transactions for multi-doc money writes (closure). Batch where atomic.
- Idempotency keys prevent double closure/duplicate.
- Query rules: rules are not filters — queries must match rules (documented query/rule matrix).
- Soft-delete/deactivation for tractors/users (status/active) — never hard-delete history.
- No cross-org/cross-user reads.

## Query & index plan
Documented composite indexes needed (Firestore): trips by (org,date); (org,tractor,date); (org,driver,date); (org,labourer via array-contains,date); users by (org,role,status); notifications by (recipient,unread); audit by (org,actor,date); closures by (org,date); leaderboards by (org,period). Each deployed with rules.
