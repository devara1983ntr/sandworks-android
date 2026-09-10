# Roles & Access Matrix — SAND WORKS (authoritative)

Status: **SPECIFIED.** Backend-enforced. UI hiding is never a security control.

Roles: **OWNER** (Ramesh Sahu, exactly one), **DRIVER** (e.g. Mansingh Rana, plus approved drivers), **LABOURER**. No ADMIN. Mansingh Rana is a DRIVER.

Legend: R=read, C=create, U=update, D=delete/void, A=approve, X=execute/use.
- O = OWNER only; D = DRIVER (own scope); L = LABOURER (own scope, read-only).

| Capability | OWNER | DRIVER | LABOURER |
|---|---|---|---|
| Sign in / manage own session | ✓ | ✓ | ✓ |
| Own profile (read/limited edit) | ✓ | ✓ | ✓ |
| Profile picture (plan-dependent) | ✓ | ✓ | ✓ |
| Approve/reject driver & labourer | A | — | — |
| Disable/suspend/reactivate user | U | — | — |
| Grant/revoke temporary access | C/U | U (authorized only) | — |
| Manage tractor registry (add/edit/deactivate/reactivate) | C/U/D | R (active) | R (active) |
| View tractors available for a trip | ✓ | ✓ | — |
| Create trip | C | C | — |
| Edit own permitted trip | U | U (own, scope) | — |
| Void/correct a trip | U (audit) | U (own, error) | — |
| Configure rate & distribution rule | U | — | — |
| Read organisation accrual overview | R | — | — |
| Read own accrual / totals | R | R | R |
| Trigger/inspect daily closure | U/R | — | — |
| Attendance mark/correct | U (reason) | — | — |
| View own working/absent days & dates | ✓ | ✓ | ✓ |
| View leaderboards | ✓ | own rank + top | top-3 + own rank |
| Export PDF/CSV | X | — | — |
| Send broadcast message / emergency warning | X | — | — |
| Read audit/activity | R | — | — |
| Read another user's private money | — (owner may see org totals) | — | — |
| Act as admin | — (owner is the single authority; no ADMIN role) | — | — |

## Notes
- All reads/writes are org-scoped and (for D/L) own-scoped. Cross-org and cross-user private-money access is denied by rules.
- Owner capabilities are bounded by the product; every capability has validation, authorization, error handling and audit (see SECURITY/API/docs).
- No role may write another user's private financial data.
- DRIVER never becomes admin; LABOURER writes nothing operational.
