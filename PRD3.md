SAND WORKS — Production-Ready Product Requirements Document

Trip Management • Labour Tracking • Driver Participation • Automatic Money Calculation • Attendance • Analytics • Export & WhatsApp Sharing

Application: SAND WORKS
Application ID / Namespace: com.roshan.sandworks
Version: 1.0.0 / Version Code 1
Platform: Android
Minimum SDK: 24
Target / Compile SDK: 36
Architecture: Native Android + Clean Architecture + MVVM/UDF
UI: Kotlin + Jetpack Compose + Material 3
Persistence: Room + DataStore
Backend: None
Network/API: None required for core operation
Primary currency: INR (₹)
Default trip rate: ₹200.00
Primary purpose: Accurate local management of sand-loading trips, participating labourers/drivers, accrued money, attendance, daily closure, history, analysis and reporting.


---

1. PRODUCT VISION

SAND WORKS is a private, local-only industrial operations application for recording sand-loading work performed by multiple tractors, labourers and drivers.

The application's most important principle is:

> Nothing is permanently assigned to a loading team. Every trip is an independent event.



The application must never assume:

a fixed tractor order

fixed labourer-to-tractor assignments

fixed driver-to-tractor loading participation

fixed number of labourers

fixed number of participants

that everyone present works every trip


Everything is calculated from what actually happened on that individual trip.

The application should eliminate manual calculation errors even when a day contains 10, 20, 33, 50+ trips.


---

2. CORE BUSINESS RULE

2.1 Trip rate

Default:

1 loaded trip = ₹200

This ₹200 is the total labour/accrual pool for that trip.

It is not ₹200 per person.


---

3. TRIP PARTICIPATION MODEL

For every individual trip, the application records:

Trip number

Date

Time

Tractor

Driver assigned to that tractor

People who physically participated in loading

Full-share participants

Optional half-share participant

Rate applicable to that trip

Calculation

Distributed amount

Remaining amount

Calculation status

Audit information


Example

T1 is loading.

Participants:

Labour A

Labour B

Labour C

Labour D

Driver 1


Total participants = 5.

₹200 ÷ 5 = ₹40

Everyone receives ₹40.


---

4. CRITICAL: TRACTOR ORDER IS RANDOM

The system must never assume a fixed tractor sequence.

Possible sequence:

> T1 → T2 → T3 → T1 → T2 → T3



But also:

> T1 → T3 → T2 → T1 → T1 → T3 → T2



Or:

> T3 → T3 → T1 → T2 → T1 → T3 → T2



Or any other valid sequence.

The user selects the actual tractor for each trip.


---

5. CRITICAL: DRIVERS CAN HELP OTHER TRACTORS

Drivers are not restricted to their own tractor for loading participation.

Example:

Tractor 1 is loading

Participants:

Labour A

Labour B

Driver 1

Driver 2


4 participants.

₹200 ÷ 4 = ₹50

Driver 1 → ₹50
Driver 2 → ₹50

Driver 3 → ₹0 for this trip.

Likewise:

Tractor 2 is loading

Driver 1 and Driver 3 can participate.

The application must therefore distinguish:

Driver's assigned tractor

from

Driver's actual loading participation.

These are two different pieces of data.


---

6. TRIP CALCULATION ENGINE

6.1 Normal calculation

For a trip with N full participants:

Per-person share = floor(₹200 / N)

The user's agreed settlement rule is whole-rupee calculation.

Examples:

Participants	Mathematical result	Settled share	Remaining

1	₹200.00	₹200	₹0
2	₹100.00	₹100	₹0
3	₹66.67	₹66	₹2
4	₹50.00	₹50	₹0
5	₹40.00	₹40	₹0
6	₹33.33	₹33	₹2
7	₹28.57	₹28	₹4
8	₹25.00	₹25	₹0


Therefore:

> Remaining Money = Trip Rate − Total Actually Distributed



The application must never silently lose the remainder.


---

7. MONEY STORAGE

Internally:

Never use Float or Double for financial calculations.

Use:

Long integer paise

Example:

₹200 = 20,000 paise

For normal settlement:

calculate using integer arithmetic

convert the agreed whole-rupee settlement to paise

preserve the remaining amount explicitly


Example:

₹200 / 3:

Share = ₹66 = 6,600 paise

3 × ₹66 = ₹198

Remaining = ₹2


Database therefore remains financially deterministic.


---

8. HALF-SHARE RULE

Half-share is:

optional

rare

manually decided by the other labourers

never automatically assumed


A participant can be marked:

FULL

or

HALF

For example, if a trip normally has 4 full participants:

₹200 ÷ 4 = ₹50.

A person approved for half-share may receive:

₹25

The treatment of the remaining money must be explicitly recorded according to the group's decision.

Important product requirement

Do not automatically invent where the other half goes.

The UI should provide a controlled option such as:

"Distribute remaining amount"

and allow the responsible user to select the recipients/amounts.

Every adjustment must be recorded in the trip calculation record.


---

9. ROUNDING POLICY

The app must have a clearly visible setting:

Settlement rounding

Whole Rupee — Round Down

Example:

₹200 ÷ 3 → ₹66 each → ₹2 remaining.

The system should never display ₹66.67 as the actual settled labour share if the configured settlement rule is ₹66.


---

10. DAY / TRIP NUMBERING

Trip numbering is per working date, not per tractor.

Example:

Morning:

Trip 1

Trip 2

Trip 3

Trip 4


Evening:

Trip 5

Trip 6

Trip 7


It must continue the same day's sequence.

Critical requirement

If morning ends at Trip 4 and work resumes in the evening:

> Evening starts at Trip 5, not Trip 1.




---

11. DATE WITH NO MORNING WORK

If no work occurs in the morning but work begins in the evening:

First trip is:

Trip 1

Then:

Trip 2, Trip 3, Trip 4...

There must be no artificial morning requirement.


---

12. DAILY TRIP EXAMPLE

Suppose:

T1 = 4 trips

T2 = 5 trips

T3 = 3 trips


Total:

12 trips

Gross trip amount:

12 × ₹200 = ₹2,400

But the actual distribution depends on the participant list of each individual trip.

The app must never calculate simply:

> T1 × 4 + T2 × 5 + T3 × 3



for labour earnings.

