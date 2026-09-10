# WIREFRAMES — SAND WORKS (specification wireframes)

Status: **SPECIFIED — these are specification wireframes, NOT implemented UI.** ASCII layout to communicate hierarchy. Owner approval of final visual mockups is separate (no mockup is claimed to exist here).

## Auth — Sign In
```
[ LOGO — SAND WORKS ]
Email           [_______________]
Password        [_______________]
[          Sign In (filled)     ]
Forgot password?      Create account
(network/error/approval-pending status area above button)
```

## Owner Dashboard (Home)
```
[⇧] SAND WORKS              [bell 🔔] [⋮]
Trips today: 12   Work: 3   Accrued ₹[amount]
Active drivers: 2  Active labourers: 5  Pending approvals: 1
Recent activity        [1 pending approval →]
  • 12:40 Trip #124 ...
Daily summary card        [open]
[B Home] [Trips] [People] [More]
```

## Driver Dashboard
```
[⇧] Good day, Mansingh
Today: 4 trips    Tractor: Sonalika
Assigned labourers today: 3
[        + ADD TRIP (primary)        ]
Recent trips list ...
[B Home] [My Trips] [My Totals] [More]
```

## Labourer Dashboard (read-only)
```
[⇧] Your summary
Total trips: 8    Accrued: ₹640
Working days: 3   Absent days: 1
Dates: [1 Mar ✓] [2 Mar ✓] ...
Weekly rank: #2   Monthly: top-3
[B Home] [My Days] [Leaderboard] [More]
```

## Add Trip (owner/driver)
```
[←] Add Trip
Date [01/03]  Time [12:30]
Tractor ▾ [Sonalika]   Driver [Mansingh Rana]
Labourers present: [+ add]  ☑ A ☑ B
Rate snapshot: ₹200 (immutable)
Total: ₹400  Trip # (auto on save)
[ Cancel ]           [ Save Trip ]
```

## Trip Detail
```
[←] Trip #124
Date/Time  Tractor  Driver  Labourers
Rate snapshot ₹200  Total ₹400
Status: active  [Edit] [Void (owner)]
```

## User Approval
```
[←] Pending Approvals
Name Role  Requested   [Approve] [Reject]
...
```

## Tractor Management
```
[←] Tractors    [+ Add]
Sonalika        active  [Edit] [Deactivate]
John Deere      active  [Edit] [Deactivate]
```

## Money / Accrual
```
[←] Accrual Overview (owner)
Date range [01/03–31/03]
Driver    trips   accrued
Labourer  trips   accrued
[Export PDF] [Export CSV]
```

## Leaderboard (monthly top-3)
```
[←] Monthly Leaderboard (Mar)
#1  A — 24 trips
#2  B — 20 trips
(no fabricated #3 if fewer)
```

## Emergency Warning (owner)
```
[←] Send Warning
Message (optional) [___________]
Recipients: ☑ all drivers ☑ all labourers
[ Cancel ]  [ Confirm & Send ]
Warning is urgent; Android may not override Silent/DND.
```

## Notifications
```
[←] Notifications
🔔 Daily accrued-money summary — today ... [unread]
⚠ Operational warning from owner ...
[read/unread filter]
```

## Profile
```
[←] Profile
[photo] Name  Role: DRIVER
Phone  Status: active
[Edit] [Change password] [Logout]
```

## Settings (owner)
```
[←] Settings
Daily summary time [19:30]
Notifications [on]
Org profile ...
```

## Export Center
```
[←] Export
Range [date]  Breakdown [per driver ▾]
Format [PDF|CSV]  [Generate]
History ...
```

## Representative empty/error/loading
```
Loading: skeleton rows
Empty (trips): "No trips yet. [Add trip]"
Error: "Couldn't load. [Retry]"
Offline: banner "Offline — showing latest cached data (not live). [Retry]"
```
