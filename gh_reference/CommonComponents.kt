package com.roshan.sandworks.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roshan.sandworks.R
import com.roshan.sandworks.domain.MoneyEngine
import com.roshan.sandworks.model.*
import com.roshan.sandworks.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SandWorksTopBar(
    title: String,
    role: Role? = null,
    onSignOutClick: (() -> Unit)? = null,
    onNotificationsClick: (() -> Unit)? = null,
    onHelpClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    unreadAlertCount: Int = 0
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sand_works_icon),
                    contentDescription = "SAND WORKS Logo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (role != null) {
                        Text(
                            text = "ROLE: ${role.name}",
                            style = MaterialTheme.typography.labelMedium,
                            color = BrandOrange
                        )
                    }
                }
            }
        },
        actions = {
            if (onNotificationsClick != null) {
                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier.testTag("notifications_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadAlertCount > 0) {
                                Badge { Text("$unreadAlertCount") }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = if (unreadAlertCount > 0) SemanticError else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            if (onHelpClick != null) {
                IconButton(
                    onClick = onHelpClick,
                    modifier = Modifier.testTag("help_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About & Help",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (onSettingsClick != null) {
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (onSignOutClick != null) {
                IconButton(
                    onClick = onSignOutClick,
                    modifier = Modifier.testTag("sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Sign Out",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}


@Composable
fun OwnerBottomNav(
    currentScreen: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        val items = listOf(
            Triple("dashboard", "Home", Icons.Default.Home),
            Triple("trips", "Trips", Icons.Default.LocalShipping),
            Triple("people", "People", Icons.Default.People),
            Triple("tractors", "Tractors", Icons.Default.Agriculture),
            Triple("accrual", "Accrual", Icons.Default.AccountBalance)
        )
        items.forEach { (route, label, icon) ->
            NavigationBarItem(
                selected = currentScreen == route,
                onClick = { onNavigate(route) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrandOrange,
                    selectedTextColor = BrandOrange,
                    indicatorColor = BrandOrange.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_$route")
            )
        }
    }
}

@Composable
fun DriverBottomNav(
    currentScreen: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        val items = listOf(
            Triple("dashboard", "Home", Icons.Default.Home),
            Triple("add_trip", "Log Trip", Icons.Default.AddCircle),
            Triple("history", "My Trips", Icons.Default.History),
            Triple("accrued", "Earnings", Icons.Default.Payments),
            Triple("share", "Share", Icons.Default.Share)
        )
        items.forEach { (route, label, icon) ->
            NavigationBarItem(
                selected = currentScreen == route,
                onClick = { onNavigate(route) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrandOrange,
                    selectedTextColor = BrandOrange,
                    indicatorColor = BrandOrange.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_$route")
            )
        }
    }
}

@Composable
fun LabourerBottomNav(
    currentScreen: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        val items = listOf(
            Triple("dashboard", "Home", Icons.Default.Home),
            Triple("accrued", "Accrued", Icons.Default.AccountBalanceWallet),
            Triple("attendance", "Attendance", Icons.Default.DateRange),
            Triple("leaderboard", "Top Team", Icons.Default.Leaderboard),
            Triple("profile", "Profile", Icons.Default.Person)
        )
        items.forEach { (route, label, icon) ->
            NavigationBarItem(
                selected = currentScreen == route,
                onClick = { onNavigate(route) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrandOrange,
                    selectedTextColor = BrandOrange,
                    indicatorColor = BrandOrange.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_$route")
            )
        }
    }
}

@Composable
fun EmergencyAlertBanner(
    alert: EmergencyAlert,
    onAcknowledge: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = SemanticError.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .border(1.dp, SemanticError, RoundedCornerShape(12.dp))
            .testTag("emergency_alert_banner")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Urgent Warning",
                tint = SemanticError,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "URGENT ALERT: ${alert.title}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SemanticError
                )
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "From ${alert.senderName} • Android Compliant High-Priority Notice",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onAcknowledge,
                colors = ButtonDefaults.buttonColors(containerColor = SemanticError),
                modifier = Modifier.testTag("ack_alert_button")
            ) {
                Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MoneyCard(
    title: String,
    paise: Long,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = MoneyEngine.formatPaise(paise),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = BrandOrange
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status.uppercase()) {
        "ACTIVE", "PRESENT" -> SemanticSuccess.copy(alpha = 0.2f) to SemanticSuccess
        "PENDING" -> SemanticWarning.copy(alpha = 0.2f) to SemanticWarning
        "REJECTED", "SUSPENDED", "VOIDED", "ABSENT" -> SemanticError.copy(alpha = 0.2f) to SemanticError
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector = Icons.Default.Inbox,
    title: String = "No Records Found",
    message: String = "There are no items matching the current view or filter criteria.",
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp)
            .testTag("empty_state_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                modifier = Modifier.testTag("empty_state_action_button")
            ) {
                Text(actionLabel, color = Color.White)
            }
        }
    }
}

@Composable
fun LoadingStateView(
    message: String = "Loading data...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp)
            .testTag("loading_state_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = BrandOrange,
            modifier = Modifier.size(44.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ErrorStateView(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SemanticError.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(1.dp, SemanticError.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .testTag("error_state_view")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Error",
                tint = SemanticError,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Unable to complete request",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SemanticError
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = SemanticError),
                modifier = Modifier.testTag("retry_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Retry Operation", color = Color.White)
            }
        }
    }
}

@Composable
fun OfflineNoticeBanner(
    isOffline: Boolean,
    onReconnectClick: () -> Unit
) {
    if (isOffline) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SemanticWarning.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = SemanticWarning)
                .testTag("offline_notice_banner")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "Offline Mode",
                        tint = SemanticWarning,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Working Offline • Tap Retry to reconnect",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                TextButton(onClick = onReconnectClick) {
                    Text("Retry", color = BrandOrange, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