It calculates trip-by-trip.


---

13. DAILY ACCOUNTING EQUATION

For every working day:

Gross Trip Money

=

Total Trips × Applicable Trip Rate

Then:

Gross Trip Money

=

Distributed Money + Remaining Money + Any Explicit Adjustment

The dashboard must always reconcile these numbers.


---

14. EXAMPLE WITH DIFFERENT PARTICIPATION

Trip 1 — T1

A + B + C + D + Driver 1

5 people.

₹200 ÷ 5 = ₹40.


---

Trip 2 — T3

A + C + Driver 2

3 people.

₹200 ÷ 3 = ₹66.

Remaining = ₹2.


---

Trip 3 — T2

B + D + Driver 1 + Driver 2

4 people.

₹200 ÷ 4 = ₹50.


---

Trip 4 — T1

Only Labour A

1 person.

Labour A receives:

₹200


---

This illustrates why the application must calculate every trip independently.


---

15. PRODUCT INFORMATION ARCHITECTURE

Recommended bottom navigation:

1. Home


2. Trips


3. People


4. Reports



A fifth destination can be:

5. More



Settings and secondary administration can live under More.

The navigation should remain limited to 4–5 primary destinations.


---

16. COMPLETE SCREEN MAP

Primary

1. Splash


2. First-time setup


3. Home Dashboard


4. Add Trip


5. Trip Details


6. Edit Trip


7. Trip Calculation


8. Trip History


9. Daily Summary


10. People


11. Labourer Details


12. Add/Edit Labourer


13. Driver Details


14. Add/Edit Driver


15. Tractor List


16. Add/Edit Tractor


17. Attendance


18. Attendance Details


19. Reports


20. Analysis


21. Leaderboard


22. Calculation Summary


23. Date/Period Summary


24. Export


25. Share


26. Notifications


27. Settings


28. Data & Storage


29. Backup/Restore


30. Audit/Activity


31. About


32. Help / Calculation Rules


33. Error states


34. Confirmation dialogs


35. Empty states




---

17. SPLASH SCREEN

UI

SAND WORKS approved logo

Industrial charcoal background

Orange/sand-gold accent

Minimal animation

App name

Version


Use the supplied approved PNG logo as the source of truth.

Do not redraw or recreate the logo as SVG.

The two supplied PNG assets should be treated as brand assets, with the appropriate variant selected for the relevant context.

Behaviour

Splash should:

1. Load local database.


2. Load DataStore preferences.


3. Check database integrity/readiness.


4. Determine current date/time.


5. Determine whether first-time setup is complete.


6. Navigate to setup or Home.




---

18. FIRST-TIME SETUP

Only shown when required.

Step 1 — App setup

Application name

Currency

Default trip rate


Default:

₹200

Step 2 — Add tractors

Example:

Tractor 1

Tractor 2

Tractor 3


Each tractor:

Name/number

Brand

Model

Registration identifier — optional

Driver association


Step 3 — Add drivers

Name

Optional phone

Assigned tractor


Step 4 — Add labourers

Name

Optional phone

Active/inactive status


Step 5 — Calculation settings

Default trip rate

Rounding rule

Daily calculation time

Morning/evening display preference

Leaderboard preferences


Completion

Show:

"SAND WORKS is ready."

Do not insert sample people, tractors or trips.


---

19. HOME DASHBOARD

This is the operational command centre.

Header

SAND WORKS branding

Current date

Time

Morning / Evening indicator

Hamburger menu


Main card

Today's Work

Total Trips

Gross Money

Distributed

Remaining

Calculation status


Example:

> 12 Trips
₹2,400 Gross
₹2,388 Distributed
₹12 Remaining



Quick actions

Large touch-friendly actions:

+ Add Trip

Attendance

Today's Calculation

Share Today


Tractor activity

Show:

T1 — 4 trips

T2 — 5 trips

T3 — 3 trips


But this is only a trip count, not a labour assignment.

Recent trips

Latest 3–5 trips.


---

20. ADD TRIP SCREEN

This is the most important screen.

Header

Back arrow +:

Add Trip

Section 1 — Date/time

Automatically detect:

Current date

Current time

Morning/Evening


Allow editing if needed.

Section 2 — Tractor

Large selectable cards:

Tractor 1

Tractor 2

Tractor 3


No ordering assumption.

Section 3 — Trip number

Automatically calculated.

Example:

> Last trip today: 4
New trip: 5



User should normally not manually type the number.

Section 4 — Participants

Two grouped sections:

Labourers

☐ A
☐ B
☐ C
☐ D

Drivers

☐ Driver 1
☐ Driver 2
☐ Driver 3

The driver's assigned tractor should be shown as secondary information.

Example:

> Driver 2
Tractor 2



But Driver 2 must still be selectable while T1 or T3 is loading.

Section 5 — Participation type

Default:

Full share

Optional:

Half share

Only enable half-share when explicitly selected.

Live calculation panel

Immediately update:

> Trip Rate ₹200
Participants 5
Share/person ₹40
Distributed ₹200
Remaining ₹0



Save Trip

Before saving, validate everything.


---

21. ADD TRIP VALIDATION

Block save when:

No tractor selected.

No participant selected.

Duplicate participant selection exists.

Same person is accidentally selected twice.

Invalid date.

Invalid rate.

Closed date is being modified without reopening.

Trip number conflicts.

Database transaction fails.

Participant is inactive without explicit historical handling.

A voided trip is being edited as an active trip.


Warning, not automatic blocking

If:

only one participant exists

only a driver exists

only one labourer exists


show:

> "Only 1 participant is selected. They will receive the full ₹200."



Require confirmation.


---

22. TRIP DETAILS SCREEN

Display:

Trip identity

Trip #12

Date

Time

Tractor

Driver assigned to tractor

Status


Participants

Each participant:

Name

Type

Full/Half

Earned amount


Calculation

Example:

> Trip rate: ₹200
Full participants: 4
Share: ₹50
Distributed: ₹200
Remaining: ₹0



Actions

Edit

Recalculate

Void Trip

Share

Export

View audit history



---

23. EDIT TRIP

Editing must be controlled.

Possible edits:

Tractor

Participants

Participation type

Date/time

Rate if allowed by policy


Critical rule

The trip must store a snapshot of the rate used at creation/calculation time.

