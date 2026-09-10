package com.roshan.sandworks.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roshan.sandworks.domain.MoneyEngine
import com.roshan.sandworks.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCalculationRulesScreen(
    onNavigateBack: () -> Unit
) {
    var testLoaderCount by remember { mutableStateOf(3) }
    var testHalfShareCount by remember { mutableStateOf(0) }

    val simParticipants = remember(testLoaderCount, testHalfShareCount) {
        val list = mutableListOf<MoneyEngine.ParticipantInput>()
        val fullCount = (testLoaderCount - testHalfShareCount).coerceAtLeast(0)
        repeat(fullCount) { list.add(MoneyEngine.ParticipantInput("p$it", isHalfShare = false)) }
        repeat(testHalfShareCount.coerceAtMost(testLoaderCount)) { list.add(MoneyEngine.ParticipantInput("half$it", isHalfShare = true)) }
        list
    }

    val simResult = remember(simParticipants) {
        MoneyEngine.calculateTrip(MoneyEngine.DEFAULT_TRIP_RATE_PAISE, simParticipants)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculation Rules & Sandbox", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Rules Overview
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                    border = BorderStroke(1.dp, BrandSlate)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "CORE CALCULATION LAWS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandSandGold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "1. Fixed Trip Rate Pool: Default ₹200 (20,000 paise). The trip pool is snapshot at trip creation.",
                            fontSize = 13.sp,
                            color = BrandOffWhite
                        )
                        Text(
                            text = "2. Independent Events: Each trip is calculated independently. Unloading attendance doesn't affect loading trip share.",
                            fontSize = 13.sp,
                            color = BrandOffWhite
                        )
                        Text(
                            text = "3. Whole-Rupee Settlement Rule: Floor division to whole rupees (e.g. ₹200 / 3 = ₹66 each).",
                            fontSize = 13.sp,
                            color = BrandOffWhite
                        )
                        Text(
                            text = "4. Conservation Invariant: Trip Rate = Distributed + Remaining. Remaining money is accounted for and never lost.",
                            fontSize = 13.sp,
                            color = BrandOffWhite
                        )
                    }
                }
            }

            // Interactive Simulator
            item {
                Text(
                    text = "INTERACTIVE CALCULATION SANDBOX",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandSteel,
                    letterSpacing = 1.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                    border = BorderStroke(1.dp, BrandOrange.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Test Loader Count: $testLoaderCount loaders",
                            fontWeight = FontWeight.Bold,
                            color = BrandOffWhite
                        )
                        Slider(
                            value = testLoaderCount.toFloat(),
                            onValueChange = {
                                testLoaderCount = it.toInt()
                                if (testHalfShareCount > testLoaderCount) testHalfShareCount = testLoaderCount
                            },
                            valueRange = 1f..8f,
                            steps = 6,
                            colors = SliderDefaults.colors(thumbColor = BrandOrange, activeTrackColor = BrandOrange)
                        )

                        Divider(color = BrandSlate)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Full Share", fontSize = 11.sp, color = BrandMuted)
                                Text(
                                    MoneyEngine.formatPaise(simResult.baseSharePaise),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SemanticSuccess
                                )
                            }
                            Column {
                                Text("Distributed", fontSize = 11.sp, color = BrandMuted)
                                Text(
                                    MoneyEngine.formatPaise(simResult.distributedPaise),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandOffWhite
                                )
                            }
                            Column {
                                Text("Remaining", fontSize = 11.sp, color = BrandMuted)
                                Text(
                                    MoneyEngine.formatPaise(simResult.remainingPaise),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandSandGold
                                )
                            }
                        }

                        Text(
                            text = "Formula: ${MoneyEngine.formatPaise(MoneyEngine.DEFAULT_TRIP_RATE_PAISE)} ÷ $testLoaderCount loaders = ${MoneyEngine.formatPaise(simResult.baseSharePaise)} each (remainder: ${MoneyEngine.formatPaise(simResult.remainingPaise)})",
                            fontSize = 12.sp,
                            color = BrandMuted
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
