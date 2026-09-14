package com.roshan.sandworks

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.roshan.sandworks.domain.model.UserRole
import com.roshan.sandworks.ui.SandWorksViewModel
import com.roshan.sandworks.ui.SandWorksViewModelFactory
import com.roshan.sandworks.ui.components.EmergencyAlertBanner
import com.roshan.sandworks.ui.screens.*
import com.roshan.sandworks.ui.theme.BrandDeepCharcoal
import com.roshan.sandworks.ui.theme.BrandOrange
import com.roshan.sandworks.ui.theme.BrandSteel
import com.roshan.sandworks.ui.theme.SandWorksTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SandWorksViewModel by viewModels {
        val app = application as SandWorksApp
        SandWorksViewModelFactory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SandWorksTheme(darkTheme = true) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "home"
                val userRole by viewModel.userRole.collectAsState()
                val activeEmergencyAlert by viewModel.activeEmergencyAlert.collectAsState()

                val bottomBarRoutes = listOf("home", "trips", "people", "reports", "more")
                val showBottomBar = currentRoute in bottomBarRoutes

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = BrandDeepCharcoal,
                                contentColor = BrandOrange
                            ) {
                                NavigationBarItem(
                                    selected = currentRoute == "home",
                                    onClick = { navController.navigate("home") { launchSingleTop = true } },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("Home") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = BrandOrange,
                                        selectedTextColor = BrandOrange,
                                        unselectedIconColor = BrandSteel,
                                        unselectedTextColor = BrandSteel,
                                        indicatorColor = BrandOrange.copy(alpha = 0.2f)
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentRoute == "trips",
                                    onClick = { navController.navigate("trips") { launchSingleTop = true } },
                                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = "Trips") },
                                    label = { Text("Trips") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = BrandOrange,
                                        selectedTextColor = BrandOrange,
                                        unselectedIconColor = BrandSteel,
                                        unselectedTextColor = BrandSteel,
                                        indicatorColor = BrandOrange.copy(alpha = 0.2f)
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentRoute == "people",
                                    onClick = { navController.navigate("people") { launchSingleTop = true } },
                                    icon = { Icon(Icons.Default.People, contentDescription = "People") },
                                    label = { Text("People") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = BrandOrange,
                                        selectedTextColor = BrandOrange,
                                        unselectedIconColor = BrandSteel,
                                        unselectedTextColor = BrandSteel,
                                        indicatorColor = BrandOrange.copy(alpha = 0.2f)
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentRoute == "reports",
                                    onClick = { navController.navigate("reports") { launchSingleTop = true } },
                                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Reports") },
                                    label = { Text("Reports") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = BrandOrange,
                                        selectedTextColor = BrandOrange,
                                        unselectedIconColor = BrandSteel,
                                        unselectedTextColor = BrandSteel,
                                        indicatorColor = BrandOrange.copy(alpha = 0.2f)
                                    )
                                )

                                NavigationBarItem(
                                    selected = currentRoute == "more",
                                    onClick = { navController.navigate("more") { launchSingleTop = true } },
                                    icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "More") },
                                    label = { Text("More") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = BrandOrange,
                                        selectedTextColor = BrandOrange,
                                        unselectedIconColor = BrandSteel,
                                        unselectedTextColor = BrandSteel,
                                        indicatorColor = BrandOrange.copy(alpha = 0.2f)
                                    )
                                )
                            }
                        }
                    }
                ) { padding ->
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        activeEmergencyAlert?.let { alert ->
                            EmergencyAlertBanner(
                                alert = alert,
                                onAcknowledge = { viewModel.acknowledgeEmergencyAlert(it) }
                            )
                        }

                        NavHost(
                            navController = navController,
                            startDestination = "home",
                            modifier = Modifier.weight(1f)
                        ) {

                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToAddTrip = { navController.navigate("add_trip") },
                                onNavigateToTrips = { navController.navigate("trips") },
                                onNavigateToAttendance = { navController.navigate("attendance") },
                                onNavigateToCalculation = { navController.navigate("daily_calculation") },
                                onNavigateToReports = { navController.navigate("reports") },
                                onNavigateToDriverTotals = { navController.navigate("driver_totals") },
                                onNavigateToLabourerDays = { navController.navigate("labourer_days") },
                                onNavigateToExceptions = { navController.navigate("exceptions") },
                                onShareSummary = { text -> shareText(text) }
                            )
                        }

                        composable("trips") {
                            TripsHistoryScreen(
                                viewModel = viewModel,
                                onNavigateToAddTrip = { navController.navigate("add_trip") }
                            )
                        }

                        composable("add_trip") {
                            AddTripScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("people") {
                            PeopleScreen(
                                viewModel = viewModel,
                                onNavigateToDriverTotals = { navController.navigate("driver_totals") },
                                onNavigateToLabourerDays = { navController.navigate("labourer_days") }
                            )
                        }

                        composable("reports") {
                            ReportsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onShareText = { text -> shareText(text) }
                            )
                        }

                        composable("driver_totals") {
                            DriverTotalsScreen(
                                viewModel = viewModel,
                                onNavigateToAddTrip = { navController.navigate("add_trip") },
                                onNavigateBack = { navController.popBackStack() },
                                onShareSummary = { text -> shareText(text) }
                            )
                        }

                        composable("labourer_days") {
                            LabourerDaysScreen(
                                viewModel = viewModel,
                                onNavigateToLeaderboard = { navController.navigate("leaderboard") },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("more") {
                            MoreMenuScreen(
                                viewModel = viewModel,
                                onNavigateToAttendance = { navController.navigate("attendance") },
                                onNavigateToLeaderboard = { navController.navigate("leaderboard") },
                                onNavigateToTractors = { navController.navigate("tractors") },
                                onNavigateToCalculation = { navController.navigate("daily_calculation") },
                                onNavigateToExceptions = { navController.navigate("exceptions") },
                                onNavigateToDriverTotals = { navController.navigate("driver_totals") },
                                onNavigateToLabourerDays = { navController.navigate("labourer_days") },
                                onNavigateToDataHealth = { navController.navigate("data_health") },
                                onNavigateToRules = { navController.navigate("rules") },
                                onNavigateToAudit = { navController.navigate("audit") },
                                onNavigateToBackup = { navController.navigate("backup") },
                                onNavigateToAbout = { navController.navigate("about") }
                            )
                        }

                        composable("data_health") {
                            DataHealthScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToAudit = { navController.navigate("audit") },
                                onNavigateToBackup = { navController.navigate("backup") }
                            )
                        }


                        composable("backup") {
                            BackupRestoreScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onShareText = { text -> shareText(text) }
                            )
                        }

                        composable("attendance") {
                            AttendanceScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("leaderboard") {
                            LeaderboardScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("tractors") {
                            TractorsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("daily_calculation") {
                            DailyCalculationScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("exceptions") {
                            ExceptionCenterScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("rules") {
                            HelpCalculationRulesScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("audit") {
                            AuditHistoryScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("about") {
                            AboutScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

    private fun shareText(text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share via")
        startActivity(shareIntent)
    }
}
