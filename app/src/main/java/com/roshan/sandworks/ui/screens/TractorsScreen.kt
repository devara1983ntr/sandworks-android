package com.roshan.sandworks.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roshan.sandworks.ui.SandWorksViewModel
import com.roshan.sandworks.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TractorsScreen(
    viewModel: SandWorksViewModel,
    onNavigateBack: () -> Unit
) {
    val tractors by viewModel.allTractors.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tractors & Fleet", fontWeight = FontWeight.Bold) },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BrandOrange,
                contentColor = BrandOffWhite
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Tractor")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            item {
                Text(
                    text = "REGISTERED TRACTORS (${tractors.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandSteel,
                    letterSpacing = 1.sp
                )
            }

            if (tractors.isEmpty()) {
                item {
                    Text("No tractors registered yet. Tap + to add Tractor 1, 2, 3.", color = BrandMuted)
                }
            } else {
                items(tractors) { tractor ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                        border = BorderStroke(1.dp, BrandSlate)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Agriculture, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(32.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tractor.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = BrandOffWhite
                                )
                                val details = listOf(tractor.brand, tractor.model, tractor.registrationIdentifier)
                                    .filter { it.isNotBlank() }
                                    .joinToString(" • ")
                                if (details.isNotBlank()) {
                                    Text(
                                        text = details,
                                        fontSize = 12.sp,
                                        color = BrandMuted
                                    )
                                }
                            }
                            Surface(
                                color = if (tractor.active) SemanticSuccess.copy(alpha = 0.2f) else BrandSlate,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (tractor.active) "Active" else "Inactive",
                                    fontSize = 11.sp,
                                    color = if (tractor.active) SemanticSuccess else BrandSteel,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(64.dp)) }
        }
    }

    if (showAddDialog) {
        var nameInput by remember { mutableStateOf("") }
        var brandInput by remember { mutableStateOf("") }
        var modelInput by remember { mutableStateOf("") }
        var regInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Tractor") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Display Name (e.g. Tractor 1)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = brandInput,
                        onValueChange = { brandInput = it },
                        label = { Text("Brand (e.g. Sonalika, John Deere)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = modelInput,
                        onValueChange = { modelInput = it },
                        label = { Text("Model (e.g. DI 745)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = regInput,
                        onValueChange = { regInput = it },
                        label = { Text("Registration Identifier (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            viewModel.addTractor(nameInput, brandInput, modelInput, "", regInput)
                            showAddDialog = false
                        }
                    },
                    enabled = nameInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
