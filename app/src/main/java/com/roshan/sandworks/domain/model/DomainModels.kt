package com.roshan.sandworks.domain.model

enum class PersonType {
    LABOURER,
    DRIVER
}

enum class UserRole {
    OWNER,
    DRIVER,
    LABOURER
}

enum class ParticipationType {
    FULL,
    HALF
}

enum class TripStatus {
    ACTIVE,
    VOIDED
}

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    UNMARKED
}

enum class DerivedAttendance {
    WORKING,              // Present + participated in >= 1 trip
    PRESENT_NO_LOADING,   // Present + 0 trips
    ABSENT,               // Marked absent
    UNRECORDED            // Not marked
}

enum class ClosureStatus {
    NO_WORK,
    OPEN,
    CALCULATED,
    CHANGES_PENDING,
    READY_TO_CLOSE,
    CLOSED,
    REOPENED
}

data class Person(
    val id: String,
    val name: String,
    val type: PersonType,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class Tractor(
    val id: String,
    val name: String,
    val brand: String = "",
    val model: String = "",
    val specification: String = "",
    val registrationIdentifier: String = "",
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class Trip(
    val id: String,
    val workDate: String, // "YYYY-MM-DD"
    val tripNumber: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val tractorId: String,
    val driverId: String? = null, // assigned tractor driver (for audit/info)
    val ratePaise: Long = 20_000L,
    val status: TripStatus = TripStatus.ACTIVE,
    val voidReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class TripParticipant(
    val id: String,
    val tripId: String,
    val personId: String,
    val participationType: ParticipationType = ParticipationType.FULL,
    val calculatedSharePaise: Long,
    val adjustmentPaise: Long = 0L,
    val finalSharePaise: Long
)

data class TripCalculation(
    val id: String,
    val tripId: String,
    val ratePaise: Long,
    val participantCount: Int,
    val baseSharePaise: Long,
    val distributedPaise: Long,
    val remainingPaise: Long,
    val calculationVersion: Int = 1,
    val calculatedAt: Long = System.currentTimeMillis()
)

data class TripAdjustment(
    val id: String,
    val tripId: String,
    val recipientId: String,
    val amountPaise: Long,
    val reason: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class Attendance(
    val id: String,
    val personId: String,
    val workDate: String,
    val status: AttendanceStatus,
    val reason: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class DailyClosure(
    val id: String,
    val workDate: String,
    val grossPaise: Long,
    val distributedPaise: Long,
    val remainingPaise: Long,
    val status: ClosureStatus,
    val calculatedAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null,
    val reopenedAt: Long? = null
)

data class AuditEvent(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String,
    val entityType: String,
    val entityId: String,
    val description: String,
    val oldValue: String? = null,
    val newValue: String? = null
)

data class TripWithDetails(
    val trip: Trip,
    val tractor: Tractor?,
    val participants: List<ParticipantWithPerson>,
    val calculation: TripCalculation?,
    val adjustments: List<TripAdjustment> = emptyList()
)

data class ParticipantWithPerson(
    val participant: TripParticipant,
    val person: Person
)

data class DailySummary(
    val workDate: String,
    val status: ClosureStatus,
    val totalTrips: Int,
    val activeTrips: Int,
    val voidedTrips: Int,
    val grossPaise: Long,
    val distributedPaise: Long,
    val remainingPaise: Long,
    val tractorTripCounts: Map<String, Int>,
    val lastCalculated: Long,
    val lastChanged: Long
)

data class PersonSummary(
    val person: Person,
    val totalTrips: Int,
    val totalEarnedPaise: Long,
    val workingDays: Int,
    val presentDays: Int,
    val absentDays: Int,
    val unmarkedDays: Int
) {
    val attendanceDays: Int get() = presentDays
}

data class LeaderboardEntry(
    val rank: Int,
    val person: Person,
    val qualifyingTrips: Int,
    val fullTrips: Int,
    val halfTrips: Int,
    val totalEarnedPaise: Long
)

data class ParticipationMatrixRow(
    val person: Person,
    val tractorCounts: Map<String, Int>,
    val totalTrips: Int
)

data class ExceptionItem(
    val id: String,
    val title: String,
    val description: String,
    val type: String, // "ATTENDANCE_CONFLICT", "REMAINING_MONEY", "CALCULATION_PENDING", "BACKUP_OVERDUE"
    val severity: String = "INFO" // "INFO", "WARNING", "ALERT"
)

data class DataHealthReport(
    val dbStatus: String = "Online (Local Room SQLite sand_works.db)",
    val totalTrips: Int,
    val activeTrips: Int,
    val voidedTrips: Int,
    val totalPeople: Int,
    val activePeople: Int,
    val driversCount: Int,
    val labourersCount: Int,
    val totalTractors: Int,
    val activeTractors: Int,
    val attendanceRecordsCount: Int,
    val dailyClosuresCount: Int,
    val auditEventsCount: Int,
    val calculationErrors: List<String>,
    val isHealthy: Boolean,
    val checkedAt: Long = System.currentTimeMillis()
)
