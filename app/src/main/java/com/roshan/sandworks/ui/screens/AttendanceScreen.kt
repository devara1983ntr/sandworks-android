package com.roshan.sandworks.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roshan.sandworks.domain.model.AttendanceStatus
import com.roshan.sandworks.domain.model.TripStatus
import com.roshan.sandworks.ui.SandWorksViewModel
import com.roshan.sandworks.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: SandWorksViewModel,
    onNavigateBack: () -> Unit
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val activePeople by viewModel.activePeople.collectAsState()
    val attendanceList by viewModel.attendanceForSelectedDate.collectAsState()
    val trips by viewModel.tripsForSelectedDate.collectAsState()

    // Find people who loaded in active trips today
    val loadedPersonIds = remember(trips) {
        val active = trips.filter { it.trip.status == TripStatus.ACTIVE }
        active.flatMap { it.participants.map { p -> p.person.id } }.toSet()
    }

    val attendanceMap = remember(attendanceList) {
        attendanceList.associateBy { it.personId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance — $selectedDate", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            item {
                Text(
                    text = "MARK ATTENDANCE FOR LABOURERS & DRIVERS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandSteel,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "If a worker is marked absent but loaded trips today, an attendance conflict warning will be recorded.",
                    fontSize = 11.sp,
                    color = BrandMuted
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (activePeople.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (loadedPersonIds.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { viewModel.autoMarkLoadedWorkersPresent(loadedPersonIds) },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandSandGold),
                                border = BorderStroke(1.dp, BrandSandGold)
                            ) {
                                Text("Auto-Mark Loaded (${loadedPersonIds.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { viewModel.markAllPresent(activePeople.map { it.id }) },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SemanticSuccess)
                        ) {
                            Text("Mark All Present", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (activePeople.isEmpty()) {
                item {
                    Surface(
                        color = BrandGraphite,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No active workers found", fontWeight = FontWeight.Bold, color = BrandOffWhite)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Add labourers or drivers in the People tab first.", fontSize = 12.sp, color = BrandMuted)
                        }
                    }
                }
            } else {
                items(activePeople) { person ->
                val currentStatus = attendanceMap[person.id]?.status ?: AttendanceStatus.UNMARKED
                val hasLoadedToday = person.id in loadedPersonIds
                val hasConflict = currentStatus == AttendanceStatus.ABSENT && hasLoadedToday

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                    border = BorderStroke(
                        1.dp,
                        if (hasConflict) SemanticWarning else BrandSlate
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = person.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = BrandOffWhite
                                )
                                Text(
                                    text = if (hasLoadedToday) "Worked in trips today" else "No trips loaded today",
                                    fontSize = 11.sp,
                                    color = if (hasLoadedToday) BrandSandGold else BrandMuted
                                )
                            }

                            if (hasConflict) {
                                Surface(
                                    color = SemanticWarning.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = SemanticWarning, modifier = Modifier.size(12.dp))
                                        Text("Conflict", color = SemanticWarning, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Status Selector Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = currentStatus == AttendanceStatus.PRESENT,
                                onClick = { viewModel.setAttendance(person.id, AttendanceStatus.PRESENT) },
                                label = { Text("Present") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SemanticSuccess.copy(alpha = 0.25f),
                                    selectedLabelColor = SemanticSuccess
                                )
                            )

                            FilterChip(
                                selected = currentStatus == AttendanceStatus.ABSENT,
                                onClick = { viewModel.setAttendance(person.id, AttendanceStatus.ABSENT) },
                                label = { Text("Absent") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SemanticError.copy(alpha = 0.25f),
                                    selectedLabelColor = SemanticError
                                )
                            )

                            FilterChip(
                                selected = currentStatus == AttendanceStatus.UNMARKED,
                                onClick = { viewModel.setAttendance(person.id, AttendanceStatus.UNMARKED) },
                                label = { Text("Unmarked") }
                            )
                        }
                    }
                }
            }
        }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
