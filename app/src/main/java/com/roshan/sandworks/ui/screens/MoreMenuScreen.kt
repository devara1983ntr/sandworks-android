package com.roshan.sandworks.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roshan.sandworks.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreMenuScreen(
    viewModel: com.roshan.sandworks.ui.SandWorksViewModel? = null,
    onNavigateToAttendance: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToTractors: () -> Unit,
    onNavigateToCalculation: () -> Unit,
    onNavigateToExceptions: () -> Unit,
    onNavigateToDriverTotals: () -> Unit = {},
    onNavigateToLabourerDays: () -> Unit = {},
    onNavigateToDataHealth: () -> Unit = {},
    onNavigateToRules: () -> Unit,
    onNavigateToAudit: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    var showBroadcastDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var broadcastTitle by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var broadcastMessage by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var broadcastSeverity by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("URGENT") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("More Options & Tools", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandDeepCharcoal,
                    titleContentColor = BrandOffWhite
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            item {
                Text(
                    text = "OPERATIONAL TOOLS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandSteel,
                    letterSpacing = 1.sp
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.Campaign,
                    title = "Broadcast Operational Warning",
                    subtitle = "Send immediate urgent alert to drivers and labourers",
                    onClick = { showBroadcastDialog = true }
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.Badge,
                    title = "Attendance Register",
                    subtitle = "Mark labourer & driver attendance, track conflicts",
                    onClick = onNavigateToAttendance
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.DirectionsCar,
                    title = "Driver Shifts & Accruals",
                    subtitle = "Driven trips, daily loading splits, and rate totals",
                    onClick = onNavigateToDriverTotals
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.CalendarMonth,
                    title = "Labourer Work Days & History",
                    subtitle = "Days worked, trip participation breakdowns, and earnings",
                    onClick = onNavigateToLabourerDays
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.EmojiEvents,
                    title = "Trip Leaderboard",
                    subtitle = "Weekly qualifying trips & monthly honours",
                    onClick = onNavigateToLeaderboard
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.Agriculture,
                    title = "Tractors & Fleet",
                    subtitle = "Manage tractor names, brands, and models",
                    onClick = onNavigateToTractors
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.Calculate,
                    title = "Daily Calculation & Reconciliation",
                    subtitle = "Verify gross pool and close work day",
                    onClick = onNavigateToCalculation
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.Warning,
                    title = "Exception Center",
                    subtitle = "Review attendance conflicts & pending items",
                    onClick = onNavigateToExceptions
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.HealthAndSafety,
                    title = "Data Consistency & Health",
                    subtitle = "Verify local Room invariants & check discrepancies",
                    onClick = onNavigateToDataHealth
                )
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "RULES, AUDIT & ABOUT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandSteel,
                    letterSpacing = 1.sp
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.MenuBook,
                    title = "Calculation Rules & Sandbox",
                    subtitle = "Interactive test of ₹200 whole-rupee floor math",
                    onClick = onNavigateToRules
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.History,
                    title = "Audit History Log",
                    subtitle = "Chronological record of all changes & actions",
                    onClick = onNavigateToAudit
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.Shield,
                    title = "Backup & Data Safety",
                    subtitle = "Export JSON backup snapshot & restore records",
                    onClick = onNavigateToBackup
                )
            }

            item {
                MoreMenuItem(
                    icon = Icons.Default.Info,
                    title = "About SAND WORKS",
                    subtitle = "Version 1.0.0 • Offline-first industrial safety",
                    onClick = onNavigateToAbout
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    if (showBroadcastDialog) {
        AlertDialog(
            onDismissRequest = { showBroadcastDialog = false },
            title = { Text("Broadcast Operational Warning") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Broadcast an urgent operational notice to all active drivers and labourers. It will appear at the top of their screens until acknowledged.",
                        fontSize = 13.sp,
                        color = BrandMuted
                    )
                    OutlinedTextField(
                        value = broadcastTitle,
                        onValueChange = { broadcastTitle = it },
                        label = { Text("Alert Title") },
                        placeholder = { Text("e.g. Heavy Rain / Route Diversion") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = broadcastMessage,
                        onValueChange = { broadcastMessage = it },
                        label = { Text("Detailed Instructions") },
                        placeholder = { Text("e.g. Halt river sand extraction temporarily until water clears.") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = broadcastSeverity == "URGENT",
                            onClick = { broadcastSeverity = "URGENT" },
                            label = { Text("URGENT") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandOrange.copy(alpha = 0.2f),
                                selectedLabelColor = BrandOrange
                            )
                        )
                        FilterChip(
                            selected = broadcastSeverity == "CRITICAL",
                            onClick = { broadcastSeverity = "CRITICAL" },
                            label = { Text("CRITICAL") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SemanticError.copy(alpha = 0.2f),
                                selectedLabelColor = SemanticError
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (broadcastTitle.isNotBlank()) {
                            viewModel?.broadcastEmergencyAlert(
                                title = broadcastTitle,
                                message = broadcastMessage,
                                severity = broadcastSeverity
                            )
                            showBroadcastDialog = false
                            broadcastTitle = ""
                            broadcastMessage = ""
                        }
                    },
                    enabled = broadcastTitle.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (broadcastSeverity == "CRITICAL") SemanticError else BrandOrange
                    )
                ) {
                    Text("Broadcast Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBroadcastDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun MoreMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BrandGraphite),
        border = BorderStroke(1.dp, BrandSlate)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(icon, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(24.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = BrandOffWhite)
                Text(subtitle, fontSize = 12.sp, color = BrandMuted)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = BrandSteel)
        }
    }
}
