package com.roshan.sandworks.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.roshan.sandworks.domain.MoneyEngine
import com.roshan.sandworks.domain.model.Person
import com.roshan.sandworks.domain.model.PersonSummary
import com.roshan.sandworks.domain.model.PersonType
import com.roshan.sandworks.ui.SandWorksViewModel
import com.roshan.sandworks.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen(
    viewModel: SandWorksViewModel,
    onNavigateToDriverTotals: () -> Unit = {},
    onNavigateToLabourerDays: () -> Unit = {}
) {
    val allPeople by viewModel.allPeople.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Labourers, 1 = Drivers
    var searchQuery by remember { mutableStateOf("") }

    var showAddPersonDialog by remember { mutableStateOf(false) }
    var selectedPersonForDetails by remember { mutableStateOf<Person?>(null) }

    val filteredPeople = remember(allPeople, selectedTab, searchQuery) {
        val targetType = if (selectedTab == 0) PersonType.LABOURER else PersonType.DRIVER
        allPeople.filter {
            it.type == targetType &&
            (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("People & Labourers", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandDeepCharcoal,
                    titleContentColor = BrandOffWhite
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddPersonDialog = true },
                containerColor = BrandOrange,
                contentColor = BrandOffWhite
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Person")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = BrandGraphite,
                contentColor = BrandOrange
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Labourers (${allPeople.count { it.type == PersonType.LABOURER }})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Drivers (${allPeople.count { it.type == PersonType.DRIVER }})") }
                )
            }

            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Search")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(12.dp)
            )

            if (filteredPeople.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.People, contentDescription = null, tint = BrandSteel, modifier = Modifier.size(52.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matches for \"$searchQuery\"" else "No ${if (selectedTab == 0) "labourers" else "drivers"} registered yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = BrandOffWhite
                        )
                        if (searchQuery.isNotBlank()) {
                            OutlinedButton(onClick = { searchQuery = "" }) {
                                Text("Clear Search")
                            }
                        } else {
                            Text(
                                text = "Add workers to track their trips loaded, attendance, and wage calculations.",
                                fontSize = 12.sp,
                                color = BrandMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Button(
                                onClick = {
                                    typeInput = if (selectedTab == 0) PersonType.LABOURER else PersonType.DRIVER
                                    showAddPersonDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add ${if (selectedTab == 0) "Labourer" else "Driver"}")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredPeople) { person ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPersonForDetails = person },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                            border = BorderStroke(1.dp, BrandSlate)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        if (person.type == PersonType.DRIVER) Icons.Default.DirectionsCar else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (person.active) BrandOrange else BrandSteel
                                    )
                                    Column {
                                        Text(
                                            text = person.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (person.active) BrandOffWhite else BrandMuted
                                        )
                                        Text(
                                            text = if (person.active) "Active" else "Inactive",
                                            fontSize = 11.sp,
                                            color = if (person.active) SemanticSuccess else BrandSteel
                                        )
                                    }
                                }

                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = BrandSteel)
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(64.dp)) }
                }
            }
        }
    }

    // Add Person Dialog
    if (showAddPersonDialog) {
        var nameInput by remember { mutableStateOf("") }
        var typeInput by remember { mutableStateOf(if (selectedTab == 0) PersonType.LABOURER else PersonType.DRIVER) }

        AlertDialog(
            onDismissRequest = { showAddPersonDialog = false },
            title = { Text("Add Person") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = typeInput == PersonType.LABOURER,
                            onClick = { typeInput = PersonType.LABOURER },
                            label = { Text("Labourer") }
                        )
                        FilterChip(
                            selected = typeInput == PersonType.DRIVER,
                            onClick = { typeInput = PersonType.DRIVER },
                            label = { Text("Driver") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            viewModel.addPerson(nameInput, typeInput)
                            showAddPersonDialog = false
                        }
                    },
                    enabled = nameInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPersonDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Person Details & Ledger Dialog
    selectedPersonForDetails?.let { person ->
        val coroutineScope = rememberCoroutineScope()
        var summary by remember { mutableStateOf<PersonSummary?>(null) }

        LaunchedEffect(person.id) {
            summary = viewModel.getPersonSummary(person.id)
        }

        AlertDialog(
            onDismissRequest = { selectedPersonForDetails = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(person.name, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Role: ${person.type.name}")
                    Text("Status: ${if (person.active) "Active" else "Inactive"}")
                    Divider(color = BrandSlate)

                    if (summary != null) {
                        Text("CAREER TOTALS & EARNINGS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrandSandGold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Trips Loaded:")
                            Text("${summary!!.totalTrips}", fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Earned:")
                            Text(MoneyEngine.formatPaise(summary!!.totalEarnedPaise), fontWeight = FontWeight.Bold, color = SemanticSuccess)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Working Days:")
                            Text("${summary!!.workingDays}", fontWeight = FontWeight.Bold)
                        }
                        Divider(color = BrandSlate)
                        Text("Why this amount?", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(
                            "Every rupee is traced directly to the exact trips loaded, calculated with whole-rupee floor rules with no arbitrary estimations.",
                            fontSize = 11.sp,
                            color = BrandMuted
                        )
                        Divider(color = BrandSlate)
                        Button(
                            onClick = {
                                viewModel.selectPerson(person.id)
                                selectedPersonForDetails = null
                                if (person.type == PersonType.DRIVER) {
                                    onNavigateToDriverTotals()
                                } else {
                                    onNavigateToLabourerDays()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                        ) {
                            Icon(
                                if (person.type == PersonType.DRIVER) Icons.Default.DirectionsCar else Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (person.type == PersonType.DRIVER) "Open Driver Workstation & Shifts" else "Open Labourer Days & Earnings")
                        }
                    } else {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        viewModel.togglePersonActive(person)
                        selectedPersonForDetails = null
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (person.active) SemanticError else SemanticSuccess
                    ),
                    border = BorderStroke(1.dp, if (person.active) SemanticError else SemanticSuccess)
                ) {
                    Text(if (person.active) "Deactivate" else "Activate")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPersonForDetails = null }) {
                    Text("Close")
                }
            }
        )
    }
}