If the default rate later changes from:

₹200 → ₹250

old trips must remain ₹200.

Never retroactively recalculate historical trips simply because the global rate changed.


---

24. VOID TRIP

Never physically delete a financial trip by default.

Instead:

VOIDED

with:

reason

timestamp

who performed action

previous calculation

void status


Voided trips:

excluded from active totals

excluded from daily accrual

excluded from leaderboard qualifying activity

remain in audit history


This prevents accidental destruction of historical accounting.


---

25. DAILY CALCULATION SCREEN

Show a detailed reconciliation.

Header

Today's Calculation

Summary

Total trips

Gross trip amount

Distributed money

Remaining money

Voided trips

Active trips


Trip breakdown

Trip	Tractor	Participants	Share	Distributed	Remaining

1	T1	5	₹40	₹200	₹0
2	T3	3	₹66	₹198	₹2
3	T2	4	₹50	₹200	₹0


Person summary

Person	Trips	Money

A	8	₹...
B	7	₹...
C	9	₹...
D	6	₹...
Driver 1	5	₹...
Driver 2	6	₹...
Driver 3	4	₹...


No fabricated values.


---

26. DAILY CLOSURE

The application supports:

automatic calculation

manual Calculate Now

configurable daily calculation/closure time


Default:

7:00–8:00 PM operational window

Because Android background execution is not guaranteed to run at an exact clock minute, the product must not falsely promise exact background execution.

Recommended architecture:

WorkManager for best-effort scheduled processing

calculate on app open/resume

calculate when trip is saved

calculate when user presses Calculate

calculate when date changes

calculate at configured closure processing opportunity


Idempotency

Running calculation:

once

twice

five times


must produce the same result.

No duplicate money.


---

27. CLOSED DAY

Once a day is closed:

Normal users cannot silently modify historical records.

If editing is permitted:

> Reopen Day



requires confirmation.

Reopening should:

1. record an audit event


2. invalidate/recalculate affected totals


3. mark closure as reopened


4. recalculate


5. allow explicit re-close




---

28. CONTINUOUS TRIP NUMBERING

Suppose:

Morning:

Trip 1
Trip 2
Trip 3
Trip 4

Then work stops.

Evening:

Trip 5

Not:

Trip 1.

If there was no morning work:

Evening begins:

Trip 1.

The sequence is determined from the selected work date's previous valid trip records.


---

29. TRIP HISTORY

Filters:

Today

Yesterday

Date range

Tractor

Person

Driver

Labourer

Active

Voided


Search:

Trip number

Person

Tractor


Sort:

Newest

Oldest

Highest participants

Highest remaining money



---

30. PEOPLE SCREEN

Tabs/chips:

Labourers | Drivers

Each card:

Name

Today's trips

Today's money

Working status

Attendance indicator


Actions:

View

Edit

Attendance

History



---

31. LABOURER DETAILS SCREEN

This screen should provide a complete individual ledger.

Header

Name

Summary cards

Total Trips

Total Money

Working Days

Absent Days

Current Week Trips

Current Month Trips


Working history

Example:

Date	Trips	Money	Status

9 Sep	8	₹...	Working
8 Sep	6	₹...	Working
7 Sep	0	₹0	Absent


Date history

Allow:

day

week

month

custom range


Trip details

Show every trip they participated in.


---

32. LABOUR ATTENDANCE

Attendance must be distinct from trip participation.

This distinction is important.

A labourer can be:

Present + worked

Attendance = Working
Trips > 0

Present + did not load

Attendance = Present
Trips = 0

Absent

Attendance = Absent
Trips = 0

Therefore:

> Trip participation must not automatically overwrite attendance.



The user should be able to explicitly mark attendance.


---

33. ATTENDANCE SCREEN

Calendar view.

Statuses:

Working

Present

Absent

Not recorded


Do not silently classify missing records as absent unless the user has explicitly enabled that policy.

Statistics

Total working days

Total present days

Total absent days

Unrecorded days


Date details

Selecting a date shows:

attendance

trips

money

notes/reason if available



---

34. LEADERBOARD

Weekly

Resets every week.

Ranking metric:

Qualifying trips participated

Do not use total money as the ranking metric unless explicitly configured.

Example

Rank	Labourer	Trips

🥇 1	A	42
🥈 2	B	39
🥉 3	C	35


If only two people qualify:

Rank	Labourer	Trips

1	A	42
2	B	39


Never fabricate a third person.


---

35. TIE HANDLING

Tie handling must be deterministic.

Example:

A = 40
B = 40
C = 35

Both A and B receive the same ranking score.

The UI can show:

> 1st — A
1st — B
3rd — C



Or use a deterministic secondary ordering.

The exact policy should be fixed in the specification and tested consistently.


---

36. MONTHLY LEADERBOARD

Monthly ranking:

Current calendar month

Top 3 only

Reset at month boundary


Do not mix weekly and monthly statistics.


---

37. DRIVER DETAILS

Driver profile:

Name

Assigned tractor

Active status

Total loading participation

Total trips participated

Total accrued money

Attendance

History


Important:

> Driver's own tractor trips and driver's loading participation are separate metrics.



Example:

Driver 1 drives T1 but helps T2.

That T2 trip counts toward Driver 1's loading participation.


---

38. TRACTOR MANAGEMENT

Tractor list:

Tractor number/name

Brand

Model

Power/specification

Driver

Active/inactive

Total trips

Date-range trips


Example:

> Sonalika
5045



or:

> John Deere
5050



The app must support arbitrary tractor names/models.

No hardcoded tractor model list.


---

39. TRACTOR DETAILS

Display:

Total trips

Today

This week

This month

Date range

Assigned driver

Loading history


Important:

Tractor statistics describe tractor activity, not labour ownership.


---

40. REPORTS

Report types:

Daily

Trips

Tractor counts

Participant counts

Person totals

Gross

Distributed

Remaining


Weekly

Total trips

Labour ranking

Money

Attendance


Monthly

Total trips

Total gross

Total distributed

Remaining

Labour totals

Driver totals

Tractor activity

Attendance


Custom

User selects:

From date → To date.


---

41. ANALYSIS SCREEN

Professional visual analytics.

Metrics:

Trips/day

Trips/tractor

Labour participation

