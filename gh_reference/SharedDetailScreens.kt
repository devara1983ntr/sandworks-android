package com.roshan.sandworks.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.roshan.sandworks.R
import com.roshan.sandworks.data.SandWorksRepository
import com.roshan.sandworks.domain.MoneyEngine
import com.roshan.sandworks.model.*
import com.roshan.sandworks.ui.components.StatusBadge
import com.roshan.sandworks.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TripDetailDialog(
    trip: Trip,
    currentUser: User?,
    onDismiss: () -> Unit,
    onEditTrip: (Trip) -> Unit,
    onVoidTrip: (Trip) -> Unit
) {
    val isOwner = currentUser?.role == Role.OWNER
    val isLoggingDriver = currentUser?.role == Role.DRIVER && trip.driverId == currentUser.uid
    val canEdit = (isOwner || isLoggingDriver) && trip.status == TripStatus.ACTIVE

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("trip_detail_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Trip #${trip.tripNumber}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${trip.date} • Rev ${trip.revision}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusBadge(trip.status.name)
                }

                if (trip.status == TripStatus.VOIDED) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SemanticError.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("VOIDED TRIP", fontWeight = FontWeight.Bold, color = SemanticError, fontSize = 12.sp)
                            Text("Reason: ${trip.voidReason ?: "Unspecified"}", style = MaterialTheme.typography.bodySmall, color = SemanticError)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // Tractor & Driver Info
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Agriculture, contentDescription = null, tint = BrandSandGold, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Tractor Registry", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(trip.tractorName, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Driver", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(trip.driverName, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Labourers
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Group, contentDescription = null, tint = BrandSandGold, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Participating Labourers (${trip.labourerNames.size})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (trip.labourerNames.isEmpty()) {
                            Text("None", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Text(trip.labourerNames.joinToString(", "), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // Financial Distribution (Integer Paise)
                Text("Authoritative Accrual Breakdown", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Rate Snapshot:", style = MaterialTheme.typography.bodySmall)
                            Text(MoneyEngine.formatPaise(trip.rateSnapshotPaise), fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Pool:", style = MaterialTheme.typography.bodySmall)
                            Text(MoneyEngine.formatPaise(trip.totalPoolPaise), fontWeight = FontWeight.Bold, color = BrandOrange)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(6.dp))

                        trip.sharesPaise.forEach { (uid, sharePaise) ->
                            val name = if (uid == trip.driverId) trip.driverName + " (Driver)" else {
                                val idx = trip.labourerIds.indexOf(uid)
                                if (idx >= 0) trip.labourerNames.getOrNull(idx) ?: "Labourer" else "Labourer"
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(name, style = MaterialTheme.typography.bodySmall)
                                Text(MoneyEngine.formatPaise(sharePaise), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).testTag("dialog_close_button")
                    ) {
                        Text("Close")
                    }

                    if (canEdit) {
                        Button(
                            onClick = {
                                onDismiss()
                                onEditTrip(trip)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                            modifier = Modifier.weight(1f).testTag("dialog_edit_trip_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit")
                        }
                    }

                    if (isOwner && trip.status == TripStatus.ACTIVE) {
                        Button(
                            onClick = {
                                onDismiss()
                                onVoidTrip(trip)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SemanticError),
                            modifier = Modifier.weight(1f).testTag("dialog_void_trip_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Void")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditTripDialog(
    trip: Trip,
    tractors: List<Tractor>,
    labourers: List<User>,
    onSave: (String, String, List<String>, List<String>, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTractorId by remember { mutableStateOf(trip.tractorId) }
    var selectedTractorName by remember { mutableStateOf(trip.tractorName) }
    val selectedLabourerIds = remember { mutableStateListOf<String>().apply { addAll(trip.labourerIds) } }
    var editReason by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("edit_trip_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Edit Trip #${trip.tripNumber}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Rate Snapshot: ${MoneyEngine.formatPaise(trip.rateSnapshotPaise)} (Immutable)",
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandOrange
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage != null) {
                    Text(errorMessage!!, color = SemanticError, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Tractor Selection
                Text("Select Tractor", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                tractors.filter { it.isActive }.forEach { tr ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTractorId = tr.id
                                selectedTractorName = tr.name
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedTractorId == tr.id,
                            onClick = {
                                selectedTractorId = tr.id
                                selectedTractorName = tr.name
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${tr.name} (${tr.registrationNumber})")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Labourer Selection
                Text("Participating Labourers", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                labourers.filter { it.role == Role.LABOURER && it.status == UserStatus.ACTIVE }.forEach { lb ->
                    val isChecked = selectedLabourerIds.contains(lb.uid)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) selectedLabourerIds.remove(lb.uid)
                                else selectedLabourerIds.add(lb.uid)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                if (checked) selectedLabourerIds.add(lb.uid)
                                else selectedLabourerIds.remove(lb.uid)
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(lb.fullName)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Edit Reason (Mandatory)
                OutlinedTextField(
                    value = editReason,
                    onValueChange = { editReason = it },
                    label = { Text("Reason for edit (required)") },
                    placeholder = { Text("e.g. Corrected tractor assignment") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_reason_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (editReason.isBlank()) {
                                errorMessage = "Reason is required to update trip"
                                return@Button
                            }
                            val activeLabourers = labourers.filter { selectedLabourerIds.contains(it.uid) }
                            val names = activeLabourers.map { it.fullName }
                            onSave(selectedTractorId, selectedTractorName, selectedLabourerIds.toList(), names, editReason.trim())
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                        modifier = Modifier.weight(1f).testTag("edit_save_button")
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}

@Composable
fun UserDetailDialog(
    user: User,
    isOwner: Boolean,
    trips: List<Trip>,
    onUpdateStatus: (UserStatus) -> Unit,
    onDismiss: () -> Unit
) {
    val userTrips = remember(trips, user.uid) {
        trips.filter { t ->
            t.status == TripStatus.ACTIVE && (t.driverId == user.uid || t.labourerIds.contains(user.uid))
        }
    }
    val totalAccrued = remember(userTrips, user.uid) {
        userTrips.sumOf { it.sharesPaise[user.uid] ?: 0L }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("user_detail_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(user.fullName, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        Text("ROLE: ${user.role.name}", color = BrandOrange, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    StatusBadge(user.status.name)
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = BrandSandGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(if (user.phone.isNotBlank()) user.phone else "No phone recorded", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = BrandSandGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(user.email, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lifetime Metrics
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Operational Metrics", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Active Trips Participated:", style = MaterialTheme.typography.bodySmall)
                            Text("${userTrips.size}", fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Accrued Earnings:", style = MaterialTheme.typography.bodySmall)
                            Text(MoneyEngine.formatPaise(totalAccrued), fontWeight = FontWeight.Bold, color = BrandOrange)
                        }
                    }
                }

                if (isOwner && user.role != Role.OWNER) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Owner Controls", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (user.status == UserStatus.PENDING) {
                            Button(
                                onClick = {
                                    onUpdateStatus(UserStatus.ACTIVE)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SemanticSuccess),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Approve")
                            }
                            Button(
                                onClick = {
                                    onUpdateStatus(UserStatus.REJECTED)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SemanticError),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reject")
                            }
                        } else if (user.status == UserStatus.ACTIVE) {
                            Button(
                                onClick = {
                                    onUpdateStatus(UserStatus.SUSPENDED)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SemanticWarning),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Suspend")
                            }
                        } else {
                            Button(
                                onClick = {
                                    onUpdateStatus(UserStatus.ACTIVE)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SemanticSuccess),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reactivate")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun TractorDetailDialog(
    tractor: Tractor,
    isOwner: Boolean,
    trips: List<Trip>,
    onToggleActive: () -> Unit,
    onDismiss: () -> Unit
) {
    val tractorTrips = remember(trips, tractor.id) {
        trips.filter { it.tractorId == tractor.id }
    }
    val activeTrips = remember(tractorTrips) {
        tractorTrips.filter { it.status == TripStatus.ACTIVE }
    }
    val totalPool = remember(activeTrips) {
        activeTrips.sumOf { it.totalPoolPaise }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("tractor_detail_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(tractor.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        Text(tractor.registrationNumber, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    StatusBadge(if (tractor.isActive) "ACTIVE" else "INACTIVE")
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Fleet Performance", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Trips Recorded:", style = MaterialTheme.typography.bodySmall)
                            Text("${tractorTrips.size}", fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Active Non-Voided:", style = MaterialTheme.typography.bodySmall)
                            Text("${activeTrips.size}", fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Accrued Pool:", style = MaterialTheme.typography.bodySmall)
                            Text(MoneyEngine.formatPaise(totalPool), fontWeight = FontWeight.Bold, color = BrandOrange)
                        }
                    }
                }

                if (isOwner) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            onToggleActive()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (tractor.isActive) SemanticWarning else SemanticSuccess
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (tractor.isActive) "Deactivate Tractor" else "Activate Tractor")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun NotificationCenterDialog(
    notifications: List<AppNotification>,
    onMarkRead: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredList = remember(notifications, selectedFilter) {
        when (selectedFilter) {
            "ACCRUALS" -> notifications.filter { it.type == NotificationType.ACCRUAL_SUMMARY }
            "ALERTS" -> notifications.filter { it.type == NotificationType.EMERGENCY_ALERT }
            "SYSTEM" -> notifications.filter { it.type != NotificationType.ACCRUAL_SUMMARY && it.type != NotificationType.EMERGENCY_ALERT }
            else -> notifications
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
                .padding(8.dp)
                .testTag("notification_center_dialog")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = BrandOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Notifications", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    }
                    TextButton(onClick = onMarkAllRead) {
                        Text("Mark all read", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("All (${notifications.size})", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = selectedFilter == "ACCRUALS",
                        onClick = { selectedFilter = "ACCRUALS" },
                        label = { Text("Accruals", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = selectedFilter == "ALERTS",
                        onClick = { selectedFilter = "ALERTS" },
                        label = { Text("Alerts", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = selectedFilter == "SYSTEM",
                        onClick = { selectedFilter = "SYSTEM" },
                        label = { Text("System", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No notifications in this category", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredList) { notif ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (!notif.isRead) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onMarkRead(notif.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    val icon = when (notif.type) {
                                        NotificationType.ACCRUAL_SUMMARY -> Icons.Default.CurrencyRupee
                                        NotificationType.EMERGENCY_ALERT -> Icons.Default.Warning
                                        NotificationType.APPROVAL_OUTCOME -> Icons.Default.CheckCircle
                                        NotificationType.TRIP_ASSIGNMENT -> Icons.Default.LocalShipping
                                        NotificationType.EXPIRY_WARNING -> Icons.Default.Timer
                                        NotificationType.SYSTEM_ACCOUNT -> Icons.Default.Info
                                    }
                                    val iconTint = if (notif.type == NotificationType.EMERGENCY_ALERT) SemanticError else BrandOrange
                                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            if (!notif.isRead) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(BrandOrange)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(notif.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(notif.timestamp)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun HelpAboutDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("help_about_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sand_works_logo),
                    contentDescription = "SAND WORKS Master Logo",
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("SAND WORKS", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black), color = BrandOrange)
                Text("Version 1.0.0 (Production Build)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Private Enterprise Console",
                    fontWeight = FontWeight.Bold,
                    color = BrandSandGold,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Owned and operated exclusively by Ramesh Sahu. Driver Mansingh Rana & operational team.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("• Strict integer paise precision (Zero float math)", style = MaterialTheme.typography.bodySmall)
                        Text("• ₹200 trip rate snapshot per recorded trip", style = MaterialTheme.typography.bodySmall)
                        Text("• 19:30 IST daily accrued-money closure", style = MaterialTheme.typography.bodySmall)
                        Text("• Single Owner, Driver & Labourer roles (No Admin)", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(
    user: User,
    onDismiss: () -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var soundAlertsEnabled by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = BrandOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Settings", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (user.role == Role.OWNER) {
                    Text("Operational Schedule", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Daily Closure Time: 19:30 Asia/Kolkata (IST)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Text("Trips recorded before 19:30 are aggregated into the daily accrued-money ledger.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text("Alert Preferences", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Push Notifications", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Vibration for High-Alerts", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = vibrationEnabled, onCheckedChange = { vibrationEnabled = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Audio Sound Prompt", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = soundAlertsEnabled, onCheckedChange = { soundAlertsEnabled = it })
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Diagnostics & Monitoring", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SemanticSuccess, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Firebase Analytics: Active", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SemanticSuccess, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Crashlytics SDK: Initialized", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SemanticSuccess, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Performance Monitoring: Active", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = {
                                // Diagnostic crash trigger for Firebase Crashlytics validation
                                throw RuntimeException("Test Crash - SandWorks Verification")
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SemanticError),
                            modifier = Modifier.fillMaxWidth().testTag("test_crash_button")
                        ) {
                            Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(16.dp), tint = SemanticError)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Trigger Test Crash (Crashlytics)", color = SemanticError)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Save & Close")
                }
            }
        }
    }
}

@Composable
fun ForgotPasswordDialog(
    onSendReset: (String, (Boolean, String?) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("forgot_password_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Reset Password", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Enter your account email address to receive password reset instructions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (statusMessage != null) {
                    Text(
                        statusMessage!!,
                        color = if (isSuccess) SemanticSuccess else SemanticError,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("forgot_email_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (email.isBlank()) {
                                statusMessage = "Email is required"
                                isSuccess = false
                                return@Button
                            }
                            isLoading = true
                            onSendReset(email.trim()) { success, msg ->
                                isLoading = false
                                isSuccess = success
                                statusMessage = msg ?: if (success) "Reset email sent" else "Failed to send reset email"
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                        modifier = Modifier.weight(1f).testTag("forgot_submit_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Text("Send Link")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogoutConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Logout, contentDescription = null, tint = BrandOrange) },
        title = { Text("Confirm Sign Out") },
        text = { Text("Are you sure you want to sign out from the SAND WORKS console?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = SemanticError),
                modifier = Modifier.testTag("confirm_sign_out_button")
            ) {
                Text("Sign Out")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
