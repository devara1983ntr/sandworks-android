package com.roshan.sandworks.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.roshan.sandworks.data.SandWorksRepository
import com.roshan.sandworks.domain.MoneyEngine
import com.roshan.sandworks.model.*
import com.roshan.sandworks.ui.components.MoneyCard
import com.roshan.sandworks.ui.components.StatusBadge
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.unit.sp
import com.roshan.sandworks.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LabourerDashboardScreen(
    repository: SandWorksRepository,
    currentLabourer: User
) {
    val trips by repository.trips.collectAsState()
    val attendance by repository.attendance.collectAsState()

    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val todayDate = remember { sdf.format(Date()) }

    val todayAttendance = remember(attendance, todayDate, currentLabourer.uid) {
        attendance.find { it.userId == currentLabourer.uid && it.date == todayDate }
    }

    val myTodayTrips = remember(trips, todayDate, currentLabourer.uid) {
        trips.filter {
            it.date == todayDate &&
                    it.labourerIds.contains(currentLabourer.uid) &&
                    it.status == TripStatus.ACTIVE
        }
    }

    val myTodayAccruedPaise = remember(myTodayTrips, currentLabourer.uid) {
        myTodayTrips.sumOf { it.sharesPaise[currentLabourer.uid] ?: 0L }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Namaste, ${currentLabourer.fullName}",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Labourer Operational View (Read-Only)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Attendance card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("labourer_attendance_card")
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Today's Attendance Status", style = MaterialTheme.typography.labelMedium)
                    Text(todayDate, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusBadge(todayAttendance?.status?.name ?: "PRESENT")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MoneyCard(
                title = "Today's Accrued",
                paise = myTodayAccruedPaise,
                subtitle = "Accrued operational summary",
                modifier = Modifier.weight(1f).testTag("labourer_today_accrued_card")
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Trips Worked", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${myTodayTrips.size}",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = BrandOrange
                    )
                    Text("Participating trips today", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Today's Participating Trips", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))

        if (myTodayTrips.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No participating trips recorded yet today.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            myTodayTrips.forEach { trip ->
                TripRowItem(trip = trip, onVoidClick = null)
            }
        }
    }
}

@Composable
fun LabourerAccruedScreen(
    repository: SandWorksRepository,
    currentLabourer: User
) {
    val trips by repository.trips.collectAsState()
    val myTrips = remember(trips, currentLabourer.uid) {
        trips.filter {
            it.labourerIds.contains(currentLabourer.uid) && it.status == TripStatus.ACTIVE
        }
    }
    val totalLifetimeAccrued = remember(myTrips, currentLabourer.uid) {
        myTrips.sumOf { it.sharesPaise[currentLabourer.uid] ?: 0L }
    }
    val byDate = remember(myTrips) { myTrips.groupBy { it.date } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Daily Accrued-Money Summary", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
        Text(
            "Displays accrued operational earnings in integer paise. Note: Represents accrued operational allocation, not a direct payout.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        MoneyCard(
            title = "Total Lifetime Accrued",
            paise = totalLifetimeAccrued,
            subtitle = "From ${myTrips.size} trips participated"
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Date-wise Accrual History", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(10.dp))

        if (byDate.isEmpty()) {
            Text("No accrued trip records found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            byDate.forEach { (date, list) ->
                val dayPaise = list.sumOf { it.sharesPaise[currentLabourer.uid] ?: 0L }
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
                            Text("${list.size} trips completed", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            MoneyEngine.formatPaise(dayPaise),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BrandOrange
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LabourerAttendanceScreen(
    repository: SandWorksRepository,
    currentLabourer: User
) {
    val attendance by repository.attendance.collectAsState()
    val myAttendance = remember(attendance, currentLabourer.uid) {
        attendance.filter { it.userId == currentLabourer.uid }.sortedByDescending { it.date }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Attendance History", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
        Text("Maintained by owner Ramesh Sahu.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(16.dp))

        if (myAttendance.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No past attendance records marked yet.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(myAttendance) { record ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(record.date, fontWeight = FontWeight.Bold)
                                if (record.reason.isNotBlank()) {
                                    Text("Note: ${record.reason}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            StatusBadge(record.status.name)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LabourerLeaderboardScreen(
    repository: SandWorksRepository,
    currentLabourer: User
) {
    val trips by repository.trips.collectAsState()
    var selectedPeriod by remember { mutableStateOf("WEEKLY") }
    var isRefreshing by remember { mutableStateOf(false) }

    val leaderboard = remember(trips, selectedPeriod) {
        repository.getLeaderboard(selectedPeriod)
    }

    // Full participant ranking for own rank detection
    val windowMillis = if (selectedPeriod == "WEEKLY") 7 * 24 * 3600 * 1000L else 30 * 24 * 3600 * 1000L
    val now = System.currentTimeMillis()
    val allLabourerCounts = remember(trips, selectedPeriod) {
        val activeTrips = trips.filter { it.status == TripStatus.ACTIVE && (now - it.timestamp) <= windowMillis }
        val counts = mutableMapOf<String, Int>()
        activeTrips.forEach { trip ->
            trip.labourerIds.forEach { id ->
                counts[id] = (counts[id] ?: 0) + 1
            }
        }
        counts.toList().sortedByDescending { it.second }
    }

    val myRankIndex = allLabourerCounts.indexOfFirst { it.first == currentLabourer.uid }
    val myTripCount = if (myRankIndex >= 0) allLabourerCounts[myRankIndex].second else 0

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Team Leaderboard", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                Text("Top verified labourers based on completed trips", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = {
                isRefreshing = true
                isRefreshing = false
            }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh Leaderboard", tint = BrandOrange)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Period Switcher: Weekly / Monthly
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedPeriod == "WEEKLY",
                onClick = { selectedPeriod = "WEEKLY" },
                label = { Text("Weekly (Last 7 Days)") },
                leadingIcon = {
                    if (selectedPeriod == "WEEKLY") {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandSandGold)
                    }
                }
            )
            FilterChip(
                selected = selectedPeriod == "MONTHLY",
                onClick = { selectedPeriod = "MONTHLY" },
                label = { Text("Monthly (Last 30 Days)") },
                leadingIcon = {
                    if (selectedPeriod == "MONTHLY") {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(16.dp), tint = BrandSandGold)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Own Rank Summary Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = BrandSandGold.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Your Standing", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = BrandSandGoldDark)
                    Text(
                        if (myRankIndex >= 0) "Rank #${myRankIndex + 1} of ${allLabourerCounts.size} members"
                        else "No trips recorded in this period",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    "$myTripCount trips",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandOrange
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Top 3 Performers", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(8.dp))

        if (leaderboard.topLabourers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No trips logged for this $selectedPeriod period.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Rankings update in real-time as trips are verified.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(leaderboard.topLabourers) { index, participant ->
                    val isMe = participant.id == currentLabourer.uid
                    val rankBadge = when (index) {
                        0 -> "🥇 1st"
                        1 -> "🥈 2nd"
                        2 -> "🥉 3rd"
                        else -> "#${index + 1}"
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMe) BrandSandGold.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    rankBadge,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    modifier = Modifier.width(60.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isMe) "${participant.name} (You)" else participant.name,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("Verified trip contributor", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(
                                "${participant.count} trips",
                                fontWeight = FontWeight.Bold,
                                color = BrandOrange
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LabourerProfileScreen(
    currentLabourer: User,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(72.dp), tint = BrandOrange)
        Spacer(modifier = Modifier.height(16.dp))
        Text(currentLabourer.fullName, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
        Text("Role: LABOURER • Verified Team Member", color = BrandSandGold, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Email: ${currentLabourer.email} • Phone: ${currentLabourer.phone}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSignOut,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Sign Out", color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
