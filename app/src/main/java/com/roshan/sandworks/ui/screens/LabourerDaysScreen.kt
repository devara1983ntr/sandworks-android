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
import com.roshan.sandworks.domain.model.PersonSummary
import com.roshan.sandworks.domain.model.PersonType
import com.roshan.sandworks.domain.model.TripStatus
import com.roshan.sandworks.ui.SandWorksViewModel
import com.roshan.sandworks.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabourerDaysScreen(
    viewModel: SandWorksViewModel,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    val allPeople by viewModel.allPeople.collectAsState()
    val selectedPersonId by viewModel.selectedPersonId.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val trips by viewModel.tripsForSelectedDate.collectAsState()

    val labourers = remember(allPeople) {
        allPeople.filter { it.type == PersonType.LABOURER && it.active }
    }

    val currentLabourer = remember(labourers, selectedPersonId) {
        labourers.find { it.id == selectedPersonId } ?: labourers.firstOrNull()
    }

    var personSummary by remember { mutableStateOf<PersonSummary?>(null) }
    var isLoadingSummary by remember { mutableStateOf(false) }

    LaunchedEffect(currentLabourer?.id) {
        currentLabourer?.let { p ->
            isLoadingSummary = true
            personSummary = viewModel.getPersonSummary(p.id)
            isLoadingSummary = false
        }
    }

    // Filter state for dates
    var selectedTab by remember { mutableStateOf(0) } // 0 = All, 1 = Worked, 2 = Absent

    // Today's trips for this labourer
    val todaysLabourerTrips = remember(trips, currentLabourer) {
        if (currentLabourer == null) emptyList()
        else trips.filter { td ->
            td.trip.status == TripStatus.ACTIVE &&
                    td.participants.any { it.person.id == currentLabourer.id }
        }
    }

    val todaysEarningsPaise = remember(todaysLabourerTrips, currentLabourer) {
        todaysLabourerTrips.sumOf { td ->
            td.participants.find { it.person.id == currentLabourer?.id }?.participant?.finalSharePaise ?: 0L
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("labourer_days_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Column {
                        Text("My Work Days & History", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = currentLabourer?.name ?: "Labourer Workstation",
                            fontSize = 12.sp,
                            color = BrandOrange
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToLeaderboard,
                        modifier = Modifier.testTag("labourer_leaderboard_nav_button")
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = "Leaderboard", tint = BrandSandGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandDarkSurface,
                    titleContentColor = BrandOffWhite
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Profile & Attendance Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BrandOrange.copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = BrandOrange)
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = currentLabourer?.name ?: "No Labourer Selected",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = BrandOffWhite
                                )
                                Text(
                                    text = "SAND WORKS Labour Team",
                                    fontSize = 11.sp,
                                    color = BrandMuted
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BrandSandGold.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "ACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandSandGold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4-stat metrics grid
                    val summary = personSummary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatTile(
                            modifier = Modifier.weight(1f),
                            label = "Work Days",
                            value = "${summary?.attendanceDays ?: 0}",
                            color = SemanticSuccess
                        )
                        StatTile(
                            modifier = Modifier.weight(1f),
                            label = "Absent Days",
                            value = "${summary?.absentDays ?: 0}",
                            color = SemanticError
                        )
                        StatTile(
                            modifier = Modifier.weight(1f),
                            label = "Total Trips",
                            value = "${summary?.totalTrips ?: 0}",
                            color = BrandOrange
                        )
                        StatTile(
                            modifier = Modifier.weight(1.3f),
                            label = "Total Earned",
                            value = MoneyEngine.formatPaise(summary?.totalEarnedPaise ?: 0L),
                            color = BrandSandGold
                        )
                    }
                }
            }

            // Filter Tabs Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = BrandDarkSurface,
                contentColor = BrandOrange,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Today's Work", fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Career Summary", fontSize = 13.sp) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTab) {
                0 -> {
                    // Today's work view
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                        border = BorderStroke(1.dp, BrandSlate)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Selected Date: $selectedDate", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = BrandOffWhite)
                                Text("${todaysLabourerTrips.size} trips loaded today", fontSize = 11.sp, color = BrandMuted)
                            }
                            Text(
                                text = MoneyEngine.formatPaise(todaysEarningsPaise),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = SemanticSuccess
                            )
                        }
                    }

                    if (todaysLabourerTrips.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = BrandSteel, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No trips loaded on $selectedDate", color = BrandMuted, fontSize = 14.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(todaysLabourerTrips) { td ->
                                val participant = td.participants.find { it.person.id == currentLabourer?.id }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                                    border = BorderStroke(1.dp, BrandSlate)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Surface(
                                                    color = BrandOrange.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        "#${td.trip.tripNumber}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = BrandOrange,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Text(td.tractor?.name ?: "Tractor", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = BrandOffWhite)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${td.participants.size} loaders sharing",
                                                fontSize = 11.sp,
                                                color = BrandMuted
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = MoneyEngine.formatPaise(participant?.participant?.finalSharePaise ?: 0L),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = SemanticSuccess
                                            )
                                            Text(
                                                text = participant?.participant?.participationType?.name ?: "FULL",
                                                fontSize = 10.sp,
                                                color = BrandSteel
                                            )
                                        }
                                    }
                                }
                            }

                            item { Spacer(modifier = Modifier.height(72.dp)) }
                        }
                    }
                }
                1 -> {
                    // Career overview view
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                                border = BorderStroke(1.dp, BrandSlate)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Career Statistics", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BrandOffWhite)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    DetailRow(label = "Member Name", value = currentLabourer?.name ?: "-")
                                    DetailRow(label = "Role", value = "Loading Labourer")
                                    DetailRow(label = "Standard Trip Rate", value = "₹200.00")
                                    DetailRow(label = "Attendance Rate", value = "${calcAttendanceRate(personSummary)}%")
                                    DetailRow(label = "Average Trips/Workday", value = calcAvgTrips(personSummary))
                                    DetailRow(label = "Lifetime Accrued", value = MoneyEngine.formatPaise(personSummary?.totalEarnedPaise ?: 0L))
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                                border = BorderStroke(1.dp, BrandSlate)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = BrandSandGold, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Calculation Transparency", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandSandGold)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "All participant shares are calculated via strict integer paise precision using the whole-rupee floor rule. No fractional paise or rounding errors exist.",
                                        fontSize = 12.sp,
                                        color = BrandMuted
                                    )
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(72.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = BrandDarkSurface,
        border = BorderStroke(1.dp, BrandSlate)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.sp, color = BrandMuted, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color, maxLines = 1)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = BrandMuted)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = BrandOffWhite)
    }
}

private fun calcAttendanceRate(summary: PersonSummary?): Int {
    if (summary == null) return 0
    val total = summary.attendanceDays + summary.absentDays
    if (total == 0) return 100
    return ((summary.attendanceDays.toDouble() / total) * 100).toInt()
}

private fun calcAvgTrips(summary: PersonSummary?): String {
    if (summary == null || summary.attendanceDays == 0) return "0.0"
    val avg = summary.totalTrips.toDouble() / summary.attendanceDays
    return String.format("%.1f", avg)
}
