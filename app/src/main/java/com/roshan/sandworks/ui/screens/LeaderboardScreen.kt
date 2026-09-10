package com.roshan.sandworks.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roshan.sandworks.domain.MoneyEngine
import com.roshan.sandworks.ui.SandWorksViewModel
import com.roshan.sandworks.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: SandWorksViewModel,
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Weekly, 1 = Monthly Top 3
    val weeklyEntries by viewModel.weeklyLeaderboard.collectAsState()
    val monthlyEntries by viewModel.monthlyLeaderboard.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trip Leaderboard", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = BrandGraphite,
                contentColor = BrandOrange
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Weekly (Mon–Sun)") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Monthly (Top 3)") }
                )
            }

            val currentList = if (selectedTab == 0) weeklyEntries else monthlyEntries

            if (currentList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = BrandSteel, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No qualifying trips recorded for this period", color = BrandMuted)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = if (selectedTab == 0) "RANKED BY QUALIFYING TRIPS LOADED" else "CALENDAR MONTH TOP 3 HONOURS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandSandGold,
                            letterSpacing = 1.sp
                        )
                    }

                    items(currentList) { entry ->
                        val medalColor = when (entry.rank) {
                            1 -> BrandSandGold
                            2 -> BrandSteel
                            3 -> BrandDeepOrange
                            else -> BrandSteel
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                            border = BorderStroke(
                                if (entry.rank == 1) 2.dp else 1.dp,
                                if (entry.rank == 1) BrandSandGold.copy(alpha = 0.6f) else BrandSlate
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        color = medalColor.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "#${entry.rank}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = medalColor,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = entry.person.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = BrandOffWhite
                                        )
                                        Text(
                                            text = "${entry.fullTrips} full + ${entry.halfTrips} half trips",
                                            fontSize = 12.sp,
                                            color = BrandMuted
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${entry.qualifyingTrips} trips",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = BrandOrange
                                    )
                                    Text(
                                        text = MoneyEngine.formatPaise(entry.totalEarnedPaise),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SemanticSuccess
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
