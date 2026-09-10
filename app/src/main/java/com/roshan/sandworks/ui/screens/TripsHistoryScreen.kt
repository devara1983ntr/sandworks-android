package com.roshan.sandworks.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.roshan.sandworks.domain.model.ParticipationType
import com.roshan.sandworks.domain.model.PersonType
import com.roshan.sandworks.domain.model.TripStatus
import com.roshan.sandworks.domain.model.TripWithDetails
import com.roshan.sandworks.ui.SandWorksViewModel
import com.roshan.sandworks.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsHistoryScreen(
    viewModel: SandWorksViewModel,
    onNavigateToAddTrip: () -> Unit
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val trips by viewModel.tripsForSelectedDate.collectAsState()
    val tractors by viewModel.activeTractors.collectAsState()
    val allPeople by viewModel.allPeople.collectAsState()

    var selectedTractorFilter by remember { mutableStateOf<String?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<TripStatus?>(null) }
    var selectedTripForDetails by remember { mutableStateOf<TripWithDetails?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    val filteredTrips = remember(trips, selectedTractorFilter, selectedStatusFilter) {
        trips.filter { t ->
            val tractorMatches = selectedTractorFilter == null || t.trip.tractorId == selectedTractorFilter
            val statusMatches = selectedStatusFilter == null || t.trip.status == selectedStatusFilter
            tractorMatches && statusMatches
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trips", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.goToPreviousDay() }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                    }
                    Text(
                        text = selectedDate,
                        fontWeight = FontWeight.Bold,
                        color = BrandOrange,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { viewModel.goToToday() }
                    )
                    IconButton(onClick = { viewModel.goToNextDay() }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandDeepCharcoal,
                    titleContentColor = BrandOffWhite
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTrip,
                containerColor = BrandOrange,
                contentColor = BrandOffWhite
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Trip")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filters
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedTractorFilter == null,
                        onClick = { selectedTractorFilter = null },
                        label = { Text("All Tractors") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandOrange.copy(alpha = 0.2f),
                            selectedLabelColor = BrandOrange
                        )
                    )
                }
                items(tractors) { tractor ->
                    FilterChip(
                        selected = selectedTractorFilter == tractor.id,
                        onClick = { selectedTractorFilter = tractor.id },
                        label = { Text(tractor.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandOrange.copy(alpha = 0.2f),
                            selectedLabelColor = BrandOrange
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatusFilter == TripStatus.ACTIVE,
                        onClick = {
                            selectedStatusFilter = if (selectedStatusFilter == TripStatus.ACTIVE) null else TripStatus.ACTIVE
                        },
                        label = { Text("Active") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedStatusFilter == TripStatus.VOIDED,
                        onClick = {
                            selectedStatusFilter = if (selectedStatusFilter == TripStatus.VOIDED) null else TripStatus.VOIDED
                        },
                        label = { Text("Voided") }
                    )
                }
            }

            if (filteredTrips.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = BrandSteel, modifier = Modifier.size(54.dp))
                        Text(
                            text = if (trips.isEmpty()) "No trips recorded for $selectedDate" else "No trips matching current filter",
                            color = BrandOffWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (trips.isEmpty()) "Record tractor trips and loaders to compute accurate wage distribution." else "Try adjusting or clearing the filters above.",
                            color = BrandMuted,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (trips.isEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = onNavigateToAddTrip,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Record First Trip Today", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTrips) { tripDetail ->
                        val isVoided = tripDetail.trip.status == TripStatus.VOIDED
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTripForDetails = tripDetail },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isVoided) BrandGraphite.copy(alpha = 0.5f) else BrandGraphite
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isVoided) SemanticError.copy(alpha = 0.4f) else BrandSlate
                            )
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
                                            color = if (isVoided) SemanticError.copy(alpha = 0.2f) else BrandOrange.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "#${tripDetail.trip.tripNumber}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = if (isVoided) SemanticError else BrandOrange,
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

                                    if (isVoided) {
                                        Surface(
                                            color = SemanticError.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "VOIDED",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = SemanticError,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = MoneyEngine.formatPaise(tripDetail.trip.ratePaise),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = BrandOffWhite
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Participants list with chips
                                val pSummary = tripDetail.participants.joinToString { p ->
                                    "${p.person.name} (${MoneyEngine.formatPaise(p.participant.finalSharePaise)})"
                                }
                                Text(
                                    text = "Loaders (${tripDetail.participants.size}): $pSummary",
                                    fontSize = 12.sp,
                                    color = BrandMuted
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val timeStr = remember(tripDetail.trip.timestamp) {
                                        Instant.ofEpochMilli(tripDetail.trip.timestamp)
                                            .atZone(ZoneId.systemDefault())
                                            .format(DateTimeFormatter.ofPattern("hh:mm a"))
                                    }
                                    Text(text = timeStr, fontSize = 11.sp, color = BrandSteel)
                                    if ((tripDetail.calculation?.remainingPaise ?: 0L) > 0L && !isVoided) {
                                        Text(
                                            text = "Remaining: ${MoneyEngine.formatPaise(tripDetail.calculation!!.remainingPaise)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BrandSandGold
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(64.dp)) }
                }
            }
        }
    }

    // Trip Details & Void Dialog
    selectedTripForDetails?.let { td ->
        var showVoidDialog by remember { mutableStateOf(false) }
        var voidReason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { selectedTripForDetails = null },
            title = {
                Text("Trip #${td.trip.tripNumber} — ${td.tractor?.name ?: "Tractor"}")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Date: ${td.trip.workDate}")
                    Text("Rate: ${MoneyEngine.formatPaise(td.trip.ratePaise)}")
                    Text("Status: ${td.trip.status.name}")
                    if (td.trip.voidReason != null) {
                        Text("Void Reason: ${td.trip.voidReason}", color = SemanticError)
                    }
                    Divider(color = BrandSlate)
                    Text("Participants & Shares:", fontWeight = FontWeight.Bold)
                    td.participants.forEach { p ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("• ${p.person.name} (${p.participant.participationType.name})")
                            Text(MoneyEngine.formatPaise(p.participant.finalSharePaise), fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Divider(color = BrandSlate)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Distributed:")
                        Text(MoneyEngine.formatPaise(td.calculation?.distributedPaise ?: 0L))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Remaining:")
                        Text(MoneyEngine.formatPaise(td.calculation?.remainingPaise ?: 0L), color = BrandSandGold)
                    }
                }
            },
            confirmButton = {
                if (td.trip.status == TripStatus.ACTIVE) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showEditDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                        ) {
                            Text("Edit Trip")
                        }
                        Button(
                            onClick = { showVoidDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SemanticError)
                        ) {
                            Text("Void Trip")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTripForDetails = null }) {
                    Text("Close")
                }
            }
        )

        if (showEditDialog) {
            var editTractorId by remember { mutableStateOf(td.trip.tractorId) }
            var editDriverId by remember { mutableStateOf(td.trip.driverId) }
            val selectedLoaderIds = remember {
                mutableStateMapOf<String, ParticipationType>().apply {
                    td.participants.forEach { p ->
                        put(p.person.id, p.participant.participationType)
                    }
                }
            }

            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit Trip #${td.trip.tripNumber}") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Tractor", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(tractors) { tractor ->
                                FilterChip(
                                    selected = editTractorId == tractor.id,
                                    onClick = { editTractorId = tractor.id },
                                    label = { Text(tractor.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandOrange.copy(alpha = 0.2f),
                                        selectedLabelColor = BrandOrange
                                    )
                                )
                            }
                        }

                        Text("Driver", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        val drivers = remember(allPeople) { allPeople.filter { it.type == PersonType.DRIVER && it.active } }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(drivers) { driver ->
                                FilterChip(
                                    selected = editDriverId == driver.id,
                                    onClick = { editDriverId = if (editDriverId == driver.id) null else driver.id },
                                    label = { Text(driver.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandSandGold.copy(alpha = 0.2f),
                                        selectedLabelColor = BrandSandGold
                                    )
                                )
                            }
                        }

                        Text("Loaders Participating", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        val loaders = remember(allPeople) { allPeople.filter { it.active } }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            loaders.take(8).forEach { person ->
                                val isSelected = selectedLoaderIds.containsKey(person.id)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                if (checked) selectedLoaderIds[person.id] = ParticipationType.FULL
                                                else selectedLoaderIds.remove(person.id)
                                            }
                                        )
                                        Text(person.name, fontSize = 13.sp, color = BrandOffWhite)
                                    }
                                    if (isSelected) {
                                        val type = selectedLoaderIds[person.id] ?: ParticipationType.FULL
                                        FilterChip(
                                            selected = type == ParticipationType.HALF,
                                            onClick = {
                                                selectedLoaderIds[person.id] =
                                                    if (type == ParticipationType.HALF) ParticipationType.FULL else ParticipationType.HALF
                                            },
                                            label = { Text(if (type == ParticipationType.HALF) "Half" else "Full", fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val participants = selectedLoaderIds.map { (pid, ptype) ->
                                MoneyEngine.ParticipantInput(personId = pid, participationType = ptype)
                            }
                            if (participants.isNotEmpty() && editTractorId.isNotBlank()) {
                                viewModel.editTrip(
                                    tripId = td.trip.id,
                                    tractorId = editTractorId,
                                    driverId = editDriverId,
                                    participants = participants
                                ) {
                                    showEditDialog = false
                                    selectedTripForDetails = null
                                }
                            }
                        },
                        enabled = selectedLoaderIds.isNotEmpty() && editTractorId.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                    ) {
                        Text("Save Changes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showVoidDialog) {

            AlertDialog(
                onDismissRequest = { showVoidDialog = false },
                title = { Text("Void Trip #${td.trip.tripNumber}?") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("This will mark Trip #${td.trip.tripNumber} as VOIDED. Its earnings will be removed from all participants' daily totals, but the trip sequence number is preserved for audit.")
                        OutlinedTextField(
                            value = voidReason,
                            onValueChange = { voidReason = it },
                            label = { Text("Reason (required)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (voidReason.isNotBlank()) {
                                viewModel.voidTrip(td.trip.id, voidReason)
                                showVoidDialog = false
                                selectedTripForDetails = null
                            }
                        },
                        enabled = voidReason.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = SemanticError)
                    ) {
                        Text("Confirm Void")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showVoidDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
