package com.roshan.sandworks.data.repository

import com.roshan.sandworks.data.local.*
import com.roshan.sandworks.domain.MoneyEngine
import com.roshan.sandworks.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.UUID

class SandWorksRepository(
    private val database: SandWorksDatabase
) {
    private val personDao = database.personDao()
    private val tractorDao = database.tractorDao()
    private val tripDao = database.tripDao()
    private val attendanceDao = database.attendanceDao()
    private val closureDao = database.dailyClosureDao()
    private val auditDao = database.auditDao()
    private val alertDao = database.emergencyAlertDao()

    // --- Emergency Alerts ---
    val activeEmergencyAlert: Flow<EmergencyAlertEntity?> = alertDao.getLatestUnacknowledgedAlert()
    val allAlerts: Flow<List<EmergencyAlertEntity>> = alertDao.getAllAlerts()

    suspend fun sendEmergencyAlert(
        title: String,
        message: String,
        severity: String = "URGENT",
        targetRole: String = "ALL"
    ) = withContext(Dispatchers.IO) {
        val alert = EmergencyAlertEntity(
            alertId = UUID.randomUUID().toString(),
            title = title.trim(),
            message = message.trim(),
            severity = severity,
            targetRole = targetRole,
            senderName = "Ramesh Sahu (Owner)",
            timestamp = System.currentTimeMillis(),
            acknowledged = false
        )
        alertDao.insertAlert(alert)
        auditDao.insertAuditEvent(
            AuditEventEntity(
                auditId = UUID.randomUUID().toString(),
                eventType = "EMERGENCY_ALERT_BROADCAST",
                entityType = "ALERT",
                entityId = alert.alertId,
                description = "Broadcast [$severity]: $title"
            )
        )
    }

    suspend fun acknowledgeAlert(alertId: String) = withContext(Dispatchers.IO) {
        alertDao.acknowledgeAlert(alertId)
    }

    // --- People ---
    val allPeople: Flow<List<Person>> = personDao.getAllPeople().map { list ->
        list.map { it.toDomain() }
    }

    val activePeople: Flow<List<Person>> = personDao.getActivePeople().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun addPerson(name: String, type: PersonType): String = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val entity = PersonEntity(
            personId = id,
            name = name.trim(),
            type = type.name,
            active = true,
            createdAt = System.currentTimeMillis()
        )
        personDao.insertPerson(entity)
        auditDao.insertAuditEvent(
            AuditEventEntity(
                auditId = UUID.randomUUID().toString(),
                eventType = "PERSON_CREATED",
                entityType = "PERSON",
                entityId = id,
                description = "Added ${type.name.lowercase()} $name"
            )
        )
        id
    }

    suspend fun updatePerson(person: Person) = withContext(Dispatchers.IO) {
        personDao.updatePerson(
            PersonEntity(
                personId = person.id,
                name = person.name.trim(),
                type = person.type.name,
                active = person.active,
                createdAt = person.createdAt
            )
        )
        auditDao.insertAuditEvent(
            AuditEventEntity(
                auditId = UUID.randomUUID().toString(),
                eventType = "PERSON_UPDATED",
                entityType = "PERSON",
                entityId = person.id,
                description = "Updated person ${person.name} (active=${person.active})"
            )
        )
    }

    // --- Tractors ---
    val allTractors: Flow<List<Tractor>> = tractorDao.getAllTractors().map { list ->
        list.map { it.toDomain() }
    }

    val activeTractors: Flow<List<Tractor>> = tractorDao.getActiveTractors().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun addTractor(
        name: String,
        brand: String,
        model: String,
        specification: String,
        registrationIdentifier: String
    ): String = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val entity = TractorEntity(
            tractorId = id,
            name = name.trim(),
            brand = brand.trim(),
            model = model.trim(),
            specification = specification.trim(),
            registrationIdentifier = registrationIdentifier.trim(),
            active = true,
            createdAt = System.currentTimeMillis()
        )
        tractorDao.insertTractor(entity)
        auditDao.insertAuditEvent(
            AuditEventEntity(
                auditId = UUID.randomUUID().toString(),
                eventType = "TRACTOR_CREATED",
                entityType = "TRACTOR",
                entityId = id,
                description = "Added tractor $name ($brand $model)"
            )
        )
        id
    }

    suspend fun updateTractor(tractor: Tractor) = withContext(Dispatchers.IO) {
        tractorDao.updateTractor(
            TractorEntity(
                tractorId = tractor.id,
                name = tractor.name.trim(),
                brand = tractor.brand.trim(),
                model = tractor.model.trim(),
                specification = tractor.specification.trim(),
                registrationIdentifier = tractor.registrationIdentifier.trim(),
                active = tractor.active,
                createdAt = tractor.createdAt
            )
        )
    }

    // --- Trips ---
    fun getTripsForDate(workDate: String): Flow<List<TripWithDetails>> {
        return tripDao.getTripsForDate(workDate).map { trips ->
            trips.map { tripEntity ->
                buildTripWithDetails(tripEntity)
            }
        }
    }

    suspend fun getNextTripNumber(workDate: String): Int = withContext(Dispatchers.IO) {
        val maxNum = tripDao.getMaxTripNumberForDate(workDate) ?: 0
        maxNum + 1
    }

    suspend fun createTripAtomic(
        workDate: String,
        tractorId: String,
        driverId: String?,
        ratePaise: Long,
        participants: List<MoneyEngine.ParticipantInput>,
        adjustments: Map<String, Long> = emptyMap()
    ): String = withContext(Dispatchers.IO) {
        require(participants.isNotEmpty()) { "At least one participant required" }
        require(tractorId.isNotBlank()) { "Tractor selection required" }

        val tripNumber = getNextTripNumber(workDate)
        val tripId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        // Deterministic whole-rupee calculation
        val calcResult = MoneyEngine.calculateTrip(
            ratePaise = ratePaise,
            participants = participants,
            explicitAdjustments = adjustments
        )

        val tripEntity = TripEntity(
            tripId = tripId,
            workDate = workDate,
            tripNumber = tripNumber,
            timestamp = now,
            tractorId = tractorId,
            driverId = driverId,
            ratePaise = ratePaise,
            status = TripStatus.ACTIVE.name,
            voidReason = null,
            createdAt = now,
            updatedAt = now
        )

        val participantEntities = calcResult.participantShares.map { p ->
            TripParticipantEntity(
                tripParticipantId = UUID.randomUUID().toString(),
                tripId = tripId,
                personId = p.personId,
                participationType = if (p.isHalfShare) ParticipationType.HALF.name else ParticipationType.FULL.name,
                calculatedSharePaise = p.calculatedSharePaise,
                adjustmentPaise = p.adjustmentPaise,
                finalSharePaise = p.finalSharePaise
            )
        }

        val calculationEntity = TripCalculationEntity(
            calculationId = UUID.randomUUID().toString(),
            tripId = tripId,
            ratePaise = ratePaise,
            participantCount = calcResult.participantCount,
            baseSharePaise = calcResult.baseSharePaise,
            distributedPaise = calcResult.distributedPaise,
            remainingPaise = calcResult.remainingPaise,
            calculationVersion = 1,
            calculatedAt = now
        )

        val adjustmentEntities = adjustments.map { (recipientId, amount) ->
            TripAdjustmentEntity(
                adjustmentId = UUID.randomUUID().toString(),
                tripId = tripId,
                recipientId = recipientId,
                amountPaise = amount,
                reason = "Explicit half-share / manual distribution",
                createdAt = now
            )
        }

        tripDao.insertTripWithParticipantsAndCalc(
            trip = tripEntity,
            participants = participantEntities,
            calc = calculationEntity,
            adjustments = adjustmentEntities
        )

        auditDao.insertAuditEvent(
            AuditEventEntity(
                auditId = UUID.randomUUID().toString(),
                eventType = "TRIP_CREATED",
                entityType = "TRIP",
                entityId = tripId,
                description = "Trip #$tripNumber on $workDate created (${calcResult.participantCount} loaders, distributed ${MoneyEngine.formatPaise(calcResult.distributedPaise)}, remaining ${MoneyEngine.formatPaise(calcResult.remainingPaise)})"
            )
        )

        tripId
    }

    suspend fun voidTrip(tripId: String, reason: String) = withContext(Dispatchers.IO) {
        val trip = tripDao.getTripById(tripId) ?: return@withContext
        val updated = trip.copy(
            status = TripStatus.VOIDED.name,
            voidReason = reason,
            updatedAt = System.currentTimeMillis()
        )
        tripDao.updateTrip(updated)
        auditDao.insertAuditEvent(
            AuditEventEntity(
                auditId = UUID.randomUUID().toString(),
                eventType = "TRIP_VOIDED",
                entityType = "TRIP",
                entityId = tripId,
                description = "Trip #${trip.tripNumber} on ${trip.workDate} voided: $reason"
            )
        )
    }

    suspend fun editTrip(
        tripId: String,
        tractorId: String,
        driverId: String?,
        ratePaise: Long,
        participants: List<MoneyEngine.ParticipantInput>,
        adjustments: Map<String, Long> = emptyMap()
    ) = withContext(Dispatchers.IO) {
        val existingTrip = tripDao.getTripById(tripId) ?: return@withContext
        val now = System.currentTimeMillis()

        val calcResult = MoneyEngine.calculateTrip(ratePaise, participants, adjustments)

        val updatedTrip = existingTrip.copy(
            tractorId = tractorId,
            driverId = driverId,
            ratePaise = ratePaise,
            updatedAt = now
        )
        tripDao.updateTrip(updatedTrip)

        // Replace participants & calculation
        tripDao.deleteParticipantsForTrip(tripId)
        val participantEntities = calcResult.participantShares.map { p ->
            TripParticipantEntity(
                tripParticipantId = UUID.randomUUID().toString(),
                tripId = tripId,
                personId = p.personId,
                participationType = if (p.isHalfShare) ParticipationType.HALF.name else ParticipationType.FULL.name,
                calculatedSharePaise = p.calculatedSharePaise,
                adjustmentPaise = p.adjustmentPaise,
                finalSharePaise = p.finalSharePaise
            )
        }
        tripDao.insertParticipants(participantEntities)

        val calculationEntity = TripCalculationEntity(
            calculationId = UUID.randomUUID().toString(),
            tripId = tripId,
            ratePaise = ratePaise,
            participantCount = calcResult.participantCount,
            baseSharePaise = calcResult.baseSharePaise,
            distributedPaise = calcResult.distributedPaise,
            remainingPaise = calcResult.remainingPaise,
            calculationVersion = 1,
            calculatedAt = now
        )
        tripDao.insertCalculation(calculationEntity)

        auditDao.insertAuditEvent(
            AuditEventEntity(
                auditId = UUID.randomUUID().toString(),
                eventType = "TRIP_EDITED",
                entityType = "TRIP",
                entityId = tripId,
                description = "Trip #${existingTrip.tripNumber} edited with ${participants.size} participants"
            )
        )
    }

    private suspend fun buildTripWithDetails(tripEntity: TripEntity): TripWithDetails {
        val tractor = tractorDao.getTractorById(tripEntity.tractorId)?.toDomain()
        val pEntities = tripDao.getParticipantsForTrip(tripEntity.tripId)
        val calc = tripDao.getCalculationForTrip(tripEntity.tripId)?.toDomain()
        val adjustments = tripDao.getAdjustmentsForTrip(tripEntity.tripId).map { it.toDomain() }

        val participantsWithPerson = pEntities.map { pe ->
            val person = personDao.getPersonById(pe.personId)?.toDomain() ?: Person(
                id = pe.personId,
                name = "Unknown",
                type = PersonType.LABOURER
            )
            ParticipantWithPerson(pe.toDomain(), person)
        }

        return TripWithDetails(
            trip = tripEntity.toDomain(),
            tractor = tractor,
            participants = participantsWithPerson,
            calculation = calc,
            adjustments = adjustments
        )
    }

    // --- Daily Summary & Reconciliation ---
    suspend fun getDailySummary(workDate: String): DailySummary = withContext(Dispatchers.IO) {
        val allTrips = tripDao.getTripsForDate(workDate).first()
        val activeTrips = allTrips.filter { it.status == TripStatus.ACTIVE.name }
        val voidedTrips = allTrips.filter { it.status == TripStatus.VOIDED.name }

        var grossPaise = 0L
        var distributedPaise = 0L
        var remainingPaise = 0L
        val tractorCounts = mutableMapOf<String, Int>()

        for (t in activeTrips) {
            grossPaise += t.ratePaise
            val calc = tripDao.getCalculationForTrip(t.tripId)
            if (calc != null) {
                distributedPaise += calc.distributedPaise
                remainingPaise += calc.remainingPaise
            } else {
                remainingPaise += t.ratePaise
            }
            tractorCounts[t.tractorId] = (tractorCounts[t.tractorId] ?: 0) + 1
        }

        val closure = closureDao.getClosureForDateSync(workDate)
        val status = when {
            allTrips.isEmpty() -> ClosureStatus.NO_WORK
            closure != null && closure.status == ClosureStatus.CLOSED.name -> ClosureStatus.CLOSED
            closure != null && closure.status == ClosureStatus.REOPENED.name -> ClosureStatus.REOPENED
            else -> ClosureStatus.OPEN
        }

        val lastChanged = allTrips.maxOfOrNull { it.updatedAt } ?: 0L
        val lastCalculated = closure?.calculatedAt ?: System.currentTimeMillis()

        DailySummary(
            workDate = workDate,
            status = status,
            totalTrips = allTrips.size,
            activeTrips = activeTrips.size,
            voidedTrips = voidedTrips.size,
            grossPaise = grossPaise,
            distributedPaise = distributedPaise,
            remainingPaise = remainingPaise,
            tractorTripCounts = tractorCounts,
            lastCalculated = lastCalculated,
            lastChanged = lastChanged
        )
    }

    suspend fun closeDay(workDate: String) = withContext(Dispatchers.IO) {
        val summary = getDailySummary(workDate)
        val now = System.currentTimeMillis()
        val closure = DailyClosureEntity(
            closureId = UUID.randomUUID().toString(),
            workDate = workDate,
            grossPaise = summary.grossPaise,
            distributedPaise = summary.distributedPaise,
            remainingPaise = summary.remainingPaise,
            status = ClosureStatus.CLOSED.name,
            calculatedAt = now,
            closedAt = now,
            reopenedAt = null
        )
        closureDao.insertOrUpdateClosure(closure)
        auditDao.insertAuditEvent(
            AuditEventEntity(
                auditId = UUID.randomUUID().toString(),
                eventType = "DAY_CLOSED",
                entityType = "WORK_DAY",
                entityId = workDate,
                description = "Closed work day $workDate with ${summary.activeTrips} active trips (Gross: ${MoneyEngine.formatPaise(summary.grossPaise)})"
            )
        )
    }

    suspend fun reopenDay(workDate: String) = withContext(Dispatchers.IO) {
        val existing = closureDao.getClosureForDateSync(workDate)
        val now = System.currentTimeMillis()
        val updated = existing?.copy(
            status = ClosureStatus.REOPENED.name,
            reopenedAt = now
        ) ?: DailyClosureEntity(
            closureId = UUID.randomUUID().toString(),
            workDate = workDate,
            grossPaise = 0L,
            distributedPaise = 0L,
            remainingPaise = 0L,
            status = ClosureStatus.REOPENED.name,
            calculatedAt = now,
            closedAt = null,
            reopenedAt = now
        )
        closureDao.insertOrUpdateClosure(updated)
        auditDao.insertAuditEvent(
            AuditEventEntity(
                auditId = UUID.randomUUID().toString(),
                eventType = "DAY_REOPENED",
                entityType = "WORK_DAY",
                entityId = workDate,
                description = "Reopened work day $workDate for correction"
            )
        )
    }

    // --- Attendance ---
    fun getAttendanceForDate(workDate: String): Flow<List<Attendance>> {
        return attendanceDao.getAttendanceForDate(workDate).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun setAttendance(personId: String, workDate: String, status: AttendanceStatus, notes: String? = null) = withContext(Dispatchers.IO) {
        val entity = AttendanceEntity(
            attendanceId = UUID.randomUUID().toString(),
            personId = personId,
            workDate = workDate,
            status = status.name,
            reason = null,
            notes = notes,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        attendanceDao.insertAttendance(entity)
        auditDao.insertAuditEvent(
            AuditEventEntity(
                auditId = UUID.randomUUID().toString(),
                eventType = "ATTENDANCE_UPDATED",
                entityType = "ATTENDANCE",
                entityId = personId,
                description = "Attendance for $personId on $workDate set to ${status.name}"
            )
        )
    }

    // --- Ledgers & Analytics ---
    suspend fun getPersonSummary(personId: String): PersonSummary = withContext(Dispatchers.IO) {
        val person = personDao.getPersonById(personId)?.toDomain() ?: Person(personId, "Unknown", PersonType.LABOURER)
        val participants = tripDao.getParticipantsForPerson(personId)
        
        var totalEarned = 0L
        val activeTripsForPerson = mutableListOf<TripEntity>()

        for (p in participants) {
            val trip = tripDao.getTripById(p.tripId)
            if (trip != null && trip.status == TripStatus.ACTIVE.name) {
                totalEarned += p.finalSharePaise
                activeTripsForPerson.add(trip)
            }
        }

        val workingDates = activeTripsForPerson.map { it.workDate }.distinct()
        val attendances = attendanceDao.getAttendanceForPerson(personId)

        val presentDays = attendances.count { it.status == AttendanceStatus.PRESENT.name }
        val absentDays = attendances.count { it.status == AttendanceStatus.ABSENT.name }
        val unmarkedDays = attendances.count { it.status == AttendanceStatus.UNMARKED.name }

        PersonSummary(
            person = person,
            totalTrips = activeTripsForPerson.size,
            totalEarnedPaise = totalEarned,
            workingDays = workingDates.size,
            presentDays = presentDays,
            absentDays = absentDays,
            unmarkedDays = unmarkedDays
        )
    }

    suspend fun getParticipationMatrix(workDate: String): List<ParticipationMatrixRow> = withContext(Dispatchers.IO) {
        val allTrips = tripDao.getTripsForDate(workDate).first().filter { it.status == TripStatus.ACTIVE.name }
        val people = personDao.getActivePeople().first().map { it.toDomain() }

        val rows = mutableListOf<ParticipationMatrixRow>()
        for (person in people) {
            val tractorCounts = mutableMapOf<String, Int>()
            var personTrips = 0
            for (trip in allTrips) {
                val participants = tripDao.getParticipantsForTrip(trip.tripId)
                if (participants.any { it.personId == person.id }) {
                    tractorCounts[trip.tractorId] = (tractorCounts[trip.tractorId] ?: 0) + 1
                    personTrips++
                }
            }
            if (personTrips > 0) {
                rows.add(
                    ParticipationMatrixRow(
                        person = person,
                        tractorCounts = tractorCounts,
                        totalTrips = personTrips
                    )
                )
            }
        }
        rows.sortedByDescending { it.totalTrips }
    }

    suspend fun getWeeklyLeaderboard(referenceDate: LocalDate): List<LeaderboardEntry> = withContext(Dispatchers.IO) {
        val monday = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val sunday = referenceDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        val people = personDao.getActivePeople().first().map { it.toDomain() }
        val entries = mutableListOf<LeaderboardEntry>()

        for (person in people) {
            val pRecords = tripDao.getParticipantsForPerson(person.id)
            var qualifyingTrips = 0
            var fullTrips = 0
            var halfTrips = 0
            var earned = 0L

            for (p in pRecords) {
                val trip = tripDao.getTripById(p.tripId)
                if (trip != null && trip.status == TripStatus.ACTIVE.name) {
                    val tripDate = LocalDate.parse(trip.workDate)
                    if (!tripDate.isBefore(monday) && !tripDate.isAfter(sunday)) {
                        qualifyingTrips++
                        if (p.participationType == ParticipationType.HALF.name) halfTrips++ else fullTrips++
                        earned += p.finalSharePaise
                    }
                }
            }

            if (qualifyingTrips > 0) {
                entries.add(
                    LeaderboardEntry(
                        rank = 0,
                        person = person,
                        qualifyingTrips = qualifyingTrips,
                        fullTrips = fullTrips,
                        halfTrips = halfTrips,
                        totalEarnedPaise = earned
                    )
                )
            }
        }

        // Deterministic competition ranking: Ties get same rank (1st A, 1st B, 3rd C)
        entries.sortByDescending { it.qualifyingTrips }
        val ranked = mutableListOf<LeaderboardEntry>()
        var currentRank = 1
        for (i in entries.indices) {
            if (i > 0 && entries[i].qualifyingTrips < entries[i - 1].qualifyingTrips) {
                currentRank = i + 1
            }
            ranked.add(entries[i].copy(rank = currentRank))
        }
        ranked
    }

    suspend fun getMonthlyLeaderboard(yearMonth: java.time.YearMonth): List<LeaderboardEntry> = withContext(Dispatchers.IO) {
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()

        val people = personDao.getActivePeople().first().map { it.toDomain() }
        val entries = mutableListOf<LeaderboardEntry>()

        for (person in people) {
            val pRecords = tripDao.getParticipantsForPerson(person.id)
            var qualifyingTrips = 0
            var fullTrips = 0
            var halfTrips = 0
            var earned = 0L

            for (p in pRecords) {
                val trip = tripDao.getTripById(p.tripId)
                if (trip != null && trip.status == TripStatus.ACTIVE.name) {
                    val tripDate = LocalDate.parse(trip.workDate)
                    if (!tripDate.isBefore(startDate) && !tripDate.isAfter(endDate)) {
                        qualifyingTrips++
                        if (p.participationType == ParticipationType.HALF.name) halfTrips++ else fullTrips++
                        earned += p.finalSharePaise
                    }
                }
            }

            if (qualifyingTrips > 0) {
                entries.add(
                    LeaderboardEntry(
                        rank = 0,
                        person = person,
                        qualifyingTrips = qualifyingTrips,
                        fullTrips = fullTrips,
                        halfTrips = halfTrips,
                        totalEarnedPaise = earned
                    )
                )
            }
        }

        entries.sortByDescending { it.qualifyingTrips }
        // Per PRD: Top 3 only. Never fabricate if fewer qualify.
        val topN = entries.take(3)
        val ranked = mutableListOf<LeaderboardEntry>()
        var currentRank = 1
        for (i in topN.indices) {
            if (i > 0 && topN[i].qualifyingTrips < topN[i - 1].qualifyingTrips) {
                currentRank = i + 1
            }
            ranked.add(topN[i].copy(rank = currentRank))
        }
        ranked
    }

    // --- Exceptions ---
    suspend fun getExceptions(workDate: String): List<ExceptionItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<ExceptionItem>()
        val trips = tripDao.getTripsForDate(workDate).first()
        val activeTrips = trips.filter { it.status == TripStatus.ACTIVE.name }
        val attendance = attendanceDao.getAttendanceForDateSync(workDate)

        // 1. Attendance conflicts: marked absent but loaded
        val absentPeopleIds = attendance.filter { it.status == AttendanceStatus.ABSENT.name }.map { it.personId }.toSet()
        for (t in activeTrips) {
            val participants = tripDao.getParticipantsForTrip(t.tripId)
            for (p in participants) {
                if (p.personId in absentPeopleIds) {
                    val person = personDao.getPersonById(p.personId)
                    list.add(
                        ExceptionItem(
                            id = "conflict_${p.personId}_${t.tripId}",
                            title = "Attendance Conflict",
                            description = "${person?.name ?: p.personId} is marked absent but loaded Trip #${t.tripNumber}",
                            type = "ATTENDANCE_CONFLICT",
                            severity = "WARNING"
                        )
                    )
                }
            }
        }

        // 2. Remaining money on active trips
        for (t in activeTrips) {
            val calc = tripDao.getCalculationForTrip(t.tripId)
            if (calc != null && calc.remainingPaise > 0L) {
                list.add(
                    ExceptionItem(
                        id = "remainder_${t.tripId}",
                        title = "Undistributed Remainder",
                        description = "Trip #${t.tripNumber} has ${MoneyEngine.formatPaise(calc.remainingPaise)} remaining undistributed",
                        type = "REMAINING_MONEY",
                        severity = "INFO"
                    )
                )
            }
        }

        list
    }

    // Audit
    val allAuditEvents: Flow<List<AuditEvent>> = auditDao.getAllAuditEvents().map { list ->
        list.map { it.toDomain() }
    }

    // --- Backup & Restore ---
    suspend fun createBackupJson(): String = withContext(Dispatchers.IO) {
        val root = org.json.JSONObject()
        root.put("schemaVersion", 1)
        root.put("appName", "SAND WORKS")
        root.put("exportedAt", System.currentTimeMillis())

        // People
        val peopleArray = org.json.JSONArray()
        for (p in personDao.getAllPeopleSync()) {
            val obj = org.json.JSONObject()
            obj.put("personId", p.personId)
            obj.put("name", p.name)
            obj.put("type", p.type)
            obj.put("active", p.active)
            obj.put("createdAt", p.createdAt)
            peopleArray.put(obj)
        }
        root.put("people", peopleArray)

        // Tractors
        val tractorArray = org.json.JSONArray()
        for (t in tractorDao.getAllTractorsSync()) {
            val obj = org.json.JSONObject()
            obj.put("tractorId", t.tractorId)
            obj.put("name", t.name)
            obj.put("brand", t.brand)
            obj.put("model", t.model)
            obj.put("specification", t.specification)
            obj.put("registrationIdentifier", t.registrationIdentifier)
            obj.put("active", t.active)
            obj.put("createdAt", t.createdAt)
            tractorArray.put(obj)
        }
        root.put("tractors", tractorArray)

        // Trips
        val tripsArray = org.json.JSONArray()
        for (tr in tripDao.getAllTripsSync()) {
            val obj = org.json.JSONObject()
            obj.put("tripId", tr.tripId)
            obj.put("workDate", tr.workDate)
            obj.put("tripNumber", tr.tripNumber)
            obj.put("timestamp", tr.timestamp)
            obj.put("tractorId", tr.tractorId)
            obj.put("driverId", tr.driverId)
            obj.put("ratePaise", tr.ratePaise)
            obj.put("status", tr.status)
            obj.put("voidReason", tr.voidReason)
            obj.put("createdAt", tr.createdAt)
            obj.put("updatedAt", tr.updatedAt)
            tripsArray.put(obj)
        }
        root.put("trips", tripsArray)

        // Participants
        val partArray = org.json.JSONArray()
        for (pt in tripDao.getAllParticipantsSync()) {
            val obj = org.json.JSONObject()
            obj.put("tripParticipantId", pt.tripParticipantId)
            obj.put("tripId", pt.tripId)
            obj.put("personId", pt.personId)
            obj.put("participationType", pt.participationType)
            obj.put("calculatedSharePaise", pt.calculatedSharePaise)
            obj.put("adjustmentPaise", pt.adjustmentPaise)
            obj.put("finalSharePaise", pt.finalSharePaise)
            partArray.put(obj)
        }
        root.put("participants", partArray)

        // Calculations
        val calcArray = org.json.JSONArray()
        for (c in tripDao.getAllCalculationsSync()) {
            val obj = org.json.JSONObject()
            obj.put("calculationId", c.calculationId)
            obj.put("tripId", c.tripId)
            obj.put("ratePaise", c.ratePaise)
            obj.put("participantCount", c.participantCount)
            obj.put("baseSharePaise", c.baseSharePaise)
            obj.put("distributedPaise", c.distributedPaise)
            obj.put("remainingPaise", c.remainingPaise)
            obj.put("calculationVersion", c.calculationVersion)
            obj.put("calculatedAt", c.calculatedAt)
            calcArray.put(obj)
        }
        root.put("calculations", calcArray)

        // Adjustments
        val adjArray = org.json.JSONArray()
        for (a in tripDao.getAllAdjustmentsSync()) {
            val obj = org.json.JSONObject()
            obj.put("adjustmentId", a.adjustmentId)
            obj.put("tripId", a.tripId)
            obj.put("recipientId", a.recipientId)
            obj.put("amountPaise", a.amountPaise)
            obj.put("reason", a.reason)
            obj.put("createdAt", a.createdAt)
            adjArray.put(obj)
        }
        root.put("adjustments", adjArray)

        // Attendance
        val attArray = org.json.JSONArray()
        for (at in attendanceDao.getAllAttendanceSync()) {
            val obj = org.json.JSONObject()
            obj.put("attendanceId", at.attendanceId)
            obj.put("personId", at.personId)
            obj.put("workDate", at.workDate)
            obj.put("status", at.status)
            obj.put("reason", at.reason)
            obj.put("notes", at.notes)
            obj.put("createdAt", at.createdAt)
            attArray.put(obj)
        }
        root.put("attendance", attArray)

        // Closures
        val closeArray = org.json.JSONArray()
        for (cl in closureDao.getAllClosuresSync()) {
            val obj = org.json.JSONObject()
            obj.put("closureId", cl.closureId)
            obj.put("workDate", cl.workDate)
            obj.put("closedAt", cl.closedAt)
            obj.put("closedBy", cl.closedBy)
            obj.put("status", cl.status)
            obj.put("totalTrips", cl.totalTrips)
            obj.put("activeTrips", cl.activeTrips)
            obj.put("voidedTrips", cl.voidedTrips)
            obj.put("grossPaise", cl.grossPaise)
            obj.put("distributedPaise", cl.distributedPaise)
            obj.put("remainingPaise", cl.remainingPaise)
            obj.put("notes", cl.notes)
            closeArray.put(obj)
        }
        root.put("closures", closeArray)

        // Audit Events
        val auditArray = org.json.JSONArray()
        for (ae in auditDao.getAllAuditEventsSync()) {
            val obj = org.json.JSONObject()
            obj.put("auditId", ae.auditId)
            obj.put("timestamp", ae.timestamp)
            obj.put("eventType", ae.eventType)
            obj.put("entityType", ae.entityType)
            obj.put("entityId", ae.entityId)
            obj.put("description", ae.description)
            obj.put("oldValue", ae.oldValue)
            obj.put("newValue", ae.newValue)
            auditArray.put(obj)
        }
        root.put("auditEvents", auditArray)

        root.toString(2)
    }

    suspend fun restoreBackupJson(jsonString: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val root = org.json.JSONObject(jsonString)
            val version = root.optInt("schemaVersion", 0)
            if (version != 1) {
                return@withContext Result.failure(IllegalArgumentException("Unsupported backup version: $version"))
            }

            // Parse People
            val peopleList = mutableListOf<PersonEntity>()
            val peopleArray = root.optJSONArray("people") ?: org.json.JSONArray()
            for (i in 0 until peopleArray.length()) {
                val o = peopleArray.getJSONObject(i)
                peopleList.add(
                    PersonEntity(
                        personId = o.getString("personId"),
                        name = o.getString("name"),
                        type = o.getString("type"),
                        active = o.optBoolean("active", true),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            // Parse Tractors
            val tractorList = mutableListOf<TractorEntity>()
            val tractorArray = root.optJSONArray("tractors") ?: org.json.JSONArray()
            for (i in 0 until tractorArray.length()) {
                val o = tractorArray.getJSONObject(i)
                tractorList.add(
                    TractorEntity(
                        tractorId = o.getString("tractorId"),
                        name = o.getString("name"),
                        brand = o.optString("brand", ""),
                        model = o.optString("model", ""),
                        specification = o.optString("specification", ""),
                        registrationIdentifier = o.optString("registrationIdentifier", ""),
                        active = o.optBoolean("active", true),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            // Parse Trips
            val tripList = mutableListOf<TripEntity>()
            val tripsArray = root.optJSONArray("trips") ?: org.json.JSONArray()
            for (i in 0 until tripsArray.length()) {
                val o = tripsArray.getJSONObject(i)
                tripList.add(
                    TripEntity(
                        tripId = o.getString("tripId"),
                        workDate = o.getString("workDate"),
                        tripNumber = o.getInt("tripNumber"),
                        timestamp = o.getLong("timestamp"),
                        tractorId = o.getString("tractorId"),
                        driverId = o.optString("driverId", null),
                        ratePaise = o.getLong("ratePaise"),
                        status = o.getString("status"),
                        voidReason = o.optString("voidReason", null),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            // Parse Participants
            val partList = mutableListOf<TripParticipantEntity>()
            val partArray = root.optJSONArray("participants") ?: org.json.JSONArray()
            for (i in 0 until partArray.length()) {
                val o = partArray.getJSONObject(i)
                partList.add(
                    TripParticipantEntity(
                        tripParticipantId = o.getString("tripParticipantId"),
                        tripId = o.getString("tripId"),
                        personId = o.getString("personId"),
                        participationType = o.getString("participationType"),
                        calculatedSharePaise = o.getLong("calculatedSharePaise"),
                        adjustmentPaise = o.optLong("adjustmentPaise", 0L),
                        finalSharePaise = o.getLong("finalSharePaise")
                    )
                )
            }

            // Parse Calculations
            val calcList = mutableListOf<TripCalculationEntity>()
            val calcArray = root.optJSONArray("calculations") ?: org.json.JSONArray()
            for (i in 0 until calcArray.length()) {
                val o = calcArray.getJSONObject(i)
                calcList.add(
                    TripCalculationEntity(
                        calculationId = o.getString("calculationId"),
                        tripId = o.getString("tripId"),
                        ratePaise = o.getLong("ratePaise"),
                        participantCount = o.getInt("participantCount"),
                        baseSharePaise = o.getLong("baseSharePaise"),
                        distributedPaise = o.getLong("distributedPaise"),
                        remainingPaise = o.getLong("remainingPaise"),
                        calculationVersion = o.optInt("calculationVersion", 1),
                        calculatedAt = o.optLong("calculatedAt", System.currentTimeMillis())
                    )
                )
            }

            // Parse Adjustments
            val adjList = mutableListOf<TripAdjustmentEntity>()
            val adjArray = root.optJSONArray("adjustments") ?: org.json.JSONArray()
            for (i in 0 until adjArray.length()) {
                val o = adjArray.getJSONObject(i)
                adjList.add(
                    TripAdjustmentEntity(
                        adjustmentId = o.getString("adjustmentId"),
                        tripId = o.getString("tripId"),
                        recipientId = o.getString("recipientId"),
                        amountPaise = o.getLong("amountPaise"),
                        reason = o.optString("reason", ""),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            // Parse Attendance
            val attList = mutableListOf<AttendanceEntity>()
            val attArray = root.optJSONArray("attendance") ?: org.json.JSONArray()
            for (i in 0 until attArray.length()) {
                val o = attArray.getJSONObject(i)
                attList.add(
                    AttendanceEntity(
                        attendanceId = o.getString("attendanceId"),
                        personId = o.getString("personId"),
                        workDate = o.getString("workDate"),
                        status = o.getString("status"),
                        reason = o.optString("reason", null),
                        notes = o.optString("notes", null),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            // Parse Closures
            val closeList = mutableListOf<DailyClosureEntity>()
            val closeArray = root.optJSONArray("closures") ?: org.json.JSONArray()
            for (i in 0 until closeArray.length()) {
                val o = closeArray.getJSONObject(i)
                closeList.add(
                    DailyClosureEntity(
                        closureId = o.getString("closureId"),
                        workDate = o.getString("workDate"),
                        closedAt = o.getLong("closedAt"),
                        closedBy = o.optString("closedBy", null),
                        status = o.getString("status"),
                        totalTrips = o.getInt("totalTrips"),
                        activeTrips = o.getInt("activeTrips"),
                        voidedTrips = o.getInt("voidedTrips"),
                        grossPaise = o.getLong("grossPaise"),
                        distributedPaise = o.getLong("distributedPaise"),
                        remainingPaise = o.getLong("remainingPaise"),
                        notes = o.optString("notes", null)
                    )
                )
            }

            // Perform Database Swap
            personDao.deleteAllPeople()
            tractorDao.deleteAllTractors()
            tripDao.deleteAllTrips()
            tripDao.deleteAllParticipants()
            tripDao.deleteAllCalculations()
            tripDao.deleteAllAdjustments()
            attendanceDao.deleteAllAttendance()
            closureDao.deleteAllClosures()

            if (peopleList.isNotEmpty()) personDao.insertAllPeople(peopleList)
            if (tractorList.isNotEmpty()) tractorDao.insertAllTractors(tractorList)
            if (tripList.isNotEmpty()) tripDao.insertAllTrips(tripList)
            if (partList.isNotEmpty()) tripDao.insertParticipants(partList)
            if (calcList.isNotEmpty()) tripDao.insertAllCalculations(calcList)
            if (adjList.isNotEmpty()) tripDao.insertAdjustments(adjList)
            if (attList.isNotEmpty()) attendanceDao.insertAllAttendance(attList)
            if (closeList.isNotEmpty()) closureDao.insertAllClosures(closeList)

            auditDao.insertAuditEvent(
                AuditEventEntity(
                    auditId = UUID.randomUUID().toString(),
                    eventType = "RESTORE_COMPLETED",
                    entityType = "DATABASE",
                    entityId = "ALL",
                    description = "Restored ${tripList.size} trips, ${peopleList.size} people, ${tractorList.size} tractors"
                )
            )

            Result.success("Successfully restored ${tripList.size} trips, ${peopleList.size} people, and ${tractorList.size} tractors.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        personDao.deleteAllPeople()
        tractorDao.deleteAllTractors()
        tripDao.deleteAllTrips()
        tripDao.deleteAllParticipants()
        tripDao.deleteAllCalculations()
        tripDao.deleteAllAdjustments()
        attendanceDao.deleteAllAttendance()
        closureDao.deleteAllClosures()
        auditDao.deleteAllAuditEvents()

        auditDao.insertAuditEvent(
            AuditEventEntity(
                auditId = UUID.randomUUID().toString(),
                eventType = "DATABASE_CLEARED",
                entityType = "DATABASE",
                entityId = "ALL",
                description = "All user data was wiped via Factory Reset"
            )
        )
    }

    suspend fun runDataHealthCheck(): DataHealthReport = withContext(Dispatchers.IO) {
        val people = personDao.getAllPeopleSync()
        val tractors = tractorDao.getAllTractorsSync()
        val trips = tripDao.getAllTripsSync()
        val participants = tripDao.getAllParticipantsSync()
        val calculations = tripDao.getAllCalculationsSync()
        val attendance = attendanceDao.getAllAttendanceSync()
        val closures = closureDao.getAllClosuresSync()
        val auditEvents = auditDao.getAllAuditEventsSync()

        val activeTrips = trips.filter { it.status == "ACTIVE" }
        val voidedTrips = trips.filter { it.status == "VOIDED" }

        val calcMap = calculations.associateBy { it.tripId }
        val partGrouped = participants.groupBy { it.tripId }

        val errors = mutableListOf<String>()

        // Mathematical Invariant Checks (PRD3 §126)
        for (trip in activeTrips) {
            val calc = calcMap[trip.tripId]
            val tripParts = partGrouped[trip.tripId] ?: emptyList()

            if (calc == null) {
                errors.add("Trip #${trip.tripNumber} (${trip.workDate}): Missing calculation record")
                continue
            }

            // Invariant 1: rate = distributed + remaining
            if (trip.ratePaise != (calc.distributedPaise + calc.remainingPaise)) {
                errors.add("Trip #${trip.tripNumber} (${trip.workDate}): Rate mismatch! rate=${trip.ratePaise}, distributed+remaining=${calc.distributedPaise + calc.remainingPaise}")
            }

            // Invariant 2: sum(shares) = distributed
            val sumShares = tripParts.sumOf { it.finalSharePaise }
            if (sumShares != calc.distributedPaise) {
                errors.add("Trip #${trip.tripNumber} (${trip.workDate}): Participant shares sum mismatch! shares=$sumShares, distributed=${calc.distributedPaise}")
            }

            // Invariant 3: non-negative paise
            if (trip.ratePaise < 0 || calc.distributedPaise < 0 || calc.remainingPaise < 0) {
                errors.add("Trip #${trip.tripNumber} (${trip.workDate}): Negative money detected in calculations")
            }
            for (p in tripParts) {
                if (p.finalSharePaise < 0) {
                    errors.add("Trip #${trip.tripNumber} (${trip.workDate}): Negative participant share detected")
                }
            }
        }

        // Monotonic trip numbering check per date
        val tripsByDate = trips.groupBy { it.workDate }
        for ((date, dateTrips) in tripsByDate) {
            val numbers = dateTrips.map { it.tripNumber }.sorted()
            val expected = (1..numbers.size).toList()
            if (numbers != expected) {
                // Not necessarily fatal if user voided or deleted, but check for duplicates
                val duplicateNums = numbers.groupBy { it }.filter { it.value.size > 1 }.keys
                if (duplicateNums.isNotEmpty()) {
                    errors.add("Date $date has duplicate trip numbers: $duplicateNums")
                }
            }
        }

        // Record diagnostic audit event if errors found
        if (errors.isNotEmpty()) {
            auditDao.insertAuditEvent(
                AuditEventEntity(
                    auditId = UUID.randomUUID().toString(),
                    eventType = "INTEGRITY_CHECK_WARNING",
                    entityType = "HEALTH_CHECK",
                    entityId = "ALL",
                    description = "Integrity check found ${errors.size} discrepancies"
                )
            )
        }

        DataHealthReport(
            dbStatus = "Online (Local Room SQLite sand_works.db)",
            totalTrips = trips.size,
            activeTrips = activeTrips.size,
            voidedTrips = voidedTrips.size,
            totalPeople = people.size,
            activePeople = people.count { it.active },
            driversCount = people.count { it.type == "DRIVER" },
            labourersCount = people.count { it.type == "LABOURER" },
            totalTractors = tractors.size,
            activeTractors = tractors.count { it.active },
            attendanceRecordsCount = attendance.size,
            dailyClosuresCount = closures.size,
            auditEventsCount = auditEvents.size,
            calculationErrors = errors,
            isHealthy = errors.isEmpty()
        )
    }
}

// Entity to domain mapping helpers
private fun PersonEntity.toDomain() = Person(personId, name, PersonType.valueOf(type), active, createdAt)
private fun TractorEntity.toDomain() = Tractor(tractorId, name, brand, model, specification, registrationIdentifier, active, createdAt)
private fun TripEntity.toDomain() = Trip(tripId, workDate, tripNumber, timestamp, tractorId, driverId, ratePaise, TripStatus.valueOf(status), voidReason, createdAt, updatedAt)
private fun TripParticipantEntity.toDomain() = TripParticipant(tripParticipantId, tripId, personId, ParticipationType.valueOf(participationType), calculatedSharePaise, adjustmentPaise, finalSharePaise)
private fun TripCalculationEntity.toDomain() = TripCalculation(calculationId, tripId, ratePaise, participantCount, baseSharePaise, distributedPaise, remainingPaise, calculationVersion, calculatedAt)
private fun TripAdjustmentEntity.toDomain() = TripAdjustment(adjustmentId, tripId, recipientId, amountPaise, reason, createdAt)
private fun AttendanceEntity.toDomain() = Attendance(attendanceId, personId, workDate, AttendanceStatus.valueOf(status), reason, notes, createdAt)
private fun AuditEventEntity.toDomain() = AuditEvent(auditId, timestamp, eventType, entityType, entityId, description, oldValue, newValue)
