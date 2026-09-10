package com.roshan.sandworks.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roshan.sandworks.R
import com.roshan.sandworks.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About SAND WORKS", fontWeight = FontWeight.Bold) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = painterResource(id = R.drawable.sand_works_logo),
                contentDescription = "Logo",
                modifier = Modifier.height(64.dp)
            )
            Text(
                text = "SAND WORKS",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = BrandOffWhite
            )
            Text(
                text = "Version 1.0.0 (Build 2026.09)",
                fontSize = 13.sp,
                color = BrandMuted
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = BrandGraphite),
                border = BorderStroke(1.dp, BrandSlate)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("INDUSTRIAL INTEGRITY DIRECTIVE", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrandSandGold)
                    Text(
                        "SAND WORKS manages sand-loading trip distribution, labour and driver participation, deterministic whole-rupee math, daily reconciliation, and local device data safety.",
                        fontSize = 13.sp,
                        color = BrandOffWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Built offline-first with zero external cloud dependencies for 100% operational continuity on rural riverbanks and quarry sites.",
                        fontSize = 12.sp,
                        color = BrandMuted
                    )
                }
            }
        }
    }
}
