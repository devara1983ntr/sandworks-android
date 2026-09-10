package com.roshan.sandworks.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PersonEntity::class,
        TractorEntity::class,
        TripEntity::class,
        TripParticipantEntity::class,
        TripCalculationEntity::class,
        TripAdjustmentEntity::class,
        AttendanceEntity::class,
        DailyClosureEntity::class,
        AuditEventEntity::class,
        EmergencyAlertEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SandWorksDatabase : RoomDatabase() {

    abstract fun personDao(): PersonDao
    abstract fun tractorDao(): TractorDao
    abstract fun tripDao(): TripDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun dailyClosureDao(): DailyClosureDao
    abstract fun auditDao(): AuditDao
    abstract fun emergencyAlertDao(): EmergencyAlertDao

    companion object {
        @Volatile
        private var INSTANCE: SandWorksDatabase? = null

        fun getInstance(context: Context): SandWorksDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SandWorksDatabase::class.java,
                    "sand_works.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