Driver participation

Money/day

Remaining money/day

Working days

Absence days

Top labourers

Most active tractor

Average participants/trip


Charts should only appear when sufficient real data exists.

No fake chart values.

Empty chart state:

> "Not enough recorded trips to analyse this period."




---

42. EXPORT

Export formats:

CSV

XLSX

PDF

Shareable summary


Export scopes

Today

Date range

Person

Tractor

Trips

Attendance

Complete report


Export must include

For trip report:

Date

Time

Trip number

Tractor

Assigned driver

Participants

Participant type

Participation share

Rate

Distributed

Remaining

Status



---

43. WHATSAPP SHARING

Do not require a WhatsApp API.

Use Android's standard:

ACTION_SEND / Sharesheet

User can select WhatsApp if installed.

Share formats

Today summary

Example:

SAND WORKS — Daily Summary

Date: 09 Sep 2026

Total Trips: 33
Gross: ₹6,600
Distributed: ₹6,578
Remaining: ₹22

Labour:

A — 25 trips — ₹...
B — 28 trips — ₹...
C — 22 trips — ₹...
D — 26 trips — ₹...

Drivers:

Driver 1 — ...
Driver 2 — ...
Driver 3 — ...

The app should generate the text from real local records only.

No fake values.


---

44. SHARE TRIP

A trip can be shared individually.

Example:

Trip #17

Tractor: T2
Time: 4:32 PM

Participants:

A — ₹40
B — ₹40
C — ₹40
Driver 2 — ₹40
Driver 3 — ₹40

Trip Total: ₹200
Remaining: ₹0


---

45. SETTINGS

Sections:

General

Currency

Language

Theme

Date format

Time format


Calculation

Default trip rate

Rounding

Daily calculation time

Closure behaviour

Half-share settings


People

Labourers

Drivers


Tractors

Tractor management


Leaderboard

Weekly reset

Monthly ranking

Tie rules


Notifications

Calculation reminder

Missing attendance

Unclosed day

Backup reminder


Data

Backup

Restore

Export

Storage information


Security

App lock

Biometric lock if supported

Privacy settings


About

Version

Brand

Legal information

Calculation rules



---

46. HAMBURGER MENU

The 3-line menu should contain secondary destinations:

Daily Summary

Attendance

Leaderboard

Analysis

Reports

Export

Backup & Restore

Audit History

Notifications

Settings

Help

About


Do not overload bottom navigation.


---

47. DATABASE ARCHITECTURE

Use:

Room

with normalized entities.

Recommended core entities:

Person

personId

name

type

active

createdAt

updatedAt


type:

LABOURER

DRIVER



---

LabourerProfile

labourerId

personId

notes

active



---

DriverProfile

driverId

personId

assignedTractorId

active



---

Tractor

tractorId

name

brand

model

specification

registrationIdentifier

active

createdAt

updatedAt



---

Trip

tripId

workDate

startedAt

completedAt

tripNumber

tractorId

ratePaise

status

createdAt

updatedAt


Unique constraint:

workDate + tripNumber


---

TripParticipant

tripParticipantId

tripId

personId

participationType

calculatedSharePaise

adjustmentPaise

finalSharePaise


Participation type:

FULL

HALF



---

TripCalculation

calculationId

tripId

ratePaise

participantCount

baseSharePaise

distributedPaise

remainingPaise

calculationVersion

calculatedAt



---

DailyClosure

closureId

workDate

grossPaise

distributedPaise

remainingPaise

status

calculatedAt

closedAt

reopenedAt



---

Attendance

attendanceId

personId

workDate

status

reason

notes

createdAt

updatedAt


Unique:

personId + workDate


---

AuditEvent

auditId

timestamp

eventType

entityType

entityId

description

oldValue/reference

newValue/reference



---

AppSettings

Stored primarily with DataStore where appropriate.

Examples:

default rate

calculation time

theme

notification settings

rounding policy



---

48. DATABASE SAFETY

Never automatically delete

No automatic deletion of:

trips

people

tractors

attendance

calculations

audit records


Instead:

Active / Inactive / Voided / Archived

where appropriate.


---

49. PEOPLE DEACTIVATION

If Labour A leaves the work:

Do not delete Labour A.

Mark:

Inactive

Historical trips continue displaying Labour A.

New trips should not normally offer inactive people.

Historical reporting remains intact.


---

50. TRACTOR DEACTIVATION

Same principle.

A tractor can become inactive.

Historical trips remain associated with it.


---

51. DATA INTEGRITY

Use Room transactions for:

Create trip + participants + calculation

This should be atomic.

Either:

everything succeeds


or:

nothing is committed.


Never allow:

Trip exists but participant records are missing.


---

52. CALCULATION TRANSACTION

When saving a trip:

1. Validate trip.


2. Validate participants.


3. Lock/read applicable rate.


4. Calculate share.


5. Calculate distributed.


6. Calculate remaining.


7. Save Trip.


8. Save participants.


9. Save calculation.


10. Update necessary aggregate state.


11. Commit transaction.




---

53. IDEMPOTENCY

If the same trip calculation runs multiple times:

Result must remain identical.

Do not add money every time calculation executes.

Calculations should be recomputed from source records, not accumulated blindly.


---

54. NO BACKEND

Core application must work without:

Firebase database

Supabase

server

REST API

cloud account

internet connection


The application is local-first.

API requirement

There is no required runtime API for the initial product.

Retrofit/OkHttp should not be added merely because they are listed as common Android technologies.

Only add networking if a future explicitly approved feature requires it.


---

55. LOCAL STORAGE

Primary:

Room

Preferences:

DataStore

Potentially store:

database version

calculation configuration

theme

notification configuration

onboarding state


Never store critical financial records only in DataStore.


---

56. BACKUP & RESTORE

Because there is no backend, backup is extremely important.

Provide:

Export Backup

A complete application backup containing:

people

tractors

trips

participants

calculations

attendance

settings

audit information


Restore

Require:

1. File selection


2. Validation


3. Schema/version validation


4. Integrity validation


5. Preview


6. Explicit confirmation


7. Transactional restore



Never overwrite data silently.

Provide options such as:

Replace local data

Merge, if merge semantics are implemented safely


Do not claim merge support unless it is actually implemented.


---

57. SECURITY

