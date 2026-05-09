package com.corecoders.gymbuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.corecoders.gymbuddy.ui.theme.OLEDBlack
import com.corecoders.gymbuddy.ui.theme.SlateSurface
import com.corecoders.gymbuddy.ui.theme.SurgicalRed
import com.corecoders.gymbuddy.ui.theme.SurgicalDivider
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SETTINGS", style = MaterialTheme.typography.titleMedium, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OLEDBlack)
            )
        },
        containerColor = OLEDBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {

            // --- ACCOUNT ---
            SettingsGroupHeader("ACCOUNT")
            SettingsCard {
                SettingsNavRow(
                    icon = Icons.Outlined.Person,
                    title = "Profile",
                    subtitle = "Manage your physical metrics",
                    onClick = { /* TODO */ }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- PREFERENCES ---
            SettingsGroupHeader("PREFERENCES")
            SettingsCard {
                var darkMode by remember { mutableStateOf(true) }
                var unitSystem by remember { mutableStateOf(true) }

                SettingsSwitchRow(
                    icon = Icons.Outlined.DarkMode,
                    title = "Dark Mode",
                    checked = darkMode,
                    onCheckedChange = { darkMode = it }
                )
                HorizontalDivider(color = SurgicalDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))
                SettingsSwitchRow(
                    icon = Icons.Outlined.Language,
                    title = "Unit System",
                    subtitle = "Metric (kg, km)",
                    checked = unitSystem,
                    onCheckedChange = { unitSystem = it }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- WORKOUT ---
            SettingsGroupHeader("WORKOUT")
            SettingsCard {
                var autoComplete by remember { mutableStateOf(true) }
                SettingsSwitchRow(
                    icon = Icons.Outlined.CheckCircle,
                    title = "Auto Complete Set",
                    subtitle = "Log sets on numeric entry",
                    checked = autoComplete,
                    onCheckedChange = { autoComplete = it }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- SIGN OUT BUTTON ---
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, SurgicalDivider),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        Firebase.auth.signOut()
                        navController.navigate("login") { popUpTo(0) }
                    }
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sign Out", color = SurgicalRed, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun SettingsGroupHeader(title: String) {
    Text(
        text = title,
        color = Color.Gray,
        style = MaterialTheme.typography.labelSmall,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(start = 12.dp, bottom = 12.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, SurgicalDivider),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsNavRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = SurgicalRed, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.DarkGray)
    }
}

@Composable
fun SettingsSwitchRow(icon: ImageVector, title: String, subtitle: String? = null, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = SurgicalRed, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            if (subtitle != null) {
                Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SurgicalRed,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.DarkGray,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
