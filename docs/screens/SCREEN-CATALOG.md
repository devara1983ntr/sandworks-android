# Complete Screen Catalogue — SAND WORKS

Status: **SPECIFIED.** This is the exhaustive inventory. Every screen is detailed in the referenced role/shared spec files. No screen is listed purely to inflate the count — each exists because a real workflow needs it.

Conventions: role `O`=OWNER, `D`=DRIVER, `L`=LABOURER, `*`=all. Screen detail per screen is given in `AUTH.md`, `OWNER.md`, `DRIVER.md`, `LABOURER.md`, `SHARED.md` in this folder.

## Global / auth
| Screen | Role | Where |
|---|---|---|
| Splash/Initialization | * | AUTH.md |
| Welcome | * | AUTH.md |
| Sign In | * | AUTH.md |
| Sign Up | D/L | AUTH.md |
| Forgot Password | * | AUTH.md |
| Email/Account verification (if applicable) | * | AUTH.md |
| Approval Pending | D/L | AUTH.md |
| Account Rejected | D/L | AUTH.md |
| Account Suspended/Blocked | * | AUTH.md |
| Session Expired | * | AUTH.md |
| Network Unavailable | * | ERROR-STATES.md (global overlay) |
| Maintenance/System Unavailable | * | ERROR-STATES.md |
| Profile | * | SHARED.md |
| Edit Profile | * | SHARED.md |
| Change Password | * | SHARED.md |
| Notifications | * | SHARED.md |
| Notification Detail | * | SHARED.md |
| Settings | O (full) / D,L (own subset) | SHARED.md |
| Help/About | * | SHARED.md |
| Logout confirmation | * | SHARED.md |

## OWNER
| Screen | Where |
|---|---|
| Owner Dashboard (Home) | OWNER.md |
| Daily Summary | OWNER.md |
| Trip Management / Trip History / Search-Filter Trips | OWNER.md |
| Add Trip / Edit Trip / Trip Detail | OWNER.md (owner may add/edit any) |
| Tractor List / Add / Edit / Detail | OWNER.md |
| Driver List / Detail; Labourer List / Detail | OWNER.md |
| User Approval / Pending Users | OWNER.md |
| Temporary Access | OWNER.md |
| Rate Configuration | OWNER.md |
| Money/Accrual Overview / Person Accrual Detail | OWNER.md |
| Daily Closure | OWNER.md |
| Weekly Summary / Monthly Summary / Leaderboards | OWNER.md |
| Attendance | OWNER.md |
| Emergency Warning | OWNER.md |
| Message Composer / Message History | OWNER.md |
| Export Center / Export Configuration | OWNER.md |
| Audit/Activity History | OWNER.md |
| Owner Profile | OWNER.md |
| Application Settings / Security-Account Settings | OWNER.md |

## DRIVER
| Screen | Where |
|---|---|
| Driver Dashboard | DRIVER.md |
| Today's Trips | DRIVER.md |
| Add Trip / Edit Trip / Trip Detail | DRIVER.md (own) |
| Trip History | DRIVER.md |
| Tractor Selection / Labourer Selection | DRIVER.md (modals/sheets) |
| Daily Total | DRIVER.md |
| Accrued Money | DRIVER.md |
| Share Today's Trips | DRIVER.md |
| Notifications / Profile / Settings | DRIVER.md |
| Temporary Assignment status / Assignment Expired | DRIVER.md |

## LABOURER
| Screen | Where |
|---|---|
| Labourer Dashboard | LABOURER.md |
| Today's Work | LABOURER.md |
| Trip History (read-only) | LABOURER.md |
| Accrued Money | LABOURER.md |
| Remaining/accrued balance | LABOURER.md |
| Working Days / Absent Days / Date Detail | LABOURER.md |
| Weekly Leaderboard / Monthly Top-3 | LABOURER.md |
| Notifications / Profile / Settings / Account-Access Status | LABOURER.md |

## No unnecessary screens
Only the above are specified. If a build task proposes an extra screen, it must map to a documented workflow; otherwise it is rejected as scope creep.
