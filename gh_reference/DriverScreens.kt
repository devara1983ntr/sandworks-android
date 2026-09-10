package com.roshan.sandworks.ui.screens

import android.content.Intent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roshan.sandworks.data.SandWorksRepository
import com.roshan.sandworks.domain.MoneyEngine
import com.roshan.sandworks.model.*
import com.roshan.sandworks.ui.components.MoneyCard
import com.roshan.sandworks.ui.components.StatusBadge
import com.roshan.sandworks.ui.theme.BrandOrange
import com.roshan.sandworks.ui.theme.BrandSandGold
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DriverDashboardScreen(
    repository: SandWorksRepository,
    currentDriver: User,
    onNavigate: (String) -> Unit
) {
    val trips by repository.trips.collectAsState()
    val tractors by repository.tractors.collectAsState()
    val users by repository.users.collectAsState()
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val todayDate = remember { sdf.format(Date()) }

    var viewingTrip by remember { mutableStateOf<Trip?>(null) }
    var editingTrip by remember { mutableStateOf<Trip?>(null) }

    val myTodayTrips = remember(trips, todayDate, currentDriver.uid) {
        trips.filter { it.driverId == currentDriver.uid && it.date == todayDate && it.status == TripStatus.ACTIVE }
    }
    val myTodayAccruedPaise = remember(myTodayTrips, currentDriver.uid) {
        myTodayTrips.sumOf { it.sharesPaise[currentDriver.uid] ?: 0L }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Welcome, ${currentDriver.fullName}",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Driver Console • Vehicle & Trip Logging",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MoneyCard(
                title = "Today's Accrued Earnings",
                paise = myTodayAccruedPaise,
                subtitle = "Accrued total (not payment)",
                modifier = Modifier.weight(1f).testTag("driver_accrued_card")
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Today's Trips", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${myTodayTrips.size}",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = BrandOrange
                    )
                    Text("Completed today", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { onNavigate("add_trip") },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("driver_log_trip_button"),
            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.AddCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log New Sand Trip (₹200 Snapshot)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Today's Trips", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))

        if (myTodayTrips.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No trips logged yet today.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            myTodayTrips.forEach { trip ->
                TripRowItem(
                    trip = trip,
                    onVoidClick = null,
                    onClick = { viewingTrip = trip }
                )
            }
        }
    }

    if (viewingTrip != null) {
        TripDetailDialog(
            trip = viewingTrip!!,
            currentUser = currentDriver,
            onDismiss = { viewingTrip = null },
            onEditTrip = { trip ->
                viewingTrip = null
                editingTrip = trip
            },
            onVoidTrip = { /* Drivers cannot void trips directly */ }
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
}

@Composable
fun DriverAddTripScreen(
    repository: SandWorksRepository,
    currentDriver: User,
    onTripAdded: () -> Unit
) {
    val tractors by repository.tractors.collectAsState()
    val users by repository.users.collectAsState()
    val currentRate by repository.currentTripRatePaise.collectAsState()

    val activeTractors = remember(tractors) { tractors.filter { it.isActive } }
    val activeLabourers = remember(users) { users.filter { it.role == Role.LABOURER && it.status == UserStatus.ACTIVE } }

    var selectedTractor by remember { mutableStateOf(activeTractors.firstOrNull()) }
    val selectedLabourerIds = remember { mutableStateListOf<String>() }
    var isSubmitting by remember { mutableStateOf(false) }

    val previewDistribution = remember(currentRate, currentDriver.uid, selectedLabourerIds.toList()) {
        MoneyEngine.calculateDistribution(currentRate, currentDriver.uid, selectedLabourerIds.toList())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Record New Trip",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = BrandOrange)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Per-Trip Rate Snapshot: ${MoneyEngine.formatPaise(currentRate)}", fontWeight = FontWeight.Bold)
                    Text("Fixed at trip recording time. Immutable in history.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Text("Select Tractor:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        activeTractors.forEach { tractor ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedTractor?.id == tractor.id) BrandOrange.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedTractor = tractor }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selectedTractor?.id == tractor.id, onClick = { selectedTractor = tractor })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(tractor.name, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("(${tractor.registrationNumber})", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Text("Select Participating Labourers:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        if (activeLabourers.isEmpty()) {
            Text("No active labourers currently available. Driver receives full ₹200 snapshot.", style = MaterialTheme.typography.bodySmall)
        } else {
            activeLabourers.forEach { labourer ->
                val isChecked = selectedLabourerIds.contains(labourer.uid)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isChecked) BrandSandGold.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isChecked) selectedLabourerIds.remove(labourer.uid)
                            else selectedLabourerIds.add(labourer.uid)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                if (checked) selectedLabourerIds.add(labourer.uid)
                                else selectedLabourerIds.remove(labourer.uid)
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(labourer.fullName, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Live Reconciled Split
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Projected Accrual Split:", fontWeight = FontWeight.Bold, color = BrandSandGold)
                previewDistribution.forEach { (uid, sharePaise) ->
                    val name = if (uid == currentDriver.uid) "${currentDriver.fullName} (Driver)"
                    else activeLabourers.find { it.uid == uid }?.fullName ?: uid
                    Text("$name: ${MoneyEngine.formatPaise(sharePaise)}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Button(
            onClick = {
                if (selectedTractor != null && !isSubmitting) {
                    isSubmitting = true
                    val labourerNames = selectedLabourerIds.mapNotNull { id ->
                        activeLabourers.find { it.uid == id }?.fullName
                    }
                    repository.addTrip(
                        tractorId = selectedTractor!!.id,
                        tractorName = selectedTractor!!.name,
                        driverId = currentDriver.uid,
                        driverName = currentDriver.fullName,
                        labourerIds = selectedLabourerIds.toList(),
                        labourerNames = labourerNames
                    )
                    onTripAdded()
                }
            },
            enabled = selectedTractor != null && !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("driver_submit_trip_button"),
            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Record Trip", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DriverTripsHistoryScreen(
    repository: SandWorksRepository,
    currentDriver: User
) {
    val trips by repository.trips.collectAsState()
    val myTrips = remember(trips, currentDriver.uid) {
        trips.filter { it.driverId == currentDriver.uid }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "My Logged Trips History (${myTrips.size})",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (myTrips.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No past trips found.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(myTrips) { trip ->
                    TripRowItem(trip = trip, onVoidClick = null)
                }
            }
        }
    }
}

@Composable
fun DriverAccruedScreen(
    repository: SandWorksRepository,
    currentDriver: User
) {
    val trips by repository.trips.collectAsState()
    val myTrips = remember(trips, currentDriver.uid) {
        trips.filter { it.driverId == currentDriver.uid && it.status == TripStatus.ACTIVE }
    }

    val totalLifetimeAccrued = remember(myTrips, currentDriver.uid) {
        myTrips.sumOf { it.sharesPaise[currentDriver.uid] ?: 0L }
    }

    // Group by date
    val byDate = remember(myTrips, currentDriver.uid) {
        myTrips.groupBy { it.date }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Daily Accrued-Money Summary", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
        Text(
            "Accrued operational earnings in integer paise. Note: This indicates accumulated operational value, not a paid disbursement.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        MoneyCard(
            title = "Total Lifetime Accrued",
            paise = totalLifetimeAccrued,
            subtitle = "From ${myTrips.size} trips completed"
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Accrual Breakdown by Date", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(10.dp))

        byDate.forEach { (date, tripsOnDate) ->
            val dailyPaise = tripsOnDate.sumOf { it.sharesPaise[currentDriver.uid] ?: 0L }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(date, fontWeight = FontWeight.Bold)
                        Text("${tripsOnDate.size} trips recorded", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        MoneyEngine.formatPaise(dailyPaise),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BrandOrange
                    )
                }
            }
        }
    }
}

@Composable
fun DriverShareScreen(
    repository: SandWorksRepository,
    currentDriver: User
) {
    val context = LocalContext.current
    val trips by repository.trips.collectAsState()
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val todayDate = remember { sdf.format(Date()) }

    val myTodayTrips = remember(trips, todayDate, currentDriver.uid) {
        trips.filter { it.driverId == currentDriver.uid && it.date == todayDate && it.status == TripStatus.ACTIVE }
    }
    val myTodayAccrued = remember(myTodayTrips, currentDriver.uid) {
        myTodayTrips.sumOf { it.sharesPaise[currentDriver.uid] ?: 0L }
    }

    val shareText = remember(myTodayTrips, myTodayAccrued, currentDriver.fullName, todayDate) {
        buildString {
            append("SAND WORKS - Daily Trip Summary\n")
            append("Driver: ${currentDriver.fullName}\n")
            append("Date: $todayDate\n")
            append("Total Trips: ${myTodayTrips.size}\n")
            append("Accrued Earnings: ${MoneyEngine.formatPaise(myTodayAccrued)}\n\n")
            myTodayTrips.forEachIndexed { idx, trip ->
                append("${idx + 1}. Trip #${trip.tripNumber} - ${trip.tractorName} (Rate: ${MoneyEngine.formatPaise(trip.rateSnapshotPaise)})\n")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Share Today's Trips", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Generate a summary to share with owner Ramesh Sahu.", color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            Text(
                text = shareText,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Share Daily Summary")
                context.startActivity(shareIntent)
            },
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("share_summary_button"),
            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Share Summary")
        }
    }
}
