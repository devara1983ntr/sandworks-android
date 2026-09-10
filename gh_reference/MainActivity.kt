package com.roshan.sandworks

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.roshan.sandworks.data.AuthState
import com.roshan.sandworks.data.SandWorksRepository
import com.roshan.sandworks.model.Role
import com.roshan.sandworks.model.UserStatus
import com.roshan.sandworks.ui.components.*
import com.roshan.sandworks.ui.screens.*
import com.roshan.sandworks.ui.theme.SandWorksTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = SandWorksRepository.getInstance()

        setContent {
            SandWorksTheme(darkTheme = true) {
                MainAppContent(repository = repository)
            }
        }
    }
}

@Composable
fun MainAppContent(repository: SandWorksRepository) {
    val authState by repository.authState.collectAsState()
    val currentUser by repository.currentUser.collectAsState()
    val emergencyAlerts by repository.emergencyAlerts.collectAsState()
    val notifications by repository.notifications.collectAsState()
    val isOffline by repository.isOffline.collectAsState()

    // Runtime permission launcher for notifications on Android 13+ (API 33+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var authScreen by remember { mutableStateOf("welcome") } // "welcome", "signin", "signup"
    var signInError by remember { mutableStateOf<String?>(null) }
    var signUpError by remember { mutableStateOf<String?>(null) }
    var isAuthLoading by remember { mutableStateOf(false) }

    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Navigation sub-states for active roles
    var ownerScreen by remember { mutableStateOf("dashboard") }
    var driverScreen by remember { mutableStateOf("dashboard") }
    var labourerScreen by remember { mutableStateOf("dashboard") }

    val user = currentUser
    if (user == null || authState !is AuthState.Authenticated) {
        when (authScreen) {
            "welcome" -> {
                WelcomeScreen(
                    onNavigateToSignIn = {
                        signInError = null
                        authScreen = "signin"
                    },
                    onNavigateToSignUp = {
                        signUpError = null
                        authScreen = "signup"
                    }
                )
            }
            "signin" -> {
                SignInScreen(
                    onSignIn = { email, pass ->
                        isAuthLoading = true
                        signInError = null
                        repository.signIn(email, pass) { success, err ->
                            isAuthLoading = false
                            if (!success) {
                                signInError = err ?: "Sign in failed"
                            }
                        }
                    },
                    onNavigateToSignUp = {
                        signUpError = null
                        authScreen = "signup"
                    },
                    onNavigateToForgotPassword = {
                        showForgotPasswordDialog = true
                    },
                    isLoading = isAuthLoading,
                    errorMessage = signInError
                )
            }
            "signup" -> {
                SignUpScreen(
                    onSignUp = { email, pass, name, phone, role ->
                        isAuthLoading = true
                        signUpError = null
                        repository.signUp(email, pass, name, phone, role) { success, err ->
                            isAuthLoading = false
                            if (!success) {
                                signUpError = err ?: "Sign up failed"
                            }
                        }
                    },
                    onNavigateToSignIn = {
                        signInError = null
                        authScreen = "signin"
                    },
                    isLoading = isAuthLoading,
                    errorMessage = signUpError
                )
            }
        }

        if (showForgotPasswordDialog) {
            ForgotPasswordDialog(
                onSendReset = { email, callback ->
                    repository.sendPasswordReset(email, callback)
                },
                onDismiss = { showForgotPasswordDialog = false }
            )
        }
        return
    }

    // Authenticated user check
    if (user.status != UserStatus.ACTIVE && user.role != Role.OWNER) {
        AccountStatusScreen(
            userStatus = user.status,
            userName = user.fullName,
            onSignOut = {
                repository.signOut()
                authScreen = "welcome"
            }
        )
        return
    }

    // Active urgent alerts for current user
    val activeUnackAlert = remember(emergencyAlerts, user.uid) {
        emergencyAlerts.firstOrNull { !it.acknowledgedUserIds.contains(user.uid) }
    }
    val unreadAlertsCount = remember(emergencyAlerts, user.uid) {
        emergencyAlerts.count { !it.acknowledgedUserIds.contains(user.uid) }
    }

    when (user.role) {
        Role.OWNER -> {
            Scaffold(
                topBar = {
                    SandWorksTopBar(
                        title = "SAND WORKS",
                        role = Role.OWNER,
                        onSignOutClick = {
                            repository.signOut()
                            authScreen = "welcome"
                        },
                        onNotificationsClick = { showNotificationsDialog = true },
                        onHelpClick = { showHelpDialog = true },
                        onSettingsClick = { showSettingsDialog = true },
                        unreadAlertCount = unreadAlertsCount
                    )
                },
                bottomBar = {
                    OwnerBottomNav(
                        currentScreen = ownerScreen,
                        onNavigate = { ownerScreen = it }
                    )
                }
            ) { padding ->
                Surface(modifier = Modifier.padding(padding)) {
                    Column {
                        OfflineNoticeBanner(
                            isOffline = isOffline,
                            onReconnectClick = { repository.retryConnection() }
                        )
                        if (activeUnackAlert != null) {
                            EmergencyAlertBanner(
                                alert = activeUnackAlert,
                                onAcknowledge = { repository.acknowledgeAlert(activeUnackAlert.id, user.uid) }
                            )
                        }
                        when (ownerScreen) {
                            "dashboard" -> OwnerDashboardScreen(repository = repository, onNavigate = { ownerScreen = it })
                            "trips" -> OwnerTripsScreen(repository = repository)
                            "people" -> OwnerPeopleScreen(repository = repository)
                            "tractors" -> OwnerTractorsScreen(repository = repository)
                            "accrual" -> OwnerAccrualClosureScreen(repository = repository)
                            "attendance" -> OwnerAttendanceScreen(repository = repository)
                            "exports" -> OwnerExportScreen(repository = repository)
                            "audit" -> OwnerAuditScreen(repository = repository)
                            "temp_access" -> OwnerTemporaryAccessScreen(repository = repository)
                            else -> OwnerDashboardScreen(repository = repository, onNavigate = { ownerScreen = it })
                        }
                    }
                }
            }
        }
        Role.DRIVER -> {
            Scaffold(
                topBar = {
                    SandWorksTopBar(
                        title = "SAND WORKS",
                        role = Role.DRIVER,
                        onSignOutClick = {
                            repository.signOut()
                            authScreen = "welcome"
                        },
                        onNotificationsClick = { showNotificationsDialog = true },
                        onHelpClick = { showHelpDialog = true },
                        onSettingsClick = { showSettingsDialog = true },
                        unreadAlertCount = unreadAlertsCount
                    )
                },
                bottomBar = {
                    DriverBottomNav(
                        currentScreen = driverScreen,
                        onNavigate = { driverScreen = it }
                    )
                }
            ) { padding ->
                Surface(modifier = Modifier.padding(padding)) {
                    Column {
                        OfflineNoticeBanner(
                            isOffline = isOffline,
                            onReconnectClick = { repository.retryConnection() }
                        )
                        if (activeUnackAlert != null) {
                            EmergencyAlertBanner(
                                alert = activeUnackAlert,
                                onAcknowledge = { repository.acknowledgeAlert(activeUnackAlert.id, user.uid) }
                            )
                        }
                        when (driverScreen) {
                            "dashboard" -> DriverDashboardScreen(
                                repository = repository,
                                currentDriver = user,
                                onNavigate = { driverScreen = it }
                            )
                            "add_trip" -> DriverAddTripScreen(
                                repository = repository,
                                currentDriver = user,
                                onTripAdded = { driverScreen = "dashboard" }
                            )
                            "history" -> DriverTripsHistoryScreen(repository = repository, currentDriver = user)
                            "accrued" -> DriverAccruedScreen(repository = repository, currentDriver = user)
                            "share" -> DriverShareScreen(repository = repository, currentDriver = user)
                            else -> DriverDashboardScreen(repository = repository, currentDriver = user, onNavigate = { driverScreen = it })
                        }
                    }
                }
            }
        }
        Role.LABOURER -> {
            Scaffold(
                topBar = {
                    SandWorksTopBar(
                        title = "SAND WORKS",
                        role = Role.LABOURER,
                        onSignOutClick = {
                            repository.signOut()
                            authScreen = "welcome"
                        },
                        onNotificationsClick = { showNotificationsDialog = true },
                        onHelpClick = { showHelpDialog = true },
                        onSettingsClick = { showSettingsDialog = true },
                        unreadAlertCount = unreadAlertsCount
                    )
                },
                bottomBar = {
                    LabourerBottomNav(
                        currentScreen = labourerScreen,
                        onNavigate = { labourerScreen = it }
                    )
                }
            ) { padding ->
                Surface(modifier = Modifier.padding(padding)) {
                    Column {
                        OfflineNoticeBanner(
                            isOffline = isOffline,
                            onReconnectClick = { repository.retryConnection() }
                        )
                        if (activeUnackAlert != null) {
                            EmergencyAlertBanner(
                                alert = activeUnackAlert,
                                onAcknowledge = { repository.acknowledgeAlert(activeUnackAlert.id, user.uid) }
                            )
                        }
                        when (labourerScreen) {
                            "dashboard" -> LabourerDashboardScreen(repository = repository, currentLabourer = user)
                            "accrued" -> LabourerAccruedScreen(repository = repository, currentLabourer = user)
                            "attendance" -> LabourerAttendanceScreen(repository = repository, currentLabourer = user)
                            "leaderboard" -> LabourerLeaderboardScreen(repository = repository, currentLabourer = user)
                            "profile" -> LabourerProfileScreen(
                                currentLabourer = user,
                                onSignOut = {
                                    repository.signOut()
                                    authScreen = "welcome"
                                }
                            )
                            else -> LabourerDashboardScreen(repository = repository, currentLabourer = user)
                        }
                    }
                }
            }
        }
    }

    if (showNotificationsDialog) {
        NotificationCenterDialog(
            notifications = notifications,
            onMarkRead = { repository.markNotificationRead(it) },
            onMarkAllRead = { repository.markAllNotificationsRead() },
            onDismiss = { showNotificationsDialog = false }
        )
    }

    if (showHelpDialog) {
        HelpAboutDialog(
            onDismiss = { showHelpDialog = false }
        )
    }

    if (showSettingsDialog && user != null) {
        SettingsDialog(
            user = user,
            onDismiss = { showSettingsDialog = false }
        )
    }
}