Since there is no backend:

Security focus is local device/data security.

Recommended:

optional app lock

biometric authentication

encrypted sensitive local preferences where justified

Android Keystore

no secrets in source code

no API keys

no service account credentials

no unnecessary permissions


Do not create fake "military-grade encryption" claims.


---

58. PERMISSIONS

Keep permissions minimal.

Potential:

Notifications

Android 13+:

POST_NOTIFICATIONS

Only request when notification functionality is actually enabled/needed.

Storage

Prefer modern Android Storage Access Framework / system file picker.

Do not request broad storage permission unnecessarily.

Biometric

Use Android biometric APIs if app lock is enabled.

No location, contacts, microphone, camera or phone permissions unless a future feature genuinely requires them.


---

59. NOTIFICATIONS

Useful notifications:

Daily calculation

> "Today's SAND WORKS calculation is ready."



Unclosed day

> "Today's trips have not been closed yet."



Missing attendance

> "Attendance has not been recorded for today's workers."



Backup reminder

Only if enabled.

Notifications should never contain unnecessary sensitive information on the lock screen.


---

60. TIME HANDLING

Use:

java.time

Store timestamps robustly.

The application should detect:

current date

current time

morning

afternoon

evening


But do not make business calculations depend solely on a vague "morning/evening" label.

The actual work date is authoritative.

Example:

9 Sep 2026:

Morning trips:

1–4

Evening trips:

5–12

All belong to:

9 Sep 2026


---

61. DAY BOUNDARY

Default day boundary:

Calendar date

Not "morning starts a new day."

If a trip occurs at 11:30 PM:

still the same calendar date.

If the next trip is at 12:10 AM:

new work date and new Trip 1.


---

62. AUTO CALCULATION

Calculation should happen at multiple safe points:

Immediately after trip save

Update today's live totals.

On dashboard open

Recalculate/verify today's derived totals.

Calculate button

Force calculation.

Scheduled daily processing

WorkManager best effort.

App resume

Check whether calculation/closure needs processing.

This creates resilience if Android delays background execution.


---

63. LIVE CALCULATION

While selecting participants:

5 participants:

> ₹40/person



Change to 4:

> ₹50/person



Add another driver:

> ₹40/person



Remove someone:

> calculation updates immediately.



The user should always see the consequence before saving.


---

64. CALCULATION PREVIEW

Before final Save:

Example

Trip #18

Tractor: T2
Rate: ₹200

Participants:

A
B
Driver 1
Driver 3

4 participants

Share:

₹50 each

Distributed:

₹200

Remaining:

₹0

Button:

Confirm & Save Trip


---

65. CONFIRMATION FOR RISKY ACTIONS

Require confirmation for:

Void trip

Delete/irreversible operation if any

Reopen closed day

Restore backup

Replace local database

Change historical calculation

Bulk edit


Do not use confirmation dialogs for routine actions excessively.


---

66. EMPTY STATES

Every major screen needs meaningful empty states.

No trips

> No trips recorded today.



Action:

Add Trip

No labourers

> No labourers added yet.



Action:

Add Labourer

No drivers

> No drivers added yet.



No tractors

> Add your first tractor to record trips.



No leaderboard

> Not enough qualifying work data yet.



Never show fake names.


---

67. LOADING STATES

Use:

skeletons

progress indicators

disabled action state


Avoid unnecessary loading for Room queries that complete immediately.


---

68. ERROR STATES

Database error

> "SAND WORKS couldn't read local data."



Actions:

Retry

Backup/Recovery

Contact/support information


Save error

> "The trip was not saved. No money was added."



Very important.

Export error

> "The report couldn't be generated."



Action:

Retry.

Restore error

> "Backup validation failed. Existing data has not been changed."



This message is critical.


---

69. ACCESSIBILITY

Minimum:

48dp touch targets

TalkBack

semantic labels

content descriptions

dynamic font scaling

sufficient contrast

no information conveyed by colour alone

keyboard/focus support where relevant



---

70. GESTURE UX

Use gestures carefully.

Swipe trip card

Swipe left:

Void


But show confirmation.

Swipe labourer

Swipe:

Quick attendance action


Long press

Long press trip:

Select

Share

Edit

Void


Pull to refresh

Useful for recalculating derived dashboard information.

Horizontal date swipe

Daily summary:

Swipe left/right to previous/next day.

Important

Every gesture must have an accessible alternative.


---

71. MATERIAL 3 DESIGN

Use:

Material 3

Compose

adaptive layouts

cards

segmented controls

chips

bottom sheets

dialogs

navigation bars


Corner radius:

12–16dp

Touch:

minimum 48dp

Spacing:

8dp grid


---

72. BRAND DESIGN

Locked palette:

Primary

#F97316

Deep Orange

#EA580C

Sand Gold

#F5B942

Deep Charcoal

#0B0D0F

Graphite

#15191D

Slate

#20262B

Steel

#66717A

Off White

#F5F7F8

Muted

#A7B0B7

Light Background

#F6F7F8

White

#FFFFFF

Light Text

#111518

Light Secondary

#5F686E

Light Border

#DCE1E4

These colours are locked.

Do not substitute arbitrary oranges/yellows or use #0D0D0D, #F59E0B, etc.

Semantic colours may be used for:

error

warning

success

informational states


without replacing the brand palette.


---

73. TYPOGRAPHY

Primary:

Roboto

Material 3 typography hierarchy.

Use strong typography for:

trip numbers

monetary totals

dashboard metrics

headings


Do not use decorative fonts.


---

74. LOGO

The supplied SAND WORKS PNG assets are authoritative.

Use the approved PNG directly.

Do not:

recreate

trace

redraw

convert into a replacement SVG

AI-regenerate

alter unnecessarily


The square icon/mark and horizontal logo variant should be selected according to the UI context.


---

75. VISUAL STYLE

The UI should feel:

Industrial + premium + operational + trustworthy

Not:

childish

gaming-like

generic banking

overly corporate

cluttered

flashy


Use orange/sand-gold as emphasis, not everywhere.


---

76. HOME UX PRIORITY

The user should be able to:

Open app → Add Trip → Select Tractor → Select Participants → See ₹ calculation → Save

with minimum friction.

The primary workflow should require very few taps.


---

77. QUICK ADD DESIGN

