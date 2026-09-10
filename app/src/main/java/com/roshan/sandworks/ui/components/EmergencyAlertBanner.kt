package com.roshan.sandworks.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roshan.sandworks.data.local.EmergencyAlertEntity
import com.roshan.sandworks.ui.theme.*

@Composable
fun EmergencyAlertBanner(
    alert: EmergencyAlertEntity,
    onAcknowledge: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isCritical = alert.severity == "CRITICAL"
    val containerBg = if (isCritical) SemanticError.copy(alpha = 0.9f) else BrandOrange.copy(alpha = 0.95f)
    val contentColor = BrandOffWhite

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("emergency_alert_banner"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = BorderStroke(1.dp, if (isCritical) SemanticError else BrandSandGold)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Alert",
                    tint = contentColor,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isCritical) "CRITICAL ALERT" else "OPERATIONAL NOTICE",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = contentColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${alert.senderName}",
                            fontSize = 11.sp,
                            color = contentColor.copy(alpha = 0.8f)
                        )
                    }
                    Text(
                        text = alert.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = contentColor
                    )
                    if (alert.message.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = alert.message,
                            fontSize = 12.sp,
                            color = contentColor.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { onAcknowledge(alert.alertId) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandDarkSurface,
                    contentColor = BrandOffWhite
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("acknowledge_alert_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Got it", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
