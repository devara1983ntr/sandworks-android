package com.roshan.sandworks.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roshan.sandworks.domain.MoneyEngine
import com.roshan.sandworks.domain.model.ClosureStatus
import com.roshan.sandworks.domain.model.ParticipationType
import com.roshan.sandworks.domain.model.TripStatus
import com.roshan.sandworks.ui.SandWorksViewModel
import com.roshan.sandworks.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyCalculationScreen(
    viewModel: SandWorksViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val summary by viewModel.dailySummary.collectAsState()
    val trips by viewModel.tripsForSelectedDate.collectAsState()

    var showCloseDayDialog by remember { mutableStateOf(false) }
    var showReopenDayDialog by remember { mutableStateOf(false) }

    // Aggregate person earnings for this date
    val personEarnings = remember(trips) {
        val activeTrips = trips.filter { it.trip.status == TripStatus.ACTIVE }
        val map = mutableMapOf<String, Triple<String, Int, Long>>() // personId -> (Name, tripCount, earnedPaise)

        for (t in activeTrips) {
            for (p in t.participants) {
                val current = map[p.person.id] ?: Triple(p.person.name, 0, 0L)
                map[p.person.id] = Triple(
                    current.first,
                    current.second + 1,
                    current.third + p.participant.finalSharePaise
                )
            }
        }
        map.values.sortedByDescending { it.third }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Reconciliation", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Date Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WORK DATE: $selectedDate",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandSandGold,
                        letterSpacing = 1.sp
                    )
                    Surface(
                        color = when (summary.status) {
                            ClosureStatus.CLOSED -> SemanticSuccess.copy(alpha = 0.2f)
                            ClosureStatus.REOPENED -> SemanticWarning.copy(alpha = 0.2f)
                            else -> BrandOrange.copy(alpha = 0.2f)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = summary.status.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (summary.status) {
                                ClosureStatus.CLOSED -> SemanticSuccess
                                ClosureStatus.REOPENED -> SemanticWarning
                                else -> BrandOrange
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Reconciliation Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                    border = BorderStroke(1.dp, BrandSlate)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "OFFICIAL RECONCILIATION SUMMARY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandSandGold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Gross Pool", fontSize = 12.sp, color = BrandMuted)
                                Text(
                                    text = MoneyEngine.formatPaise(summary.grossPaise),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandOffWhite
                                )
                                Text("${summary.activeTrips} active trips", fontSize = 11.sp, color = BrandSteel)
                            }
                            Column {
                                Text("Distributed", fontSize = 12.sp, color = BrandMuted)
                                Text(
                                    text = MoneyEngine.formatPaise(summary.distributedPaise),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SemanticSuccess
                                )
                                Text("to loaders", fontSize = 11.sp, color = BrandSteel)
                            }
                            Column {
                                Text("Remaining", fontSize = 12.sp, color = BrandMuted)
                                Text(
                                    text = MoneyEngine.formatPaise(summary.remainingPaise),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (summary.remainingPaise > 0L) BrandSandGold else BrandMuted
                                )
                                Text("undistributed", fontSize = 11.sp, color = BrandSteel)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = BrandSlate)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isBalanced = summary.grossPaise == (summary.distributedPaise + summary.remainingPaise)
                            Icon(
                                if (isBalanced) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (isBalanced) SemanticSuccess else SemanticError
                            )
                            Text(
                                text = if (isBalanced) "Reconciliation Verified: Exact Whole-Rupee Settlement" else "Discrepancy detected",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isBalanced) SemanticSuccess else SemanticError
                            )
                        }
                    }
                }
            }

            // Day Closure Action
            item {
                if (summary.status == ClosureStatus.CLOSED) {
                    OutlinedButton(
                        onClick = { showReopenDayDialog = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SemanticWarning),
                        border = BorderStroke(1.dp, SemanticWarning)
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reopen Work Day for Corrections")
                    }
                } else {
                    Button(
                        onClick = { showCloseDayDialog = true },
                        enabled = summary.activeTrips > 0,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Close Work Day & Finalize Calculation", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Person Earnings Breakdown Table
            item {
                Text(
                    text = "PERSON EARNINGS BREAKDOWN (${personEarnings.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandSteel,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (personEarnings.isEmpty()) {
                item {
                    Text("No loaders participated today.", color = BrandMuted, fontSize = 13.sp)
                }
            } else {
                items(personEarnings) { (name, tripCount, earnedPaise) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                        border = BorderStroke(1.dp, BrandSlate)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BrandOffWhite)
                                Text("$tripCount trips loaded", fontSize = 12.sp, color = BrandMuted)
                            }
                            Text(
                                text = MoneyEngine.formatPaise(earnedPaise),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = SemanticSuccess
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showCloseDayDialog) {
        AlertDialog(
            onDismissRequest = { showCloseDayDialog = false },
            title = { Text("Close Work Day: $selectedDate?") },
            text = {
                Text("This finalizes today's financial calculations. All ${summary.activeTrips} trips will be locked. You can still reopen the day if corrections are required.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.closeDay()
                        showCloseDayDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                ) {
                    Text("Close Day")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseDayDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showReopenDayDialog) {
        AlertDialog(
            onDismissRequest = { showReopenDayDialog = false },
            title = { Text("Reopen Work Day?") },
            text = {
                Text("Reopening will allow you to edit or void trips on $selectedDate. An audit log event will be recorded.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reopenDay()
                        showReopenDayDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SemanticWarning)
                ) {
                    Text("Reopen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReopenDayDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