Consider a persistent:

+ Trip

FAB.

Tap:

Add Trip sheet opens.

The previous participants should not automatically become participants because that would violate the fundamental business rule.

However, the UI may show:

"Recent participants"

for fast selection, but they must require explicit selection.

No automatic carry-forward.


---

78. SMART PRESELECTION

Safe:

Remember recently used tractor as a convenience suggestion.

Remember recently used people as suggestions.


Unsafe:

Automatically selecting them.


Therefore:

> Suggested ≠ selected.



The user must explicitly confirm participants.


---

79. DUPLICATE PROTECTION

Prevent:

same person twice

duplicate trip number

accidental repeated save

duplicate calculation

duplicate attendance record


Double tapping Save should not create two trips.

Use transaction/idempotency protections.


---

80. OFFLINE BEHAVIOUR

Because the app is local-only:

Offline = normal operation.

Do not show:

> "No internet connection."



for normal functionality.

Instead:

> "Stored locally on this device."




---

81. DATABASE VERSIONING

Room migrations must be implemented.

Never solve schema changes by:

> deleting database



or

> destructive migration



in production.

Historical records must survive application updates.


---

82. DATA RETENTION

Default:

Never automatically delete operational data.

No:

automatic 30-day deletion

automatic yearly cleanup

automatic old-trip deletion


User explicitly controls archival/export if such functionality exists.


---

83. AUDIT LOG

Record important operations:

Trip created

Trip edited

Trip voided

Day closed

Day reopened

Rate changed

Person added

Person deactivated

Tractor added/deactivated

Attendance changed

Backup created

Restore performed

Calculation adjustment made


Audit history should be read-only.


---

84. RATE MANAGEMENT

Settings:

Default Trip Rate = ₹200

When changed:

Example:

9 Sep:

₹200

10 Sep:

₹250

Trips on 9 Sep remain ₹200.

The trip stores:

rate snapshot

so historical calculations remain correct.


---

85. REPORT RECONCILIATION

Every report should have a reconciliation footer.

Example:

> Gross Trip Money: ₹6,600
Distributed: ₹6,578
Remaining: ₹22
Reconciliation: ₹6,600 ✓



If numbers don't reconcile:

show an error state.

Never hide discrepancies.


---

86. PERFORMANCE

Target:

instant trip entry

Room queries off main thread

Compose stable state

LazyColumn for large trip lists

pagination/windowing for very large history

avoid unnecessary recomposition

indexed database fields

efficient aggregate queries


The application should remain responsive with thousands of historical trips.


---

87. DATABASE INDEXES

Important indexes:

Trip(workDate)

Trip(workDate, tripNumber)

Trip(tractorId)

Trip(status)

TripParticipant(personId)

TripParticipant(tripId)

Attendance(personId, workDate)

AuditEvent(timestamp)



---

88. CLEAN ARCHITECTURE

Recommended layers:

Presentation

Compose UI

ViewModels

UI State

UI Events


Domain

entities/value objects

calculation use cases

trip use cases

attendance use cases

report use cases


Data

Room

DAOs

repositories

DataStore

backup/export



---

89. USE CASES

Examples:

CreateTrip

UpdateTrip

VoidTrip

CalculateTrip

CalculateDay

CloseDay

ReopenDay

GetDailySummary

GetPersonSummary

GetLeaderboard

RecordAttendance

ExportReport

CreateBackup

RestoreBackup



---

90. REPOSITORY

Central repository:

SandWorksRepository

But avoid making one enormous class containing every implementation detail.

It can expose domain-oriented operations while internally delegating to focused data sources.


---

91. DEPENDENCY INJECTION

Use:

Hilt

Provide:

Room database

DAOs

repositories

calculation services

DataStore

WorkManager workers where appropriate



---

92. STATE MANAGEMENT

Use:

StateFlow

Flow

ViewModel

unidirectional data flow


UI:

State → UI

User:

Event → ViewModel → Use Case → Repository → Database → State

Avoid direct database access from Composables.


---

93. CALCULATION DOMAIN SERVICE

Create a dedicated calculation component.

Its responsibility:

Given:

rate

participants

participation types

adjustments


return:

participant shares

distributed amount

remaining amount

calculation metadata


It must be deterministic and heavily unit tested.


---

94. TESTING CALCULATION ENGINE

Mandatory tests:

1 participant

₹200 → ₹200

2

₹100 each

3

₹66 each + ₹2 remaining

4

₹50 each

5

₹40 each

6

₹33 each + ₹2 remaining

7

₹28 each + ₹4 remaining

8

₹25 each

Also test:

drivers

labourers

mixed participants

random tractor order

skipped labourers

cross-tractor drivers

inactive people

void trips

historical rates

repeated calculation

half-share

manual adjustment

closed days

reopened days



---

95. END-TO-END EXAMPLE TEST

Create:

33 trips.

Random tractor sequence:

T1, T3, T2, T2, T1, T3...

For every trip:

Random valid participant combination.

Then verify:

Trip count

33

Gross

33 × ₹200 = ₹6,600

Then calculate every person's earnings.

Finally:

Person totals + remaining money = ₹6,600

This should be an automated integration test.


---

96. EXTREME TEST

Test:

500+ trips

with:

changing tractor

changing participant count

changing drivers

skipped labourers

multiple dates

closed days

reopened day

exports


Verify:

no duplicate trips

no calculation drift

no missing participants

no UI freezing

totals reconcile.



---

97. UI TESTING

Compose UI tests:

onboarding

add tractor

add labourer

add driver

add trip

select participants

calculation preview

save

edit

void

daily summary

attendance

leaderboard

export

settings

theme switching



---

98. SECURITY TESTING

Verify:

no secrets committed

no API keys

no passwords

no signing credentials

no service-account files

no insecure logs containing private data

backup validation

restore safety

database corruption handling

biometric/app lock behaviour

exported file handling



---

99. LOGGING

Production logging must not expose:

unnecessary personal information

sensitive backup data

financial data unnecessarily

authentication secrets


Use structured logs.

Debug logs should be disabled/reduced for release.


---

100. CRASH HANDLING

Use a production crash monitoring solution only if intentionally configured.

Never insert:

fake DSN

placeholder DSN

dummy credentials


