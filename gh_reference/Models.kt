package com.roshan.sandworks.model

/**
 * Roles invariant: Exactly one OWNER (Ramesh Sahu), DRIVER (e.g. Mansingh Rana), LABOURER.
 * NO ADMIN role ever.
 */
enum class Role {
    OWNER,
    DRIVER,
    LABOURER
}

enum class UserStatus {
    PENDING,
    ACTIVE,
    REJECTED,
    SUSPENDED
}

data class User(
    val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val phone: String = "",
    val role: Role = Role.LABOURER,
    val status: UserStatus = UserStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val approvedAt: Long? = null,
    val approvedBy: String? = null
)

data class Tractor(
    val id: String = "",
    val name: String = "",
    val registrationNumber: String = "",
    val isActive: Boolean = true,
    val totalTrips: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

enum class TripStatus {
    ACTIVE,
    VOIDED
}

enum class DistributionRule {
    EQUAL,
    DRIVER_LABOUR_RATIO,
    CUSTOM_PERCENT,
    FIXED_ALLOCATION
}

/**
 * Trip model:
 * All authoritative money is in integer paise. Default rate is ₹200 (20000 paise).
 * Every trip captures an immutable rateSnapshotPaise.
 */
data class Trip(
    val id: String = "",
    val tripNumber: Long = 0L,
    val tractorId: String = "",
    val tractorName: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val labourerIds: List<String> = emptyList(),
    val labourerNames: List<String> = emptyList(),
    val rateSnapshotPaise: Long = 20_000L, // ₹200.00 immutable snapshot
    val totalPoolPaise: Long = 20_000L,
    val distributionRule: DistributionRule = DistributionRule.EQUAL,
    val sharesPaise: Map<String, Long> = emptyMap(),
    val date: String = "", // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(),
    val status: TripStatus = TripStatus.ACTIVE,
    val voidReason: String? = null,
    val idempotencyKey: String = "",
    val tempAssignmentId: String? = null,
    val revision: Long = 1L
)

enum class TemporaryAssignmentStatus {
    ACTIVE,
    EXPIRED,
    REVOKED
}

data class TemporaryAssignment(
    val id: String = "",
    val assigningOwnerId: String = "",
    val targetUserId: String = "",
    val targetUserName: String = "",
    val tractorId: String = "",
    val tractorName: String = "",
    val assignedRole: Role = Role.DRIVER,
    val startTime: Long = System.currentTimeMillis(),
    val expiryTime: Long = System.currentTimeMillis() + (8 * 3600 * 1000), // 8 hours
    val reason: String = "",
    val status: TemporaryAssignmentStatus = TemporaryAssignmentStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis()
)

data class LeaderboardParticipant(
    val id: String = "",
    val name: String = "",
    val count: Int = 0
)

data class Leaderboard(
    val id: String = "",
    val period: String = "WEEKLY",
    val computedAt: Long = System.currentTimeMillis(),
    val topDrivers: List<LeaderboardParticipant> = emptyList(),
    val topLabourers: List<LeaderboardParticipant> = emptyList()
)


enum class AttendanceStatus {
    PRESENT,
    ABSENT
}

data class AttendanceRecord(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userRole: Role = Role.LABOURER,
    val date: String = "", // YYYY-MM-DD
    val status: AttendanceStatus = AttendanceStatus.PRESENT,
    val reason: String = "",
    val markedByOwner: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

data class DailyClosure(
    val id: String = "",
    val date: String = "",
    val totalTrips: Int = 0,
    val totalPoolPaise: Long = 0L,
    val driverAccrualsPaise: Map<String, Long> = emptyMap(),
    val labourerAccrualsPaise: Map<String, Long> = emptyMap(),
    val closedAt: Long = System.currentTimeMillis(),
    val summaryNotice: String = "Daily accrued-money summary" // Never labeled "payment"
)

enum class AlertSeverity {
    URGENT,
    CRITICAL
}

data class EmergencyAlert(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val title: String = "",
    val message: String = "",
    val severity: AlertSeverity = AlertSeverity.URGENT,
    val timestamp: Long = System.currentTimeMillis(),
    val acknowledgedUserIds: List<String> = emptyList()
)

data class BroadcastMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val title: String = "",
    val message: String = "",
    val targetRole: String = "ALL",
    val timestamp: Long = System.currentTimeMillis()
)

data class AuditLog(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val action: String = "",
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class NotificationType {
    ACCRUAL_SUMMARY, // Type A: Daily accrued-money summary
    EMERGENCY_ALERT, // Type B: Operational/emergency warning
    APPROVAL_OUTCOME, // Type C: Approval outcome
    TRIP_ASSIGNMENT, // Type D: Trip assignment
    EXPIRY_WARNING, // Type E: Temporary assignment expiry
    SYSTEM_ACCOUNT // Type F: Operational / system account
}

data class AppNotification(
    val id: String = "",
    val recipientId: String = "",
    val type: NotificationType = NotificationType.SYSTEM_ACCOUNT,
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val targetRoute: String? = null
)
