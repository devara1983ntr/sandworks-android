package com.roshan.sandworks.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Query("SELECT * FROM people ORDER BY name ASC")
    fun getAllPeople(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people WHERE active = 1 ORDER BY name ASC")
    fun getActivePeople(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people WHERE type = :type AND active = 1 ORDER BY name ASC")
    fun getActivePeopleByType(type: String): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people WHERE personId = :id")
    suspend fun getPersonById(id: String): PersonEntity?

    @Query("SELECT * FROM people")
    suspend fun getAllPeopleSync(): List<PersonEntity>

    @Query("DELETE FROM people")
    suspend fun deleteAllPeople()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPeople(people: List<PersonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: PersonEntity)

    @Update
    suspend fun updatePerson(person: PersonEntity)
}

@Dao
interface TractorDao {
    @Query("SELECT * FROM tractors ORDER BY name ASC")
    fun getAllTractors(): Flow<List<TractorEntity>>

    @Query("SELECT * FROM tractors WHERE active = 1 ORDER BY name ASC")
    fun getActiveTractors(): Flow<List<TractorEntity>>

    @Query("SELECT * FROM tractors WHERE tractorId = :id")
    suspend fun getTractorById(id: String): TractorEntity?

    @Query("SELECT * FROM tractors")
    suspend fun getAllTractorsSync(): List<TractorEntity>

    @Query("DELETE FROM tractors")
    suspend fun deleteAllTractors()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTractors(tractors: List<TractorEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTractor(tractor: TractorEntity)

    @Update
    suspend fun updateTractor(tractor: TractorEntity)
}

@Dao
interface TripDao {
    @Query("SELECT * FROM trips WHERE workDate = :workDate ORDER BY tripNumber ASC")
    fun getTripsForDate(workDate: String): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE workDate = :workDate AND status = 'ACTIVE' ORDER BY tripNumber ASC")
    suspend fun getActiveTripsForDateSync(workDate: String): List<TripEntity>

    @Query("SELECT * FROM trips ORDER BY timestamp DESC")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE tripId = :tripId")
    suspend fun getTripById(tripId: String): TripEntity?

    @Query("SELECT MAX(tripNumber) FROM trips WHERE workDate = :workDate")
    suspend fun getMaxTripNumberForDate(workDate: String): Int?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTrip(trip: TripEntity)

    @Update
    suspend fun updateTrip(trip: TripEntity)

    // Participants
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipants(participants: List<TripParticipantEntity>)

    @Query("DELETE FROM trip_participants WHERE tripId = :tripId")
    suspend fun deleteParticipantsForTrip(tripId: String)

    @Query("SELECT * FROM trip_participants WHERE tripId = :tripId")
    suspend fun getParticipantsForTrip(tripId: String): List<TripParticipantEntity>

    @Query("SELECT * FROM trip_participants WHERE tripId = :tripId")
    fun getParticipantsForTripFlow(tripId: String): Flow<List<TripParticipantEntity>>

    @Query("SELECT * FROM trip_participants WHERE personId = :personId")
    suspend fun getParticipantsForPerson(personId: String): List<TripParticipantEntity>

    // Calculations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(calc: TripCalculationEntity)

    @Query("SELECT * FROM trip_calculations WHERE tripId = :tripId")
    suspend fun getCalculationForTrip(tripId: String): TripCalculationEntity?

    // Adjustments
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdjustments(adjustments: List<TripAdjustmentEntity>)

    @Query("SELECT * FROM trip_adjustments WHERE tripId = :tripId")
    suspend fun getAdjustmentsForTrip(tripId: String): List<TripAdjustmentEntity>

    // Bulk queries and reset
    @Query("SELECT * FROM trips")
    suspend fun getAllTripsSync(): List<TripEntity>

    @Query("SELECT * FROM trip_participants")
    suspend fun getAllParticipantsSync(): List<TripParticipantEntity>

    @Query("SELECT * FROM trip_calculations")
    suspend fun getAllCalculationsSync(): List<TripCalculationEntity>

    @Query("SELECT * FROM trip_adjustments")
    suspend fun getAllAdjustmentsSync(): List<TripAdjustmentEntity>

    @Query("DELETE FROM trips")
    suspend fun deleteAllTrips()

    @Query("DELETE FROM trip_participants")
    suspend fun deleteAllParticipants()

    @Query("DELETE FROM trip_calculations")
    suspend fun deleteAllCalculations()

    @Query("DELETE FROM trip_adjustments")
    suspend fun deleteAllAdjustments()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTrips(trips: List<TripEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCalculations(calcs: List<TripCalculationEntity>)

    @Transaction
    suspend fun insertTripWithParticipantsAndCalc(
        trip: TripEntity,
        participants: List<TripParticipantEntity>,
        calc: TripCalculationEntity,
        adjustments: List<TripAdjustmentEntity> = emptyList()
    ) {
        insertTrip(trip)
        insertParticipants(participants)
        insertCalculation(calc)
        if (adjustments.isNotEmpty()) {
            insertAdjustments(adjustments)
        }
    }
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE workDate = :workDate")
    fun getAttendanceForDate(workDate: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE workDate = :workDate")
    suspend fun getAttendanceForDateSync(workDate: String): List<AttendanceEntity>

    @Query("SELECT * FROM attendance WHERE personId = :personId")
    suspend fun getAttendanceForPerson(personId: String): List<AttendanceEntity>

    @Query("SELECT * FROM attendance WHERE personId = :personId AND workDate = :workDate")
    suspend fun getAttendance(personId: String, workDate: String): AttendanceEntity?

    @Query("SELECT * FROM attendance")
    suspend fun getAllAttendanceSync(): List<AttendanceEntity>

    @Query("DELETE FROM attendance")
    suspend fun deleteAllAttendance()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAttendance(attendances: List<AttendanceEntity>)
}

@Dao
interface DailyClosureDao {
    @Query("SELECT * FROM daily_closures WHERE workDate = :workDate")
    fun getClosureForDate(workDate: String): Flow<DailyClosureEntity?>

    @Query("SELECT * FROM daily_closures WHERE workDate = :workDate")
    suspend fun getClosureForDateSync(workDate: String): DailyClosureEntity?

    @Query("SELECT * FROM daily_closures ORDER BY workDate DESC")
    fun getAllClosures(): Flow<List<DailyClosureEntity>>

    @Query("SELECT * FROM daily_closures")
    suspend fun getAllClosuresSync(): List<DailyClosureEntity>

    @Query("DELETE FROM daily_closures")
    suspend fun deleteAllClosures()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllClosures(closures: List<DailyClosureEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateClosure(closure: DailyClosureEntity)
}

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_events ORDER BY timestamp DESC")
    fun getAllAuditEvents(): Flow<List<AuditEventEntity>>

    @Query("SELECT * FROM audit_events")
    suspend fun getAllAuditEventsSync(): List<AuditEventEntity>

    @Query("DELETE FROM audit_events")
    suspend fun deleteAllAuditEvents()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAuditEvents(events: List<AuditEventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditEvent(event: AuditEventEntity)
}

@Dao
interface EmergencyAlertDao {
    @Query("SELECT * FROM emergency_alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<EmergencyAlertEntity>>

    @Query("SELECT * FROM emergency_alerts WHERE acknowledged = 0 ORDER BY timestamp DESC")
    fun getUnacknowledgedAlerts(): Flow<List<EmergencyAlertEntity>>

    @Query("SELECT * FROM emergency_alerts WHERE acknowledged = 0 ORDER BY timestamp DESC LIMIT 1")
    fun getLatestUnacknowledgedAlert(): Flow<EmergencyAlertEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: EmergencyAlertEntity)

    @Query("UPDATE emergency_alerts SET acknowledged = 1, acknowledgedAt = :time WHERE alertId = :alertId")
    suspend fun acknowledgeAlert(alertId: String, time: Long = System.currentTimeMillis())

    @Query("DELETE FROM emergency_alerts")
    suspend fun deleteAllAlerts()
}

