package com.roshan.sandworks.ui.screens

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.roshan.sandworks.data.SandWorksRepository
import com.roshan.sandworks.domain.MoneyEngine
import com.roshan.sandworks.model.*
import com.roshan.sandworks.ui.components.StatusBadge
import com.roshan.sandworks.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OwnerExportScreen(repository: SandWorksRepository) {
    val context = LocalContext.current
    var selectedRange by remember { mutableStateOf("Last 7 Days") }
    var selectedBreakdown by remember { mutableStateOf("Overall Summary") }
    var selectedFormat by remember { mutableStateOf("Plaintext / Report") }
    var generatedContent by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("owner_export_screen")
    ) {
        Text(
            text = "Export Center",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Generate and share audit-ready accrued operational records in CSV or plaintext reports.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Range
                Text("Date Range", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Today", "Last 7 Days", "Last 30 Days", "All Time").forEach { r ->
                        FilterChip(
                            selected = selectedRange == r,
                            onClick = { selectedRange = r },
                            label = { Text(r, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Breakdown
                Text("Aggregation Breakdown", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Overall Summary", "By Tractor", "By Driver", "By Labourer").forEach { b ->
                        FilterChip(
                            selected = selectedBreakdown == b,
                            onClick = { selectedBreakdown = b },
                            label = { Text(b, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Format
                Text("Export Format", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedFormat == "Plaintext / Report",
                        onClick = { selectedFormat = "Plaintext / Report" },
                        label = { Text("Report / Plaintext", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = selectedFormat == "CSV",
                        onClick = { selectedFormat = "CSV" },
                        label = { Text("CSV (Spreadsheet)", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isGenerating = true
                        val formatKey = if (selectedFormat.contains("CSV")) "CSV" else "TEXT"
                        generatedContent = repository.generateExport(selectedRange, selectedBreakdown, formatKey)
                        isGenerating = false
                    },
                    modifier = Modifier.fillMaxWidth().testTag("generate_export_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Authoritative Export")
                }
            }
        }

        if (generatedContent != null) {
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Generated Output", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                Button(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, generatedContent)
                            type = if (selectedFormat.contains("CSV")) "text/comma-separated-values" else "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share SAND WORKS Export")
                        context.startActivity(shareIntent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SemanticSuccess),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("share_export_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share / Export")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = generatedContent!!,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun OwnerAttendanceScreen(repository: SandWorksRepository) {
    val users by repository.users.collectAsState()
    val attendance by repository.attendance.collectAsState()
    val trips by repository.trips.collectAsState()

    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var selectedDate by remember { mutableStateOf(sdf.format(Date())) }
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("ALL") }

    // Dialog for correction / marking with reason
    var markingTargetUser by remember { mutableStateOf<User?>(null) }
    var targetAttendanceStatus by remember { mutableStateOf<AttendanceStatus?>(null) }
    var mandatoryReason by remember { mutableStateOf("") }
    var reasonError by remember { mutableStateOf<String?>(null) }

    val labourers = remember(users) {
        users.filter { it.role == Role.LABOURER && it.status == UserStatus.ACTIVE }
    }

    val activeTripsOnDate = remember(trips, selectedDate) {
        trips.filter { it.date == selectedDate && it.status == TripStatus.ACTIVE }
    }

    val attendanceForDate = remember(attendance, selectedDate) {
        attendance.filter { it.date == selectedDate }
    }

    val presentCount = labourers.count { lb ->
        val record = attendanceForDate.find { it.userId == lb.uid }
        val inTrips = activeTripsOnDate.any { it.labourerIds.contains(lb.uid) }
        record?.status == AttendanceStatus.PRESENT || (record == null && inTrips)
    }
    val absentCount = labourers.size - presentCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("owner_attendance_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Attendance Registry", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                Text("Date: $selectedDate", color = BrandOrange, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }

            // Quick date toggle
            Row {
                IconButton(onClick = {
                    val cal = Calendar.getInstance().apply { time = sdf.parse(selectedDate) ?: Date(); add(Calendar.DATE, -1) }
                    selectedDate = sdf.format(cal.time)
                }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                }
                IconButton(onClick = {
                    val cal = Calendar.getInstance().apply { time = sdf.parse(selectedDate) ?: Date(); add(Calendar.DATE, 1) }
                    selectedDate = sdf.format(cal.time)
                }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Summary Card
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SemanticSuccess.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("WORKING TODAY", fontWeight = FontWeight.Bold, color = SemanticSuccess, fontSize = 11.sp)
                    Text("$presentCount", fontWeight = FontWeight.Black, fontSize = 22.sp, color = SemanticSuccess)
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = SemanticError.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ABSENT TODAY", fontWeight = FontWeight.Bold, color = SemanticError, fontSize = 11.sp)
                    Text("$absentCount", fontWeight = FontWeight.Black, fontSize = 22.sp, color = SemanticError)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search & Filter
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search labourer by name...") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(
                selected = statusFilter == "ALL",
                onClick = { statusFilter = "ALL" },
                label = { Text("All (${labourers.size})", fontSize = 12.sp) }
            )
            FilterChip(
                selected = statusFilter == "PRESENT",
                onClick = { statusFilter = "PRESENT" },
                label = { Text("Working ($presentCount)", fontSize = 12.sp) }
            )
            FilterChip(
                selected = statusFilter == "ABSENT",
                onClick = { statusFilter = "ABSENT" },
                label = { Text("Absent ($absentCount)", fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val filteredLabourers = remember(labourers, searchQuery, statusFilter, attendanceForDate, activeTripsOnDate) {
            labourers.filter { lb ->
                val matchesSearch = lb.fullName.contains(searchQuery, ignoreCase = true)
                val record = attendanceForDate.find { it.userId == lb.uid }
                val inTrips = activeTripsOnDate.any { it.labourerIds.contains(lb.uid) }
                val isPresent = record?.status == AttendanceStatus.PRESENT || (record == null && inTrips)

                val matchesStatus = when (statusFilter) {
                    "PRESENT" -> isPresent
                    "ABSENT" -> !isPresent
                    else -> true
                }
                matchesSearch && matchesStatus
            }
        }

        if (filteredLabourers.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("No matching labourers for $selectedDate", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredLabourers) { lb ->
                    val record = attendanceForDate.find { it.userId == lb.uid }
                    val tripsParticipated = activeTripsOnDate.count { it.labourerIds.contains(lb.uid) }
                    val isPresent = record?.status == AttendanceStatus.PRESENT || (record == null && tripsParticipated > 0)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(lb.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                if (tripsParticipated > 0) {
                                    Text("Logged on $tripsParticipated trip(s) today", style = MaterialTheme.typography.bodySmall, color = BrandOrange)
                                }
                                if (record?.reason?.isNotBlank() == true) {
                                    Text("Note: ${record.reason}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = {
                                        markingTargetUser = lb
                                        targetAttendanceStatus = AttendanceStatus.PRESENT
                                        mandatoryReason = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isPresent) SemanticSuccess else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("Working", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        markingTargetUser = lb
                                        targetAttendanceStatus = AttendanceStatus.ABSENT
                                        mandatoryReason = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (!isPresent) SemanticError else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("Absent", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Mandatory Reason Dialog when modifying/marking attendance
    if (markingTargetUser != null && targetAttendanceStatus != null) {
        Dialog(onDismissRequest = { markingTargetUser = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Confirm Attendance Mark", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Marking ${markingTargetUser?.fullName} as ${targetAttendanceStatus?.name} for $selectedDate.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (reasonError != null) {
                        Text(reasonError!!, color = SemanticError, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    OutlinedTextField(
                        value = mandatoryReason,
                        onValueChange = { mandatoryReason = it },
                        label = { Text("Reason (Required for audit)") },
                        placeholder = { Text("e.g. In field working, or informed absence") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { markingTargetUser = null }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (mandatoryReason.isBlank()) {
                                    reasonError = "Reason is required to log attendance change"
                                    return@Button
                                }
                                repository.markAttendance(
                                    userId = markingTargetUser!!.uid,
                                    userName = markingTargetUser!!.fullName,
                                    userRole = markingTargetUser!!.role,
                                    date = selectedDate,
                                    status = targetAttendanceStatus!!,
                                    reason = mandatoryReason.trim()
                                )
                                markingTargetUser = null
                                targetAttendanceStatus = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerAuditScreen(repository: SandWorksRepository) {
    val auditLogs by repository.auditLogs.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedActionFilter by remember { mutableStateOf("ALL") }

    val actionsList = listOf("ALL", "ADD_TRIP", "VOID_TRIP", "EDIT_TRIP", "DAILY_CLOSURE", "GRANT_TEMP_ACCESS", "EMERGENCY_ALERT")

    val filteredLogs = remember(auditLogs, searchQuery, selectedActionFilter) {
        auditLogs.filter { log ->
            val matchesAction = selectedActionFilter == "ALL" || log.action == selectedActionFilter
            val matchesQuery = log.details.contains(searchQuery, ignoreCase = true) ||
                    log.userName.contains(searchQuery, ignoreCase = true) ||
                    log.action.contains(searchQuery, ignoreCase = true)
            matchesAction && matchesQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("owner_audit_screen")
    ) {
        Text("Authoritative Audit Log", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
        Text("Tamper-evident activity trail for compliance and operations.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search logs by user, action, or details...") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            actionsList.take(4).forEach { act ->
                FilterChip(
                    selected = selectedActionFilter == act,
                    onClick = { selectedActionFilter = act },
                    label = { Text(act, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("No audit records matching query", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredLogs) { log ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusBadge(log.action)
                                Text(
                                    SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(log.details, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("By: ${log.userName}", style = MaterialTheme.typography.labelSmall, color = BrandSandGold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerTemporaryAccessScreen(repository: SandWorksRepository) {
    val tempAssignments by repository.tempAssignments.collectAsState()
    val users by repository.users.collectAsState()
    val tractors by repository.tractors.collectAsState()

    var showGrantDialog by remember { mutableStateOf(false) }

    val labourers = remember(users) {
        users.filter { it.role == Role.LABOURER && it.status == UserStatus.ACTIVE }
    }
    val activeTractors = remember(tractors) {
        tractors.filter { it.isActive }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("owner_temp_access_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Temporary Driver Access", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                Text("Grant time-bounded driver authority to labourers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
                onClick = { showGrantDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("grant_temp_access_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Grant")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (tempAssignments.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("No temporary assignments created.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tempAssignments) { assignment ->
                    val now = System.currentTimeMillis()
                    val isStillValid = assignment.status == TemporaryAssignmentStatus.ACTIVE && assignment.expiryTime > now

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
                                Text(assignment.targetUserName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                StatusBadge(if (isStillValid) "ACTIVE" else assignment.status.name)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Tractor: ${assignment.tractorName}", style = MaterialTheme.typography.bodySmall)
                            Text("Reason: ${assignment.reason}", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(4.dp))

                            val expiryStr = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(assignment.expiryTime))
                            Text("Expires: $expiryStr", style = MaterialTheme.typography.labelSmall, color = if (isStillValid) BrandOrange else SemanticError)

                            if (isStillValid) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = { repository.revokeTemporaryAccess(assignment.id) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SemanticError),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Revoke Access")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGrantDialog) {
        GrantTemporaryAccessDialog(
            labourers = labourers,
            tractors = activeTractors,
            onGrant = { labourer, tractor, durationHours, reason ->
                repository.grantTemporaryAccess(
                    targetUserId = labourer.uid,
                    targetUserName = labourer.fullName,
                    tractorId = tractor.id,
                    tractorName = tractor.name,
                    durationHours = durationHours,
                    reason = reason
                )
                showGrantDialog = false
            },
            onDismiss = { showGrantDialog = false }
        )
    }
}

@Composable
fun GrantTemporaryAccessDialog(
    labourers: List<User>,
    tractors: List<Tractor>,
    onGrant: (User, Tractor, Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedLabourer by remember { mutableStateOf(labourers.firstOrNull()) }
    var selectedTractor by remember { mutableStateOf(tractors.firstOrNull()) }
    var selectedDurationHours by remember { mutableStateOf(8) }
    var reason by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Grant Temporary Driver Access", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Allows a labourer to operate as Driver for a strictly bounded time window.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (errorMsg != null) {
                    Text(errorMsg!!, color = SemanticError, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Select Labourer
                Text("Select Labourer", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                labourers.forEach { lb ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedLabourer = lb }
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedLabourer?.uid == lb.uid, onClick = { selectedLabourer = lb })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(lb.fullName)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Select Tractor
                Text("Select Tractor", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                tractors.forEach { tr ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTractor = tr }
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedTractor?.id == tr.id, onClick = { selectedTractor = tr })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${tr.name} (${tr.registrationNumber})")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Duration
                Text("Access Window", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(Pair("2 Hours", 2), Pair("4 Hours", 4), Pair("8 Hours", 8), Pair("Full Day", 24)).forEach { (label, hrs) ->
                        FilterChip(
                            selected = selectedDurationHours == hrs,
                            onClick = { selectedDurationHours = hrs },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Reason
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason (Required)") },
                    placeholder = { Text("e.g. Regular driver Mansingh Rana on leave") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (selectedLabourer == null || selectedTractor == null) {
                                errorMsg = "Labourer and Tractor must be chosen"
                                return@Button
                            }
                            if (reason.isBlank()) {
                                errorMsg = "Reason is mandatory"
                                return@Button
                            }
                            onGrant(selectedLabourer!!, selectedTractor!!, selectedDurationHours, reason.trim())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Grant Access")
                    }
                }
            }
        }
    }
}