If monitoring isn't configured, leave the integration absent rather than pretending it works.


---

101. RELEASE BUILD

Production:

com.roshan.sandworks

Release:

1.0.0

Version code:

1

Use a dedicated production release keystore.

Never commit:

.jks

passwords

signing secrets

keystore properties


Use secure environment variables/CI secrets.

Do not reuse an unrelated legacy Flutter keystore.


---

102. BUILD QUALITY GATES

Before release:

Compilation

clean build

debug build

release build


Static analysis

Kotlin compiler

Android Lint

Detekt if adopted

dependency checks


Tests

unit

integration

Compose UI

migration tests


Database

migration verification

backup/restore verification


Calculation

reconciliation tests


Security

secret scanning

manifest permission audit



---

103. NO PLACEHOLDERS POLICY

This is a strict requirement.

The AI coding agent must not create:

placeholder screens

dummy data

fake users

fake trips

fake leaderboard values

mock production logic

temporary hardcoded calculations

fake API responses

fake database repositories

TODO production logic

// TODO

"coming soon" for required functionality

lorem ipsum

sample names presented as real records


If a feature is required by this PRD, implement it.

If it cannot safely be implemented, stop and report the blocker rather than silently substituting fake functionality.


---

104. NO HARDCODED BUSINESS DATA

The following must be configurable/data-driven:

people

tractors

drivers

trip rate

trip count

participant lists

dates

attendance

leaderboard

calculations


₹200 can be the default configuration, but should not be scattered throughout the application as hardcoded business logic.


---

105. NO AUTOMATIC DATA FABRICATION

The system must never infer:

> "A usually works, therefore A worked."



It must use explicit records.

Likewise:

> "Driver 1 owns T1, therefore Driver 1 loaded T1."



False assumption.

The system only records participation when explicitly selected.


---

106. DAILY WORKFLOW

Start of day

Open SAND WORKS.

Dashboard:

> 0 trips



User may record attendance.


---

First trip

Tap:

+ Trip

Select:

T1

Select actual loaders.

Preview calculation.

Save.

Trip #1 created.


---

Second trip

Tap:

+ Trip

Select actual tractor.

Select actual participants.

Trip #2 automatically generated.


---

Tractor changes

T1 → T3 → T2 → T1

No issue.


---

Labourer changes

A/B/C/D

then

A/C

then

B/D

No issue.


---

Driver changes

T1 loading:

Driver 1 + Driver 3 help.

Both receive shares if selected.

No issue.


---

Evening

The same date continues.

If morning ended at Trip 12:

Evening starts:

Trip 13


---

Calculation

The app continuously maintains the day's calculated state.

At configured time:

daily processing/closure occurs when Android permits scheduled work.


---

107. DAILY CLOSURE UX

Show:

> Ready to Close



with:

33 trips

₹6,600 gross

₹X distributed

₹X remaining


Then:

Close Day

Confirmation:

> "After closing, historical trip records will require reopening before modification."




---

108. AFTER CLOSURE

Dashboard shows:

DAY CLOSED

Actions:

View

Share

Export

Reopen


No accidental editing.


---

109. PARTICULAR LABOURER LEADERBOARD

Labourer profile can show:

This week

Trips: 32
Rank: #2

This month

Trips: 124
Rank: #1

Working history

Total working days: 24

Absent:

6 days

Unrecorded:

1 day

Dates expandable.


---

110. LEADERBOARD RESET

Weekly ranking is calculated using the current week window.

At a new week:

Previous week's ranking remains available in historical reports.

Current leaderboard resets automatically because its date range changes.

Do not delete previous rankings.

Monthly leaderboard similarly changes with month boundaries.


---

111. "ONLY 3 MONTHLY LEADERS"

For monthly:

Display maximum:

Top 3

If:

1 qualifying person → display 1

2 → display 2

3+ → display 3


Never create fake positions.


---

112. SEARCH

Global/local search can find:

labourer

driver

tractor

trip number


Example:

Search:

A

shows matching people and relevant records.


---

113. FILTERING

Trip filters:

date

tractor

driver

labourer

participant

status


People filters:

active

inactive

labourer

driver


Reports:

date range



---

114. DATE PICKER

Use Material 3 date picker.

Prevent invalid future historical operations unless explicitly allowed.

If future trips are not part of the workflow:

block future trip dates.

Allow historical correction only through controlled edit/reopen mechanisms.


---

115. DATA VALIDATION

Examples:

Person

Name:

required

trimmed

reasonable maximum length

no empty whitespace-only values


Tractor

Name/identifier:

required

unique among active tractors where appropriate


Trip

valid date

valid tractor

at least one participant

valid rate

valid participant identities



---

116. DUPLICATE PERSON PROTECTION

Warn if user tries to create:

> "Ramesh"



when an existing active person has the same name.

Do not blindly block because two people can legitimately have the same name.

Offer:

> Existing person found — use existing / create anyway.




---

117. INACTIVE PERSON LOGIC

Historical trip:

Allowed.

New trip:

Not selectable by default.

If necessary to record a historical correction:

allow controlled selection with a warning.


---

118. CALCULATION DETAILS

Every trip should make it possible to answer:

> "Why did A receive ₹66?"



Example:

Trip rate:

₹200

Participants:

A, B, C

Count:

3

Settlement:

₹66 each

Distributed:

₹198

Remaining:

₹2

This level of transparency is essential.


---

119. REPORT DETAIL LEVELS

Compact

Good for WhatsApp.

Standard

Good for phone viewing.

Detailed

Full trip-by-trip calculation.

Audit

Detailed modification history.


---

120. EXPORT FILE SAFETY

Exports should:

use UTF-8 where applicable

preserve ₹ correctly

use safe filenames

include export date/time

identify date range

include app version

never overwrite existing files silently


Example:

SAND_WORKS_Daily_2026-09-09.xlsx


---

121. BACKUP FILE VERSIONING

Backup must contain:

schema version

application version

export timestamp

data integrity metadata


Restore must reject incompatible/corrupt files safely.


---

122. MIGRATION TEST

Before every release:

Test:

v1 database → v2

without losing:

trips

participants

money

attendance

audit records.



---

123. PERFORMANCE TARGET

The common workflow:

Open Add Trip → select tractor → select people → save

should feel instantaneous on a normal modern Android device.

