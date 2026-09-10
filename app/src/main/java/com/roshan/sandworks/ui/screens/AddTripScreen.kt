package com.roshan.sandworks.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roshan.sandworks.domain.MoneyEngine
import com.roshan.sandworks.domain.model.Person
import com.roshan.sandworks.domain.model.PersonType
import com.roshan.sandworks.ui.SandWorksViewModel
import com.roshan.sandworks.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTripScreen(
    viewModel: SandWorksViewModel,
    onNavigateBack: () -> Unit
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val activeTractors by viewModel.activeTractors.collectAsState()
    val activePeople by viewModel.activePeople.collectAsState()

    var selectedTractorId by remember { mutableStateOf("") }
    var selectedDriverId by remember { mutableStateOf<String?>(null) }
    // Ensure default tractor selected if available
    LaunchedEffect(activeTractors) {
        if (selectedTractorId.isEmpty() && activeTractors.isNotEmpty()) {
            selectedTractorId = activeTractors.first().id
        }
    }

    // Selected participants: map personId -> isHalfShare
    val selectedParticipants = remember { mutableStateMapOf<String, Boolean>() }
    var tripRatePaise by remember { mutableStateOf(MoneyEngine.DEFAULT_TRIP_RATE_PAISE) }

    val labourers = remember(activePeople) {
        activePeople.filter { it.type == PersonType.LABOURER }
    }
    val drivers = remember(activePeople) {
        activePeople.filter { it.type == PersonType.DRIVER }
    }

    // Live calculation calculation
    val participantInputs = remember(selectedParticipants.size, selectedParticipants.values.toList()) {
        selectedParticipants.map { (id, isHalf) ->
            MoneyEngine.ParticipantInput(id, isHalf)
        }
    }

    val liveCalc = remember(tripRatePaise, participantInputs) {
        MoneyEngine.calculateTrip(tripRatePaise, participantInputs)
    }

    var showSingleLoaderWarning by remember { mutableStateOf(false) }
    var pendingNextTrip by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    fun executeSave(addNext: Boolean) {
        if (selectedParticipants.size == 1 && !showSingleLoaderWarning) {
            showSingleLoaderWarning = true
            pendingNextTrip = addNext
            return
        }

        isSaving = true
        viewModel.createTrip(
            tractorId = selectedTractorId,
            driverId = selectedDriverId,
            ratePaise = tripRatePaise,
            participants = participantInputs,
            onSuccess = {
                isSaving = false
                if (addNext) {
                    // Reset participants for next trip, keep tractor & driver
                    selectedParticipants.clear()
                } else {
                    onNavigateBack()
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Sand Loading Trip", fontWeight = FontWeight.Bold) },
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

            // Date & Trip Info
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WORK DATE: $selectedDate",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandSandGold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "RATE: ${MoneyEngine.formatPaise(tripRatePaise)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandOffWhite
                    )
                }
            }

            // 1. Select Tractor
            item {
                Text(
                    text = "1. SELECT TRACTOR",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandSteel,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (activeTractors.isEmpty()) {
                    Text("No tractors available. Please add tractors first.", color = SemanticError)
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        activeTractors.forEach { tractor ->
                            val isSelected = tractor.id == selectedTractorId
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedTractorId = tractor.id },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) BrandOrange.copy(alpha = 0.2f) else BrandGraphite,
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) BrandOrange else BrandSlate
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.Agriculture,
                                        contentDescription = null,
                                        tint = if (isSelected) BrandOrange else BrandSteel
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = tractor.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) BrandOrange else BrandOffWhite
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Select Driver Who Drove
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "2. TRACTOR DRIVER (WHO DROVE)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandSteel,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (drivers.isEmpty()) {
                    Text("No drivers registered. Drivers can be added in People tab.", fontSize = 12.sp, color = BrandMuted)
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedDriverId == null,
                            onClick = { selectedDriverId = null },
                            label = { Text("None / Self") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandSandGold,
                                selectedLabelColor = BrandDeepCharcoal
                            )
                        )
                        drivers.forEach { driver ->
                            FilterChip(
                                selected = selectedDriverId == driver.id,
                                onClick = { selectedDriverId = driver.id },
                                label = { Text(driver.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandOrange,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Live Calculation Preview Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                    border = BorderStroke(1.dp, BrandSlate)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CALCULATION PREVIEW",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandSandGold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${liveCalc.participantCount} Loaders",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandOffWhite
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Full Share", fontSize = 11.sp, color = BrandMuted)
                                Text(
                                    text = MoneyEngine.formatPaise(liveCalc.baseSharePaise),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SemanticSuccess
                                )
                            }
                            if (liveCalc.halfCount > 0) {
                                Column {
                                    Text("Half Share", fontSize = 11.sp, color = BrandMuted)
                                    Text(
                                        text = MoneyEngine.formatPaise(liveCalc.halfSharePaise),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SemanticWarning
                                    )
                                }
                            }
                            Column {
                                Text("Distributed", fontSize = 11.sp, color = BrandMuted)
                                Text(
                                    text = MoneyEngine.formatPaise(liveCalc.distributedPaise),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandOffWhite
                                )
                            }
                            Column {
                                Text("Remaining", fontSize = 11.sp, color = BrandMuted)
                                Text(
                                    text = MoneyEngine.formatPaise(liveCalc.remainingPaise),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (liveCalc.remainingPaise > 0L) BrandSandGold else BrandMuted
                                )
                            }
                        }
                    }
                }
            }

            // 2. Select Labourers
            item {
                Text(
                    text = "2. SELECT LABOURERS WHO LOADED",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandSteel,
                    letterSpacing = 1.sp
                )
            }

            items(labourers) { person ->
                LoaderSelectionRow(
                    person = person,
                    isSelected = selectedParticipants.containsKey(person.id),
                    isHalfShare = selectedParticipants[person.id] ?: false,
                    onToggleSelect = {
                        if (selectedParticipants.containsKey(person.id)) {
                            selectedParticipants.remove(person.id)
                        } else {
                            selectedParticipants[person.id] = false
                        }
                    },
                    onToggleHalfShare = {
                        val current = selectedParticipants[person.id] ?: false
                        selectedParticipants[person.id] = !current
                    }
                )
            }

            // 3. Select Drivers (if participating in loading)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "3. DRIVERS WHO ALSO LOADED (OPTIONAL)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandSteel,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Drivers who participate in loading receive an equal loader share.",
                    fontSize = 11.sp,
                    color = BrandMuted
                )
            }

            items(drivers) { person ->
                LoaderSelectionRow(
                    person = person,
                    isSelected = selectedParticipants.containsKey(person.id),
                    isHalfShare = selectedParticipants[person.id] ?: false,
                    onToggleSelect = {
                        if (selectedParticipants.containsKey(person.id)) {
                            selectedParticipants.remove(person.id)
                        } else {
                            selectedParticipants[person.id] = false
                        }
                    },
                    onToggleHalfShare = {
                        val current = selectedParticipants[person.id] ?: false
                        selectedParticipants[person.id] = !current
                    }
                )
            }

            // Action Buttons
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { executeSave(addNext = false) },
                        enabled = !isSaving && selectedParticipants.isNotEmpty() && selectedTractorId.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = BrandOffWhite,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Saving Trip...")
                        } else {
                            Text(
                                text = "Save Trip (#${liveCalc.participantCount} Loaders)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { executeSave(addNext = true) },
                        enabled = !isSaving && selectedParticipants.isNotEmpty() && selectedTractorId.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandSandGold),
                        border = BorderStroke(1.dp, BrandSlate)
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save & Add Next Trip", fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showSingleLoaderWarning) {
        AlertDialog(
            onDismissRequest = { showSingleLoaderWarning = false },
            title = { Text("Single Loader Confirmation") },
            text = {
                Text("Only 1 person loaded this entire tractor. They will receive the full ${MoneyEngine.formatPaise(tripRatePaise)}. Confirm?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSingleLoaderWarning = false
                        isSaving = true
                        viewModel.createTrip(
                            tractorId = selectedTractorId,
                            driverId = selectedDriverId,
                            ratePaise = tripRatePaise,
                            participants = participantInputs,
                            onSuccess = {
                                isSaving = false
                                if (pendingNextTrip) {
                                    selectedParticipants.clear()
                                } else {
                                    onNavigateBack()
                                }
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSingleLoaderWarning = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LoaderSelectionRow(
    person: Person,
    isSelected: Boolean,
    isHalfShare: Boolean,
    onToggleSelect: () -> Unit,
    onToggleHalfShare: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleSelect() },
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) BrandOrange.copy(alpha = 0.12f) else BrandGraphite,
        border = BorderStroke(
            1.dp,
            if (isSelected) BrandOrange.copy(alpha = 0.6f) else BrandSlate
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    colors = CheckboxDefaults.colors(checkedColor = BrandOrange)
                )
                Column {
                    Text(
                        text = person.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = BrandOffWhite
                    )
                    Text(
                        text = if (person.type == PersonType.DRIVER) "Driver" else "Labourer",
                        fontSize = 11.sp,
                        color = if (person.type == PersonType.DRIVER) BrandSandGold else BrandMuted
                    )
                }
            }

            if (isSelected) {
                // Half share toggle
                FilterChip(
                    selected = isHalfShare,
                    onClick = onToggleHalfShare,
                    label = {
                        Text(
                            text = if (isHalfShare) "Half Share" else "Full Share",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandSandGold.copy(alpha = 0.25f),
                        selectedLabelColor = BrandSandGold,
                        containerColor = BrandSlate,
                        labelColor = BrandOffWhite
                    )
                )
            }
        }
    }
}
