package com.roshan.sandworks

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class SandWorksApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        // Initialize Firebase App Check
        initAppCheck()

        // Create FCM Notification Channels
        createNotificationChannels()
    }

    private fun initAppCheck() {
        try {
            val appCheck = FirebaseAppCheck.getInstance()
            if (BuildConfig.DEBUG) {
                // Debug provider for local JVM / emulator / development
                appCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
            } else {
                // Play Integrity for genuine production Android devices
                appCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
            }
        } catch (_: Exception) {
            // Gracefully handle environments without Google Play Services
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 1. Emergency Alerts Channel (Urgent, High Importance, Vibration)
            val emergencyChannel = NotificationChannel(
                CHANNEL_EMERGENCY,
                "Emergency Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical site warnings and urgent operational alerts"
                enableVibration(true)
                enableLights(true)
            }

            // 2. Broadcast Messages Channel
            val broadcastChannel = NotificationChannel(
                CHANNEL_BROADCAST,
                "Team Broadcasts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Operational messages and directives from the Owner"
            }

            // 3. Daily Accrual Summaries Channel
            val accrualChannel = NotificationChannel(
                CHANNEL_ACCRUALS,
                "Daily Accrual Summaries",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "End-of-day accrued money accounting summaries"
            }

            notificationManager.createNotificationChannels(
                listOf(emergencyChannel, broadcastChannel, accrualChannel)
            )
        }
    }

    companion object {
        const val CHANNEL_EMERGENCY = "sandworks_emergency_channel"
        const val CHANNEL_BROADCAST = "sandworks_broadcast_channel"
        const val CHANNEL_ACCRUALS = "sandworks_accruals_channel"
    }
}
