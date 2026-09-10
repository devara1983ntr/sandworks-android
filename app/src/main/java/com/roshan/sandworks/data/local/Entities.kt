package com.roshan.sandworks.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "people",
    indices = [Index(value = ["name"])]
)
data class PersonEntity(
    @PrimaryKey val personId: String,
    val name: String,
    val type: String, // "LABOURER" or "DRIVER"
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tractors")
data class TractorEntity(
    @PrimaryKey val tractorId: String,
    val name: String,
    val brand: String = "",
    val model: String = "",
    val specification: String = "",
    val registrationIdentifier: String = "",
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "trips",
    indices = [
        Index(value = ["workDate", "tripNumber"], unique = true),
        Index(value = ["workDate"]),
        Index(value = ["tractorId"]),
        Index(value = ["status"])
    ]
)
data class TripEntity(
    @PrimaryKey val tripId: String,
    val workDate: String,
    val tripNumber: Int,
    val timestamp: Long,
    val tractorId: String,
    val driverId: String?,
    val ratePaise: Long,
    val status: String, // "ACTIVE", "VOIDED"
    val voidReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "trip_participants",
    indices = [
        Index(value = ["tripId"]),
        Index(value = ["personId"]),
        Index(value = ["tripId", "personId"], unique = true)
    ]
)
data class TripParticipantEntity(
    @PrimaryKey val tripParticipantId: String,
    val tripId: String,
    val personId: String,
    val participationType: String, // "FULL", "HALF"
    val calculatedSharePaise: Long,
    val adjustmentPaise: Long = 0L,
    val finalSharePaise: Long
)

@Entity(
    tableName = "trip_calculations",
    indices = [
        Index(value = ["tripId"], unique = true)
    ]
)
data class TripCalculationEntity(
    @PrimaryKey val calculationId: String,
    val tripId: String,
    val ratePaise: Long,
    val participantCount: Int,
    val baseSharePaise: Long,
    val distributedPaise: Long,
    val remainingPaise: Long,
    val calculationVersion: Int = 1,
    val calculatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "trip_adjustments",
    indices = [
        Index(value = ["tripId"])
    ]
)
data class TripAdjustmentEntity(
    @PrimaryKey val adjustmentId: String,
    val tripId: String,
    val recipientId: String,
    val amountPaise: Long,
    val reason: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "attendance",
    indices = [
        Index(value = ["personId", "workDate"], unique = true),
        Index(value = ["workDate"])
    ]
)
data class AttendanceEntity(
    @PrimaryKey val attendanceId: String,
    val personId: String,
    val workDate: String,
    val status: String, // "PRESENT", "ABSENT", "UNMARKED"
    val reason: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "daily_closures",
    indices = [
        Index(value = ["workDate"], unique = true)
    ]
)
data class DailyClosureEntity(
    @PrimaryKey val closureId: String,
    val workDate: String,
    val grossPaise: Long,
    val distributedPaise: Long,
    val remainingPaise: Long,
    val status: String, // "OPEN", "CALCULATED", "READY_TO_CLOSE", "CLOSED", "REOPENED"
    val calculatedAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null,
    val reopenedAt: Long? = null
)

@Entity(
    tableName = "audit_events",
    indices = [
        Index(value = ["timestamp"])
    ]
)
data class AuditEventEntity(
    @PrimaryKey val auditId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String,
    val entityType: String,
    val entityId: String,
    val description: String,
    val oldValue: String? = null,
    val newValue: String? = null
)

@Entity(
    tableName = "emergency_alerts",
    indices = [
        Index(value = ["timestamp"])
    ]
)
data class EmergencyAlertEntity(
    @PrimaryKey val alertId: String,
    val title: String,
    val message: String,
    val severity: String, // "URGENT", "CRITICAL"
    val targetRole: String = "ALL", // "ALL", "DRIVER", "LABOURER"
    val senderName: String = "Ramesh Sahu (Owner)",
    val timestamp: Long = System.currentTimeMillis(),
    val acknowledged: Boolean = false,
    val acknowledgedAt: Long? = null
)
