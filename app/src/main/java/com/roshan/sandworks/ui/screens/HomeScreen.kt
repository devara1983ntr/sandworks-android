package com.roshan.sandworks.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roshan.sandworks.R
import com.roshan.sandworks.domain.MoneyEngine
import com.roshan.sandworks.domain.model.ClosureStatus
import com.roshan.sandworks.ui.SandWorksViewModel
import com.roshan.sandworks.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: SandWorksViewModel,
    onNavigateToAddTrip: () -> Unit,
    onNavigateToTrips: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToCalculation: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToDriverTotals: () -> Unit = {},
    onNavigateToLabourerDays: () -> Unit = {},
    onNavigateToExceptions: () -> Unit = {},
    onShareSummary: (String) -> Unit
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dailySummary by viewModel.dailySummary.collectAsState()
    val trips by viewModel.tripsForSelectedDate.collectAsState()
    val tractors by viewModel.activeTractors.collectAsState()
    val allPeople by viewModel.allPeople.collectAsState()
    val exceptions by viewModel.exceptions.collectAsState()

    val parsedDate = remember(selectedDate) {
        try { LocalDate.parse(selectedDate) } catch (e: Exception) { LocalDate.now() }
    }

    var showShareDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.sand_works_logo),
                            contentDescription = "SAND WORKS Logo",
                            modifier = Modifier.height(34.dp)
                        )
                    }
                },
                actions = {
                    // Date Navigation
                    IconButton(
                        onClick = { viewModel.goToPreviousDay() },
                        modifier = Modifier.testTag("home_prev_day_button")
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                    }
                    Text(
                        text = parsedDate.format(DateTimeFormatter.ofPattern("dd MMM")),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandOrange,
                        modifier = Modifier
                            .testTag("home_current_date_text")
                            .clickable { viewModel.goToToday() }
                    )
                    IconButton(
                        onClick = { viewModel.goToNextDay() },
                        modifier = Modifier.testTag("home_next_day_button")
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // Gesture-Enabled Daily Summary Card with Horizontal Swipe
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_summary_card")
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, dragAmount ->
                                change.consume()
                                if (dragAmount < -50f) {
                                    viewModel.goToNextDay()
                                } else if (dragAmount > 50f) {
                                    viewModel.goToPreviousDay()
                                }
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                    border = BorderStroke(1.dp, BrandSlate)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TODAY'S OPERATIONS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.sp,
                                    color = BrandSandGold
                                )
                                Text(
                                    text = "${dailySummary.activeTrips} Trips Recorded",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandOffWhite
                                )
                            }
                            // Status Badge
                            val statusColor = when (dailySummary.status) {
                                ClosureStatus.CLOSED -> SemanticSuccess
                                ClosureStatus.REOPENED -> SemanticWarning
                                ClosureStatus.NO_WORK -> BrandSteel
                                else -> BrandOrange
                            }
                            Surface(
                                color = statusColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, statusColor)
                            ) {
                                Text(
                                    text = dailySummary.status.name.replace("_", " "),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = BrandSlate, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Financial Breakdown Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Gross Pool", fontSize = 12.sp, color = BrandMuted)
                                Text(
                                    text = MoneyEngine.formatPaise(dailySummary.grossPaise),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandOffWhite
                                )
                            }
                            Column {
                                Text("Distributed", fontSize = 12.sp, color = BrandMuted)
                                Text(
                                    text = MoneyEngine.formatPaise(dailySummary.distributedPaise),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SemanticSuccess
                                )
                            }
                            Column {
                                Text("Remaining", fontSize = 12.sp, color = BrandMuted)
                                Text(
                                    text = MoneyEngine.formatPaise(dailySummary.remainingPaise),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dailySummary.remainingPaise > 0) BrandSandGold else BrandMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SemanticSuccess, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Mathematical Invariant: Gross == Distributed + Remaining",
                                fontSize = 11.sp,
                                color = BrandSteel
                            )
                        }
                    }
                }
            }

            // Exceptions banner if any conflicts
            if (exceptions.isNotEmpty()) {
                item {
                    Surface(
                        color = SemanticWarning.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SemanticWarning.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToExceptions() }
                            .testTag("home_exceptions_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = SemanticWarning)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${exceptions.size} Exception(s) Require Attention",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = BrandOffWhite
                                )
                                Text(
                                    text = exceptions.first().description,
                                    fontSize = 12.sp,
                                    color = BrandMuted
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = BrandSteel)
                        }
                    }
                }
            }

            // Primary Quick Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onNavigateToAddTrip,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("home_add_trip_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Add Trip", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onNavigateToAttendance,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("home_attendance_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandOffWhite),
                        border = BorderStroke(1.dp, BrandSlate)
                    ) {
                        Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Attendance", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToCalculation,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("home_calculate_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandSandGold),
                        border = BorderStroke(1.dp, BrandSlate)
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Calculate & Close", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = { showShareDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("home_share_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandOffWhite),
                        border = BorderStroke(1.dp, BrandSlate)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Today", fontSize = 13.sp)
                    }
                }
            }

            // Workstations Quick Jump Cards
            item {
                Text(
                    text = "WORKER REGISTERS & LEDGERS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = BrandSteel,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToDriverTotals() }
                            .testTag("home_driver_workstation_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                        border = BorderStroke(1.dp, BrandSlate)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = BrandSandGold, modifier = Modifier.size(24.dp))
                            Column {
                                Text("Driver Shifts", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandOffWhite)
                                Text("Totals & Accruals", fontSize = 11.sp, color = BrandMuted)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToLabourerDays() }
                            .testTag("home_labourer_workstation_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                        border = BorderStroke(1.dp, BrandSlate)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(24.dp))
                            Column {
                                Text("Labourer Days", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandOffWhite)
                                Text("Earnings & Shifts", fontSize = 11.sp, color = BrandMuted)
                            }
                        }
                    }
                }
            }

            // Tractor Activity Summary
            item {
                Text(
                    text = "TRACTOR ACTIVITY TODAY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = BrandSteel,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (tractors.isEmpty()) {
                        Surface(
                            color = BrandGraphite,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            border = BorderStroke(1.dp, BrandOrange.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "No tractors registered yet. Setup standard 3-tractor fleet to record trips.",
                                    fontSize = 12.sp,
                                    color = BrandOffWhite
                                )
                                Button(
                                    onClick = { viewModel.seedDefaultFleet() },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Agriculture, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Setup Standard 3-Tractor Fleet", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        tractors.forEach { tractor ->
                            val count = dailySummary.tractorTripCounts[tractor.id] ?: 0
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                                border = BorderStroke(1.dp, BrandSlate)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = tractor.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = BrandOffWhite
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$count",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = if (count > 0) BrandOrange else BrandSteel
                                    )
                                    Text("trips", fontSize = 11.sp, color = BrandMuted)
                                }
                            }
                        }
                    }
                }
            }

            // Recent Trips Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT TRIPS TODAY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = BrandSteel
                    )
                    TextButton(onClick = onNavigateToTrips) {
                        Text("View All (${trips.size})", color = BrandOrange, fontSize = 13.sp)
                    }
                }
            }

            if (trips.isEmpty()) {
                item {
                    Surface(
                        color = BrandGraphite,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = BrandSteel, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No trips recorded today", fontWeight = FontWeight.Bold, color = BrandOffWhite)
                            Text("Tap + Add Trip when loading starts", fontSize = 12.sp, color = BrandMuted)
                        }
                    }
                }
            } else {
                items(trips.take(5)) { tripDetail ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToTrips() },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                        border = BorderStroke(1.dp, BrandSlate)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        color = BrandDeepOrange.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "#${tripDetail.trip.tripNumber}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = BrandOrange,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = tripDetail.tractor?.name ?: "Tractor",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = BrandOffWhite
                                    )
                                }
                                Text(
                                    text = MoneyEngine.formatPaise(tripDetail.trip.ratePaise),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = BrandOffWhite
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            // Participants list
                            val participantNames = tripDetail.participants.joinToString {
                                "${it.person.name} (${MoneyEngine.formatPaise(it.participant.finalSharePaise)})"
                            }
                            Text(
                                text = "Loaders: $participantNames",
                                fontSize = 12.sp,
                                color = BrandMuted
                            )

                            if ((tripDetail.calculation?.remainingPaise ?: 0L) > 0L) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Remaining: ${MoneyEngine.formatPaise(tripDetail.calculation!!.remainingPaise)}",
                                    fontSize = 11.sp,
                                    color = BrandSandGold
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Share Today's Summary") },
            text = {
                Text("This will generate today's operational and financial summary for WhatsApp or messaging. Includes total trips, tractor breakdown, loader earnings, and remaining money balance.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showShareDialog = false
                        val text = viewModel.generateShareSummaryText("QUICK")
                        onShareSummary(text)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                ) {
                    Text("Share")
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