Do not perform unnecessary full database scans for every checkbox tap.

Use efficient derived state/calculation.


---

124. LARGE DATA HANDLING

The application must be designed for:

33 trips/day

100+ trips/month

thousands of lifetime trips

many attendance records


without requiring a database reset.


---

125. DATA CONSISTENCY CHECK SCREEN

Settings → Data Health

Show:

Database status

Number of trips

Number of people

Number of tractors

Number of attendance records

Last backup

Last calculation

Unreconciled records


If healthy:

> All local data checks passed.



If not:

show actionable repair/recovery options.


---

126. CALCULATION HEALTH

The system should periodically verify:

For every active trip:

rate = distributed + remaining

and:

sum(participant shares + explicit adjustments) = distributed

If mismatch:

flag:

Calculation Integrity Error

Do not silently correct financial records.


---

127. NO SILENT CORRECTION

If data becomes inconsistent:

Never:

> silently change money.



Instead:

preserve original

report mismatch

offer controlled recalculation

record audit event



---

128. ARCHITECTURAL PRINCIPLE

The application should follow:

> Source data first. Derived totals second.



The source records are:

trips

participants

rates

attendance

explicit adjustments


Totals can be recalculated.

This makes the system recoverable and trustworthy.


---

129. WHAT SHOULD NOT BE STORED AS THE ONLY SOURCE

Do not treat:

> "A earned ₹4,200"



as the only source of truth.

Instead store individual trip participation/calculation records.

Then:

> A's ₹4,200



is a derived result.

This is extremely important for corrections and audits.


---

130. USER EXPERIENCE PRINCIPLE

The app should make the user enter what happened, not perform mathematics.

User responsibility:

> "T3 was loading. A, C and Driver 2 loaded."



Application responsibility:

> "3 participants → ₹66 each → ₹198 distributed → ₹2 remaining."



That is the core value proposition of SAND WORKS.


---

131. FINAL PRODUCT FLOW

SPLASH
   ↓
FIRST-TIME SETUP
   ↓
HOME
   ↓
+ ADD TRIP
   ↓
SELECT TRACTOR
   ↓
SELECT ACTUAL PARTICIPANTS
   ↓
AUTOMATIC CALCULATION
   ↓
VALIDATION
   ↓
CONFIRM
   ↓
SAVE ATOMICALLY
   ↓
TRIP DETAILS
   ↓
TODAY'S TOTALS
   ↓
DAILY CALCULATION
   ↓
CLOSE DAY
   ↓
REPORT / EXPORT / SHARE


---

132. RECOMMENDED PRIMARY BOTTOM NAVIGATION

🏠 Home

Today's operation and quick actions.

🚜 Trips

Trip history and trip management.

👥 People

Labourers + drivers.

📊 Reports

Daily/weekly/monthly/custom analysis.

☰ More

Settings, attendance, leaderboard, backup, audit, help.

This keeps the primary navigation clean instead of putting 10+ destinations at the bottom.


---

133. FINAL ENGINEERING ACCEPTANCE CRITERIA

The AI coding agent must not consider SAND WORKS complete until all of the following work:

Core

[ ] Add/edit/deactivate labourers

[ ] Add/edit/deactivate drivers

[ ] Add/edit/deactivate tractors

[ ] Record trips

[ ] Random tractor sequence

[ ] Random labour participation

[ ] Cross-tractor driver participation

[ ] Skipped workers

[ ] Variable participant count

[ ] Automatic trip numbering

[ ] Morning/evening continuation

[ ] ₹200 calculation

[ ] Whole-rupee rounding

[ ] Remaining money

[ ] Half-share exception

[ ] Explicit adjustment

[ ] Historical rate preservation

[ ] Daily closure

[ ] Reopen

[ ] Void

[ ] Audit


People

[ ] Labour profiles

[ ] Driver profiles

[ ] Attendance

[ ] Working/absent dates

[ ] Individual trip history

[ ] Individual money totals


Analytics

[ ] Weekly leaderboard

[ ] Monthly top 3

[ ] Deterministic ties

[ ] No fake rankings

[ ] Daily analysis

[ ] Monthly analysis

[ ] Custom date ranges


Data

[ ] Room

[ ] DataStore

[ ] Transactions

[ ] Migrations

[ ] Backup

[ ] Restore

[ ] No automatic deletion

[ ] Integrity checks


UX

[ ] Dark theme

[ ] Light theme

[ ] Brand palette

[ ] Approved PNG logo

[ ] Material 3

[ ] Responsive Compose UI

[ ] Empty states

[ ] Loading states

[ ] Error states

[ ] Confirmation states

[ ] Accessible gestures

[ ] 48dp targets


Export

[ ] CSV

[ ] XLSX

[ ] PDF

[ ] WhatsApp/system share

[ ] Date-range export

[ ] Detailed trip export


Quality

[ ] Unit tests

[ ] Calculation tests

[ ] Database tests

[ ] Migration tests

[ ] Compose UI tests

[ ] Integration tests

[ ] 33-trip scenario

[ ] 100+ trip scenario

[ ] Randomized participant tests

[ ] Security checks

[ ] Release build

[ ] Signing verification

[ ] No secrets

[ ] No TODOs

[ ] No placeholders

[ ] No fake data

[ ] No mock production logic

[ ] No broken navigation

[ ] No unresolved compiler/lint errors



---

134. THE MOST IMPORTANT RULE FOR THE AI CODING AGENT

The implementation must be based on this exact mental model:

> A tractor trip is an independent financial calculation event.



Not:

> "Driver 1's tractor made 4 trips, therefore Driver 1 gets 4 shares."



Not:

> "Labour A usually works with Tractor 1, therefore select A."



Not:

> "Everyone present gets paid."



Instead:

> For every trip, record exactly who physically loaded it. Divide that trip's ₹200 according to those actual participants. Store the result. Repeat for the next trip.



Then aggregate those immutable/traceable trip-level results into:

Person → Trips → Money → Attendance → Ranking → Reports.

That model is what makes the application capable of handling 33 trips, 100 trips, random tractor order, constantly changing labour combinations, drivers helping other tractors, skipped trips, rounding leftovers and historical corrections without manual spreadsheet-style calculation.

And critically, the app should calculate the money—not guess who worked.
