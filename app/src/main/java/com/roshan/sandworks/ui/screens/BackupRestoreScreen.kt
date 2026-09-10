package com.roshan.sandworks.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.roshan.sandworks.ui.SandWorksViewModel
import com.roshan.sandworks.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    viewModel: SandWorksViewModel,
    onNavigateBack: () -> Unit,
    onShareText: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var backupJson by remember { mutableStateOf<String?>(null) }
    var restoreInput by remember { mutableStateOf("") }
    var restoreStatusMessage by remember { mutableStateOf<String?>(null) }
    var isErrorStatus by remember { mutableStateOf(false) }

    var showConfirmRestoreDialog by remember { mutableStateOf(false) }
    var showConfirmClearDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Data Safety", fontWeight = FontWeight.Bold) },
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

            // Local Data Safety Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                    border = BorderStroke(1.dp, BrandSandGold.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = BrandSandGold, modifier = Modifier.size(28.dp))
                        Column {
                            Text("100% OFFLINE DATA PRIVACY", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrandSandGold)
                            Text(
                                "All records are stored securely in local SQLite database on your device. Export a backup regularly to keep records safe.",
                                fontSize = 12.sp,
                                color = BrandMuted
                            )
                        }
                    }
                }
            }

            // Export Backup Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                    border = BorderStroke(1.dp, BrandSlate)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("CREATE BACKUP SNAPSHOT", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrandSteel)
                        Text(
                            "Generates a complete JSON backup containing all trips, calculations, participants, fleet tractors, labourers, attendance, closures, and audit logs.",
                            fontSize = 12.sp,
                            color = BrandMuted
                        )

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val json = viewModel.createBackupJson()
                                    backupJson = json
                                    onShareText(json)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export & Share Full Backup")
                        }

                        if (backupJson != null) {
                            Text(
                                "Backup successfully generated (${backupJson!!.length} bytes).",
                                fontSize = 12.sp,
                                color = SemanticSuccess
                            )
                        }
                    }
                }
            }

            // Restore Backup Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                    border = BorderStroke(1.dp, BrandSlate)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("RESTORE FROM BACKUP", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrandSteel)
                        Text(
                            "Paste the JSON backup string below to restore all records. Existing database records will be replaced.",
                            fontSize = 12.sp,
                            color = BrandMuted
                        )

                        OutlinedTextField(
                            value = restoreInput,
                            onValueChange = { restoreInput = it },
                            placeholder = { Text("Paste JSON backup data here...") },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 180.dp),
                            shape = RoundedCornerShape(8.dp)
                        )

                        if (restoreStatusMessage != null) {
                            Text(
                                text = restoreStatusMessage!!,
                                fontSize = 12.sp,
                                color = if (isErrorStatus) SemanticError else SemanticSuccess
                            )
                        }

                        Button(
                            onClick = {
                                if (restoreInput.isNotBlank()) {
                                    showConfirmRestoreDialog = true
                                }
                            },
                            enabled = restoreInput.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandSandGold, contentColor = BrandDeepCharcoal)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Restore Database", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Danger Zone: Clear Data
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                    border = BorderStroke(1.dp, SemanticError.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("DANGER ZONE — RESET DATA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SemanticError)
                        Text(
                            "Wipe all local data from this device. Use with caution.",
                            fontSize = 12.sp,
                            color = BrandMuted
                        )
                        OutlinedButton(
                            onClick = { showConfirmClearDialog = true },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SemanticError),
                            border = BorderStroke(1.dp, SemanticError)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clear All Local Data")
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    if (showConfirmRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmRestoreDialog = false },
            title = { Text("Confirm Database Restore?") },
            text = {
                Text("Restoring will replace all current trips, attendances, and person records with the backup file data. Do you want to proceed?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmRestoreDialog = false
                        viewModel.restoreBackupJson(restoreInput) { result ->
                            result.onSuccess { msg ->
                                restoreStatusMessage = msg
                                isErrorStatus = false
                                restoreInput = ""
                            }.onFailure { err ->
                                restoreStatusMessage = "Restore failed: ${err.message}"
                                isErrorStatus = true
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                ) {
                    Text("Proceed & Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmRestoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showConfirmClearDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmClearDialog = false },
            title = { Text("Wipe All Data?") },
            text = {
                Text("This action cannot be undone. All trips, people, and fleet records will be permanently removed from this device.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmClearDialog = false
                        viewModel.clearAllData {
                            restoreStatusMessage = "All local data has been successfully wiped."
                            isErrorStatus = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SemanticError)
                ) {
                    Text("Permanently Wipe")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
