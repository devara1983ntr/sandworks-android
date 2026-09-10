package com.roshan.sandworks.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
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
fun ReportsScreen(
    viewModel: SandWorksViewModel,
    onNavigateBack: () -> Unit,
    onShareText: (String) -> Unit
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dailySummary by viewModel.dailySummary.collectAsState()
    val matrix by viewModel.participationMatrix.collectAsState()
    val tractors by viewModel.activeTractors.collectAsState()

    var showShareWarningDialog by remember { mutableStateOf(false) }
    var showCsvDialog by remember { mutableStateOf(false) }
    var csvContent by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports & Matrix", fontWeight = FontWeight.Bold) },
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

            // Export Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showShareWarningDialog = true },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Summary")
                    }

                    OutlinedButton(
                        onClick = {
                            csvContent = viewModel.generateCsv()
                            showCsvDialog = true
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandSandGold),
                        border = BorderStroke(1.dp, BrandSlate)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export CSV")
                    }
                }
            }

            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                    border = BorderStroke(1.dp, BrandSlate)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "DAILY REPORT: $selectedDate",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandSandGold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Active Trips Loaded:")
                            Text("${dailySummary.activeTrips}", fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Gross Sand Pool:")
                            Text(MoneyEngine.formatPaise(dailySummary.grossPaise), fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Distributed:")
                            Text(MoneyEngine.formatPaise(dailySummary.distributedPaise), fontWeight = FontWeight.Bold, color = SemanticSuccess)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Undistributed Remaining:")
                            Text(MoneyEngine.formatPaise(dailySummary.remainingPaise), fontWeight = FontWeight.Bold, color = BrandSandGold)
                        }
                    }
                }
            }

            // Participation Matrix Header
            item {
                Text(
                    text = "PARTICIPATION MATRIX (LOADER × TRACTOR)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandSteel,
                    letterSpacing = 1.sp
                )
            }

            if (matrix.isEmpty()) {
                item {
                    Text("No loader trip participations recorded for today.", color = BrandMuted, fontSize = 13.sp)
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                        border = BorderStroke(1.dp, BrandSlate)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(12.dp)
                        ) {
                            // Table Header Row
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text("Loader", fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp), color = BrandSandGold)
                                tractors.forEach { t ->
                                    Text(t.name, fontWeight = FontWeight.Bold, modifier = Modifier.width(70.dp), color = BrandOffWhite)
                                }
                                Text("Total", fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp), color = BrandOrange)
                            }
                            Divider(color = BrandSlate)

                            // Rows
                            matrix.forEach { row ->
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(row.person.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(110.dp), color = BrandOffWhite)
                                    tractors.forEach { t ->
                                        val count = row.tractorCounts[t.id] ?: 0
                                        Text(
                                            text = if (count > 0) "$count" else "-",
                                            modifier = Modifier.width(70.dp),
                                            color = if (count > 0) BrandOffWhite else BrandSteel
                                        )
                                    }
                                    Text("${row.totalTrips}", fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp), color = BrandOrange)
                                }
                                Divider(color = BrandSlate.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    if (showShareWarningDialog) {
        AlertDialog(
            onDismissRequest = { showShareWarningDialog = false },
            title = { Text("Share Confirmation") },
            text = {
                Text("This summary contains financial and worker trip details. Only share with authorized supervisors or tractor owners.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showShareWarningDialog = false
                        val text = viewModel.generateShareSummaryText("FULL")
                        onShareText(text)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                ) {
                    Text("Proceed to Share")
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareWarningDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCsvDialog) {
        AlertDialog(
            onDismissRequest = { showCsvDialog = false },
            title = { Text("CSV Export Ready") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Formatted CSV data for $selectedDate:")
                    Surface(
                        color = BrandDeepCharcoal,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)
                    ) {
                        Text(
                            text = csvContent,
                            fontSize = 11.sp,
                            color = BrandMuted,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCsvDialog = false
                        onShareText(csvContent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                ) {
                    Text("Share CSV Text")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCsvDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}
