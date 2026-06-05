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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.corecoders.gymbuddy.viewmodel.SettingsViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingsViewModel) {
    val scrollState = rememberScrollState()
    val darkMode by viewModel.darkMode.collectAsState()
    val unitSystem by viewModel.unitSystemMetric.collectAsState()
    val autoComplete by viewModel.autoCompleteSet.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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
                SettingsSwitchRow(
                    icon = Icons.Outlined.DarkMode,
                    title = "Dark Mode",
                    checked = darkMode,
                    onCheckedChange = { viewModel.toggleDarkMode(it) }
                )
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))
                SettingsSwitchRow(
                    icon = Icons.Outlined.Language,
                    title = "Unit System",
                    subtitle = if (unitSystem) "Metric (kg, km)" else "Imperial (lb, mi)",
                    checked = unitSystem,
                    onCheckedChange = { viewModel.toggleUnitSystem(it) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- WORKOUT ---
            SettingsGroupHeader("WORKOUT")
            SettingsCard {
                SettingsSwitchRow(
                    icon = Icons.Outlined.CheckCircle,
                    title = "Auto Complete Set",
                    subtitle = "Log sets on numeric entry",
                    checked = autoComplete,
                    onCheckedChange = { viewModel.toggleAutoCompleteSet(it) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- SIGN OUT BUTTON ---
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
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
                    Text("Sign Out", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp)
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
        color = MaterialTheme.colorScheme.secondary,
        style = MaterialTheme.typography.labelSmall,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(start = 12.dp, bottom = 12.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
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
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
            Text(text = subtitle, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
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
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
            if (subtitle != null) {
                Text(text = subtitle, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
