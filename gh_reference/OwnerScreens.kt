package com.roshan.sandworks.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roshan.sandworks.data.SandWorksRepository
import com.roshan.sandworks.domain.MoneyEngine
import com.roshan.sandworks.model.*
import com.roshan.sandworks.ui.components.MoneyCard
import com.roshan.sandworks.ui.components.StatusBadge
import com.roshan.sandworks.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OwnerDashboardScreen(
    repository: SandWorksRepository,
    onNavigate: (String) -> Unit
) {
    val trips by repository.trips.collectAsState()
    val tractors by repository.tractors.collectAsState()
    val users by repository.users.collectAsState()
    val currentRate by repository.currentTripRatePaise.collectAsState()

    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val todayDate = remember { sdf.format(Date()) }

    val todayTrips = remember(trips, todayDate) {
        trips.filter { it.date == todayDate && it.status == TripStatus.ACTIVE }
    }
    val todayPool = remember(todayTrips) {
        todayTrips.sumOf { it.totalPoolPaise }
    }
    val pendingUsers = remember(users) {
        users.filter { it.status == UserStatus.PENDING }
    }

    var showAddTripDialog by remember { mutableStateOf(false) }
    var showEmergencyDialog by remember { mutableStateOf(false) }
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var viewingTrip by remember { mutableStateOf<Trip?>(null) }
    var editingTrip by remember { mutableStateOf<Trip?>(null) }
    var selectedTractorForDetail by remember { mutableStateOf<Tractor?>(null) }
    var leaderboardPeriod by remember { mutableStateOf("WEEKLY") }
    val currentUser by repository.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Pending approvals notification banner if any
        if (pendingUsers.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SemanticWarning.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("people") }
                    .testTag("pending_approvals_banner")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = SemanticWarning)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${pendingUsers.size} pending account approval(s) require review",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SemanticWarning)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MoneyCard(
                title = "Today's Accruals",
                paise = todayPool,
                subtitle = "${todayTrips.size} active trips",
                modifier = Modifier.weight(1f).testTag("today_pool_card")
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Trip Snapshot Rate", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        MoneyEngine.formatPaise(currentRate),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = BrandSandGold
                    )
                    Text("Configurable by Owner", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Actions Row
        Text("Quick Operational Controls", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { showAddTripDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("quick_add_trip_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Log Trip")
            }
            OutlinedButton(
                onClick = { showEmergencyDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SemanticError),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("quick_emergency_button")
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Alert")
            }
            OutlinedButton(
                onClick = { showBroadcastDialog = true },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("quick_broadcast_button")
            ) {
                Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Message")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Operations Hub Shortcuts
        Text("Operations Hub", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate("attendance") }
                    .testTag("hub_attendance_shortcut")
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.FactCheck, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Attendance", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate("exports") }
                    .testTag("hub_exports_shortcut")
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = BrandSandGold, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Exports", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate("audit") }
                    .testTag("hub_audit_shortcut")
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Audit Log", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate("temp_access") }
                    .testTag("hub_temp_access_shortcut")
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Badge, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Temp Access", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Fleet Status
        Text("Active Fleet", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))

        tractors.forEach { tractor ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { selectedTractorForDetail = tractor }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Agriculture, contentDescription = null, tint = BrandSandGold)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(tractor.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("${tractor.registrationNumber} • ${tractor.totalTrips} lifetime trips", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    StatusBadge(if (tractor.isActive) "ACTIVE" else "INACTIVE")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Activity Leaderboard (Backend Aggregated)
        val activityLeaderboard = remember(trips, leaderboardPeriod) { repository.getLeaderboard(leaderboardPeriod) }
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("leaderboard_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = BrandSandGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (leaderboardPeriod == "WEEKLY") "Weekly Activity Rankings" else "Monthly Activity Rankings",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = leaderboardPeriod == "WEEKLY",
                            onClick = { leaderboardPeriod = "WEEKLY" },
                            label = { Text("Week", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = leaderboardPeriod == "MONTHLY",
                            onClick = { leaderboardPeriod = "MONTHLY" },
                            label = { Text("Month", fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    // Top Drivers
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Drivers", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        if (activityLeaderboard.topDrivers.isEmpty()) {
                            Text("No trips logged this period", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            activityLeaderboard.topDrivers.forEachIndexed { idx, d ->
                                Text("${idx + 1}. ${d.name} (${d.count})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Top Labourers
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Labourers", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        if (activityLeaderboard.topLabourers.isEmpty()) {
                            Text("No participation logged", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            activityLeaderboard.topLabourers.forEachIndexed { idx, l ->
                                Text("${idx + 1}. ${l.name} (${l.count})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recent Trips Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Logged Trips", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            TextButton(onClick = { onNavigate("trips") }) {
                Text("View All (${trips.size})")
            }
        }

        if (trips.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No trips logged yet. Tap 'Log Trip' to record the first trip.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            trips.take(5).forEach { trip ->
                TripRowItem(
                    trip = trip,
                    onVoidClick = null,
                    onClick = { viewingTrip = trip }
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    if (viewingTrip != null) {
        TripDetailDialog(
            trip = viewingTrip!!,
            currentUser = currentUser,
            onDismiss = { viewingTrip = null },
            onEditTrip = { trip ->
                viewingTrip = null
                editingTrip = trip
            },
            onVoidTrip = { trip ->
                viewingTrip = null
                repository.voidTrip(trip.id, "Voided by owner from trip inspection")
            }
        )
    }

    if (editingTrip != null) {
        val activeTractors = remember(tractors) { tractors.filter { it.isActive } }
        val activeLabourers = remember(users) { users.filter { it.role == Role.LABOURER && it.status == UserStatus.ACTIVE } }
        EditTripDialog(
            trip = editingTrip!!,
            tractors = activeTractors,
            labourers = activeLabourers,
            onSave = { tractorId, tractorName, labourerIds, labourerNames, reason ->
                repository.editTrip(
                    tripId = editingTrip!!.id,
                    newTractorId = tractorId,
                    newTractorName = tractorName,
                    newLabourerIds = labourerIds,
                    newLabourerNames = labourerNames,
                    editReason = reason
                )
                editingTrip = null
            },
            onDismiss = { editingTrip = null }
        )
    }

    if (selectedTractorForDetail != null) {
        TractorDetailDialog(
            tractor = selectedTractorForDetail!!,
            isOwner = true,
            trips = trips,
            onToggleActive = { repository.toggleTractorStatus(selectedTractorForDetail!!.id) },
            onDismiss = { selectedTractorForDetail = null }
        )
    }

    if (showAddTripDialog) {
        AddTripDialog(
            repository = repository,
            onDismiss = { showAddTripDialog = false }
        )
    }

    if (showEmergencyDialog) {
        EmergencyAlertDialog(
            onSend = { title, msg, sev ->
                repository.sendEmergencyAlert(title, msg, sev)
                showEmergencyDialog = false
            },
            onDismiss = { showEmergencyDialog = false }
        )
    }

    if (showBroadcastDialog) {
        BroadcastDialog(
            onSend = { title, msg, target ->
                repository.sendBroadcastMessage(title, msg, target)
                showBroadcastDialog = false
            },
            onDismiss = { showBroadcastDialog = false }
        )
    }
}

@Composable
fun OwnerTripsScreen(
    repository: SandWorksRepository
) {
    val trips by repository.trips.collectAsState()
    val tractors by repository.tractors.collectAsState()
    val users by repository.users.collectAsState()
    val currentUser by repository.currentUser.collectAsState()
    var filterStatus by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var tripToVoid by remember { mutableStateOf<Trip?>(null) }
    var viewingTrip by remember { mutableStateOf<Trip?>(null) }
    var editingTrip by remember { mutableStateOf<Trip?>(null) }
    var voidReason by remember { mutableStateOf("") }

    val filteredTrips = remember(trips, filterStatus, searchQuery) {
        trips.filter { trip ->
            val matchesFilter = when (filterStatus) {
                "ACTIVE" -> trip.status == TripStatus.ACTIVE
                "VOIDED" -> trip.status == TripStatus.VOIDED
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                    trip.driverName.contains(searchQuery, ignoreCase = true) ||
                    trip.tractorName.contains(searchQuery, ignoreCase = true) ||
                    trip.tripNumber.toString().contains(searchQuery)
            matchesFilter && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search by Driver, Tractor or Trip #") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("trips_search_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = filterStatus == "ALL",
                onClick = { filterStatus = "ALL" },
                label = { Text("All (${trips.size})") }
            )
            FilterChip(
                selected = filterStatus == "ACTIVE",
                onClick = { filterStatus = "ACTIVE" },
                label = { Text("Active") }
            )
            FilterChip(
                selected = filterStatus == "VOIDED",
                onClick = { filterStatus = "VOIDED" },
                label = { Text("Voided") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredTrips) { trip ->
                TripRowItem(
                    trip = trip,
                    onVoidClick = if (trip.status == TripStatus.ACTIVE) {
                        { tripToVoid = trip }
                    } else null,
                    onClick = { viewingTrip = trip }
                )
            }
        }
    }

    if (viewingTrip != null) {
        TripDetailDialog(
            trip = viewingTrip!!,
            currentUser = currentUser,
            onDismiss = { viewingTrip = null },
            onEditTrip = { trip ->
                viewingTrip = null
                editingTrip = trip
            },
            onVoidTrip = { trip ->
                viewingTrip = null
                tripToVoid = trip
            }
        )
    }

    if (editingTrip != null) {
        val activeTractors = remember(tractors) { tractors.filter { it.isActive } }
        val activeLabourers = remember(users) { users.filter { it.role == Role.LABOURER && it.status == UserStatus.ACTIVE } }
        EditTripDialog(
            trip = editingTrip!!,
            tractors = activeTractors,
            labourers = activeLabourers,
            onSave = { tractorId, tractorName, labourerIds, labourerNames, reason ->
                repository.editTrip(
                    tripId = editingTrip!!.id,
                    newTractorId = tractorId,
                    newTractorName = tractorName,
                    newLabourerIds = labourerIds,
                    newLabourerNames = labourerNames,
                    editReason = reason
                )
                editingTrip = null
            },
            onDismiss = { editingTrip = null }
        )
    }

    if (tripToVoid != null) {
        AlertDialog(
            onDismissRequest = { tripToVoid = null },
            title = { Text("Void Trip #${tripToVoid?.tripNumber}") },
            text = {
                Column {
                    Text("Voiding will soft-delete this trip from accrual and closure computations. An audit record will be logged.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = voidReason,
                        onValueChange = { voidReason = it },
                        label = { Text("Reason for Voiding") },
                        modifier = Modifier.fillMaxWidth().testTag("void_reason_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        tripToVoid?.let { repository.voidTrip(it.id, voidReason) }
                        tripToVoid = null
                        voidReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SemanticError),
                    enabled = voidReason.isNotBlank(),
                    modifier = Modifier.testTag("confirm_void_button")
                ) {
                    Text("Confirm Void")
                }
            },
            dismissButton = {
                TextButton(onClick = { tripToVoid = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun TripRowItem(
    trip: Trip,
    onVoidClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (trip.status == TripStatus.VOIDED)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .testTag("trip_item_${trip.tripNumber}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Trip #${trip.tripNumber}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(trip.status.name)
                }
                Text(
                    text = MoneyEngine.formatPaise(trip.rateSnapshotPaise),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = BrandOrange
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Tractor: ${trip.tractorName} • Driver: ${trip.driverName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (trip.labourerNames.isNotEmpty()) {
                Text(
                    text = "Labourers: ${trip.labourerNames.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Labourers: None (Driver retains entire pool)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Shares breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Date: ${trip.date} • Snapshotted ₹200 rate",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (onVoidClick != null) {
                    TextButton(
                        onClick = onVoidClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = SemanticError),
                        modifier = Modifier.testTag("void_trip_button_${trip.tripNumber}")
                    ) {
                        Text("Void Trip")
                    }
                }
            }

            if (trip.status == TripStatus.VOIDED && trip.voidReason != null) {
                Text(
                    text = "Void Reason: ${trip.voidReason}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SemanticError
                )
            }
        }
    }
}

@Composable
fun OwnerPeopleScreen(repository: SandWorksRepository) {
    val users by repository.users.collectAsState()
    val tempAssignments by repository.tempAssignments.collectAsState()
    val tractors by repository.tractors.collectAsState()
    val trips by repository.trips.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var assigningLabourer by remember { mutableStateOf<User?>(null) }
    var selectedUserForDetail by remember { mutableStateOf<User?>(null) }

    val pendingUsers = remember(users) { users.filter { it.status == UserStatus.PENDING } }
    val activeDrivers = remember(users) { users.filter { it.role == Role.DRIVER && it.status == UserStatus.ACTIVE } }
    val activeLabourers = remember(users) { users.filter { it.role == Role.LABOURER && it.status == UserStatus.ACTIVE } }
    val activeTempAssignments = remember(tempAssignments) { tempAssignments.filter { it.status == TemporaryAssignmentStatus.ACTIVE } }
    val otherUsers = remember(users) { users.filter { it.status == UserStatus.REJECTED || it.status == UserStatus.SUSPENDED } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 0.dp) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Pending (${pendingUsers.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Drivers (${activeDrivers.size})") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Labourers (${activeLabourers.size})") }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Temp Drivers (${activeTempAssignments.size})") }
            )
            Tab(
                selected = selectedTab == 4,
                onClick = { selectedTab = 4 },
                text = { Text("Others (${otherUsers.size})") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> {
                if (pendingUsers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No pending account approvals.")
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(pendingUsers) { user ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedUserForDetail = user }
                                    .testTag("pending_user_${user.uid}")
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                            Text("Applied Role: ${user.role.name}", color = BrandOrange, fontWeight = FontWeight.SemiBold)
                                            Text("Email: ${user.email} • Phone: ${user.phone}", style = MaterialTheme.typography.bodySmall)
                                        }
                                        StatusBadge(user.status.name)
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Button(
                                            onClick = { repository.approveUser(user.uid) },
                                            colors = ButtonDefaults.buttonColors(containerColor = SemanticSuccess),
                                            modifier = Modifier.weight(1f).testTag("approve_button_${user.uid}")
                                        ) {
                                            Text("Approve")
                                        }
                                        OutlinedButton(
                                            onClick = { repository.rejectUser(user.uid) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SemanticError),
                                            modifier = Modifier.weight(1f).testTag("reject_button_${user.uid}")
                                        ) {
                                            Text("Reject")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(activeDrivers) { driver ->
                        UserCardItem(
                            user = driver,
                            onSuspend = { repository.updateUserStatus(driver.uid, UserStatus.SUSPENDED) },
                            onClick = { selectedUserForDetail = driver }
                        )
                    }
                }
            }
            2 -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(activeLabourers) { labourer ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedUserForDetail = labourer }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(labourer.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text(
                                            text = "LABOURER • ${labourer.phone.ifBlank { labourer.email }}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    StatusBadge(labourer.status.name)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { assigningLabourer = labourer },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.DriveEta, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Assign Temp Driver", fontSize = 12.sp)
                                    }
                                    TextButton(
                                        onClick = { repository.updateUserStatus(labourer.uid, UserStatus.SUSPENDED) }
                                    ) {
                                        Text("Suspend", color = SemanticError, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            3 -> {
                if (tempAssignments.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No temporary driver assignments recorded.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    val sdf = remember { SimpleDateFormat("HH:mm, dd MMM", Locale.getDefault()) }
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(tempAssignments) { assignment ->
                            val now = System.currentTimeMillis()
                            val isExpired = now > assignment.expiryTime
                            val displayStatus = when {
                                assignment.status == TemporaryAssignmentStatus.REVOKED -> "REVOKED"
                                isExpired -> "EXPIRED"
                                else -> "ACTIVE"
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(assignment.targetUserName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Text("Tractor: ${assignment.tractorName}", style = MaterialTheme.typography.bodySmall, color = BrandOrange)
                                            Text("Expires: ${sdf.format(Date(assignment.expiryTime))}", style = MaterialTheme.typography.bodySmall)
                                            if (assignment.reason.isNotBlank()) {
                                                Text("Reason: ${assignment.reason}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        StatusBadge(displayStatus)
                                    }
                                    if (assignment.status == TemporaryAssignmentStatus.ACTIVE && !isExpired) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = { repository.revokeTemporaryAccess(assignment.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = SemanticError),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Revoke Driver Access")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            4 -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(otherUsers) { user ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedUserForDetail = user }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(user.fullName, fontWeight = FontWeight.Bold)
                                    Text("${user.role} • ${user.email}", style = MaterialTheme.typography.bodySmall)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StatusBadge(user.status.name)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TextButton(onClick = { repository.updateUserStatus(user.uid, UserStatus.ACTIVE) }) {
                                        Text("Reactivate")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedUserForDetail != null) {
        UserDetailDialog(
            user = selectedUserForDetail!!,
            isOwner = true,
            trips = trips,
            onUpdateStatus = { newStatus ->
                repository.updateUserStatus(selectedUserForDetail!!.uid, newStatus)
                selectedUserForDetail = null
            },
            onDismiss = { selectedUserForDetail = null }
        )
    }

    assigningLabourer?.let { labourer ->
        var selectedTractor by remember { mutableStateOf(tractors.firstOrNull()) }
        var durationHours by remember { mutableStateOf(8) }
        var reasonText by remember { mutableStateOf("Seasonal driver assignment") }

        AlertDialog(
            onDismissRequest = { assigningLabourer = null },
            title = { Text("Assign as Temporary Driver") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Assigning: ${labourer.fullName}", fontWeight = FontWeight.Bold)

                    Text("Select Tractor:", style = MaterialTheme.typography.labelMedium)
                    tractors.forEach { tractor ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTractor = tractor }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedTractor?.id == tractor.id,
                                onClick = { selectedTractor = tractor }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(tractor.name, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    OutlinedTextField(
                        value = durationHours.toString(),
                        onValueChange = { durationHours = it.toIntOrNull() ?: 8 },
                        label = { Text("Duration (Hours)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        label = { Text("Reason for Assignment") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val tractor = selectedTractor ?: tractors.firstOrNull()
                        if (tractor != null) {
                            repository.grantTemporaryAccess(
                                targetUserId = labourer.uid,
                                targetUserName = labourer.fullName,
                                tractorId = tractor.id,
                                tractorName = tractor.name,
                                durationHours = durationHours,
                                reason = reasonText
                            )
                        }
                        assigningLabourer = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                ) {
                    Text("Grant Access")
                }
            },
            dismissButton = {
                TextButton(onClick = { assigningLabourer = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun UserCardItem(
    user: User,
    onSuspend: () -> Unit,
    onClick: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = "${user.role} • ${user.phone.ifBlank { user.email }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusBadge(user.status.name)
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onSuspend) {
                Icon(Icons.Default.Block, contentDescription = "Suspend", tint = SemanticError)
            }
        }
    }
}

@Composable
fun OwnerTractorsScreen(repository: SandWorksRepository) {
    val tractors by repository.tractors.collectAsState()
    val trips by repository.trips.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTractorForDetail by remember { mutableStateOf<Tractor?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Fleet Management", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                modifier = Modifier.testTag("add_tractor_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Tractor")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(tractors) { tractor ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedTractorForDetail = tractor }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(tractor.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                Text(tractor.registrationNumber, style = MaterialTheme.typography.bodyMedium, color = BrandSandGold)
                            }
                            StatusBadge(if (tractor.isActive) "ACTIVE" else "INACTIVE")
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Lifetime trips recorded: ${tractor.totalTrips}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(
                                onClick = { repository.toggleTractorStatus(tractor.id) },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = if (tractor.isActive) SemanticError else SemanticSuccess
                                )
                            ) {
                                Text(if (tractor.isActive) "Deactivate" else "Activate")
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedTractorForDetail != null) {
        TractorDetailDialog(
            tractor = selectedTractorForDetail!!,
            isOwner = true,
            trips = trips,
            onToggleActive = { repository.toggleTractorStatus(selectedTractorForDetail!!.id) },
            onDismiss = { selectedTractorForDetail = null }
        )
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var regNumber by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Tractor to Fleet") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Tractor Name (e.g. Sonalika)") },
                        modifier = Modifier.fillMaxWidth().testTag("tractor_name_input")
                    )
                    OutlinedTextField(
                        value = regNumber,
                        onValueChange = { regNumber = it },
                        label = { Text("Registration Number") },
                        modifier = Modifier.fillMaxWidth().testTag("tractor_reg_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.addTractor(name, regNumber)
                        showAddDialog = false
                    },
                    enabled = name.isNotBlank() && regNumber.isNotBlank(),
                    modifier = Modifier.testTag("submit_tractor_button")
                ) {
                    Text("Add Tractor")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun OwnerAccrualClosureScreen(repository: SandWorksRepository) {
    val closures by repository.closures.collectAsState()
    val trips by repository.trips.collectAsState()
    val currentRate by repository.currentTripRatePaise.collectAsState()

    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val today = remember { sdf.format(Date()) }
    var selectedDate by remember { mutableStateOf(today) }
    var closureResult by remember { mutableStateOf<DailyClosure?>(null) }
    var showRateConfigDialog by remember { mutableStateOf(false) }

    val tripsForDate = remember(trips, selectedDate) {
        trips.filter { it.date == selectedDate && it.status == TripStatus.ACTIVE }
    }
    val totalPoolForDate = remember(tripsForDate) {
        tripsForDate.sumOf { it.totalPoolPaise }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Daily Accrued-Money Summary", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
        Text(
            "Note: Displays accrued operational earnings in integer paise. Not a payment or disbursement.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Rate Configuration Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Base Trip Rate", style = MaterialTheme.typography.labelMedium)
                    Text(MoneyEngine.formatPaise(currentRate), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = BrandOrange)
                    Text("Immutable per-trip snapshot", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = { showRateConfigDialog = true },
                    modifier = Modifier.testTag("configure_rate_button")
                ) {
                    Text("Change Rate")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Date selection and Closure Action
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Date Selected: $selectedDate", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Active Trips: ${tripsForDate.size} • Total Pool: ${MoneyEngine.formatPaise(totalPoolForDate)}")
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        closureResult = repository.performDailyClosure(selectedDate)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("execute_closure_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                ) {
                    Text("Execute Daily Closure for $selectedDate")
                }
            }
        }

        if (closureResult != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SemanticSuccess.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, SemanticSuccess, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Closure Reconciled Successfully", fontWeight = FontWeight.Bold, color = SemanticSuccess)
                    Text("Total Trips: ${closureResult?.totalTrips} • Pool: ${MoneyEngine.formatPaise(closureResult?.totalPoolPaise ?: 0L)}")
                    Text("Summary Notice: ${closureResult?.summaryNotice}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Historical Closures", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(10.dp))

        if (closures.isEmpty()) {
            Text("No daily closures executed yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            closures.forEach { closure ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(closure.date, fontWeight = FontWeight.Bold)
                            Text(MoneyEngine.formatPaise(closure.totalPoolPaise), color = BrandOrange, fontWeight = FontWeight.Bold)
                        }
                        Text("${closure.totalTrips} trips closed • ${closure.summaryNotice}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    if (showRateConfigDialog) {
        var rateInputRupees by remember { mutableStateOf((currentRate / 100).toString()) }

        AlertDialog(
            onDismissRequest = { showRateConfigDialog = false },
            title = { Text("Configure Default Trip Rate") },
            text = {
                Column {
                    Text("Enter new trip rate in Rupees. Historical trips retain their immutable snapshot and will NOT be recomputed.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = rateInputRupees,
                        onValueChange = { rateInputRupees = it },
                        label = { Text("Trip Rate in ₹ (e.g. 200)") },
                        modifier = Modifier.fillMaxWidth().testTag("rate_rupees_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rupees = rateInputRupees.toLongOrNull() ?: 200L
                        repository.updateRatePaise(rupees * 100L)
                        showRateConfigDialog = false
                    },
                    modifier = Modifier.testTag("confirm_rate_button")
                ) {
                    Text("Save Rate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRateConfigDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// --- Dialogs ---

@Composable
fun AddTripDialog(
    repository: SandWorksRepository,
    onDismiss: () -> Unit
) {
    val tractors by repository.tractors.collectAsState()
    val users by repository.users.collectAsState()
    val currentRate by repository.currentTripRatePaise.collectAsState()

    val activeTractors = remember(tractors) { tractors.filter { it.isActive } }
    val activeDrivers = remember(users) { users.filter { it.role == Role.DRIVER && it.status == UserStatus.ACTIVE } }
    val activeLabourers = remember(users) { users.filter { it.role == Role.LABOURER && it.status == UserStatus.ACTIVE } }

    var selectedTractor by remember { mutableStateOf(activeTractors.firstOrNull()) }
    var selectedDriver by remember { mutableStateOf(activeDrivers.firstOrNull()) }
    val selectedLabourerIds = remember { mutableStateListOf<String>() }
    var isSubmitting by remember { mutableStateOf(false) }

    // Live distribution preview
    val previewShares = remember(currentRate, selectedDriver, selectedLabourerIds.toList()) {
        if (selectedDriver != null) {
            MoneyEngine.calculateDistribution(currentRate, selectedDriver!!.uid, selectedLabourerIds.toList())
        } else emptyMap()
    }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Log Operational Trip") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Immutable Rate Snapshot: ${MoneyEngine.formatPaise(currentRate)}", color = BrandOrange, fontWeight = FontWeight.Bold)

                Text("Select Tractor:", style = MaterialTheme.typography.labelMedium)
                activeTractors.forEach { tractor ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTractor = tractor }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedTractor?.id == tractor.id, onClick = { selectedTractor = tractor })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${tractor.name} (${tractor.registrationNumber})")
                    }
                }

                Text("Select Driver:", style = MaterialTheme.typography.labelMedium)
                if (activeDrivers.isEmpty()) {
                    Text("No active drivers found. Mansingh Rana or other drivers must be approved.", color = SemanticWarning, style = MaterialTheme.typography.bodySmall)
                } else {
                    activeDrivers.forEach { driver ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDriver = driver }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedDriver?.uid == driver.uid, onClick = { selectedDriver = driver })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(driver.fullName)
                        }
                    }
                }

                Text("Participating Labourers:", style = MaterialTheme.typography.labelMedium)
                if (activeLabourers.isEmpty()) {
                    Text("No active labourers. Driver will receive entire pool.", style = MaterialTheme.typography.bodySmall)
                } else {
                    activeLabourers.forEach { labourer ->
                        val isChecked = selectedLabourerIds.contains(labourer.uid)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) selectedLabourerIds.remove(labourer.uid)
                                    else selectedLabourerIds.add(labourer.uid)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) selectedLabourerIds.add(labourer.uid)
                                    else selectedLabourerIds.remove(labourer.uid)
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(labourer.fullName)
                        }
                    }
                }

                // Split Preview
                if (previewShares.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Live Paise Reconciled Split:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            previewShares.forEach { (uid, paise) ->
                                val name = if (uid == selectedDriver?.uid) "${selectedDriver?.fullName} (Driver)"
                                else activeLabourers.find { it.uid == uid }?.fullName ?: uid
                                Text("$name: ${MoneyEngine.formatPaise(paise)}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedTractor != null && selectedDriver != null && !isSubmitting) {
                        isSubmitting = true
                        val labourerNames = selectedLabourerIds.mapNotNull { id ->
                            activeLabourers.find { it.uid == id }?.fullName
                        }
                        repository.addTrip(
                            tractorId = selectedTractor!!.id,
                            tractorName = selectedTractor!!.name,
                            driverId = selectedDriver!!.uid,
                            driverName = selectedDriver!!.fullName,
                            labourerIds = selectedLabourerIds.toList(),
                            labourerNames = labourerNames
                        )
                        onDismiss()
                    }
                },
                enabled = selectedTractor != null && selectedDriver != null && !isSubmitting,
                modifier = Modifier.testTag("submit_add_trip_button"),
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
            ) {
                Text("Confirm Trip Log")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Cancel") }
        }
    )
}

@Composable
fun EmergencyAlertDialog(
    onSend: (String, String, AlertSeverity) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf(AlertSeverity.URGENT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Issue Urgent Emergency Alert", color = SemanticError) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Broadcasts an immediate operational emergency notice to all team members using Android-compliant high-priority notification channels.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Alert Headline") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("emergency_title_input")
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Detailed Instructions") },
                    modifier = Modifier.fillMaxWidth().testTag("emergency_message_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSend(title, message, severity) },
                enabled = title.isNotBlank() && message.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SemanticError),
                modifier = Modifier.testTag("send_emergency_button")
            ) {
                Text("Send Urgent Alert")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun BroadcastDialog(
    onSend: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var targetRole by remember { mutableStateOf("ALL") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Broadcast Team Message") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth().testTag("broadcast_title_input")
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message Body") },
                    modifier = Modifier.fillMaxWidth().testTag("broadcast_body_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSend(title, message, targetRole) },
                enabled = title.isNotBlank() && message.isNotBlank(),
                modifier = Modifier.testTag("send_broadcast_button")
            ) {
                Text("Send Broadcast")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
