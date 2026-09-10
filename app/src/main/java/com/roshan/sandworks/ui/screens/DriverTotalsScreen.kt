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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roshan.sandworks.domain.MoneyEngine
import com.roshan.sandworks.domain.model.PersonType
import com.roshan.sandworks.domain.model.TripStatus
import com.roshan.sandworks.ui.SandWorksViewModel
import com.roshan.sandworks.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverTotalsScreen(
    viewModel: SandWorksViewModel,
    onNavigateToAddTrip: () -> Unit,
    onNavigateBack: () -> Unit = {},
    onShareSummary: (String) -> Unit
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val allPeople by viewModel.allPeople.collectAsState()
    val selectedPersonId by viewModel.selectedPersonId.collectAsState()
    val trips by viewModel.tripsForSelectedDate.collectAsState()
    val attendance by viewModel.attendanceForSelectedDate.collectAsState()

    val drivers = remember(allPeople) {
        allPeople.filter { it.type == PersonType.DRIVER && it.active }
    }

    // Default to first driver if none selected
    val currentDriver = remember(drivers, selectedPersonId) {
        drivers.find { it.id == selectedPersonId } ?: drivers.firstOrNull()
    }

    // Filter trips where driver drove OR loaded
    val driverTrips = remember(trips, currentDriver) {
        if (currentDriver == null) emptyList()
        else trips.filter { td ->
            td.trip.status == TripStatus.ACTIVE &&
                    (td.trip.driverId == currentDriver.id || td.participants.any { it.person.id == currentDriver.id })
        }
    }

    val drivenTripsCount = remember(driverTrips, currentDriver) {
        driverTrips.count { it.trip.driverId == currentDriver?.id }
    }

    val loadedTripsCount = remember(driverTrips, currentDriver) {
        driverTrips.count { td -> td.participants.any { it.person.id == currentDriver?.id } }
    }

    val totalAccruedPaise = remember(driverTrips, currentDriver) {
        driverTrips.sumOf { td ->
            td.participants.find { it.person.id == currentDriver?.id }?.participant?.finalSharePaise ?: 0L
        }
    }

    val driverAttendance = remember(attendance, currentDriver) {
        attendance.find { it.personId == currentDriver?.id }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("driver_totals_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Column {
                        Text("My Totals & Accruals", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = currentDriver?.name ?: "Driver Workstation",
                            fontSize = 12.sp,
                            color = BrandOrange
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val text = buildDriverSummaryShareText(
                                driverName = currentDriver?.name ?: "Driver",
                                date = selectedDate,
                                drivenCount = drivenTripsCount,
                                loadedCount = loadedTripsCount,
                                totalPaise = totalAccruedPaise,
                                trips = driverTrips,
                                currentDriverId = currentDriver?.id ?: ""
                            )
                            onShareSummary(text)
                        },
                        modifier = Modifier.testTag("share_driver_summary_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Summary", tint = BrandSandGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandDarkSurface,
                    titleContentColor = BrandOffWhite
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTrip,
                containerColor = BrandOrange,
                contentColor = BrandOffWhite,
                modifier = Modifier.testTag("driver_add_trip_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Trip")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Driver Switcher & Date Navigation Row
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                border = BorderStroke(1.dp, BrandSlate)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Driver: ${currentDriver?.name ?: "None"}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = BrandOffWhite
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (driverAttendance?.status?.name == "PRESENT") SemanticSuccess.copy(alpha = 0.2f) else BrandSteel.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = driverAttendance?.status?.name ?: "UNMARKED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (driverAttendance?.status?.name == "PRESENT") SemanticSuccess else BrandSteel,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Date row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val prev = LocalDate.parse(selectedDate).minusDays(1).toString()
                                viewModel.selectDate(prev)
                            },
                            modifier = Modifier.testTag("driver_prev_date_button")
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day", tint = BrandOrange)
                        }
                        Text(
                            text = selectedDate,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = BrandSandGold
                        )
                        IconButton(
                            onClick = {
                                val next = LocalDate.parse(selectedDate).plusDays(1).toString()
                                viewModel.selectDate(next)
                            },
                            modifier = Modifier.testTag("driver_next_date_button")
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Day", tint = BrandOrange)
                        }
                    }
                }
            }

            // Stat Cards Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                    border = BorderStroke(1.dp, BrandSlate)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Driven", fontSize = 11.sp, color = BrandMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$drivenTripsCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandOrange
                        )
                        Text("trips", fontSize = 10.sp, color = BrandSteel)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                    border = BorderStroke(1.dp, BrandSlate)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Loaded", fontSize = 11.sp, color = BrandMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$loadedTripsCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandSandGold
                        )
                        Text("shares", fontSize = 10.sp, color = BrandSteel)
                    }
                }

                Card(
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                    border = BorderStroke(1.dp, BrandOrange.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Accrued Total", fontSize = 11.sp, color = BrandMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = MoneyEngine.formatPaise(totalAccruedPaise),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SemanticSuccess
                        )
                        Text("loading earnings", fontSize = 9.sp, color = BrandSteel)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Trips List Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Work Records (${driverTrips.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = BrandOffWhite
                )
                Text(
                    text = "Rate: ₹200/trip",
                    fontSize = 12.sp,
                    color = BrandMuted
                )
            }

            if (driverTrips.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = BrandSteel,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No trips recorded for this date",
                            color = BrandMuted,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToAddTrip,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Log First Trip")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(driverTrips) { td ->
                        val isDriver = td.trip.driverId == currentDriver?.id
                        val loaderParticipant = td.participants.find { it.person.id == currentDriver?.id }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                            border = BorderStroke(1.dp, BrandSlate)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
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
                                            color = BrandOrange.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "#${td.trip.tripNumber}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = BrandOrange,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = td.tractor?.name ?: "Tractor",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = BrandOffWhite
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (isDriver) {
                                            Surface(
                                                color = BrandOrange.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "DRIVER",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BrandOrange,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        if (loaderParticipant != null) {
                                            Surface(
                                                color = SemanticSuccess.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "LOADER (+${MoneyEngine.formatPaise(loaderParticipant.participant.finalSharePaise)})",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SemanticSuccess,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                val timeStr = remember(td.trip.timestamp) {
                                    Instant.ofEpochMilli(td.trip.timestamp)
                                        .atZone(ZoneId.systemDefault())
                                        .format(DateTimeFormatter.ofPattern("hh:mm a"))
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${td.participants.size} loaders participating",
                                        fontSize = 12.sp,
                                        color = BrandMuted
                                    )
                                    Text(
                                        text = timeStr,
                                        fontSize = 11.sp,
                                        color = BrandSteel
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }
}

private fun buildDriverSummaryShareText(
    driverName: String,
    date: String,
    drivenCount: Int,
    loadedCount: Int,
    totalPaise: Long,
    trips: List<com.roshan.sandworks.domain.model.TripWithDetails>,
    currentDriverId: String
): String {
    val sb = StringBuilder()
    sb.append("SAND WORKS — Driver Shift Summary\n")
    sb.append("Driver: $driverName\n")
    sb.append("Date: $date\n")
    sb.append("Driven Trips: $drivenCount\n")
    sb.append("Loaded Shares: $loadedCount\n")
    sb.append("Total Accrued: ${MoneyEngine.formatPaise(totalPaise)}\n\n")
    sb.append("Trips Breakdown:\n")
    for (td in trips) {
        val isDriver = td.trip.driverId == currentDriverId
        val loader = td.participants.find { it.person.id == currentDriverId }
        val roles = mutableListOf<String>()
        if (isDriver) roles.add("Driver")
        if (loader != null) roles.add("Loader (${MoneyEngine.formatPaise(loader.participant.finalSharePaise)})")
        sb.append("• Trip #${td.trip.tripNumber} (${td.tractor?.name ?: "Tractor"}): ${roles.joinToString(", ")}\n")
    }
    sb.append("\nRecorded via SAND WORKS App")
    return sb.toString()
}
